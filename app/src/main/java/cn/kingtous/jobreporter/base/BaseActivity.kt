package cn.kingtous.jobreporter.base

import android.os.Bundle
import android.view.Gravity
import cn.droidlover.xdroidmvp.mvp.IPresent
import cn.droidlover.xdroidmvp.mvp.XActivity
import cn.kingtous.jobreporter.R
import com.qmuiteam.qmui.util.QMUIStatusBarHelper
import com.qmuiteam.qmui.widget.QMUITopBarLayout

/**
 * Author: Kingtous
 * Since: 2019-11-10
 * Email: me@kingtous.cn
 */

abstract class BaseActivity<P: IPresent<*>?> : XActivity<P>() {

    override fun initData(savedInstanceState: Bundle?) {

    }

    protected fun initTopBar(title:String){
        val topBar = findViewById<QMUITopBarLayout>(R.id.topbar)
        topBar?.addLeftBackImageButton()?.setOnClickListener { finish() }
        topBar?.setTitle(title)?.setTextColor(resources.getColor(R.color.qmui_config_color_white))
        topBar?.setTitleGravity(Gravity.CENTER)
        QMUIStatusBarHelper.translucent(this)
    }

}