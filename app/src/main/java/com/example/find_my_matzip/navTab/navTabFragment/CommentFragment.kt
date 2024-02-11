package com.matzip.find_my_matzip.navTab.navTabFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.MyApplication
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.FragmentCommentBinding
import com.matzip.find_my_matzip.model.BoardDtlDto
import com.matzip.find_my_matzip.model.CommentDto
import com.matzip.find_my_matzip.model.ProfileDto
import com.matzip.find_my_matzip.navTab.adapter.CommentAdapter
import com.matzip.find_my_matzip.navTab.adapter.CommentAdapterListener
import com.matzip.find_my_matzip.utils.SharedPreferencesManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class CommentFragment : BottomSheetDialogFragment(), CommentAdapterListener {

    lateinit var binding: FragmentCommentBinding
    lateinit var adapter: CommentAdapter
    private var loginUserId: String = ""
    var commentContents: EditText? = null
    var saveBtn: Button? = null
    private var userImage: String = ""
    var comment: List<CommentDto> = emptyList() // 이 부분을 추가하고 초기화


        companion object {
            const val TAG = "BottomSheetDialogFragment"

            private const val ARG_BOARD_ID = "boardId"
            private const val ARG_COMMENTS = "comments"

            fun newInstance(boardId: String?, comments: List<CommentDto> = emptyList()): CommentFragment {
                val fragment = CommentFragment()
                val args = Bundle()
                args.putString(ARG_BOARD_ID, boardId)
                args.putSerializable("comments", ArrayList(comments))
                fragment.arguments = args
                return fragment
            }
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SdoLifeCycle", "boardDtlFragment onCreate")
        super.onCreate(savedInstanceState)

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCommentBinding.inflate(layoutInflater, container, false)
        loginUserId = SharedPreferencesManager.getString("id", "")
//        commentContents = binding.commentContents
//        saveBtn = binding.saveBtn
//        boardId = arguments?.getLong("boardId") ?: 0L
        val boardId = arguments?.getLong("boardId") ?: 0L


        // 로그인한 사용자의 아이디를 가져와서 해당 사용자의 프로필 정보를 서버에서 조회
        val userId = SharedPreferencesManager.getString("id", "")
        val userService = (context?.applicationContext as MyApplication).userService

        val profileList = userService.getProfile(userId, 5)

        Log.d("CommentFragment", "profileList.enqueue 호출전 : ")


        // 유저 이미지를 위한 enqueue
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
                    adapter = CommentAdapter(this@CommentFragment, boardId, commentList.toMutableList(), null)

                    binding.commentRecyclerView.adapter = adapter

                    //댓글작성 이벤트
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
                                parentId = null,
                                depth = 0, // 댓글 깊이, 일반 댓글은 0
                                commentCreatedTime = "",
                                userImage = this@CommentFragment.userImage
                            )

                            // userImage = userImage, // CommentDto 전체에 대한 사용자 이미지 (선택적)
                            // children = null // 대댓글 목록 (대댓글을 작성할 때만 사용)

                            val commentList = commentService.save(commentDto)

                            commentList.enqueue(object : Callback<CommentDto> {
                                override fun onResponse(
                                    call: Call<CommentDto>,
                                    response: Response<CommentDto>
                                ) {
                                    if (response.isSuccessful) {
                                        val commentDto = response.body()
                                        if (commentDto != null) {
                                            Toast.makeText(
                                                requireContext().applicationContext,
                                                "댓글작성되었습니다요^ㅡㅡ^",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                            Log.d("CommentFragment", "댓글 commentDto.성공 : $commentDto")
                                            // 서버에서 받아온 commentId를 사용하여 CommentDto를 생성
                                            commentDto.commentId?.let { commentId ->
                                                val newCommentDto = CommentDto(
                                                    commentId = commentId,
                                                    commentWriter = commentDto.commentWriter,
                                                    commentContents = commentDto.commentContents,
                                                    boardId = commentDto.boardId,
                                                    parentId = commentDto.parentId,
                                                    depth = commentDto.depth,
                                                    commentCreatedTime = commentDto.commentCreatedTime,
                                                    userImage = commentDto.userImage
                                                )
                                                adapter.updateData(mutableListOf(commentDto))
                                                binding.commentContents.text.clear()

                                                Log.e("CommentFragment", "댓글 commentDto.성공 : $newCommentDto")
                                            }
                                        }
                                    } else {
                                        val errorBody = response?.errorBody()?.string()
                                        Toast.makeText(
                                            requireContext().applicationContext,
                                            "댓글 작성 실패. 에러 메시지: $errorBody",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.e("CommentFragment", "댓글 작성 실패. 에러 메시지: $errorBody")
                                    }
                                }

                                override fun onFailure(call: Call<CommentDto>, t: Throwable) {
                                    // TODO: 실패 처리 코드 추가
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


    override fun onReplyClick(comment: CommentDto, boardId: Long) {
        Log.d(TAG, "onReplyClick called for comment: $comment")

        // 댓글 작성자 태그 추가 부분을 동적으로 처리
        val commentWriterTag = if (!comment.commentWriter.isNullOrEmpty()) {
            "@${comment.commentWriter} "
        } else {
            ""
        }
        binding.commentContents.setText(commentWriterTag)
        binding.commentContents.setSelection(binding.commentContents.text.length)

        binding.saveBtn.setOnClickListener {



            // 사용자가 작성한 댓글 내용
            val replyText = binding.commentContents.text.toString()

            // 작성자 태그 확인
            val isAuthorTagPresent = replyText.contains("@${comment.commentWriter}")

            // 대댓글 작성 처리
            if (isAuthorTagPresent) {
                saveReplyComment(comment, replyText)
            } else {

            }
        }
    }


    private fun saveReplyComment(commentDto: CommentDto, replyText: String) {
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


                        val newCommentDto = CommentDto(
                            commentWriter = loginUserId,
                            commentContents = replyText,
                            boardId = boardId,
                            parentId = commentDto.commentId, // 수정된 부분
                            depth = commentDto.depth + 1,
                            commentCreatedTime = "",
                            userImage = userImage
                        )


                        val commentList = commentService.saveReply(newCommentDto, newCommentDto.commentId)
                        // commentList.enqueue에서 서버 응답으로 받은 CommentDto의 ID를 사용하여 commentDto.commentId를 업데이트
                        commentList.enqueue(object : Callback<CommentDto> {
                            override fun onResponse(call: Call<CommentDto>, response: Response<CommentDto>) {
                                if (response.isSuccessful) {
                                    val createdCommentDto = response.body()
                                    if (createdCommentDto != null) {
                                        newCommentDto.commentId = createdCommentDto.commentId

                                        // 이후의 로직은 그대로 유지
                                        Toast.makeText(
                                            requireContext().applicationContext,
                                            "대댓글작성되었습니다요^ㅡㅡ^",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        commentDto.commentId?.let { commentId ->
                                            val newCommentDto = CommentDto(
                                                commentId = commentId,
                                                commentWriter = commentDto.commentWriter,
                                                commentContents = commentDto.commentContents,
                                                boardId = commentDto.boardId,
                                                parentId = commentDto.parentId,
                                                depth = commentDto.depth,
                                                commentCreatedTime = commentDto.commentCreatedTime,
                                                userImage = commentDto.userImage
                                            )

                                            // 어댑터에 데이터 추가
                                            adapter.updateData(mutableListOf(newCommentDto))

                                            // 댓글 내용 초기화
                                            binding.commentContents.text.clear()

                                            // 어댑터에게 데이터 변경을 알림
                                            adapter.notifyDataSetChanged()

                                            // 프래그먼트를 새로고침합니다.
                                            refreshFragment()
                                        }
                                    }
                                } else {
                                    // 실패 처리 로직은 그대로 유지
                                    val errorBody = response?.errorBody()?.string()
                                    Toast.makeText(
                                        requireContext().applicationContext,
                                        "대댓글 작성 실패. 에러 메시지: $errorBody",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    Log.e("CommentFragment", "대댓글 작성 실패. 에러 메시지: $errorBody")
                                    Log.e("CommentFragment", "대댓글 commentDto. 에러 메시지: $commentDto")
                                }
                            }

                            override fun onFailure(call: Call<CommentDto>, t: Throwable) {
                                // 실패 처리 코드 추가
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
    private fun refreshFragment() {
        // 프래그먼트 트랜잭션 시작
        val transaction = requireFragmentManager().beginTransaction()

        // 현재의 프래그먼트를 제거
        transaction.remove(this)

        // 새로운 BottomSheetDialogFragment를 생성
        val newFragment = CommentFragment.newInstance(arguments?.getString("boardId") ?: "")


        // 전환 효과를 제거
//        newFragment.enterTransition = null
//        newFragment.exitTransition = null

        // 생성된 BottomSheetDialogFragment를 보여줌
        newFragment.show(requireFragmentManager(), CommentFragment.TAG)

        // 트랜잭션 커밋
        transaction.commit()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)


        // BottomSheet의 레이아웃을 가져오기
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        // BottomSheetBehavior 가져오기
        val bottomSheetBehavior = bottomSheet?.let { BottomSheetBehavior.from(it) }

        // 최초에 보여지는 높이 설정
        bottomSheetBehavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.peek_height2)

        // BottomSheetBehavior 설정
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_COLLAPSED


        // BottomSheet 상태 변경 리스너 등록
        bottomSheetBehavior?.addBottomSheetCallback(object :
            BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // 상태 변경에 따른 동작 처리
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // BottomSheet가 축소된 상태
                    }

                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // BottomSheet가 확장된 상태
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 중일 때의 동작 처리
            }
        })
    }
}
