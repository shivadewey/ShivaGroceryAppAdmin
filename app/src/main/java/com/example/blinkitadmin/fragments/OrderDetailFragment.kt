package com.example.blinkitadmin.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.util.Util
import com.example.blinkitadmin.R
import com.example.blinkitadmin.adapter.AdapterCartProduct
import com.example.blinkitadmin.databinding.FragmentOrderDetailBinding
import com.example.blinkitadmin.utils.Utils
import com.example.blinkitadmin.viewmodels.AdminViewModel
import kotlinx.coroutines.launch

class OrderDetailFragment : Fragment() {
    private val disabledItems = mutableSetOf<Int>()
    private val viewModel : AdminViewModel by viewModels()
    private lateinit var binding : FragmentOrderDetailBinding
    private lateinit var adapterCartProduct: AdapterCartProduct
    private var orderId : String = " "
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderDetailBinding.inflate(layoutInflater)
        val bundle = arguments
        orderId = bundle?.getString("orderId")!!
        val orderStatus = bundle.getInt("orderStatus")

        showStatus(orderStatus)

        lifecycleScope.launch { getTheOrderedItems(orderId) }

        if(orderStatus != 3){
            binding.btnChangeStatus.setOnClickListener { changeStatus(it) }
        }
        else{
            Utils.showToast(requireContext() , "Product Delivered")
        }

        return binding.root
    }

    private fun changeStatus(view: View?) {
        val popupMenu = PopupMenu(requireContext(),view)
        popupMenu.menuInflater.inflate(R.menu.menu_popup, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener {item : MenuItem->
            when(item.itemId){
                R.id.menuReceived ->{
                    showStatus(1)
                    lifecycleScope.launch {
                        viewModel.updateOrderStatus(orderId,1 )
                        viewModel.sendNotification(orderId,"Your item has been received by the store owner","Order received orderId = ($orderId)")
                    }
                    true
                }
                R.id.menuDispatched ->{
                    showStatus(2)
                    lifecycleScope.launch {
                        viewModel.updateOrderStatus(orderId,2)
                        viewModel.sendNotification(orderId,"Your item has been dispatched by the store owner","Order dispatched orderId = ($orderId)")
                    }
                    true
                }
                R.id.menuDelivered ->{
                    showStatus(3)
                    lifecycleScope.launch {
                        viewModel.updateOrderStatus(orderId,3 )
                        viewModel.sendNotification(orderId,"Your item has been delivered by the store owner","Order delivered orderId = ($orderId)")
                    }

                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    private suspend fun getTheOrderedItems(orderId: String?) {
        viewModel.getOrderedItems(orderId!!).collect{orderedItems->
            if(orderedItems!!.isNotEmpty()){
                adapterCartProduct = AdapterCartProduct(requireContext())
                binding.rvProductsItems.adapter = adapterCartProduct
                adapterCartProduct.differ.submitList(orderedItems)
            }
        }
    }

    private fun showStatus(orderStatus: Int?) {
        when(orderStatus){
            0 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
            }
            1 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
            }
            2 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv3.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
            }
            3 ->{
                binding.iv1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view1.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv3.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view2.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.iv4.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
                binding.view3.backgroundTintList = ContextCompat.getColorStateList(requireContext() , R.color.blue)
            }
        }
    }


}