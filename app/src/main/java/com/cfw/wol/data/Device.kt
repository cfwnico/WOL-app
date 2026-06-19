package com.cfw.wol.data

import java.util.UUID

data class Device(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mac: String,
    val port: Int = 9
)
