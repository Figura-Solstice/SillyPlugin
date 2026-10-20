# SillyPlugin
Plugin for [Figura](https://modrinth.com/mod/figura), adding in random-yet-somewhat-useful features.
Successor to [Goofyplugin](https://modrinth.com/mod/figura-goofyplugin)
## Features
- Tracking memory and execution time used by scripts, and _what_ uses that memory.
- Proper handling for panic mode and avatar load/unload
- Proper permission handling for the more "cheaty" features.
- Utility features such as `:applyEmojis()`, `:getRawString()`, etc
- Goofyplugin's beloved movement functions, and also bumpscocity.
- The GUI_RENDER event, with a powerful API for drawing to the gui.
- Backports of features from Figura's main branch.
- Integrations with other mods, like [SVC](https://modrinth.com/mod/simple-voice-chat)
- An entire handler for creating persistent ghost blocks from lua, with proper cleanup.

## Maintained versions
| Minecraft | Loaders          |
|:---------:|:-----------------|
|  1.20.1   | Fabric           |
|  1.20.4   | Fabric           |
|  1.21.1   | Fabric, Neoforge |
|  1.21.4   | Fabric, Neoforge |

If there is a minecraft version you want Sillyplugin to work on, feel free to ping me (@niko.oneshot.real) and ask in one of the following places:
- [The official plugin post](https://discord.com/channels/1129805506354085959/1472393506931740672) (https://discord.gg/figuramc)
- [The discord](https://discord.gg/xcThU4geVh)

There are some exceptions:
- Only fabric and neoforge will be considered for loaders. Not lexforge (legacy forge), and not quilt (fabric should be compatible with quilt anyway, so it'd be extra work for no reason)
  - (If you have a really compelling reason, and are willing to implement it into the build system yourself, you may suggest another loader. As long as its not lexforge)
- Figura has to be stable on said version, alongside having a maven release. If you don't know what this means, just assume 1.16.5-1.21.4 is fair game, but not beyond either extreme.

## Contributing
If you wish to contribute, you're welcome to. Here's some tips:
- Where you'd normally run `./gradlew build` or something similar, instead run `./gradlew chiseledBuild`
  - better yet, use the build script, `build.sh` or `build.bat`
  - The build script will output all jars into the folder `silly_out`.
- Please **do not** refactor major parts of the codebase without at least checking with me first.
- My motivation to review may sway to the whims of factors beyond both our comprehension, so review times on PRs may be slow.

(repo based on the [Modstitch Stonecutter Template](https://github.com/isXander/modstitch-stonecutter-template))