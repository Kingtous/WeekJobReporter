package cn.kingtous.jobreporter.business.main.data

import java.io.Serializable

/**
 * Author: Kingtous
 * Since: 2019-11-10
 * Email: me@kingtous.cn
 */

class Report : Serializable{

    var former:String = ""
    var complete:String = ""
    var later:String = ""

    constructor(former: String, complete: String, later: String) {
        this.former = former
        this.complete = complete
        this.later = later
    }
}
