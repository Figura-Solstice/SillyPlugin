plugins {
    id("dev.isxander.modstitch.base") version "0.5.12"
}

fun prop(name: String, consumer: (prop: String) -> Unit) {
    (findProperty(name) as? String?)
        ?.let(consumer)
}

val minecraft = property("deps.minecraft") as String;

modstitch {
    minecraftVersion = minecraft

    // Alternatively use stonecutter.eval if you have a lot of versions to target.
    // https://stonecutter.kikugie.dev/stonecutter/guide/setup#checking-versions
    javaTarget = when (minecraft) {
        "1.20.1" -> 17
        "1.21.1" -> 21
        "1.21.4" -> 21
        else -> throw IllegalArgumentException("Please store the java version for ${property("deps.minecraft")} in build.gradle.kts!")
    }

    // If parchment doesnt exist for a version yet you can safely
    // omit the "deps.parchment" property from your versioned gradle.properties
    parchment {
        prop("deps.parchment") { mappingsVersion = it }
    }

    // This metadata is used to fill out the information inside
    // the metadata files found in the templates folder.
    metadata {
        modId = "sillyplugin"
        modName = "Figura Silly Plugin"
        modVersion = property("silly_version") as String?
        modGroup = "dev.celestial"
        modAuthor = "Niko Solstice"
        modDescription = "Spiritual successor of GoofyPlugin."
        val creds = arrayOf(
            "TheKillerBunny: for making GoofyPlugin",
            "Sam ★: Being so cool fr",
            "cosmic_the_cat: Creating the icon for the plugin",
            "Somataru: French translations"
        )
        modCredits = creds.joinToString("\n");
        modLicense = "MIT"

        fun <K: Any, V: Any> MapProperty<K, V>.populate(block: MapProperty<K, V>.() -> Unit) {
            block()
        }

        replacementProperties.populate {
            // You can put any other replacement properties/metadata here that
            // modstitch doesn't initially support. Some examples below.
            put("mod_issue_tracker", "https://github.com/figura-solstice/sillyplugin")
            put("pack_format", when (property("deps.minecraft")) {
                "1.20.1" -> 15
                "1.21.1" -> 34
                "1.21.4" -> 46
                else -> throw IllegalArgumentException("Please store the resource pack version for ${property("deps.minecraft")} in build.gradle.kts! https://minecraft.wiki/w/Pack_format")
            }.toString())
        }
    }

    // Fabric Loom (Fabric)
    loom {
        // It's not recommended to store the Fabric Loader version in properties.
        // Make sure its up to date.
        fabricLoaderVersion = "0.16.10"
        // Configure loom like normal in this block.
        configureLoom {
            val aw = project.file("src/main/resources/sillyplugin.accesswidener")
            if (aw.exists())
                accessWidenerPath = aw
        }
    }

    // ModDevGradle (NeoForge, Forge, Forgelike)
    moddevgradle {
        enable {
            prop("deps.forge") { forgeVersion = it }
            prop("deps.neoform") { neoFormVersion = it }
            prop("deps.neoforge") { neoForgeVersion = it }
            prop("deps.mcp") { mcpVersion = it }
        }

        // Configures client and server runs for MDG, it is not done by default
        defaultRuns()

        // This block configures the `neoforge` extension that MDG exposes by default,
        // you can configure MDG like normal from here
        configureNeoforge {
            val at = project.file("src/main/resources/META-INF/accesstransformer.cfg")
            if (at.exists())
                accessTransformers.from(at)

            runs.all {
                disableIdeRun()
            }
        }
    }

    mixin { // re-enable if modstitch fixes mixins being added multiple times
        // You do not need to specify mixins in any mods.json/toml file if this is set to
        // true, it will automatically be generated.
        addMixinsToModManifest = false

//        configs.register("sillyplugin")

        // Most of the time you wont ever need loader specific mixins.
        // If you do, simply make the mixin file and add it like so for the respective loader:
        // if (isLoom) configs.register("examplemod-fabric")
        // if (isModDevGradleRegular) configs.register("examplemod-neoforge")
        // if (isModDevGradleLegacy) configs.register("examplemod-forge")
    }
}

// Stonecutter constants for mod loaders.
// See https://stonecutter.kikugie.dev/stonecutter/guide/comments#condition-constants
var constraint: String = name.split("-")[1]
stonecutter {
    consts(
        "fabric" to constraint.equals("fabric"),
        "neoforge" to constraint.equals("neoforge"),
        "forge" to constraint.equals("forge"),
        "vanilla" to constraint.equals("vanilla")
    )
}

// All dependencies should be specified through modstitch's proxy configuration.
// Wondering where the "repositories" block is? Go to "stonecutter.gradle.kts"
// If you want to create proxy configurations for more source sets, such as client source sets,
// use the modstitch.createProxyConfigurations(sourceSets["client"]) function.
var luaj_version = property("luaj_version")
var nv_websocket_version = property("nv_websocket_version")
var figura_version = property("figura_version")
fun figura(loader: String): String {
    return "org.figuramc:figura-$loader:$figura_version+$minecraft"
}

var voicechat_api_version = property("voicechat_api_version")
var loader = "fabric";
if (modstitch.isModDevGradleRegular)
    loader = "neoforge"

var svc_version = "";
if (modstitch.isLoom) {
    svc_version = when (property("deps.minecraft")) {
        "1.20.1" -> "UiVFkKer"
        "1.21.1" -> "IttovdN3"
        "1.21.4" -> "B0SmLrhu"
        else -> throw IllegalArgumentException("Please store the fabric svc version for ${property("deps.minecraft")} in build.gradle.kts!");
    }
} else {
    svc_version = when (property("deps.minecraft")) {
        "1.21.1" -> "8xOu3Um5"
        "1.21.4" -> "5ERpmU4w"
        else -> throw IllegalArgumentException("Please store the neoforge svc version for ${property("deps.minecraft")} in build.gradle.kts!");
    }
}

dependencies {
    modstitch.loom {
        modstitchModImplementation("net.fabricmc.fabric-api:fabric-api:${property("deps.fapi")}+${minecraft}")
    }
//    modstitch.moddevgradle {
//    }
    modstitchModImplementation(figura(loader))

    // svc
    modstitchModImplementation("maven.modrinth:9eGKb6K1:${svc_version}")
    modstitchModImplementation("de.maxhenkel.voicechat:voicechat-api:${voicechat_api_version}")

    // Vivecraft official Mod API (since Vivecraft 1.3.0)
    var vivecraft_version = property("deps.vivecraft") as String
    modstitchModImplementation("maven.modrinth:vivecraft:${vivecraft_version}")


//    modstitchCompileOnly(figura("common-mojmap"))
    modstitchModImplementation("com.github.FiguraMC.luaj:luaj-core:$luaj_version-figura")
    modstitchModImplementation("com.github.FiguraMC.luaj:luaj-jse:$luaj_version-figura")
    modstitchModImplementation("com.neovisionaries:nv-websocket-client:$nv_websocket_version")
    modstitchModImplementation("com.pngencoder:pngencoder:0.16.0")
    modstitchJiJ("com.pngencoder:pngencoder:0.16.0")

}