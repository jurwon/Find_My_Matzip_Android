package com.example.find_my_matzip.navTab.navTabFragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.find_my_matzip.ChatActivity
import com.example.find_my_matzip.MessageActivity
import com.example.find_my_matzip.R
import com.example.find_my_matzip.model.FollowDto
import com.example.find_my_matzip.model.Friend
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class FriendsFragment : Fragment() {
    companion object{
        fun newInstance() : FriendsFragment {
            return FriendsFragment()
        }
    }

    private lateinit var database: DatabaseReference
    private var friend : ArrayList<Friend> = arrayListOf()

    // 메모리에 올라갔을 때
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    // 프래그먼트를 포함하고 있는 액티비티에 붙었을 때
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    // 뷰가 생성되었을 때
    // 프래그먼트와 레이아웃을 연결시켜주는 부분
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        database = Firebase.database.reference
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycler)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()

        return view
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        init {
            val myUid = Firebase.auth.currentUser?.uid.toString()
            println(myUid)

            FirebaseDatabase.getInstance().reference.child("users").addValueEventListener(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    friend.clear()
                    for(data in snapshot.children){
                        val item = data.getValue<Friend>()
                        Log.d("Friend", "$item")
                        if(item?.uid.equals(myUid)) { continue } // 본인은 친구 창에서 제외
                        friend.add(item!!)
                    }
                    notifyDataSetChanged()
                }
            })
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.item_friends, parent, false))
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.home_item_iv)
            val textView : TextView = itemView.findViewById(R.id.home_item_tv)
            val textViewEmail : TextView = itemView.findViewById(R.id.home_item_email)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            // 유저 정보 + 확장자 불러오기
            /* Glide.with(holder.itemView.context).load(friend[position].email + ".jpg")
                .apply(RequestOptions().circleCrop())
                .into(holder.imageView) */
            holder.textView.text = friend[position].name
            holder.textViewEmail.text = friend[position].email

            holder.itemView.setOnClickListener{
                val intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("destinationUid", friend[position].uid)
                context?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return friend.size
        }
    }
}