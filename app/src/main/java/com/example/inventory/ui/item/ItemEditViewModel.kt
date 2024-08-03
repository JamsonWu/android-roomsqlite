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
import com.example.inventory.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve and update an item from the [ItemsRepository]'s data source.
 */
class ItemEditViewModel(
    // 外部传入，实现关注点分离，ViewModel中不需要知道用什么状态持久化工具
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
    /**
     * Holds current item ui state
     * 这里只是可变状态，并不是流式 MutableStateFlow
     * 这里只是普通可观察状态
     */
    var itemUiState by mutableStateOf(ItemUiState())
        private set

    // 使用StateFlow协程用到
    // var itemUiStateB = MutableStateFlow(ItemUiState())

    // 生命周期内的状态保存:A handle to saved state passed down to androidx. lifecycle. ViewModel.
    // 状态持久化：savedStateHandle允许你将数据（如用户输入、选择项等）保存在Bundle中，
    // 这样即使在应用的生命周期事件（如屏幕旋转）发生时，这些数据也不会丢失。
    // ItemEditDestination.itemIdArg 对当前的ItemId会持久化
    private val itemId: Int = checkNotNull(savedStateHandle[ItemEditDestination.itemIdArg])

    // 在viewModel初始化时通过协程上下文发起异步读取数据库流
    // 使用这个方法可能会有UI渲染问题
    init {
        viewModelScope.launch {
           itemUiState = itemsRepository.getItemStream(itemId)
               // 不为空时才过滤
               .filterNotNull()
               // 取第一条
               .first()
               .toItemUiState()
        }
    }

    // 需要从数据库中读取数据
    //    val itemUiState: StateFlow<ItemUiState> =
    //        // HomeUiState(it)将Item为HomeUiState
    //        itemsRepository.getItemStream(itemId).map {
    //            // 将数据库返回的列表数据存到数据类中，实例化数据类
    //            // 这里it可能为空如何处理
    //            // it?.toItemDetails() 如果it不为空，则转为Item
    //            // 如果为空，则使用默认值
    //            ItemUiState(itemDetails = it?.toItemDetails() ?: ItemDetails())
    //        }
    //            .stateIn(
    //                // scope - The viewModelScope defines the lifecycle of the StateFlow.
    //                // When the viewModelScope is canceled, the StateFlow is also canceled.
    //                // 当viewModelScope上下文取消了，自动取消流订阅
    //                scope = viewModelScope,
    //                // The pipeline should only be active when the UI is visible
    //                // 当UI可见时才激活，当UI不可见时延迟取消订阅
    //                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
    //                // 设置初始值
    //                initialValue = ItemUiState()
    //            )


    // 校验明细数据
    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        // with语法
        return with(uiState) {
            name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
        }
    }

    fun updateUiState(itemDetails: ItemDetails) {
       itemUiState = ItemUiState(itemDetails = itemDetails, isEntryValid = validateInput(itemDetails))
    }

    // 这里是组件可以直接调用
    // 而新增时在ViewModel中是声明异步方法，在新增组件那里协程环境调用
    // 这两种方法到底哪种更合适？
    fun saveItem(){
        // 由于updateItem是异步方法所以需要在协程上下文调用
        viewModelScope.launch {
            if (validateInput(itemUiState.itemDetails)) {
                itemsRepository.updateItem(itemUiState.itemDetails.toItem())
            }

        }

    }
}