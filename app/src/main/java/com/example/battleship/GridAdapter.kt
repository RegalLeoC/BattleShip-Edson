package com.example.battleship

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.battleship.databinding.GridItemBinding

class GridAdapter(private val context: Context, private val gridItems: List<GridItem>) : BaseAdapter() {

    override fun getCount(): Int {
        return gridItems.size
    }

    override fun getItem(position: Int): Any {
        return gridItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val binding: GridItemBinding
        val view: View

        if (convertView == null) {
            binding = GridItemBinding.inflate(LayoutInflater.from(context), parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as GridItemBinding
            view = convertView
        }

        val item = gridItems[position]

        if (item.isHit) {
            if (item.isShip) {
                view.setBackgroundResource(R.drawable.hit_background)
            } else {
                view.setBackgroundResource(R.drawable.miss_background)
            }
        } else {
            view.setBackgroundResource(R.drawable.default_background)
        }

        return view
    }
}
