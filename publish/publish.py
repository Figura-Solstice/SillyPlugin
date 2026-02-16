import re
import traceback
from pathlib import Path
import asyncio
from json import dumps

from modrinth_api import ModrinthAPI

def prompt_yes_no(text: str, default: bool):
    default = default or False
    msg = text + " ["
    if default: msg += "Y/n]"
    else: msg += "y/N]"
    msg += " "
    res = input(msg)
    return default if res == "" else (res.lower() == "y")

OUTPUT_FOLDER = Path("../silly_out")
STAGING = prompt_yes_no("Staging?", True)
keyfile = Path("./mr_key.txt")
key = None
if keyfile.exists():
    key = keyfile.read_text().strip()
else:
    key = input("Modrinth API key?\n(psst... you can store it in mr_key.txt and it'll be automatically used...)\n-> ").strip()

filename_re = re.compile(r"([a-z]+)-(\d+\.\d+\.\d+)-(\d+\.\d+\.\d+)-([a-z]+).jar")


async def main():
    try:
        mr = ModrinthAPI(key, STAGING)
        await mr.setup()
        async with mr:
            api_info = await mr.hello()
            print(f"Connecting to {api_info.name}-{api_info.version}@{api_info.build_info.git_hash} on {api_info.build_info.profile}")
            SLUG = input("Modrinth project ID/slug?\n-> ")
            dry = prompt_yes_no("Dry run?", True)
            if dry or await mr.project_exists(SLUG):
                if prompt_yes_no("Clear versions?", False):
                    if prompt_yes_no("Are you sure??", False):
                        print("Clearing...")
                        if not dry: await mr.clear_versions(SLUG)
                for file in OUTPUT_FOLDER.iterdir():
                    content = file.read_bytes()
                    match = filename_re.match(file.name)
                    name = match.group(1)
                    version = match.group(2)
                    mc_version = match.group(3)
                    loader = match.group(4)
                    if prompt_yes_no(f"Upload {name}/{version}@{mc_version} for {loader}?", True):
                        print("UPLOADING:", name, version, mc_version, loader)
                        try:
                            if not dry: await mr.create_version(SLUG, mc_version, name + ".jar", content, version, dependencies=[],
                                                    loaders=[loader], name = f"Figura SillyPlugin {version}")
                            print("Successful!")
                        except Exception as e:
                            print("Unsuccessful: " + str(e))
                            if prompt_yes_no("Exit?", True): exit(1)
            else:
                print(f"Project {SLUG!r} does not exist!")
    except Exception as e:
        traceback.print_exception(e)



if __name__ == "__main__":
    asyncio.new_event_loop().run_until_complete(main())