package com.matzip.find_my_matzip.utils

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionManager {

    companion object{
        fun checkPermission (activity: AppCompatActivity) {
            //후처리 함수, 1번 앱 -> 2번 앱으로 가서,
            // 1)사진 선택, 2) 권한을 획득을 하건, 뭔가 2번 앱에서 가져오기.
            // 2번,돌아 왔을 때 처리하는 함수
            // 1번, 해당 권한을 요청하는 인텐트 요청.
            // 2번, 처리하는 함수 구성.
            val requestPermissionLauncher = activity.registerForActivityResult(
                // 권한 요청 후, 확인시 사용.
                ActivityResultContracts.RequestPermission()
            ) {
                if (it) {
                    //Toast.makeText(activity,"갤러리 접근 권한 승인됨", Toast.LENGTH_SHORT).show()
                } else {
                   // Toast.makeText(activity,"갤러리 접근 권한 승인 안됨", Toast.LENGTH_SHORT).show()
                }
            }

            //갤러리
            // 1번 요청하기.
            // 33버전 이상: READ_MEDIA_IMAGES
            // 33버전 미만: READ_EXTERNAL_STORAGE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                if(ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.READ_MEDIA_IMAGES
                    ) !== PackageManager.PERMISSION_GRANTED) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }

            } else {

                if (ContextCompat.checkSelfPermission(
                        activity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) !== PackageManager.PERMISSION_GRANTED
                ) {
                    requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }

        
        
        }

    }
}