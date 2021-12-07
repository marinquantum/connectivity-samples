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
package com.example.bluetoothlechat.chat

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bluetoothlechat.bluetooth.Message
import com.example.bluetoothlechat.R
import com.example.bluetoothlechat.bluetooth.ChatServer
import com.example.bluetoothlechat.databinding.FragmentBluetoothChatBinding
import com.example.bluetoothlechat.gone
import com.example.bluetoothlechat.visible

private const val TAG = "BluetoothChatFragment"

class BluetoothChatFragment : Fragment() {

    private var _binding: FragmentBluetoothChatBinding? = null
    // this property is valid between onCreateView and onDestroyView.
    private val binding: FragmentBluetoothChatBinding
        get() = _binding!!

    private var connectedDevices = mutableListOf<String>()

    private val deviceConnectionObserver = Observer<DeviceConnectionState> { state ->
        when(state) {
            is DeviceConnectionState.Connected -> {
                val device = state.device
                Log.d(TAG, "Gatt connection observer: have device $device")
                chatWith(device)
                if(!connectedDevices.contains("device")) {
                    connectedDevices.add("device")
                }
            }
            is DeviceConnectionState.Disconnected -> {
                if(connectedDevices.contains("device")) {
                    connectedDevices.remove("device")
                }
                if(connectedDevices.count() == 0) {
                    showDisconnected()
                }
            }
        }
    }

    private val deviceConnectionObserver0 = Observer<DeviceConnectionState> { state ->
        when(state) {
            is DeviceConnectionState.Connected -> {
                val device = state.device
                Log.d(TAG, "Gatt connection observer 0: have device $device")
                chatWith(device, 0)
                if(!connectedDevices.contains("device0")) {
                    connectedDevices.add("device0")
                }
            }
            is DeviceConnectionState.Disconnected -> {
                if(connectedDevices.contains("device0")) {
                    connectedDevices.remove("device0")
                }
                if(connectedDevices.count() == 0) {
                    showDisconnected()
                }
            }
        }
    }

    private val deviceConnectionObserver1 = Observer<DeviceConnectionState> { state ->
        when(state) {
            is DeviceConnectionState.Connected -> {
                val device = state.device
                Log.d(TAG, "Gatt connection observer 1: have device $device")
                chatWith(device, 1)
                if(!connectedDevices.contains("device1")) {
                    connectedDevices.add("device1")
                }
            }
            is DeviceConnectionState.Disconnected -> {
                if(connectedDevices.contains("device1")) {
                    connectedDevices.remove("device1")
                }
                if(connectedDevices.count() == 0) {
                    showDisconnected()
                }
            }
        }
    }

    private val deviceConnectionObserver2 = Observer<DeviceConnectionState> { state ->
        when(state) {
            is DeviceConnectionState.Connected -> {
                val device = state.device
                Log.d(TAG, "Gatt connection observer 2: have device $device")
                chatWith(device, 2)
                if(!connectedDevices.contains("device2")) {
                    connectedDevices.add("device2")
                }
            }
            is DeviceConnectionState.Disconnected -> {
                if(connectedDevices.contains("device2")) {
                    connectedDevices.remove("device2")
                }
                if(connectedDevices.count() == 0) {
                    showDisconnected()
                }
            }
        }
    }

    private val connectionRequestObserver = Observer<BluetoothDevice> { device ->
        Log.d(TAG, "Connection request observer: have device $device")
        ChatServer.setCurrentChatConnection(device)
    }

    private val connectionRequestObserver0 = Observer<BluetoothDevice> { device ->
        Log.d(TAG, "Connection request observer 0: have device $device")
        ChatServer.setCurrentChatConnection(device, 0)
    }

    private val connectionRequestObserver1 = Observer<BluetoothDevice> { device ->
        Log.d(TAG, "Connection request observer 1: have device $device")
        ChatServer.setCurrentChatConnection(device, 1)
    }

    private val connectionRequestObserver2 = Observer<BluetoothDevice> { device ->
        Log.d(TAG, "Connection request observer 2: have device $device")
        ChatServer.setCurrentChatConnection(device, 2)
    }

    private val messageObserver = Observer<Message> { message ->
        Log.d(TAG, "Have message ${message.text}")
        adapter.addMessage(message)
    }

    private val adapter = MessageAdapter()

    private val inputMethodManager by lazy {
        requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBluetoothChatBinding.inflate(inflater, container, false)

        Log.d(TAG, "chatWith: set adapter $adapter")
        binding.messages.layoutManager = LinearLayoutManager(context)
        binding.messages.adapter = adapter

        showDisconnected()

        binding.connectDevices.setOnClickListener {
            findNavController().navigate(R.id.action_find_new_device)
        }

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        requireActivity().setTitle(R.string.chat_title)
        ChatServer.connectionRequest.observe(viewLifecycleOwner, connectionRequestObserver)
        ChatServer.connectionRequestList[0].observe(viewLifecycleOwner, connectionRequestObserver0)
        ChatServer.connectionRequestList[1].observe(viewLifecycleOwner, connectionRequestObserver1)
        ChatServer.connectionRequestList[2].observe(viewLifecycleOwner, connectionRequestObserver2)

        ChatServer.deviceConnection.observe(viewLifecycleOwner, deviceConnectionObserver)
        ChatServer.deviceConnectionList[0].observe(viewLifecycleOwner, deviceConnectionObserver0)
        ChatServer.deviceConnectionList[1].observe(viewLifecycleOwner, deviceConnectionObserver1)
        ChatServer.deviceConnectionList[2].observe(viewLifecycleOwner, deviceConnectionObserver2)
        ChatServer.messages.observe(viewLifecycleOwner, messageObserver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun chatWith(device: BluetoothDevice) {
        binding.connectedContainer.visible()
        binding.notConnectedContainer.gone()

        val chattingWithString = resources.getString(R.string.chatting_with_device, device.address)
        binding.connectedDeviceName.text = chattingWithString
        binding.sendMessage.setOnClickListener {
            val message = binding.messageText.text.toString()
            // only send message if it is not empty
            if (message.isNotEmpty()) {
                ChatServer.sendMessage(message)
                // clear message
                binding.messageText.setText("")
            }
        }
    }

    private fun chatWith(device: BluetoothDevice, serverIndex: Int) {
        binding.connectedContainer.visible()
        binding.notConnectedContainer.gone()

        val chattingWithString = resources.getString(R.string.chatting_with_device, device.address)
        binding.connectedDeviceName.text = chattingWithString
        binding.sendMessage.setOnClickListener {
            val message = binding.messageText.text.toString()
            // only send message if it is not empty
            if (message.isNotEmpty()) {
                ChatServer.sendMessage(message, serverIndex)
                // clear message
                binding.messageText.setText("")
            }
        }
    }

    private fun showDisconnected() {
        hideKeyboard()
        binding.notConnectedContainer.visible()
        binding.connectedContainer.gone()
    }

    private fun hideKeyboard() {
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}