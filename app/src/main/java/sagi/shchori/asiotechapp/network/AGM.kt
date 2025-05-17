package sagi.shchori.asiotechapp.network

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory
import com.bumptech.glide.module.AppGlideModule

@GlideModule
class AGM : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val cacheSizeBytes = 1024 * 1024 * 100L // 100MB
        builder.setDiskCache(DiskLruCacheFactory(context.cacheDir.absolutePath, cacheSizeBytes))
        super.applyOptions(context, builder)
    }
}