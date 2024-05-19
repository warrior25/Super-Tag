package com.huikka.supertag

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.huikka.supertag.data.dto.Card

class CardListAdapter(
    private val cardList: ArrayList<Card>,
) : ListAdapter<Card, CardListAdapter.CardViewHolder>(CardComparator()) {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardViewHolder {
        return CardViewHolder.create(parent)
    }

    /**
     * Creates possibility to click recyclerView item
     */
    override fun onBindViewHolder(holder: CardViewHolder, position: Int) {
        val current = cardList[position]
        holder.bind(current)

        //TODO("If card is disabled(in use) then disable click listener and mark card grey")

        holder.itemView.setOnClickListener {
            notifyItemChanged(holder.adapterPosition)

        }
    }

    /**
     * Binds journey data to one recyclerView line
     */
    class CardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardName: TextView = itemView.findViewById(R.id.name)
        private val cardPrice: TextView = itemView.findViewById(R.id.price)
        private val cardImage: ImageView = itemView.findViewById(R.id.image)


        fun bind(card: Card) {
            cardName.text = card.name
            cardPrice.text = card.price.toString()
            cardImage.setImageResource(card.icon)

        }

        companion object {
            fun create(parent: ViewGroup): CardViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_card, parent, false)
                return CardViewHolder(view)
            }
        }
    }

    override fun getItemCount() = cardList.size


    class CardComparator : DiffUtil.ItemCallback<Card>() {
        override fun areItemsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Card, newItem: Card): Boolean {
            return oldItem.id == newItem.id
        }
    }
}