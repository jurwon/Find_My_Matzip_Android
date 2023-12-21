package com.example.find_my_matzip.navTab.navTabFragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.find_my_matzip.HomeTabActivity
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentNewHomeBinding
import com.example.find_my_matzip.model.NewMainBoardDto
import com.example.find_my_matzip.navTab.adapter.NewHomeRecyclerAdapter
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NewHomeFragment : Fragment() {
    lateinit var binding : FragmentNewHomeBinding
    lateinit var adapter: NewHomeRecyclerAdapter
    lateinit var boardList: Call<List<NewMainBoardDto>>
    private val TAG : String = "NewHomeFragment"
    //페이징처리 1
    var currentPage = 0

    //식당의 게시글 띄우는 로직
    private var resId:String? = null
    companion object {
        // HomeFragment 인스턴스 생성
        fun newInstance(text: String, resId:String): NewHomeFragment {
            val fragment = NewHomeFragment()
            val args = Bundle()
            args.putString("text", text)
            args.putString("resId", resId)
            Log.d("HomeFragment", "내가 newInstance에서 넣은 text : $text")
            Log.d("HomeFragment", "내가 newInstance에서 넣은 resId : $resId")
            fragment.arguments = args
            return fragment
        }
    }

        override fun onCreate(savedInstanceState: Bundle?) {
            Log.d(TAG,"NewHomeFragment onCreate")
        super.onCreate(savedInstanceState)
            binding = FragmentNewHomeBinding.inflate(layoutInflater)
    }// 온크리트의 끝

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"NewHomeFragment onCreateView")

        binding = FragmentNewHomeBinding.inflate(layoutInflater,container,false)
//        val view = binding.root


        adapter = NewHomeRecyclerAdapter(requireContext()).apply {
            setOnItemClickListener { boardId ->
                navigateToBoardDetail(boardId)
            }
            setOnUserClickListener { userId ->
                navigateToUserProfile(userId)
            }
        }

        adapter.setOnItemClickListener {boardId ->
            navigateToBoardDetail(boardId)
        }

        binding.toFollowHome.setOnClickListener {
            // 클릭 시 HomeFollowFragment로 이동하는 코드
            val fragment = NewHomeFollowFragment()

            // 트랜잭션에 이름 부여
            val transaction = parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                //    .addToBackStack("HomeFragment")
                .commit()

            // 현재의 HomeFragment를 백 스택에서 제거
            parentFragmentManager.popBackStack("NewHomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }


        val layoutManager = LinearLayoutManager(requireContext())
        binding.newHomeRecyclerView.layoutManager = layoutManager

        binding.newHomeRecyclerView.adapter = adapter

        binding.newHomeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                    currentPage++
                    loadNextPageData(currentPage)
                }
            }
        })
        loadNextPageData(currentPage)

        return binding.root
    }//온크리트뷰의 끝

    private fun navigateToBoardDetail(boardId: String) {
        val fragment = boardDtlFragment.newInstance(boardId)
        parentFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .addToBackStack("HomeFragment")
            .commit()
    }//navigateToBoardDetail 끝

    private fun navigateToUserProfile(userId: String?) {
        if (userId.isNullOrEmpty()) {
            Log.d("메인프로필->페이지이동", "userId is null or empty")
            return
        }

        val transaction = parentFragmentManager.beginTransaction()
        val userFrag: Fragment = if (SharedPreferencesManager.getString("id", "") == userId) {
            MyPageFragment.newInstance(userId)
        } else {
            ProfileFragment.newInstance(userId)
        }

        transaction.replace(R.id.fragmentContainer, userFrag)
        transaction.addToBackStack(null)
        transaction.commit()
    }
    private fun loadNextPageData(page: Int) {
        val boardService = (context?.applicationContext as MyApplication).boardService
        boardList = boardService.getNewAllBoardsPager(page)
        boardList.enqueue(object : Callback<List<NewMainBoardDto>> {
            override fun onResponse(call: Call<List<NewMainBoardDto>>, response: Response<List<NewMainBoardDto>>) {
                if (response.isSuccessful) {
                    val newBoardList = response.body()
                    newBoardList?.let {
                        adapter.addData(it)
                    }
                }
            }

            override fun onFailure(call: Call<List<NewMainBoardDto>>, t: Throwable) {
                t.printStackTrace()
                call.cancel()
                Log.d(TAG, " 통신 실패")
            }
        })
    } //loadNextPageData의 끝
    fun showExitDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Exit?")
        builder.setMessage("앱을 종료하시겠습니까?")
        builder.setNegativeButton("아니오") { dialog, which ->
            // 아무 작업도 수행하지 않음
        }
        builder.setPositiveButton("예") { dialog, which ->
            // 프래그먼트가 호스트하는 액티비티의 onBackPressed() 호출
            (requireActivity() as? HomeTabActivity)?.onBackPressed2()
        }
        builder.show()
    }

}//프래그먼트의 끝