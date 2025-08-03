package com.fabiosf34.secretvideorecorder.model.repository

import androidx.room.*
import com.fabiosf34.secretvideorecorder.model.Video
import com.fabiosf34.secretvideorecorder.model.utilities.DBConstants


@Dao
interface VideoDAO {
    @Insert
    fun insert(video: Video): Long

    @Update
    fun update(video: Video): Int

    @Delete
    fun delete(video: Video)

    @Query("SELECT * FROM ${DBConstants.Video.TABLE_NAME} WHERE ${DBConstants.Video.COLUMN_ID} = :id")
    fun get(id: Int): Video

    @Query("SELECT * FROM ${DBConstants.Video.TABLE_NAME} WHERE ${DBConstants.Video.COLUMN_URI} = :uri")
    fun getVideoIdFromUri(uri: String): Int

    @Query("SELECT * FROM ${DBConstants.Video.TABLE_NAME}")
    fun getAll(): List<Video>

}