package com.dubd.adminmh.view.products

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.dubd.adminmh.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_admin.view.*
import kotlinx.android.synthetic.main.activity_pending_products.*
import kotlinx.android.synthetic.main.activity_sync_products.*
import kotlinx.android.synthetic.main.activity_sync_products.mainList

class DeliveredOrdersActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delivered_orders)

        val snapArray = ArrayList<String>()
        val titleArrayList = ArrayList<String>()
        val arrayAdapter = ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, titleArrayList)
        mainList.adapter = arrayAdapter

        val databaseReference = FirebaseDatabase.getInstance().getReference("deliveredOrders")
        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                avi.visibility = View.GONE
                mainList.visibility = View.VISIBLE
                snapArray.clear()
                titleArrayList.clear()
                for(snap in snapshot.children){
                    snapArray.add(snap.key.toString())
                    titleArrayList.add(snap.child("name").value.toString())
                    arrayAdapter.notifyDataSetChanged()
                }
                mainList.onItemClickListener =
                    AdapterView.OnItemClickListener { _, _, p2, _ ->
                        val intent = Intent(this@DeliveredOrdersActivity,SingleProductActivity::class.java)
                        intent.putExtra("fRoot","deliveredOrders/")
                        intent.putExtra("sRoot","deliveredOrders")
                        intent.putExtra("product", snapArray[p2])
                        startActivity(intent)
                    }
            }
            override fun onCancelled(error: DatabaseError) {
                error.toException().printStackTrace()
            }
        })


    }
}