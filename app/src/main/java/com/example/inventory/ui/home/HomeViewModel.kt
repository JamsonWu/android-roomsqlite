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

package com.example.inventory.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve all items in the Room database.
 */
class HomeViewModel(private val itemsRepository: ItemsRepository) : ViewModel() {
    // companion是特殊的单例对象，仅在作用域内可见
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
    // 由于接口getAllItemsStream返回的是数据流Flow,存在以下问题
    // 1.当横屏竖屏切换时Activity会重新创建，那么就要重新订阅这个流
    // 2.如果没有人订阅这个流或组件生命周期结束，希望能取消订阅这个流
    // 希望屏幕旋转时能缓存状态不要重新订阅
    // ViewModel的StateFlow可以达到这个目的，那么Flow如何转为StateFlow呢？
    // 需要使用stateIn操作符
    // 当数据库数据发生变化时会自动更新，通知UI更新
    val homeUiState: StateFlow<HomeUiState> =
        // HomeUiState(it)将Item为HomeUiState
        itemsRepository.getAllItemsStream().map {
            // 将数据库返回的列表数据存到数据类中，实例化数据类
            HomeUiState(it)
        }
            .stateIn(
                // scope - The viewModelScope defines the lifecycle of the StateFlow.
                // When the viewModelScope is canceled, the StateFlow is also canceled.
                // 当viewModelScope上下文取消了，自动取消流订阅
                scope = viewModelScope,
                // The pipeline should only be active when the UI is visible
                // 当UI可见时才激活，当UI不可见时延迟取消订阅
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                // 设置初始值
                initialValue = HomeUiState()
            )
}

/**
 * Ui State for HomeScreen
 * ui状态数据类，有一个字段保存数据库列表数据
 * UiState都要创建一个数据类，具体数据存在属性中
 */
data class HomeUiState(val itemList: List<Item> = listOf())
