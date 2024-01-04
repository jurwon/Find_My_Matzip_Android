package com.example.find_my_matzip

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.find_my_matzip.databinding.FragmentWriteReviewBinding
import com.example.find_my_matzip.model.BoardImgDto
import com.example.find_my_matzip.model.ProfileDto
import com.example.find_my_matzip.navTab.adapter.WriteReviewAdapter
import com.example.find_my_matzip.navTab.navTabFragment.NewHomeFragment
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.example.find_my_matzip.utils.LoadingDialog
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.UUID

class WriteReviewFragment : Fragment() {
    lateinit var binding : FragmentWriteReviewBinding
    private var uuid = UUID.randomUUID().toString()

    // 로그인한 사용자의 아이디를 가져와서 해당 사용자의 프로필 정보를 서버에서 조회
    val userId = SharedPreferencesManager.getString("id","")

    private var uriList = ArrayList<Uri>()
    //    일반 이미지 업로드 최대 갯수
    private val maxNumber = 5
    lateinit var adapter: WriteReviewAdapter
    lateinit var homeTabActivity: HomeTabActivity

    private val TAG: String = "WriteReviewFragment"

    lateinit var boardImgDtoList: MutableList<BoardImgDto>
    lateinit var boardDtoMap : MutableMap<String,Any>
    lateinit var uploadedImg : BoardImgDto

    //resId가져오기
    companion object {
        fun newInstance(resId: Long?): WriteReviewFragment {
            Log.d("SdoLifeCycle","WriteReviewFragment newInstance")
            val fragment = WriteReviewFragment()
            val args = Bundle()
            args.putLong("resId", resId!!)
            fragment.arguments = args
            return fragment
        }
    }//resId가져오기

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
        binding = FragmentWriteReviewBinding.inflate(layoutInflater,container,false)

        // 이전 프래그먼트에서 전달된 resId 가져오기
        val resId = arguments?.getLong("resId")
        binding.userId.text = userId

        //db에 저장하기 위해서 list생성
        boardImgDtoList = mutableListOf<BoardImgDto>()
        boardDtoMap = mutableMapOf<String,Any>()
        uploadedImg = BoardImgDto(1,"abc","abc","abc","Y")

        val loadingDialog = LoadingDialog(requireContext())

        printCount()
        // RecyclerView에 Adapter 연결하기
        adapter = WriteReviewAdapter(requireContext(),uriList)
        binding.recyclerview.adapter = adapter
        // LinearLayoutManager을 사용하여 수평으로 아이템을 배치한다.
        binding.recyclerview.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        // ImageView를 클릭할 경우
        // 선택 가능한 이미지의 최대 개수를 초과하지 않았을 경우에만 앨범을 호출한다.
        binding.imageArea.setOnClickListener {
            if (uriList.count() == maxNumber) {
                Toast.makeText(
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

        // ★★★★등록 버튼눌렀을 때 ★★★★
        binding.submitBtn.setOnClickListener {
            Log.d("WriteReviewFragment", "=================로딩창 on===================================")
            loadingDialog.show()
            val scoreText = binding.boardScore.text.toString()
            val score = scoreText.toIntOrNull()?:3

            if(uriList.size == 0){
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "이미지는 최소 1장 업로드가 필요합니다", Toast.LENGTH_SHORT).show()
            }else if(score in 1..5){

                Log.d("TAG","DB로 전달하는 boardDtoMap : $boardDtoMap")

                //파이어베이스에 이미지 업로드 + repImgYn 값 설정==================================================================================
                for (i in 0 until uriList.count()) {
                    val fileName = "$userId-$resId-$uuid-$i"
                    imageUpload(uriList.get(i), i)
                    boardImgDtoList[i].imgName =
                        "$fileName"
                    boardImgDtoList[i].imgUrl =
                        "https://firebasestorage.googleapis.com/v0/b/findmymatzip.appspot.com/o/newboardimg%2F${fileName}.png?alt=media"
                    try {
                        Thread.sleep(500)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }

                    // 첫 번째 이미지인 경우 repImgYn을 "Y"로 설정
                    if (i == 0) {
                        boardImgDtoList[0].repImgYn = "Y"
                        Log.d(TAG,"1번째 이미지 업로드 완료 repImgYn 값이 Y인지 확인 : ${boardImgDtoList[0].repImgYn}")
                    } else {
                        Log.d(TAG,"${i+1} 번째 이미지 업로드 완료 repImgYn 값이 N인지 확인 : ${boardImgDtoList[i].repImgYn}")
                    }
                    Log.d(TAG,"")

                }//파이어베이스에 이미지 업로드 + repImgYn 값 설정
                //파이어베이스에 이미지 업로드 + repImgYn 값 설정==================================================================================

                //DB로 보낼 게시글 정보를 boardDtoMap에 담기
                boardDtoMap["userId"] = userId
                boardDtoMap["boardViewStatus"] = "VIEW"
                boardDtoMap["boardTitle"] = binding.boardTitle.text.toString()
                boardDtoMap["content"] = binding.boardContent.text.toString()
                boardDtoMap["score"] = score
                boardDtoMap["boardImgDtoList"] = boardImgDtoList
                Log.d("WriteReviewFragment","boardDtoMap의 내용 확인(다담은상태) : ${boardDtoMap} ")

                val boardService = (context?.applicationContext as MyApplication).boardService
                val call = resId?.let { it1 -> boardService.createBoard3(it1, boardDtoMap) }

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
                            Toast.makeText(requireContext(),"스토리지/DB 업로드 완료", Toast.LENGTH_SHORT).show()
                            //로딩창 지우기
                            loadingDialog.dismiss()
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
                }) //DB로 전달하는 콜백함수
                //DB로 전달하는 콜백함수==================================================================================
                Log.d(TAG, "작업 완료후 게시글작성 프래그먼트 닫기")
//                parentFragmentManager.beginTransaction().remove(this@WriteReviewFragment).commit()
                val fragment = NewHomeFragment()
                parentFragmentManager.beginTransaction()
                    .add(R.id.fragmentContainer, fragment)
                    .remove(this@WriteReviewFragment)
                    .commit()

            } else{
                loadingDialog.dismiss()
                Toast.makeText(requireContext(), "평점은 1~5 사이의 숫자만 입력할 수 있습니다", Toast.LENGTH_SHORT).show()
            }
            Log.d("TAG","DB로 전달하는 작업이 끝남  : $boardDtoMap")
        }// ★★★★등록 버튼눌렀을 때 ★★★★
        // ★★★★등록 버튼눌렀을 때 ★★★★

        adapter.setItemClickListener(object : WriteReviewAdapter.onItemClickListener {
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
                    val resId = arguments?.getLong("resId")

                    if (clipData != null) { // 이미지를 여러 개 선택할 경우
                        val clipDataSize = clipData.itemCount
                        val selectableCount = maxNumber - uriList.count()
                        if (clipDataSize > selectableCount) { // 최대 선택 가능한 개수를 초과해서 선택한 경우
                            Toast.makeText(
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
                                val fileName = "$userId-$resId-$uuid-$i"
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
                    adapter.notifyDataSetChanged()
                    printCount()
                }
            }
        }

    private fun printCount() {
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
        // storage에 저장할 파일명 선언
        // 파일명 생성 : uuid+현재시간
//         val currentTime = System.currentTimeMillis().toString()
//         val uuid = UUID.randomUUID().toString()
        val resId = arguments?.getLong("resId")
        val fileName = "$userId-$resId-$uuid-$count"
//        Log.d("WriteReviewFragment", "로그인 된 유저 확인 resId : $resId")


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


}//프래그먼트의 끝
