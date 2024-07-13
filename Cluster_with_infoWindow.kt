package com.ldl.magnitudo.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.Build
import android.util.TypedValue
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.MarkerInfoWindow
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.ldl.magnitudo.R
import com.ldl.magnitudo.viewmodel.EarthQuakeUIModel
import com.ldl.magnitudo.viewmodel.MapViewModel
import kotlinx.coroutines.launch


@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun MapsScreen(mapsViewModel: MapViewModel) {
    val context = LocalContext.current
    val state = mapsViewModel.getEarthquakes().observeAsState()
    val earthQuakesList = state.value?.toMutableList() ?: listOf()
    var itemSelected by remember {
        mutableStateOf<EarthQuakeUIModel?>(null)
    }
    val coroutineScope = rememberCoroutineScope()
    val markerState = rememberMarkerState()

    val pin = mapsViewModel.getHomePosition()
    if (pin != null) {
        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(pin, 5f)
        }
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 72.dp),
            cameraPositionState = cameraPositionState,
            googleMapOptionsFactory = {
                val option = GoogleMapOptions()
                option.mapType(com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID)
                option
            },
            onMapClick = { itemSelected = null }
        ) {
            Clustering(
                items = earthQuakesList,
                onClusterItemClick = {
                    if (itemSelected != null) itemSelected = null
                    itemSelected = it
                    false
                },
                clusterContent = { cluster ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(color = Color.Blue, shape = CircleShape)
                    ) {
                        Text(
                            modifier = Modifier.align(Alignment.Center),
                            text = cluster.size.toString(),
                            color = Color.White
                        )
                    }
                }, clusterItemContent = {
                    Image(
                        modifier = Modifier.size(48.dp),
                        painter = painterResource(id = R.drawable.ic_home_broken),
                        contentDescription = "pin"
                    )
                })
            itemSelected?.let { e ->
                MarkerInfoWindow(
                    state = markerState,
                    icon = BitmapDescriptorFactory.fromBitmap(
                        getBitmapFromVectorDrawable(
                            context,
                            R.drawable.ic_home_broken
                        )
                    ),
                    onInfoWindowClose = {
                        itemSelected = null
                    }) {
                    Column(
                        modifier = Modifier
                            .wrapContentSize()
                            .background(
                                color = Color.White.copy(alpha = 0.75f),
                                shape = RoundedCornerShape(24.dp)
                            ).padding(16.dp)
                    ) {
                        Text(
                            text = "This is a Test",
                            color = Color.Blue,
                            style = TextStyle(fontSize = 24.sp)
                        )
                        Text(
                            text = "${itemSelected?.locality.orEmpty()} - ${itemSelected?.magnitude}",
                            color = Color.Blue,
                            style = TextStyle(fontSize = 18.sp)
                        )
                    }
                }
                coroutineScope.launch {
                    markerState.position = LatLng(
                        itemSelected?.latitude ?: 0.0,
                        itemSelected?.longitude ?: 0.0
                    )
                    markerState.showInfoWindow()
                }
            }
        }
    } else {
        GoogleMap(
            modifier = Modifier.fillMaxSize()
        )
    }
}

fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): Bitmap {
    val drawable = ContextCompat.getDrawable(context!!, drawableId)

    val widthPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics
    ).toInt()
    val heightPx = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP, 48f, context.resources.displayMetrics
    ).toInt()

    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)

    val canvas = Canvas(bitmap)
    drawable!!.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)

    return bitmap
}
