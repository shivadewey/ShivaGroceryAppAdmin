package com.example.blinkitadmin.models

import java.util.UUID

data class Product(
    val id : String = UUID.randomUUID().toString(),
    var productRandomId : String ? = null,
    var productTitle : String ? = null,
    var productQuantity : String ? = null,
    var productUnit : String ? = null,
    var productPrice : String ? = null,
    var productStock : String ? = null,
    var productCategory : String ? = null,
    var productType : String ? = null,
    var itemCount : String ? = null,
    var storeType : String ? = null,
    var storeOwnerUid : String ? = null,
    var productImageUris : ArrayList<String?> ? = null,
)