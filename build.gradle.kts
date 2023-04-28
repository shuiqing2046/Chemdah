import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    `maven-publish`
    id("io.izzel.taboolib") version "1.51"
    id("org.jetbrains.kotlin.jvm") version "1.5.31"
    id("org.jetbrains.dokka") version "1.6.0"
}

taboolib {
    description {
        contributors {
            name("坏黑")
            name("PengLx")
            name("AmazingOcean")
            name("R-Josef")
            name("Galaxy-VN")
        }
        dependencies {
            name("Adyeshach")
        }
    }
    install("common")
    install("common-5")
    install("module-effect")
    install("module-database")
    // install("module-database-mongodb")
    install("module-configuration")
    install("module-kether")
    install("module-chat")
    install("module-lang")
    install("module-metrics")
    install("module-navigation")
    install("module-ai")
    install("module-nms")
    install("module-nms-util")
    install("module-ui")
    install("platform-bukkit")
    install("expansion-command-helper")
    install("expansion-javascript")
    classifier = null
    version = "6.0.10-115"
    relocate("ink.ptms.um", "ink.ptms.chemdah.um")
    options("keep-kotlin-module")
}

repositories {
    maven { url = uri("https://repo.pcgamingfreaks.at/repository/maven-everything") }
    maven { url = uri("https://jitpack.io") }
    mavenLocal()
    mavenCentral()
}

dependencies {
    // adyeshach
    compileOnly("ink.ptms.adyeshach:all:2.0.0-snapshot-4")

    taboo("ink.ptms:um:1.0.0-beta-23")
    compileOnly("ink.ptms:error_reporter:1.0.0")
    compileOnly("net.milkbowl.vault:Vault:1")
    compileOnly("org.serverct.ersha.dungeon:DungeonPlus:1.1.3")
    compileOnly("com.github.angeschossen:LandsAPI:5.13.0")
    compileOnly("at.pcgamingfreaks:MarriageMaster-API-Bukkit:2.4")
    compileOnly("me.badbones69:crazycrates-plugin:1.10")
    compileOnly("com.sk89q.worldedit:WorldEdit:7")
    compileOnly("public:FriendsAPI:1.1.0.9.1")
    compileOnly("public:QuickShop:4.0.9.1")
    compileOnly("public:nuvotifier:1.0.0")
    compileOnly("public:Jobs:1.0.0")
    compileOnly("public:even-more-fish:1.0.0")
    compileOnly("public:ChatReaction:1.0.0")
    compileOnly("public:Team:1.0.0:7")
    compileOnly("public:Team:1.0.0:9")
    compileOnly("public:Team:1.0.0:10")
    compileOnly("public:Team2:1.0.0:1")
    compileOnly("public:Team2:1.0.0:2")
    compileOnly("public:Team2:1.0.0:3")
    compileOnly("public:Team3:1.0.0:1")
    compileOnly("public:Team3:1.0.0:2")
    compileOnly("public:Team3:1.0.0:3")
    compileOnly("public:CustomGo:1.0.0")
    compileOnly("public:Skript:1.0.0")
    compileOnly("public:SkillAPI:s1.98")
    //compileOnly("com.promcteam:proskillapi:1.1.8")
    //compileOnly("com.promcteam:promccore:1.0.4")
    compileOnly("public:mcMMO:1.0.0")
    compileOnly("public:MMOLib:1.0.0")
    compileOnly("public:MMOCore:1.10.2")
    compileOnly("public:MMOItems:1.0.0")
    compileOnly("public:Parties:1.0.0")
    compileOnly("public:NexEngine:1.0.0")
    compileOnly("public:QuantumRPG:1.0.0")
    compileOnly("public:JulyItems:1.0.0")
    compileOnly("public:RPGItems:1.0.0")
    compileOnly("public:Citizens:1.0.0")
    compileOnly("public:MythicLib:1.0.0")
    compileOnly("public:MythicMobs:1.0.1")
    compileOnly("public:MythicMobs5:5.0.4")
    compileOnly("public:ExecutableItems:1.0.0")
    compileOnly("public:Brewery:1.0.0")
    // compileOnly("ink.ptms:Blockdb:1.1.0")
    compileOnly("ink.ptms:Zaphkiel:1.6.0")
    // compileOnly("ink.ptms:Adyeshach:1.5.13-op16")
    compileOnly("ink.ptms:Sandalphon:1.3.0")
    compileOnly("ink.ptms.core:v11900:11900:all-mapped")
    compileOnly("ink.ptms.core:v11400:11400")
    compileOnly("ink.ptms:nms-all:1.0.0")
    implementation(kotlin("stdlib"))
    compileOnly(fileTree("libs"))
    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:1.6.0")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xjvm-default=all")
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.tabooproject.org/repository/releases")
            credentials {
                username = project.findProperty("taboolibUsername").toString()
                password = project.findProperty("taboolibPassword").toString()
            }
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }
    publications {
        create<MavenPublication>("library") {
            from(components["java"])
            groupId = "ink.ptms"
        }
    }
}
