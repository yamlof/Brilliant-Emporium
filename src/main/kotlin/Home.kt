package com.example

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup

suspend fun Home() : List<String> {
    val url = "https://shadowslave.fandom.com/api.php?action=parse&page=Shadow_Slave_Wiki&format=json&formatversion=2"

    val response: ParseResponse = client.get(url).body()
    val html = response.parse.text  // Get HTML from response
    val document = Jsoup.parse(html)  // Parse with Jsoup

    val homeImages = document.select("div.gallerybox img")

    val imageUrls = homeImages.mapNotNull { img ->
        img.attr("data-src").takeIf { it.isNotEmpty()}
            ?: img.attr("src").takeIf { it.isNotEmpty() }
    }
    imageUrls.forEach {println(it)}
    return imageUrls
}

fun main(): kotlin.Unit = runBlocking {
    Home()
}
