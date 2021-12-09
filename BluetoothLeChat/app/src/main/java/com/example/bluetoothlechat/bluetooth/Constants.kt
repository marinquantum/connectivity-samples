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
val SERVICE_UUID: UUID = UUID.fromString("3032454c-426b-7261-5074-72616d536557")

/**
 * UUID for the message
 */
val MESSAGE_UUID: UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b")

val MESSAGE_UUID1: UUID = UUID.fromString("00008a01-0000-1000-8000-00805f9b34fb")
val MESSAGE_UUID2: UUID = UUID.fromString("00008a02-0000-1000-8000-00805f9b34fb")
val MESSAGE_UUID3: UUID = UUID.fromString("00008a03-0000-1000-8000-00805f9b34fb")

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
