package com.example.wallet3

object Routes {
    const val MAIN = "main"
    const val EDITOR = "editor/{id}"
    const val MAP = "map"
    const val SETTINGS = "settings"

    /** id = -1 — новая запись. */
    fun editor(id: Long) = "editor/$id"
}
