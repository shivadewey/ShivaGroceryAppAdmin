package com.example.blinkitadmin.auth


import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.blinkitadmin.R
import com.example.blinkitadmin.databinding.FragmentSignInBinding
import com.example.blinkitadmin.models.Stores
import com.example.blinkitadmin.utils.Constants
import com.example.blinkitadmin.utils.Utils
import com.example.blinkitadmin.viewmodels.AuthViewModel

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private val viewModel : AuthViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(layoutInflater)

        setStatusBarColor()
        settingStateCityStoreType()
        onContinueButtonClicked()

        return binding.root
    }

    private fun settingStateCityStoreType() {
        val stateAdapter = ArrayAdapter(requireContext() , R.layout.showing_states_layout ,Constants.allIndianStates)
        val storeAdapter = ArrayAdapter(requireContext() , R.layout.showing_store_layout ,Constants.storesType)
        binding.apply {
            etStoreState.setAdapter(stateAdapter)
            etStoreType.setAdapter(storeAdapter)
        }
    }

    private fun onContinueButtonClicked() {
        binding.btnContinue.setOnClickListener {
            saveStoreInfo()
        }
    }

    private fun saveStoreInfo() {
        val storeOwnerName = binding.etStoreOwnerName.text.toString()
        val storeName = binding.etStoreName.text.toString()
        val storeState = binding.etStoreState.text.toString()
        val storeCity = binding.etStoreCity.text.toString()
        val storeAddress = binding.etDescriptiveAddress.text.toString()
        val storeTypes = binding.etStoreType.text.toString()
        val storeOwnerNumber = binding.etUserNumber.text.toString()
        val storeId =(1..25).map { (('A'..'Z') + ('a'..'z') + ('0'..'9')).random() }.joinToString("")
        val storeOwnerUid = " "

        if(storeOwnerName.isEmpty())  {
            Utils.hideDialog()
            binding.etStoreOwnerName.error = "Please enter your name"
        }
        else if (storeName.isEmpty()) {
            Utils.hideDialog()
            binding.etStoreName.error = "Please enter your store name"
        }
        else if (storeState.isEmpty()) {
            Utils.hideDialog()
            binding.etStoreState.error = "Please enter your store state"
        }
        else if (storeCity.isEmpty()) {
            Utils.hideDialog()
            binding.etStoreCity.error = "Please enter your store city"
        }
        else if (storeTypes.isEmpty()) {
            Utils.hideDialog()
            binding.etStoreType.error = "Please enter your store type"
        }
        else if (storeOwnerNumber.isEmpty()) {
            Utils.hideDialog()
            binding.etUserNumber.error = "Please enter your phone number"
        }
        else{
            val store = Stores(storeOwnerUid,storeId,storeOwnerName,storeName,storeState,storeCity,storeAddress,storeTypes,storeOwnerNumber)
            Log.d("ff" , store.toString())
            navigateToOtpFragment(store)
        }
    }

    private fun navigateToOtpFragment(stores: Stores) {
        binding.apply {
                val phoneNumber = binding.etUserNumber.text.toString()
                if(phoneNumber.isEmpty() || phoneNumber.length != 10){
                    Utils.showToast(requireContext() , "Please enter your valid mobile number")
                }else{
                    val bundle = Bundle()
                    Log.d("ff" , "1")

                    bundle.putString("storeOwnerUid" , stores.storeOwnerUid)
                    bundle.putString("storeId" , stores.storeId)
                    bundle.putString("storeOwnerName" , stores.storeOwnerName)
                    bundle.putString("storeName" , stores.storeName)
                    bundle.putString("storeState" , stores.storeState)
                    bundle.putString("storeCity" , stores.storeCity)
                    bundle.putString("storeAddress" , stores.storeAddress)
                    bundle.putString("storeTypes" , stores.storeTypes)
                    bundle.putString("storeOwnerNumber" , stores.storeOwnerNumber)
                    Log.d("ff" , "2")

                    findNavController().navigate(R.id.action_signInFragment_to_OTPFragment , bundle)
                }
        }
    }


    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }
}