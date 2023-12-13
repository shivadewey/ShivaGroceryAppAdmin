package com.example.blinkitadmin.auth


import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.blinkitadmin.R
import com.example.blinkitadmin.activity.AdminMainActivity
import com.example.blinkitadmin.databinding.FragmentOTPBinding
import com.example.blinkitadmin.models.Stores
import com.example.blinkitadmin.utils.Utils
import com.example.blinkitadmin.viewmodels.AuthViewModel
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch


class OTPFragment : Fragment() {

    private lateinit var binding: FragmentOTPBinding
    private var userNumber: String? = null
    private var storeId: String? = null
    private val mainViewModel: AuthViewModel by viewModels()
    private var verificationId: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentOTPBinding.inflate(layoutInflater)

        customizingEnteringOTP()

        backingToSignInFragment()

        receiveAndShowUserNumber()

        sendOTP(userNumber)

        onLogin()

        return binding.root
    }

    private fun onLogin() {
        binding.btnLogin.setOnClickListener {
            Utils.showDialog(requireContext(), "Creating your account...")
            val editTexts = arrayOf(
                binding.etOtp1, binding.etOtp2, binding.etOtp3,
                binding.etOtp4, binding.etOtp5, binding.etOtp6
            )
            val otp = editTexts.joinToString("") { it.text.toString() }
            if (otp.length < editTexts.size) {
                Toast.makeText(requireContext(), "Please enter the OTP", Toast.LENGTH_SHORT).show()
            } else {
                editTexts.forEach { it.text?.clear(); it.clearFocus() }
                verifyOTP(otp)
            }
        }
    }

    private fun verifyOTP(userOtp: String) {
        lifecycleScope.launch {
            mainViewModel.verificationId.collect {
                verificationId = it
            }
        }
        val credential = PhoneAuthProvider.getCredential(verificationId.toString(), userOtp)
        mainViewModel.apply {
            signInWithPhoneAuthCredential(credential, userNumber.toString(), storeId)
            lifecycleScope.launch {
                isSignedInSuccessful.collect {
                    if (it == true) {
                        Utils.hideDialog()
                        savingStoreInfo()
                    }
                }
            }
        }
    }

    private fun savingStoreInfo() {
        Utils.showToast(requireContext(), "Account Created...")

        Utils.showDialog(requireContext(), "Signing you...")
        val bundle = arguments
        val storeOwnerUid = bundle?.getString("storeOwnerUid")
        val storeId = bundle?.getString("storeId")
        val storeOwnerName = bundle?.getString("storeOwnerName")
        val storeName = bundle?.getString("storeName")
        val storeState = bundle?.getString("storeState")
        val storeCity = bundle?.getString("storeCity")
        val storeAddress = bundle?.getString("storeAddress")
        val storeTypes = bundle?.getString("storeTypes")
        val storeOwnerNumber = bundle?.getString("storeOwnerNumber")
        val store = Stores(
            storeOwnerUid =storeOwnerUid,
            storeId = storeId,
            storeOwnerName = storeOwnerName,
            storeName = storeName,
            storeState = storeState,
            storeCity = storeCity,
            storeAddress = storeAddress,
            storeTypes = storeTypes,
            storeOwnerNumber = storeOwnerNumber
        )

        lifecycleScope.launch {
            mainViewModel.apply {
                saveStoreInfoToFirebase(store, storeId!!)
                storeSaved.collect {
                    if (it == true) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "Successfully signed in...")
                        startActivity(Intent(requireActivity(), AdminMainActivity::class.java))
                        requireActivity().finish()
                    }
                }
            }
        }

    }

    private fun sendOTP(userNumber: String?) {
        Utils.showDialog(requireContext(), "Sending OTP")
        mainViewModel.apply {
            sendOTP(userNumber!!, requireActivity())
            lifecycleScope.launch {
                otpSent.collect {
                    if (it == true) {
                        Utils.hideDialog()
                        Utils.showToast(requireContext(), "OTP sent to your number")
                    }
                }
            }
        }
    }


    private fun backingToSignInFragment() {
        binding.tbOtpFragment.apply {
            setNavigationOnClickListener {
                findNavController().navigate(R.id.action_OTPFragment_to_signInFragment)
            }
        }
    }

    private fun customizingEnteringOTP() {
        val editTexts = arrayOf(
            binding.etOtp1,
            binding.etOtp2,
            binding.etOtp3,
            binding.etOtp4,
            binding.etOtp5,
            binding.etOtp6
        )
        for (i in editTexts.indices) {
            editTexts[i].addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1) {
                        if (i < editTexts.size - 1) {
                            editTexts[i + 1].requestFocus()
                        }
                    } else if (s?.length == 0) {
                        if (i > 0) {
                            editTexts[i - 1].requestFocus()
                        }
                    }
                }
            })
        }
    }

    private fun receiveAndShowUserNumber() {
        val bundle = arguments
        userNumber = bundle?.getString("storeOwnerNumber")
        binding.tvUserNumber.text = userNumber
    }

}