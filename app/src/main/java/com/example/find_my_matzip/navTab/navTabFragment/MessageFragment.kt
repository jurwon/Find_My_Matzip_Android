package com.example.find_my_matzip.navTab.navTabFragment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.find_my_matzip.MessageActivity
import com.example.find_my_matzip.R
import com.example.find_my_matzip.model.Friend
import com.example.find_my_matzip.model.MessageModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.ktx.Firebase

class MessageFragment : Fragment() {
    companion object{
        fun newInstance(): MessageFragment {
            return MessageFragment()
        }
    }
    private val fireDatabase = FirebaseDatabase.getInstance().reference

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
    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_message, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.messagefragment_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()

        return view
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        private val MessageModel = ArrayList<MessageModel>()
        private var uid : String? = null
        private val destinationUsers : ArrayList<String> = arrayListOf()

        init {
            uid = Firebase.auth.currentUser?.uid.toString()
            println(uid)

            fireDatabase.child("chatrooms").orderByChild("users/$uid").equalTo(true).addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    MessageModel.clear()
                    for(data in snapshot.children){
                        MessageModel.add(data.getValue<MessageModel>()!! as MessageModel)
                        println(data)
                    }
                    notifyDataSetChanged()
                }

            })

        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {


            return CustomViewHolder(LayoutInflater.from(context).inflate(R.layout.item_message, parent, false))
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.message_item_imageview)
            val textView_title : TextView = itemView.findViewById(R.id.message_textview_title)
            val textView_lastMessage : TextView = itemView.findViewById(R.id.message_item_textview_lastmessage)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            var destinationUid: String? = null
            // 채팅방에 있는 유저 모두 체크
            for (user in MessageModel[position].users.keys) {
                if (!user.equals(uid)) {
                    destinationUid = user
                    destinationUsers.add(destinationUid)
                }
            }
            fireDatabase.child("users").child("$destinationUid").addListenerForSingleValueEvent(object :
                ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }


                override fun onDataChange(snapshot: DataSnapshot) {
                    val friend = snapshot.getValue<Friend>()
                    /*
                    Glide.with(holder.itemView.context).load(friend?.profileImageUrl)
                        .apply(RequestOptions().circleCrop())
                        .into(holder.imageView) */
                    holder.textView_title.text = friend?.name

                }
            })

            /*
            // 메시지 내림차순 정렬 후, 마지막 메세지의 키값 가지기
            val commentMap = TreeMap<String, MesssageModel.Comment>(reverseOrder())
            commentMap.putAll(MessageModel[position].comments)
            val lastMessageKey = commentMap.keys.toTypedArray()[0]
            holder.textView_lastMessage.text = MessageModel[position].comments[lastMessageKey]?.message

             */

            // 채팅창 선책 시 이동
            holder.itemView.setOnClickListener {
                val intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("destinationUid", destinationUsers[position])
                context?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return MessageModel.size
        }
    }
}