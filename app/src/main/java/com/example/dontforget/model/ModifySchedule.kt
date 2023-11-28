package com.example.dontforget.model

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import com.example.dontforget.R
import com.example.dontforget.databinding.ActivityDeleteOrModifyPopUpBinding
import com.example.dontforget.databinding.ActivityMainBinding

class ModifySchedule : AppCompatActivity() {
    val binding by lazy{ ActivityDeleteOrModifyPopUpBinding.inflate(layoutInflater)}
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)


//        binding.deleteSchedule.setOnClickListener{
//
//        }
//        binding.modifySchedule.setOnClickListener{
//
//        }
    }

}