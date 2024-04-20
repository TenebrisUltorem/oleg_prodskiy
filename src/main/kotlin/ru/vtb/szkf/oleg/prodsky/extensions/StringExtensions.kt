package ru.vtb.szkf.oleg.prodsky.extensions

fun String.toMaskedString(): String = this.replaceRange(
    1, this.lastIndex, StringBuilder().apply {
        repeat(this.length - 2) { append("*") }
    }
)
