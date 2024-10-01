package com.epicmillennium.cheshmap.presentation.ui.components


import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun onDebounceClick(
    debounceInterval: Long = 700,
    onClick: () -> Unit,
): () -> Unit {
    var lastClickTimeMillis by remember { mutableLongStateOf(0L) }
    return {
        System.currentTimeMillis().let { currentTimeMillis ->
            if ((currentTimeMillis - lastClickTimeMillis) >= debounceInterval) {
                lastClickTimeMillis = currentTimeMillis
                onClick()
            }
        }
    }
}