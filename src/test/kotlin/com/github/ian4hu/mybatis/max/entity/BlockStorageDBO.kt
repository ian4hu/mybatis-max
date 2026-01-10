package com.github.ian4hu.mybatis.max.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.util.Date

@TableName("block_storage")
class BlockStorageDBO {

    @TableId(type = IdType.AUTO)
    var id : Long? = null
    var gmtCreate: Date? = null

    var gmtModified: Date? = null
    var outBizId : String? = null
    var type : String? = null
    var mediaType : String? = null
    var sha256 : String? = null
    var metadata: String? = null
    var buffSize : Long? = null
    var buffer : ByteArray? = null
}