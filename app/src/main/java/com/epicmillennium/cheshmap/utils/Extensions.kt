package com.epicmillennium.cheshmap.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.epicmillennium.cheshmap.core.ui.theme.AppThemeMode
import com.epicmillennium.cheshmap.core.ui.theme.DarkTheme
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

// String
fun String.trimTo13Chars(): String {
    return if (length > 13) {
        take(13)
    } else {
        this
    }
}

// Context
fun Context.checkLocationPermissions(): Boolean {
    return ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(
        this,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
}

fun Context.clearAppCacheFromAttachments() {
    val listOfAttachmentsInCache = cacheDir.listFiles()?.filter { it.name.contains("IMG_") }
    if (!listOfAttachmentsInCache.isNullOrEmpty()) {
        listOfAttachmentsInCache.forEach {
            it.delete()
        }
    }
}

// Compose states
@Composable
fun AppThemeMode.retrieveDarkThemeFromState(): DarkTheme = when (this) {
    AppThemeMode.MODE_AUTO -> DarkTheme(isSystemInDarkTheme())
    AppThemeMode.MODE_DAY -> DarkTheme(false)
    AppThemeMode.MODE_NIGHT -> DarkTheme(true)
}

// Uri
fun Uri.deleteImageFromAppFolder(): Boolean {
    val file = this.path?.let { File(it) } ?: return true
    return if (file.exists()) file.delete() else true
}

fun Uri.copyAttachmentToCache(context: Context): Uri? {
    try {
        val file = File(context.cacheDir, this.toFile().name)

        val inputStream: InputStream? = context.contentResolver.openInputStream(this)
        val fileOutputStream = FileOutputStream(file)

        inputStream?.use { input ->
            fileOutputStream.use { output ->
                input.copyTo(output)
            }
        }

        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

// Bitmap
fun Bitmap.rotate(degrees: Int): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Bitmap.toUri(context: Context): Uri? {
    val filename = "IMG_${System.currentTimeMillis()}.jpg"

    try {
        val externalAttachmentDir = File(context.getExternalFilesDir(null), "attachments")

        if (!externalAttachmentDir.exists()) {
            externalAttachmentDir.mkdir()
        }

        val file = File(externalAttachmentDir, filename)

        FileOutputStream(file).use { outputStream ->
            this.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
        }

        return file.toUri()
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

// Modifier
fun Modifier.circleGradientBackground(colors: List<Color>, angle: Float) = this.then(
    Modifier.drawBehind {
        // Setting the angle in radians
        val angleRad = angle / 180f * PI

        // Fractional x
        val x = kotlin.math.cos(angleRad).toFloat()

        // Fractional y
        val y = kotlin.math.sin(angleRad).toFloat()

        // Set the Radius and offset as shown below
        val radius = sqrt(size.width.pow(2) + size.height.pow(2)) / 2f
        val offset = center + Offset(x * radius, y * radius)

        // Setting the exact offset
        val exactOffset = Offset(
            x = kotlin.math.min(offset.x.coerceAtLeast(0f), size.width),
            y = size.height - kotlin.math.min(offset.y.coerceAtLeast(0f), size.height)
        )

        // Draw a rectangle with the above values
        drawCircle(
            brush = Brush.linearGradient(
                colors = colors,
                start = Offset(size.width, size.height) - exactOffset,
                end = exactOffset
            ),
        )
    }
)