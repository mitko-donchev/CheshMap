package com.epicmillennium.cheshmap.presentation.ui.add


import android.Manifest
import android.content.Context
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ShareCompat
import androidx.wear.compose.material.LocalContentAlpha
import coil.compose.rememberAsyncImagePainter
import com.composables.core.Menu
import com.composables.core.MenuButton
import com.composables.core.MenuContent
import com.composables.core.MenuItem
import com.composables.core.rememberMenuState
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.core.ui.theme.CheshMapTheme
import com.epicmillennium.cheshmap.core.ui.theme.DarkTheme
import com.epicmillennium.cheshmap.core.ui.theme.LocalTheme
import com.epicmillennium.cheshmap.domain.marker.WaterSourceStatus
import com.epicmillennium.cheshmap.domain.marker.WaterSourceType
import com.epicmillennium.cheshmap.presentation.ui.camera.CameraView
import com.epicmillennium.cheshmap.presentation.ui.components.onDebounceClick
import com.epicmillennium.cheshmap.utils.Constants.CONTACT_EMAIL
import com.epicmillennium.cheshmap.utils.copyAttachmentToCache
import com.epicmillennium.cheshmap.utils.deleteImageFromAppFolder
import com.epicmillennium.cheshmap.utils.trimTo13Chars
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun AddNewSourceView(
    pickedLatLng: LatLng?,
    onNavigateBack: () -> Unit,
) {
    val maxCharsForName = 50
    val maxCharsForDetails = 250

    val spacingBetweenFields = 4.dp

    val pickedLat = pickedLatLng?.latitude ?: ""
    val pickedLong = pickedLatLng?.longitude ?: ""

    var name by remember { mutableStateOf(TextFieldValue("")) }
    var details by remember { mutableStateOf(TextFieldValue("")) }
    var latitude by remember {
        mutableStateOf(
            TextFieldValue(
                pickedLat.toString().trimTo13Chars()
            )
        )
    }
    var longitude by remember {
        mutableStateOf(
            TextFieldValue(
                pickedLong.toString().trimTo13Chars()
            )
        )
    }
    var selectedType by remember { mutableStateOf(WaterSourceType.NONE) }
    var selectedStatus by remember { mutableStateOf(WaterSourceStatus.NONE) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }

    var isLatError by remember { mutableStateOf(false) }
    var isLongError by remember { mutableStateOf(false) }
    var isWaterTypeError by remember { mutableStateOf(false) }
    var isWaterStatusError by remember { mutableStateOf(false) }
    var takingPhoto by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        photoUri = uri
    }

    val textColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val singlePermissionState =
        rememberPermissionState(permission = Manifest.permission.CAMERA) {
            coroutineScope.launch {
                var snackText = ""

                if (it) {
                    takingPhoto = true
                } else {
                    snackText = context.getString(R.string.camera_permission_is_required)
                }

                if (snackText.isNotEmpty()) snackbarHostState.showSnackbar(
                    message = snackText,
                    duration = SnackbarDuration.Short
                )
            }
        }

    // Handle back press
    BackHandler {
        photoUri?.deleteImageFromAppFolder()
        onNavigateBack()
    }

    CompositionLocalProvider(value = LocalTheme provides DarkTheme(LocalTheme.current.isDark)) {
        CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize(),
                topBar = {
                    CenterAlignedTopAppBar(
                        title = { Text(text = stringResource(R.string.new_water_source)) },
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
                },
                snackbarHost = { SnackbarHost(snackbarHostState) },
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp)
                            .imePadding()
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                if (it.text.length <= maxCharsForName) {
                                    name = it
                                }
                            },
                            shape = MaterialTheme.shapes.small,
                            label = {
                                if (name.text.isEmpty()) {
                                    Text(
                                        stringResource(R.string.name),
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Text(
                                        stringResource(
                                            R.string.name_limits,
                                            name.text.length,
                                            maxCharsForName
                                        ),
                                        fontSize = 14.sp
                                    )
                                }
                            },
                            placeholder = {
                                Text(
                                    stringResource(R.string.ex_name),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(color = textColor),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(spacingBetweenFields))

                        OutlinedTextField(
                            value = details,
                            onValueChange = {
                                if (it.text.length <= maxCharsForDetails) {
                                    details = it
                                }
                            },
                            shape = MaterialTheme.shapes.small,
                            label = {
                                if (details.text.isEmpty()) {
                                    Text(
                                        stringResource(R.string.details),
                                        fontSize = 14.sp
                                    )
                                } else {
                                    Text(
                                        stringResource(
                                            R.string.details_limits,
                                            details.text.length,
                                            maxCharsForDetails
                                        ),
                                        fontSize = 14.sp
                                    )
                                }
                            },
                            placeholder = {
                                Text(
                                    stringResource(R.string.ex_details),
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                            },
                            textStyle = LocalTextStyle.current.copy(color = textColor),
                            modifier = Modifier.fillMaxWidth(),
                        )

                        Spacer(modifier = Modifier.height(spacingBetweenFields))

                        // Water source type and status
                        Row(
                            modifier = Modifier.padding(top = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            WaterSourceTypeDropDownComp(
                                modifier = Modifier.weight(1f),
                                isWaterTypeError,
                                selectedType,
                                onItemSelected = {
                                    if (it != WaterSourceType.NONE) {
                                        isWaterTypeError = false
                                    }
                                    selectedType = it
                                }
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            WaterSourceStatusDropDownComp(
                                modifier = Modifier.weight(1f),
                                isWaterStatusError,
                                selectedStatus,
                                onItemSelected = {
                                    if (it != WaterSourceStatus.NONE) {
                                        isWaterStatusError = false
                                    }
                                    selectedStatus = it
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(spacingBetweenFields))

                        // Lat and Long
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = latitude,
                                onValueChange = {
                                    // the maximum numbers should be 13
                                    if (it.text.length <= 13) {
                                        isLatError = !isLatInBulgaria(it.text)

                                        latitude = it
                                    }
                                },
                                singleLine = true,
                                shape = MaterialTheme.shapes.small,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = {
                                    Text(
                                        stringResource(R.string.latitude),
                                        fontSize = 14.sp
                                    )
                                },
                                placeholder = { Text(stringResource(R.string.ex_42_7339), fontSize = 14.sp) },
                                isError = isLatError,
                                supportingText = {
                                    if (isLatError) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = stringResource(R.string.coordinate_is_outside_bulgaria),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                textStyle = LocalTextStyle.current.copy(color = textColor),
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            OutlinedTextField(
                                value = longitude,
                                onValueChange = {
                                    // the maximum numbers should be 13
                                    if (it.text.length <= 13) {
                                        isLongError = !isLongInBulgaria(it.text)

                                        longitude = it
                                    }
                                },
                                singleLine = true,
                                shape = MaterialTheme.shapes.small,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                label = {
                                    Text(
                                        stringResource(R.string.longitude),
                                        fontSize = 14.sp
                                    )
                                },
                                placeholder = { Text(stringResource(R.string.ex_25_4858), fontSize = 14.sp) },
                                isError = isLongError,
                                supportingText = {
                                    if (isLongError) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = stringResource(R.string.coordinate_is_outside_bulgaria),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                textStyle = LocalTextStyle.current.copy(color = textColor),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Text(text = stringResource(R.string.mandatory_field))

                        Spacer(modifier = Modifier.height(spacingBetweenFields))

                        // Take a phone or pick a picture
                        if (photoUri == null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        if (singlePermissionState.status == PermissionStatus.Granted) {
                                            takingPhoto = true
                                        } else {
                                            singlePermissionState.launchPermissionRequest()
                                        }
                                    }
                                ) {
                                    Text(stringResource(R.string.take_a_photo))
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    modifier = Modifier.weight(1f),
                                    onClick = { imagePickerLauncher.launch("image/*") }
                                ) {
                                    Text(stringResource(R.string.pick_a_photo))
                                }
                            }
                        }

                        photoUri?.let {
                            Spacer(modifier = Modifier.height(8.dp))

                            Box {
                                Image(
                                    painter = rememberAsyncImagePainter(it),
                                    contentDescription = stringResource(R.string.selected_photo),
                                    modifier = Modifier
                                        .size(128.dp)
                                        .background(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clip(shape = RoundedCornerShape(8.dp))
                                )

                                FilledIconButton(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .align(Alignment.TopEnd)
                                        .size(24.dp),
                                    onClick = onDebounceClick {
                                        photoUri?.deleteImageFromAppFolder()
                                        photoUri = null
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        tint = Color.Black,
                                        contentDescription = stringResource(R.string.remove_photo),
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .padding(16.dp),
                        onClick = onDebounceClick {
                            if (!isLatError && !isLongError) {
                                if (selectedType == WaterSourceType.NONE) {
                                    isWaterTypeError = true
                                    return@onDebounceClick
                                }

                                if (selectedStatus == WaterSourceStatus.NONE) {
                                    isWaterStatusError = true
                                    return@onDebounceClick
                                }

                                // if the mandatory field are okay we can send an email
                                sendEmailWithWaterSourceInfo(
                                    context,
                                    name.text,
                                    details.text,
                                    latitude.text,
                                    longitude.text,
                                    selectedType,
                                    selectedStatus,
                                    photoUri
                                )
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.submit_water_source))
                    }
                }
            }

            AnimatedVisibility(
                visible = takingPhoto,
                enter = scaleIn(animationSpec = tween(durationMillis = 500)),
                exit = scaleOut()
            ) {
                CameraView(
                    onNavigateBack = { takingPhoto = false },
                    onImageCaptured = { photoUri = it }
                )
            }
        }
    }
}

@Composable
fun WaterSourceTypeDropDownComp(
    modifier: Modifier,
    isError: Boolean,
    waterSourceType: WaterSourceType,
    onItemSelected: (WaterSourceType) -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier
    ) {
        WaterSourceTypeDropDownMenu(
            isError,
            waterSourceType,
            onItemSelected = {
                onItemSelected.invoke(convertIntToWaterSourceType(it))
            }
        )
    }
}

@Composable
fun WaterSourceStatusDropDownComp(
    modifier: Modifier,
    isError: Boolean,
    waterSourceStatus: WaterSourceStatus,
    onItemSelected: (WaterSourceStatus) -> Unit
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier
    ) {
        WaterSourceStatusDropDownMenu(
            isError,
            waterSourceStatus,
            onItemSelected = {
                onItemSelected.invoke(convertIntToWaterSourceStatus(it))
            }
        )
    }
}

@Composable
private fun WaterSourceTypeDropDownMenu(
    isError: Boolean,
    waterSourceType: WaterSourceType,
    onItemSelected: (Int) -> Unit
) {
    var shouldShowPlaceholder by remember { mutableStateOf(true) }
    val placeholderTextColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)

    val currentWaterSourceType = when (waterSourceType) {
        WaterSourceType.URBAN_WATER -> R.string.urban_water_source
        WaterSourceType.ESTABLISHMENT -> R.string.establishment
        WaterSourceType.MINERAL_WATER -> R.string.mineral_water
        WaterSourceType.HOT_MINERAL_WATER -> R.string.hot_mineral_water
        WaterSourceType.SPRING_WATER -> R.string.spring_water
        WaterSourceType.NONE -> R.string.select_type
    }

    Menu(
        modifier = Modifier.wrapContentSize(),
        state = rememberMenuState(expanded = false)
    ) {
        MenuButton(
            modifier = Modifier
                .border(
                    width = if (isError) 2.dp else 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.Gray,
                    shape = MaterialTheme.shapes.small
                )
                .height(56.dp),
            indication = ripple(radius = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (shouldShowPlaceholder) {
                    Text(
                        text = stringResource(currentWaterSourceType),
                        style = TextStyle(placeholderTextColor.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                } else {
                    Text(
                        text = stringResource(currentWaterSourceType),
                        style = TextStyle(
                            fontWeight = FontWeight(500),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp)
                    )
                }
            }
        }

        MenuContent(
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(6.dp),
            exit = fadeOut()
        ) {
            if (currentWaterSourceType != R.string.urban_water_source || shouldShowPlaceholder) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        shouldShowPlaceholder = false
                        onItemSelected.invoke(R.string.urban_water_source)
                    }) {
                    BasicText(
                        text = stringResource(R.string.urban_water_source),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }

            if (currentWaterSourceType != R.string.establishment) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        shouldShowPlaceholder = false
                        onItemSelected.invoke(R.string.establishment)
                    }) {
                    BasicText(
                        text = stringResource(R.string.establishment),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }

            if (currentWaterSourceType != R.string.mineral_water) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        shouldShowPlaceholder = false
                        onItemSelected.invoke(R.string.mineral_water)
                    }) {
                    BasicText(
                        text = stringResource(R.string.mineral_water),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }

            if (currentWaterSourceType != R.string.hot_mineral_water) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        shouldShowPlaceholder = false
                        onItemSelected.invoke(R.string.hot_mineral_water)
                    }) {
                    BasicText(
                        text = stringResource(R.string.hot_mineral_water),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }

            if (currentWaterSourceType != R.string.spring_water) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        shouldShowPlaceholder = false
                        onItemSelected.invoke(R.string.spring_water)
                    }) {
                    BasicText(
                        text = stringResource(R.string.spring_water),
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
private fun WaterSourceStatusDropDownMenu(
    isError: Boolean,
    waterSourceStatus: WaterSourceStatus,
    onItemSelected: (Int) -> Unit
) {
    var shouldShowPlaceholder by remember { mutableStateOf(true) }
    val placeholderTextColor = LocalContentColor.current.copy(alpha = LocalContentAlpha.current)

    val currentWaterSourceType = when (waterSourceStatus) {
        WaterSourceStatus.WORKING -> R.string.working
        WaterSourceStatus.UNDER_CONSTRUCTION -> R.string.under_construction
        WaterSourceStatus.OUT_OF_ORDER -> R.string.out_of_order
        WaterSourceStatus.FOR_REVIEW -> R.string.for_review
        WaterSourceStatus.NONE -> R.string.select_status
    }

    Menu(
        modifier = Modifier.wrapContentSize(),
        state = rememberMenuState(expanded = false)
    ) {
        MenuButton(
            Modifier
                .border(
                    width = if (isError) 2.dp else 1.dp,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.Gray,
                    shape = MaterialTheme.shapes.small
                )
                .height(56.dp),
            indication = ripple(radius = 0.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (shouldShowPlaceholder) {
                    Text(
                        text = stringResource(currentWaterSourceType),
                        style = TextStyle(placeholderTextColor.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    )
                } else {
                    Text(
                        text = stringResource(currentWaterSourceType),
                        style = TextStyle(
                            fontWeight = FontWeight(500),
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 10.dp)
                    )
                }
            }
        }

        MenuContent(
            modifier = Modifier
                .padding(top = 4.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White)
                .padding(6.dp),
            exit = fadeOut()
        ) {
            if (currentWaterSourceType != R.string.working || shouldShowPlaceholder) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        shouldShowPlaceholder = false
                        onItemSelected.invoke(R.string.working)
                    }) {
                    BasicText(
                        text = stringResource(R.string.working),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }

            if (currentWaterSourceType != R.string.under_construction) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        shouldShowPlaceholder = false
                        onItemSelected.invoke(R.string.under_construction)
                    }) {
                    BasicText(
                        text = stringResource(R.string.under_construction),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }

            if (currentWaterSourceType != R.string.out_of_order) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        shouldShowPlaceholder = false
                        onItemSelected.invoke(R.string.out_of_order)
                    }) {
                    BasicText(
                        text = stringResource(R.string.out_of_order),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }

            if (currentWaterSourceType != R.string.for_review) {
                MenuItem(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                    onClick = {
                        shouldShowPlaceholder = false
                        onItemSelected.invoke(R.string.for_review)
                    }) {
                    BasicText(
                        text = stringResource(R.string.for_review),
                        style = TextStyle(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .padding(vertical = 10.dp, horizontal = 8.dp)
                    )
                }
            }
        }
    }
}

fun sendEmailWithWaterSourceInfo(
    context: Context,
    name: String,
    details: String,
    latitude: String,
    longitude: String,
    selectedType: WaterSourceType,
    selectedStatus: WaterSourceStatus,
    photoUri: Uri?
) {

    // Format the email body with the water source information
    val emailBody = """
        Информация за източника:

        Име: $name
        Детайли: $details
        Latitude: $latitude
        Longitude: $longitude
        Тип: ${selectedType.ordinal}
        Статус: ${selectedStatus.ordinal}
    """.trimIndent()

    if (photoUri != null) {
        val uri = photoUri.copyAttachmentToCache(context) ?: return

        ShareCompat.IntentBuilder(context)
            .setType("*/*")
            .setSubject("Предложение за нов източник")
            .setEmailTo(arrayOf(CONTACT_EMAIL))
            .setText(emailBody)
            .addStream(uri)
            .startChooser()
    } else {
        ShareCompat.IntentBuilder(context)
            .setType("text/plain")
            .setSubject("Предложение за нов източник")
            .setEmailTo(arrayOf(CONTACT_EMAIL))
            .setText(emailBody)
            .startChooser()
    }
}

private fun convertIntToWaterSourceType(waterSourceTypeIntRes: Int): WaterSourceType {
    return when (waterSourceTypeIntRes) {
        R.string.urban_water_source -> WaterSourceType.URBAN_WATER
        R.string.establishment -> WaterSourceType.ESTABLISHMENT
        R.string.mineral_water -> WaterSourceType.MINERAL_WATER
        R.string.hot_mineral_water -> WaterSourceType.HOT_MINERAL_WATER
        R.string.spring_water -> WaterSourceType.SPRING_WATER
        else -> WaterSourceType.URBAN_WATER
    }
}

private fun convertIntToWaterSourceStatus(waterSourceStatusIntRes: Int): WaterSourceStatus {
    return when (waterSourceStatusIntRes) {
        R.string.working -> WaterSourceStatus.WORKING
        R.string.under_construction -> WaterSourceStatus.UNDER_CONSTRUCTION
        R.string.out_of_order -> WaterSourceStatus.OUT_OF_ORDER
        R.string.for_review -> WaterSourceStatus.FOR_REVIEW
        else -> WaterSourceStatus.WORKING
    }
}

private fun isLatInBulgaria(latitude: String): Boolean {
    val minLatitude = 41.235347
    val maxLatitude = 44.216667

    return latitude.toDouble() in minLatitude..maxLatitude
}

private fun isLongInBulgaria(longitude: String): Boolean {
    val minLongitude = 22.357332
    val maxLongitude = 28.612194

    return longitude.toDouble() in minLongitude..maxLongitude
}