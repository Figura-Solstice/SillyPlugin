"""
{
    "about": "Welcome traveler!",
    "build_info": {
        "comp_date": "2026-02-15 @ 09:37 PM (Etc/UTC)",
        "git_hash": "0f194690dfa173a15ec1d87a6467558134e73b5d",
        "profile": "release"
    },
    "documentation": "https://docs.modrinth.com",
    "name": "modrinth-labrinth",
    "version": "2.7.0"
}
"""
from datetime import datetime


class ModrinthHello:
    def __init__(self, res):
        self.about = res.pop("about")
        self.build_info = ModrinthBuildInfo(res.pop("build_info"))
        self.documentation = res.pop("documentation")
        self.name = res.pop("name")
        self.version = res.pop("version")

class ModrinthBuildInfo:
    def __init__(self, data):
        self.comp_date = data.pop("comp_date")
        self.git_hash = data.pop("git_hash")
        self.profile = data.pop("profile")