package com.example.blinkitadmin.models


import android.net.Uri
import androidx.annotation.NonNull





data class CartProducts(
    val productId : String  = "random", // cant apply nullability check here.
    val productTitle : String ? = null,
    val productQuantity : String ? = null,
    val productPrice : String ? = null,
    var productCount : Int ? = null,
    var productStock : Int ? = null,
    var productImage : String ? = null,
    var productCategory : String ? = null,

)