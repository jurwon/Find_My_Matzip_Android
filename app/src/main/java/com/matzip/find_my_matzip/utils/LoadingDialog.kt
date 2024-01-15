package com.matzip.find_my_matzip.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.databinding.DataBindingUtil.setContentView
import com.matzip.find_my_matzip.R

//커스텀 로딩 dialog
class LoadingDialog
    constructor(context: Context) : Dialog(context){

        init {
            //외부 화면을 터치할 때 다이얼로그 종료되지 않게 설정
            setCanceledOnTouchOutside(false)
            //배경 투명하게 설정
            window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            setContentView(R.layout.dialog_progress)
        }
}