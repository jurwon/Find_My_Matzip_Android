package com.example.find_my_matzip

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.find_my_matzip.databinding.FragmentProfileUpdateBinding
import com.example.find_my_matzip.databinding.FragmentWriteReviewBinding
import com.example.find_my_matzip.model.BoardImgDto
import com.example.find_my_matzip.model.ProfileDto
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.example.find_my_matzip.utils.LoadingDialog
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class WriteReviewFragment : Fragment() {
    lateinit var binding : FragmentWriteReviewBinding

    private val TAG: String = "WriteReviewFragment"

    // 갤러리에서 선택된 , 파일의 위치(로컬)
    lateinit var filePath : String


    // 갤러리에서 선택된 , 파일의 위치(로컬) , 테스트 2
    lateinit var filePathTest : String

//    lateinit var storageRef : FirebaseStorage
//    lateinit var storage: FirebaseStorage

    private var imgCounter = 1
    private var cnt = 1

    lateinit var selectImgBtn1: Button
    lateinit var selectImgBtn2: Button
    lateinit var selectImgBtn3: Button
    lateinit var selectImgBtn4: Button
    lateinit var addImg: Button
    lateinit var deleteImg2: Button
    lateinit var deleteImg3: Button
    lateinit var deleteImg4: Button
    lateinit var reviewImg1: ImageView
    lateinit var reviewImg2: ImageView
    lateinit var reviewImg3: ImageView
    lateinit var reviewImg4: ImageView
    lateinit var imgUploadLayout2: LinearLayout
    lateinit var imgUploadLayout3: LinearLayout
    lateinit var imgUploadLayout4: LinearLayout
    lateinit var boardImgDtoList: MutableList<BoardImgDto>
    lateinit var boardDtoMap : MutableMap<String,Any>
    lateinit var uploadedImg : BoardImgDto

    companion object {
        fun newInstance(resId: String): WriteReviewFragment {
            Log.d("SdoLifeCycle","WriteReviewFragment newInstance")
            val fragment = WriteReviewFragment()
            val args = Bundle()
            args.putString("resId", resId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","WriteReviewFragment onCreateView")
        binding = FragmentWriteReviewBinding.inflate(layoutInflater,container,false)
        // 이전 프래그먼트에서 전달된 resId 가져오기
        val resId = arguments?.getString("resId")

        // 로그인한 사용자의 아이디를 가져와서 해당 사용자의 프로필 정보를 서버에서 조회
        val userId = SharedPreferencesManager.getString("id","")
        val userService = (context?.applicationContext as MyApplication).userService
        val profileList = userService.getProfile(userId,5)

        Log.d("MyPageFragment", "profileList.enqueue 호출전 : ")

        profileList.enqueue(object : Callback<ProfileDto> {
            override fun onResponse(call: Call<ProfileDto>, response: Response<ProfileDto>) {
                Log.d("WriteReviewFragment", "도착 확인=================================================== ")
                val profileDto = response.body()
                Log.d("WriteReviewFragment", "로그인 된 유저 확인 userId : $userId")
                if (profileDto != null) {
                    // 유저정보 확인하기
                    binding.userId.text = userId
                }
                else {
                    Log.e("WriteReviewFragment", "유저 정보를 받아오지 못했습니다.")
                }
            }
            // 통신 실패 시 로그 출력
            override fun onFailure(call: Call<ProfileDto>, t: Throwable) {
                t.printStackTrace()
                call.cancel()
                Log.e("MyPageFragment", " 통신 실패")
            }
        })

        val loadingDialog = LoadingDialog(requireContext())

        boardImgDtoList = mutableListOf<BoardImgDto>()
        boardDtoMap = mutableMapOf<String,Any>()
        uploadedImg = BoardImgDto(1,"abc","abc","abc","Y")

        selectImgBtn1 = binding.selectImgBtn1
        selectImgBtn2 = binding.selectImgBtn2
        selectImgBtn3 = binding.selectImgBtn3
        selectImgBtn4 = binding.selectImgBtn4

        addImg = binding.addImg

        deleteImg2 = binding.deleteImg2
        deleteImg3 = binding.deleteImg3
        deleteImg4 = binding.deleteImg4

        reviewImg1 = binding.reviewImg1
        reviewImg2 = binding.reviewImg2
        reviewImg3 = binding.reviewImg3
        reviewImg4 = binding.reviewImg4

        imgUploadLayout2 = binding.imgUploadLayout2
        imgUploadLayout3 = binding.imgUploadLayout3
        imgUploadLayout4 = binding.imgUploadLayout4


//// 사진 한장 만 업로드시 테스트용
//        val requestLauncher = registerForActivityResult(
//            // 갤러리에서, 사진을 선택해서 가져왔을 때, 수행할 함수.
//            ActivityResultContracts.StartActivityForResult()
//        ) {
//            // it 이라는 곳에 사진 이미지가 있음.
//            if(it.resultCode === android.app.Activity.RESULT_OK) {
//                Glide
//                    .with(requireContext())
//                    // 사진을 읽기.
//                    .load(it.data?.data)
//                    // 크기 지정 , 가로,세로
//                    .apply(RequestOptions().override(250,200))
//                    // 선택된 사진 크기 자동 조정
//                    .centerCrop()
//                    // 결과 뷰에 사진 넣기.
//                    .into(binding.reviewImg1)
//
//                // 업로드 상관없음. 미리보기용. 전체 업로드 로직과 관련 없음.
//                binding.reviewImg1.visibility = View.VISIBLE
//                val cursor = activity?.contentResolver?.query(it.data?.data as Uri,
//                    arrayOf<String>(MediaStore.Images.Media.DATA),null,
//                    null,null);
//
//                cursor?.moveToFirst().let {
//                    filePathTest = cursor?.getString(0) as String
//                }
//                Log.d("WriteReviewFragment","filePathTest : ${filePathTest}")
//
//            } // 조건문 닫는 블록
//        }

        //이미지 선택버튼
        selectImgBtn1.setOnClickListener {
            // 갤러리 열기 인텐트 호출
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startGalleryForImage(1)
            //이거 쌤이 선택해서 하던거...
//            intent.setDataAndType(
//                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*"
//            )
//            requestLauncher.launch(intent)
        }
        selectImgBtn2.setOnClickListener {
            // 갤러리 열기 인텐트 호출
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startGalleryForImage(2)
        }
        selectImgBtn3.setOnClickListener {
            // 갤러리 열기 인텐트 호출
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startGalleryForImage(3)
        }
        selectImgBtn4.setOnClickListener {
            // 갤러리 열기 인텐트 호출
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startGalleryForImage(4)
        }

        //이미지 추가 부분 확인.
        addImg.setOnClickListener {
            if (imgCounter < 4) {
                when (imgCounter) {
                    1 -> imgUploadLayout2.visibility = View.VISIBLE
                    2 -> imgUploadLayout3.visibility = View.VISIBLE
                    3 -> imgUploadLayout4.visibility = View.VISIBLE
                }
                imgCounter++
                Log.d("WriteReviewFragment","imgCounter의 값 : $imgCounter")
            } else {
                Log.d("WriteReviewFragment","이미지는 총4개 까지만 가능")
                Toast.makeText(requireContext(),"이미지는 총4개 까지만 가능합니다", Toast.LENGTH_SHORT).show()
            }
        }

        deleteImg2.setOnClickListener {
            imgUploadLayout2.visibility = View.GONE
            removeImage(reviewImg2)
            imgCounter--
            updateLayoutVisibility()
        }
        deleteImg3.setOnClickListener {
            imgUploadLayout3.visibility = View.GONE
            removeImage(reviewImg3)
            imgCounter--
            updateLayoutVisibility()
        }
        deleteImg4.setOnClickListener {
            imgUploadLayout4.visibility = View.GONE
            removeImage(reviewImg4)
            imgCounter--
            updateLayoutVisibility()
        }

        //업로드 할 이미지가 총 1개일때 하는작업
        fun uploadImageToFirebaseStorage1(imageUri: Uri, fileName: String) {
            Log.d("WriteReviewFragment", " uploadImageToFirebaseStorage 순서2")
            Log.d("imageUri1 의 경로 알아보기. ", "imageUri1 : ${imageUri} 1번이미지 업로드시작")
            Log.d("fileName1 의 경로 알아보기. ", "fileName1 : ${fileName} 1번이미지 업로드시작")

            val storage = MyApplication.storage
            val storageRef = storage.reference
            val imgRef = storageRef.child("board_img_img/${fileName}.jpg")

            Log.d("WriteReviewFragment", " imgRef.putFile(imageUri) 시작")

            imgRef.putFile(imageUri)
                .addOnCompleteListener {
                    Log.d("WriteReviewFragment","이미지 파베 이미지 업로드 성공")
                }
                .addOnFailureListener {
                    // 업로드 실패 시 처리
                    Log.e("WriteReviewFragment", "파이어베이스에 이미지 업로드 실패: ${it.message}")
                    // 실패했을 때의 추가 작업 수행
                }
            Log.d("WriteReviewFragment", " uploadImageToFirebaseStorage 순서 2작업끝")
        }// 작업 함수의 끝

        //업로드 할 이미지가 총 2개일때 하는작업
        fun uploadImageToFirebaseStorage2(imageUri: Uri, fileName: String) {
            Log.d("WriteReviewFragment", " uploadImageToFirebaseStorage 순서2")
            Log.d("imageUri1 의 경로 알아보기. ", "imageUri1 : ${imageUri} 1번이미지 업로드시작")
            Log.d("fileName1 의 경로 알아보기. ", "fileName1 : ${fileName} 1번이미지 업로드시작")

            val storage = MyApplication.storage
            val storageRef = storage.reference
            val imgRef = storageRef.child("board_img_img/${fileName}.jpg")

            Log.d("WriteReviewFragment", " imgRef.putFile(imageUri) 시작")

            imgRef.putFile(imageUri)
                .addOnCompleteListener {
                    Log.d("WriteReviewFragment","파베 이미지 업로드 성공")
                }
                .addOnFailureListener {
                    // 업로드 실패 시 처리
                    Log.e("WriteReviewFragment", "파이어베이스에 이미지 업로드 실패: ${it.message}")
                    // 실패했을 때의 추가 작업 수행
                }
            Log.d("WriteReviewFragment", " uploadImageToFirebaseStorage 순서 2작업끝")
        }// 작업 함수의 끝

        //이미지가 총 3개일때 하는작업
        fun uploadImageToFirebaseStorage3(imageUri: Uri, fileName: String) {
            Log.d("WriteReviewFragment", " uploadImageToFirebaseStorage 순서2")
            Log.d("imageUri1 의 경로 알아보기. ", "imageUri1 : ${imageUri} 1번이미지 업로드시작")
            Log.d("fileName1 의 경로 알아보기. ", "fileName1 : ${fileName} 1번이미지 업로드시작")

            val storage = MyApplication.storage
            val storageRef = storage.reference
            val imgRef = storageRef.child("board_img_img/${fileName}.jpg")

            Log.d("WriteReviewFragment", " imgRef.putFile(imageUri) 시작")

            imgRef.putFile(imageUri)
                .addOnCompleteListener {
                    Log.d("WriteReviewFragment","파베 이미지 업로드 성공")
                }
                .addOnFailureListener {
                    // 업로드 실패 시 처리
                    Log.e("WriteReviewFragment", "파이어베이스에 이미지 업로드 실패: ${it.message}")
                    // 실패했을 때의 추가 작업 수행
                }
            Log.d("WriteReviewFragment", " uploadImageToFirebaseStorage 순서 2작업끝")
        }// 작업 함수의 끝

        //업로드 할 이미지가 총 4개일때 하는작업
        fun uploadImageToFirebaseStorage4(imageUri: Uri, fileName: String) {
            Log.d("WriteReviewFragment", " uploadImageToFirebaseStorage 순서2")
            Log.d("imageUri1 의 경로 알아보기. ", "imageUri1 : ${imageUri} 1번이미지 업로드시작")
            Log.d("fileName1 의 경로 알아보기. ", "fileName1 : ${fileName} 1번이미지 업로드시작")

            val storage = MyApplication.storage
            val storageRef = storage.reference
            val imgRef = storageRef.child("board_img_img/${fileName}.jpg")

            Log.d("WriteReviewFragment", " imgRef.putFile(imageUri) 시작")

            imgRef.putFile(imageUri)
                .addOnCompleteListener {
                   Log.d("WriteReviewFragment","파이어베이스 업로드 성공")
                }
                .addOnFailureListener {
                    // 업로드 실패 시 처리
                    Log.e("WriteReviewFragment", "파이어베이스에 이미지 업로드 실패: ${it.message}")
                    // 실패했을 때의 추가 작업 수행
                }
            Log.d("WriteReviewFragment", " uploadImageToFirebaseStorage 순서 2작업끝")
        }// 작업 함수의 끝



        //이거 안씀
        fun uploadDb(){
            Log.d("WriteReviewFragment", "uploadDb 함수호출.")
            val scoreText = binding.boardScore.text.toString()
            val score = scoreText.toIntOrNull()?:3

//            val boardFormDto = BoardFormDto(
//                userId = userId,
//                boardViewStatus = "VIEW",
//                boardTitle = binding.boardTitle.text.toString(),
//                content = binding.boardContent.text.toString(),
//                // 정수 변화는 검색.
//                score = score,
//                boardImgDtoList
//            )
//
            boardDtoMap["userId"] = userId
            boardDtoMap["boardViewStatus"] = "VIEW"
            boardDtoMap["boardTitle"] = binding.boardTitle.text.toString()
            boardDtoMap["content"] = binding.boardContent.text.toString()
            boardDtoMap["score"] = score
            Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기전 : $boardImgDtoList")
            boardDtoMap["boardImgDtoList"] = boardImgDtoList
//            boardDtoMap["boardImgDtoListString"] = boardImgDtoListString
//            boardDtoMap["boardImgDtoListString"]

            Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기후 : $boardImgDtoList")
            Log.d("WriteReviewFragment","boardDtoMap의 내용 확인 : ${boardDtoMap} ")

            Log.d("WriteReviewFragment", " boardFormDto 담기작업끝 ===========================================")
//            Log.d("WriteReviewFragment", " boardFormDto :$boardFormDto")

//                val resId = "123"

            val boardService = (context?.applicationContext as MyApplication).boardService

            Log.d("WriteReviewFragment", " resId? : $resId")
//            val call = resId?.let { it1 -> boardService.createBoard2(it1, boardFormDto) }
            val call = resId?.let { it1 -> boardService.createBoard3(it1, boardDtoMap) }

            Log.d("WriteReviewFragment", " val call = resId?.let { it1 -> boardService.createBoard2(it1, boardFormDto) } ")
            call?.enqueue(object : Callback<Unit> {
                override fun onResponse(
                    call: Call<Unit>,
                    response: Response<Unit>
                ) {

                    Log.d("WriteReviewFragment", "Request URL: ${call.request().url()}")
                    Log.d("WriteReviewFragment", "Request Body: ${call.request().body()}")
                    Log.d("WriteReviewFragment", "Response Code: ${response.code()}")
                    if (response.isSuccessful) {
                        Log.d("WriteReviewFragment", "성공(Board) :  ${boardDtoMap}")
                        Log.d("WriteReviewFragment", "성공(createBoard_body) :  ${response.body().toString()}")
                        //파이어베이스업로드
//                            uploadImagesToFirebaseStorage(fileName)
                        Toast.makeText(requireContext(),"스토리지/DB 업로드 완료", Toast.LENGTH_SHORT).show()
                        //로딩창 지우기
                        loadingDialog.dismiss()
                    } else {
                        Log.d("WriteReviewFragment", "서버 응답 실패: ${response.code()}")
                        //로딩창 지우기
                        loadingDialog.dismiss()
                        try {
                            val errorBody = response.errorBody()?.string()

                            val jsonError = JSONObject(errorBody)
                            val errorMessage = jsonError.optString("message", "Unknown Error")

                            Log.d("WriteReviewFragment", "Error Body: $errorBody")
                            Log.d("WriteReviewFragment", "Error errorMessage: ${errorMessage}")

                            if (errorMessage.equals("게시글등록실패")) {
                                Toast.makeText(requireContext(), "게시글등록실패.", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
                override fun onFailure(call: Call<Unit>, t: Throwable) {
                    Toast.makeText(requireContext(), "게시글등록실패onFailure.", Toast.LENGTH_SHORT).show()
                    Log.d("WriteReviewFragment", "실패 ${t.message}")

                    //로딩창 지우기
                    loadingDialog.dismiss()
                    call.cancel()
                }
            })
        }

//        fun uploadImagesToFirebaseStorage(fileName: String) : MutableList<BoardImgDto>{
fun uploadImagesToFirebaseStorage(fileName: String) {
            Log.d("WriteReviewFragment", " 이미지들 파이어베이스에 올리기 순서1")

    //업로드 1개인식
            if (reviewImg1.drawable != null && reviewImg2.drawable == null &&reviewImg3.drawable == null &&reviewImg4.drawable == null) {
                val imageUri1: Uri? = (reviewImg1.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-1.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)                }
                val fileName1 = "$fileName-1" // 이미지 파일명
                if (imageUri1 != null) {
                    Log.d("WriteReviewFragment", " 1번이미지 업로드시작")
                    Log.d("imageUri1 의 경로 알아보기. ", "imageUri1 : ${imageUri1} 1번이미지 업로드시작")
                    Log.d("fileName1 의 경로 알아보기. ", "fileName1 : ${fileName1} 1번이미지 업로드시작")
                    uploadImageToFirebaseStorage1(imageUri1, fileName1)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-1.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName1, // 이미지 파일명
                        oriImgName = fileName1, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                }
                Log.d("WriteReviewFragment", " 1번이미지 업로드완료")
                boardDtoMap["boardImgDtoList"] = boardImgDtoList

                Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부  boardDtoMap 성공 후 순서 6 함수 내부 : $boardDtoMap")
                Log.d("WriteReviewFragment", "uploadDb 함수호출.")
                val scoreText = binding.boardScore.text.toString()
                val score = scoreText.toIntOrNull()?:3

                boardDtoMap["userId"] = userId
                boardDtoMap["boardViewStatus"] = "VIEW"
                boardDtoMap["boardTitle"] = binding.boardTitle.text.toString()
                boardDtoMap["content"] = binding.boardContent.text.toString()
                boardDtoMap["score"] = score
                Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기전 : $boardImgDtoList")
                boardDtoMap["boardImgDtoList"] = boardImgDtoList

                Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기후 : $boardImgDtoList")
                Log.d("WriteReviewFragment","boardDtoMap의 내용 확인 : ${boardDtoMap} ")
                Log.d("WriteReviewFragment", " boardFormDto 담기작업끝 ===========================================")

                val boardService = (context?.applicationContext as MyApplication).boardService

                Log.d("WriteReviewFragment", " resId? : $resId")
                val call = resId?.let { it1 -> boardService.createBoard3(it1, boardDtoMap) }

                Log.d("WriteReviewFragment", " val call = resId?.let { it1 -> boardService.createBoard2(it1, boardFormDto) } ")
                call?.enqueue(object : Callback<Unit> {
                    override fun onResponse(
                        call: Call<Unit>,
                        response: Response<Unit>
                    ) {

                        Log.d("WriteReviewFragment", "Request URL: ${call.request().url()}")
                        Log.d("WriteReviewFragment", "Request Body: ${call.request().body()}")
                        Log.d("WriteReviewFragment", "Response Code: ${response.code()}")
                        if (response.isSuccessful) {
                            Log.d("WriteReviewFragment", "성공(Board) :  ${boardDtoMap}")
                            Log.d("WriteReviewFragment", "성공(createBoard_body) :  ${response.body().toString()}")
                            Toast.makeText(requireContext(),"스토리지/DB 업로드 완료", Toast.LENGTH_SHORT).show()
                            //로딩창 지우기
                            loadingDialog.dismiss()
                        } else {
                            Log.d("WriteReviewFragment", "서버 응답 실패: ${response.code()}")
                            //로딩창 지우기
                            loadingDialog.dismiss()
                            try {
                                //로딩창 지우기
                                loadingDialog.dismiss()
                                val errorBody = response.errorBody()?.string()

                                val jsonError = JSONObject(errorBody)
                                val errorMessage = jsonError.optString("message", "Unknown Error")

                                Log.d("WriteReviewFragment", "Error Body: $errorBody")
                                Log.d("WriteReviewFragment", "Error errorMessage: ${errorMessage}")

                                if (errorMessage.equals("게시글등록실패")) {
                                    Toast.makeText(requireContext(), "게시글등록실패.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Toast.makeText(requireContext(), "게시글등록실패onFailure.", Toast.LENGTH_SHORT).show()
                        Log.d("WriteReviewFragment", "실패 ${t.message}")

                        //로딩창 지우기
                        loadingDialog.dismiss()
                        call.cancel()
                    }
                })
            }
    //업로드 2개인식 -> 이미지 그리는 순간에 리스트에 담는걸로 해놓음
            if (reviewImg1.drawable != null && reviewImg2.drawable != null &&reviewImg3.drawable == null &&reviewImg4.drawable == null) {
                val imageUri1: Uri? = (reviewImg1.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-1.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

//                    FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName, file)
                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)
                }
                val fileName1 = "$fileName-1" // 이미지 파일명
                if (imageUri1 != null) {
                    Log.d("WriteReviewFragment", " 2번이미지 업로드시작")
                    // 2장짜리.
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부 boardImgDtoList 순서 3 함수 호출 전 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부  boardDtoMap 순서 4 함수 호출 전 : $boardDtoMap")
                    uploadImageToFirebaseStorage1(imageUri1, fileName1)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-1.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName1, // 이미지 파일명
                        oriImgName = fileName1, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                    Log.d("WriteReviewFragment", "boardImgDtoList  순서 7 함수 호출 후 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "boardDtoMap 순서 8 함수 호출 후 : $boardDtoMap")
                }

                val imageUri2: Uri? = (reviewImg2.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-2.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

//                    FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName, file)
                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)
                }
                val fileName2 = "$fileName-2" // 이미지 파일명
                if (imageUri2 != null) {
                    Log.d("WriteReviewFragment", " 2번이미지 업로드시작")
                    // 2장짜리.
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부 boardImgDtoList 순서 3 함수 호출 전 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부  boardDtoMap 순서 4 함수 호출 전 : $boardDtoMap")
                    uploadImageToFirebaseStorage2(imageUri2, fileName2)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-2.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName2, // 이미지 파일명
                        oriImgName = fileName2, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                    Log.d("WriteReviewFragment", "boardImgDtoList  순서 7 함수 호출 후 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "boardDtoMap 순서 8 함수 호출 후 : $boardDtoMap")
                }
                Log.d("WriteReviewFragment", " 2번이미지 업로드완료")
                boardDtoMap["boardImgDtoList"] = boardImgDtoList

                Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부  boardDtoMap 성공 후 순서 6 함수 내부 : $boardDtoMap")
                Log.d("WriteReviewFragment", "uploadDb 함수호출.")
                val scoreText = binding.boardScore.text.toString()
                val score = scoreText.toIntOrNull()?:3

                boardDtoMap["userId"] = userId
                boardDtoMap["boardViewStatus"] = "VIEW"
                boardDtoMap["boardTitle"] = binding.boardTitle.text.toString()
                boardDtoMap["content"] = binding.boardContent.text.toString()
                boardDtoMap["score"] = score
                Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기전 : $boardImgDtoList")
                boardDtoMap["boardImgDtoList"] = boardImgDtoList

                Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기후 : $boardImgDtoList")
                Log.d("WriteReviewFragment","boardDtoMap의 내용 확인 : ${boardDtoMap} ")
                Log.d("WriteReviewFragment", " boardFormDto 담기작업끝 ===========================================")

                val boardService = (context?.applicationContext as MyApplication).boardService

                Log.d("WriteReviewFragment", " resId? : $resId")
                val call = resId?.let { it1 -> boardService.createBoard3(it1, boardDtoMap) }

                Log.d("WriteReviewFragment", " val call = resId?.let { it1 -> boardService.createBoard2(it1, boardFormDto) } ")
                call?.enqueue(object : Callback<Unit> {
                    override fun onResponse(
                        call: Call<Unit>,
                        response: Response<Unit>
                    ) {

                        Log.d("WriteReviewFragment", "Request URL: ${call.request().url()}")
                        Log.d("WriteReviewFragment", "Request Body: ${call.request().body()}")
                        Log.d("WriteReviewFragment", "Response Code: ${response.code()}")
                        if (response.isSuccessful) {
                            Log.d("WriteReviewFragment", "성공(Board) :  ${boardDtoMap}")
                            Log.d("WriteReviewFragment", "성공(createBoard_body) :  ${response.body().toString()}")
                            Toast.makeText(requireContext(),"스토리지/DB 업로드 완료", Toast.LENGTH_SHORT).show()
                            //로딩창 지우기
                            loadingDialog.dismiss()
                        } else {
                            Log.d("WriteReviewFragment", "서버 응답 실패: ${response.code()}")
                            //로딩창 지우기
                            loadingDialog.dismiss()
                            try {
                                //로딩창 지우기
                                loadingDialog.dismiss()
                                val errorBody = response.errorBody()?.string()

                                val jsonError = JSONObject(errorBody)
                                val errorMessage = jsonError.optString("message", "Unknown Error")

                                Log.d("WriteReviewFragment", "Error Body: $errorBody")
                                Log.d("WriteReviewFragment", "Error errorMessage: ${errorMessage}")

                                if (errorMessage.equals("게시글등록실패")) {
                                    Toast.makeText(requireContext(), "게시글등록실패.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Toast.makeText(requireContext(), "게시글등록실패onFailure.", Toast.LENGTH_SHORT).show()
                        Log.d("WriteReviewFragment", "실패 ${t.message}")

                        //로딩창 지우기
                        loadingDialog.dismiss()
                        call.cancel()
                    }
                })
            }

    //업로드3개인식
            if (reviewImg1.drawable != null && reviewImg2.drawable != null &&reviewImg3.drawable != null &&reviewImg4.drawable == null) {
                val imageUri1: Uri? = (reviewImg1.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-1.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

//                    FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName, file)
                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)
                }
                val fileName1 = "$fileName-1" // 이미지 파일명
                if (imageUri1 != null) {
                    Log.d("WriteReviewFragment", " 2번이미지 업로드시작")
                    // 2장짜리.
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부 boardImgDtoList 순서 3 함수 호출 전 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부  boardDtoMap 순서 4 함수 호출 전 : $boardDtoMap")
                    uploadImageToFirebaseStorage2(imageUri1, fileName1)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-1.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName1, // 이미지 파일명
                        oriImgName = fileName1, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                    Log.d("WriteReviewFragment", "boardImgDtoList  순서 7 함수 호출 후 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "boardDtoMap 순서 8 함수 호출 후 : $boardDtoMap")
                }

                val imageUri2: Uri? = (reviewImg2.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-2.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

//                    FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName, file)
                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)
                }
                val fileName2 = "$fileName-2" // 이미지 파일명
                if (imageUri2 != null) {
                    Log.d("WriteReviewFragment", " 2번이미지 업로드시작")
                    // 2장짜리.
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부 boardImgDtoList 순서 3 함수 호출 전 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부  boardDtoMap 순서 4 함수 호출 전 : $boardDtoMap")
                    uploadImageToFirebaseStorage2(imageUri2, fileName2)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-2.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName2, // 이미지 파일명
                        oriImgName = fileName2, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                    Log.d("WriteReviewFragment", "boardImgDtoList  순서 7 함수 호출 후 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "boardDtoMap 순서 8 함수 호출 후 : $boardDtoMap")
                }

                val imageUri3: Uri? = (reviewImg3.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-3.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)                }
                val fileName3 = "$fileName-3" // 이미지 파일명
                if (imageUri3 != null) {
                    Log.d("WriteReviewFragment", " 3번이미지 업로드시작")
                    uploadImageToFirebaseStorage3(imageUri3, fileName3)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-3.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName3, // 이미지 파일명
                        oriImgName = fileName3, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                }
                Log.d("WriteReviewFragment", " 3번이미지 업로드완료")
                boardDtoMap["boardImgDtoList"] = boardImgDtoList

                Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부  boardDtoMap 성공 후 순서 6 함수 내부 : $boardDtoMap")
                Log.d("WriteReviewFragment", "uploadDb 함수호출.")
                val scoreText = binding.boardScore.text.toString()
                val score = scoreText.toIntOrNull()?:3

                boardDtoMap["userId"] = userId
                boardDtoMap["boardViewStatus"] = "VIEW"
                boardDtoMap["boardTitle"] = binding.boardTitle.text.toString()
                boardDtoMap["content"] = binding.boardContent.text.toString()
                boardDtoMap["score"] = score
                Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기전 : $boardImgDtoList")
                boardDtoMap["boardImgDtoList"] = boardImgDtoList

                Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기후 : $boardImgDtoList")
                Log.d("WriteReviewFragment","boardDtoMap의 내용 확인 : ${boardDtoMap} ")
                Log.d("WriteReviewFragment", " boardFormDto 담기작업끝 ===========================================")

                val boardService = (context?.applicationContext as MyApplication).boardService

                Log.d("WriteReviewFragment", " resId? : $resId")
                val call = resId?.let { it1 -> boardService.createBoard3(it1, boardDtoMap) }

                Log.d("WriteReviewFragment", " val call = resId?.let { it1 -> boardService.createBoard2(it1, boardFormDto) } ")
                call?.enqueue(object : Callback<Unit> {
                    override fun onResponse(
                        call: Call<Unit>,
                        response: Response<Unit>
                    ) {

                        Log.d("WriteReviewFragment", "Request URL: ${call.request().url()}")
                        Log.d("WriteReviewFragment", "Request Body: ${call.request().body()}")
                        Log.d("WriteReviewFragment", "Response Code: ${response.code()}")
                        if (response.isSuccessful) {
                            Log.d("WriteReviewFragment", "성공(Board) :  ${boardDtoMap}")
                            Log.d("WriteReviewFragment", "성공(createBoard_body) :  ${response.body().toString()}")
                            Toast.makeText(requireContext(),"스토리지/DB 업로드 완료", Toast.LENGTH_SHORT).show()
                            //로딩창 지우기
                            loadingDialog.dismiss()
                        } else {
                            Log.d("WriteReviewFragment", "서버 응답 실패: ${response.code()}")
                            //로딩창 지우기
                            loadingDialog.dismiss()
                            try {
                                //로딩창 지우기
                                loadingDialog.dismiss()
                                val errorBody = response.errorBody()?.string()

                                val jsonError = JSONObject(errorBody)
                                val errorMessage = jsonError.optString("message", "Unknown Error")

                                Log.d("WriteReviewFragment", "Error Body: $errorBody")
                                Log.d("WriteReviewFragment", "Error errorMessage: ${errorMessage}")

                                if (errorMessage.equals("게시글등록실패")) {
                                    Toast.makeText(requireContext(), "게시글등록실패.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Toast.makeText(requireContext(), "게시글등록실패onFailure.", Toast.LENGTH_SHORT).show()
                        Log.d("WriteReviewFragment", "실패 ${t.message}")

                        //로딩창 지우기
                        loadingDialog.dismiss()
                        call.cancel()
                    }
                })
            }

    //업로드 4개인식
            if (reviewImg1.drawable != null && reviewImg2.drawable != null && reviewImg3.drawable != null && reviewImg4.drawable != null) {
                val imageUri1: Uri? = (reviewImg1.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-1.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

//                    FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName, file)
                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)
                }
                val fileName1 = "$fileName-1" // 이미지 파일명
                if (imageUri1 != null) {
                    Log.d("WriteReviewFragment", " 2번이미지 업로드시작")
                    // 2장짜리.
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부 boardImgDtoList 순서 3 함수 호출 전 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부  boardDtoMap 순서 4 함수 호출 전 : $boardDtoMap")
                    uploadImageToFirebaseStorage2(imageUri1, fileName1)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-1.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName1, // 이미지 파일명
                        oriImgName = fileName1, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                    Log.d("WriteReviewFragment", "boardImgDtoList  순서 7 함수 호출 후 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "boardDtoMap 순서 8 함수 호출 후 : $boardDtoMap")
                }

                val imageUri2: Uri? = (reviewImg2.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-2.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

//                    FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName, file)
                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)
                }
                val fileName2 = "$fileName-2" // 이미지 파일명
                if (imageUri2 != null) {
                    Log.d("WriteReviewFragment", " 2번이미지 업로드시작")
                    // 2장짜리.
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부 boardImgDtoList 순서 3 함수 호출 전 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage 함수 내부  boardDtoMap 순서 4 함수 호출 전 : $boardDtoMap")
                    uploadImageToFirebaseStorage2(imageUri2, fileName2)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-2.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName2, // 이미지 파일명
                        oriImgName = fileName2, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                    Log.d("WriteReviewFragment", "boardImgDtoList  순서 7 함수 호출 후 : $boardImgDtoList")
                    Log.d("WriteReviewFragment", "boardDtoMap 순서 8 함수 호출 후 : $boardDtoMap")
                }

                val imageUri3: Uri? = (reviewImg3.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-3.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)                }
                val fileName3 = "$fileName-3" // 이미지 파일명
                if (imageUri3 != null) {
                    Log.d("WriteReviewFragment", " 3번이미지 업로드시작")
                    uploadImageToFirebaseStorage3(imageUri3, fileName3)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-3.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName3, // 이미지 파일명
                        oriImgName = fileName3, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                }
                val imageUri4: Uri? = (reviewImg4.drawable as? BitmapDrawable)?.bitmap?.let { bitmap ->
                    // 이미지 파일을 캐시 디렉토리에 저장하여 Uri를 얻는 방법
                    val imagesFolder = File(requireContext().cacheDir, "uploadImg")
                    if (!imagesFolder.exists()) imagesFolder.mkdirs()

                    val file = File(imagesFolder, "$fileName-4.jpg")
                    val fileOutputStream = FileOutputStream(file)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                    fileOutputStream.flush()
                    fileOutputStream.close()

                    FileProvider.getUriForFile(requireContext(),"com.example.find_my_matzip", file)                }
                val fileName4 = "$fileName-4" // 이미지 파일명
                if (imageUri4 != null) {
                    Log.d("WriteReviewFragment", " 4번이미지 업로드시작")
                    uploadImageToFirebaseStorage4(imageUri4, fileName4)
                    val imgStorageUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/board_img_img%2F${fileName}-4.jpg?alt=media"
                    uploadedImg = BoardImgDto(
                        id = 0, // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                        imgName = fileName4, // 이미지 파일명
                        oriImgName = fileName4, // 원본 이미지명
                        imgUrl = imgStorageUrl, // 이미지 URL
                        repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                    )

                    boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                    Log.d("WriteReviewFragment","boardImgDtoList.add(uploadedImg) 이미지 정보리스트추가 !!!!!!")
                    Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부 boardImgDtoList 성공 후 순서 5 함수 내부 : $boardImgDtoList")

                }
                Log.d("WriteReviewFragment", " 4번이미지 업로드완료")
                boardDtoMap["boardImgDtoList"] = boardImgDtoList

                Log.d("WriteReviewFragment", "uploadImageToFirebaseStorage 함수 내부  boardDtoMap 성공 후 순서 6 함수 내부 : $boardDtoMap")
                Log.d("WriteReviewFragment", "uploadDb 함수호출.")
                val scoreText = binding.boardScore.text.toString()
                val score = scoreText.toIntOrNull()?:3

                boardDtoMap["userId"] = userId
                boardDtoMap["boardViewStatus"] = "VIEW"
                boardDtoMap["boardTitle"] = binding.boardTitle.text.toString()
                boardDtoMap["content"] = binding.boardContent.text.toString()
                boardDtoMap["score"] = score
                Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기전 : $boardImgDtoList")
                boardDtoMap["boardImgDtoList"] = boardImgDtoList

                Log.d("WriteReviewFragment", "boardImgDtoList 맵에 넣기후 : $boardImgDtoList")
                Log.d("WriteReviewFragment","boardDtoMap의 내용 확인 : ${boardDtoMap} ")
                Log.d("WriteReviewFragment", " boardFormDto 담기작업끝 ===========================================")

                val boardService = (context?.applicationContext as MyApplication).boardService

                Log.d("WriteReviewFragment", " resId? : $resId")
                val call = resId?.let { it1 -> boardService.createBoard3(it1, boardDtoMap) }

                Log.d("WriteReviewFragment", " val call = resId?.let { it1 -> boardService.createBoard2(it1, boardFormDto) } ")
                call?.enqueue(object : Callback<Unit> {
                    override fun onResponse(
                        call: Call<Unit>,
                        response: Response<Unit>
                    ) {

                        Log.d("WriteReviewFragment", "Request URL: ${call.request().url()}")
                        Log.d("WriteReviewFragment", "Request Body: ${call.request().body()}")
                        Log.d("WriteReviewFragment", "Response Code: ${response.code()}")
                        if (response.isSuccessful) {
                            Log.d("WriteReviewFragment", "성공(Board) :  ${boardDtoMap}")
                            Log.d("WriteReviewFragment", "성공(createBoard_body) :  ${response.body().toString()}")
                            Toast.makeText(requireContext(),"스토리지/DB 업로드 완료", Toast.LENGTH_SHORT).show()
                            //로딩창 지우기
                            loadingDialog.dismiss()
                        } else {
                            Log.d("WriteReviewFragment", "서버 응답 실패: ${response.code()}")
                            //로딩창 지우기
                            loadingDialog.dismiss()
                            try {
                                //로딩창 지우기
                                loadingDialog.dismiss()
                                val errorBody = response.errorBody()?.string()

                                val jsonError = JSONObject(errorBody)
                                val errorMessage = jsonError.optString("message", "Unknown Error")

                                Log.d("WriteReviewFragment", "Error Body: $errorBody")
                                Log.d("WriteReviewFragment", "Error errorMessage: ${errorMessage}")

                                if (errorMessage.equals("게시글등록실패")) {
                                    Toast.makeText(requireContext(), "게시글등록실패.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Toast.makeText(requireContext(), "게시글등록실패onFailure.", Toast.LENGTH_SHORT).show()
                        Log.d("WriteReviewFragment", "실패 ${t.message}")

                        //로딩창 지우기
                        loadingDialog.dismiss()
                        call.cancel()
                    }
                })
            }

//            //여기서 업로드가 끝나면 db업로드 되도록 변경
//            uploadDb()

        }

        //게시글 작성버튼
        binding.submitBtn.setOnClickListener {
            Log.d("WriteReviewFragment", "=================게시글등록버튼 클릭됨===================================")
            //문자열을 정수로 변환. 평점란에 숫자가 아닌 다른것이 들어간다면 3을 기본값으로 설정
            val scoreText = binding.boardScore.text.toString()
            val score = scoreText.toIntOrNull()?:3

            if (reviewImg1.drawable == null) {
                Toast.makeText(requireContext(), "이미지는 최소 1장 업로드가 필요합니다", Toast.LENGTH_SHORT).show()
                //score에 1~5사이의 입력값이 정상적으로 들어 왔을 경우
            } else if(score in 1..5){
                //로딩창 띄우기
                Log.d("WriteReviewFragment", "=================로딩창 on===================================")
                loadingDialog.show()

                // 파일명 생성 : uuid+현재시간
                val currentTime = System.currentTimeMillis().toString()
                val uuid = UUID.randomUUID().toString()
                val fileName = "$uuid-$currentTime"

                Log.d("WriteReviewFragment", "====================================================현재 유저 아이디 : userId : $userId")

                Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage boardImgDtoList 순서1 함수 호출 전 : $boardImgDtoList")
                Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage boardDtoMap 순서2 함수 호출 전 : $boardDtoMap")
                uploadImagesToFirebaseStorage(fileName)
                Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage boardImgDtoList 순서 9 함수 호출 후 : $boardImgDtoList")
                Log.d("WriteReviewFragment", "uploadImagesToFirebaseStorage boardDtoMap 순서 10 함수 호출 후 : $boardDtoMap")
//                uploadImageTest(filePathTest)
                // 프래그먼트 전환, 끄고, 확인시, 정상 동작함.
//                uploadImageToFirebaseStorageTest(filePathTest,fileName)
                Toast.makeText(requireContext(), "업로드후", Toast.LENGTH_SHORT).show()
                Log.d("WriteReviewFragment", " uploadImagesToFirebaseStorage 실행끗!!!!!=====================================")
                //여기서 업로드가 끝나면 db업로드 되도록 변경
                Log.d("WriteReviewFragment", "boardImgDtoList uploadDb 함수 호출 전 : $boardImgDtoList")
                //
//                uploadDb()
                Log.d("WriteReviewFragment", "boardImgDtoList uploadDb 함수 호출 후 : $boardImgDtoList")
                Log.d("WriteReviewFragment", " uploadDb 끝=====================================")
            }else{
                Toast.makeText(requireContext(), "평점은 1~5 사이의 숫자만 입력할 수 있습니다", Toast.LENGTH_SHORT).show()
            }
            Log.d("WriteReviewFragment", "=================게시글 작성버튼 끝===================================")

            Log.d("WriteReviewFragment", "모든 작업이 끝났습니다 HomeFragment로 이동합니다.")

//            val newFragment = HomeFragment() // 전환할 새로운 프래그먼트 인스턴스 생성
//            // 프래그먼트 트랜잭션 시작
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                replace(R.id.homeFragment, newFragment) // R.id.container에는 새 프래그먼트가 표시될 컨테이너의 ID를 지정하세요.
//                addToBackStack(null) // 이전 상태로 돌아갈 수 있도록 back stack에 추가
//                commit()
//            }
            loadingDialog.dismiss()
        }//게시글작성버튼

        loadingDialog.dismiss()
        return binding.root
    } // onCreateView

    // 갤러리 열기 및 이미지 선택 요청
    private fun startGalleryForImage(imageIndex: Int) {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, imageIndex)
    }

    fun updateLayoutVisibility() {
        when (imgCounter) {
            1 -> {
                imgUploadLayout2.visibility = View.GONE
                imgUploadLayout3.visibility = View.GONE
                imgUploadLayout4.visibility = View.GONE
            }
            2 -> {
                imgUploadLayout2.visibility = View.VISIBLE
                imgUploadLayout3.visibility = View.GONE
                imgUploadLayout4.visibility = View.GONE
            }
            3 -> {
                imgUploadLayout2.visibility = View.VISIBLE
                imgUploadLayout3.visibility = View.VISIBLE
                imgUploadLayout4.visibility = View.GONE
            }
            4 -> {
                imgUploadLayout2.visibility = View.VISIBLE
                imgUploadLayout3.visibility = View.VISIBLE
                imgUploadLayout4.visibility = View.VISIBLE
            }
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data
            when (requestCode) {
                1 -> loadImage(imageUri, reviewImg1)
                2 -> loadImage(imageUri, reviewImg2)
                3 -> loadImage(imageUri, reviewImg3)
                4 -> loadImage(imageUri, reviewImg4)
            }
        }
    }
    private fun loadImage(imageUri: Uri?, imageView: ImageView) {
        Glide.with(this)
            .load(imageUri)
            .apply(RequestOptions().override(900, 900))
            .centerCrop()
            .into(imageView)
        imageView.visibility = View.VISIBLE

        imageUri?.let { uri ->
            val cursor = requireContext().contentResolver.query(
                uri,
                arrayOf(MediaStore.Images.Media.DATA),
                null,
                null,
                null
            )

            cursor?.use { c ->
                val columnIndex = c.getColumnIndex(MediaStore.Images.Media.DATA)
                if (columnIndex != -1 && c.moveToFirst()) {
                    filePath = c.getString(columnIndex)
                    Log.d("WriteReviewFragment", "갤러리 filePath : $filePath")
                } else {
                    Log.e("WriteReviewFragment", "컬럼 인덱스를 찾을 수 없거나 데이터를 가져올 수 없습니다.")
                }
            }
            cursor?.close()
        }
    }

    private fun removeImage(imageView: ImageView) {
        Glide.with(this)
            .clear(imageView)
        imageView.setImageDrawable(null)
        imageView.visibility = View.GONE
    }


    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","WriteReviewFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","WriteReviewFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","WriteReviewFragment onDestroy")
        super.onDestroy()
    }

}//프래그먼트의 끝


//라디오 설정할때 참고할것
//fun getValue(v: View?): String? {
//    val male = binding.radio1
//    val female = binding.radio2
//    var pickValue: String? = null
//    if (male.isChecked) {
//        pickValue = male.text.toString()
//    } else if (female.isChecked) {
//        pickValue = female.text.toString()
//    }
//    return pickValue
//}