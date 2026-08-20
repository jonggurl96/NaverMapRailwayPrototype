package com.example.prototype.map.fab

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.prototype.R

@Composable
fun ExpandableFabMenu(
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val painterSource = if (expanded) R.drawable.ic_home else R.drawable.ic_favorite

    val shape = if (expanded) RoundedCornerShape(16.dp) else CircleShape

    val expandedModifier = if (expanded) {
        Modifier
            .width(200.dp)
            .height(180.dp)
    } else {
        Modifier
            .width(40.dp)
            .height(40.dp)
    }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        FloatingActionButton(
            onClick = { expanded = true },
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary,
            shape = shape,
            modifier = expandedModifier.border(
                width = 3.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = shape
            ),
        ) {
            if (expanded) {
                content()
            } else {
                Icon(
                    painter = painterResource(painterSource),
                    contentDescription = if (expanded) "메뉴 닫기" else "메뉴 열기",
                    modifier = Modifier.padding(all = 6.dp)
                )
            }
        }
    }
}

