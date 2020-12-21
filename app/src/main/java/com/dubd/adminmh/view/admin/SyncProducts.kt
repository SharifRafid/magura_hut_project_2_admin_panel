package com.dubd.adminmh.view.admin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import com.dubd.adminmh.R
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_sync_products.*
import me.gilo.woodroid.Woocommerce
import me.gilo.woodroid.models.Product
import me.gilo.woodroid.models.filters.ProductFilter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.Exception

class SyncProducts : AppCompatActivity() {

    private val baseURL = "https://www.magurahut.com"
    private val consumerKey = "ck_06ecd62ab483d6578cf5e36850c77599e42e4d04"
    private val consumerSecret = "cs_ffa883a40dd6b1d1d6a8812f5e1f41a6ec8d0e04"
    private lateinit var woocommerce : Woocommerce
    var products : ArrayList<Product> = ArrayList()
    var productString : ArrayList<String> = ArrayList()
    private var pageNum = 1
    private var updated = 0
    lateinit var arrayAdapter: ArrayAdapter<String>
    lateinit var databaseReference : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sync_products)
        setTheme(R.style.MyTheme)

        databaseReference = FirebaseDatabase.getInstance().getReference("data/products")

        arrayAdapter = ArrayAdapter(this,R.layout.support_simple_spinner_dropdown_item,productString)
        mainList.adapter = arrayAdapter

        woocommerce = Woocommerce.Builder().setSiteUrl(baseURL)
            .setApiVersion(Woocommerce.API_V2)
            .setConsumerKey(consumerKey)
            .setConsumerSecret(consumerSecret)
            .build()

        preLoadProducts()
    }

    private fun preLoadProducts() {
        val productFilter = ProductFilter()
        productFilter.page = pageNum
        productFilter.per_page = 1
        loadProducts(productFilter)
    }

    private fun loadProducts(productFilter: ProductFilter){
        woocommerce.ProductRepository().products(productFilter).enqueue(object :
            Callback<List<Product>> {
            override fun onResponse(call: Call<List<Product>>, response: Response<List<Product>>) {
                try{
                    val productsResponse = response.body()
                    productsResponse?.get(0)?.let { product ->
                        products.add(product)
                        textView.text = "DATA LOADED : "+products.size.toString()
                        productString.add(product.name)
                        if(product!=null){
                            databaseReference.child(product.id.toString()).child("id").setValue(product.id.toString())
                            databaseReference.child(product.id.toString()).child("name").setValue(product.name)
                            databaseReference.child(product.id.toString()).child("shortDescription").setValue(product.short_description)
                            databaseReference.child(product.id.toString()).child("description").setValue(product.description)
                            databaseReference.child(product.id.toString()).child("averageRating").setValue(product.average_rating)
                            databaseReference.child(product.id.toString()).child("rating").setValue(product.rating_count.toString())
                            databaseReference.child(product.id.toString()).child("image").setValue(product.images[0].src)
                            databaseReference.child(product.id.toString()).child("price").setValue(product.price)
                            var cat : String = ""
                            for(i in product.categories){
                                cat += i.name+" , "
                            }
                            databaseReference.child(product.id.toString()).child("categories").setValue(cat)
                            databaseReference.child(product.id.toString()).child("isInStock").setValue(product.isIn_stock.toString()).addOnCompleteListener {
                                if(it.isSuccessful){
                                    updated ++
                                    textView2.text = "UPDATED : "+updated.toString()
                                }
                            }
                        }

                    }
                    arrayAdapter.notifyDataSetChanged()
                    pageNum ++
                    preLoadProducts()
                }catch (e : Exception){
                    e.printStackTrace()
                    textView.text = "UPDATE PROCESS STOPPED"
                }

            }
            override fun onFailure(call: Call<List<Product>>, t: Throwable) {
                t.printStackTrace()
                textView.text = "UPDATE PROCESS FAILURE"
            }
        })
    }

}