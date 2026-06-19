package com.cfw.wol.ui

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cfw.wol.data.Device
import com.cfw.wol.data.DeviceManager
import com.cfw.wol.util.ShortcutHelper
import com.cfw.wol.util.WolSender
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(deviceManager: DeviceManager) {
    val devices by deviceManager.devicesFlow.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var editingDevice by remember { mutableStateOf<Device?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("局域网唤醒 (WOL)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingDevice = null
                showDialog = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "添加设备")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (devices.isNotEmpty()) {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            devices.forEach { device ->
                                WolSender.sendMagicPacket(device.mac, device.port)
                            }
                        }
                        Toast.makeText(context, "已发送全部唤醒指令", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp)
                ) {
                    Text("唤醒全部设备", fontSize = 18.sp)
                }
            } else {
                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text("暂无设备，请点击右下角添加", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(devices, key = { it.id }) { device ->
                    DeviceCard(
                        device = device,
                        onWake = {
                            coroutineScope.launch(Dispatchers.IO) {
                                WolSender.sendMagicPacket(device.mac, device.port)
                            }
                            Toast.makeText(context, "正在唤醒 ${device.name}", Toast.LENGTH_SHORT).show()
                        },
                        onEdit = {
                            editingDevice = device
                            showDialog = true
                        },
                        onDelete = { deviceManager.removeDevice(device) },
                        onCreateShortcut = {
                            ShortcutHelper.createShortcut(context, device)
                            Toast.makeText(context, "已尝试创建快捷方式", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        DeviceDialog(
            initialDevice = editingDevice,
            onDismiss = { showDialog = false },
            onSave = { device ->
                deviceManager.addOrUpdateDevice(device)
                showDialog = false
            }
        )
    }
}

@Composable
fun DeviceCard(
    device: Device,
    onWake: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onCreateShortcut: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onWake() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = device.mac, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Box {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "选项")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("添加到桌面") },
                        onClick = {
                            expanded = false
                            onCreateShortcut()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("编辑") },
                        onClick = {
                            expanded = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        onClick = {
                            expanded = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDialog(
    initialDevice: Device?,
    onDismiss: () -> Unit,
    onSave: (Device) -> Unit
) {
    var name by remember { mutableStateOf(initialDevice?.name ?: "") }
    var mac by remember { mutableStateOf(initialDevice?.mac ?: "") }
    var portStr by remember { mutableStateOf(initialDevice?.port?.toString() ?: "9") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val macRegex = Regex("^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialDevice == null) "添加设备" else "编辑设备") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("设备名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = mac,
                    onValueChange = { mac = it },
                    label = { Text("MAC 地址") },
                    placeholder = { Text("例如: AA:BB:CC:DD:EE:FF") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = portStr,
                    onValueChange = { portStr = it },
                    label = { Text("唤醒端口 (默认 9)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val port = portStr.toIntOrNull()
                    if (name.isBlank()) {
                        errorMessage = "设备名称不能为空"
                        showError = true
                        return@TextButton
                    }
                    if (!macRegex.matches(mac.uppercase())) {
                        errorMessage = "MAC 地址格式不正确"
                        showError = true
                        return@TextButton
                    }
                    if (port == null || port !in 0..65535) {
                        errorMessage = "端口号必须在 0 - 65535 之间"
                        showError = true
                        return@TextButton
                    }

                    showError = false
                    val newDevice = Device(
                        id = initialDevice?.id ?: java.util.UUID.randomUUID().toString(),
                        name = name.trim(),
                        mac = mac.uppercase().trim(),
                        port = port
                    )
                    onSave(newDevice)
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
