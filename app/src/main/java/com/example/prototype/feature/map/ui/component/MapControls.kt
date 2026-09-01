package com.example.prototype.feature.map.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prototype.core.ui.component.SliderSwitch
import com.example.prototype.feature.map.model.MapStyleOption

@Composable
fun MapStyleSelector(selectedIndex: Int, onStyleSelected: (Int) -> Unit) {
    MapControlRow(label = "지도") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(Color.Transparent, ControlShape)
                .border(BorderStroke(2.dp, MaterialTheme.colorScheme.outline), ControlShape)
                .clip(ControlShape),
            horizontalArrangement = Arrangement.End
        ) {
            MapStyleOption.entries.forEachIndexed { index, option ->
                TextButton(
                    modifier = Modifier.weight(1f).fillMaxHeight()
                        .defaultMinSize(minHeight = 0.dp, minWidth = 0.dp),
                    onClick = { onStyleSelected(index) },
                    shape = RectangleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (index == selectedIndex) {
                            MaterialTheme.colorScheme.primary
                        } else MaterialTheme.colorScheme.background
                    )
                ) {
                    Text(
                        text = option.label,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (index == selectedIndex) {
                            MaterialTheme.colorScheme.onPrimary
                        } else MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun MapLayerSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    MapControlRow(label) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            SliderSwitch(checked, onCheckedChange, width = 64.dp, height = 32.dp)
        }
    }
}

@Composable
private fun MapControlRow(label: String, content: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(40.dp)) {
        Text(
            text = label,
            modifier = Modifier.fillMaxWidth(0.2f).background(Color.Transparent),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Surface(
            modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight().padding(2.dp)
        ) { content() }
    }
}

private val ControlShape = RoundedCornerShape(32.dp)
