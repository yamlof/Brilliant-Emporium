package com.example

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

fun Application.configureRouting() {
    routing {
        get("/") {
            val pageTitle = "shadowslave"
            val images = runBlocking { fetchFromFandom() }
            call.respond(ImageResponse(pageTitle,images))
        }
    }
}

val homepage2 = "Shadow_Slave_Wiki"
val toExtract = mutableListOf("title","url","score","num_comments","view_count","ups","downs","selftext")

@Serializable
data class ImageResponse(val pageTitle: String, val images: List<String>)

suspend fun fetchFromFandom(): List<String> {
    val pageTitle = "Sunny"
    val url = "https://shadowslave.fandom.com/api.php" +
            "?action=parse&page=$pageTitle&format=json&formatversion=2"

    val client = HttpClient(CIO) {
        install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
    }

    return try {
        val response: ParseResponse = client.get(url).body()
        println("Title: ${response.parse.title}")

        val html = response.parse.text
        val document = Jsoup.parse(html)

        val images = document.select("img")

        val imageUrls = images.mapNotNull { img ->
            val src = img.attr("src").ifBlank { img.attr("data-src") }
            if (src.startsWith("http")) src else null
        }

        println("Extracted image URLs:")
        imageUrls.forEach { println(" - $it") }

        imageUrls
    } catch (e: Exception) {
        println("Error fetching data: ${e.message}")
        emptyList()
    } finally {
        client.close()
    }
}

@Serializable
data class ParseResponse(
    val parse: ParsedContent
)

@Serializable
data class ParsedContent(
    val title: String,
    val text: String
)
