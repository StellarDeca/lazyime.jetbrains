import org.jetbrains.changelog.markdownToHTML

plugins {
    id("java")  // 引入 java 环境
    id("org.jetbrains.kotlin.jvm") version "2.1.20"  // 指定 kotlin 编译器版本
    id("org.jetbrains.intellij.platform") version "2.10.2"  // 引入 jetbrains 插件
    kotlin("plugin.serialization") version "2.1.20"
    id("org.jetbrains.changelog") version "2.2.1" // 引入 Changelog 插件
}

group = "io.github.StellarDeca.lazyime.jetbrains"
version = "0.1.3"

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
            val englishDescription = """
                Lazyime is an intelligent Input Method Editor (IME) switcher for JetBrains IDEs.<br/>
                It leverages Tree-sitter for real-time syntax analysis to automatically switch between 
                English and your native language based on the cursor context (Code vs. Comment).
                It also switches automatically when using the IDE tool window.
                <br/><br/>
            """.trimIndent()
            englishDescription + markdownToHTML(it)
        }

        changeNotes = """
            添加IDE焦点设置和监听（由于windows安全策略，Ide失焦输入法切换大概率失败）<br>
            增加多项目支持<br>
            修复了尾随注释导致输入打断的bug<br>
            添加：只有当语法状态发生变化时才更新输入法<br>
            添加输入合成检查以避免输入中断<br>
            排除对非主要编辑和非活跃编辑者的响应<br>
            修复由制表符引起的注释分析错误<br>
            新增服务器崩溃监控<br>
            添加新的arm64法官方法<br><br>
            Add ide focus settings and listener(Due to Windows security policy, there is a high probability that IDE out-of-focus input method switching will fail)<br>
            Add multi project support<br>
            Fixed a bug where input could not be stabilized in trailing comments<br>
            Add Update the input method only when the syntax state changes<br>
            Add input synthesis checks to avoid input interruptions<br>
            Exclude responses to non-primary and inactive editors<br>
            Fix comments analysis errors caused by tabs<br>
            Added Server crash monitoring<br>
            Add new arm64 judge method<br>
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

    // 证书 与 签名
    signPlugin {
        val certFile = project.rootProject.file("chain.crt")
        val keyFile = project.rootProject.file("private.pem")
        certificateChain.set(certFile.readText())
        privateKey.set(keyFile.readText())
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
