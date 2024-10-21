package com.epicmillennium.cheshmap.presentation.ui.settings


import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.core.Menu
import com.composables.core.MenuButton
import com.composables.core.MenuContent
import com.composables.core.MenuItem
import com.composables.core.rememberMenuState
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.presentation.theme.AppThemeMode
import com.epicmillennium.cheshmap.presentation.theme.CheshMapTheme
import com.epicmillennium.cheshmap.presentation.theme.DarkTheme
import com.epicmillennium.cheshmap.presentation.theme.LocalTheme
import kotlinx.coroutines.Job

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsView(
    globalThemeState: AppThemeMode,
    isUserLocationTrackingEnabled: Boolean,
    setGlobalThemeMode: (AppThemeMode) -> Job,
    setUserLocationTrackingEnabled: (Boolean) -> Job,
    onNavigateBack: () -> Unit
) {
    // Handle back press
    BackHandler { onNavigateBack() }

    CompositionLocalProvider(value = LocalTheme provides DarkTheme(LocalTheme.current.isDark)) {
        CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
            Scaffold(modifier = Modifier.fillMaxSize(),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(text = stringResource(R.string.settings)) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.navigate_back)
                                )
                            }
                        },
                        windowInsets = WindowInsets(0, 0, 0, 0)
                    )
                }
            ) { paddingValues ->

                var isTrackingLocation by remember { mutableStateOf(isUserLocationTrackingEnabled) }

                val selectedTheme by remember { mutableStateOf(globalThemeState) }

                var infoDialogMessage by remember { mutableStateOf("") }
                var shouldShowInfoDialog by remember { mutableStateOf(false) }

                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(paddingValues)
                        .padding(horizontal = 16.dp)
                ) {
                    SettingsGroup(name = R.string.privacy_settings) {
                        val userTrackingInfo = stringResource(R.string.user_tracking_info)

                        SettingsSwitchComp(
                            name = R.string.track_my_location,
                            state = isTrackingLocation,
                            onInfoDialogClick = {
                                infoDialogMessage = userTrackingInfo
                                shouldShowInfoDialog = true
                            },
                            onClick = {
                                isTrackingLocation = !isTrackingLocation
                                setUserLocationTrackingEnabled.invoke(isTrackingLocation)
                            }
                        )
                    }

                    SettingsGroup(name = R.string.other_settings) {
                        SettingsThemeDropDownComp(selectedTheme) {
                            setGlobalThemeMode.invoke(it)
                        }
                    }
                }

                AnimatedVisibility(
                    visible = shouldShowInfoDialog
                ) {
                    InfoAlertDialog(
                        message = infoDialogMessage,
                        dismiss = {
                            shouldShowInfoDialog = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsGroup(
    @StringRes name: Int,
    // to accept only composables compatible with column
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(stringResource(id = name))

        Spacer(modifier = Modifier.height(8.dp))

        Surface(
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16),
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
fun SettingsSwitchComp(
    @StringRes name: Int,
    state: Boolean,
    onInfoDialogClick: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = stringResource(id = name),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Start,
                    )

                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = stringResource(id = R.string.alert_dialog_info),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(20.dp)
                            .clickable {
                                onInfoDialogClick.invoke()
                            }
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                Switch(
                    checked = state,
                    onCheckedChange = { onClick() }
                )
            }
        }
    }
}

@Composable
fun SettingsThemeDropDownComp(
    selectedTheme: AppThemeMode,
    onItemSelected: (AppThemeMode) -> Unit
) {
    val currentTheme = when (selectedTheme) {
        AppThemeMode.MODE_AUTO -> R.string.system_theme
        AppThemeMode.MODE_DAY -> R.string.light_theme
        AppThemeMode.MODE_NIGHT -> R.string.dark_theme
    }

    var currentThemeStringRes by remember { mutableIntStateOf(currentTheme) }

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .padding(horizontal = 16.dp),
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(id = R.string.theme_settings),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Start,
                )

                Spacer(modifier = Modifier.weight(1f))

                AppThemeDropDownMenu(
                    currentThemeStringRes, onItemSelected = {
                        currentThemeStringRes = it
                        onItemSelected.invoke(convertIntToAppThemeMode(it))
                    }
                )
            }
        }
    }
}

@Composable
private fun AppThemeDropDownMenu(currentThemeStringRes: Int, onItemSelected: (Int) -> Unit) {
    Menu(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .wrapContentSize(),
        state = rememberMenuState(expanded = false)
    ) {
        MenuButton(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.onBackground.copy(alpha = 0.1f))
        ) {
            Text(
                text = stringResource(currentThemeStringRes),
                style = TextStyle(
                    fontWeight = FontWeight(500),
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .width(84.dp)
                    .padding(vertical = 10.dp)
            )
        }

        MenuContent(
            modifier = Modifier
                .padding(top = 4.dp)
                .width(84.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(6.dp),
            exit = fadeOut()
        ) {
            if (currentThemeStringRes != R.string.system_theme) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    onClick = { onItemSelected.invoke(R.string.system_theme) }) {
                    BasicText(
                        text = stringResource(R.string.system_theme),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }

            if (currentThemeStringRes != R.string.light_theme) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    onClick = { onItemSelected.invoke(R.string.light_theme) }) {
                    BasicText(
                        text = stringResource(R.string.light_theme),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }

            if (currentThemeStringRes != R.string.dark_theme) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(16.dp)),
                    onClick = { onItemSelected.invoke(R.string.dark_theme) }) {
                    BasicText(
                        text = stringResource(R.string.dark_theme),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoAlertDialog(
    message: String,
    dismiss: () -> Unit
) {
    AlertDialog(
        title = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text(text = stringResource(R.string.alert_dialog_info))
            }
        },
        text = { Text(text = message) },
        confirmButton = {
            TextButton(onClick = dismiss) {
                Text(text = stringResource(R.string.ok))
            }
        },
        onDismissRequest = dismiss,
    )
}

private fun convertIntToAppThemeMode(appThemeStringRes: Int): AppThemeMode {
    return when (appThemeStringRes) {
        R.string.system_theme -> AppThemeMode.MODE_AUTO
        R.string.light_theme -> AppThemeMode.MODE_DAY
        R.string.dark_theme -> AppThemeMode.MODE_NIGHT
        else -> AppThemeMode.MODE_AUTO
    }
}
