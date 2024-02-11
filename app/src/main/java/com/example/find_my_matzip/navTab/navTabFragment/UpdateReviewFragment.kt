package com.matzip.find_my_matzip

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.system.Os.remove
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.matzip.find_my_matzip.databinding.FragmentUpdateReviewBinding
import com.matzip.find_my_matzip.databinding.FragmentWriteReviewBinding
import com.matzip.find_my_matzip.model.BoardDtlDto
import com.matzip.find_my_matzip.model.BoardImgDto
import com.matzip.find_my_matzip.model.ProfileDto
import com.matzip.find_my_matzip.navTab.adapter.UpdateReviewAdapter
import com.matzip.find_my_matzip.navTab.navTabFragment.NewHomeFragment
import com.matzip.find_my_matzip.utils.SharedPreferencesManager
import com.matzip.find_my_matzip.utils.LoadingDialog
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import es.dmoral.toasty.Toasty
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class UpdateReviewFragment : Fragment() {
    lateinit var binding : FragmentUpdateReviewBinding
    private var uuid = UUID.randomUUID().toString()
    private lateinit var imgNameList : List<String>

    // 로그인한 사용자의 아이디를 가져와서 해당 사용자의 프로필 정보를 서버에서 조회
    val loginuserId = SharedPreferencesManager.getString("id","")
    private var resId: String? = null

    private var uriList = ArrayList<Uri>()
    //    일반 이미지 업로드 최대 갯수
    private val maxNumber = 5
    lateinit var adapter: UpdateReviewAdapter
    lateinit var homeTabActivity: HomeTabActivity

    private val TAG: String = "WriteReviewFragment"

    private var boardImgDtoList: MutableList<BoardImgDto> = mutableListOf()
    lateinit var boardDtoMap : MutableMap<String,Any>
    lateinit var uploadedImg : BoardImgDto

//    가져온이미지url 변환하기위한코드
    private fun convertImageUrlToUri(imageUrl: String): Uri {
        return Uri.parse(imageUrl)
    }//    가져온이미지url 변환하기위한코드

    private fun getImageUriFromBitmap(context: Context, bitmap: Bitmap): Uri? {
        // 비트맵을 파일로 저장
        val imagesDir = File(context.cacheDir, "images")
        if (!imagesDir.exists()) {
            imagesDir.mkdirs()
        }
        val file = File(imagesDir, "${System.currentTimeMillis()}.png")
        try {
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
        // 파일로부터 Uri 생성
        return FileProvider.getUriForFile(context, "${context.packageName}", file)
    }

    // Uri를 uriList에 추가하는 함수
    private fun addUriToList(uri: Uri) {
        uriList.add(uri)
        adapter.notifyDataSetChanged()
        Log.d(TAG, "addUriToList의 결과 uriList: $uriList")
        printCount()
    }

    companion object {
        fun newInstance(boardId: String): UpdateReviewFragment {
            Log.d("SdoLifeCycle","UpdateReviewFragment newInstance")
            val fragment = UpdateReviewFragment()
            val args = Bundle()
            args.putString("boardId", boardId)
            fragment.arguments = args
            return fragment
        }
    }

    //homeTabActivity 연결
    override fun onAttach(context: Context) {
        super.onAttach(context)

        homeTabActivity = context as HomeTabActivity
    }//homeTabActivity 연결

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","WriteReviewFragment onCreateView")
        binding = FragmentUpdateReviewBinding.inflate(layoutInflater,container,false)

        adapter = UpdateReviewAdapter(requireContext(),uriList)
        binding.recyclerview.adapter = adapter
        // LinearLayoutManager을 사용하여 수평으로 아이템을 배치한다.
        binding.recyclerview.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val boardService = (context?.applicationContext as MyApplication).boardService
        val updateBoard = arguments?.getString("boardId")?.let { boardService.getBoardDtl(it) }

        updateBoard?.enqueue(object : Callback<BoardDtlDto>{
            override fun onResponse(call: Call<BoardDtlDto>, response: Response<BoardDtlDto>) {
                Log.d("kkt","데이터 도착 확인.")
                val boardDto = response.body()
                imgNameList = boardDto?.board?.boardImgDtoList
                    ?.map { it.imgName }
                    ?.filter { it.isNotBlank() } ?: emptyList()
                Log.d("TAG","파이어베이스에서 삭제할 이미지의 이름 리스트 imgNameList : $imgNameList")
                Log.d("kkt","데이터 도착 확인1. : BoardDtlDto $boardDto")
                Log.d("kkt","데이터 도착 확인2. : BoardDtlDto.board ${boardDto?.board}")
                Log.d("kkt","데이터 도착 확인3. : BoardDtlDto.restaurant ${boardDto?.restaurant}")
                Log.d("kkt","데이터 도착 확인4. : BoardDtlDto.users ${boardDto?.users}")

                binding.userId.text = boardDto?.users?.userid.toString()
                val boardTitle = boardDto?.board?.boardTitle ?: ""
                binding.boardTitle.text = Editable.Factory.getInstance().newEditable(boardTitle)

                val boardContent = boardDto?.board?.content ?: ""
                binding.boardContent.text = Editable.Factory.getInstance().newEditable(boardContent)

                val boardScore = boardDto?.board?.score?.toString() ?: ""
                binding.boardScore.text = Editable.Factory.getInstance().newEditable(boardScore)

                resId = boardDto?.restaurant?.res_id.toString()


                // 기존 데이터를 업데이트한 후, 새로운 데이터를 얻었을 때 adapter에 설정하고 알립니다.
                val imgUriList: List<Uri> = boardDto?.board?.boardImgDtoList?.mapNotNull { convertImageUrlToUri(it.imgUrl) } ?: emptyList()

                uriList.clear()

                // boardImgDtoList에서 imgUrl을 추출하여 Uri 리스트로 변환하여 반환하는 함수
                fun extractUriListFromBoardDto(boardImgDtoList: List<BoardImgDto>): List<Uri> {
                    return boardImgDtoList.mapNotNull { convertImageUrlToUri(it.imgUrl) }
                }

                // 이미지 URL 리스트를 Glide를 사용하여 비트맵으로 변환하고 Uri로 변환하여 uriList에 추가하는 함수
                fun urlToUri(imageUrls: List<String>) {
                    val requestOptions = RequestOptions()
                        .format(DecodeFormat.PREFER_ARGB_8888)
                        .fitCenter()

                    imageUrls.forEach { imageUrl ->
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(imageUrl)
                            .apply(requestOptions)
                            .into(object : CustomTarget<Bitmap>() {
                                override fun onResourceReady(
                                    resource: Bitmap,
                                    transition: Transition<in Bitmap>?
                                ) {
                                    val imageUri = getImageUriFromBitmap(requireContext(), resource)
                                    imageUri?.let {
//                                        uriList.add(it)
//                                        adapter.notifyDataSetChanged()
                                        addUriToList(it)
                                    }
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    Log.d(TAG,"이미지로드가 취소됨.")
                                }
                            })
                    }
                }

                // boardImgDtoList에서 imgUrl을 추출하여 Uri 리스트로 변환하여 uriList에 할당
                val uriList: List<Uri> = extractUriListFromBoardDto(boardDto?.board?.boardImgDtoList ?: emptyList())

                // 이미지 URL 리스트를 Glide를 사용하여 비트맵으로 변환하고 Uri로 변환하여 uriList에 추가
                urlToUri(uriList.map { it.toString() })
//                uriList.clear()
//                uriList.addAll(imgUriList)

                boardImgDtoList.clear()
                boardImgDtoList.addAll(boardDto?.board?.boardImgDtoList ?: emptyList())
                Log.d(TAG,"boardImgDtoList중에 빈 요소 제거전 : $boardImgDtoList")
                //boardImgDtoList중에 빈 요소를 찾아 리스트에서 제거.
                for(i in 4 downTo 1){
                    if (boardImgDtoList[i].imgUrl.isBlank()){
                        boardImgDtoList.removeAt(i)
                    } else{
                        break
                    }
                }//boardImgDtoList중에 빈 요소를 찾아 리스트에서 제거.
                Log.d(TAG,"boardImgDtoList중에 빈 요소 제거 완료 : $boardImgDtoList")


                adapter.notifyDataSetChanged()
                //이때
                Log.d(TAG,"uriList : $uriList")
                Log.d(TAG,"boardImgDtoList : $boardImgDtoList")
                printCount()
            }

            override fun onFailure(call: Call<BoardDtlDto>, t: Throwable) {

            }

        })

        boardDtoMap = mutableMapOf<String,Any>()
        uploadedImg = BoardImgDto(1,"abc","abc","abc","Y")

        val loadingDialog = LoadingDialog(requireContext())

        // ImageView를 클릭할 경우
        // 선택 가능한 이미지의 최대 개수를 초과하지 않았을 경우에만 앨범을 호출한다.
        binding.imageArea.setOnClickListener {
            binding.imageArea.setBackgroundResource(R.drawable.radius3)
            if (uriList.count() == maxNumber) {
                Toasty.error(
                    requireActivity(),
                    "이미지는 최대 ${maxNumber}장까지 첨부할 수 있습니다.",
                    Toast.LENGTH_SHORT
                ).show();
                return@setOnClickListener
            }
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            registerForActivityResult.launch(intent)
        }
        binding.boardTitle.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // 포커스가 있을 때의 배경 설정
                view.setBackgroundResource(R.drawable.radius3)
            } else {
                // 포커스가 없을 때의 배경 설정
                view.setBackgroundResource(R.drawable.radius3)
            }
        }
        binding.boardScore.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                // 포커스가 있을 때의 배경 설정
                view.setBackgroundResource(R.drawable.radius3)
            } else {
                // 포커스가 없을 때의 배경 설정
                view.setBackgroundResource(R.drawable.radius3)
            }
        }

        // ★★★★등록 버튼눌렀을 때 ★★★★
        binding.submitBtn.setOnClickListener {
            Log.d("WriteReviewFragment", "=================로딩창 on===================================")
            loadingDialog.show()
            val scoreText = binding.boardScore.text.toString()
            val score = scoreText.toIntOrNull()?:9

            if(uriList.size == 0){
                loadingDialog.dismiss()
                Toasty.error(requireContext(), "이미지는 최소 1장 업로드가 필요합니다", Toast.LENGTH_SHORT).show()
                binding.imageArea.setBackgroundResource(R.drawable.radius_edittext_red2)
            }
            else if(binding.boardTitle.text.isEmpty()){
                loadingDialog.dismiss()
                Toasty.error(requireContext(), "제목을 입력해주세요.", Toast.LENGTH_SHORT).show()
                binding.boardTitle.setBackgroundResource(R.drawable.radius_edittext_red2)
            }
            else if(score !in 1..5){
                loadingDialog.dismiss()
                Toasty.error(requireContext(), "평점은 1~5 사이의 숫자만 입력할 수 있습니다.", Toast.LENGTH_SHORT).show()
                binding.boardScore.setBackgroundResource(R.drawable.radius_edittext_red2)
            }
            else {

                Log.d("TAG","DB로 전달하는 boardDtoMap : $boardDtoMap")
                Log.d("TAG","파이어베이스에서 삭제할 이미지의 이름 리스트 imgNameList : $imgNameList")
                //파이어베이스에 이미지 업로드 + repImgYn 값 설정==================================================================================
                for (i in 0 until uriList.count()) {
                    val fileName = "$loginuserId-$resId-$uuid-$i"
                    imageUpload(uriList.get(i), i) //이걸로 파이어베이스에 업로드 끝
                    boardImgDtoList[i].imgName =
                        "$fileName"
                    boardImgDtoList[i].imgUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/newboardimg%2F${fileName}.png?alt=media"
                    // 첫 번째 이미지인 경우 repImgYn을 "Y"로 설정
                    if (i == 0) {
                        boardImgDtoList[0].repImgYn = "Y"
                        Log.d(TAG,"1번째 이미지 업로드 완료 repImgYn 값이 Y인지 확인 : ${boardImgDtoList[0].repImgYn}")
                    } else {
                        Log.d(TAG,"${i+1} 번째 이미지 업로드 완료 repImgYn 값이 N인지 확인 : ${boardImgDtoList[i].repImgYn}")
                    }
                    Log.d(TAG,"")

                } //파이어베이스에 이미지 업로드 + repImgYn 값 설정==================================================================================
                Log.d(TAG,"파이어베이스에 이미지들 업로드 완료, 남아있던 이미지들 삭제")
                deleteFirebaseImages(imgNameList)

                //이미지의 갯수가 5개가 안될 때 그 자리에 빈 데이터 넣기
                if (boardImgDtoList.count() <5) {
                    //들어가있는 갯수 ~ 5개 까지 반복한다
                    for (i in boardImgDtoList.count() until 5) {
                        uploadedImg = BoardImgDto(
                            id = i.toLong(), // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                            imgName = "", // 이미지 파일명
                            oriImgName = "", // 원본 이미지명
                            imgUrl = "", // 이미지 URL
                            repImgYn = "N"
                        )
                        boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                        Log.d(TAG,"${boardImgDtoList.count()}번째 자리에, 빈 데이터 추가완료.")
                    }

                }//이미지의 갯수가 5개가 안될 때 그 자리에 빈 데이터 넣기

                //DB로 보낼 게시글 정보를 boardDtoMap에 담기
                boardDtoMap["userId"] = loginuserId
                boardDtoMap["boardViewStatus"] = "VIEW"
                boardDtoMap["boardTitle"] = binding.boardTitle.text.toString()
                boardDtoMap["content"] = binding.boardContent.text.toString()
                boardDtoMap["score"] = score
                boardDtoMap["boardImgDtoList"] = boardImgDtoList
                Log.d("WriteReviewFragment","boardDtoMap의 내용 확인(다담은상태) : ${boardDtoMap} ")

                val boardService = (context?.applicationContext as MyApplication).boardService
                //boardId가져와야함
                val call = arguments?.getString("boardId")?.let { it1 -> boardService.editBoard(it1, boardDtoMap) }
                Log.d("WriteReviewFragment"," 콜백으로 boardId 잘 보내는지 확인  : ${arguments?.getString("boardId")} ")

                //DB로 전달하는 콜백함수==================================================================================
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
                        } else {
                            Log.d("WriteReviewFragment", "서버 응답 실패: ${response.code()}")
                            try {
                                val errorBody = response.errorBody()?.string()

                                val jsonError = JSONObject(errorBody)
                                val errorMessage = jsonError.optString("message", "Unknown Error")

                                Log.d("WriteReviewFragment", "Error Body: $errorBody")
                                Log.d("WriteReviewFragment", "Error errorMessage: ${errorMessage}")

                                if (errorMessage.equals("게시글등록실패")) {
                                    //로딩창 지우기
                                    loadingDialog.dismiss()
                                    Toasty.error(requireContext(), "게시글등록실패.", Toast.LENGTH_SHORT)
                                        .show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Unit>, t: Throwable) {
                        Toasty.error(requireContext(), "게시글등록실패onFailure.", Toast.LENGTH_SHORT).show()
                        Log.d("WriteReviewFragment", "실패 ${t.message}")

                        //로딩창 지우기
                        loadingDialog.dismiss()
                        call.cancel()
                    }
                }) //DB로 전달하는 콜백함수
                //DB로 전달하는 콜백함수==================================================================================
                Log.d(TAG, "작업 완료후 게시글작성 프래그먼트 닫기")
                Handler().postDelayed({
                    loadingDialog.dismiss()
                    Toasty.success(requireContext(),"게시글 수정 완료했습니다",Toast.LENGTH_SHORT).show()
                    val fragment = NewHomeFragment()
                    parentFragmentManager.beginTransaction()
                        .add(R.id.fragmentContainer, fragment)
                        .remove(this@UpdateReviewFragment)
                        .commit()
                }, 1500) // 2000 밀리초 (2초) 동안 기다린 후 실행
            }
            Log.d("TAG","DB로 전달하는 작업이 끝남  : $boardDtoMap")
        }// ★★★★등록 버튼눌렀을 때 ★★★★
        // ★★★★등록 버튼눌렀을 때 ★★★★

        adapter.setItemClickListener(object : UpdateReviewAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                uriList.removeAt(position)
                boardImgDtoList.removeAt(position)
                adapter.notifyDataSetChanged()
                printCount()
                Log.d("TAG","uriList : $uriList")
                Log.d("TAG","boardImgDtoList : $boardImgDtoList")
            }

        })

        return binding.root
    } // onCreateView

    private val registerForActivityResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            when (result.resultCode) {
                AppCompatActivity.RESULT_OK -> {

                    val clipData = result.data?.clipData

                    if (clipData != null) { // 이미지를 여러 개 선택할 경우
                        val clipDataSize = clipData.itemCount
                        val selectableCount = maxNumber - uriList.count()
                        if (clipDataSize > selectableCount) { // 최대 선택 가능한 개수를 초과해서 선택한 경우
                            Toasty.error(
                                requireActivity(),
                                "이미지는 최대 ${selectableCount}장까지 더 첨부할 수 있습니다.",
                                Toast.LENGTH_SHORT
                            ).show();
                        } else {
                            // 선택 가능한 경우 ArrayList에 가져온 uri를 넣어준다.
                            for (i in 0 until clipDataSize) {
                                uriList.add(clipData.getItemAt(i).uri)

//                                //여기다가 이걸해주고
//                                문제점 지금당장 fileName값을 할당받는 시점이 파이어베이스에 업로드를 할 시점인데
//                                여기서 리스트를 담아버리면 fileName값이 2번 생성되어 주소가 달라지게 된다.
//                                그렇다면 fileName을
                                val fileName = "$loginuserId-$resId-$uuid-$i"
                                val imgStorageUrl =
                                    "임시 이미지url"
                                uploadedImg = BoardImgDto(
                                    id = i.toLong(), // 이미지 ID는 서버에서 생성되므로 0으로 설정하거나 다른 값으로 임시 설정해주세요.
                                    imgName = fileName, // 이미지 파일명
                                    oriImgName = fileName, // 원본 이미지명
                                    imgUrl = imgStorageUrl, // 이미지 URL
//                                  repImgYn = if (boardImgDtoList.isEmpty()) "Y" else "N" // 첫 번째 이미지인 경우 'Y', 그 외에는 'N'으로 설정
                                    repImgYn = "N"

                                )//일단 여기까지 완료
//
                                boardImgDtoList.add(uploadedImg) // 이미지 정보를 리스트에 추가
                            }
                        }
                    } else { // 이미지를 한 개만 선택할 경우 null이 올 수 있다.
                        val uri = result?.data?.data
                        if (uri != null) {
                            uriList.add(uri)
                        }
                    }
                    // notifyDataSetChanged()를 호출하여 adapter에게 값이 변경 되었음을 알려준다.
//                    adapter.notifyDataSetChanged()
                    printCount()
                    Log.d(TAG,"uriList에 담긴 이미지 갯수 : ${uriList.count()}")
                    Log.d(TAG,"uriList : $uriList")
                    Log.d(TAG,"boardImgDtoList에 담긴 이미지 갯수 : ${boardImgDtoList.count()}")
                    Log.d(TAG,"boardImgDtoList : $boardImgDtoList")
//                    adapter.loadImages(uriList)
                    adapter.notifyDataSetChanged()
                }
            }
        }

    private fun printCount() {
        Log.d(TAG,"프린트 카운트 uriList : $uriList")
        Log.d(TAG,"프린트 카운트 boardImgDtoList : $boardImgDtoList")
        val text = "${uriList.count()}/${maxNumber}"
        binding.countArea.text = text
    }

    // 파일 업로드
    // 파일을 가리키는 참조를 생성한 후 putFile에 이미지 파일 uri를 넣어 파일을 업로드한다.
    private fun imageUpload(uri: Uri, count: Int) {
        // storage 인스턴스 생성
        val storage = Firebase.storage
        // storage 참조
        val storageRef = storage.getReference("newboardimg")
        val fileName = "$loginuserId-$resId-$uuid-$count"

        val mountainsRef = storageRef.child("${fileName}.png")
        val uploadTask = mountainsRef.putFile(uri)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            // 파일 업로드 성공
//            Toast.makeText(requireActivity(), "갤러리에서 이미지 가져오기 성공", Toast.LENGTH_SHORT).show();
        }.addOnFailureListener {
            // 파일 업로드 실패
//            Toast.makeText(requireActivity(), "갤러리에서 이미지 가져오기 실패", Toast.LENGTH_SHORT).show();
        }
    }

    //파이어베이스에서 이미지 삭제 로직
//    private fun deleteFirebaseImages(imageUrls: List<String>) {
//        val storage = Firebase.storage
//        val storageRef = storage.reference
//
//        for (imageUrl in imageUrls) {
//            val httpsReference = storage.getReferenceFromUrl(imageUrl)
//
//            // Delete the file
//            httpsReference.delete()
//                .addOnSuccessListener {
//                    // 파일 삭제 성공
//                    Log.d(TAG, "Firebase 이미지 삭제 성공: $imageUrl")
//                }
//                .addOnFailureListener { e ->
//                    // 파일 삭제 실패
//                    Log.e(TAG, "Firebase 이미지 삭제 실패: $imageUrl, Exception: $e")
//                }
//        }
//    }
    private fun deleteFirebaseImages(imageNames: List<String>) {
        val storage = Firebase.storage
        val storageRef = storage.reference

        for (imageName in imageNames) {
            // 삭제할 이미지에 대한 참조 생성
            val imageRef = storageRef.child("newboardimg/$imageName.png")

            // 이미지 삭제
            imageRef.delete()
                .addOnSuccessListener {
                    // 이미지 삭제 성공
                    Log.d(TAG, "Firebase 이미지 삭제됨: $imageName")
                }
                .addOnFailureListener { e ->
                    // 이미지 삭제 실패
                    Log.e(TAG, "Firebase 이미지 삭제 실패: $imageName", e)
                }
        }
    }
// 파이어베이스에서 이미지 삭제 로직




}//프래그먼트의 끝
