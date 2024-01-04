package com.example.find_my_matzip.navTab.navTabFragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentCommentBinding
import com.example.find_my_matzip.model.BoardDtlDto
import com.example.find_my_matzip.model.CommentDto
import com.example.find_my_matzip.model.ProfileDto
import com.example.find_my_matzip.navTab.adapter.CommentAdapter
import com.example.find_my_matzip.navTab.adapter.CommentAdapterListener
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CommentFragment : BottomSheetDialogFragment(), CommentAdapterListener {

    //    private var boardId: Long = 0L
//    private var commentId: Long = 0L
//    private var parentId: Long = 0L
    lateinit var binding: FragmentCommentBinding
    lateinit var adapter: CommentAdapter
    private var loginUserId: String = ""
    var commentContents: EditText? = null
    var saveBtn: Button? = null
    private val TAG: String = "CommentFragment"
    private var userImage: String = ""
    companion object {

        fun newInstance(boardId: String?): CommentFragment {
            Log.d("CommentFragment", "게시판 아이디 잘받았나욥  . boardId: $boardId")
            val fragment = CommentFragment()
            val args = Bundle()
            if (boardId != null) {
                args.putString("boardId", boardId)
            } else {
                // boardId가 null인 경우에 대한 처리
                Log.e("CommentFragment", "boardId 없음!")
            }
            fragment.arguments = args
            return fragment
        }

        // 유저 아이디를 전달하는 newInstance2 메서드
        fun newInstance2(userId: String): CommentFragment {
            val fragment = CommentFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SdoLifeCycle", "boardDtlFragment onCreate")
        super.onCreate(savedInstanceState)
    }

    //    private val userId: String
//        get() = SharedPreferencesManager.getString("id", "")
// 댓글 업데이트를 위한 함수

    override fun onReplyClick(comment: CommentDto, boardId: Long) {
        showReplyDialog(comment, boardId)
    }

    fun showReplyDialog(parentComment: CommentDto, boardId: Long) {


        val isParentComment = parentComment.depth == 0
        val commentWriterText = parentComment.commentWriter
        Log.d("CommentFragment", "commentWriterText: $commentWriterText")
        val boardIdText = parentComment.boardId.toString()
        val titleText = if (isParentComment) "$commentWriterText 에 대한 답글 작성" else {
            if (commentWriterText.isNotEmpty()) {
                "$commentWriterText 에 대한 답글 작성"
            } else {
                "댓글 작성 $boardIdText"
            }
        }
        Log.d("CommentFragment", "titleText: $titleText")
        val alertDialogBuilder = AlertDialog.Builder(requireContext())
        alertDialogBuilder.setTitle(titleText)
        alertDialogBuilder.setMessage("${loginUserId} 님 답글을 입력하세요:")
        val input = EditText(requireContext())
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = layoutParams
        alertDialogBuilder.setView(input)

        alertDialogBuilder.setPositiveButton("답글 작성") { dialog, which ->
            val replyText = input.text.toString()
            saveReplyComment(parentComment, replyText)
        }

        alertDialogBuilder.setNegativeButton("취소") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    private fun saveReplyComment(parentComment: CommentDto, replyText: String) {
        val boardService = (context?.applicationContext as MyApplication).boardService
        val boardDtl = arguments?.getString("boardId")?.let { boardService.getBoardDtl(it) }

        boardDtl?.enqueue(object : Callback<BoardDtlDto> {
            override fun onResponse(call: Call<BoardDtlDto>, response: Response<BoardDtlDto>) {
                if (response.isSuccessful) {
                    val boardDto = response.body()

                    val commentService =
                        (context?.applicationContext as MyApplication).commentService

                    if (boardDto != null) {
                        val boardId = boardDto.board.id
                        val commentWriter = loginUserId
                        val commentDto = CommentDto(
                            commentWriter = loginUserId,
                            commentContents = replyText,
                            boardId = boardId,
                            parentId = parentComment.commentId ?: 0,
                            depth = parentComment.depth + 1,
                            commentCreatedTime = "",
                            userImage = ""
                        )
                        val commentList =
                            commentService.saveReply(commentDto, parentComment.commentId)

                        commentList.enqueue(object : Callback<Unit> {
                            override fun onResponse(
                                call: Call<Unit?>,
                                response: Response<Unit?>
                            ) {
                                if (response.isSuccessful) {
                                    Toast.makeText(
                                        requireContext().applicationContext,
                                        "대댓글작성되었습니다요^ㅡㅡ^",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // 현재의 프래그먼트를 제거
                                    val transaction =
                                        requireActivity().supportFragmentManager.beginTransaction()
                                    transaction.remove(this@CommentFragment)
                                        .commit()

                                    // 새로운 인스턴스를 생성하여 추가
                                    val newFragment =
                                        CommentFragment.newInstance(boardId.toString())
                                    val newTransaction =
                                        requireActivity().supportFragmentManager.beginTransaction()
                                    newTransaction.add(
                                        R.id.fragmentContainer,
                                        newFragment
                                    )
                                    newTransaction.addToBackStack(null)
                                    newTransaction.commit()
                                } else {
                                    val errorBody = response?.errorBody()?.string()

                                    Toast.makeText(
                                        requireContext().applicationContext,
                                        "대댓글 작성 실패. 에러 메시지: $errorBody",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e("CommentFragment", "대댓글 작성 실패. 에러 메시지: $errorBody")
                                }
                            }

                            override fun onFailure(call: Call<Unit?>, t: Throwable) {
                                // TODO: 실패 처리 코드 추가
                            }
                        })

                    }
                }
            }

            override fun onFailure(call: Call<BoardDtlDto>, t: Throwable) {
                // TODO: 실패 처리 코드 추가
            }
        })
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentBinding.inflate(layoutInflater, container, false)
        loginUserId = SharedPreferencesManager.getString("id", "")
        commentContents = binding.commentContents
        saveBtn = binding.saveBtn
//        boardId = arguments?.getLong("boardId") ?: 0L
        val boardId = arguments?.getLong("boardId") ?: 0L

        // 로그인한 사용자의 아이디를 가져와서 해당 사용자의 프로필 정보를 서버에서 조회
        val userId = SharedPreferencesManager.getString("id", "")
        val userService = (context?.applicationContext as MyApplication).userService

        val profileList = userService.getProfile(userId, 5)

        Log.d("CommentFragment", "profileList.enqueue 호출전 : ")


        profileList.enqueue(object : Callback<ProfileDto> {
            override fun onResponse(
                call: Call<ProfileDto>,
                response: Response<ProfileDto>
            ) {
                Log.d(
                    "CommentFragment",
                    "도착 확인=================================================== "
                )
                val profileDto = response.body()
                Log.d("CommentFragment", "로그인 된 유저 확인 userId : $userId")

                val commentWriter =
                    SharedPreferencesManager.getString("id", "")
                val commentContents =
                    binding.commentContents.text.toString()
                val userImage = binding.userImage
                val commentService =
                    (context?.applicationContext as MyApplication).commentService

                if (profileDto != null) {
                    // 사용자 이미지 URL이 있다면 Glide로 이미지 로드

                    val userImage = profileDto.loginUserDto?.user_image ?: ""

                    if (!userImage.isNullOrBlank()) {
                        Glide.with(requireContext())
                            .load(userImage)
                            .override(900, 900)
                            .into(binding.userImage)

                        // 수정: userImage 변수에 이미지 URL 할당
                        this@CommentFragment.userImage = userImage
                    }

                    val commentDto = CommentDto(
                        commentWriter = commentWriter,
                        commentContents = commentContents,
                        boardId = boardId,
                        parentId = null,
                        depth = 0,
                        commentCreatedTime = "",
                        userImage = this@CommentFragment.userImage // 사용자 이미지 URL 설정
                    )


                } else {
                    Log.e("CommentFragment", "유저 정보를 받아오지 못했습니다.")
                }
            }
            // 통신 실패 시 로그 출력
            override fun onFailure(call: Call<ProfileDto>, t: Throwable) {
                t.printStackTrace()
                call.cancel()
                Log.e("CommentFragment", " 통신 실패")
            }
        })

        val boardService = (context?.applicationContext as MyApplication).boardService
        val boardDtl =
            arguments?.getString("boardId")?.let { boardService.getBoardDtl(it) }
        Log.d(TAG, "잘받았나욥 레트로핏 . boardDtl: $boardDtl")



        boardDtl?.enqueue(object : Callback<BoardDtlDto> {
            override fun onResponse(
                call: Call<BoardDtlDto>,
                response: Response<BoardDtlDto>
            ) {
                Log.d(TAG, "서버 응답 코드(BoardDtlDto아래): ${response.code()}")
                if (response.isSuccessful) {
                    val boardDto = response.body()
                    Log.d(TAG, "데이터 도착 확인.")
                    Log.d(TAG, "게시판 아이디디디디 : ${boardDto?.board?.id}")
                    Log.d(TAG, "유저아이디 : ${boardDto?.users?.user_image}")

                    // 댓글 목록 데이터 초기화


                    // 수정된 코드
                    val commentList: List<CommentDto> =
                        boardDto?.commentsPage?.content.orEmpty()
                    val layoutManager =
                        LinearLayoutManager(requireContext().applicationContext)
                    binding.commentRecyclerView.layoutManager = layoutManager

                    // CommentFragment에서 CommentAdapter2 초기화 부분
                    // 어댑터를 생성할 때 OnReplyClickListener를 전달
                    adapter =
                        CommentAdapter(this@CommentFragment, boardId, commentList)
                    binding.commentRecyclerView.adapter = adapter


                    binding.saveBtn.setOnClickListener {

                        if (boardDto != null) {
                            val boardId = boardDto.board?.id!!
                            val commentWriter =
                                SharedPreferencesManager.getString("id", "")
                            val commentContents =
                                binding.commentContents.text.toString()
                            val userImage = binding.userImage
                            val commentService =
                                (context?.applicationContext as MyApplication).commentService
                            // 안드로이드에서 CommentDto를 생성할 때 parentId를 null로 설정
                            // 댓글 등록에 필요한 정보를 담은 CommentDto 객체 생성
                            val commentDto = CommentDto(
                                commentWriter = commentWriter, // 작성자 아이디 또는 다른 정보
                                commentContents = commentContents, // 댓글 내용
                                boardId = boardId, // 게시글 ID
                                // parentId = parentId, // parentId는 대댓글을 작성할 때 사용
                                depth = 0, // 댓글 깊이, 일반 댓글은 0
                                commentCreatedTime = "",
                                userImage = ""
                            )

                            // userImage = userImage, // CommentDto 전체에 대한 사용자 이미지 (선택적)
                            // children = null // 대댓글 목록 (대댓글을 작성할 때만 사용)

                            val commentList = commentService.save(commentDto)

                            commentList.enqueue(object : Callback<Unit> {
                                override fun onResponse(
                                    call: Call<Unit?>,
                                    response: Response<Unit?>
                                ) {


                                    Log.d(
                                        "CommentFragment",
                                        "Unit아래응답코드: ${response.code()}"
                                    )
                                    if (response.isSuccessful) {
                                        val save = response.body()
                                        Log.e("CommentFragment", "savedComment: ${save}")
                                        Toast.makeText(
                                            requireContext().applicationContext,
                                            "댓글작성되었습니다요^ㅡㅡ^",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // 현재의 프래그먼트를 제거
                                        val transaction =
                                            requireActivity().supportFragmentManager.beginTransaction()
                                        transaction.remove(this@CommentFragment)
                                            .commit()

                                        // 새로운 인스턴스를 생성하여 추가
                                        val newFragment =
                                            CommentFragment.newInstance(boardId.toString())
                                        val newTransaction =
                                            requireActivity().supportFragmentManager.beginTransaction()
                                        newTransaction.add(
                                            R.id.fragmentContainer,
                                            newFragment
                                        )
                                        newTransaction.addToBackStack(null)
                                        newTransaction.commit()
                                        // TODO: 댓글 작성 성공 시의 추가 처리
                                    } else {
                                        val errorBody = response?.errorBody()?.string()
                                        Log.e("CommentFragment", "댓글 작성 실패. 에러 메시지: $errorBody")
                                        Toast.makeText(
                                            requireContext().applicationContext,
                                            "댓글 작성 실패. 에러 메시지: $errorBody",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Toast.makeText(
                                            requireContext().applicationContext,
                                            "댓글 작성 실패",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e(
                                            "CommentFragment",
                                            "댓글 작성 실패: ${response.errorBody()}"
                                        )
                                        Log.e(
                                            "CommentFragment",
                                            "댓글 작성 실패: ${
                                                response.errorBody().toString()
                                            }"
                                        )
                                        Log.d(TAG, "댓글 작성 실패. 에러 메시지:  $errorBody.")

                                    }
                                }

                                override fun onFailure(
                                    call: Call<Unit?>,
                                    t: Throwable
                                ) {
                                    // TODO: 네트워크 오류 또는 예외 발생 시의 처리
                                    Toast.makeText(
                                        requireContext().applicationContext,
                                        "댓글 작성 실패",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e(TAG, "댓글 작성 실패: ${t.message}", t)
                                }

                            })
                        }

                    }

                }
            }


            override fun onFailure(call: Call<BoardDtlDto>, t: Throwable) {
                // TODO: 네트워크 오류 또는 예외 발생 시의 처리
                Toast.makeText(
                    requireContext().applicationContext,
                    "댓글 작성 실패",
                    Toast.LENGTH_SHORT
                ).show()
                Log.e(TAG, "댓글 작성 실패: ${t.message}", t)
            }
        })




        return binding.root
    }
}



