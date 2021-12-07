/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.bluetoothlechat.bluetooth

import android.app.Application
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.Context
import android.os.ParcelUuid
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.bluetoothlechat.bluetooth.Message.RemoteMessage
import com.example.bluetoothlechat.chat.DeviceConnectionState

private const val TAG = "ChatServer"

object ChatServer {

    // hold reference to app context to run the chat server
    private var app: Application? = null
    private lateinit var bluetoothManager: BluetoothManager
    // BluetoothAdapter should never be null if the app is installed from the Play store
    // since BLE is required per the <uses-feature> tag in the AndroidManifest.xml.
    // If the app is installed on an emulator without bluetooth then the app will crash
    // on launch since installing via Android Studio bypasses the <uses-feature> flags
    private val adapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

    // This property will be null if bluetooth is not enabled or if advertising is not
    // possible on the device
    private var advertiser: BluetoothLeAdvertiser? = null
    private var advertiserList = mutableListOf<BluetoothLeAdvertiser?>(null, null, null)

    private var advertiseCallback: AdvertiseCallback? = null
    private var advertiseCallbackList = mutableListOf<AdvertiseCallback?>(null, null, null)

    private var advertiseSettings: AdvertiseSettings = buildAdvertiseSettings()

    private var advertiseData: AdvertiseData = buildAdvertiseData()
    private var advertiseDataList = listOf(buildAdvertiseData(0), buildAdvertiseData(1), buildAdvertiseData(2))

    // LiveData for reporting the messages sent to the device
    private val _messages = MutableLiveData<Message>()
    val messages = _messages as LiveData<Message>

    // LiveData for reporting connection requests
    private val _connectionRequest = MutableLiveData<BluetoothDevice>()
    val connectionRequest = _connectionRequest as LiveData<BluetoothDevice>

    private val _connectionRequest0 = MutableLiveData<BluetoothDevice>()
    val connectionRequest0 = _connectionRequest0 as LiveData<BluetoothDevice>

    private val _connectionRequest1 = MutableLiveData<BluetoothDevice>()
    val connectionRequest1 = _connectionRequest1 as LiveData<BluetoothDevice>

    private val _connectionRequest2 = MutableLiveData<BluetoothDevice>()
    val connectionRequest2 = _connectionRequest2 as LiveData<BluetoothDevice>

    val _connectionRequestList = listOf(_connectionRequest0, _connectionRequest1, _connectionRequest2)
    val connectionRequestList = listOf(connectionRequest0, connectionRequest1, connectionRequest2)

    // LiveData for reporting the messages sent to the device
    private val _requestEnableBluetooth = MutableLiveData<Boolean>()
    val requestEnableBluetooth = _requestEnableBluetooth as LiveData<Boolean>

    private var gattServer: BluetoothGattServer? = null
    private var gattServerList = mutableListOf<BluetoothGattServer?>(null, null, null)

    private var gattServerCallback: GattServerCallback? = null
    private var gattServerCallbackList = mutableListOf<GattServerCallback?>(null, null, null)

    private var gattClient: BluetoothGatt? = null
    private var gattClientList = mutableListOf<BluetoothGatt?>(null, null, null)

    private var gattClientCallback: GattClientCallback? = null
    private var gattClientCallbackList = mutableListOf<GattClientCallback?>(null, null, null)

    // Properties for current chat device connection
    private var currentDevice: BluetoothDevice? = null
    private var currentDeviceList = mutableListOf<BluetoothDevice?>(null, null, null)

    private val _deviceConnection = MutableLiveData<DeviceConnectionState>()
    val deviceConnection = _deviceConnection as LiveData<DeviceConnectionState>

    private val _deviceConnection0 = MutableLiveData<DeviceConnectionState>()
    val deviceConnection0 = _deviceConnection0 as LiveData<DeviceConnectionState>

    private val _deviceConnection1 = MutableLiveData<DeviceConnectionState>()
    val deviceConnection1 = _deviceConnection1 as LiveData<DeviceConnectionState>

    private val _deviceConnection2 = MutableLiveData<DeviceConnectionState>()
    val deviceConnection2 = _deviceConnection2 as LiveData<DeviceConnectionState>

    val _deviceConnectionList = listOf(_deviceConnection0, _deviceConnection1, _deviceConnection2)
    val deviceConnectionList = listOf(deviceConnection0, deviceConnection1, deviceConnection2)

    private var gatt: BluetoothGatt? = null
    private var gattList = mutableListOf<BluetoothGatt?>(null, null, null)

    private var messageCharacteristic: BluetoothGattCharacteristic? = null
    private var messageCharacteristicList = mutableListOf<BluetoothGattCharacteristic?>(null, null, null)

    fun startServer(app: Application) {
        bluetoothManager = app.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (!adapter.isEnabled) {
            // prompt the user to enable bluetooth
            _requestEnableBluetooth.value = true
        } else {
            _requestEnableBluetooth.value = false
            setupGattServer(app)
            startAdvertisement()

            val ints: IntArray = intArrayOf(0, 1, 2)

            for (i in ints) {
                setupGattServer(app, i)
                startAdvertisement(i)
            }
        }
    }

    fun stopServer() {
        stopAdvertising()

        val ints: IntArray = intArrayOf(0, 1, 2)

        for (i in ints) {
            stopAdvertising(i)
        }
    }

    /**
     * The questions of how to obtain a device's own MAC address comes up a lot. The answer is
     * you cannot; it would be a security breach. Only system apps can get that permission.
     * Otherwise apps might use that address to fingerprint a device (e.g. for advertising, etc.)
     * A user can find their own MAC address through Settings, but apps cannot find it.
     * This method, which some might be tempted to use, returns a default value,
     * usually 02:00:00:00:00:00
     */
    fun getYourDeviceAddress(): String = bluetoothManager.adapter.address

    fun setCurrentChatConnection(device: BluetoothDevice) {
        currentDevice = device
        // Set gatt so BluetoothChatFragment can display the device data
        _deviceConnection.value = DeviceConnectionState.Connected(device)
        connectToChatDevice(device)
    }

    fun setCurrentChatConnection(device: BluetoothDevice, serverIndex: Int) {
        currentDeviceList[serverIndex] = device
        // Set gatt so BluetoothChatFragment can display the device data
        _deviceConnection.value = DeviceConnectionState.Connected(device)
        connectToChatDevice(device, serverIndex)
    }

    private fun connectToChatDevice(device: BluetoothDevice) {
        gattClientCallback = GattClientCallback()
        gattClient = device.connectGatt(app, false, gattClientCallback)
    }

    private fun connectToChatDevice(device: BluetoothDevice, serverIndex: Int) {
        gattClientCallbackList[serverIndex] = GattClientCallback()
        gattClientCallbackList[serverIndex]?.serverIndex = serverIndex
        gattClientList[serverIndex] = device.connectGatt(app, false, gattClientCallbackList[serverIndex])
    }

    fun sendMessage(message: String): Boolean {
        Log.d(TAG, "Send a message")
        messageCharacteristic?.let { characteristic ->
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            val messageBytes = message.toByteArray(Charsets.UTF_8)
            characteristic.value = messageBytes
            gatt?.let {
                val success = it.writeCharacteristic(messageCharacteristic)
                Log.d(TAG, "onServicesDiscovered: message send: $success")
                if (success) {
                    _messages.value = Message.LocalMessage(message)
                }
            } ?: run {
                Log.d(TAG, "sendMessage: no gatt connection to send a message with")
            }
        }
        return false
    }

    fun sendMessage(message: String, receiver: Int): Boolean {
        Log.d(TAG, "Send a message")
        messageCharacteristicList[receiver]?.let { characteristic ->
            characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT

            val messageBytes = message.toByteArray(Charsets.UTF_8)
            characteristic.value = messageBytes
            gattList[receiver]?.let {
                val success = it.writeCharacteristic(messageCharacteristicList[receiver])
                Log.d(TAG, "onServicesDiscovered: message send 2: $success")
                if (success) {
                    _messages.value = Message.LocalMessage(message)
                }
            } ?: run {
                Log.d(TAG, "sendMessage: no gatt connection to send a message with")
            }
        }
        return false
    }

    /**
     * Function to setup a local GATT server.
     * This requires setting up the available services and characteristics that other devices
     * can read and modify.
     */
    private fun setupGattServer(app: Application) {
        gattServerCallback = GattServerCallback()

        gattServer = bluetoothManager.openGattServer(
            app,
            gattServerCallback
        ).apply {
            val setupGattServiceInstance = setupGattService()
            try {
                setupGattServiceInstance?.let {
                    addService(it)
                }
            } catch (e:NullPointerException) {
                System.err.println("Null pointer exception");
            }
        }
    }

    private fun setupGattServer(app: Application, serverIndex: Int) {
        gattServerCallbackList[serverIndex] = GattServerCallback()
        gattServerCallbackList[serverIndex]?.serverIndex = serverIndex

        gattServerList[serverIndex] = bluetoothManager.openGattServer(
            app,
            gattServerCallbackList[serverIndex]
        ).apply {
            val setupGattServiceInstance = setupGattService(serverIndex)
            try {
                setupGattServiceInstance?.let {
                    addService(it)
                }
            } catch (e:NullPointerException) {
                System.err.println("Null pointer exception");
            }
        }
    }

    /**
     * Function to create the GATT Server with the required characteristics and descriptors
     */
    private fun setupGattService(): BluetoothGattService {
        // Setup gatt service
        val service = BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY)
        // need to ensure that the property is writable and has the write permission
        val messageCharacteristic = BluetoothGattCharacteristic(
            MESSAGE_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)
        val confirmCharacteristic = BluetoothGattCharacteristic(
            CONFIRM_UUID,
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(confirmCharacteristic)

        return service
    }

    private fun setupGattService(serviceIndex: Int): BluetoothGattService {
        // Setup gatt service
        val service = BluetoothGattService(SERVICE_UUIDS[serviceIndex], BluetoothGattService.SERVICE_TYPE_PRIMARY)
        // need to ensure that the property is writable and has the write permission
        val messageCharacteristic = BluetoothGattCharacteristic(
            MESSAGE_UUIDS[serviceIndex],
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(messageCharacteristic)
        val confirmCharacteristic = BluetoothGattCharacteristic(
            CONFIRM_UUIDS[serviceIndex],
            BluetoothGattCharacteristic.PROPERTY_WRITE,
            BluetoothGattCharacteristic.PERMISSION_WRITE
        )
        service.addCharacteristic(confirmCharacteristic)

        return service
    }

    /**
     * Start advertising this device so other BLE devices can see it and connect
     */
    private fun startAdvertisement() {
        advertiser = adapter.bluetoothLeAdvertiser
        Log.d(TAG, "startAdvertisement: with advertiser $advertiser")

        if (advertiseCallback == null) {
            advertiseCallback = DeviceAdvertiseCallback()

            advertiser?.startAdvertising(advertiseSettings, advertiseData, advertiseCallback)
        }
    }

    private fun startAdvertisement(serverIndex: Int) {
        advertiserList[serverIndex] = adapter.bluetoothLeAdvertiser
        val advertiser = advertiserList[serverIndex]
        Log.d(TAG, "startAdvertisement: with advertiser $advertiser")

        if (advertiseCallbackList[serverIndex] == null) {
            advertiseCallbackList[serverIndex] = DeviceAdvertiseCallback()

            advertiserList[serverIndex]?.startAdvertising(advertiseSettings, advertiseDataList[serverIndex], advertiseCallbackList[serverIndex])
        }
    }

    /**
     * Stops BLE Advertising.
     */
    private fun stopAdvertising() {
        Log.d(TAG, "Stopping Advertising with advertiser $advertiser")
        advertiser?.stopAdvertising(advertiseCallback)
        advertiseCallback = null
        gattServer?.close()
    }

    private fun stopAdvertising(serverIndex: Int) {
        val advertiser = advertiserList[serverIndex]
        Log.d(TAG, "Stopping Advertising with advertiser $advertiser")
        advertiserList[serverIndex]?.stopAdvertising(advertiseCallbackList[serverIndex])
        advertiseCallbackList[serverIndex] = null
        gattServerList[serverIndex]?.close()
    }

    /**
     * Returns an AdvertiseData object which includes the Service UUID and Device Name.
     */
    private fun buildAdvertiseData(): AdvertiseData {
        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         * This limit is outlined in section 2.3.1.1 of this document:
         * https://inst.eecs.berkeley.edu/~ee290c/sp18/note/BLE_Vol6.pdf
         *
         * This limit includes everything put into AdvertiseData including UUIDs, device info, &
         * arbitrary service or manufacturer data.
         * Attempting to send packets over this limit will result in a failure with error code
         * AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         * onStartFailure() method of an AdvertiseCallback implementation.
         */
        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUID))
            .setIncludeDeviceName(true)

        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());
        return dataBuilder.build()
    }

    private fun buildAdvertiseData(serverIndex: Int): AdvertiseData {
        /**
         * Note: There is a strict limit of 31 Bytes on packets sent over BLE Advertisements.
         * This limit is outlined in section 2.3.1.1 of this document:
         * https://inst.eecs.berkeley.edu/~ee290c/sp18/note/BLE_Vol6.pdf
         *
         * This limit includes everything put into AdvertiseData including UUIDs, device info, &
         * arbitrary service or manufacturer data.
         * Attempting to send packets over this limit will result in a failure with error code
         * AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE. Catch this error in the
         * onStartFailure() method of an AdvertiseCallback implementation.
         */
        val dataBuilder = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid(SERVICE_UUIDS[serverIndex]))
            .setIncludeDeviceName(true)

        /* For example - this will cause advertising to fail (exceeds size limit) */
        //String failureData = "asdghkajsghalkxcjhfa;sghtalksjcfhalskfjhasldkjfhdskf";
        //dataBuilder.addServiceData(Constants.Service_UUID, failureData.getBytes());
        return dataBuilder.build()
    }

    /**
     * Returns an AdvertiseSettings object set to use low power (to help preserve battery life)
     * and disable the built-in timeout since this code uses its own timeout runnable.
     */
    private fun buildAdvertiseSettings(): AdvertiseSettings {
        return AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTimeout(0)
            .build()
    }

    /**
     * Custom callback for the Gatt Server this device implements
     */
    private class GattServerCallback : BluetoothGattServerCallback() {
        public var serverIndex:Int? = null

        override fun onConnectionStateChange(device: BluetoothDevice, status: Int, newState: Int) {
            super.onConnectionStateChange(device, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(
                TAG,
                "onConnectionStateChange: Server $device ${device.name} success: $isSuccess connected: $isConnected"
            )
            if (isSuccess && isConnected) {
                if(serverIndex != null) {
                    _connectionRequestList[serverIndex!!].postValue(device)
                }
                else {
                    _connectionRequest.postValue(device)
                }
            } else {
                if(serverIndex != null) {
                    _deviceConnectionList[serverIndex!!].postValue(DeviceConnectionState.Disconnected)
                }
                else {
                    _deviceConnection.postValue(DeviceConnectionState.Disconnected)
                }
            }
        }

        override fun onCharacteristicWriteRequest(
            device: BluetoothDevice,
            requestId: Int,
            characteristic: BluetoothGattCharacteristic,
            preparedWrite: Boolean,
            responseNeeded: Boolean,
            offset: Int,
            value: ByteArray?
        ) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value)
            if (characteristic.uuid == MESSAGE_UUID) {
                gattServer?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                val message = value?.toString(Charsets.UTF_8)
                Log.d(TAG, "onCharacteristicWriteRequest: Have message: \"$message\"")
                message?.let {
                    _messages.postValue(RemoteMessage(it))
                }
            }
            else {
                val messageUuidIndex = MESSAGE_UUIDS.indexOfFirst {
                    it.toString() == characteristic.uuid.toString()
                }
                if(messageUuidIndex != -1) {
                    gattServerList[messageUuidIndex]?.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null)
                    val message = value?.toString(Charsets.UTF_8)
                    Log.d(TAG, "onCharacteristicWriteRequest: Have message: \"$message\"")
                    message?.let {
                        _messages.postValue(RemoteMessage(it))
                    }
                }
            }
        }
    }

    private class GattClientCallback : BluetoothGattCallback() {

        public var serverIndex: Int? = null

        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            val isSuccess = status == BluetoothGatt.GATT_SUCCESS
            val isConnected = newState == BluetoothProfile.STATE_CONNECTED
            Log.d(TAG, "onConnectionStateChange: Client $gatt  success: $isSuccess connected: $isConnected")
            // try to send a message to the other device as a test
            if (isSuccess && isConnected) {
                // discover services
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(discoveredGatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(discoveredGatt, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "onServicesDiscovered: Have gatt $discoveredGatt")

                if(serverIndex != null) {
                    gattList[serverIndex!!] = discoveredGatt
                    val service = discoveredGatt.getService(SERVICE_UUIDS[serverIndex!!])
                    messageCharacteristicList[serverIndex!!] = service.getCharacteristic(MESSAGE_UUIDS[serverIndex!!])
                }
                else {
                    gatt = discoveredGatt
                    val service = discoveredGatt.getService(SERVICE_UUID)
                    messageCharacteristic = service.getCharacteristic(MESSAGE_UUID)
                }
            }
        }
    }

    /**
     * Custom callback after Advertising succeeds or fails to start. Broadcasts the error code
     * in an Intent to be picked up by AdvertiserFragment and stops this Service.
     */
    private class DeviceAdvertiseCallback : AdvertiseCallback() {
        override fun onStartFailure(errorCode: Int) {
            super.onStartFailure(errorCode)
            // Send error state to display
            val errorMessage = "Advertise failed with error: $errorCode"
            Log.d(TAG, "Advertising failed")
            //_viewState.value = DeviceScanViewState.Error(errorMessage)
        }

        override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
            super.onStartSuccess(settingsInEffect)
            Log.d(TAG, "Advertising successfully started")
        }
    }
}