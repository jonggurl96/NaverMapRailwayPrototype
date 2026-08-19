package com.example.prototype

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.prototype.map.fab.ExpandableFabMenu
import com.example.prototype.map.fab.vo.MapTypeMetadata
import com.example.prototype.map.layers.RailwayWeatherMapScreen
import com.example.prototype.map.ui.checkbox.SliderSwitch
import com.example.prototype.ui.theme.PrototypeTheme
import com.naver.maps.map.compose.MapType

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PrototypeTheme {
                PrototypeApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@PreviewScreenSizes
@Composable
fun PrototypeApp() {

    val colorTheme = MaterialTheme.colorScheme

    val mapTypeMetadataList: List<MapTypeMetadata> = listOf(
        MapTypeMetadata(MapType.Basic, 0, false),
        MapTypeMetadata(MapType.Satellite, 1, false),
        MapTypeMetadata(MapType.Basic, 2, true)
    )

    var metadataIndex: Int by rememberSaveable { mutableIntStateOf(0) }

//    현재 표시 맵 타입
    val mapType: MapType = mapTypeMetadataList[metadataIndex].getMapType()

//    현재 표시 맵 타입의 다크 모드 여부
    val isDarkMode: Boolean = mapTypeMetadataList[metadataIndex].isDarkMode()

//    날씨 표시 여부
    var weatherVisible: Boolean by rememberSaveable { mutableStateOf(false) }

//    철도 표시 여부
    var railwayVisible: Boolean by rememberSaveable { mutableStateOf(false) }

//    현재 위치 표시 여부
    var locationVisible: Boolean by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(text = "Prototype") },
                modifier = Modifier.background(colorTheme.primary)
            )
        },
        floatingActionButton = {
            ExpandableFabMenu {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MapTileSelector(metadataIndex, changeMapType = { metadataIndex = it })

                    MapLayerRoundSlider(
                        label = "날씨",
                        state = weatherVisible,
                        setState = { weatherVisible = it }
                    )

                    MapLayerRoundSlider(
                        label = "철도",
                        state = railwayVisible,
                        setState = { railwayVisible = it }
                    )

                    MapLayerRoundSlider(
                        label = "위치",
                        state = locationVisible,
                        setState = { locationVisible = it }
                    )
                }
            }
        }
    ) { innerPadding ->
        RailwayWeatherMapScreen(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(innerPadding),
            mapType = mapType,
            isDarkMode = isDarkMode
        )
    }
}

@Composable
private fun FabMenuInlineItem(
    label: String,
    content: @Composable () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .height(30.dp)
    ) {
        Text(
            text = label,
            modifier = Modifier
                .fillMaxWidth(0.2f)
                .background(color = Color.Transparent),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Surface(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight()
                .padding(all = 2.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun MapTileSelector(
    metadataIndex: Int,
    changeMapType: (Int) -> Unit
) {

    val mapTypeLabels = listOf("기본지도", "위성지도", "다크지도")

    FabMenuInlineItem(label = "지도") {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = bdr32dp()
                )
                .border(
                    BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = bdr32dp()
                )
                .clip(bdr32dp()),
            horizontalArrangement = Arrangement.End
        ) {
            mapTypeLabels.forEachIndexed { index, mapTypeLabel ->
                TextButton(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .defaultMinSize(minHeight = 0.dp, minWidth = 0.dp),
                    onClick = {
                        changeMapType(index)
                    },
                    shape = RectangleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = if (index == metadataIndex) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.background
                        },
                    )
                ) {
                    Text(
                        text = mapTypeLabel,
                        fontSize = 8.sp,
                        color = if (index == metadataIndex) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            MaterialTheme.colorScheme.onBackground
                        },
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
private fun MapLayerRoundSlider(
    label: String,
    state: Boolean,
    setState: (Boolean) -> Unit
) {
    FabMenuInlineItem(label) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            SliderSwitch(
                checked = state,
                onCheckedChange = setState
            )
        }
    }
}

private fun bdr32dp(): RoundedCornerShape {
    return RoundedCornerShape(32.dp)
}