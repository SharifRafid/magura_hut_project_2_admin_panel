package com.dubd.adminmh.view.products

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.dubd.adminmh.R
import com.dubd.adminmh.adapters.CartAdapter
import com.dubd.adminmh.models.CartItem
import com.dubd.adminmh.view.map.MapsActivity
import com.google.firebase.database.*
import com.google.firebase.ktx.Firebase
import com.shashank.sony.fancytoastlib.FancyToast
import com.shreyaspatil.MaterialDialog.MaterialDialog
import kotlinx.android.synthetic.main.activity_single_product.*
import kotlinx.android.synthetic.main.pin_dia.view.*

class SingleProductActivity : AppCompatActivity() {

    private var firstDataRoot = "pendingOrders/"
    private var secondDataRoot = "verifiedOrders"
    private var deleteOnClick = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_single_product)

        firstDataRoot = intent.getStringExtra("fRoot").toString()
        secondDataRoot = intent.getStringExtra("sRoot").toString()

        when(firstDataRoot){
            "pendingOrders/" -> {
                confirm_button.text = "কনফার্ম করুন"
                deleteOnClick = false
            }
            "verifiedOrders/" -> {
                confirm_button.text = "ডেলিভারি করুন"
                deleteOnClick = false
            }
            "deliveredOrders/" -> {
                confirm_button.text = "ডিলেট করুন"
                deleteOnClick = true
            }
        }

        val items = ArrayList<CartItem>()
        var price = 0
        val key = intent.getStringExtra("product") as String

        val databaseReference = FirebaseDatabase.getInstance().getReference(firstDataRoot+key)
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                items.clear()
                price = 0
                val name = snapshot.child("name").value.toString()
                nameTxt.text = "Name : $name"
                val addressString = snapshot.child("address").value.toString()
                addressText.text = "Address : $addressString"
                val number = snapshot.child("phone").value.toString()
                callNow.text = "কল করুন - $number"
                callNow.setOnClickListener {
                    if(callPermissionCheck(this@SingleProductActivity, this@SingleProductActivity)){
                        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number"))
                        startActivity(callIntent)
                    }
                }
                val latLang = snapshot.child("location").value.toString()
                showLocation.setOnClickListener {
                    if(latLang!=","){
                        val intentLocation = Intent(this@SingleProductActivity,MapsActivity::class.java)
                        intentLocation.putExtra("lat",latLang.split(",")[0].trim())
                        intentLocation.putExtra("lang",latLang.split(",")[1].trim())
                        startActivity(intentLocation)
                    }
                }
                for(product in snapshot.child("products").children){
                    price += (product.child("price").value.toString().trim().toInt()*product.child("count").value.toString().trim().toInt())
                    items.add(CartItem(
                        product.key.toString(),
                        product.child("name").value.toString(),
                        product.child("src").value.toString(),
                        product.child("price").value.toString().trim().toInt(),
                        product.child("count").value.toString().trim().toInt()
                    ))
                }
                wholePrice.text = price.toString()
                val adapter = CartAdapter(this@SingleProductActivity,items)
                recyclerMain.adapter = adapter
                val linearLayoutManager = LinearLayoutManager(this@SingleProductActivity)
                linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
                recyclerMain.layoutManager = linearLayoutManager
                recyclerMain.setHasFixedSize(true)
                adapter.notifyDataSetChanged()
                avi.visibility = View.GONE

                confirm_button.setOnClickListener {
                    if(deleteOnClick){
                        val dialog = Dialog(this@SingleProductActivity)
                        val view = LayoutInflater.from(this@SingleProductActivity).inflate(R.layout.pin_dia,null)
                        view.orderNow.text = "ডিলেট করুন"
                        view.orderNow.setOnClickListener {
                            if(view.address.text?.isNotEmpty()!! && view.address.text.toString().trim() == "7777"){
                                FirebaseDatabase.getInstance().getReference("users/$number/orders/$key").removeValue()
                                databaseReference.removeValue()
                                finish()
                            }else{
                                view.address.error = "পিন ঠিক করুন"
                            }
                        }
                        dialog.setContentView(view)
                        dialog.show()
                    }else{
                        finalOrderPlace(
                            key,
                            number,
                            name,
                            latLang.split(",")[0].trim(),
                            latLang.split(",")[1].trim(),
                            items,
                            addressString,
                            databaseReference
                        )
                    }
                }
                confirm_button.setOnLongClickListener {
                    val dialog = Dialog(this@SingleProductActivity)
                    val view = LayoutInflater.from(this@SingleProductActivity).inflate(R.layout.pin_dia,null)
                    view.orderNow.text = "ডিলেট করুন"
                    view.orderNow.setOnClickListener {
                        if(view.address.text?.isNotEmpty()!! && view.address.text.toString().trim() == "7777"){
                            FirebaseDatabase.getInstance().getReference("users/$number/orders/$key").removeValue()
                            databaseReference.removeValue()
                            finish()
                        }else{
                            view.address.error = "পিন ঠিক করুন"
                        }
                    }
                    dialog.setContentView(view)
                    dialog.show()
                    true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                FancyToast.makeText(this@SingleProductActivity,"সম্পন্ন করা যায়নি, কিছুক্ষন পরে আবার চেষ্টা করুন",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,false).show()
            }

        })
    }

    private fun finalOrderPlace(
        key : String,
        phone : String,
        name : String,
        lat : String,
        lang : String,
        cartItems : ArrayList<CartItem>,
        address : String,
        databaseReference: DatabaseReference
    ) {
        val orderRef = FirebaseDatabase.getInstance().getReference(secondDataRoot)
        orderRef.child(key).child("phone").setValue(phone)
        orderRef.child(key).child("name").setValue(name)
        orderRef.child(key).child("location").setValue("$lat,$lang")
        for(item in cartItems){
            orderRef.child(key).child("products").child(item.id).child("name").setValue(item.name)
            orderRef.child(key).child("products").child(item.id).child("src").setValue(item.src)
            orderRef.child(key).child("products").child(item.id).child("price").setValue(item.price.toString())
            orderRef.child(key).child("products").child(item.id).child("count").setValue(item.count.toString())
        }
        orderRef.child(key).child("address").setValue(address).addOnCompleteListener {
            if(it.isSuccessful){
                databaseReference.removeValue()
                FancyToast.makeText(this,"সফল ভাবে সম্পন্ন",
                    FancyToast.LENGTH_LONG,
                    FancyToast.SUCCESS,false).show()
                finish()
            }else{
                FancyToast.makeText(this,"সম্পন্ন করা যায়নি, কিছুক্ষন পরে আবার চেষ্টা করুন",
                    FancyToast.LENGTH_SHORT,
                    FancyToast.ERROR,false).show()
            }
        }
    }

    fun callPermissionCheck(context: Context, activity: SingleProductActivity): Boolean {
        return if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    activity,
                    Manifest.permission.CALL_PHONE
                )
            ) {
                val mDialog = MaterialDialog.Builder(activity)
                    .setTitle("কল পারমিশন প্রদান করুন")
                    .setMessage("কল করার জন্য প্রথমবারের মতো পারমিশন প্রদান করুন,")
                    .setCancelable(false)
                    .setPositiveButton(
                        "আচ্ছা", R.drawable.ic_delete
                    ) { diaInt, _ ->
                        ActivityCompat.requestPermissions(
                            activity,
                            arrayOf(Manifest.permission.CALL_PHONE),
                            0
                        )
                        diaInt.dismiss()
                    }
                    .setNegativeButton(
                        "না। থাক।",
                        R.drawable.ic_clear
                    ) { dialogInterface, _ -> dialogInterface.dismiss() }
                    .build()

                mDialog.show()
            } else {
                ActivityCompat.requestPermissions(activity,arrayOf(Manifest.permission.CALL_PHONE), 0)
            }
            false
        } else {
            true
        }
    }
}