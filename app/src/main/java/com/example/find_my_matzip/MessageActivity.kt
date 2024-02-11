package com.matzip.find_my_matzip

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.matzip.find_my_matzip.databinding.ActivityMesssageBinding
import com.matzip.find_my_matzip.model.Friend
import com.matzip.find_my_matzip.model.MessageModel
import com.matzip.find_my_matzip.utils.SharedPreferencesManager

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
// ktx 부분 수정
import com.google.firebase.auth.auth
import com.google.firebase.database.getValue
import com.google.firebase.Firebase
import java.text.SimpleDateFormat
import java.util.Date


class MessageActivity : AppCompatActivity() {

    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var chatRoomUid : String? = null
    private var destinationUid : String? = null
    private var uid : String? = null
    private var recyclerView : RecyclerView? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {

        lateinit var binding: ActivityMesssageBinding

        binding = ActivityMesssageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        super.onCreate(savedInstanceState)

        val imageView = findViewById<ImageView>(R.id.messageActivity_ImageView)
        val editText = findViewById<TextView>(R.id.messageActivity_editText)


        // 메시지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월 dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()
        val readable = false

        destinationUid = intent.getStringExtra("destinationUid")
        // uid = Firebase.auth.currentUser?.uid.toString()
        uid = SharedPreferencesManager.getString("id", "").split("@")[0]
        recyclerView = findViewById(R.id.messageActivity_recyclerview)

        imageView.setOnClickListener {
            Log.d("클릭 시 dest", "$destinationUid")
            val messageModel = MessageModel()
            messageModel.users.put(uid.toString(), true)
            messageModel.users.put(destinationUid!!, true)

            val comment = MessageModel.Comment(uid, editText.text.toString(), curTime, readable.toString())

            if(chatRoomUid == null){
                imageView.isEnabled = false
                fireDatabase.child("chatrooms").push().setValue(messageModel).addOnSuccessListener {
                    // 채팅방 생성
                    checkChatRoom()
                    // 메시지 보내기
                    Handler(Looper.getMainLooper()).postDelayed({
                        println(chatRoomUid)
                        fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments").push().setValue(comment)
                        val messageActivity_editText = findViewById<EditText>(R.id.messageActivity_editText)
                        messageActivity_editText.text = null
                    }, 1000L)
                    Log.d("chatUidNull dest", "$destinationUid")
                }
            }else{
                fireDatabase.child("chatrooms").child(chatRoomUid.toString()).
                child("comments").push().setValue(comment)
                val messageActivity_editText = findViewById<EditText>(R.id.messageActivity_editText)
                messageActivity_editText.text = null
                Log.d("chatUidNotNull dest", "$destinationUid")
            }
        }
        checkChatRoom()
    }


        private fun checkChatRoom(){
        fireDatabase.child("chatrooms").orderByChild("users/$uid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children){
                        println(item)
                        val messageModel = item.getValue<MessageModel>()
                        if(messageModel?.users!!.containsKey(destinationUid)){
                            chatRoomUid = item.key
                            val messageActivity_ImageView = findViewById<ImageView>(R.id.messageActivity_ImageView)
                            messageActivity_ImageView.isEnabled = true
                            recyclerView?.layoutManager = LinearLayoutManager(this@MessageActivity)
                            recyclerView?.adapter = RecyclerViewAdapter()
                        }
                    }
                }
            })
    }


    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>() {

        private val comments = ArrayList<MessageModel.Comment>()
        private var friend : Friend? = null
        init{
            fireDatabase.child("users").child(destinationUid.toString()).
            addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    friend = snapshot.getValue<Friend>()
                    val messageActivity_textView_topName = findViewById<TextView>(R.id.messageActivity_textView_topName)
                    messageActivity_textView_topName.text = friend?.name
                    getMessageList()
                }
            })
        }

        fun getMessageList(){
            fireDatabase.child("chatrooms").child(chatRoomUid.toString()).
            child("comments").addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    comments.clear()
                    for(data in snapshot.children){
                        val item = data.getValue<MessageModel.Comment>()
                        comments.add(item!!)
                        println(comments)
                    }
                    notifyDataSetChanged()

                    // 메시지를 보낼 때, 화면을 맨 밑으로 내리기
                    recyclerView?.scrollToPosition(comments.size - 1)
                }
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view : View = LayoutInflater.from(parent.context).
            inflate(R.layout.item_message_time, parent, false)

            return MessageViewHolder(view)
        }
        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

            holder.textView_message.textSize = 20F
            holder.textView_message.text = comments[position].message
//            Toast.makeText(this@MessageActivity, comments[position].toString() +"    ", Toast.LENGTH_SHORT).show()
            holder.textView_time.text = comments[position].time

            if (comments[position].readable == false){
                holder.read_counter.text = "1"
            }else{
                holder.read_counter.text = ""
            }

            if(comments[position].uid.equals(uid)){

                // 본인 채팅
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
//                holder.read_counter.text = ""

            }else{


                /*
                // 상대방 채팅
                Glide.with(holder.itemView.context)
                    .load(friend?.profileImageUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(holder.imageView_profile) */ 

                holder.textView_name.text = friend?.name
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_name.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.layout_main.gravity = Gravity.LEFT

                // uid 채팅방 내의 메시지 모두 불러오기 -> 해당 메시지 중, 메시지 정보에 있는 유저 아이디와 현재 아이디 비교
                fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments").addValueEventListener(object : ValueEventListener{
                    override fun onCancelled(error: DatabaseError) {
                    }
                    override fun onDataChange(snapshot: DataSnapshot) {
                        for(data in snapshot.children){
                            val item = data.getValue<MessageModel.Comment>()
                            if (item != null) {
                                if(item.uid != uid) {
                                    if (item.readable == false) {
                                        // 상대방 채팅 중, 해당 채팅의 readable 정보가 false 이면 -> 읽음 수헹, item.readable == true로 바뀜
                                        fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments").child(data.key.toString()).child("readable").setValue(true)
                                    }
                                }
                            }

                        }
                    }
                })
            }
        }

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            val textView_message: TextView = view.findViewById(R.id.messageItem_textView_message)
            val textView_name: TextView = view.findViewById(R.id.messageItem_textview_name)
            val imageView_profile: ImageView = view.findViewById(R.id.messageItem_imageview_profile)
            val layout_destination: LinearLayout = view.findViewById(R.id.messageItem_layout_destination)
            val layout_main: LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time : TextView = view.findViewById(R.id.messageItem_textView_time)
            val read_counter : TextView = view.findViewById(R.id.read_counter)
        }

        override fun getItemCount(): Int {
            return comments.size
        }

    }
}