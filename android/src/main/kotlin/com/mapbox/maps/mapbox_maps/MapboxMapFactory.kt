package com.mapbox.maps.mapbox_maps

import android.annotation.SuppressLint
import android.content.Context
import com.mapbox.common.FeatureTelemetryCounter
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapOptions
import com.mapbox.maps.applyDefaultParams
import com.mapbox.maps.mapbox_maps.pigeons._MapInterface
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class MapboxMapFactory(
  private val messenger: BinaryMessenger,
  private val flutterAssets: FlutterPlugin.FlutterAssets,
  private val lifecycleProvider: MapboxMapsPlugin.LifecycleProvider
) : PlatformViewFactory(_MapInterface.codec) {

  @SuppressLint("RestrictedApi")
  override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
    if (context == null) {
      throw RuntimeException("Context is null, can't create MapView!")
    }
    val params = args as Map<String, Any>
    val mapOptions = params["mapOptions"] as com.mapbox.maps.mapbox_maps.pigeons.MapOptions?
    val cameraOptions = params["cameraOptions"] as com.mapbox.maps.mapbox_maps.pigeons.CameraOptions?
    val channelSuffix = params["channelSuffix"] as Long
    val textureView = params["textureView"] as? Boolean ?: false
    val styleUri = params["styleUri"] as? String
    val styleJson = params["styleJson"] as? String
    val initialScaleBarEnabled = params["initialScaleBarEnabled"] as? Boolean
    val initialCompassEnabled = params["initialCompassEnabled"] as? Boolean
    val pluginVersion = params["mapboxPluginVersion"] as String
    val eventTypes = params["eventTypes"] as List<Long>

    // When styleJson is provided it takes precedence — we load it post-init
    // via MapboxMap.loadStyleJson. MapInitOptions.styleUri stays null so the
    // SDK doesn't start a redundant URI load first.
    val mapInitOptions = MapInitOptions(
      context = context,
      mapOptions = mapOptions?.toMapOptions(context) ?: MapOptions.Builder()
        .applyDefaultParams(context).build(),
      cameraOptions = cameraOptions?.toCameraOptions(context),
      textureView = textureView,
      styleUri = if (styleJson != null) null else styleUri
    )
    mapCounter.increment()
    return MapboxMapController(
      context,
      mapInitOptions,
      lifecycleProvider,
      messenger,
      channelSuffix,
      pluginVersion,
      eventTypes,
      initialScaleBarEnabled,
      initialCompassEnabled,
      styleJson
    )
  }

  companion object {
    @SuppressLint("RestrictedApi")
    private val mapCounter = FeatureTelemetryCounter.create("maps-mobile/flutter/map")
  }
}