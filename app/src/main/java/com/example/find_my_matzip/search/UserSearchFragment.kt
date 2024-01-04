package com.example.find_my_matzip.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentUserSearchBinding
import com.example.find_my_matzip.model.MainBoardUserDto
import com.example.find_my_matzip.model.UsersFormDto
import com.example.find_my_matzip.navTab.navTabFragment.MyPageFragment
import com.example.find_my_matzip.navTab.navTabFragment.ProfileFragment
import com.example.find_my_matzip.search.adapter.UserSearchResultRecyclerViewAdapter
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserSearchFragment : Fragment() {
    lateinit var binding: FragmentUserSearchBinding
    lateinit var adapter: UserSearchResultRecyclerViewAdapter
    lateinit var userList: Call<List<UsersFormDto>>

    private val TAG: String = "UserSearchFragment"

    //페이징처리 1
    var currentPage = 0

    companion object {
        fun newInstance(text: String) =
            UserSearchFragment().apply {
                arguments = Bundle().apply {
                    putString("text", text)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"UserSearchFragment onCreate")
        super.onCreate(savedInstanceState)
        binding = FragmentUserSearchBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","UserSearchFragment onCreateView")
        binding = FragmentUserSearchBinding.inflate(layoutInflater, container, false)

        //전달 받은 검색어
        val newText = arguments?.getString("text")
        Log.d(TAG, "newText : $newText")


        //user item클릭 이벤트
        adapter = UserSearchResultRecyclerViewAdapter(requireContext()).apply {
            setOnUserClickListener { userId ->
                navigateToUserProfile(userId)
            }
        }

        //초기화
        val layoutManager = LinearLayoutManager(requireContext())
        binding.userSearchRecyclerView.layoutManager = layoutManager
        binding.userSearchRecyclerView.adapter = adapter


        if(newText!!.isNotEmpty()){
            binding.userSearchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = LinearLayoutManager(requireContext())

                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount && firstVisibleItemPosition >= 0) {
                        currentPage++
                        Log.d(TAG, "currentPage : $currentPage")
                        loadNextPageData(currentPage,newText)
                    }
                }
            })
            loadNextPageData(currentPage,newText)
        }
        return binding.root
    }

    private fun loadNextPageData(page: Int,newText:String){


        val userService = (context?.applicationContext as MyApplication).userService
        userList = userService.getAllUsers(newText,page)
        userList.enqueue(object : Callback<List<UsersFormDto>>{

            override fun onResponse(call: Call<List<UsersFormDto>>, response: Response<List<UsersFormDto>>) {
                if (response.isSuccessful) {
                    val newUserList = response.body()
                    newUserList?.let {
                        adapter.addData(it)
                    }
                    if (newUserList?.isEmpty()!!&& currentPage==0) {
                        //결과값이 비었다면
                        binding.noSearch.visibility = View.VISIBLE
                        binding.noSearch.text = " \" $newText \" 검색 결과 없음"
                    }else{
                        //초기화
                        binding.noSearch.visibility = View.GONE
                        binding.noSearch.text = ""
                    }
                }
            }

            override fun onFailure(call: Call<List<UsersFormDto>>, t: Throwable) {
                t.printStackTrace()
                call.cancel()
                Log.d(TAG, " 통신 실패")
            }

        })
    }

    private fun navigateToUserProfile(userId: String?) {
        if (userId.isNullOrEmpty()) {
            Log.d(TAG, "userId is null or empty")
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


}