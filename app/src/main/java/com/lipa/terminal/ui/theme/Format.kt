package com.lipa.terminal.ui.theme

object KmfFormat {
    fun format(amount: Long?): String {
        if (amount == null) return "—"
        val sign = if (amount < 0) "-" else ""
        val abs = kotlin.math.abs(amount).toString()
        val withSep = buildString {
            val rev = abs.reversed()
            rev.chunked(3).forEachIndexed { i, c ->
                if (i > 0) append(' ')
                append(c)
            }
        }.reversed()
        return sign + withSep
    }

    fun format(amount: Int?): String = format(amount?.toLong())
}
