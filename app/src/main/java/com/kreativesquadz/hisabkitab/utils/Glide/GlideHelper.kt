package com.kreativesquadz.hisabkitab.utils.Glide

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.CustomTarget
import com.kreativesquadz.hisabkitab.Config
import com.kreativesquadz.hisabkitab.R
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import java.io.InputStream

object GlideHelper {

    // Initialize Glide with OkHttpClient and API Key for headers
    fun initializeGlideWithOkHttp(context: Context) {
        val client = GlideUtils.createOkHttpClient()
        Glide.get(context).registry.replace(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(client))
    }

    // Load image with API key header
    fun loadImage(context: Context, url: String, imageView: ImageView) {
        Glide.with(context)
            .load(GlideUrl(Config.APP_API_IMAGE_URL + url))
            .apply(RequestOptions().placeholder(R.drawable.add_image).error(R.drawable.add_image))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }
    fun loadImageWithLoader(context: Context, url: String?, imageView: ImageView, progressBar: ProgressBar) {
        if (url.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.add_image)
            progressBar.visibility = View.GONE // Hide loader
            return
        }

        progressBar.visibility = View.VISIBLE // Show loader

        Glide.with(context)
            .load(GlideUrl(Config.APP_API_IMAGE_URL + url))
            .apply(RequestOptions().placeholder(R.drawable.add_image).error(R.drawable.add_image))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE // Hide loader on error
                    return false // Allow Glide to handle the error drawable
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    progressBar.visibility = View.GONE // Hide loader when loaded
                    return false // Allow Glide to set the ImageView
                }
            })
            .into(imageView)
    }

    fun loadBitmap(context: Context, url: String, onBitmapLoaded: (Bitmap) -> Unit) {
        Glide.with(context)
            .asBitmap()
            .load(GlideUrl(Config.APP_API_IMAGE_URL + url))
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                    onBitmapLoaded(resource) // Callback with the loaded bitmap
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle cleanup if necessary
                }
            })
    }


}
