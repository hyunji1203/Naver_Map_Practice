package com.example.naver_map_practice

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class DialogBuilder(private val context: Context) {
    fun createDialog() {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("현재 위치를 확인하시려면 설정에서 위치 권한을 허용해주세요.")
        builder.setPositiveButton("설정으로 이동") { dialog, which ->
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:${context.packageName}"),
            )
            context.startActivity(intent)
        }
        builder.setNegativeButton("취소") { _, _ -> }
        builder.show()
    }
}