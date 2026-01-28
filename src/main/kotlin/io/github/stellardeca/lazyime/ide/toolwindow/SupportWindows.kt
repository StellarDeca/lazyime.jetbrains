package io.github.stellardeca.lazyime.ide.toolwindow

enum class SupportWindows {
    Run,
    Project,
    Commit,
    Terminal;

    companion object {
        fun fromString(value: String?): SupportWindows? {
            return entries.find { it.name.equals(value, ignoreCase = true) }
        }
    }
}
