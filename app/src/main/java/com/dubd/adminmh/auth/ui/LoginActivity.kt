package com.dubd.adminmh.auth.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import com.dubd.adminmh.R
import com.dubd.adminmh.view.admin.AdminActivity
import com.google.firebase.auth.FirebaseAuth
import com.shashank.sony.fancytoastlib.FancyToast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setTheme(R.style.MyTheme)

        firebaseAuth = FirebaseAuth.getInstance()

        if(firebaseAuth.currentUser!=null){
            startActivity(Intent(this, AdminActivity::class.java))
            finish()
        }else{
            username.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    checkValidity()
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })
            password.addTextChangedListener(object : TextWatcher{
                override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                }

                override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    checkValidity()
                }

                override fun afterTextChanged(p0: Editable?) {
                }

            })

            login.setOnClickListener {
                logIn(username.text.toString(),password.text.toString())
            }
        }


    }

    private fun logIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {
            if(it.isSuccessful){
                startActivity(Intent(this, AdminActivity::class.java))
                FancyToast.makeText(this,"লগ ইন করা হয়েছে",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.SUCCESS,false).show()
                finish()
            }else {
                FancyToast.makeText(this,"লগ ইন করা সম্ভব হয় নি",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,false).show()
            }
        }
    }

    private fun checkValidity() {
        login.isEnabled = username.text.toString().isNotEmpty() && password.text.toString().isNotEmpty()
    }
}