package me.bmax.apatch.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.UUID

@Parcelize
data class ScriptInfo(
    val id: String = UUID.randomUUID().toString(),
    val path: String,
    val alias: String,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable
