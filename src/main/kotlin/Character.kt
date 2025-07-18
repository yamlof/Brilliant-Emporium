package com.example

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.io.File

@Serializable
data class Character(
    val name: String,
    val photoCover: List<String>,
)
val client = HttpClient(CIO) {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        })
    }
}

suspend fun fetchCharacterDetail(pageTitle: String?): Character {
    val name = pageTitle ?: "Unknown"
    val url = "https://shadowslave.fandom.com/api.php" +
            "?action=parse&page=$pageTitle&format=json&formatversion=2"

    //println("[fetchCharacterDetail] Fetching data for: $name")
    //println("[fetchCharacterDetail] Request URL: $url")

    return try {
        val response: ParseResponse = client.get(url).body()

        //println("[fetchCharacterDetail] Received response from Fandom API")
        //println("[fetchCharacterDetail] Page title: ${response.parse.title}")

        val html = response.parse.text
        val document = Jsoup.parse(html)

        val images = document.select("img")
        //println("[fetchCharacterDetail] Found ${images.size} <img> tags")

        val imageUrls = images.mapNotNull { img ->
            val src = img.attr("src").ifBlank { img.attr("data-src") }
            if (src.startsWith("http")) src else null
        }.distinct()


        //println("[fetchCharacterDetail] Filtered to ${imageUrls.size} valid image URLs")
        imageUrls.forEachIndexed { index, url ->
            println("  [$index] $url")
        }

        Character(name = pageTitle ?: "Unknown", photoCover = imageUrls)

    } catch (e: Exception) {
        println("Error: ${e.message}")
        Character(name = "Unknown", photoCover = emptyList())
    }
}

/*
fun main(): kotlin.Unit = runBlocking {
    fetchCharacterDetail("Sunny")
} */