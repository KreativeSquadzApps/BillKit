package com.kreativesquadz.billkit.utils.Glide

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader
import com.bumptech.glide.load.model.GlideUrl
import com.kreativesquadz.billkit.Config
import com.kreativesquadz.billkit.R
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
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
            .load(GlideUrl(Config.APP_API_IMAGE_URL + url)) // Make sure the complete URL is correct
            .apply(RequestOptions().placeholder(R.drawable.add_image).error(R.drawable.add_image))
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
    }

}
