package com.attilapalfi.exceptional.persistence

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.support.v4.util.LruCache
import android.util.Log
import android.widget.ImageView
import com.attilapalfi.exceptional.dependency_injection.Injector
import com.attilapalfi.exceptional.model.Friend
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.math.BigInteger
import java.net.URL
import java.util.*
import javax.inject.Inject

/**
 * Created by 212461305 on 2015.07.06..
 */
public class ImageCache {
    private lateinit var imageWarehouse: LruCache<BigInteger, Bitmap>
    private lateinit var filePath: String
    private val viewRefreshMap = Collections.synchronizedMap(HashMap<Friend, ImageView>())
    private val handler: Handler
    public var context: Context? = null
        @Inject
        public set
        get

    init {
        Injector.INSTANCE.applicationComponent.inject(this)
        handler = Handler(Looper.getMainLooper())
        try {
            if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable()) {
                filePath = context!!.externalCacheDir!!.path
            } else {
                filePath = context!!.cacheDir!!.path
            }

            val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
            val cacheSize = maxMemory / 8
            imageWarehouse = object : LruCache<BigInteger, Bitmap>(cacheSize) {
                override fun sizeOf(key: BigInteger?, value: Bitmap?): Int {
                    val bitmapByteCount = value!!.rowBytes * value.height // The cache size will be measured in kilobytes rather than number of items.
                    return bitmapByteCount / 1024
                }
            }

        } catch (npe: NullPointerException) {
            Log.d("NullPointerException", npe.message)
        }

    }

    public fun setImageToView(friend: Friend, imageView: ImageView) {
        val cachedBitmap = imageWarehouse.get(friend.id)
        if (cachedBitmap != null) {
            imageView.setImageBitmap(cachedBitmap)
        } else {
            if (friend.isImageDownloaded()) {
                async {
                    val bitmap = getImageForFriend(friend)
                    uiThread {
                        imageView.setImageBitmap(bitmap)
                    }
                }
            } else {
                viewRefreshMap.put(friend, imageView)
            }
        }
    }

    public fun loadImagesInitiallyAsync(friendList: List<Friend>) {
        Thread {
            friendList.forEach { getImageForFriend(it) }
            handler.post { setBitmapsToViews(friendList) }
        }.start()
    }

    public fun loadImagesInitially(friendList: List<Friend>) {
        friendList.forEach { getImageForFriend(it) }
        handler.post { setBitmapsToViews(friendList) }
    }

    private fun setBitmapsToViews(friendList: List<Friend>) {
        friendList.forEach { friend ->
            val bitmap = imageWarehouse.get(friend.id)
            val view = viewRefreshMap[friend]
            view?.setImageBitmap(bitmap)
            viewRefreshMap.remove(friend)
        }
    }

    public fun updateImageAsync(newFriendState: Friend, oldFriendState: Friend) {
        async {
            try {
                removeImage(oldFriendState)
                val bitmap = getFromInternet(newFriendState)
                saveBitmapToDiskAndMem(newFriendState, bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getImageForFriend(friend: Friend): Bitmap? {
        var bitmap: Bitmap? = imageWarehouse.get(friend.id)
        if (bitmap == null) {
            if (imageFileExists(friend)) {
                bitmap = getFromDisk(friend)
                imageWarehouse.put(friend.id, bitmap)
            } else {
                try {
                    bitmap = getFromInternet(friend)
                    saveBitmapToDiskAndMem(friend, bitmap)
                } catch (e: IOException) {
                    e.printStackTrace()
                }

            }
        }
        return bitmap
    }

    private fun saveBitmapToDiskAndMem(friend: Friend, image: Bitmap) {
        imageWarehouse.put(friend.id, image)
        val pictureFilePath = getFriendsImageFilePath(friend)
        if (pictureFilePath != null) {
            saveImageToFile(image, pictureFilePath)
        }
    }

    private fun saveImageToFile(image: Bitmap, pictureFilePath: String) {
        val pictureFile = File(pictureFilePath)
        try {
            val fos = FileOutputStream(pictureFile)
            image.compress(Bitmap.CompressFormat.JPEG, 95, fos)
            fos.close()
        } catch (ioe: IOException) {
            Log.d("IOException", ioe.message)
        }

    }

    private fun isNotInMemory(friend: Friend): Boolean {
        return imageWarehouse.get(friend.id) == null
    }


    private fun imageFileExists(friend: Friend): Boolean {
        val filePath = getFriendsImageFilePath(friend)
        val file = File(filePath)
        return file.exists() && !file.isDirectory
    }

    private fun getFriendsImageFilePath(friend: Friend): String? {
        try {
            val mediaStorageDir = makeStorageDirectory() ?: return null
            val mImageName = "${friend.id}.jpg"
            return mediaStorageDir.path + File.separator + mImageName
        } catch (e: NullPointerException) {
            return null
        }

    }

    private fun makeStorageDirectory(): File? {
        val mediaStorageDir = File(filePath)
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null
            }
        }
        return mediaStorageDir
    }

    @Throws(IOException::class)
    private fun getFromInternet(friend: Friend): Bitmap {
        val url = URL(friend.imageUrl)
        val connection = url.openConnection()
        connection.useCaches = true
        val inputStream = connection.content as InputStream
        return BitmapFactory.decodeStream(inputStream)
    }

    private fun getFromDisk(friend: Friend): Bitmap? {
        val filePath = getFriendsImageFilePath(friend)
        if (filePath != null) {
            val file = File(filePath)
            return BitmapFactory.decodeFile(file.absolutePath)
        }

        return null
    }

    private fun removeImage(friend: Friend) {
        imageWarehouse.remove(friend.id)
        deleteFromDisk(friend)
    }

    public fun wipe(friendList: List<Friend>) {
        imageWarehouse.evictAll()
        friendList.forEach { this.deleteFromDisk(it) }
    }

    private fun deleteFromDisk(friend: Friend) {
        val filePath = getFriendsImageFilePath(friend)
        val imageFile = File(filePath)
        if (imageFile.exists()) {
            imageFile.delete()
        }
    }
}
