package com.fabiosf34.secretvideorecorder.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fabiosf34.secretvideorecorder.model.utilities.DBConstants

@Entity(tableName = DBConstants.Video.TABLE_NAME)
open class Video {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = DBConstants.Video.COLUMN_ID)
    var id: Int = 0

    @ColumnInfo(name = DBConstants.Video.COLUMN_TITLE)
    var title: String = ""

    @ColumnInfo(name = DBConstants.Video.COLUMN_URI)
    var uri: String = ""
}