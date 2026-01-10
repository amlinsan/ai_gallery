package com.elink.aigallery

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import android.os.SystemClock
import java.io.File
import java.io.IOException

data class TestImage(
    val uri: Uri,
    val displayName: String,
    val relativePath: String,
    val folderName: String,
    val dataPath: String,
    val mediaStoreId: Long
)

object TestMediaStore {
    fun insertImageFromAsset(
        context: Context,
        assetName: String,
        displayName: String,
        relativePath: String
    ): TestImage {
        val resolver = context.contentResolver
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, displayName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, relativePath)
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: throw IOException("Failed to insert media for $displayName")
        resolver.openOutputStream(uri)?.use { output ->
            val assets = InstrumentationRegistry.getInstrumentation().context.assets
            assets.open(assetName).use { input ->
                input.copyTo(output)
            }
        } ?: throw IOException("Failed to open output stream for $uri")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val pendingValues = ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }
            resolver.update(uri, pendingValues, null, null)
        }
        val dataPath = resolveDataPath(context, uri, relativePath, displayName)
        val folderName = relativePath.trimEnd('/').substringAfterLast('/')
        val mediaStoreId = android.content.ContentUris.parseId(uri)
        return TestImage(uri, displayName, relativePath, folderName, dataPath, mediaStoreId)
    }

    fun deleteIfExists(context: Context, uri: Uri) {
        runCatching {
            context.contentResolver.delete(uri, null, null)
        }
    }

    private fun resolveDataPath(
        context: Context,
        uri: Uri,
        relativePath: String,
        displayName: String
    ): String {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                if (index >= 0) {
                    val path = cursor.getString(index)
                    if (!path.isNullOrBlank()) {
                        return path
                    }
                }
            }
        }
        val rootDir = Environment.getExternalStorageDirectory()
        return File(rootDir, relativePath + displayName).absolutePath
    }
}

object TestPermissions {
    fun grantMediaPermissions(device: UiDevice) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val pkg = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        runShellCommand(device, "pm grant $pkg android.permission.READ_MEDIA_IMAGES")
        runShellCommand(device, "pm grant $pkg android.permission.READ_MEDIA_VIDEO")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runShellCommand(device, "pm grant $pkg android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
        }
    }

    fun revokeMediaPermissions(device: UiDevice) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val pkg = InstrumentationRegistry.getInstrumentation().targetContext.packageName
        runShellCommand(device, "pm revoke $pkg android.permission.READ_MEDIA_IMAGES")
        runShellCommand(device, "pm revoke $pkg android.permission.READ_MEDIA_VIDEO")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            runShellCommand(device, "pm revoke $pkg android.permission.READ_MEDIA_VISUAL_USER_SELECTED")
        }
    }

    private fun runShellCommand(device: UiDevice, command: String) {
        val pfd = InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand(command)
        pfd.close()
        device.waitForIdle()
    }
}

object SystemDialogHelper {
    fun clickAllow(device: UiDevice, timeoutMs: Long = 5_000L) {
        val allowAllCandidates = listOf(
            "允许访问所有照片",
            "允许访问所有照片和视频",
            "允许访问所有照片及视频",
            "允许所有照片",
            "允许全部",
            "Allow all",
            "Allow access to all photos",
            "Allow access to all photos and videos"
        )
        if (clickButtonByTextCandidates(device, allowAllCandidates, timeoutMs, emptyList(), allowMissing = true)) {
            return
        }
        clickButtonByTextCandidates(
            device,
            listOf("允许", "Allow"),
            timeoutMs,
            disallowSubstrings = listOf("所选", "selected"),
            allowMissing = false
        )
    }

    fun clickDelete(device: UiDevice, appPackageName: String? = null, timeoutMs: Long = 8_000L) {
        if (appPackageName != null) {
            waitUntilNotPackage(device, appPackageName, timeoutMs / 2)
        }
        val deleteCandidates = listOf(
            "删除",
            "Delete",
            "移至回收站",
            "移到回收站",
            "Move to trash",
            "Remove"
        )
        if (clickButtonByTextCandidates(device, deleteCandidates, timeoutMs, allowMissing = true)) {
            return
        }
        clickRightMostNonCancelButton(device, timeoutMs)
    }

    fun waitForPermissionDialog(device: UiDevice, timeoutMs: Long = 5_000L) {
        val candidates = listOf(
            "允许",
            "Allow",
            "不允许",
            "Don't allow",
            "所选",
            "selected"
        )
        if (!hasAnyTextCandidates(device, candidates, timeoutMs)) {
            throw AssertionError("Permission dialog not found")
        }
    }

    private fun clickButtonByTextCandidates(
        device: UiDevice,
        texts: List<String>,
        timeoutMs: Long,
        disallowSubstrings: List<String> = emptyList(),
        allowMissing: Boolean = false
    ): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            for (text in texts) {
                val exact = device.findObject(By.text(text))
                if (exact != null && exact.isClickable && !matchesDisallowed(exact.text, disallowSubstrings)) {
                    exact.click()
                    device.waitForIdle()
                    return true
                }
                val contains = device.findObject(By.textContains(text))
                if (contains != null && contains.isClickable && !matchesDisallowed(contains.text, disallowSubstrings)) {
                    contains.click()
                    device.waitForIdle()
                    return true
                }
            }
            device.waitForIdle()
            SystemClock.sleep(200)
        }
        if (!allowMissing) {
            throw AssertionError("System dialog button not found for ${texts.joinToString(",")}")
        }
        return false
    }

    private fun matchesDisallowed(text: String?, disallowSubstrings: List<String>): Boolean {
        if (text.isNullOrBlank() || disallowSubstrings.isEmpty()) return false
        return disallowSubstrings.any { disallow -> text.contains(disallow, ignoreCase = true) }
    }

    private fun hasAnyTextCandidates(
        device: UiDevice,
        texts: List<String>,
        timeoutMs: Long
    ): Boolean {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            for (text in texts) {
                if (device.findObject(By.text(text)) != null) return true
                if (device.findObject(By.textContains(text)) != null) return true
            }
            device.waitForIdle()
            SystemClock.sleep(200)
        }
        return false
    }

    private fun clickRightMostNonCancelButton(device: UiDevice, timeoutMs: Long) {
        val cancelTokens = listOf("取消", "Cancel", "不允许", "Don't allow")
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            val buttons = device.findObjects(By.clazz("android.widget.Button"))
                .filter { it.isClickable }
                .filter { button ->
                    val text = button.text ?: ""
                    cancelTokens.none { token -> text.contains(token, ignoreCase = true) }
                }
            if (buttons.isNotEmpty()) {
                val target = buttons.maxByOrNull { it.visibleBounds.right } ?: buttons.first()
                target.click()
                device.waitForIdle()
                return
            }
            device.waitForIdle()
            SystemClock.sleep(200)
        }
        throw AssertionError("System dialog confirm button not found")
    }

    private fun waitUntilNotPackage(device: UiDevice, packageName: String, timeoutMs: Long) {
        val deadline = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < deadline) {
            if (device.currentPackageName != packageName) return
            device.waitForIdle()
            SystemClock.sleep(200)
        }
    }
}

fun clearScanPrefs(context: Context) {
    val prefs = context.getSharedPreferences("media_scan_prefs", Context.MODE_PRIVATE)
    prefs.edit().clear().commit()
}

fun waitForText(device: UiDevice, text: String, timeoutMs: Long = 15_000L) {
    if (!device.wait(Until.hasObject(By.text(text)), timeoutMs)) {
        throw AssertionError("Timed out waiting for text: $text")
    }
}

fun waitForTextGone(device: UiDevice, text: String, timeoutMs: Long = 8_000L) {
    if (!device.wait(Until.gone(By.text(text)), timeoutMs)) {
        throw AssertionError("Timed out waiting for text to disappear: $text")
    }
}

fun waitForAppForeground(device: UiDevice, packageName: String, timeoutMs: Long = 15_000L) {
    if (!device.wait(Until.hasObject(By.pkg(packageName)), timeoutMs)) {
        throw AssertionError("App not in foreground: $packageName")
    }
}

fun ensureAppForeground(device: UiDevice, packageName: String, timeoutMs: Long = 10_000L) {
    val deadline = SystemClock.uptimeMillis() + timeoutMs
    while (SystemClock.uptimeMillis() < deadline) {
        if (device.currentPackageName == packageName) return
        device.pressBack()
        device.waitForIdle()
        SystemClock.sleep(300)
    }
    throw AssertionError("App not in foreground: $packageName")
}
