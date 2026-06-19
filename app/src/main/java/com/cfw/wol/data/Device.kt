package com.cfw.wol.data

import androidx.annotation.Keep
import java.util.UUID

@Keep
data class Device(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val mac: String,
    val port: Int = 9
)
