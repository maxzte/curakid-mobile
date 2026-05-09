package com.misterio.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.misterio.core.ui.theme.CurakidTheme


/**
 * Adds a single item to a [LazyListScope] with standard horizontal padding.
 *
 * @param key A stable and unique key representing the item.
 * @param contentType The type of the content of this item.
 * @param content The composable content to be displayed inside the item wrapper.
 */
fun LazyListScope.itemWrapper(
    key: Any? = null,
    contentType: Any? = null,
    content: @Composable () -> Unit,
) {
    item(
        key = key,
        contentType = contentType
    ) {
        Box(
            modifier = Modifier.padding(horizontal = CurakidTheme.paddings.medium)
        ) {
            content()
        }
    }
}

