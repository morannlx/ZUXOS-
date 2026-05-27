package me.inkdye.zuxos.ui.theme

enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM;

    companion object {
        fun fromOrdinal(ordinal: Int): ThemeMode =
            entries.getOrElse(ordinal) { SYSTEM }
    }
}
