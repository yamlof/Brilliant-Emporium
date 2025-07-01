package com.example
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup


val url = "https://shadowslave.fandom.com/api.php?action=parse&page=Sunny&format=json&formatversion=2"

val homepage = "https://shadowslave.fandom.com/api.php?action=parse&page=Shadow_Slave_Wiki&format=json&formatversion=2"

