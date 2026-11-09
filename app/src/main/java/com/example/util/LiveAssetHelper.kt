package com.example.util

import android.content.Context
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder

object LiveAssetHelper {

    private var imageLoaderInstance: ImageLoader? = null

    /**
     * Singleton ImageLoader configured with GIF decoding support.
     * Compatible with Android 7.0+ (API 24+) through ImageDecoder on API 28+
     * and GifDecoder fallback on older APIs.
     */
    fun getImageLoader(context: Context): ImageLoader {
        return imageLoaderInstance ?: synchronized(this) {
            imageLoaderInstance ?: ImageLoader.Builder(context)
                .components {
                    if (Build.VERSION.SDK_INT >= 28) {
                        add(ImageDecoderDecoder.Factory())
                    } else {
                        add(GifDecoder.Factory())
                    }
                }
                .build()
                .also { imageLoaderInstance = it }
        }
    }

    /**
     * Helper to build Coil-friendly file:///android_asset/ URI
     */
    fun getAssetUri(relativePath: String): String {
        val cleanPath = relativePath.trimStart('/')
        return "file:///android_asset/$cleanPath"
    }

    /**
     * Check if a specific asset file exists in assets folder
     */
    fun assetExists(context: Context, relativePath: String): Boolean {
        return try {
            context.assets.open(relativePath.trimStart('/')).use { true }
        } catch (e: Exception) {
            false
        }
    }
}
