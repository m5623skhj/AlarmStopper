package com.example.alarmstopper

object GlobalItem {
    private var pairedBluetoothNameList: MutableList<String> = mutableListOf<String>();

    fun RegisterPairBluetoothName(registerBluetoothName: String) {
        pairedBluetoothNameList += registerBluetoothName
    }

    fun DeregisterPairBluetoothName(deregisterBluetoothName: String) {
        pairedBluetoothNameList.remove(deregisterBluetoothName)
    }

    fun GetList() : List<String> {
        return pairedBluetoothNameList.toList()
    }
}