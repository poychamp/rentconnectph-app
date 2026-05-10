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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.Style
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures

@Composable
fun LocationMapDisplay(
    latitude: Double,
    longitude: Double,
    onTouchChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val isDark = isSystemInDarkTheme()
    val style = if (isDark) Style.DARK else Style.MAPBOX_STREETS
    val point = remember(latitude, longitude) { Point.fromLngLat(longitude, latitude) }
    val mapViewportState = rememberMapViewportState {
        setCameraOptions(
            CameraOptions.Builder()
                .center(point)
                .zoom(15.0)
                .build()
        )
    }
    val markerBitmap = remember { createOrangeMarker() }

    Box(modifier = modifier) {
        MapboxMap(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            mapViewportState = mapViewportState,
            style = { com.mapbox.maps.extension.compose.style.MapStyle(style = style) },
        ) {
            MapEffect(Unit) { mapView ->
                val annotationManager = mapView.annotations.createPointAnnotationManager()
                annotationManager.create(
                    PointAnnotationOptions()
                        .withPoint(point)
                        .withIconImage(markerBitmap)
                )

                mapView.gestures.addOnMoveListener(object : OnMoveListener {
                    override fun onMoveBegin(detector: com.mapbox.android.gestures.MoveGestureDetector) {
                        onTouchChanged(true)
                    }
                    override fun onMove(detector: com.mapbox.android.gestures.MoveGestureDetector): Boolean = false
                    override fun onMoveEnd(detector: com.mapbox.android.gestures.MoveGestureDetector) {
                        onTouchChanged(false)
                    }
                })
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp),
        ) {
            SmallFloatingActionButton(
                onClick = {
                    val currentZoom = mapViewportState.cameraState?.zoom ?: 15.0
                    mapViewportState.setCameraOptions(
                        CameraOptions.Builder().zoom(currentZoom + 1.0).build()
                    )
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Zoom in")
            }
            Spacer(Modifier.height(4.dp))
            SmallFloatingActionButton(
                onClick = {
                    val currentZoom = mapViewportState.cameraState?.zoom ?: 15.0
                    mapViewportState.setCameraOptions(
                        CameraOptions.Builder().zoom(currentZoom - 1.0).build()
                    )
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
    val size = 48
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = 0xFFF97316.toInt()
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint)
    paint.color = 0xFFFFFFFF.toInt()
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f
    canvas.drawCircle(size / 2f, size / 2f, size / 2f - 2f, paint)
    return bitmap
}
