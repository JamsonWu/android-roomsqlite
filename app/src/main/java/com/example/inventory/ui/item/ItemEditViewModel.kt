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
import com.example.inventory.data.ItemsRepository

/**
 * ViewModel to retrieve and update an item from the [ItemsRepository]'s data source.
 */
class ItemEditViewModel(
    // 外部传入，实现关注点分离，ViewModel中不需要知道用什么状态持久化工具
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

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

    // 校验明细数据
    private fun validateInput(uiState: ItemDetails = itemUiState.itemDetails): Boolean {
        // with语法
        return with(uiState) {
            name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank()
        }
    }
}
