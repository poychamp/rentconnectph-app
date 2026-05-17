package ph.rentconnect.app.feature.detail.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import androidx.compose.ui.geometry.Offset
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import ph.rentconnect.app.R

@Composable
fun LocationMapDisplay(
    latitude: Double,
    longitude: Double,
    onTouchChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current
    val position = remember(latitude, longitude) { LatLng(latitude, longitude) }
    var currentZoom by remember { mutableFloatStateOf(15f) }

    val cameraPositionState = rememberCameraPositionState {
        this.position = CameraPosition.fromLatLngZoom(position, 15f)
    }

    LaunchedEffect(cameraPositionState.isMoving) {
        if (cameraPositionState.isMoving &&
            cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE
        ) {
            onTouchChanged(true)
        } else if (!cameraPositionState.isMoving) {
            onTouchChanged(false)
            currentZoom = cameraPositionState.position.zoom
        }
    }

    val mapStyleOptions = remember(isDark) {
        if (isDark) {
            MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark)
        } else {
            null
        }
    }

    val mapProperties = remember(mapStyleOptions) {
        MapProperties(mapStyleOptions = mapStyleOptions)
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false,
            zoomGesturesEnabled = true,
            scrollGesturesEnabled = true,
            rotationGesturesEnabled = true,
            tiltGesturesEnabled = false,
        )
    }

    val markerBitmap = remember { createOrangeMarker() }
    val markerIcon = remember(markerBitmap) {
        MapsInitializer.initialize(context)
        BitmapDescriptorFactory.fromBitmap(markerBitmap)
    }

    Box(modifier = modifier) {
        GoogleMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onPOIClick = {},
        ) {
            Marker(
                state = MarkerState(position = position),
                icon = markerIcon,
                anchor = Offset(0.5f, 0.5f),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
        ) {
            SmallFloatingActionButton(
                onClick = {
                    val newZoom = currentZoom + 1f
                    currentZoom = newZoom
                    cameraPositionState.move(CameraUpdateFactory.zoomTo(newZoom))
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Zoom in")
            }
            Spacer(Modifier.height(4.dp))
            SmallFloatingActionButton(
                onClick = {
                    val newZoom = currentZoom - 1f
                    currentZoom = newZoom
                    cameraPositionState.move(CameraUpdateFactory.zoomTo(newZoom))
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Zoom out")
            }
        }
    }
}

private fun createOrangeMarker(): Bitmap {
    val sizePx = 40
    val strokePx = 4f
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = 0xFFF97316.toInt()
    paint.style = Paint.Style.FILL
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - strokePx / 2f, paint)
    paint.color = 0xFFFFFFFF.toInt()
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = strokePx
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - strokePx / 2f, paint)
    return bitmap
}
