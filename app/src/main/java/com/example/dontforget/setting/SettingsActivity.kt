package com.example.dontforget.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dontforget.R
import com.example.dontforget.databinding.ActivitySettingsBinding
import com.example.dontforget.model.ModifySchedule
import com.example.dontforget.util.UiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.provider.Settings

class SettingsActivity : AppCompatActivity() {
    val binding by lazy{ ActivitySettingsBinding.inflate(layoutInflater)}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


        setupListeners()
    }

    private fun setupListeners(){

        binding.backButton.setOnClickListener{ finish() }
        showCountAllSchedule()
        appInfo()
        moveUserPermission()
    }

    private fun showCountAllSchedule(){
        val countSchedule=getScheduleCount(this)
        binding.countAllSchedule.setText("지금까지 작성된 메모의 수 : ${countSchedule}개")
    }

    private fun getScheduleCount(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("MemoCount", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("count", 0)
    }

    private fun appInfo(){
        binding.howToUse.setOnClickListener{
            val builder = AlertDialog.Builder(this@SettingsActivity)
            builder.setTitle("Don't Forget 사용법")
                .setMessage(UiText().appInfo)
                .setNegativeButton("확인", DialogInterface.OnClickListener { _, _ ->
                })

            builder.show()
        }
    }

    private fun moveUserPermission(){
        binding.notificationPermission.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", this.packageName, null)
            intent.data = uri
            this.startActivity(intent)
        }
    }
}