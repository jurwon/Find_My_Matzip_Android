package com.example.find_my_matzip

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.find_my_matzip.databinding.ActivityJoinBinding
import com.example.find_my_matzip.model.UsersFormDto
import com.example.find_my_matzip.utils.LoadingDialog
import com.example.find_my_matzip.utils.PermissionManager
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

class JoinActivity : AppCompatActivity() {
    lateinit var binding: ActivityJoinBinding
    private val TAG: String = "JoinActivity"

    //editview 밖의 공간 클릭시 키보드 내리기 기능 구현
    //설정 1)
    private lateinit var imm: InputMethodManager

    // 갤러리에서 선택된 , 파일의 위치(로컬)
    private var filePath : String? = null

    // 카메라 이미지 파일 위치
    lateinit var profileImageUri : String
    //파이어베이스 사진 저장 경로
    lateinit var imgStorageUrl:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJoinBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //로딩 다이얼로그
        val loadingDialog = LoadingDialog(this)

        //설정 2)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        //갤러리 접근권한 질문
        PermissionManager.checkPermission(this@JoinActivity)

        //갤러리에서 사진 선택 후
        //가져와서 처리하는 후처리 함수
        val requestLauncher = registerForActivityResult(
            // 갤러리에서, 사진을 선택해서 가져왔을 때, 수행할 함수.
            ActivityResultContracts.StartActivityForResult()
        ) {
            // it 이라는 곳에 사진 이미지가 있음.
            if(it.resultCode === android.app.Activity.RESULT_OK) {
                // 이미지 불러오는 라이브러리 glide 사용하기, 코루틴이 적용이되어서, 매우 빠름.
                // with(this) this 현재 액티비티 가리킴. 대신해서.
                // 1) applicationContext
                // 2) getApplicationContext()
                Glide
                    .with(getApplicationContext())
                    // 사진을 읽기.
                    .load(it.data?.data)
                    .apply(RequestOptions().override(900, 900))
                    // 선택된 사진 크기 자동 조정
                    .centerCrop()
                    // 결과 뷰에 사진 넣기.
                    .into(binding.resultUserImage)

                // filePath, 갤러리에서 불러온 이미지 파일 정보 가져오기.
                // 커서에 이미지 파일이름이 등록이 되어 있음.
                val cursor = contentResolver.query(it.data?.data as Uri,
                    arrayOf<String>(MediaStore.Images.Media.DATA),null,
                    null,null);

                cursor?.moveToFirst().let {
                    filePath = cursor?.getString(0) as String
                }
                Log.d(TAG,"갤러리 filePath : ${filePath}")


            } // 조건문 닫는 블록
        }

        //1) 버튼 이용해서, 갤러리 (2번째 앱) 호출해서, 사진 선택 후 작업하기.
        binding.galleryBtn.setOnClickListener {
            // 샘플코드 처럼, 갤러리 호출, 호출된 이미지 선택 부분,
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*"
            )
            requestLauncher.launch(intent)
        }

        //카메라 호출해서, 사진 촬영된 사진 가져오기.
        // 2) 후처리하는 함수를 이용해서, 촬영된 사진을 결과 뷰에 출력하는 로직.
        val requestCameraFileLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == Activity.RESULT_OK) {

                profileImageUri = Uri.fromFile(File(filePath)).toString()

                Log.d(TAG,"profileImageUri : $profileImageUri")

                Glide
                    .with(applicationContext)
                    .load(profileImageUri)
                    .apply(RequestOptions().override(250,200))
                    .centerCrop()
                    .into(binding.resultUserImage)


            } else if (it.resultCode == Activity.RESULT_CANCELED) {
                // The user canceled the camera capture
                Log.d(TAG, "카메라 기능 취소됨")
            } else {
                // Handle other cases if needed
                Log.d(TAG, "카메라 기능 로딩 실패: ${it.resultCode}")
            }


        }

        // 1) 카메라 호출하는 버튼 , 액션 문자열로 카메라 외부앱 연동.
        binding.cameraBtn.setOnClickListener {
            // 사진이 촬영이되고, 저장이될 때, 파일이름을 정하기.
            // 중복이 안되게끔 이름을 작성, UUID를 많이 쓰는데,
            // 일단, 날짜를 기준으로 사진의 파일명을 구분 짓기.

            //파일 이름 준비하기.
            val timeStamp : String =
                SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

            Log.d(TAG,"timeStamp : $timeStamp")

            // 촬영된 사진의 저장소 위치 정하기.
            // Environment.DIRECTORY_PICTURES : 정해진 위치, 갤러리
            val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            // 위에서 만든 파일이름과, 저장소위치에 실제 물리 파일 생성하기.
            // 빈 파일.
            val file = File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
            )

            // 실제 사진 파일 저장소 위치 정의 , 절대 경로
            // 전역으로 빼기.
            // 위에서 선언만하고, 실제 파일위치가 나올 이 때, 할당을 하는 구조.
            filePath = file.absolutePath

            Log.d(TAG,"file.absolutePath : $filePath")

            //콘텐츠 프로바이더를 이용해서, 데이터를 가져와야 함.
            // provider에서 정한 authorities 값이 필요함.
            // 매니페스트 파일에 가서,
            var photoURI:Uri = FileProvider.getUriForFile(
                this@JoinActivity,
                "com.example.find_my_matzip",
                file
            )
            // 카메라를 촬영하는 정해진 액션 문자열
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            // 인텐트 데이터를 담아서 전달.
            // 키: MediaStore.EXTRA_OUTPUT , 값 : photoURI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            // 후처리 함수로 촬영된 사진을 처리하는 로직.
            requestCameraFileLauncher.launch(intent)

        }



        //회원가입 버튼
        binding.buttonInsert.setOnClickListener {

            //먼저, 필수 입력요소들 검증
            val inputUserId = binding.userId.text.toString()
            val inputUserPw = binding.userPwd.text.toString()
            val inputUserName = binding.userName.text.toString()

            Log.d(TAG, "회원가입 inputUserId: ${inputUserId}")
            Log.d(TAG, "회원가입 inputUserPw: ${inputUserPw}")
            Log.d(TAG, "회원가입 inputUserName: ${inputUserName}")

            if(inputUserId == ""){
                Toast.makeText(this@JoinActivity,"아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "회원가입 inputUserId: null")
                return@setOnClickListener
            }
            if(inputUserPw == ""){
                Toast.makeText(this@JoinActivity,"비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "회원가입 inputUserPw: null")
                return@setOnClickListener
            }
            if(inputUserName == ""){
                Toast.makeText(this@JoinActivity,"이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "회원가입 inputUserName: null")
                return@setOnClickListener
            }

            //로딩창 띄우기
            loadingDialog.show()

            // 스토리지 접근 도구 ,인스턴스
            val storage = MyApplication.storage
            // 스토리지에 저장할 인스턴스
            val storageRef = storage.reference

            // 파일명 생성 : userid+현재시간
           // val uuid = binding.userId.text.toString()+Date()+System.currentTimeMillis();


            // 이미지 저장될 위치 및 파일명(파이어베이스)
            val imgRef = storageRef.child("users_img/${binding.userId.text}.jpg")

            
            //입력된 이미지 있을때만 db에 이미지 경로 저장
            if(filePath != null){
                //이미지 url
                imgStorageUrl = "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/users_img%2F${binding.userId.text}.jpg?alt=media"
            }else{
                imgStorageUrl = ""
            }
            

            //회원가입 요청할 user객체 구성
            var usersFormDto = UsersFormDto(
                userid = inputUserId,
                user_pwd = inputUserPw,
                username = inputUserName,
                user_address = binding.searchAddress.text.toString() + binding.userAddressDetail.text,
                user_role = "ADMIN",
                userphone = binding.userPhone.text.toString(),
                user_image = imgStorageUrl,
                gender = getValue(binding.testRadioGroup),
            )


            val userService = (applicationContext as MyApplication).userService
            val newUsers = userService.newUsers(usersFormDto)

            newUsers.enqueue(object: Callback<Unit> {
                override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

                    Log.d(TAG, "Request URL: ${call.request().url()}")
                    Log.d(TAG, "Request Body: ${call.request().body()}")
                    Log.d(TAG, "Response Code: ${response.code()}")
                    if(response.isSuccessful) {

                        Log.d(TAG, "성공(Users) :  ${usersFormDto}")
                        Log.d(TAG, "성공(newUsers_body) :  ${response.body().toString()}")


                        //입력된 사진 있을때만 파이어베이스에 사진 저장
                        if(filePath != null){
                            //저장할 파일 불러오기
                            val file = Uri.fromFile(File(filePath))

                            // 파이어베이스 스토리지에 업로드하는 함수.
                            imgRef.putFile(file)
                                // 업로드 후, 수행할 콜백 함수 정의. 실패했을 경우 콜백함수 정의
                                .addOnCompleteListener{
                                    //로딩창 지우기
                                    loadingDialog.dismiss()
                                    Log.d(TAG, "스토리지 업로드 완료")
                                   
                                }
                                .addOnFailureListener {
                                    //로딩창 지우기
                                    loadingDialog.dismiss()

                                    //db의 유저정보 지우기

                                    Log.d(TAG, "스토리지 업로드 실패")
                                }
                        }

                        Toast.makeText(this@JoinActivity,"회원가입 되었습니다.",Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this@JoinActivity, LoginActivity::class.java)
                        startActivity(intent)

                    }else {
                        Log.d(TAG, "서버 응답 실패: ${response.code()}")
                        //로딩창 지우기
                        loadingDialog.dismiss()
                        try {
                            val errorBody = response.errorBody()?.string()

                            val jsonError = JSONObject(errorBody)
                            val errorMessage = jsonError.optString("message", "Unknown Error")

                            Log.d(TAG, "Error Body: $errorBody")
                            Log.d(TAG, "Error errorMessage: ${errorMessage}")

                            if(errorMessage.equals("이미 가입된 회원입니다.")){
                                Toast.makeText(this@JoinActivity,"중복된 아이디입니다.", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Log.d(TAG, "실패 ${t.message}")
                    //로딩창 지우기
                    loadingDialog.dismiss()
                    call.cancel()
                }


            })
        }//회원가입

        //주소 검색후 결과값 받아오는 후처리 함수
        val getSearchResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                if (result.data != null) {
                    val data = result.data!!.getStringExtra("data")
                    binding.searchAddress.setText(data)
                }
            }
        }


        //주소 검색
        binding.searchAddress.setOnClickListener{
            //주소 검색 화면으로 이동
            val intent = Intent(this@JoinActivity, SearchAddressActivity::class.java)
            getSearchResult.launch(intent)
        }

        //설정 3)
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Hide the keyboard when the user clicks outside of the EditText
                val view = this.currentFocus
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
            false
        }


    }


    //성별
    fun getValue(v: View?): String? {
        val male = binding.radio1
        val female = binding.radio2
        var pickValue: String? = null
        if (male.isChecked) {
            pickValue = male.text.toString()
        } else if (female.isChecked) {
            pickValue = female.text.toString()
        }
        return pickValue
    }


}