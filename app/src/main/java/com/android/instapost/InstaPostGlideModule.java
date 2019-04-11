package com.android.instapost;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.module.AppGlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.firebase.storage.StorageReference;

import java.io.InputStream;

import static com.bumptech.glide.load.DecodeFormat.PREFER_ARGB_8888;

@com.bumptech.glide.annotation.GlideModule
public class InstaPostGlideModule extends AppGlideModule {

    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
//        int memoryCacheSizeBytes = 1024 * 1024 * 20; // 20mb
//        builder.setMemoryCache(new LruResourceCache(memoryCacheSizeBytes));
//        builder.setDiskCache(new InternalCacheDiskCacheFactory(context, memoryCacheSizeBytes));
        builder.setDefaultRequestOptions(requestOptions(context));
    }

    @Override
    public void registerComponents(Context context, Glide glide, Registry registry) {
        // Register FirebaseImageLoader to handle StorageReference
        registry.append(StorageReference.class, InputStream.class,
                new FirebaseImageLoader.Factory());
    }

    private static RequestOptions requestOptions(Context context){
        return new RequestOptions()
                .centerCrop()
                .encodeFormat(Bitmap.CompressFormat.JPEG)
                .encodeQuality(100)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .format(PREFER_ARGB_8888)
                .skipMemoryCache(false);
    }
}
