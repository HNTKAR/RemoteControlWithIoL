package com.example.pcremotecontrol

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.pcremotecontrol.ui.theme.PCRemoteControlTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        setContent {
            PCRemoteControlTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "WoL Button")
                        Button(onClick = {
                            scope.launch { PacketSender().sendMagicPacket("80:e8:2c:30:d8:c0") }
                        }) {
                            Text(text = "OK")
                        }
                    }
                }
            }
        }
    }
}

class PacketSender {
    private var macAddress = ByteArray(6)

    private fun checkMacAddress(strMacAddress: String): Boolean {
        var macCounter = 0
        val splitMacAddress = strMacAddress.split(Regex("[:.-]"))
        if (splitMacAddress.size != 6) {
            Log.d("App", "Mac Address Format Error")
            return false
        }
        for (i in splitMacAddress) {
            if (i.isEmpty()) {
                Log.d("App", "Zero in Mac Address")
                return false
            } else {
                Log.d("App", "i=$i")
                try {
                    macAddress[macCounter++] = i.toInt(16).toByte()
                } catch (e: NumberFormatException) {
                    Log.d("App", "Mac Address Format Error")
                    return false
                } catch (e: IllegalArgumentException) {
                    Log.d("App", "Mac Address Format Error")
                    return false
                }
                Log.d("App", macAddress[macCounter - 1].toString())
            }
        }
        return true
    }

    suspend fun sendMagicPacket(strMacAddress: String) {
        val data = ByteArray(6 * 17)

        if (!checkMacAddress(strMacAddress))
            return

        for (i in 0..5)
            data[i] = "ff".toInt(16).toByte()
        for (i in 1..16)
            for (j in macAddress.indices)
                data[i * 6 + j] = macAddress[j]

        Log.d("App", "data=V")
        for (i in data)
            Log.d("App", i.toString())

        withContext(Dispatchers.IO) {
            Log.d("App", "send  1")
            val ip = InetAddress.getByName("255.255.255.255")
            Log.d("App", "send  2")
            val packet = DatagramPacket(data, data.size, ip, 40000)
            val sock = DatagramSocket()
            sock.send(packet)
            sock.close()
        }
        Log.d("App", "send  3")
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    PCRemoteControlTheme {
        Greeting("Android")
    }
}