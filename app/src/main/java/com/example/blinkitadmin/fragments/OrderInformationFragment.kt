package com.example.blinkitadmin.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.blinkitadmin.R
import com.example.blinkitadmin.adapter.AdapterOrders
import com.example.blinkitadmin.databinding.FragmentOrderInformationBinding
import com.example.blinkitadmin.models.OrderedItems
import com.example.blinkitadmin.viewmodels.AdminViewModel
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class OrderInformationFragment : Fragment() {
    val viewModel : AdminViewModel by viewModels()
    private lateinit var binding : FragmentOrderInformationBinding
    private lateinit var adapterOrders: AdapterOrders
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentOrderInformationBinding.inflate(layoutInflater)

        lifecycleScope.launch { fetchingAllTheOrderedProducts()}

        return binding.root
    }

    private suspend fun fetchingAllTheOrderedProducts() {
        //first fetch the data then make a new dataclass and then send to rv
        viewModel.fetchingOrderedProducts().collect{orderLists ->
            Log.d("test" , orderLists.toString())
            if(orderLists.isNotEmpty()){
                val orderedLists = ArrayList<OrderedItems>()
                for(ordersList in orderLists){

                    val titles = StringBuilder()
                    var totalPrice  = 0
                    for (orders in ordersList.orderList!!){
                        Log.d("list" , orders.toString())
                        Log.d("list" , orders.productCategory.toString())
                        titles.append("${orders.productCategory}, ")
                        val productPrice = orders.productPrice?.substring(1)?.toInt()!!
                        val productCount = orders.productCount!!
                        totalPrice += (productPrice * productCount)
                    }

                    val orderedItems = OrderedItems(ordersList.orderId,ordersList.orderDate,ordersList.orderStatus,titles.toString(),totalPrice)
                    orderedLists.add(orderedItems)
                }
                Log.d("test" , orderedLists.toString())
                adapterOrders = AdapterOrders(requireContext())
                binding.rvOrders.adapter = adapterOrders
                adapterOrders.differ.submitList(orderedLists)
            }
        }
    }

}