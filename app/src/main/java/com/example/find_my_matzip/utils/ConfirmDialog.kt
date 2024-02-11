package com.matzip.find_my_matzip.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.TextView
import com.matzip.find_my_matzip.R

class ConfirmDialog(context: Context): Dialog(context) {
    private lateinit var onClickListener: OnDialogClickListener

    init {
        // 다이얼로그를 둥글게 표현하기 위해 필요 (Required to round corner)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        setContentView(R.layout.dialog_confirm)
        setContent()
    }

    fun setContent(){

        val cancelButton = findViewById<TextView>(R.id.cancel_button)
        val confirmButton = findViewById<TextView>(R.id.confirm_button)


        cancelButton.setOnClickListener {
            onClickListener.onClicked("Cancel Clicked")
            dismiss()
        }

        confirmButton.setOnClickListener {
            onClickListener.onClicked("Confirm Clicked")
            dismiss()
        }
    }

    interface OnDialogClickListener {
        fun onClicked(name: String)
    }

    fun setOnClickListener(listener: OnDialogClickListener) {
        onClickListener = listener
    }
}