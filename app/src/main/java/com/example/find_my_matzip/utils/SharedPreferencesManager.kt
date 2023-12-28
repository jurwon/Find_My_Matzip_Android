package com.example.find_my_matzip.utiles

import android.content.Context
import android.content.SharedPreferences

//로그인 SharedPreferences관리
object SharedPreferencesManager{

    private var prefs: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null

    //SharedPreferences 싱글톤 객체 생성 (auto_login)
    fun init(context: Context){
        prefs = context.getSharedPreferences("auto_login", Context.MODE_PRIVATE)
        editor = prefs?.edit()
    }


    //로그인 정보 저장
    //사용 예시 : SharedPreferencesManager.setLoginInfo("id" , "pw");
    fun setLoginInfo(id: String?, pw: String?,token:String?,autoLogin:Boolean) {
        editor?.putString("id", id)
        editor?.putString("pw", pw)
        editor?.putString("token", token)
        editor?.putBoolean("autoLogin", autoLogin)
        editor?.apply()
    }


    //로그인 정보 불러오기
    // 사용 예시
    //  Map<String, String> loginInfo = SharedPreferencesManager.getLoginInfo();
    //  if (!loginInfo.isEmpty()){
    //      String email    = loginInfo.get("email");
    //      String password = loginInfo.get("password");
    //      String token = loginInfo.get("token");
    //      boolean autoLogin = loginInfo.get("autoLogin");
    //  }
    fun getLoginInfo(): Map<String, Any?>? {
        val loginInfo: MutableMap<String, Any?> = HashMap()
        val id = prefs?.getString("id", "")
        val pw = prefs?.getString("pw", "")
        val token = prefs?.getString("token","")
        val autoLogin = prefs?.getBoolean("autoLogin",false)
        loginInfo["id"] = id
        loginInfo["pw"] = pw
        loginInfo["token"] = token
        loginInfo["autoLogin"] = autoLogin
        return loginInfo
    }

    //로그인 정보 삭제 (로그아웃, 회원 탈퇴시)
    //사용 예시 : clearPreferences()
    fun clearPreferences() {
        editor?.clear()
        editor?.apply() //비동기 처리
    }


    //원하는 값 하나만 가져오기
    //사용 예시 : getString("id", "") => "유저아이디"
    fun getString(key:String,defValue:String):String{
        return prefs?.getString(key, defValue).toString()
    }

    //boolean형태의 값(autoLogin)가져올때 사용
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return prefs?.getBoolean(key, defValue) ?: defValue
    }


}

