package ru.vtb.szkf.oleg.prodsky.extensions

fun String.toMaskedString(): String {
    val replacement = StringBuilder()
    repeat(this.length - 2) { replacement.append("*") }

    return this.replaceRange(1, this.lastIndex, replacement)
}
