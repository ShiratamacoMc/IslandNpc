import com.typewritermc.loader.ExtensionFlag

repositories {
    maven("https://repo.bg-software.com/repository/api/")
    maven("https://maven.citizensnpcs.co/repo")
}

dependencies {
    compileOnly("com.bgsoftware:SuperiorSkyblockAPI:2025.1")
    compileOnly("net.citizensnpcs:citizens-main:2.0.35-SNAPSHOT") {
        exclude(group = "*", module = "*")
    }
}

typewriter {
    namespace = "magicbili"

    extension {
        name = "IslandNpc"
        shortDescription = "Island NPC integration for TypeWriter."
        description = """
            |The Island NPC Extension allows TypeWriter NPCs to be associated with specific islands.
            |This enables island-specific NPCs that can be referenced in dialogues and quests.
        """.trimMargin()
        
        engineVersion = file("../../version.txt").readText().trim()
        channel = com.typewritermc.moduleplugin.ReleaseChannel.NONE

        paper {
            dependency("SuperiorSkyblock2")
        }
    }
}
