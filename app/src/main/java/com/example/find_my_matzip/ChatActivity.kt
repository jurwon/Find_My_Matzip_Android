package com.example.find_my_matzip

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.find_my_matzip.navTab.navTabFragment.FriendsFragment
import com.example.find_my_matzip.navTab.navTabFragment.MessageFragment
import com.example.find_my_matzip.navTab.navTabFragment.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private lateinit var auth: FirebaseAuth

private lateinit var friendsFragment: FriendsFragment
private lateinit var messageFragment: MessageFragment
private lateinit var profileFragment: ProfileFragment


class ChatActivity : AppCompatActivity() {

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        auth = Firebase.auth

        val bottom_nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottom_nav.setOnNavigationItemSelectedListener(BottomNavItemSelectedListener)

        friendsFragment = FriendsFragment.newInstance()
        supportFragmentManager.beginTransaction().add(R.id.fragments_frame, friendsFragment).commit()
    }

    private val BottomNavItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener{
        when(it.itemId){
            R.id.menu_home -> {
                friendsFragment = FriendsFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, friendsFragment).commit()
            }

            R.id.menu_chat -> {
                messageFragment = MessageFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, messageFragment).commit()
            }
/*
            R.id.menu_profile -> {
                profileFragment = ProfileFragment.newInstance()
                supportFragmentManager.beginTransaction().replace(R.id.fragments_frame, profileFragment).commit()
            }
*/

        }
        true
    }
}