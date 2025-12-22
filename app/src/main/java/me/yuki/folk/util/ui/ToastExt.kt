package me.yuki.folk.util.ui

import android.content.Context
import android.widget.Toast

/**
 * 显示Toast消息的扩展函数
 */
fun showToast(context: Context, message: String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}