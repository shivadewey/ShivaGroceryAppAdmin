package com.example.blinkitadmin.fragments

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.denzcoskun.imageslider.models.SlideModel
import com.example.blinkitadmin.R
import com.example.blinkitadmin.adapter.AdapterHomeFragment
import com.example.blinkitadmin.adapter.CategoriesAdapter
import com.example.blinkitadmin.databinding.BottomSheetBinding
import com.example.blinkitadmin.databinding.EditProductLayoutBinding
import com.example.blinkitadmin.databinding.FragmentHomeBinding
import com.example.blinkitadmin.models.Categories
import com.example.blinkitadmin.models.Product
import com.example.blinkitadmin.models.ProductType
import com.example.blinkitadmin.utils.Constants
import com.example.blinkitadmin.utils.Utils
import com.example.blinkitadmin.viewmodels.AdminViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val viewModel: AdminViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding
    private lateinit var adapterHomeFragment: AdapterHomeFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(layoutInflater)

        setStatusBarColor()

        preparingRvForHomeFragment()

        settingAllCategories()

        searchingProducts()

        showingProducts("All")

//        lifecycleScope.launch {fetchBestsellers()  }

        return binding.root
    }

//    private suspend fun fetchBestsellers() {
//        viewModel.fetchDataFromFirebase().collect{
//            for(productTypes in it){
//                Log.d("gggg",productTypes.categoryName.toString())
//                Log.d("gggg",productTypes.products?.size.toString())
//            }
//        }
//    }

    private fun onEditButtonClick(product: Product) {
        val editProductLayoutBinding = EditProductLayoutBinding.inflate(LayoutInflater.from(requireContext()))
        editProductLayoutBinding.apply {
            etProductTitle.setText(product.productTitle)
            etProductQuantity.setText(product.productQuantity)
            etProductUnit.setText(product.productUnit)
            etProductPrice.setText(product.productPrice)
            etProductStock.setText(product.productStock)
            etProductCategory.setText(product.productCategory)
            etProductType.setText(product.productType)
            etProductStoreType.setText(product.storeType)
        }

        val alertDialog = AlertDialog.Builder(requireContext())
            .setView(editProductLayoutBinding.root)
            .create()
        alertDialog.show()

        editProductLayoutBinding.btnEdit.setOnClickListener {
            editProductLayoutBinding.apply {
                etProductTitle.isEnabled = true
                etProductQuantity.isEnabled = true
                etProductUnit.isEnabled = true
                etProductPrice.isEnabled = true
                etProductStock.isEnabled = true
                etProductCategory.isEnabled = true
                etProductType.isEnabled = true
                etProductStoreType.isEnabled = true
            }
            setAutoTextComplete(editProductLayoutBinding)
        }

        editProductLayoutBinding.btnSave.setOnClickListener {
            lifecycleScope.launch {
                product.productTitle = editProductLayoutBinding.etProductTitle.text.toString()
                product.productQuantity = editProductLayoutBinding.etProductQuantity.text.toString()
                product.productUnit = editProductLayoutBinding.etProductUnit.text.toString()
                product.productPrice = editProductLayoutBinding.etProductPrice.text.toString()
                product.productStock = editProductLayoutBinding.etProductStock.text.toString()
                product.productCategory = editProductLayoutBinding.etProductCategory.text.toString()
                product.productType = editProductLayoutBinding.etProductType.text.toString()
                product.storeType = editProductLayoutBinding.etProductStoreType.text.toString()
                viewModel.updateEditedProducts(product)
            }
            Utils.showToast(requireContext() , "Saved changes!")
            alertDialog.dismiss()
        }
    }

    private fun setAutoTextComplete(view: EditProductLayoutBinding) {
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

        view.apply {
            etProductUnit.setAdapter(units)
            etProductCategory.setAdapter(category)
            etProductType.setAdapter(productType)
            etProductStoreType.setAdapter(storeType)
        }
    }




    private fun preparingRvForHomeFragment() {
        adapterHomeFragment = AdapterHomeFragment(::onProductItemClicked, ::onEditButtonClick)
        binding.rvProducts.adapter = adapterHomeFragment
    }
    private fun searchingProducts() {
        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
                val query = s.toString().trim()
                adapterHomeFragment.filter.filter(query)
            }

            override fun afterTextChanged(p0: Editable?) {}
        })
    }
    private fun showingProducts(category: String) {
        lifecycleScope.coroutineContext.cancelChildren() // after coming from another bottom menu products were not visible  , after removing these childs now it's visible
        lifecycleScope.launch {
            viewModel.fetchingAllProducts(category)
                .collect { productList ->
                    if (productList.isEmpty() && category != "All") {
                        binding.rvProducts.visibility = View.GONE
                        binding.tvText.visibility = View.VISIBLE
                    } else {
                        binding.rvProducts.visibility = View.VISIBLE
                        binding.tvText.visibility = View.GONE
                    }
                    adapterHomeFragment.differ.submitList(productList)
                    adapterHomeFragment.originalList = productList as ArrayList<Product>
                    binding.shimmerViewContainer.visibility = View.GONE
                }
        }
    }
    private fun settingAllCategories() {
        val categoriesList = ArrayList<Categories>()
        for (i in 0 until Constants.allProductsCategory.size) {
            categoriesList.add(
                Categories(
                    Constants.allProductsCategory[i],
                    Constants.allProductsCategoryIcon[i]
                )
            )
        }
        binding.rvCategories.adapter =
            CategoriesAdapter(requireContext(), categoriesList, ::onCategoryItemClicked)
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
    private fun onProductItemClicked(product: Product) {
        val bottomSheet = BottomSheetBinding.inflate(LayoutInflater.from(requireContext()))
        bottomSheet.apply {
            val imageList = ArrayList<SlideModel>()
            val productImageList = product.productImageUris
            for (i in 0 until productImageList?.size!!) {
                imageList.add(SlideModel(product.productImageUris?.get(i).toString()))
            }
            ivImageSlider.setImageList(imageList)
            tvProductTitle.text = product.productTitle

            val quantity = product.productQuantity + product.productUnit
            tvProductQuantity.text = quantity

            val price = "₹" + product.productPrice
            tvProductPrice.text = price
        }
        val dialogView = BottomSheetDialog(requireContext())
        dialogView.apply {
            setContentView(bottomSheet.root)
        }
        dialogView.show()

    }
    private var productsJob: Job? = null
    private fun onCategoryItemClicked(categories: Categories) {
        productsJob?.cancel()
        productsJob = lifecycleScope.launch { showingProducts(categories.category) }
    }

}

