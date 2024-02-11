package com.matzip.find_my_matzip.navTab.navTabFragment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.matzip.find_my_matzip.MyApplication
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.SearchAddressActivity
import com.matzip.find_my_matzip.databinding.FragmentProfileUpdateBinding
import com.matzip.find_my_matzip.model.UsersFormDto
import com.matzip.find_my_matzip.utils.SharedPreferencesManager
import com.matzip.find_my_matzip.utils.LoadingDialog
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID


class ProfileUpdateFragment : Fragment() {
    lateinit var binding: FragmentProfileUpdateBinding
    lateinit var originUserImg:String

    // 파이어베이스 사진 저장 경로
    lateinit var imgStorageUrl: String
    private var uuid = UUID.randomUUID().toString()


    // 갤러리에서 선택된 , 파일의 위치(로컬)
    private var filePath : String? = null

    // 카메라 이미지 파일 위치
    lateinit var profileImageUri : String
    
    //비밀번호 확인 여부
    private var pwCheck = false

    //editview 밖의 공간 클릭시 키보드 내리기 기능 구현
    //설정 1)
    private lateinit var imm: InputMethodManager
    private val TAG: String = "ProfileUpdateFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SdoLifeCycle","ProfileUpdateFragment onCreate")
        super.onCreate(savedInstanceState)
        binding = FragmentProfileUpdateBinding.inflate(layoutInflater)
        //설정 2)
        imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","ProfileUpdateFragment onCreateView")
        binding = FragmentProfileUpdateBinding.inflate(layoutInflater, container, false)

        //로딩 다이얼로그
        val loadingDialog = LoadingDialog(requireContext())

        //로그인 정보
        val userId = SharedPreferencesManager.getString("id","")
        val userPwd = SharedPreferencesManager.getString("pw","") //pw는 서버에서 못가져와서 가지고 있는 정보 사용

        val userService = (context?.applicationContext as MyApplication).userService
        val userProfile = userService.findbyId(userId)

        Log.d(TAG, "userProfile.enqueue 호출전")

        userProfile.enqueue(object : Callback<UsersFormDto> {
            override fun onResponse(call: Call<UsersFormDto>, response: Response<UsersFormDto>) {
                val userFormDto = response.body()
                Log.d(TAG, "API 요청 처리 성공 (response.body()) : ${response.body()}")

                if (userFormDto != null) {
                    Log.d(TAG, "회원정보 가져오기 성공 (userFormDto) : ${userFormDto.toString()}")

                    // 가져온 회원정보 변수에 저장
                    val originUserId = userFormDto.userid
                    val originUsername = userFormDto.username
                    val originUserAddr = userFormDto.user_address
                    //val originUserRole = userFormDto.user_role
                    val originUserPhone = userFormDto.userphone
                    originUserImg = userFormDto.user_image
                    val originUserGender = userFormDto.gender


                    binding.userId.text = Editable.Factory.getInstance().newEditable(originUserId)
                    binding.userName.text = Editable.Factory.getInstance().newEditable(originUsername?: "")
                    binding.userAddress.text = Editable.Factory.getInstance().newEditable(originUserAddr?: "")
                    binding.userPhone.text = Editable.Factory.getInstance().newEditable(originUserPhone?: "")


                    if(originUserGender.equals("남성")){
                        binding.radio1.isChecked = true
                    }else if(originUserGender.equals("여성")){
                        binding.radio2.isChecked = true
                    }


                    if(originUserImg != ""){
                        Glide.with(requireContext())
                            .load(originUserImg)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)// 디스크 캐시 저장 off
                            .skipMemoryCache(true)// 메모리 캐시 저장 off
                            .override(900, 900)
                            .error(R.drawable.profile)
                            .into(binding.myProfileImg)
                    }

                }
            }

            override fun onFailure(call: Call<UsersFormDto>, t: Throwable) {
                t.printStackTrace()
                call.cancel()
                Log.e(TAG, " 통신 실패")
            }
        })


        //비밀번호 확인 버튼 클릭
        binding.pwCheckBtn.setOnClickListener{
            val inputPw = binding.userPwd.text

            Log.e(TAG, " userPwd : $userPwd")
            Log.e(TAG, " inputPw.text : ${inputPw}")

            //pw가 로그인된 사용자 정보와 일치하다면
            if(inputPw.toString()==userPwd){
                pwCheck = true
                binding.userPwd.setBackgroundResource(com.matzip.find_my_matzip.R.drawable.radius_edittext_green)
                Toast.makeText(requireContext(),"비밀번호 확인되었습니다..", Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(requireContext(),"잘못된 비밀번호입니다.", Toast.LENGTH_SHORT).show()
                inputPw.clear()
                binding.userPwd.hint = "비밀번호 입력"
            }
        }

        //업데이트 버튼 클릭
        binding.updateBtn.setOnClickListener {
            //비밀번호가 확인됐다면 회원정보 변경 가능
            if(pwCheck){

                //로딩창 띄우기
                loadingDialog.show()

                // 스토리지 접근 도구 ,인스턴스
                val storage = MyApplication.storage
                // 스토리지에 저장할 인스턴스
                val storageRef = storage.reference


                // 이미지 저장될 위치 및 파일명(파이어베이스)
                val fileName = "${binding.userId.text}-$uuid"
                val imgRef = storageRef.child("users_img/${fileName}.jpg")

                //입력된 이미지 있을때만 db에 이미지 경로 저장
                if (filePath != null) {
                    //이미지가 바뀌었다면
                    imgStorageUrl = "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/users_img%2F${fileName}.jpg?alt=media"
                } else {
                    //바뀌지 않았다면
                    imgStorageUrl = originUserImg
                }


                // 변경된 회원정보 변수에 저장
                val editUsername = binding.userName.text.toString()
                val editUserAddr = binding.userAddress.text.toString() + binding.userAddressDetail.text.toString()
                val editUserPhone = binding.userPhone.text.toString()
                val editUserGender = getValue(binding.testRadioGroup)

                Log.e(TAG, " editUsername : $editUsername")
                Log.e(TAG, " editUserAddr : $editUserAddr")
                Log.e(TAG, " editUserPhone : $editUserPhone")
                Log.e(TAG, " editUserImg : $imgStorageUrl")
                Log.e(TAG, " editUserGender : $editUserGender")

                //수정 요청할 data
                var usersFormDto = UsersFormDto(
                    userid = userId,
                    user_pwd = userPwd,
                    username = editUsername,
                    user_address = editUserAddr,
                    user_role = "USER",
                    userphone = editUserPhone,
                    user_image = imgStorageUrl,
                    gender = editUserGender,
                )

                val call = userService.updateUsers(usersFormDto)

                Log.d(TAG, "userProfile.enqueue 호출전")

                call.enqueue(object : Callback<Unit> {
                    override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                        if (response.isSuccessful) {
                            Log.d(TAG, "API 요청 처리 성공 (response.body()) : ${response.body()}")

                            val resultState = response.body()
                            if (resultState != null) {
                                Log.d(TAG, "회원 수정 성공")

                                //이미지 변동 있었을 경우에만 이미지 저장
                                if(filePath != null){
                                    //firebase에 이미지 저장
                                    val file = Uri.fromFile(File(filePath))

                                    //기존 이미지 이름
                                    val imageName = getImageName(originUserImg)

                                    //파이어베이스 스토리지에서 originUserImg삭제
                                    val deleteImgRef = storageRef.child("users_img/${imageName}")

                                    if (imageName != null) {
                                        //이미지가 존재할 때
                                        Log.d(TAG,"imageName = $imageName")
                                        deleteImgRef.delete()
                                            .addOnSuccessListener {
                                                // 이미지 삭제 성공
                                                Log.d(TAG, "Firebase 이미지 삭제됨: $imageName")
                                            }
                                            .addOnFailureListener { e ->
                                                // 이미지 삭제 실패
                                                Log.e(TAG, "Firebase 이미지 삭제 실패: $imageName", e)
                                            }
                                    }



                                    // 파이어베이스 스토리지에 업로드하는 함수.
                                    imgRef.putFile(file)
                                        // 업로드 후, 수행할 콜백 함수 정의. 실패했을 경우 콜백함수 정의
                                        .addOnCompleteListener{task ->
                                            if (task.isSuccessful) {
                                                Log.e(TAG, " 회원정보 수정 완료")
                                                Toast.makeText(requireContext(),"회원정보 수정 완료",Toast.LENGTH_SHORT).show()
                                                //로딩창 지우기
                                                loadingDialog.dismiss()

                                            }else {
                                                Log.e(TAG, "스토리지 업로드 실패")
                                                //로딩창 지우기
                                                loadingDialog.dismiss()
                                            }
                                        }
                                        .addOnFailureListener { exception ->
                                            Log.e(TAG, "스토리지 업로드 실패 : ${exception.message}")
                                            //로딩창 지우기
                                            loadingDialog.dismiss()
                                        }
                                }else{
                                    //로딩창 지우기
                                    loadingDialog.dismiss()
                                }

                            }

                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        t.printStackTrace()
                        call.cancel()
                        Log.e(TAG, " 통신 실패")
                    }
                })

            }else{
                Toast.makeText(requireContext(),"비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        //취소버튼 -> mypage로 이동
        binding.cancelBtn.setOnClickListener {
            //현재 fragment 아예 지우고, 이전 fragment 띄우기
            requireActivity().supportFragmentManager.popBackStack()
        }


        //설정 3)
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Hide the keyboard when the user clicks outside of the EditText
                val view = requireActivity().currentFocus
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
            false
        }

        //주소 검색후 결과값 받아오는 후처리 함수
        val getSearchResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                if (result.data != null) {
                    val data = result.data!!.getStringExtra("data")
                    binding.userAddress.setText(data)
                    binding.userAddressDetail.visibility =View.VISIBLE
                }
            }
        }

        //주소 검색
        binding.userAddress.setOnClickListener{
            //주소 검색 화면으로 이동
            val intent = Intent(requireContext(), SearchAddressActivity::class.java)
            getSearchResult.launch(intent)
        }


        //갤러리에서 사진 선택 후
        //가져와서 처리하는 후처리 함수
        val requestLauncher = registerForActivityResult(
            // 갤러리에서, 사진을 선택해서 가져왔을 때, 수행할 함수.
            ActivityResultContracts.StartActivityForResult()
        ) {
            // it 이라는 곳에 사진 이미지가 있음.
            if(it.resultCode === android.app.Activity.RESULT_OK) {
                if (it.data?.data != null) {
                // 이미지 불러오는 라이브러리 glide 사용하기, 코루틴이 적용이되어서, 매우 빠름.
                // with(this) this 현재 액티비티 가리킴. 대신해서.
                // 1) applicationContext
                // 2) getApplicationContext()
                Glide
                    .with(requireContext())
                    // 사진을 읽기.
                    .load(it.data?.data)
                    // 크기 지정 , 가로,세로
                    .apply(RequestOptions().override(900, 900))
                    // 선택된 사진 크기 자동 조정
                    .centerCrop()
                    // 결과 뷰에 사진 넣기.
                    .error(R.drawable.profile)
                    .into(binding.myProfileImg)

                // filePath, 갤러리에서 불러온 이미지 파일 정보 가져오기.
                // 커서에 이미지 파일이름이 등록이 되어 있음.
                val cursor = requireContext().contentResolver.query(it.data?.data as Uri,
                    arrayOf<String>(MediaStore.Images.Media.DATA),null,
                    null,null);

                cursor?.moveToFirst().let {
                    filePath = cursor?.getString(0) as String
                }
                Log.d(TAG,"filePath : ${filePath}")
                Toast.makeText(requireContext(),"filePath : ${filePath}", Toast.LENGTH_LONG).show()

                } else {
                    Toast.makeText(requireContext(), "Failed to load image", Toast.LENGTH_SHORT).show()
                }

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
                    .with(this@ProfileUpdateFragment)
                    .load(profileImageUri)
                    .apply(RequestOptions().override(900, 900))
                    .centerCrop()
                    .error(R.drawable.profile)
                    .into(binding.myProfileImg)


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
            val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

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
                requireContext(),
                "com.matzip.find_my_matzip",
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


        return binding.root
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

    //파이어베이스 주소에서 이미지이름만 구하기
    fun getImageName(text:String) : String? {
        val regex = Regex("users_img%2F(.*?\\.jpg)\\?alt=media")
        val matchResult = regex.find(text)
        val fileName = matchResult?.groupValues?.get(1)

        if (fileName != null) {
            println("Extracted FileName: $fileName")
        } else {
            println("No match found.")
        }

        return fileName
    }

    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","ProfileUpdateFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","ProfileUpdateFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","ProfileUpdateFragment onDestroy")
        super.onDestroy()
    }


}
