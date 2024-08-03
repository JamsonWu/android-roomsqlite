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

package com.example.inventory.ui.item

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.Item
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve, update and delete an item from the [ItemsRepository]'s data source.
 */
class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    // 这个ItemId是如何存的？在哪里触发？
    // 路由跳转时什么时候将值传给了savedStateHandle呢？
    private val itemId: Int = checkNotNull(savedStateHandle[ItemDetailsDestination.itemIdArg])

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }

//    var uiState2 by mutableStateOf(ItemDetailsUiState())
//        private set

//    init {
//        viewModelScope.launch {
//            uiState2 = ItemDetailsUiState(
//                itemDetails =
//                itemsRepository.getItemStream(itemId)
//                    .filterNotNull()
//                    .first()
//                    .toItemDetails()
//            )
//        }
//    }


    // 从Flow中读取数据
    // 需要提供一个方法读取指定的记录
    // 读取一条记录也是Flow，也要转为StateFlow
    // 这里使用 StateFlow的好处是：数据库数据发生变化了，详情那边会自动更新
    // 如果在init内使用协程上下文读取数据，那么详情页就不会自动更新了
    // 目前的设计：详情页的数据需要用到StateFlow，这样数据修改了也能在详情页体现出来
    val uiState: StateFlow<ItemDetailsUiState> =
        itemsRepository.getItemStream(itemId)
            .filterNotNull()
            .map {
                ItemDetailsUiState(itemDetails = it.toItemDetails())
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = ItemDetailsUiState()
            )

    // 删除记录
    fun deleteItem(item: Item) {
        viewModelScope.launch {
            // 删除记录方法是异步的，要在协程环境执行
            itemsRepository.deleteItem(item)
        }
    }

    // 删除记录
    suspend fun deleteItem2(item: Item) {
        itemsRepository.deleteItem(item)
    }

}

/**
 * UI state for ItemDetailsScreen
 */
data class ItemDetailsUiState(
    val outOfStock: Boolean = true,
    val itemDetails: ItemDetails = ItemDetails()
)
