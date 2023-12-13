package com.example.blinkitadmin.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.blinkitadmin.databinding.ItemViewImageSelectionBinding
import java.lang.Exception

class ProductImageAdapter(
    private val context: Context,

    ) : Adapter<ProductImageAdapter.ImageViewHolder>() {
    class ImageViewHolder(val binding: ItemViewImageSelectionBinding) : ViewHolder(binding.root)

    private var productImageList = ArrayList<Uri>()

    fun setList(productImageList: ArrayList<Uri>) {
        this.productImageList = productImageList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            ItemViewImageSelectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return productImageList.size
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val image = productImageList[position]
        holder.binding.apply {
            ivImage.setImageURI(image)
        }
        holder.binding.closeButton.setOnClickListener {
            if (position < productImageList.size) {
                productImageList.removeAt(position)
                notifyItemRemoved(position)
            }
        }
    }
}