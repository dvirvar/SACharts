# SACharts 📊
**SACharts** is a highly customizable and interactive charting library for small datasets built from the ground up for **Compose Multiplatform**.

Supports Android, iOS, Desktop, and Web.

<div style="display: flex; flex-wrap: wrap; justify-content: center; gap: 10px;">
  <img src="/assets/line_1.png" width="300" alt="Line Chart 1">
  <img src="/assets/line_2.png" width="300" alt="Line Chart 2">
  <img src="/assets/bar_1.png" width="300" alt="Bar Chart 1">
  <img src="/assets/bar_2.png" width="300" alt="Bar Chart 2">
  <img src="/assets/horiz_bar_1.png" width="300" alt="Horizontal Bar 1">
  <img src="/assets/horiz_bar_2.png" width="300" alt="Horizontal Bar 2">
  <img src="/assets/pie_1.png" width="300" alt="Pie Chart 1">
  <img src="/assets/pie_2.png" width="300" alt="Pie Chart 2">
</div>

## 🚀 Key Features
* **Line Charts:** Dual Y-axes support, real-time point dragging (direct or via long-press), panning/zooming. Customizable axes labels, points(you can draw whatever you want on a point) and click overlays.
* **Bar & Horizontal Bar Charts:** Grouped and stacked category data models, axis alignment. Customizable axes labels, bar values(you can draw whatever you want on a bar), bars with corner radius and brush fills.
* **Pie & Dynamic Pie Charts:** Support for donut holes. Customizable slice gaps, internal slice borders, value labels and external pointing labels.
* **Dynamic Sizing:** `DynamicPieChart` auto-calculates available space, resizing dynamically fit accordingly to your data and customizations.
* **Animations:** All charts support animations.

## 📦 Installation
Add the dependency to your shared module's `commonMain` source set:

```kotlin
// build.gradle.kts
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation("com.skellyapps.charts:sacharts:1.0")
        }
    }
}
```

## 🏃‍♂️ Sample App
Download the repository and then:
- Android app: `./gradlew :androidApp:assembleDebug`
- Desktop app: `./gradlew :desktopApp:run`
- Web app:
    - Wasm target (faster, modern browsers): `./gradlew :webApp:wasmJsBrowserDevelopmentRun`
    - JS target (slower, supports older browsers): `./gradlew :webApp:jsBrowserDevelopmentRun`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

## Usage
### 📈 1. Line Chart
A simple line chart with left y-axis and bottom x-axis.

Left axis contains:
- A blue line with 13 points which ordered by x-axis and has a tag of 0
- Showing value labels every 20 values
- Grid lines colored gray and have a thickness of 1.dp
- A black divider
- Value label view which is a row with text(the value) and a horizontal divider

Bottom axis contains:
- Showing fixed amount(8) of value labels
- Grid lines colored gray and have a thickness of 1.dp
- A black divider
- Value label view which is a column with text(the value) and a vertical divider

```kotlin
private val blueLine = LineChartData.Line(
    points = (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableList(),
    pointsOrder = LineChartData.Line.PointsOrder.Ordered.X,
    tag = 0,
    customization = LineChartData.Line.Customization(Color.Blue, join = StrokeJoin.Round)
)
private val leftAxis = LineChartData.YAxis(
    lines = mutableListOf(blueLine),
    value = GridChartData.Axis.Value.Step(20.0),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)
) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(value.roundToDecimals(1).toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}
private val bottomAxis = LineChartData.XAxis(
    value = GridChartData.Axis.Value.Fixed(8),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)
) { value ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(value.roundToDecimals(1).toString())
    }
}

@Composable
fun SimpleLineChart() {
    val chartData = retain {
        LineChartData(
            leftAxis = leftAxis,
            bottomAxis = bottomAxis,
        )
    }
    LineChart(
        Modifier.fillMaxWidth().height(300.dp),
        chartData,
    )
}
```
Add zoom:
```kotlin
LineChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    data = chartData,
    zoom = Zoom(scrollJump = 0.3f, max = 5f) // <-- Add this parameter
)
```
Add animations:

You can play both animations or just 1, it's your own choice when to animate and how. 
```kotlin
val animations = retain { LineChartAnimations(
        growth = LineChartAnimations.Growth(spec = tween(2000)),
        reveal = LineChartAnimations.Reveal(spec = tween(2000))
    ) }
val scope = rememberCoroutineScope()
LaunchedEffect(scope) { 
    scope.launch {
        animations.growth!!.animate()
    }
    scope.launch { 
        animations.reveal!!.animate()
    }
}
LineChart(
  modifier = Modifier.fillMaxWidth().height(300.dp),
  data = chartData,
  animations = animations // <-- Add this parameter
)

```
Add a view(popup) on point click:
```kotlin
private val pointClick = LineChartData.PointClick(
    isPointInRange = { point, press ->
        //You can choose at what distance a click on a chart considered as a click on a point
        //Here we choose a distance of 15 dp
        (press - point).getDistance() / this.density <= 15.0
    },
    viewPosition = Position.Bottom,//How the view will be anchored to the point
    viewOffset = DpOffset(0.dp, 5.dp),//How far from the point the view will be anchored
    viewStayInChartBounds = true,//Keep view in chart bounds
    //The view itself
    view = { lineTag, index ->
        val point = leftAxis.lines[lineTag].points[index]
        //Because we have only 1 line we could also do: val point = blueLine.points[index]
        Column(Modifier.width(50.dp).background(Color.Blue.copy(.5f), AbsoluteRoundedCornerShape(25)), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(point.x.roundToDecimals(1).toString(), style = MaterialTheme.typography.bodySmall)
            HorizontalDivider(thickness = 4.dp)
            Text(point.y.roundToDecimals(1).toString(), style = MaterialTheme.typography.bodySmall)
        }
    }
)
LineChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    data = chartData,
    pointClick = pointClick // <-- Add this parameter
)
```
Add drag functionality:
```kotlin
//We need to change points to be mutable state list
private val blueLine = LineChartData.Line(
    points = (0..12).map { ChartValue(it * 10.0, Random.nextDouble(0.0, 100.0)) }.toMutableStateList(),// <-- Change to mutable state list
    ...
    
private val pointDrag = LineChartData.PointDrag(
    isPointInRange = { point, press ->
        //You can choose at what distance a press on a chart considered as a press on a point
        //Here we choose a distance of 15 dp
        (press - point).getDistance() / this.density <= 15.0
    },
    pointDragged = { lineTag, index, newPosition ->
        //We can have unrestricted changes
        //But pointsOrder must be LineChartData.Line.PointsOrder.Unordered,
        leftAxis.lines[lineTag].points[index] = newPosition
        //Or we can do some checks
        //For example keep a point between the before and after points
        val line = leftAxis.lines[lineTag]
        if (index != 0) {
            val previousPoint = line.points[index-1]
            if (newPosition.x.value <= previousPoint.x.value) {
                return@PointDrag
            }
        }
        if (index != line.points.size - 1) {
            val nextPoint = line.points[index+1]
            if (newPosition.x.value >= nextPoint.x.value) {
                return@PointDrag
            }
        }
        leftAxis.lines[lineTag].points[index] = newPosition
    }
)
LineChart(
  modifier = Modifier.fillMaxWidth().height(300.dp),
  data = chartData,
  pointDrag = pointDrag // <-- Add this parameter
  //OR
  pointDragAfterLongPress = pointDrag // <-- Add this parameter for drag after long press
)
```
Draw on chart, for example limit lines:
```kotlin
val textMeasurer = rememberTextMeasurer()
val highTextLayout = textMeasurer.measure("High")
val mediumTextLayout = textMeasurer.measure("Medium")
val lowTextLayout = textMeasurer.measure("Low")
LineChart(
  modifier = Modifier.fillMaxWidth().height(300.dp),
  data = chartData,
  zoom = Zoom(scrollJump = 0.3f, max = 5f),
  drawOnChart = { drawHelper -> // <-- Add this parameter
    //Draw limit lines
    //High y value of 60  
    val highOffsetStart = with(drawHelper) { ChartValue(0.0, 60.0).toOffset(isLeftAxis = true) }
    if (highOffsetStart.y >= 0f && highOffsetStart.y <= size.height) {
      val highOffsetEnd = Offset(size.width, highOffsetStart.y)
      drawHelper.drawText(
        textLayoutResult = highTextLayout, 
        offset = highOffsetEnd, 
        position = Position.TopLeft, 
        stayInCanvasBounds = true
      )
      drawLine(Color.Red, highOffsetStart, highOffsetEnd)
    }
    //Low y value of 20
    val lowOffsetStart = with(drawHelper) { ChartValue(0.0, 20.0).toOffset(isLeftAxis = true) }
    if (lowOffsetStart.y >= 0f && lowOffsetStart.y <= size.height) {
      val lowOffsetEnd = Offset(size.width, lowOffsetStart.y)
      drawHelper.drawText(
        textLayoutResult = lowTextLayout, 
        offset = lowOffsetEnd, 
        position = Position.BottomLeft,
        stayInCanvasBounds = true
      )
      drawLine(Color.Blue, lowOffsetStart, lowOffsetEnd)
    }
    //Draw a ranged limit(from y value of 30 to 50)
    var mediumYStart = with(drawHelper) { ChartValueCoordinate(50.0).toYPixel(isLeftAxis = true) }
    var mediumYEnd = with(drawHelper) { ChartValueCoordinate(30.0).toYPixel(isLeftAxis = true) }
    if (mediumYStart <= size.height && mediumYEnd >= 0f) {
      mediumYStart = max(0f, mediumYStart)
      mediumYEnd = min(size.height, mediumYEnd)
      drawRect(Color.Yellow.copy(0.7f), Offset(0f, mediumYStart), Size(size.width, mediumYEnd - mediumYStart))
      drawHelper.drawText(
        textLayoutResult = mediumTextLayout,
        offset = Offset(size.width, (mediumYEnd + mediumYStart) / 2f), 
        position = Position.Left,
        stayInCanvasBounds = true
      )
    }
  }
)
```
Add shapes or labels to points:

(Measuring text every draw is bad practice, you should cache your labels outside the draw function)
```kotlin
val textMeasurer = rememberTextMeasurer()
LineChart(
  modifier = Modifier.fillMaxWidth().height(300.dp),
  data = chartData,
  zoom = Zoom(scrollJump = 0.3f, max = 5f),
  drawOnEachPoint = { drawHelper, lineTag, index, offset, animatedYPixel -> // <-- Add this parameter
    val radius = 5.dp.toPx()
    //Don't draw if the point is out of bounds by radius distance
    //Needed only if zoom is set
    //The chart do clip to bounds if chart is zoomed in
    //But as you will see below, we coerce the labels to always be inside charts' bounds
    //So we need this check to not show them when they are out of bounds
    if (offset.x < -radius || offset.x > size.width + radius ||
      offset.y < -radius || offset.y > size.height + radius) {
      return@LineChart
    }
    //If index is even draw a circle, otherwise draw a square
    if (index % 2 == 0) {
      drawCircle(
        Color.Blue,
        radius,
        offset
      )
    } else {
      drawHelper.drawSquare(
        Color.Blue,
        offset,
        radius * 2f
      )
    }
    //Draw a label anchored to the top of the point
    //And coerce the label to stay in chart bounds
    val point = leftAxis.lines[lineTag].points[index]
    val xValue = point.x.roundToDecimals(1)
    val yValue = point.y.roundToDecimals(1)
    val text = "X:$xValue\nY:$yValue"
    //Bad practice, cache the text layout outside the draw function
    val textLayout = textMeasurer.measure(text)
    drawHelper.drawText(
      textLayoutResult = textLayout,
      offset = offset,
      position = Position.Top,
      stayInCanvasBounds = true
    )
  }
)
```
### 📊 2. Bar Chart & Horizontal Bar Chart
A simple vertical bar chart with left y-axis and bottom x-axis.

(Implementation of horizontal bar chart is similar to bar chart so it's not shown here)

Y-axis contains:
- A blue category with 13 values, a tag of 0 and top corner radius
- Grouped type with 0 space between bard and 5 dp space between categories
- Showing minimum value of -30 and maximum value of 30
- Showing fixed amount(15) of value labels
- Grid lines colored gray and have a thickness of 1.dp
- A black divider
- Value label view which is a row with text(the value) and a horizontal divider

Bottom axis contains:
- Grid lines colored gray and have a thickness of 1.dp
- A black divider
- Value label view which is a column with text(the index) and a vertical divider

```kotlin
private val blueCategory = BarChartData.Category(
    values = (0..12).map { ChartValueCoordinate(Random.nextDouble(-30.0, 30.0)) }.toMutableList(),
    tag = 0,
    customization = BarChartData.Category.Customization(Color.Blue, topLeftCornerRadius = CornerRadius(5f), topRightCornerRadius = CornerRadius(5f)),
)
private val yAxis = BarChartData.YAxis(
    categories = mutableListOf(blueCategory),
    type = BarChartData.Type.Grouped(barsSpace = 0.dp, categoriesSpace = 5.dp),
    minValue = -30.0,
    maxValue = 30.0,
    value = GridChartData.Axis.Value.Fixed(15),
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)
) { value ->
    Row(verticalAlignment = Alignment.CenterVertically) { 
        Text(value.roundToDecimals(1).toString())
        HorizontalDivider(Modifier.width(8.dp))
    }
}
private val bottomAxis = BarChartData.XAxis(
    gridLines = GridChartData.Axis.GridLines(customization = GridChartData.Axis.DividerCustomization(Color.Gray, 1.dp)),
    dividerCustomization = GridChartData.Axis.DividerCustomization(Color.Black)
) { index ->
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        VerticalDivider(Modifier.height(8.dp))
        Text(index.toString())
    }
}

@Composable
fun SimpleBarChartExample() {
    val chartData = retain { 
        BarChartData(
          yAXis = yAxis, 
          isLeftYAxis = true, 
          bottomAxis = bottomAxis,
      )
    }
    BarChart(
        modifier = Modifier.fillMaxWidth().height(300.dp),
        data = chartData,
    )
}
```
To show it as stacked, change the type to Stacked:

(Stacked doesn't support negative values)
```kotlin
private val blueCategory = BarChartData.Category(
    values = (0..12).map { ChartValueCoordinate(Random.nextDouble(0.0, 30.0)) }.toMutableList(),
    ...
private val yAxis = BarChartData.YAxis(
    categories = mutableListOf(blueCategory),
    type = BarChartData.Type.Stacked(categoriesSpace = 5.dp), // <-- Change to Stacked
    minValue = 0.0
    ...
```
Add animation:

It's your own choice when to animate and how.
```kotlin
val animations = retain { BarChartAnimations(
        growth = BarChartAnimations.Growth(tween(2500), 1f)
    ) }
val scope = rememberCoroutineScope()
LaunchedEffect(scope) {
  scope.launch {
    animations.growth!!.animate()
  }
}
BarChart(
  modifier = Modifier.fillMaxWidth().height(300.dp),
  data = chartData,
  animations = animations // <-- Add this parameter
)
```
Add a view(popup) on bar hover:
```kotlin
private val barHover = BarChartData.BarHover(
    viewPosition = Position.Top,//How the view will be anchored to the bar
    viewPositionMirrored = true,//On negative values the position will be mirrored
    viewOffset = DpOffset.Zero,//How far from the position the view will be anchored
    viewStayInChartBounds = true//Keep view in chart bounds
) { categoryTag, index ->
    val value = yAxis.categories.[categoryTag].values[index]
    Box(Modifier.background(colors[categoryTag].copy(0.9f), CircleShape).padding(8.dp), Alignment.Center) {
        Text(value.roundToDecimals(1).toString(), color = Color.White)
    }
}
BarChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    data = chartData,
    barHover = barHover // <-- Add this parameter
)
```
Add labels to bars:

(Measuring text every draw is bad practice, you should cache your labels outside the draw function)
```kotlin
val textMeasurer = rememberTextMeasurer()
BarChart(
    modifier = Modifier.fillMaxWidth().height(300.dp),
    data = chartData,
    drawOnEachValue = { categoryTag, index, barRect, isNegative ->
      val value = yAxis.categories[categoryTag].values[index].value
      val text = value.roundToDecimals(1).toString()
      //Bad practice, cache the text layout outside the draw function
      val textLayout = textMeasurer.measure(text)
      //Draw label outside the bar
      BarChartDrawHelper.drawTextOutside(
        textLayoutResult = textLayout,
        barRect = barRect,
        stayInCanvasBounds = true,
        isNegative = isNegative,
        color = Color.Black
      )
      //Or you can draw it inside the bar
      BarChartDrawHelper.drawTextInside(
        textLayoutResult = layout,
        barRect = barRect,
        position = Position.Bottom,
        isNegative = isNegative,
        color = Color.White,
      )
    }
)
```
### 🍕 3. Pie Chart & Dynamic Pie Chart
A simple pie chart.

```kotlin
private val colors = listOf(Color.Blue, Color.Red, Color.Black, Color.Magenta, Color.Yellow, Color.Green, Color.Cyan)
private val slices = (0..6).map { PieChartData.Slice(value = Random.nextDouble(10.0, 30.0), color = colors[it], tag = it) }.toMutableList()

@Composable
fun SimpleBarChartExample() {
    val chartData = retain { 
        PieChartData(
          slices = slices,
        )
    }
    PieChart(
      modifier = Modifier.size(300.dp),
      data = chartData,
    )
}
```
Change start angle:
```kotlin
PieChartData(
  slices = slices,
  startAngle = 90f// <-- Add this parameter
)
```
Make a hole:
```kotlin
PieChartData(
  slices = slices,
  innerRadiusPercentage = 0.5f// <-- Add this parameter
)
```
Make space between slices:
```kotlin
PieChartData(
  slices = slices,
  sliceSpacingDegrees = 5f// <-- Add this parameter
)
```
Add inner border to slices:
```kotlin
PieChartData(
  slices = slices,
  sliceBorder = Slice.Border(2.dp, Color.Black)// <-- Add this parameter
)
```
Add animations:

You can play both animations or just 1, it's your own choice when to animate and how.

(DynamicPieChart is using DynamicPieChartAnimations)
```kotlin
val animations = retain { PieChartAnimations(
  scale = PieChartAnimations.Scale(tween(2000), 1f),
  growth = PieChartAnimations.Growth(tween(2000), 1f)
) }
val scope = rememberCoroutineScope()
LaunchedEffect(scope) {
  scope.launch {
    animations.scale!!.animate()
  }
  scope.launch {
    animations.growth!!.animate()
  }
}
BarChart(
  modifier = Modifier.fillMaxWidth().height(300.dp),
  data = chartData,
  animations = animations // <-- Add this parameter
)
```
Add labels to slices:
```kotlin
private val labels = listOf("Blue", "Red", "Black", "Magenta", "Yellow", "Green", "Cyan")
private val colors = listOf(Color.Blue, Color.Red, Color.Black, Color.Magenta, Color.Yellow, Color.Green, Color.Cyan)
private val slices = (0..6).map { 
    PieChartData.Slice(value = Random.nextDouble(10.0, 30.0), color = colors[it], tag = it, label = labels[it])//Add label to slice
}.toMutableList()
...
val textMeasurer = rememberTextMeasurer()
PieChartData(
  slices = slices,
  outerRadiusPercentage = 0.7f,//You should also make available space for the labels
  labelCustomization = PieChartData.LabelCustomization( // <-- Add this parameter
    textMeasurer = textMeasurer,
    textColor = Color.Black,
    edgePadding = 2.dp,
    linePadding = 2.dp,
    lineCustomization = PieChartData.LineCustomization(
      shoulderLength = 6.dp,
      extensionMaxLength = 16.dp,
      color = Color.DarkGray,
      thickness = 2.dp,
      join = StrokeJoin.Round
    )
  )
)
```
A dynamic pie chart.
```kotlin
...
val chartData = retain {
  DynamicPieChartData(
    slices = slices,
  )
}
DynamicPieChart(
  modifier = Modifier.size(300.dp),   
  data = chartData
)
```
DynamicPieChartData has same parameters as PieChartData, the only difference is that dynamic has outerRadiusMinPercentage instead of outerRadiusPercentage.

Add value labels to slices:

(Measuring text every draw is bad practice, you should cache your labels outside the draw function)
```kotlin
val textMeasurer = rememberTextMeasurer()
PieChart(
    modifier = Modifier.size(300.dp),
    data = chartData,
    drawOnEachSlice = { sliceTag: Int, centerX: Float, centerY: Float, outerRadius: Float, innerRadius: Float, middleRad: Double ->
      //Bad practice, cache the text layout outside the draw function
      val textLayout = textMeasurer.measure(
        slices[sliceTag].value.roundToDecimals(1).toString()
      )
      //Draw label in the middle of the slice
      PieChartDrawHelper.drawTextInMiddle(
        textLayoutResult = textLayout,
        centerX = centerX,
        centerY = centerY,
        outerRadius = outerRadius,
        innerRadius = innerRadius,
        middleRad = middleRad,
        hasMoreThanOneSlice = slices.size > 1,
        color = Color.Black
      )
    }
)
```
## License
Localization is licensed under the [MIT License](LICENSE).