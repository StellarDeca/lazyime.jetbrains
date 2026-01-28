<div align="center">
    <h1>Lazyime</h1>
    <p><strong>智能感知上下文，自动切换输入法</strong></p>
    <p>基于 Tree-sitter 语法分析 | 适配 JetBrains 全家桶 | 低延迟体验</p>
</div>

## **✨ 简介**

**Lazyime** 是一款专为 JetBrains IDE 设计的智能输入法切换插件。它通过与高性能的 Rust 后端服务 **LazyInputSwitcher** 通信，利用
**Tree-sitter** 进行实时的代码语法分析。

它能精确识别你当前的光标是在 **编写代码** 还是 **撰写注释**，并据此自动切换系统输入法，让你在编码时无需频繁手动切换中英文。

## **🚀 核心功能**

* **⚡️ 语法感知切换**：
  * **代码区域 (Code)** ➔ 自动切换为 **英文模式**。
  * **注释区域 (Comment)** ➔ 自动切换为 **系统输入法 (中文/母语)**。
* **🪟 工具窗口适配**：
  * 针对 **Project (项目)**、**Run (运行)**、**Commit (提交)**、**Terminal (终端)** 常用工具窗口提供独立的输入法预设。
* **🔌 配置管理**：
  * 插件内置管理界面，支持设置工具窗口的目标输入法与手动安装、更新后端服务。

## **⚙️ 架构原理**

插件作为客户端，通过 TCP 协议与本地运行的 Rust
服务端 ([LazyInputSwitcher](https://github.com/StellarDeca/LazyInputSwitcher)) 通信。

1. **事件触发**：当你在 IDE 中移动光标或修改代码时，插件会收集当前文档的内容、语言类型及光标位置。
2. **异步分析与切换**：插件将数据发送给服务端，服务端进行语法分析与输入法切换（响应通常 \< 5ms）。

## **📦 安装与配置**

### **1\. 安装插件**

在 JetBrains IDE 中打开 Settings \-\> Plugins \-\> Marketplace，搜索 **Lazyime** 并安装。

### **2\. 配置服务端 (必要步骤)**

由于服务端包含不同平台的二进制文件，**默认不随插件打包**。你需要手动进行初始化：

1. 打开 Settings。
2. 找到Lazyime 设置页。
3. 在 **服务端** 分组下，点击 **安装** 按钮。
4. 等待安装完成后，重启 IDE。

### **3\. 工具窗口设置**

在设置页面的 **工具窗口输入法**分组中，你可以为不同的 IDE 工具窗口指定默认输入法行为：

* **Run Window**: 默认 English
* **Project View**: 默认 English
* **Commit Dialog**: 默认 English
* **Terminal**: 默认 English

## **💻 操作系统支持与注意事项**

由于涉及到底层输入法切换，不同操作系统的配置略有差异。

### **Windows**

* **支持**：微软拼音输入法 (默认支持: 简体中文, 美国英语)。
* **注意**：受限于 Windows 安全机制，系统中必须安装 **至少两种** 语言的键盘布局（例如：中文 \+ 英文），切换功能才能正常工作。

### **Linux**

* **支持**：Fcitx5 输入法框架 (默认支持: rime, pinyin , keyboard-us)。
* **注意**：如果自动下载的二进制文件无法运行，请检查插件 bin 目录下文件的执行权限 (chmod \+x)。

### **macOS**

* **支持**：系统默认输入法 (默认支持: ABC / 简体拼音)。
* **注意**：
  * 自动安装的可执行文件可能因安全策略无法直接运行。
  * **解决方案**：建议手动编译 Rust 服务端，或前往插件安装目录的 bin 文件夹下，通过终端赋予可执行权限，并在“安全性与隐私”中允许运行。

## **自定义输入法**

如果你使用的输入法不在上述预定义列表中

你需要克隆[服务端仓库](https://github.com/StellarDeca/LazyInputSwitcher)，修改对应平台的源码中预定义的输入法 **ID** 或 *
*字符串**

* src/switch/
  * /windows/mod.rs: NATIVE_LANGUAGE_ID
  * /linux/fcitx5.rs: NATIVE_METHOD
  * /macos/mod.rs: NATIVE_LANGUAGE_ID

并重新编译服务端替换插件 bin 目录下的文件。

## 💖 贡献者

<div align="center">
    <a href="https://github.com/StellarDeca/lazyime.jetbrains/graphs/contributors">
        <img src="https://contrib.rocks/image?repo=StellarDeca/lazyime.jetbrains"  alt="Authors"/>
    </a>
</div>

## **🔗 链接**

* **服务端源码**: [LazyInputSwitcher](https://github.com/StellarDeca/LazyInputSwitcher)
* **问题反馈**: [GitHub Issues](https://github.com/StellarDeca/lazyime.jetbrains)
