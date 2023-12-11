package com.example.find_my_matzip.navTab.navTabFragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.find_my_matzip.HomeTabActivity
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentHomeBinding
import com.example.find_my_matzip.model.MainBoardDto
import com.example.find_my_matzip.navTab.adapter.HomeRecyclerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class     HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    lateinit var adapter : HomeRecyclerAdapter
    lateinit var boardList: Call<List<MainBoardDto>>
    private val TAG: String = "HomeFragment"
    //검색
    private var text:String? = null

    //식당의 게시글 띄우는 로직
    private var resId:String? = null

    companion object {
        // HomeFragment 인스턴스 생성
        fun newInstance(text: String, resId:String): HomeFragment {
            val fragment = HomeFragment()
            val args = Bundle()
            args.putString("text", text)
            args.putString("resId", resId)
            Log.d("HomeFragment", "내가 newInstance에서 넣은 text : $text")
            Log.d("HomeFragment", "내가 newInstance에서 넣은 resId : $resId")
            fragment.arguments = args
            return fragment
        }
    }



    //페이징처리 1
    var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SdoLifeCycle","HomeFragment onCreate")
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        Log.d("SdoLifeCycle","HomeFragment onCreateView")

        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)


        //검색창에서 넘어왔다면 text넣어줌
        val newText = arguments?.getString("text")
        Log.d(TAG, "newText : $newText")
        if(newText != null){
            text = newText
        }

        //식당상세페이지에서 넘어왔다면
        val check = arguments?.getString("resId")
        if(check != null){
            resId = check
        }

        Log.d("HomeFragment","text : ${text}")
        Log.d("HomeFragment","resId : ${resId}")

        binding.toFollowHome.setOnClickListener {
            // 클릭 시 HomeFollowFragment로 이동하는 코드
            val fragment = HomeFollowFragment()

            // 트랜잭션에 이름 부여
            val transaction = parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                //    .addToBackStack("HomeFragment")
                .commit()

            // 현재의 HomeFragment를 백 스택에서 제거
            parentFragmentManager.popBackStack("HomeFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

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


        //검색창 관리
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                // 검색 버튼 누를 때 호출
                if (!query.isNullOrBlank()) {
                    Log.d(TAG, "검색 버튼 클릭: $query")

                    //검색 수행
                    navigateSearchResult(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색창에서 글자가 변경이 일어날 때마다 호출
                //Log.d(TAG, "글자 변경 : $newText")


                return true
            }
        })



        return binding.root
    }

    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","HomeFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","HomeFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","HomeFragment onDestroy")
        super.onDestroy()
    }

    private fun navigateToBoardDetail(boardId: String) {
        val fragment = boardDtlFragment.newInstance(boardId)
        parentFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .addToBackStack("HomeFragment")
            .commit()
    }

    private fun loadNextPageData(page: Int) {
        val boardService = (context?.applicationContext as MyApplication).boardService

        if(text != null && resId.isNullOrEmpty()){
            // 검색 단어가 있을때
            Log.d(TAG, "검색중 $text")
            boardList = boardService.getSearchMainBoards("$text",page)
        }else if(text.isNullOrEmpty() && resId != null){
            //식당에서 넘어왔다면
            Log.d(TAG, "식당 리뷰보기 resId : $resId")
            boardList = boardService.getSearchResBoards(resId!!,page)
        } else{
            //전체 조회
            Log.d(TAG, "전체 조회")
            boardList = boardService.getAllBoardsPager(page)
        }

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
                Log.d(TAG, " 통신 실패")
            }


        })
    }


    //fragment전환
    private fun navigateSearchResult(text:String) {
        val changeFragment = newInstance(text,"")
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentContainer, changeFragment)
        .addToBackStack(null)
        transaction.commit()
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


//페이징 처리전 코드
//        val boardList = boardService.getAllBoards()
//        Log.d("kkt", "boardList.enqueue 호출전 : ")
//        boardList.enqueue(object : Callback<List<MainBoardDto>> {
//            override fun onResponse(
//                call: Call<List<MainBoardDto>>,
//                response: Response<List<MainBoardDto>>
//            ) {
//                Log.d("kkt", "도착 확인1: ")
//                val boardList = response.body()
//                Log.d("kkt", "도착 확인2: restaurantList ${boardList}")
//                if (boardList != null && boardList.isNotEmpty()) {
//                    val firstBoard = boardList[0]
//                    val layoutManager = LinearLayoutManager(requireContext())
//                    binding.homeRecyclerView.layoutManager = layoutManager
//                    adapter = HomeRecyclerAdapter(this@HomeFragment,boardList)
//                    binding.homeRecyclerView.adapter = adapter
//                } else {
//                    Log.e("kkt", "Response body is null or empty.")
//
//                }
//            }

