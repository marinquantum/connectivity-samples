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
import java.util.*

/**
 * Constants for use in the Bluetooth LE Chat sample
 */
/**
 * UUID identified with this app - set as Service UUID for BLE Chat.
 *
 * Bluetooth requires a certain format for UUIDs associated with Services.
 * The official specification can be found here:
 * [://www.bluetooth.org/en-us/specification/assigned-numbers/service-discovery][https]
 */
val SERVICE_UUID: UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb")

/**
 * Creating multiple services to support more than one connection at a time
 */
val SERVICE_UUID1: UUID = UUID.fromString("0000e235-0000-1000-8000-00805f9b34fb")
val SERVICE_UUID2: UUID = UUID.fromString("0000bba7-0000-1000-8000-00805f9b34fb")
val SERVICE_UUID3: UUID = UUID.fromString("000036ae-0000-1000-8000-00805f9b34fb")

val SERVICE_UUIDS = listOf(SERVICE_UUID1, SERVICE_UUID2, SERVICE_UUID3)

/**
 * UUID for the message
 */
val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")

val MESSAGE_UUID1: UUID = UUID.fromString("7eccbba7-b943-45f6-9c65-2fc058f64acf")
val MESSAGE_UUID2: UUID = UUID.fromString("596936ae-68e9-40a1-8f86-3949d1b56de4")
val MESSAGE_UUID3: UUID = UUID.fromString("8de24b16-5926-4018-bede-24bc0c516962")

val MESSAGE_UUIDS = listOf(MESSAGE_UUID1, MESSAGE_UUID2, MESSAGE_UUID3)

/**
 * UUID to confirm device connection
 */
val CONFIRM_UUID: UUID = UUID.fromString("36d4dc5c-814b-4097-a5a6-b93b39085928")

val CONFIRM_UUID1: UUID = UUID.fromString("83906bca-cedd-4052-8239-ccc407b0464e")
val CONFIRM_UUID2: UUID = UUID.fromString("d4901856-4020-4a00-b70a-8036eca03bad")
val CONFIRM_UUID3: UUID = UUID.fromString("24413266-ad62-4fd0-bd93-1649d0870f35")

val CONFIRM_UUIDS = listOf(CONFIRM_UUID1, CONFIRM_UUID2, CONFIRM_UUID3)

const val REQUEST_ENABLE_BT = 1
