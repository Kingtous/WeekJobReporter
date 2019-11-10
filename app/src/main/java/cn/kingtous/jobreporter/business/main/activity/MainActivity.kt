package cn.kingtous.jobreporter.business.main.activity

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.Toast
import cn.kingtous.jobreporter.R
import cn.kingtous.jobreporter.base.BaseActivity
import cn.kingtous.jobreporter.business.main.presenter.MainPresenter
import cn.kingtous.jobreporter.tool.FileUtil
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import com.qmuiteam.qmui.widget.roundwidget.QMUIRoundButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_topbar.*
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : BaseActivity<MainPresenter>(),View.OnClickListener {

    private val btn_id_generate = 1000
    val WRITE_REQUEST_CODE = 1000

    override fun onClick(v: View?) {
        (v?.id == btn_id_generate).let {
            val builder = QMUIDialog.EditTextDialogBuilder(this)
            builder.setPlaceholder("请输入文件名(不含拓展名)")
            var txt = builder.editText
            builder.addAction("提交"){ qmuiDialog: QMUIDialog, i: Int ->
                if (txt.text.toString().equals("")){
                    Toast.makeText(this,"文件名不能为空",Toast.LENGTH_SHORT).show()
                }
//                else if(FileUtil.checkFileExist(txt.text.toString())){
//                    Toast.makeText(this,"文件名已存在",Toast.LENGTH_SHORT).show()
//                }
                else{
                    val params : Array<String> = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    if (!EasyPermissions.hasPermissions(this,*params)){
                        EasyPermissions.requestPermissions(this,"存取表格需要权限",WRITE_REQUEST_CODE,*params)
                    }
                    else{
                        qmuiDialog.dismiss()
                        p.storeExcel(txt.text.toString(),edit_name.text.toString(),edit_former.text.toString(),edit_complete.text.toString(),edit_later.text.toString())
                    }
                }
            }
            builder.addAction("取消"){
                    dialog: QMUIDialog?, index: Int ->
                dialog?.dismiss()
            }
            val dialog = builder.create()
            txt = builder.editText
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun newP(): MainPresenter {
        return MainPresenter()
    }

    override fun initData(savedInstanceState: Bundle?) {
        initTopBar(getString(R.string.txt_main_title))
        val btn = topbar.addRightTextButton(getString(R.string.txt_submit),btn_id_generate)
        btn.setTextColor(resources.getColor(R.color.qmui_config_color_white))
        btn.setOnClickListener(this)
    }

}
