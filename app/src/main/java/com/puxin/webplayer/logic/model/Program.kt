package com.puxin.webplayer.logic.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Data(
    val id: Int,
    val title: String,
    val director: String,
    val actor: String,
    val introduce: String,
    val elapse_time: Int,
    val country: String,
    val published_at: Int,
    val poster: Poster,
    val episode_count: Int,
    val categories: List<Category>,
    val episodes: List<Episode>,
    val programs: List<Program>,
    val playCount: Int,
    val video_id: String,
    val free: Int
): Parcelable

@Parcelize
data class Program(
    val id: Int,
    val name: String,
    val director: String,
    val actor: String,
    val introduce: String,
    val elapse_time: Int,
    val country: String,
    val deleted_at: String?,
    val created_at: String,
    val updated_at: String,
    val published_at: Int,
    val poster: Poster,
    val keywords: String,
    val pivot: Pivot
): Parcelable

@Parcelize
data class Category(
    val id: Int,
    val name: String,
    val parentId: String?,
    val pivot: Pivot
): Parcelable

@Parcelize
data class Pivot(
    val program_id: Int,
    val category_id: Int
): Parcelable

@Parcelize
data class Episode(
    val id: Int,
    val program_id: Int,
    val num_str: Int,
    val token: String?,
    val play_url: String,
    val deleted_at: String?,
    val created_at: String,
    val updated_at: String,
    val status: Int,
    val video_id: String,
    val stream_length: Int,
    val free: Int
): Parcelable

@Parcelize
data class Poster(
    val vertical: String,
    val horizontal: String
) : Parcelable