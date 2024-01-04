package com.example.find_my_matzip.utils

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.example.find_my_matzip.utiles.SharedPreferencesManager

//백그라운드에서, 앱 종료 시점 파악하여 onTaskRemoved()호출
class ForcedTerminationService : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)


        val autoLogin = SharedPreferencesManager.getBoolean("autoLogin",false)

        //로그인시 자동 로그인 체크 안했다면
        if(!autoLogin){
            //앱이 강제종료되면 SharedPreference 초기화
            SharedPreferencesManager.clearLoginPreferences()
        }
        stopSelf()		//Service도 종료
    }
}