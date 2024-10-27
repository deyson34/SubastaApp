package com.example.subastaapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class AuctionAdapter(private val auctionList: List<Auction>) : RecyclerView.Adapter<AuctionAdapter.AuctionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AuctionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_auction, parent, false)
        return AuctionViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AuctionViewHolder, position: Int) {
        val auction = auctionList[position]
        holder.titleTextView.text = auction.title
        holder.descriptionTextView.text = auction.description

        // Cargar la imagen usando Glide
        Glide.with(holder.itemView.context)
            .load(auction.imageUrl)
            .into(holder.auctionImageView)
    }

    override fun getItemCount() = auctionList.size

    class AuctionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.auctionTitle)
        val descriptionTextView: TextView = itemView.findViewById(R.id.auctionDescription)
        val auctionImageView: ImageView = itemView.findViewById(R.id.auctionImage)
    }
}
