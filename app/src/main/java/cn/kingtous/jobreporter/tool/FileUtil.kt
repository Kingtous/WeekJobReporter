package cn.kingtous.jobreporter.tool

import android.annotation.SuppressLint
import android.content.*
import android.database.Cursor
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.support.v4.content.FileProvider
import android.text.TextUtils
import cn.kingtous.jobreporter.R
import com.qmuiteam.qmui.widget.dialog.QMUITipDialog
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object FileUtil {
    /**
     * 根据URI获取文件真实路径（兼容多张机型）
     * @param context
     * @param uri
     * @return
     */
    fun getFilePathByUri(context: Context, uri: Uri): String? {
        if ("content".equals(uri.scheme!!, ignoreCase = true)) {

            val sdkVersion = Build.VERSION.SDK_INT
            return if (sdkVersion >= 19) { // api >= 19
                getRealPathFromUriAboveApi19(context, uri)
            } else { // api < 19
                getRealPathFromUriBelowAPI19(context, uri)
            }
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }
        return null
    }

    /**
     * 适配api19及以上,根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    @SuppressLint("NewApi")
    private fun getRealPathFromUriAboveApi19(context: Context, uri: Uri): String? {
        var filePath: String? = null
        if (DocumentsContract.isDocumentUri(context, uri)) {
            // 如果是document类型的 uri, 则通过document id来进行处理
            val documentId = DocumentsContract.getDocumentId(uri)
            if (isMediaDocument(uri)) { // MediaProvider
                // 使用':'分割
                val type =
                    documentId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
                val id =
                    documentId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]

                val selection = MediaStore.Images.Media._ID + "=?"
                val selectionArgs = arrayOf(id)

                //
                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                filePath = getDataColumn(context, contentUri, selection, selectionArgs)
            } else if (isDownloadsDocument(uri)) { // DownloadsProvider
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"),
                    java.lang.Long.valueOf(documentId)
                )
                filePath = getDataColumn(context, contentUri, null, null)
            } else if (isExternalStorageDocument(uri)) {
                // ExternalStorageProvider
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]
                if ("primary".equals(type, ignoreCase = true)) {
                    filePath = Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }
            } else {
                //Log.e("路径错误");
            }
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            // 如果是 content 类型的 Uri
            filePath = getDataColumn(context, uri, null, null)
        } else if ("file" == uri.scheme) {
            // 如果是 file 类型的 Uri,直接获取图片对应的路径
            filePath = uri.path
        }
        return filePath
    }

    /**
     * 适配api19以下(不包括api19),根据uri获取图片的绝对路径
     *
     * @param context 上下文对象
     * @param uri     图片的Uri
     * @return 如果Uri对应的图片存在, 那么返回该图片的绝对路径, 否则返回null
     */
    private fun getRealPathFromUriBelowAPI19(context: Context, uri: Uri): String? {
        return getDataColumn(context, uri, null, null)
    }

    /**
     * 获取数据库表中的 _data 列，即返回Uri对应的文件路径
     *
     * @return
     */
    private fun getDataColumn(
        context: Context,
        uri: Uri?,
        selection: String?,
        selectionArgs: Array<String>?
    ): String? {
        var path: String? = null

        val projection = arrayOf(MediaStore.Images.Media.DATA)
        var cursor: Cursor? = null
        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(projection[0])
                path = cursor.getString(columnIndex)
            }
        } catch (e: Exception) {
            cursor?.close()
        }

        return path
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is MediaProvider
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri the Uri to check
     * @return Whether the Uri authority is DownloadsProvider
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }
    val FOLDER_NAME = "Excel"

    /**
     * 获取存贮文件的文件夹路径
     *
     * @return
     */
    fun createFolders(): File? {
        val baseDir: File = Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        if (baseDir == null)
            return Environment.getExternalStorageDirectory()
        val aviaryFolder = File(baseDir, FOLDER_NAME)
        if (aviaryFolder.exists())
            return aviaryFolder
        if (aviaryFolder.isFile)
            aviaryFolder.delete()
        return if (aviaryFolder.mkdirs()) aviaryFolder else Environment.getExternalStorageDirectory()
    }

    fun getExcelFolder(): String {
        val baseDir = Environment
            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString()
        return baseDir + File.separator + FOLDER_NAME
    }

    fun genEditFile(fileName: String): File? {
        return getEmptyFile(fileName)
    }

    fun getEmptyFile(name: String): File? {
        val folder = createFolders()
        if (folder != null) {
            if (folder!!.exists()) {
                return File(folder, name)
            }
        }
        return null
    }

    /**
     * 删除指定文件
     *
     * @param path
     * @return
     */
    fun deleteFileNoThrow(path: String): Boolean {
        val file: File
        try {
            file = File(path)
        } catch (e: NullPointerException) {
            return false
        }

        return if (file.exists()) {
            file.delete()
        } else false
    }

    /**
     * 保存图片
     *
     * @param bitName
     * @param mBitmap
     */
    fun saveBitmap(bitName: String, mBitmap: Bitmap): String {
        val baseFolder = createFolders()
        val f = File(baseFolder!!.absolutePath, bitName)
        var fOut: FileOutputStream? = null
        try {
            f.createNewFile()
            fOut = FileOutputStream(f)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        try {
            fOut!!.flush()
            fOut.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return f.absolutePath
    }

    // 获取文件夹大小
    @Throws(Exception::class)
    fun getFolderSize(file: File): Long {
        var size: Long = 0
        try {
            val fileList = file.listFiles()
            for (i in fileList!!.indices) { // 如果下面还有文件
                if (fileList[i].isDirectory) {
                    size = size + getFolderSize(fileList[i])
                } else {
                    size = size + fileList[i].length()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return size
    }

    /** * 格式化单位 * * @param size * @return  */
    fun getFormatSize(size: Double): String {
        val kiloByte = size / 1024.0
        val megaByte = (kiloByte / 1024.0).toInt()
        return megaByte.toString() + "MB"
    }

    /**
     *
     * @Description:
     * @Author 11120500
     * @Date 2013-4-25
     */
    fun isConnect(context: Context): Boolean {
        try {
            val connectivity = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (connectivity != null) {
                val info = connectivity.activeNetworkInfo
                if (info != null && info.isConnected) {
                    if (info.state == NetworkInfo.State.CONNECTED) {
                        return true
                    }
                }
            }
        } catch (e: Exception) {
        }
        return false
    }

    fun checkFileExist(path: String): Boolean {
        if (TextUtils.isEmpty(path))
            return false
        val file = File(getExcelFolder(),path+ ".xls")
        return file.exists()
    }

    // 获取文件扩展名
    fun getExtensionName(filename: String?): String {
        if (filename != null && filename.length > 0) {
            val dot = filename.lastIndexOf('.')
            if (dot > -1 && dot < filename.length - 1) {
                return filename.substring(dot + 1)
            }
        }
        return ""
    }

    /**
     * 将图片文件加入到相册
     *
     * @param context
     * @param dstPath
     */
    fun ablumUpdate(context: Context?, dstPath: String) {
        if (TextUtils.isEmpty(dstPath) || context == null)
            return

        val file = File(dstPath)
        //System.out.println("panyi  file.length() = "+file.length());
        if (!file.exists() || file.length() == 0L) {//文件若不存在  则不操作
            return
        }

        val values = ContentValues(2)
        val extensionName = getExtensionName(dstPath)
        values.put(
            MediaStore.Images.Media.MIME_TYPE,
            "image/" + if (TextUtils.isEmpty(extensionName)) "jpeg" else extensionName
        )
        values.put(MediaStore.Images.Media.DATA, dstPath)
        context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    }

    fun sendFiles(context: Context, savePath: String) {
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        // 获得拓展名
        val type = getFormatNameOfFile(savePath)
        val file = File(savePath)
        val contentUri =
            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        intent.putExtra(Intent.EXTRA_STREAM, contentUri)
        intent.type = type
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            QMUITipDialog.Builder(context).setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                .setTipWord(context.getString(R.string.no_such_format)).create().show()
        }

    }

    fun processFile(context: Context, savePath: String) {
        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.action = Intent.ACTION_VIEW
        // 获得拓展名
        val type = getFormatNameOfFile(savePath)
        val file = File(savePath)
        val contentUri =
            FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        intent.setDataAndType(contentUri, type)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            QMUITipDialog.Builder(context).setIconType(QMUITipDialog.Builder.ICON_TYPE_INFO)
                .setTipWord("没有打开此格式的应用").create().show()
        }

    }

    fun getFormatNameOfFile(filePath: String): String {
        var type = "*/*"
        val tmp = filePath.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (tmp.size != 1) {
            for (pair in MIME_MapTable) {
                if (pair[0] == tmp[tmp.size - 1]) {
                    type = pair[1]
                    break
                }
            }
        }
        return type
    }

    val MIME_MapTable = arrayOf(
        //{后缀名，MIME类型}
        arrayOf("3gp", "video/3gpp"),
        arrayOf("apk", "application/vnd.android.package-archive"),
        arrayOf("asf", "video/x-ms-asf"),
        arrayOf("avi", "video/x-msvideo"),
        arrayOf("bin", "application/octet-stream"),
        arrayOf("bmp", "image/bmp"),
        arrayOf("c", "text/plain"),
        arrayOf("class", "application/octet-stream"),
        arrayOf("conf", "text/plain"),
        arrayOf("cpp", "text/plain"),
        arrayOf("doc", "application/msword"),
        arrayOf("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        arrayOf("xls", "application/vnd.ms-excel"),
        arrayOf("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        arrayOf("exe", "application/octet-stream"),
        arrayOf("gif", "image/gif"),
        arrayOf("gtar", "application/x-gtar"),
        arrayOf("gz", "application/x-gzip"),
        arrayOf("h", "text/plain"),
        arrayOf("htm", "text/html"),
        arrayOf("html", "text/html"),
        arrayOf("jar", "application/java-archive"),
        arrayOf("java", "text/plain"),
        arrayOf("jpeg", "image/jpeg"),
        arrayOf("jpg", "image/jpeg"),
        arrayOf("js", "application/x-javascript"),
        arrayOf("log", "text/plain"),
        arrayOf("m3u", "audio/x-mpegurl"),
        arrayOf("m4a", "audio/mp4a-latm"),
        arrayOf("m4b", "audio/mp4a-latm"),
        arrayOf("m4p", "audio/mp4a-latm"),
        arrayOf("m4u", "video/vnd.mpegurl"),
        arrayOf("m4v", "video/x-m4v"),
        arrayOf("mov", "video/quicktime"),
        arrayOf("mp2", "audio/x-mpeg"),
        arrayOf("mp3", "audio/x-mpeg"),
        arrayOf("mp4", "video/mp4"),
        arrayOf("mpc", "application/vnd.mpohun.certificate"),
        arrayOf("mpe", "video/mpeg"),
        arrayOf("mpeg", "video/mpeg"),
        arrayOf("mpg", "video/mpeg"),
        arrayOf("mpg4", "video/mp4"),
        arrayOf("mpga", "audio/mpeg"),
        arrayOf("msg", "application/vnd.ms-outlook"),
        arrayOf("ogg", "audio/ogg"),
        arrayOf("pdf", "application/pdf"),
        arrayOf("png", "image/png"),
        arrayOf("pps", "application/vnd.ms-powerpoint"),
        arrayOf("ppt", "application/vnd.ms-powerpoint"),
        arrayOf(
            "pptx",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation"
        ),
        arrayOf("prop", "text/plain"),
        arrayOf("rc", "text/plain"),
        arrayOf("rmvb", "audio/x-pn-realaudio"),
        arrayOf("rtf", "application/rtf"),
        arrayOf("sh", "text/plain"),
        arrayOf("tar", "application/x-tar"),
        arrayOf("tgz", "application/x-compressed"),
        arrayOf("txt", "text/plain"),
        arrayOf("wav", "audio/x-wav"),
        arrayOf("wma", "audio/x-ms-wma"),
        arrayOf("wmv", "audio/x-ms-wmv"),
        arrayOf("wps", "application/vnd.ms-works"),
        arrayOf("xml", "text/plain"),
        arrayOf("z", "application/x-compress"),
        arrayOf("zip", "application/x-zip-compressed"),
        arrayOf("", "*/*")
    )

}