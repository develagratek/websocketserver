package org.example

import io.ktor.application.*
import io.ktor.http.HttpHeaders.Connection
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import java.util.*
import java.util.concurrent.*
import kotlin.collections.LinkedHashSet
import kotlinx.serialization.json.Json

import org.json.JSONObject

fun textToJsonMaps() {
    // Contoh teks yang akan diubah menjadi JSON
    val teks = "{\"nama\": \"John\", \"usia\": 30, \"alamat\": \"123 Jalan ABC\"}"

    // Mengonversi teks menjadi objek JSON
    val jsonObject = JSONObject(teks)

    // Menggunakan Map untuk menyimpan pasangan key-value dari JSON
    val jsonMap = jsonObject.toMap()

    // Mengakses nilai dari Map menggunakan kunci (key) yang dinamis
    val nama = jsonMap["nama"]
    val usia = jsonMap["usia"]
    val alamat = jsonMap["alamat"]

    // Menampilkan nilai yang telah diparsing
    println("Nama: $nama")
    println("Usia: $usia")
    println("Alamat: $alamat")
}

fun detikInt():Int {
    val currentTimeMillis = System.currentTimeMillis()
    val secondsSinceEpoch = currentTimeMillis / 1000 // Konversi dari milidetik menjadi detik
    return secondsSinceEpoch.toInt()

}

fun getUser(textJson: String) : String {
    // Contoh teks yang akan diubah menjadi JSON
    var teks = "{\"nama\": \"John\", \"usia\": 30}"
    teks = """
        {"user":"Alice","age":25}
    """.trimIndent()
    println("---------- $textJson")
    teks = textJson
    // Mengonversi teks menjadi objek JSON
    val jsonObject = JSONObject(textJson)

    // Mengambil nilai dari objek JSON
    val user = jsonObject.getString("user")
    //val usia = jsonObject.getInt("usia")

    // Menampilkan nilai yang telah diparsing
    println("User: $user")
    //println("Usia: $usia")
    return user
}



fun getPurchaseInvoice(textJson: String) : String {
    // Contoh teks yang akan diubah menjadi JSON

    println("---------- $textJson")

    // Mengonversi teks menjadi objek JSON
    val jsonObject = JSONObject(textJson)

    // Mengambil nilai dari objek JSON
    val invoice = jsonObject.getString("invoice")
    val code = jsonObject.getString("code")
    val apps = jsonObject.getString("apps")
    //val usia = jsonObject.getInt("usia")

    // Menampilkan nilai yang telah diparsing
    println("Invoice: $invoice")
    //println("Usia: $usia")
    return invoice
}

fun getPurchaseCode(textJson: String) : String {
    println("---------- $textJson")

    // Mengonversi teks menjadi objek JSON
    val jsonObject = JSONObject(textJson)

    // Mengambil nilai dari objek JSON
    val invoice = jsonObject.getString("invoice")
    val code = jsonObject.getString("code")
    val apps = jsonObject.getString("apps")
    //val usia = jsonObject.getInt("usia")

    // Menampilkan nilai yang telah diparsing
    println("Invoice: $invoice")
    //println("Usia: $usia")
    return code
}




fun main() {
if (detikInt() < 1767225600) {
    val server = embeddedServer(Netty, port = 5001) {
        val clients = ConcurrentHashMap<WebSocketSession, String>()
        val printer = DateTimePrinter()
        install(io.ktor.websocket.WebSockets)

        routing {
            var serverConnection: Connection? = null
            val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
            val userConnections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
            val userAndroid = Collections.synchronizedSet<Connection?>(LinkedHashSet())
            val adminsConnection = Collections.synchronizedSet<Connection?>(LinkedHashSet())
            webSocket("/websocket") {
                println("Client connected: $this ${printer.printCurrentDateTime()}")
                val thisConnection = Connection(this)

                connections += thisConnection

                try {

                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readText()
                        val textWithUsername = "[${thisConnection.name}]: $receivedText"

                        try {
                            println("${printer.printCurrentDateTime()}------------awal_data_dari_client")
                            println("${printer.printCurrentDateTime()}--------------receivedText $receivedText")
                            try {
                                val user = getUser(receivedText)
                                if (user == "client") {
                                    println("------------------------------ lolos1")
                                    userConnections.add(thisConnection)
                                    println("------------------------------ lolos2")
                                    userConnections.forEach {
                                        println("------------------------------ lolos3")
                                        println("${printer.printCurrentDateTime()} ----------- CEK SERVER $serverConnection")
                                        if (serverConnection != null) {
                                            it.session.send("You are connected! There are ${userConnections.count()} users here. ${printer.printCurrentDateTime()}")
                                        } else {
                                            println("------------------------------ lolos4")
                                            it.session.send("Server Controller is Disconnected")
                                        }
                                    }

                                    userAndroid.forEach {
                                        println("------------------------------ lolos3")
                                        println("${printer.printCurrentDateTime()} ----------- CEK SERVER $serverConnection")
                                        if (serverConnection != null) {
                                            it.session.send("""
                                                {"jumlah_user" : ${userConnections.count()}}
                                            """.trimIndent())
                                        } else {
                                            println("------------------------------ lolos4")
                                            it.session.send("Server Controller is Disconnected")
                                        }
                                    }

                                } else {
                                    serverConnection = thisConnection
                                    println("${printer.printCurrentDateTime()} Server : $serverConnection")
                                }
                                println("${printer.printCurrentDateTime()} list_user $userConnections")
                            } catch (err1: Exception) {
                                println("${printer.printCurrentDateTime()} Bukan_data_idetifikasi_user")
                                userConnections.forEach {
                                    println("${printer.printCurrentDateTime()} -----------------connection $connections ${printer.printCurrentDateTime()}")
                                    println("-----------------for each $it ")
                                    println("-----------------incoming $incoming ")
                                    println("-----------------thisConnection $thisConnection")
                                    if (it != thisConnection) {
                                        println("${printer.printCurrentDateTime()} -----send $it  $receivedText")
                                        it.session.send(receivedText)
                                    }
                                }
                            }
                            println("${printer.printCurrentDateTime()}------------akhir_data_dari_client")
                        } catch (eJson2: Exception) {
                            println("${printer.printCurrentDateTime()} eJson2------ $eJson2 ")
                        }

                    }
                } catch (e: ClosedReceiveChannelException) {
                    println("${printer.printCurrentDateTime()} Connection closed: $this $e")
                    if (thisConnection != serverConnection) {
                        userConnections.remove(thisConnection)
                    } else {
                        serverConnection = null
                        userConnections.forEach(
                            {
                                it.session.send("You are connected! There are ${userConnections.count()} users here. ${printer.printCurrentDateTime()}")
                            }
                        )
                    }
                } finally {
                    println("${printer.printCurrentDateTime()} Client disconnected: $this")
                    // Remove the client from the list upon disconnection
                    if (thisConnection != serverConnection) {
                        userConnections.remove(thisConnection)
                        userConnections.forEach(
                            {
                                it.session.send("You are connected! There are ${userConnections.count()} users here. ${printer.printCurrentDateTime()}")
                            }
                        )
                    } else {
                        serverConnection = null
                        userConnections.forEach(
                            {
                                it.session.send("Server Controller is Disconnected")
                            }
                        )
                    }
                    clients.remove(this)
                }
            }
            webSocket("/android") {
                println("Client connected Android: $this ${printer.printCurrentDateTime()}")
                val thisConnection = Connection(this)
                connections += thisConnection
                try {

                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val receivedText = frame.readText()


                        try {
                            println("${printer.printCurrentDateTime()}>>>>>>>>>>awal_data_dari_client")
                            println("${printer.printCurrentDateTime()}>>>>>>>>>>>receivedText $receivedText")
                            try {
                                val user = getUser(receivedText)
                                if (user == "client") {
                                    println(">>>>>>>>>>>>>>>>>> lolos1")
                                    userAndroid.add(thisConnection)
                                    println(">>>>>>>>>> lolos2")
                                    userAndroid.forEach {
                                        println(">>>>>>>>>>>>> lolos3")
                                        println("${printer.printCurrentDateTime()} ----------- CEK SERVER $serverConnection")
                                        if (serverConnection != null) {
                                            it.session.send("You are connected Android! There are ${userConnections.count()} users here. ${printer.printCurrentDateTime()}")
                                        } else {
                                            println("------------------------------ lolos4")
                                            it.session.send("Server Controller is Disconnected")
                                        }
                                    }

                                } else {
                                    serverConnection = thisConnection
                                    println("${printer.printCurrentDateTime()} Server : $serverConnection")
                                }
                                println("${printer.printCurrentDateTime()} list_user $userConnections")
                            } catch (err1: Exception) {
                                println("${printer.printCurrentDateTime()} Bukan_data_idetifikasi_user")
                                userConnections.forEach {
                                    println("${printer.printCurrentDateTime()} -----------------connection $connections ${printer.printCurrentDateTime()}")
                                    println("-----------------for each $it ")
                                    println("-----------------incoming $incoming ")
                                    println("-----------------thisConnection $thisConnection")
                                    if (it != thisConnection) {
                                        println("${printer.printCurrentDateTime()} -----send $it  $receivedText")
                                        it.session.send(receivedText)
                                    }
                                }
                                userAndroid.forEach {
                                    println("------------------------------ lolos3")
                                    println("${printer.printCurrentDateTime()} ----------- CEK SERVER $serverConnection")
                                    if (serverConnection != null) {
                                        it.session.send("""
                                                {"jumlah_user" : ${userConnections.count()}}
                                            """.trimIndent())
                                    } else {
                                        println("------------------------------ lolos4")
                                        it.session.send("Server Controller is Disconnected")
                                    }
                                }
                            }
                            println("${printer.printCurrentDateTime()}------------akhir_data_dari_client")
                        } catch (eJson2: Exception) {
                            println("${printer.printCurrentDateTime()} eJson2------ $eJson2 ")
                        }

                    }
                } catch (e: ClosedReceiveChannelException) {
                    println("${printer.printCurrentDateTime()} Connection closed: $this $e")
                    if (thisConnection != serverConnection) {
                        userConnections.remove(thisConnection)
                    } else {
                        serverConnection = null
                        userConnections.forEach(
                            {
                                it.session.send("You are connected! There are ${userConnections.count()} users here. ${printer.printCurrentDateTime()}")
                            }
                        )
                    }
                } finally {
                    println("${printer.printCurrentDateTime()} Client disconnected: $this")
                    // Remove the client from the list upon disconnection
                    if (thisConnection != serverConnection) {
                        userConnections.remove(thisConnection)
                        userConnections.forEach(
                            {
                                it.session.send("You are connected! There are ${userConnections.count()} users here. ${printer.printCurrentDateTime()}")
                            }
                        )
                    } else {
                        serverConnection = null
                        userConnections.forEach(
                            {
                                it.session.send("Server Controller is Disconnected")
                            }
                        )
                    }
                    clients.remove(this)
                }
            }
        }
    }
    server.start(wait = true)
}
}
