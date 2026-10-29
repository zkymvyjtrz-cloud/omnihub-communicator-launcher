package com.clicks.omnihub.ui.components

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import android.provider.AlarmClock
import android.provider.Settings
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Apps
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.clicks.omnihub.model.HubViewMode
import com.clicks.omnihub.model.InstalledAppInfo
import com.clicks.omnihub.ui.theme.AirframeSpace
import com.clicks.omnihub.ui.theme.AirframeTheme
import com.clicks.omnihub.ui.theme.AirframeType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Airframe `TabBar` (`TabBar/preview.html`).
 * - 4 primary destinations (`Inbox`, `People`, `Widgets`, `Apps`).
 * - Height `80dp` (`size-tabbar`) on `surface-raised` with a 1px top hairline in `line`.
 * - Active tab uses `forest` color, filled icon, `600` weight caption, and a `5dp` `forest` dot (`af-tab-dot`) underneath.
 * - Inactive tabs use `text-muted`, outline icon, and a transparent `5dp` dot underneath.
 */
@Composable
fun AirframeTabBar(
    currentMode: HubViewMode,
    onSelectMode: (HubViewMode) -> Unit
) {
    val c = AirframeTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(c.surfaceRaised)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(c.line)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(AirframeSpace.sizeTabbar)
                .padding(top = 10.dp, bottom = 14.dp, start = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HubViewMode.entries.forEach { mode ->
                val selected = mode == currentMode
                val icon = when (mode) {
                    HubViewMode.STREAM -> if (selected) Icons.Filled.Inbox else Icons.Outlined.Inbox
                    HubViewMode.PEOPLE -> if (selected) Icons.Filled.Group else Icons.Outlined.Group
                    HubViewMode.WIDGETS -> if (selected) Icons.Filled.Widgets else Icons.Outlined.Widgets
                    HubViewMode.APPS -> if (selected) Icons.Filled.Apps else Icons.Outlined.Apps
                }
                val itemColor = if (selected) c.forest else c.textMuted

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = AirframeSpace.sizeTouch)
                        .clip(RoundedCornerShape(AirframeSpace.radiusMd))
                        .clickable { onSelectMode(mode) }
                        .padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = mode.label,
                        tint = itemColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = mode.label,
                        style = AirframeType.caption.copy(
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = itemColor,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (selected) c.forest else Color.Transparent)
                    )
                }
            }
        }
    }
}

/**
 * Airframe Widgets Panel (`Widgets` tab).
 * Strictly follows Airframe editorial rules:
 * - Single-column scroll (`space-gutter` 26dp horizontal padding) directly on `ground`, separated by 1px `line` hairlines.
 * - Leads with a single `numeral-xl` hero figure (`72 °F`) + `sage-soft` pill + light `lede` agenda line.
 * - Includes an Airframe `CapsuleStrip` week rhythm bar.
 * - Includes an Airframe `Feature` + `StatPair` section with the screen's single `forest` primary button ("Add system widget →")
 *   plus native Android `AppWidgetHostView` support and a `sage-wash` pinned scratchpad (`Callout`).
 */
@Composable
fun DedicatedWidgetsDashboardPage(
    appWidgetHost: AppWidgetHost,
    appWidgetManager: AppWidgetManager,
    hostedWidgetIds: List<Int>,
    quickNoteText: String,
    onUpdateQuickNote: (String) -> Unit,
    onAddSystemWidgetClick: () -> Unit,
    onRemoveSystemWidget: (Int) -> Unit,
    onOpenSystemAction: (String) -> Unit
) {
    val c = AirframeTheme.colors
    val context = LocalContext.current
    val batteryPct = remember { readBatteryPercentage(context) }
    val timeTitle = remember { SimpleDateFormat("h:mm a", Locale.US).format(Date()) }
    val weekDays = remember { buildCurrentWeekStrip() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(c.ground),
        contentPadding = PaddingValues(horizontal = AirframeSpace.gutter, vertical = 14.dp)
    ) {
        // 1. Airframe Hero `Metric` Section (One serif figure per screen: 72 °F)
        item(key = "widgets_airframe_hero_metric") {
            Column(modifier = Modifier.fillMaxWidth()) {
                AirframeEyebrow(
                    label = "Today's glance",
                    aside = "$timeTitle · Alarms →",
                    onAsideClick = { onOpenSystemAction(AlarmClock.ACTION_SHOW_ALARMS) }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "72",
                            style = AirframeType.numeralXl,
                            color = c.ink
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "°F",
                            style = AirframeType.numeralUnit,
                            color = c.text,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp)
                            .height(36.dp)
                            .clip(CircleShape)
                            .background(c.sageSoft)
                            .clickable { onOpenSystemAction(Intent.ACTION_MAIN) }
                            .padding(horizontal = AirframeSpace.space4),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Clear · H 76° L 58°",
                            style = AirframeType.body.copy(fontWeight = FontWeight.SemiBold),
                            color = c.forest
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AirframeSpace.space4))

                Text(
                    text = "Next at 10:30 AM — Q4 Board Deck Sign-Off & Forecast Sync.",
                    style = AirframeType.lede,
                    color = c.text
                )
            }
        }

        // 2. Signature Airframe `CapsuleStrip` ("This Week")
        item(key = "widgets_week_capsule_strip") {
            AirframeRule()
            Column(modifier = Modifier.fillMaxWidth()) {
                AirframeEyebrow(label = "This week")
                Spacer(modifier = Modifier.height(AirframeSpace.space4))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    weekDays.forEach { day ->
                        val barColor = when {
                            day.isToday -> c.forest
                            day.isPast -> c.capsuleDone
                            else -> c.track
                        }
                        val barHeight = if (day.isToday) AirframeSpace.sizeCapsuleCurrentHeight else AirframeSpace.sizeCapsuleHeight
                        val labelColor = when {
                            day.isToday -> c.ink
                            day.isPast -> c.text
                            else -> c.textMuted
                        }
                        val weight = if (day.isToday) FontWeight.Bold else FontWeight.Normal

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.widthIn(min = 38.dp)
                        ) {
                            Box(
                                modifier = Modifier.height(AirframeSpace.sizeCapsuleCurrentHeight),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(AirframeSpace.sizeCapsuleWidth)
                                        .height(barHeight)
                                        .clip(CircleShape)
                                        .background(barColor)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = day.shortDayName,
                                style = AirframeType.caption.copy(fontWeight = weight),
                                color = labelColor
                            )
                            Text(
                                text = day.dayNumber,
                                style = AirframeType.caption.copy(fontWeight = weight),
                                color = labelColor
                            )
                        }
                    }
                }
            }
        }

        // 3. Airframe `Feature` + `StatPair` Section (Single `forest` primary action: "Add system widget →")
        item(key = "widgets_feature_and_stats") {
            AirframeRule()
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "DEVICE & SYSTEM WIDGETS",
                    style = AirframeType.eyebrow,
                    color = c.textMuted
                )
                Spacer(modifier = Modifier.height(AirframeSpace.space3))
                Text(
                    text = "Native Android Widgets & Quick Controls",
                    style = AirframeType.titleLg,
                    color = c.ink
                )
                Spacer(modifier = Modifier.height(AirframeSpace.space2))
                Text(
                    text = "Pin interactive widgets from Weather, Calendar, Spotify, or Samsung Clock directly onto this page.",
                    style = AirframeType.lede,
                    color = c.text
                )

                Spacer(modifier = Modifier.height(AirframeSpace.space5))

                // Screen's single `forest` primary button (`af-btn af-btn--block`)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AirframeSpace.sizeButton)
                        .clip(RoundedCornerShape(AirframeSpace.radiusLg))
                        .background(c.forest)
                        .clickable { onAddSystemWidgetClick() }
                        .padding(horizontal = AirframeSpace.gutter),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Add system widget",
                        style = AirframeType.button,
                        color = c.onForest
                    )
                    Spacer(modifier = Modifier.width(AirframeSpace.space3))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = c.onForest,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                // `af-stats` StatPair (Battery & Connectivity)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenSystemAction(Intent.ACTION_POWER_USAGE_SUMMARY) }
                            .padding(end = AirframeSpace.space3),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery",
                            tint = c.text,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Battery",
                                style = AirframeType.bodyLight,
                                color = c.textMuted
                            )
                            Text(
                                text = "$batteryPct%",
                                style = AirframeType.statValue,
                                color = c.ink
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(42.dp)
                            .background(c.line)
                    )

                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onOpenSystemAction(Settings.ACTION_WIFI_SETTINGS) }
                            .padding(start = AirframeSpace.space5),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = "Wi-Fi",
                            tint = c.text,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Network",
                                style = AirframeType.bodyLight,
                                color = c.textMuted
                            )
                            Text(
                                text = "Online",
                                style = AirframeType.statValue,
                                color = c.ink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AirframeSpace.space4))

                // System Quick Settings Chips (`af-chip`)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AirframeChip(
                        label = "Wi-Fi",
                        onClick = { onOpenSystemAction(Settings.ACTION_WIFI_SETTINGS) }
                    )
                    AirframeChip(
                        label = "Bluetooth",
                        onClick = { onOpenSystemAction(Settings.ACTION_BLUETOOTH_SETTINGS) }
                    )
                    AirframeChip(
                        label = "Alarms",
                        onClick = { onOpenSystemAction(AlarmClock.ACTION_SHOW_ALARMS) }
                    )
                    AirframeChip(
                        label = "System Settings",
                        onClick = { onOpenSystemAction(Settings.ACTION_SETTINGS) }
                    )
                }
            }
        }

        // 4. User-Added Native Android System Widgets (`AppWidgetHostView`)
        items(
            items = hostedWidgetIds,
            key = { "sys_widget_$it" }
        ) { appWidgetId ->
            val providerInfo = remember(appWidgetId) {
                try {
                    appWidgetManager.getAppWidgetInfo(appWidgetId)
                } catch (_: Exception) {
                    null
                }
            }

            if (providerInfo != null) {
                AirframeRule()
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = (providerInfo.loadLabel(context.packageManager) ?: "ANDROID WIDGET").uppercase(Locale.US),
                            style = AirframeType.eyebrow,
                            color = c.textMuted
                        )
                        AirframeIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Remove widget",
                            style = AirframeIconButtonStyle.OUTLINE,
                            onClick = { onRemoveSystemWidget(appWidgetId) }
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AirframeSpace.radiusXl))
                            .background(c.surface)
                            .padding(12.dp)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                appWidgetHost.createView(ctx, appWidgetId, providerInfo).apply {
                                    setAppWidget(appWidgetId, providerInfo)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(providerInfo.minHeight.coerceIn(130, 320).dp)
                        )
                    }
                }
            }
        }

        // 5. Airframe `Callout` Pinned Scratchpad (`sage-wash`, uses native device keyboard)
        item(key = "widgets_scratchpad_callout") {
            AirframeRule()
            Column(modifier = Modifier.fillMaxWidth()) {
                AirframeEyebrow(
                    label = "Pinned scratchpad",
                    aside = "Device keyboard"
                )
                Spacer(modifier = Modifier.height(AirframeSpace.space3))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(AirframeSpace.radiusXl))
                        .background(c.sageWash)
                        .padding(horizontal = AirframeSpace.space4, vertical = 14.dp)
                ) {
                    Text(
                        text = "QUICK NOTES & REMINDERS",
                        style = AirframeType.meta.copy(color = c.forest, fontWeight = FontWeight.Bold),
                        color = c.forest
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    BasicTextField(
                        value = quickNoteText,
                        onValueChange = onUpdateQuickNote,
                        textStyle = AirframeType.body.copy(color = c.ink),
                        cursorBrush = SolidColor(c.forest),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 76.dp)
                    )
                }
                Spacer(modifier = Modifier.height(AirframeSpace.space6))
            }
        }
    }
}

/**
 * Airframe Smartphone Application Directory (`Apps` tab).
 * - Preserves the full smartphone experience with real app icons (`loadIcon(pm)`),
 *   pinned essential favorites, and a 4-column responsive app grid.
 * - Styled strictly with Airframe `ground`, 1px `line` hairlines, `AirframeEyebrow`, and `AirframeChip`.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SmartphoneAppDrawerGridPage(
    installedApps: List<InstalledAppInfo>,
    selectedCategory: String,
    searchQuery: String,
    onSelectCategory: (String) -> Unit,
    onLaunchApp: (InstalledAppInfo) -> Unit,
    onLongPressApp: (InstalledAppInfo) -> Unit,
    onOpenDefaultLauncherSettings: () -> Unit
) {
    val c = AirframeTheme.colors
    val categories = listOf("All", "Communication", "Productivity", "Media", "Tools")

    val filteredApps = remember(installedApps, selectedCategory, searchQuery) {
        installedApps.filter { app ->
            val matchesCat = selectedCategory == "All" || app.categoryTag.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() || searchQuery.startsWith("/") ||
                app.label.contains(searchQuery, ignoreCase = true)
            matchesCat && matchesQuery
        }
    }

    val favoriteApps = remember(installedApps) {
        installedApps.filter { it.isFavorite }.take(8)
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier
            .fillMaxSize()
            .background(c.ground),
        contentPadding = PaddingValues(horizontal = AirframeSpace.gutter, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Airframe Eyebrow + Category Chips
        item(
            key = "app_categories_header",
            span = { GridItemSpan(maxLineSpan) }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AirframeEyebrow(
                    label = "Applications",
                    aside = "Set default home →",
                    onAsideClick = onOpenDefaultLauncherSettings
                )
                Spacer(modifier = Modifier.height(AirframeSpace.space3))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        AirframeChip(
                            label = cat,
                            selected = cat == selectedCategory,
                            onClick = { onSelectCategory(cat) }
                        )
                    }
                }
            }
        }

        // 2. Essential Favorites Section (separated by Airframe hairline, never inside a heavy card)
        if (selectedCategory == "All" && searchQuery.isBlank() && favoriteApps.isNotEmpty()) {
            item(
                key = "favorites_hairline_section",
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    AirframeRule()
                    AirframeEyebrow(
                        label = "Essential favorites",
                        aside = "${favoriteApps.size} pinned"
                    )
                    Spacer(modifier = Modifier.height(AirframeSpace.space4))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        favoriteApps.forEach { favApp ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .width(64.dp)
                                    .clip(RoundedCornerShape(AirframeSpace.radiusMd))
                                    .clickable { onLaunchApp(favApp) }
                                    .padding(vertical = 4.dp)
                            ) {
                                AppIconAvatar(app = favApp, sizeDp = 48)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = favApp.label,
                                    style = AirframeType.caption,
                                    color = c.ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. All Applications Hairline + Eyebrow Header
        item(
            key = "all_apps_eyebrow_header",
            span = { GridItemSpan(maxLineSpan) }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                AirframeRule()
                AirframeEyebrow(
                    label = "All applications (${filteredApps.size})",
                    aside = "Hold icon for info"
                )
            }
        }

        // 4. 4-Column Real Icon App Grid
        items(
            items = filteredApps,
            key = { "grid_app_${it.packageName}" }
        ) { app ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(AirframeSpace.radiusMd))
                    .combinedClickable(
                        onClick = { onLaunchApp(app) },
                        onLongClick = { onLongPressApp(app) }
                    )
                    .padding(vertical = 6.dp, horizontal = 2.dp)
            ) {
                AppIconAvatar(app = app, sizeDp = 52)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = app.label,
                    style = AirframeType.caption,
                    color = c.ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun AppIconAvatar(
    app: InstalledAppInfo,
    sizeDp: Int
) {
    val c = AirframeTheme.colors
    if (app.iconBitmap != null) {
        Image(
            bitmap = app.iconBitmap,
            contentDescription = app.label,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape((sizeDp / 4).dp))
        )
    } else {
        Box(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape((sizeDp / 3).dp))
                .background(c.sage)
                .border(1.dp, c.line, RoundedCornerShape((sizeDp / 3).dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.label.take(2).uppercase(Locale.US),
                style = AirframeType.body.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                color = c.forest
            )
        }
    }
}

private data class WeekDayCapsuleItem(
    val shortDayName: String,
    val dayNumber: String,
    val isToday: Boolean,
    val isPast: Boolean
)

private fun buildCurrentWeekStrip(): List<WeekDayCapsuleItem> {
    val cal = Calendar.getInstance()
    val todayDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
    cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val dayFormat = SimpleDateFormat("EEE", Locale.US)
    val numFormat = SimpleDateFormat("d", Locale.US)

    return (0 until 7).map { offset ->
        val dayCal = (cal.clone() as Calendar).apply {
            add(Calendar.DAY_OF_MONTH, offset)
        }
        val dYear = dayCal.get(Calendar.DAY_OF_YEAR)
        WeekDayCapsuleItem(
            shortDayName = dayFormat.format(dayCal.time),
            dayNumber = numFormat.format(dayCal.time),
            isToday = dYear == todayDayOfYear,
            isPast = dYear < todayDayOfYear
        )
    }
}

private fun readBatteryPercentage(context: Context): Int {
    return try {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)?.coerceIn(1, 100) ?: 92
    } catch (_: Exception) {
        92
    }
}
