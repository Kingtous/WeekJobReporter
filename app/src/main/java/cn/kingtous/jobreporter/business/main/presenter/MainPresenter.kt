package cn.kingtous.jobreporter.business.main.presenter

import android.Manifest
import android.os.Environment
import cn.droidlover.xdroidmvp.mvp.XPresent
import cn.kingtous.jobreporter.business.main.activity.MainActivity
import cn.kingtous.jobreporter.tool.FileUtil
import jxl.Workbook
import jxl.format.Border
import jxl.format.BorderLineStyle
import pub.devrel.easypermissions.EasyPermissions
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import jxl.format.Colour
import jxl.write.*


/**
 * Author: Kingtous
 * Since: 2019-11-10
 * Email: me@kingtous.cn
 */

class MainPresenter : XPresent<MainActivity>() {

    val WRITE_REQUEST_CODE = 1000

    fun storeExcel(fileName:String, n:String, former:String, complete:String, next:String) {
        val params : Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if (!EasyPermissions.hasPermissions(v,*params)){
            EasyPermissions.requestPermissions(v,"存取表格需要权限",WRITE_REQUEST_CODE,*params)
        }
        else {
            val outputpath =
                Environment.getExternalStorageDirectory().toString() + File.separator + "Download" + File.separator + "Excel" + File.separator + fileName+".xls"
            val file: File? = FileUtil.getEmptyFile("$fileName.xls")
            file?.let {
                val w: WritableWorkbook
                val os: OutputStream = FileOutputStream(file)
                w = Workbook.createWorkbook(os)
                val sheet = w.createSheet("每日汇报", 0)
                // 添加表头
                val head: Array<String> = arrayOf("姓名", "本周计划(要求和上周填写计划完全一致)", "本周已完成任务", "下周计划")
                for (i in head.indices) {
                    sheet.addCell(Label(i, 0, head[i], getFormat()))
                }
                // 添加内容
                // Label (列，行，值)
                sheet.addCell(Label(0, 1, n))
                sheet.addCell(Label(1, 1, former))
                sheet.addCell(Label(2, 1, complete))
                sheet.addCell(Label(3, 1, next))
                w.write()
                w.close()
                FileUtil.sendFiles(v, outputpath)
            }
        }
    }

    private fun getFormat(): WritableCellFormat {
        val font = WritableFont(
            WritableFont.TIMES, 10,
            WritableFont.BOLD
        )// 定义字体
        try {
            font.colour = Colour.BLACK// 黑色字体
        } catch (e1: WriteException) {
            e1.printStackTrace()
        }

        val format = WritableCellFormat(font)
        try {
            format.alignment = jxl.format.Alignment.CENTRE// 左右居中
            format.verticalAlignment = jxl.format.VerticalAlignment.CENTRE// 上下居中
            format.setBorder(
                Border.ALL, BorderLineStyle.THIN,
                Colour.BLACK
            )// 黑色边框
        } catch (e: WriteException) {
            e.printStackTrace()
        }

        return format
    }

}