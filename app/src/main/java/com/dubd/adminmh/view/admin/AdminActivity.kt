package com.dubd.adminmh.view.admin

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.dubd.adminmh.R
import com.dubd.adminmh.view.products.DeliveredOrdersActivity
import com.dubd.adminmh.view.products.PendingProductsActivity
import com.dubd.adminmh.view.products.VerifiedProductsActivity
import com.google.firebase.auth.FirebaseAuth
import com.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_admin.*
import kotlinx.android.synthetic.main.pin_dia.view.*

class AdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)
        setTheme(R.style.MyTheme)

        if(FirebaseAuth.getInstance().currentUser?.email.toString().contains("admin")){
            updateProduct.visibility = View.VISIBLE
            noPermission.visibility = View.VISIBLE
            verifiedOrder.visibility = View.VISIBLE
            verifyOrder.visibility = View.VISIBLE
        }else{
            updateProduct.visibility = View.VISIBLE
            noPermission.visibility = View.GONE
            verifiedOrder.visibility = View.VISIBLE
            verifyOrder.visibility = View.GONE
        }
        onClicks()
    }

    private fun onClicks() {
        updateProduct.setOnClickListener {
            val dialog = Dialog(this)
            val view = LayoutInflater.from(this).inflate(R.layout.pin_dia,null)
            view.orderNow.setOnClickListener {
                if(view.address.text?.isNotEmpty()!! && view.address.text.toString().trim() == "7777"){
                    startActivity(Intent(this, SyncProducts::class.java))
                    dialog.dismiss()
                }else{
                    view.address.error = "পিন ঠিক করুন"
                }
            }
            dialog.setContentView(view)
            dialog.show()
        }

        noPermission.setOnClickListener {
            val dialog = Dialog(this)
            val view = LayoutInflater.from(this).inflate(R.layout.pin_dia,null)
            view.orderNow.setOnClickListener {
                if(view.address.text?.isNotEmpty()!! && view.address.text.toString().trim() == "7777"){
                    startActivity(Intent(this, PendingProductsActivity::class.java))
                    dialog.dismiss()
                }else{
                    view.address.error = "পিন ঠিক করুন"
                }
            }
            dialog.setContentView(view)
            dialog.show()
        }

        verifiedOrder.setOnClickListener {
            startActivity(Intent(this, VerifiedProductsActivity::class.java))
        }

        completedOrder.setOnClickListener {
            startActivity(Intent(this, DeliveredOrdersActivity::class.java))
        }

        backImage.setOnClickListener {
            finish()
        }

        deleteImage.setOnClickListener {
            val mDialog = MaterialDialog.Builder(this)
                .setTitle("আপনি কি নিশ্চিত ?")
                .setMessage("আপনি আপনার অ্যাকাউন্ট থেকে লগ আউট করতে যাচ্ছেন")
                .setCancelable(false)
                .setPositiveButton(
                    "লগ আউট !", R.drawable.ic_delete
                ) { diaInt, _ ->
                    FirebaseAuth.getInstance().signOut()
                    diaInt.dismiss()
                    finish()
                }
                .setNegativeButton(
                    "না। থাক।",
                    R.drawable.ic_clear
                ) { dialogInterface, _ -> dialogInterface.dismiss() }
                .build()

            mDialog.show()
        }


    }

}