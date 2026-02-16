import json

from aiohttp import ClientSession, ClientResponse, FormData
from yarl import URL

from api_types.ModrinthProject import ModrinthProject
from api_types.ModrinthHello import ModrinthHello


class ModrinthAPI:
    client: ClientSession
    __pat: str
    __staging: bool
    def __init__(self, pat: str, staging: bool):
        self.__pat = pat
        self.__staging = staging

    async def __aenter__(self):
        pass

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        await self.client.close()

    async def setup(self):
        url = "api.modrinth.com"
        if self.__staging: url = "staging-" + url
        client = ClientSession(f"https://{url}/v2/", headers={
            "Authorization": self.__pat
        })
        self.client = client

    async def __expect_ok(self, sess: ClientResponse):
        if not sess.ok:
            code = sess.status
            text = await sess.text()
            sess.close()
            if code == 404:
                raise Exception(f"404 at {sess.real_url}: {sess.reason}")
            raise Exception(f"Non-OK status returned from {sess.real_url}: {code}, {sess.reason} \n{text}")

    async def hello(self):
        async with self.client.get("/") as sess:
            await self.__expect_ok(sess)
            res = await sess.json()
            sess.close()
            return ModrinthHello(res)

    async def get_project(self, project_id) -> ModrinthProject:
        async with self.client.get(f"./project/{project_id}") as sess:
            await self.__expect_ok(sess)
            res = await sess.json()
            return ModrinthProject(res)

    async def project_exists(self, project_id):
        async with self.client.get(f"./project/{project_id}/check") as sess:
            return sess.ok

    async def list_versions(self, project_id):
        async with self.client.get(f"./project/{project_id}/version") as sess:
            await self.__expect_ok(sess)
            res = await sess.json()
            return res

    async def create_version(self, project_id: str, version: str, filename: str, file_bytes: bytes, mod_version: str, **kwargs):
        data = FormData()
        data.add_field("data", json.dumps({
            "name": filename,
            "version_number": mod_version,
            "game_versions": [
                version
            ],
            "version_type": "release",
            "project_id": project_id,
            "featured": False,
            "status": "listed",
            "file_parts": [
                "jarfile"
            ],
            "primary_file": "jarfile",
            **kwargs
        }))
        data.add_field("jarfile", file_bytes, filename=filename)
        async with self.client.post("./version", data=data) as sess:
            await self.__expect_ok(sess)
            return await sess.json()

    async def get_versions(self, project_id, include_changelog: bool = False):
        async with self.client.get(f"./project/{project_id}/version?include_changelog={str(include_changelog).lower()}") as sess:
            await self.__expect_ok(sess)
            return await sess.json()


    async def clear_versions(self, project_id):
        for version in await self.get_versions(project_id, False):
            async with self.client.delete(f"./version/{version['id']}") as sess:
                # await self.__expect_ok(sess)
                pass # seems like the DELETE /version/{id} endpoint gives a 500 after success on staging??????