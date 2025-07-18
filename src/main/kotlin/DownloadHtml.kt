package com.example

import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup
import java.io.File

suspend fun Download() {

    val url = "https://shadowslave.fandom.com/api.php?action=parse&page=Shadow_Slave_Wiki&format=json&formatversion=2"

    val response: ParseResponse = client.get(url).body()
    val html = response.parse.text  // Get HTML from response
    val document = Jsoup.parse(html)  // Parse with Jsoup

// Save the document to a file
    val outputFile = File("shadow_slave_home.html")
    outputFile.writeText(document.outerHtml())  // Save as raw HTML

    println("Saved HTML to ${outputFile.absolutePath}")
}


fun main(): kotlin.Unit = runBlocking {
    Download()
}