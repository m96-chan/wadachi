package io.github.m96chan.wadachi

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
