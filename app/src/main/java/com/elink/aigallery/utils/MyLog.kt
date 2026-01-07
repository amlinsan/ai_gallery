package com.elink.aigallery.utils

import android.util.Log

object MyLog {
    private const val BASE_TAG = "elink_aig"

    fun d(tag: String, message: String) {
        Log.d(BASE_TAG, format(tag, message))
    }

    fun d(tag: String, message: String, throwable: Throwable) {
        Log.d(BASE_TAG, format(tag, message), throwable)
    }

    fun i(tag: String, message: String) {
        Log.i(BASE_TAG, format(tag, message))
    }

    fun i(tag: String, message: String, throwable: Throwable) {
        Log.i(BASE_TAG, format(tag, message), throwable)
    }

    fun w(tag: String, message: String) {
        Log.w(BASE_TAG, format(tag, message))
    }

    fun w(tag: String, message: String, throwable: Throwable) {
        Log.w(BASE_TAG, format(tag, message), throwable)
    }

    fun e(tag: String, message: String) {
        Log.e(BASE_TAG, format(tag, message))
    }

    fun e(tag: String, message: String, throwable: Throwable) {
        Log.e(BASE_TAG, format(tag, message), throwable)
    }

    private fun format(tag: String, message: String): String {
        return "[$tag] $message"
    }
}
