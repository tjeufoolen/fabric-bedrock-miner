plugins {
    id("maven-publish")
    id("com.github.hierynomus.license") version "0.16.1" apply false
    id("fabric-loom") version "1.13-SNAPSHOT" apply false

    // https://github.com/ReplayMod/preprocessor
    // https://github.com/Fallen-Breath/preprocessor
    id("com.replaymod.preprocess") version "d452ef7612"

    // https://github.com/Fallen-Breath/yamlang
    id("me.fallenbreath.yamlang") version "1.5.0" apply false
}

preprocess {
    strictExtraMappings = false

    val mc1_21_11 = createNode("1.21.11", 1_21_11, "")

    mc1_21_11.link(mc1_21_10, null)
    mc1_21_10.link(mc1_21_09, null)
    mc1_21_09.link(mc1_21_08, null)
    mc1_21_08.link(mc1_21_07, null)
    mc1_21_07.link(mc1_21_06, null)
    mc1_21_06.link(mc1_21_05, null)
    mc1_21_05.link(mc1_21_04, null)
    mc1_21_04.link(mc1_21_03, null)
    mc1_21_03.link(mc1_21_02, null)
    mc1_21_02.link(mc1_21_01, null)
    mc1_21_01.link(mc1_21_00, null)
    mc1_21_00.link(mc1_20_06, null)
    mc1_20_06.link(mc1_20_05, null)
    mc1_20_05.link(mc1_20_04, null)
    mc1_20_04.link(mc1_20_03, null)
    mc1_20_03.link(mc1_20_02, null)
    mc1_20_02.link(mc1_20_01, null)
    mc1_20_01.link(mc1_20_00, null)
    mc1_20_00.link(mc1_19_04, null)
    mc1_19_04.link(mc1_19_03, null)
    mc1_19_03.link(mc1_19_02, file("versions/mapping-1.19.2-1.19.3.txt"))
    mc1_19_02.link(mc1_19_01, null)
    mc1_19_01.link(mc1_19_00, null)
}


// 获取所有子项目（排除 fabricWrapper）
val fabricSubprojects = rootProject.subprojects.filter { it.name != "fabricWrapper" }

tasks.register("build") {
    group = "build"
    description = "构建 fabricWrapper 版本包（推荐用于发布）"

    dependsOn("buildAndGather")
}

tasks.register("buildAndGather") {
    group = "build"
    description = "收集所有子项目的构建产物到根项目的 libs 目录（开发便利工具）"

    // 依赖所有子项目的构建任务
    fabricSubprojects.forEach { sub ->
        evaluationDependsOn(":${sub.name}")
        dependsOn(sub.tasks.named("build"))
    }

    doFirst {
        println("开始收集各个版本的构建产物...")
        val rootLibsDir = layout.buildDirectory.dir("libs").get().asFile
        // 清理根项目的 libs 目录
        delete(fileTree(rootLibsDir) {
            include("*")
        })
        // 收集各个版本的 JAR 文件
        fabricSubprojects.forEach { sub ->
            val subLibsDir = sub.layout.buildDirectory.dir("libs").get().asFile
            copy {
                from(subLibsDir) {
                    include("*.jar")
                    exclude("*-dev.jar", "*-sources.jar", "*-shadow.jar")
                }
                into(rootLibsDir)
                duplicatesStrategy = DuplicatesStrategy.INCLUDE
            }
            println("已收集 ${sub.name} 的构建产物")
        }
        println("构建产物收集完成，文件位于: ${rootLibsDir.absolutePath}")
    }
}

tasks.register("buildAll") {
    group = "build"
    description = "构建所有子项目以及 fabricWrapper 版本包"

    dependsOn(tasks.named("buildAndGather"))
    dependsOn(":fabricWrapper:build")
}

tasks.register("buildFabricWrapper") {
    group = "build"
    description = "构建 fabricWrapper 版本包"

    dependsOn(tasks.named("buildAndGather"))
    dependsOn(":fabricWrapper:build")
}
