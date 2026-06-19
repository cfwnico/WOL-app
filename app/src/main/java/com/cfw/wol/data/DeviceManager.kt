package com.cfw.wol.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DeviceManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wol_devices", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val KEY_DEVICES = "devices_list"

    private val _devicesFlow = MutableStateFlow<List<Device>>(emptyList())
    val devicesFlow: StateFlow<List<Device>> = _devicesFlow.asStateFlow()

    init {
        loadDevices()
    }

    private fun loadDevices() {
        val json = prefs.getString(KEY_DEVICES, null)
        if (json != null) {
            val type = object : TypeToken<List<Device>>() {}.type
            val list: List<Device> = gson.fromJson(json, type)
            _devicesFlow.value = list
        } else {
            _devicesFlow.value = emptyList()
        }
    }

    fun addOrUpdateDevice(device: Device) {
        val currentList = _devicesFlow.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == device.id }
        if (index != -1) {
            currentList[index] = device
        } else {
            currentList.add(device)
        }
        saveAndEmit(currentList)
    }

    fun removeDevice(device: Device) {
        val currentList = _devicesFlow.value.toMutableList()
        currentList.removeAll { it.id == device.id }
        saveAndEmit(currentList)
    }

    private fun saveAndEmit(list: List<Device>) {
        prefs.edit().putString(KEY_DEVICES, gson.toJson(list)).apply()
        _devicesFlow.value = list
    }
}
