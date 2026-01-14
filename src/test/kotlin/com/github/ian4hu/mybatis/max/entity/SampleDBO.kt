/*
 *    Copyright 2026 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package com.github.ian4hu.mybatis.max.entity

import com.baomidou.mybatisplus.annotation.IdType
import com.baomidou.mybatisplus.annotation.TableId
import com.baomidou.mybatisplus.annotation.TableName
import java.util.Date

@TableName("sample")
class SampleDBO {
    @TableId(type = IdType.AUTO)
    var id: Long? = null
    var gmtCreate: Date? = null

    var gmtModified: Date? = null
    var outBizId: String? = null
    var type: String? = null
    var mediaType: String? = null
    var sha256: String? = null
    var metadata: String? = null
    var buffSize: Long? = null
    var buffer: ByteArray? = null
}
