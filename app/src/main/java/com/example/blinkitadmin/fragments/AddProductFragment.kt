package com.example.blinkitadmin.fragments

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkitadmin.R
import com.example.blinkitadmin.activity.AdminMainActivity
import com.example.blinkitadmin.adapter.ProductImageAdapter
import com.example.blinkitadmin.databinding.FragmentAddProductBinding
import com.example.blinkitadmin.models.Product
import com.example.blinkitadmin.utils.Constants
import com.example.blinkitadmin.utils.Utils
import com.example.blinkitadmin.viewmodels.AdminViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class AddProductFragment : Fragment() {
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var binding: FragmentAddProductBinding
    private var imageUris: ArrayList<Uri> = arrayListOf()
    private lateinit var productImageAdapter: ProductImageAdapter
    private var downloadedUrls: ArrayList<String?> = arrayListOf()
    private var maxImages = 5
    private val selectImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            val fiveImagesUris = uris.take(maxImages)
            imageUris.clear()
            imageUris.addAll(fiveImagesUris)
            Log.d("tt", imageUris.toString())
            productImageAdapter = ProductImageAdapter(requireContext())
            binding.rvProductImages.adapter = productImageAdapter
            productImageAdapter.setList(imageUris)
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddProductBinding.inflate(layoutInflater)
        setStatusBarColor()
        setAutoTextComplete()
        onImageSelectButtonClick()

        onAddProductButtonClicked()
        return binding.root
    }


    private fun onAddProductButtonClicked() {
        binding.btnAddProduct.setOnClickListener {

            val productTitle = binding.etProductTitle.text.toString()
            val productQuantity = binding.etProductQuantity.text.toString()
            val productUnit = binding.etProductUnit.text.toString()
            val productPrice = binding.etProductPrice.text.toString()
            val productStock = binding.etProductStock.text.toString()
            val productCategory = binding.etProductCategory.text.toString()
            val productType = binding.etProductType.text.toString()
            val storeType = binding.etProductStoreType.text.toString()

            Utils.showDialog(requireContext(), "uploading images")
            if (productTitle.isEmpty()) {
                Utils.hideDialog()
                binding.etProductTitle.error = "Please enter product title"
            } else if (productQuantity.isEmpty()) {
                Utils.hideDialog()
                binding.etProductQuantity.error = "Please enter product quantity"
            } else if (productUnit.isEmpty()) {
                Utils.hideDialog()
                binding.etProductUnit.error = "Please enter product unit"
            } else if (productPrice.isEmpty()) {
                Utils.hideDialog()
                binding.etProductPrice.error = "Please enter product price"
            } else if (productStock.isEmpty()) {
                Utils.hideDialog()
                binding.etProductStock.error = "Please enter product stock"
            } else if (productCategory.isEmpty()) {
                Utils.hideDialog()
                binding.etProductCategory.error = "Please enter product category"
            } else if (productType.isEmpty()) {
                Utils.hideDialog()
                binding.etProductType.error = "Please enter product type"
            } else if (storeType.isEmpty()) {
                Utils.hideDialog()
                binding.etProductStoreType.error = "Please enter store type"
            } else if (imageUris.isEmpty()) {
                Utils.hideDialog()
                Utils.showToast(requireContext(), "Please select some images!")
            } else {
                val product = Product(
                    productTitle = productTitle,
                    productQuantity = productQuantity,
                    productUnit = productUnit,
                    productPrice = productPrice,
                    productStock = productStock,
                    productCategory = productCategory,
                    productType = productType,
                    storeType = storeType
                )
                saveImages(product)
            }
        }
    }

    private fun saveImages(product: Product) {
        lifecycleScope.launch {
            viewModel.apply {
                savingImagesToFirebaseStorage(imageUris)
                isImagesUploaded.collect {
                    Log.d("gg" , it.toString())
                    if (it) {
                        publishProduct(product)
                    }
                }
            }
        }
    }

    private fun publishProduct(product: Product) {

        lifecycleScope.launch {
            viewModel.downloadedUrls.collect {
                downloadedUrls = it
                Utils.hideDialog()
                startActivity(Intent(requireActivity(), AdminMainActivity::class.java))
                saveProduct(product, downloadedUrls)
            }
        }
    }

    private fun saveProduct(product: Product, downloadedUrls: ArrayList<String?>) {
        val randomId =(1..25).map { (('A'..'Z') + ('a'..'z') + ('0'..'9')).random() }.joinToString("")
        Utils.showDialog(requireContext(), "publishing products...")
        product.apply {
            productRandomId = randomId    //required vip
            itemCount = "0"
            productImageUris = downloadedUrls
            storeOwnerUid = Utils.getCurrentUserUid()
        }
        lifecycleScope.launch {

            viewModel.savingProduct(product,randomId)
            viewModel.isProductSaved.collect {
                if (it) {
                    Utils.hideDialog()
                    clearEditTexts()
                    Utils.showToast(requireContext(), "Your product is live!")
                }
            }
        }
    }

    private fun clearEditTexts() {
        binding.apply {
            etProductTitle.text?.clear()
            etProductQuantity.text?.clear()
            etProductUnit.text?.clear()
            etProductPrice.text?.clear()
            etProductStock.text?.clear()
            etProductCategory.text?.clear()
            etProductType.text?.clear()
            etProductStoreType.text?.clear()
        }
    }


    private fun onImageSelectButtonClick() {
        binding.btnSelectImage.setOnClickListener {
            selectImages.launch("image/*")
        }
    }


    private fun setAutoTextComplete() {
        val units = ArrayAdapter(
            requireContext(),
            R.layout.showing_states_layout,
            Constants.allUnitsOfProducts
        )
        val category = ArrayAdapter(
            requireContext(),
            R.layout.showing_store_layout,
            Constants.allProductsCategory
        )
        val productType =
            ArrayAdapter(requireContext(), R.layout.showing_states_layout, Constants.allProductType)
        val storeType =
            ArrayAdapter(requireContext(), R.layout.showing_states_layout, Constants.storesType)

        binding.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
            etProductStoreType.setAdapter(storeType)
        }
    }

    private fun setStatusBarColor() {
        activity?.window?.apply {
            val statusBarColors = ContextCompat.getColor(requireContext(), R.color.white_yellow)
            statusBarColor = statusBarColors
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }


}