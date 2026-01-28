import org.jetbrains.changelog.markdownToHTML

plugins {
    id("java")  // 引入 java 环境
    id("org.jetbrains.kotlin.jvm") version "2.1.20"  // 指定 kotlin 编译器版本
    id("org.jetbrains.intellij.platform") version "2.10.2"  // 引入 jetbrains 插件
    kotlin("plugin.serialization") version "2.1.20"
    id("org.jetbrains.changelog") version "2.2.1" // 引入 Changelog 插件
}

group = "io.github.StellarDeca.lazyime.jetbrains"
version = "0.1.0"

repositories {
    // 仓库配置
    maven {
        url = uri("https://maven.aliyun.com/repository/public")  // 使用镜像地址下载公共依赖
    }
    intellijPlatform {
        defaultRepositories()  // 指定 jetbrains 依赖下载地址
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        intellijIdea("2025.2.4")  // 指定开发 SDK 版本
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
        // Add plugin dependencies for compilation here:
        bundledPlugin("org.jetbrains.kotlin")  // 引入 jetbrains kotlin 依赖
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"  // 最低支持 IDE 版本
            untilBuild = null  // 不限制最高版本
        }

        // 自动从 readme 中提取 description 注入到 plugin.xml
        description = providers.fileContents(layout.projectDirectory.file("README.md")).asText.map {
            markdownToHTML(it)
        }

        changeNotes = """
            Stable release tested with 2026.1
        """.trimIndent()
    }

    /// 兼容性检查
    pluginVerification {
        ides {
            recommended()
        }
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
    buildSearchableOptions {
        enabled = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

changelog {
    groups.empty()
    repositoryUrl = "https://github.com/StellarDeca/lazyime.jetbrains"
}
