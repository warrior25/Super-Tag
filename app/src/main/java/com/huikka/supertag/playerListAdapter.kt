package com.huikka.supertag

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.huikka.supertag.data.model.Player
import kotlin.random.Random

class PlayerListAdapter(private val playerList: ArrayList<Player>, private val context: Context) :
    ListAdapter<Player, PlayerListAdapter.PlayerViewHolder>(PlayerComparator()) {

    private var checkedPosition: Int = 0;


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        return PlayerViewHolder.create(parent)
    }

    /**
     * Creates possibility to click recyclerView item
     */
    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        val current = playerList[position]
        holder.bind(current, checkedPosition)

        holder.itemView.setOnClickListener {
            val oldPosition = checkedPosition
            checkedPosition = holder.adapterPosition

            notifyItemChanged(checkedPosition)
            notifyItemChanged(oldPosition)
        }
    }

    /**
     * Binds journey data to one recyclerView line
     */
    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerName: TextView = itemView.findViewById(R.id.playerName)

        fun bind(player: Player, checkedPosition: Int) {
            if (checkedPosition == adapterPosition) {
                playerName.setBackgroundResource(R.drawable.player_selected)
                playerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.runner, 0)

            } else {
                playerName.setBackgroundResource(R.drawable.player_unselected)
                playerName.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            }
        }

        companion object {
            fun create(parent: ViewGroup): PlayerViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                    .inflate(R.layout.recyclerview_item, parent, false)
                return PlayerViewHolder(view)
            }
        }
    }

    override fun getItemCount() = playerList.size

    fun selectRandom() {
        val oldPosition = checkedPosition
        checkedPosition = Random.nextInt(0, getItemCount())

        notifyItemChanged(checkedPosition)
        notifyItemChanged(oldPosition)
    }

    class PlayerComparator : DiffUtil.ItemCallback<Player>() {
        override fun areItemsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: Player, newItem: Player): Boolean {
            return oldItem.id == newItem.id
        }
    }
}