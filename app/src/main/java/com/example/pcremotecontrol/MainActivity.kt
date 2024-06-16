package com.example.pcremotecontrol


import android.os.Bundle
import android.util.Log
import androidx.activity.*
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.pcremotecontrol.ui.theme.PCRemoteControlTheme
import kotlinx.coroutines.*
import kotlinx.coroutines.selects.select
import java.net.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val scope = CoroutineScope(Job() + Dispatchers.Main)
        val cddm = MacAddressSelector()
        val macArray = arrayOf("80:e8:2c:30:d8:c0")

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
                        cddm.ComputerSelectDropdown()
                        Button(onClick = {
                            scope.launch { PacketSender().sendMagicPacket(cddm.selectedMac) }
                        }) {
                            Text(text = "HP Note PC")
                        }
                    }
                }
            }
        }
    }
}

class MacAddressSelector() {
    private val MacAddressList = mapOf(
        "80:e8:2c:30:d8:c0" to "Note PC",
        "74:56:3c:33:bc:e3" to "Desktop PC"
    )
    var selectedMac = ""

    @Preview
    @Composable
    fun ComputerSelectDropdown() {
        var expanded by remember { mutableStateOf(false) }
        var fieldText by remember {
            mutableStateOf("Not Selected")
        }

        Row {
            TextField(
                value = fieldText,
                onValueChange = { },
                readOnly = true,
                label = {
                    Text(
                        text = "PC Name"
                    )
                })
            IconButton(onClick = { expanded = true }) {
                Icon(Icons.Default.ArrowDropDown, contentDescription = "PC")
                DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    MacAddressList.forEach { (pcMacAddress, pcName) ->
                        DropdownMenuItem(
                            text = { Text(pcName) },
                            onClick = {
                                fieldText = "$pcName($pcMacAddress)"
                                expanded = false
                                selectedMac = pcMacAddress
                            })
                    }

                    Divider()
                    DropdownMenuItem(
                        text = { Text("Clear") },
                        onClick = {
                            fieldText = ""
                            expanded = false
                            selectedMac = ""
                        })
                }
            }
        }


//        Text("AAA",modifier=Modifier DropdownMenu(expanded = , onDismissRequest = { /*TODO*/ }) {
//
//        })

//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .wrapContentSize(Alignment.Center),
//        ) {
//            Text(
//                items[selectedIndex],
//                modifier = Modifier
//                    .fillMaxWidth(0.5f)
//                    .clickable(onClick = { expanded = true })
//                    .background(
//                        Color.Gray
//                    ),
//
//                )
//            DropdownMenu(
//                expanded = expanded,
//                onDismissRequest = { expanded = false },
//                modifier = Modifier
//                    .fillMaxWidth(0.5f)
//            ) {
//                items.forEachIndexed { index, s ->
//                    DropdownMenuItem(onClick = {
//                        selectedIndex = index
//                        expanded = false
//                    }, text = {
//                        Text(text = s)
//                    })
//                }
//            }
//        }
    }
}

@Composable
fun DropdownDemo() {
    var expanded by remember { mutableStateOf(false) }
    val items = listOf("A", "B", "C", "D", "E", "F")
    val disabledValue = "B"
    var selectedIndex by remember { mutableStateOf(0) }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.Center)
    ) {
        Text(
            items[selectedIndex],
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .clickable(onClick = { expanded = true })
                .background(
                    Color.Gray
                ),

            )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.5f)
        ) {
            items.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    expanded = false
                }, text = {
                    Text(text = s)
                })
            }
            Divider()
            DropdownMenuItem(onClick = {
                expanded = false
            }, text = {
                Text(text = "")
            })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TestList() {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = Modifier
//            .fillMaxSize()
            .wrapContentSize(Alignment.TopStart)
    ) {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Localized description")
        }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {}
//            onDismissRequest = { expanded = false },

        ) {
//            DropdownMenuItem(onClick = { /* Handle refresh! */ }) {
//                Text("Refresh")
//            }
            DropdownMenuItem(
                onClick = { /* Handle settings! */ },
                text = { Text("Home PC") }
            )
            DropdownMenuItem(
                onClick = { /* Handle send feedback! */ },
                text = { Text(text = "HP Note PC") })
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