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

package com.example.inventory.ui

import android.app.Application
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.inventory.InventoryApplication
import com.example.inventory.ui.home.HomeViewModel
import com.example.inventory.ui.item.ItemDetailsViewModel
import com.example.inventory.ui.item.ItemEditViewModel
import com.example.inventory.ui.item.ItemEntryViewModel

/**
 * Provides Factory to create instance of ViewModel for the entire Inventory app
 * 应用内所有ViewModel实例化的工厂
 * ViewModel中需要参数的地方在工厂这里传入
 */
object AppViewModelProvider {
    // 为什么要使用 viewModelFactory呢？
    // 因为viewModel不能带参数，
    // 所以只能通过 viewModelFactory来实现带参数实例化Model
    val Factory = viewModelFactory {
        // 调用InitializerViewModelFactoryBuilder.initializer
        // Initializer for ItemEditViewModel
        // 初始化ViewModel
        initializer {
            ItemEditViewModel(
                // 关注点分离，ViewModel专注处理UI有关的数据模型
                // createSavedStateHandle 会保存路由导航传递过来的参数
                // 直接通过这个实例的map对象中直接读取即可
                this.createSavedStateHandle()
            )
        }
        // Initializer for ItemEntryViewModel
        initializer {
            ItemEntryViewModel(inventoryApplication().container.itemsRepository)
        }

        // Initializer for ItemDetailsViewModel
        initializer {
            ItemDetailsViewModel(
                this.createSavedStateHandle(),
                inventoryApplication().container.itemsRepository
            )
        }

        // Initializer for HomeViewModel
        initializer {
            HomeViewModel(inventoryApplication().container.itemsRepository)
        }
    }
}

/**
 * Extension function to queries for [Application] object and returns an instance of
 * [InventoryApplication].
 * 给ViewModelProvider提供扩展函数
 */
fun CreationExtras.inventoryApplication(): InventoryApplication {
    // AndroidViewModelFactory.APPLICATION_KEY 这是Factory保存Application的一个KEY
    return this[AndroidViewModelFactory.APPLICATION_KEY] as InventoryApplication
}


