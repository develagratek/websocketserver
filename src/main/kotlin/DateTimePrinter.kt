package org.example

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class DateTimePrinter {
    fun printCurrentDateTime(): String {
        // Mendapatkan waktu sekarang
        val currentDateTime = LocalDateTime.now()

        // Format tanggal dan waktu
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formattedDateTime = currentDateTime.format(formatter)

        // Mencetak tanggal dan waktu yang diformat
        return ("Last Status: $formattedDateTime")
    }
}

fun main() {
    val printer = DateTimePrinter()
    printer.printCurrentDateTime()
}
