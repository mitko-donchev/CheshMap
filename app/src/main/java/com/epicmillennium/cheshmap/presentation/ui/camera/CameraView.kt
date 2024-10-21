package com.epicmillennium.cheshmap.presentation.ui.camera


import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.epicmillennium.cheshmap.R
import com.epicmillennium.cheshmap.presentation.theme.CheshMapTheme
import com.epicmillennium.cheshmap.presentation.theme.DarkTheme
import com.epicmillennium.cheshmap.presentation.theme.LocalTheme
import com.epicmillennium.cheshmap.utils.deleteImageFromAppFolder
import com.epicmillennium.cheshmap.utils.rotate
import com.epicmillennium.cheshmap.utils.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraView(
    onNavigateBack: () -> Unit,
    onImageCaptured: (Uri) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var convertingImage by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                CameraController.IMAGE_CAPTURE
            )
        }
    }

    val previewView = remember {
        PreviewView(context).apply {
            this.controller = controller
            controller.isVideoCaptureEnabled
            controller.bindToLifecycle(lifecycleOwner)
        }
    }

    // Handle back press
    BackHandler { onNavigateBack() }

    CompositionLocalProvider(value = LocalTheme provides DarkTheme(LocalTheme.current.isDark)) {
        CheshMapTheme(darkTheme = LocalTheme.current.isDark) {
            Scaffold(modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {},
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
                // Progress indicator
                if (convertingImage && imageUri == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .zIndex(1f)
                            .background(color = MaterialTheme.colorScheme.background),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(modifier = Modifier.size(48.dp)) {
                            CircularProgressIndicator()
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    if (imageUri == null) {
                        AndroidView(
                            factory = { previewView },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                        CaptureBottomBar(
                            setConvertingImage = { convertingImage = true },
                            onImagePicked = { imageUri = it },
                            controller,
                        )
                    } else {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .build(),
                            loading = {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(modifier = Modifier.size(48.dp)) {
                                        CircularProgressIndicator()
                                    }
                                }
                            },
                            contentDescription = stringResource(R.string.captured_image_preview),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            TextButton(
                                modifier = Modifier.padding(end = 8.dp),
                                onClick = {
                                    imageUri?.deleteImageFromAppFolder()
                                    convertingImage = false
                                    imageUri = null
                                }
                            ) {
                                Text(text = stringResource(R.string.retake))
                            }

                            Button(
                                modifier = Modifier.padding(end = 8.dp),
                                onClick = {
                                    imageUri?.let { onImageCaptured.invoke(it) }
                                    onNavigateBack.invoke()
                                }
                            ) {
                                Text(text = stringResource(R.string.use))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureBottomBar(
    setConvertingImage: () -> Unit,
    onImagePicked: (Uri) -> Unit,
    controller: LifecycleCameraController,
) {
    val context = LocalContext.current

    Row(
        modifier = Modifier
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .size(64.dp)
                .background(
                    color = Color.Transparent,
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                )
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    setConvertingImage.invoke()
                    captureImage(controller, context) { bitmap ->
                        bitmap
                            .toUri(context)
                            ?.let { onImagePicked.invoke(it) }
                    }
                },
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = MaterialTheme.colorScheme.primary, shape = CircleShape),
            )
        }
    }
}

private fun captureImage(
    controller: LifecycleCameraController,
    context: Context,
    onImageCaptured: (Bitmap) -> Unit
) {
    controller.takePicture(
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(imageProxy: ImageProxy) {

                val bitmap = imageProxy.toBitmap()
                imageProxy.close()
                Log.d(this.toString(), "Image captured successfully.")

                // Send the captured bitmap to the callback
                onImageCaptured(bitmap.rotate(imageProxy.imageInfo.rotationDegrees))

                imageProxy.close()
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(this.toString(), "Failed to capture image: $exception")
            }
        }
    )
}