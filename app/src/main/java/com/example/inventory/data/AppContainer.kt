/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory.data

import android.content.Context

/**
 * App container for Dependency injection.
 * 定义数据库仓库接口
 */
interface AppContainer {
    // 表items对应的数据库仓库
    val itemsRepository: ItemsRepository
    // 其它表数据仓库
}

/**
 * [AppContainer] implementation that provides instance of [OfflineItemsRepository]
 * 接口实现类，接口只是定义了一个属性，实现类给这个属性赋值
 */
class AppDataContainer(private val context: Context) : AppContainer {
    /**
     * Implementation for [ItemsRepository]
     * 实现数据库仓库ItemsRepository
     * 由于使用了lazy懒加载，当初次使用时才会进行初始化
     */
    override val itemsRepository: ItemsRepository by lazy {
        // 仓库初始化
        OfflineItemsRepository(InventoryDatabase.getDatabase(context).itemDao())
    }

    // 如果有多张表，那这里要创建多个仓库
}
