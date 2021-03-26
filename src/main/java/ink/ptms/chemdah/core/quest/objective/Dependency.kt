package ink.ptms.chemdah.core.quest.objective

import io.izzel.taboolib.Version

annotation class Dependency(val plugin: String, val version: Version = Version.v1_7)
