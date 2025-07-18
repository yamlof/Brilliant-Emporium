package com.example

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.io.File

fun Application.configureRouting() {
    routing {
        get("/") {

            val images = runBlocking { fetchFromFandom() }

            if (images != null) {
                call.respond(images)

            } else {
                call.respond(HttpStatusCode.NotFound, "Character not found or failed to fetch data.")
            }
        }

        get("/character/{characterInfo}"){
            val characterInfo = call.parameters["characterInfo"]

            if (characterInfo == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing characterInfo path parameter")
                return@get
            }

            val characterDetail = fetchCharacterDetail(characterInfo)

            call.respond(characterDetail)

        }
        get("/home") {
            val homeImages = runBlocking { Home() }

            call.respond(homeImages)
        }
    }
}


suspend fun fetchFromFandom(): Characters? {
    val pageTitle = "Characters"
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
        val html = response.parse.text
        val document = Jsoup.parse(html)

        val imageData = document.select("span[typeof='mw:File']").mapNotNull { span ->
            val img = span.selectFirst("img")
            val labelElement = span.parent()?.selectFirst("p > b")

            val src = img?.attr("src")
            val dataSrc = img?.attr("data-src")

            val imageUrl = when {
                src?.startsWith("http") == true -> src
                dataSrc?.startsWith("http") == true -> dataSrc
                else -> null
            }

            val label = labelElement?.text()?.trim() ?: "Unknown"

            if (imageUrl != null) {
                CharacterImage(url = imageUrl, label = label)
            } else null
        }

        println("Fetched ${imageData.size} character images")
        imageData.forEach {
            println("- ${it.label}: ${it.url}")
        }

        if (imageData.isNotEmpty()) Characters(items = imageData) else null

    } catch (e: Exception) {
        println("Error: ${e.message}")
        null
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

@Serializable
data class Characters(
    val items: List<CharacterImage>
)

@Serializable
data class CharacterImage(
    val url: String,
    val label: String
)