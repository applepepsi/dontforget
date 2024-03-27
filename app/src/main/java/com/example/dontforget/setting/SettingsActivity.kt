package com.example.dontforget.setting

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.dontforget.databinding.ActivitySettingsBinding
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
        binding.countAllSchedule.setText(TOTAL_MEMO_COUNT+countSchedule)
    }

    private fun getScheduleCount(context: Context): Int {
        val sharedPreferences = context.getSharedPreferences("MemoCount", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("count", 0)
    }

    private fun appInfo(){
        binding.howToUse.setOnClickListener{
            val builder = AlertDialog.Builder(this@SettingsActivity)
            builder.setTitle("Don't Forget 사용법")
                .setMessage(APP_INFO)
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


    companion object {
        const val APP_INFO = "\n할 일을 잊지 않도록 도와주는 Don't Forget 입니다.\n\n" +
                "1. 메모 작성법: 오른쪽 상단의 작성 아이콘을 누르면 메모 작성 페이지로 이동합니다.\n\n" +
                "2. 메모 수정법: 작성된 메모를 클릭한 후 수정하기 버튼을 눌러 수정 페이지로 이동합니다.\n\n" +
                "3. 메모 삭제법: 작성된 메모를 우측이나 좌측으로 밀면 삭제할 수 있습니다.\n\n" +
                "4. 알림: 메모를 작성할 때 D - Day 를 설정하고 알림 스위치를 On으로 설정하면" +
                " 매일 오전 8시에 남은 일 수를 알림으로 전송합니다.\n\n" +
                "5. 검색: 작성한 메모를 검색하기 위해선 메인 화면의 우측 상단의 돋보기 아이콘을 터치하면 검색창이 출력되고" +
                "찾고싶은 메모의 내용이나 제목을 작성하면 해당되는 메모만 남게됩니다.\n\n"

        var TOTAL_MEMO_COUNT="지금까지 작성된 메모의 수 : "

    }
}