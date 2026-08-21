package com.example.prototype.map.fab

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.prototype.R

@Composable
fun ExpandableFabMenu(
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val painterSource = if (expanded) R.drawable.ic_home else R.drawable.ic_favorite

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        val shape = if (expanded) RoundedCornerShape(16.dp) else CircleShape

        if (expanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .clickable(
                        enabled = true,
                        onClick = { expanded = false }
                    ),
                contentAlignment = Alignment.BottomEnd,
            ) {
                Box(
                    modifier = Modifier
                        .width(280.dp)
                        .height(400.dp)
                        .border(
                            width = 3.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = shape
                        )
                        .background(MaterialTheme.colorScheme.background)
                        .clickable(
                            enabled = false,
                            onClick = {}
                        ),
                ) {
                    Box(
                        contentAlignment = Alignment.TopEnd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp)
                    ) {
                        Button(
                            onClick = { expanded = false },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.Transparent)
                                .border(0.dp, Color.Transparent)
                        ) {
                            Icon(
                                painterResource(R.drawable.ic_close),
                                contentDescription = "메뉴 닫기",
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color.Transparent)
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        content()
                    }
                }
            }
        } else {
            FloatingActionButton(
                onClick = { expanded = true },
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                shape = shape,
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = shape
                    )
                    .shadow(0.dp),
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 0.dp,
                    pressedElevation = 0.dp,
                    focusedElevation = 0.dp,
                    hoveredElevation = 0.dp
                )
            ) {
                Icon(
                    painter = painterResource(painterSource),
                    contentDescription = "메뉴 열기",
                    modifier = Modifier.padding(all = 6.dp)
                )
            }
        }

    }
}

