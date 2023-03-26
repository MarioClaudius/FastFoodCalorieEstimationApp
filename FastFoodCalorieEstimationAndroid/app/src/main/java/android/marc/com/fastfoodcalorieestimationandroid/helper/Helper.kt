package android.marc.com.fastfoodcalorieestimationandroid.helper

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Environment
import android.util.Log
import net.jpountz.lz4.LZ4BlockInputStream
import java.io.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

object Helper {
    fun String.withDateFormat(): String {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        val date = format.parse(this) as Date
        return DateFormat.getDateInstance(DateFormat.FULL).format(date)
    }
}

private const val FILENAME_FORMAT = "dd-MMM-yyyy"

val timeStamp: String = SimpleDateFormat(
    FILENAME_FORMAT,
    Locale.US
).format(System.currentTimeMillis())

fun createCustomTempFile(context: Context): File {
    val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File.createTempFile(timeStamp, ".jpg", storageDir)
}

fun rotateBitmap(bitmap: Bitmap, isBackCamera: Boolean = false): Bitmap {
    val matrix = Matrix()
    return if (isBackCamera) {
        matrix.postRotate(90f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    } else {
        matrix.postRotate(-90f)
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        Bitmap.createBitmap(
            bitmap,
            0,
            0,
            bitmap.width,
            bitmap.height,
            matrix,
            true
        )
    }
}

fun uriToFile(selectedImg: Uri, context: Context): File {
    val contentResolver: ContentResolver = context.contentResolver
    val myFile = createCustomTempFile(context)

    val inputStream = contentResolver.openInputStream(selectedImg) as InputStream
    val outputStream: OutputStream = FileOutputStream(myFile)
    val buf = ByteArray(1024)
    var len: Int
    while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
    outputStream.close()
    inputStream.close()

    return myFile
}

fun reduceFileImage(file: File): File {
    val bitmap = BitmapFactory.decodeFile(file.path)
    var compressQuality = 100
    var streamLength: Int
    do {
        val bmpStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
        val bmpPicByteArray = bmpStream.toByteArray()
        streamLength = bmpPicByteArray.size
        compressQuality -= 5
    } while (streamLength > 1000000)
    bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
    return file
}

fun toBitmap(byteArray: ByteArray) : Bitmap {
    Log.d("HELPER", byteArray::class.simpleName.toString())
    Log.d("HELPER", byteArray.size.toString())
    return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
}

fun decompressLz4(byteArray: ByteArray): ByteArray {
    val blockSize = 4096 // set the block size to read data in chunks
    val buffer = ByteArray(blockSize)
    val inputStream = ByteArrayInputStream(byteArray)
    val lz4BlockInputStream = LZ4BlockInputStream(inputStream)
    val outputStream = ByteArrayOutputStream()

    var bytesRead = 0
    while (lz4BlockInputStream.read(buffer, 0, blockSize).also { bytesRead = it } != -1) {
        outputStream.write(buffer, 0, bytesRead)
    }

    outputStream.close()
    lz4BlockInputStream.close()
    inputStream.close()

    return outputStream.toByteArray()
}