package kz.market.presentation.screens.dashboard

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.Pie
import ir.ehsannarmani.compose_charts.models.PopupProperties
import kotlinx.coroutines.launch
import kz.market.R
import kz.market.presentation.theme.MarketTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onDetailsClick: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Dashboard")
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                snackBarHostState.showSnackbar(
                                    message = "Action clicked",
                                    withDismissAction = true
                                )
                            }
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_package_add),
                            contentDescription = "Package"
                        )
                    }
                }
            )
        },

        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(40.dp),
            contentPadding = PaddingValues(horizontal = 14.dp)
        ) {
            item {
                var data by remember {
                    mutableStateOf(
                        listOf(
                            Pie(label = "Android", data = 20.0, color = Color.Red, selectedColor = Color.Green),
                            Pie(label = "Windows", data = 45.0, color = Color.Cyan, selectedColor = Color.Blue),
                            Pie(label = "Linux", data = 35.0, color = Color.Gray, selectedColor = Color.Yellow),
                        )
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        Text(
                            text = "Диаграмма оплат",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp)
                        )
                    }

                    PieChart(
                        modifier = Modifier
                            .weight(1f)
                            .size(200.dp),
                        data = data,
                        onPieClick = {
                            println("${it.label} Clicked")
                            val pieIndex = data.indexOf(it)
                            data = data.mapIndexed { mapIndex, pie -> pie.copy(selected = pieIndex == mapIndex) }
                        },
                        selectedScale = 1.2f,
                        scaleAnimEnterSpec = spring<Float>(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        colorAnimEnterSpec = tween(300),
                        colorAnimExitSpec = tween(300),
                        scaleAnimExitSpec = tween(300),
                        spaceDegreeAnimExitSpec = tween(300),
                        style = Pie.Style.Stroke()
                    )
                }
            }

            item {
                ColumnChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    indicatorProperties = HorizontalIndicatorProperties(
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        )
                    ),
                    labelHelperProperties = LabelHelperProperties(
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        ),
                        enabled = true
                    ),
                    labelProperties = LabelProperties(
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        ),
                        enabled = true
                    ),
                    data = remember {
                        listOf(
                            Bars(
                                label = "Jan",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Red)),
                                    Bars.Data(label = "Windows", value = 70.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 80.0, color = Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFFFD194), // светло-оранжевый
                                            Color(0xFFEA5455)  // красноватый
                                        )
                                    )),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 80.0, color = SolidColor(Color.Red)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 80.0, color = SolidColor(Color.Red)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            )
                        )
                    },
                    barProperties = BarProperties(
                        cornerRadius = Bars.Data.Radius.Rectangle(topLeft = 6.dp, topRight = 6.dp),
                        spacing = 3.dp,
                        thickness = 20.dp
                    ),
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    ),
                    animationMode = AnimationMode.Together()
                )
            }

            item {
                ColumnChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    indicatorProperties = HorizontalIndicatorProperties(
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        )
                    ),
                    labelHelperProperties = LabelHelperProperties(
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        ),
                        enabled = true
                    ),
                    labelProperties = LabelProperties(
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        ),
                        enabled = true
                    ),
                    data = remember {
                        listOf(
                            Bars(
                                label = "Jan",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Red)),
                                    Bars.Data(label = "Windows", value = 70.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 80.0, color = Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFFFD194), // светло-оранжевый
                                            Color(0xFFEA5455)  // красноватый
                                        )
                                    )),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 80.0, color = SolidColor(Color.Red)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 80.0, color = SolidColor(Color.Red)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            )
                        )
                    },
                    barProperties = BarProperties(
                        cornerRadius = Bars.Data.Radius.Rectangle(topLeft = 6.dp, topRight = 6.dp),
                        spacing = 3.dp,
                        thickness = 20.dp
                    ),
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    ),
                    animationMode = AnimationMode.Together()
                )
            }

            item {
                ColumnChart(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    indicatorProperties = HorizontalIndicatorProperties(
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        )
                    ),
                    labelProperties = LabelProperties(
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        ),
                        enabled = true
                    ),
                    labelHelperProperties = LabelHelperProperties(
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = MaterialTheme.typography.labelSmall.fontSize,
                        ),
                        enabled = true
                    ),
                    data = remember {
                        listOf(
                            Bars(
                                label = "Jan",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 50.0, color = SolidColor(Color.Red)),
                                    Bars.Data(label = "Windows", value = 70.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 80.0, color = Brush.verticalGradient(
                                        listOf(
                                            Color(0xFFFFD194), // светло-оранжевый
                                            Color(0xFFEA5455)  // красноватый
                                        )
                                    )),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 80.0, color = SolidColor(Color.Red)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            ),
                            Bars(
                                label = "Feb",
                                values = listOf(
                                    Bars.Data(label = "Linux", value = 80.0, color = SolidColor(Color.Red)),
                                    Bars.Data(label = "Windows", value = 60.0, color = SolidColor(Color.Red))
                                ),
                            )
                        )
                    },
                    barProperties = BarProperties(
                        cornerRadius = Bars.Data.Radius.Rectangle(topLeft = 6.dp, topRight = 6.dp),
                        spacing = 3.dp,
                        thickness = 20.dp
                    ),
                    animationSpec = tween(
                        durationMillis = 200,
                        easing = FastOutSlowInEasing
                    ),
                    animationMode = AnimationMode.Together()
                )
            }

            item {
                Text(
                    text = "Dashboard Screen",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.displaySmall
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDetailsClick
                ) {
                    Text(text = "Go to Details")
                }
            }
        }
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
fun DashboardScreenPreview() {
    MarketTheme {
        DashboardScreen(
            onDetailsClick = {}
        )
    }
}