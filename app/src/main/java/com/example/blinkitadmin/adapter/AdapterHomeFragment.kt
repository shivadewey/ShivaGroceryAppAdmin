package com.example.blinkitadmin.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.denzcoskun.imageslider.models.SlideModel
import com.example.blinkitadmin.databinding.ItemViewProductBinding
import com.example.blinkitadmin.models.Product
import com.example.blinkitadmin.utils.FilteringProducts

class AdapterHomeFragment(
    val onItemViewCLick: (Product) -> Unit,
    val onEditButtonClick: (Product) -> Unit
) : Adapter<AdapterHomeFragment.ProductsViewHolder>() , Filterable{

    class ProductsViewHolder(val binding : ItemViewProductBinding) : ViewHolder(binding.root)

    val diffUtils = object : DiffUtil.ItemCallback<Product>(){
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this,diffUtils)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductsViewHolder {
        return ProductsViewHolder(ItemViewProductBinding.inflate(LayoutInflater.from(parent.context) ,parent ,false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: ProductsViewHolder, position: Int) {
        val product = differ.currentList[position]
        holder.binding.apply {
            val imageList = ArrayList<SlideModel>()

            val productImageList = product.productImageUris

            for(i in 0 until productImageList?.size!!){
                imageList.add(SlideModel(product.productImageUris?.get(i).toString()))
            }

            ivImageSlider.setImageList(imageList)

            tvProductTitle.text = product.productTitle

            val quantity = product.productQuantity + product.productUnit
            tvProductQuantity.text = quantity

            val price = "₹"+product.productPrice
            tvProductPrice.text = price

            tvAdd.setOnClickListener { onEditButtonClick(product) }
        }

        holder.itemView.setOnClickListener {
            onItemViewCLick(product)
        }

    }
    private var filter : FilteringProducts ?= null
    var originalList = ArrayList<Product>()
    override fun getFilter(): Filter {
        if(filter == null) return FilteringProducts(this,originalList)
        return filter as FilteringProducts
    }

}