package com.example.blinkitadmin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blinkitadmin.databinding.ItemViewCartProductsBinding
import com.example.blinkitadmin.models.CartProducts

class AdapterCartProduct(
    val context: Context
) : RecyclerView.Adapter<AdapterCartProduct.CartProductViewHolder>() {

    class CartProductViewHolder(val binding : ItemViewCartProductsBinding) : RecyclerView.ViewHolder(binding.root)

    val diffutil  = object : DiffUtil.ItemCallback<CartProducts>(){
        override fun areItemsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem.productId == newItem.productId
        }

        override fun areContentsTheSame(oldItem: CartProducts, newItem: CartProducts): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this,diffutil)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartProductViewHolder {
        return CartProductViewHolder(ItemViewCartProductsBinding.inflate(LayoutInflater.from(parent.context) , parent , false))
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: CartProductViewHolder, position: Int) {
        val product = differ.currentList[position]

        holder.binding.apply {
            Glide.with(holder.itemView).load(product.productImage).into(ivProdudctImage)
            tvProductTitle.text = product.productTitle
            tvProductQuantity.text = product.productQuantity
            val price = product.productPrice
            tvProductPrice.text = price
            tvProductCount.text = product.productCount.toString()

        }

    }
}