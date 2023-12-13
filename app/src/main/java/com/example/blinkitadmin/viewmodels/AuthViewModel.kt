package com.example.blinkitadmin.viewmodels

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.blinkitadmin.models.Stores
import com.example.blinkitadmin.utils.Utils
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel(){

    private val _isSignedIn = MutableStateFlow(false)
    val isSignedIn: StateFlow<Boolean> = _isSignedIn

    private val _verificationId = MutableStateFlow<String?>(null)
    val verificationId : StateFlow<String?> = _verificationId

    private val _isSignedInSuccessful  = MutableStateFlow(false)
    val  isSignedInSuccessful : StateFlow<Boolean?> = _isSignedInSuccessful

    private val _otpSent  = MutableStateFlow(false)
    val  otpSent : StateFlow<Boolean?> = _otpSent

    private val _storeSaved  = MutableStateFlow(false)
    val  storeSaved : StateFlow<Boolean?> = _storeSaved

    init {
        Utils.getAuthInstance().currentUser?.let {
            _isSignedIn.value = true
        }
    }

    fun sendOTP(userNumber : String , activity: Activity){
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {

            }
            override fun onVerificationFailed(e: FirebaseException) {
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken,
            ) {
                _verificationId.value = verificationId
                _otpSent.value = true
                Log.d("TAG" , "oncodesent$verificationId")

            }
        }
        val options = PhoneAuthOptions.newBuilder(Utils.getAuthInstance())
            .setPhoneNumber("+91$userNumber") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(activity) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential, userNumber: String, storeId: String?
    ){
        Utils.getAuthInstance().signInWithCredential(credential)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    _isSignedInSuccessful.value = true
                }
            }
    }

    suspend fun saveStoreInfoToFirebase(store : Stores, storeId : String){
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task->
            if(!task.isSuccessful) return@addOnCompleteListener
            store.storeOwnerToken = task.result.toString()
            Utils.getDatabaseInstance().getReference("Admins").child("Stores").child(Utils.getCurrentUserUid()!!).child("Store Information").setValue(store)
                .addOnCompleteListener {
                    Utils.getDatabaseInstance().getReference("Admins").child("Stores").child(Utils.getCurrentUserUid()!!).child("Store Information").child("storeOwnerUid").setValue(Utils.getCurrentUserUid()!!)
                    _storeSaved.value = true
                }

        }

    }
}