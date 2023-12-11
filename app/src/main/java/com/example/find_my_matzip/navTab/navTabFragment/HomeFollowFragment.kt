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
import com.example.find_my_matzip.databinding.FragmentHomeFollowBinding
import com.example.find_my_matzip.model.MainBoardDto
import com.example.find_my_matzip.navTab.adapter.HomeFollowRecyclerAdapter
import com.example.find_my_matzip.navTab.adapter.HomeRecyclerAdapter
import com.example.find_my_matzip.navTab.adapter.RecyclerItem2
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeFollowFragment : Fragment() {
    lateinit var binding: FragmentHomeFollowBinding
    lateinit var adapter : HomeRecyclerAdapter

    //페이징처리 1
    var currentPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SdoLifeCycle","FollowFragment onCreate")
        super.onCreate(savedInstanceState)
        binding = FragmentHomeFollowBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","FollowFragment onCreateView")
        binding = FragmentHomeFollowBinding.inflate(layoutInflater, container, false)

//        binding.homeRecyclerView.apply {
//            adapter = HomeFollowRecyclerAdapter().build(items)
//            layoutManager =
//                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
//        }

        binding.toHome.setOnClickListener {
            // 클릭 시 HomeFragment로 이동하는 코드
            val fragment = HomeFragment()

            // 트랜잭션에 이름 부여
            val transaction = parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                //  .addToBackStack("HomeFollowFragment")
                .commit()

            // 현재의 HomeFollowFragment를 백 스택에서 제거
            parentFragmentManager.popBackStack("HomeFollowFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        //어댑터 연결
        adapter = HomeRecyclerAdapter(requireContext()).apply {
            setOnItemClickListener { boardId ->
                navigateToBoardDetail(boardId)
            }
        }

        val layoutManager = LinearLayoutManager(requireContext())
        binding.homeRecyclerView.layoutManager = layoutManager

        binding.homeRecyclerView.adapter = adapter

        binding.homeRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
    }

    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","FollowFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","FollowFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","FollowFragment onDestroy")
        super.onDestroy()
    }

    private fun navigateToBoardDetail(boardId: String) {
        val fragment = boardDtlFragment.newInstance(boardId)
        parentFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    //팔로우한 사람들의 게시글 데이터 가져오기
    private fun loadNextPageData(page: Int) {
        val boardService = (context?.applicationContext as MyApplication).boardService
        val boardList = boardService.getMatjalalBoards(page)

        boardList.enqueue(object : Callback<List<MainBoardDto>> {
            override fun onResponse(
                call: Call<List<MainBoardDto>>,
                response: Response<List<MainBoardDto>>
            ) {
                if (response.isSuccessful) {
                    val newBoardList = response.body()
                    newBoardList?.let {
                        adapter.addData(it)
                    }
                }
            }

            override fun onFailure(call: Call<List<MainBoardDto>>, t: Throwable) {
                t.printStackTrace()
                call.cancel()
                Log.e("kkt", " 통신 실패")
            }
        })
    }

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


}