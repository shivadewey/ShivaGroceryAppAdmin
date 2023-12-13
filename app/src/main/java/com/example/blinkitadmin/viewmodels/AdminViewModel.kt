package com.example.blinkitadmin.viewmodels

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.blinkitadmin.api.ApiUtilities
import com.example.blinkitadmin.models.CartProducts
import com.example.blinkitadmin.models.Orders
import com.example.blinkitadmin.models.Product
import com.example.blinkitadmin.models.ProductType
import com.example.blinkitadmin.models.Stores
import com.example.blinkitadmin.models.notification.Notification
import com.example.blinkitadmin.models.notification.NotificationData
import com.example.blinkitadmin.utils.Utils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AdminViewModel : ViewModel() {

    private val _isImagesUploaded = MutableStateFlow(false)
    var isImagesUploaded: StateFlow<Boolean> = _isImagesUploaded

    private val _isProductSaved = MutableStateFlow(false)
    var isProductSaved: StateFlow<Boolean> = _isProductSaved

    private val _downloadedUrls = MutableStateFlow<ArrayList<String?>>(arrayListOf())
    var downloadedUrls: StateFlow<ArrayList<String?>> = _downloadedUrls

    private val _productList = MutableStateFlow<ArrayList<Product?>>(arrayListOf())
    var productList = _productList

    suspend fun savingImagesToFirebaseStorage(imageUris: ArrayList<Uri>) {
        val downloadUrls = ArrayList<String?>()
        imageUris.forEach { uri ->
            val imageRef = Utils.getFirebaseStorageInstance().child(Utils.getCurrentUserUid()!!)
                .child("images/${UUID.randomUUID()}")
            imageRef.putFile(Uri.parse(uri.toString())).continueWithTask {
                    imageRef.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUrl = task.result.toString()
                        downloadUrls.add(downloadUrl)
                        if (downloadUrls.size == imageUris.size) {
                            _downloadedUrls.value = downloadUrls
                            _isImagesUploaded.value = true
                        }
                    }
                }.await()
        }
    }

    suspend fun savingProduct(product: Product, randomId: String) {
        Utils.getDatabaseInstance().getReference("Admins/Stores/${Utils.getCurrentUserUid()}/Products").child(randomId).setValue(product).addOnCompleteListener {
            Utils.getDatabaseInstance().getReference("Admins/ProductCategory/${product.productCategory}/Products").child(randomId).setValue(product).addOnCompleteListener {
                Utils.getDatabaseInstance().getReference("Admins/ProductType/${product.productType}/Products").child(randomId).setValue(product).addOnCompleteListener {
                    Utils.getDatabaseInstance().getReference("Admins/AllProducts").child(randomId).setValue(product).addOnCompleteListener {
                        Utils.getDatabaseInstance().getReference("Admins/StoreType/${product.storeType}/Products").child(randomId).setValue(product).addOnSuccessListener {
                                                _isProductSaved.value = true
                                            }
                                    }
                            }
                    }
            }.await()
    }

    fun fetchingAllProducts(categories: String): Flow<List<Product>> = callbackFlow {
        val databaseReference = Utils.getDatabaseInstance()
            .getReference("Admins/Stores/${Utils.getCurrentUserUid()}/Products")
        val valueEventListener =
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val productList = ArrayList<Product?>()
                    for (products in snapshot.children) {
                        val product = products.getValue(Product::class.java)
                        if (categories == "All" || product?.productCategory == categories) {
                            productList.add(product)
                        }
                    }
                    trySend(productList.filterNotNull()).isSuccess
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            })
        awaitClose {
            databaseReference.removeEventListener(valueEventListener)
        }
    }


    fun fetchingOrderedProducts(): Flow<ArrayList<Orders>> = callbackFlow {

        val db =
            Utils.getDatabaseInstance().getReference("Admins/Orders").orderByChild("orderStatus")
        // save orders in users node also
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentOrderList = ArrayList<Orders>()
                for (orders in snapshot.children) {
                    val order = orders.getValue(Orders::class.java)
                    currentOrderList.add(order!!)
                }
                Log.d("orders", "try send")
                trySend(currentOrderList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        db.addValueEventListener(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun getOrderedItems(orderId: String): Flow<List<CartProducts>?> = callbackFlow {
        val db = Utils.getDatabaseInstance().getReference("Admins/Orders/${orderId}")
        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cartProducts = snapshot.getValue(Orders::class.java)
                trySend(cartProducts?.orderList)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        db.addListenerForSingleValueEvent(eventListener)
        awaitClose { db.removeEventListener(eventListener) }
    }

    fun updateOrderStatus(orderId: String, orderStatus: Int) {
        Utils.getDatabaseInstance().getReference("Admins/Orders/${orderId}").child("orderStatus")
            .setValue(orderStatus)

//        val eventListener = object : ValueEventListener{
//            override fun onDataChange(snapshot: DataSnapshot) {
//                val cartProducts = snapshot.getValue(Orders::class.java)
//                db.child("orderStatus").setValue(orderStatus)
//                trySend(cartProducts?.orderStatus!!)
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                close(error.toException())
//            }
//        }
//        db.addValueEventListener(eventListener)
//        awaitClose{db.removeEventListener(eventListener)}
    }

    fun sendNotification(orderId: String, message: String, title: String) {
        val getToken = Utils.getDatabaseInstance().getReference("Admins/Orders/${orderId}").get()

        getToken.addOnSuccessListener {
            val orderDetail = it.getValue(Orders::class.java)
            val userId = orderDetail?.orderingUserId
            Log.d("send", userId.toString())

            val userToken =
                Utils.getDatabaseInstance().getReference("Users/${userId}").child("userToken").get()
            userToken.addOnSuccessListener { userToken ->
                val userTokens = userToken.getValue(String::class.java)
                Log.d("send", userTokens.toString())
                val notification = Notification(userTokens, NotificationData(title, message))
                ApiUtilities.notificationApi.sendNotification(notification)
                    .enqueue(object : Callback<Notification> {
                        override fun onResponse(
                            call: Call<Notification>, response: Response<Notification>
                        ) {
                            Log.d("send", "Notification sent")
                        }

                        override fun onFailure(call: Call<Notification>, t: Throwable) {
                            TODO("Not yet implemented")
                        }

                    })
            }

        }
    }

    fun updateEditedProducts(product: Product) {
        Utils.getDatabaseInstance().getReference("Admins/Stores/${Utils.getCurrentUserUid()}/Products").child(product.productRandomId!!).setValue(product)
        Utils.getDatabaseInstance().getReference("Admins/ProductCategory/${product.productCategory}/Products").child(product.productRandomId!!).setValue(product)
        Utils.getDatabaseInstance().getReference("Admins/AllProducts").child(product.productRandomId!!).setValue(product)
        }

    fun fetchProductTypes(): Flow<List<ProductType>> = callbackFlow {
        val db = Utils.getDatabaseInstance().getReference("Admins/ProductType")

        val eventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val productCategories = ArrayList<ProductType>()
                for (categorySnapshot in snapshot.children) {
                    val categoryName = categorySnapshot.key
                    val products = categorySnapshot.child("Products")
                    val productList = ArrayList<Product>()

                    for(allProducts in products.children){
                        val product = allProducts.getValue(Product::class.java)
                        productList.add(product!!)
                    }

                    val productCategory = ProductType(categoryName,productList)
                    productCategories.add(productCategory)
                }
                trySend(productCategories)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        db.addValueEventListener(eventListener)

        // Use awaitClose with a lambda for cleanup when the flow is cancelled
        awaitClose { db.removeEventListener(eventListener) }
    }

}