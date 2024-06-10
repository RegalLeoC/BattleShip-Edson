package com.example.battleship

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

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
        val view: View
        val imageView: ImageView
        val gridItem = gridItems[position]

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false)
            imageView = view.findViewById(R.id.gridItemImage)
            view.tag = imageView
        } else {
            view = convertView
            imageView = view.tag as ImageView
        }

        if (gridItem.isHit) {
            if (gridItem.isShip) {
                imageView.setImageResource(R.drawable.ic_ship_hit)
            } else {
                imageView.setImageResource(R.drawable.ic_miss)
            }
        } else {
            if (gridItem.isShip) {
                imageView.setImageResource(R.drawable.ic_ship)
            } else {
                imageView.setImageResource(R.drawable.ic_empty)
            }
        }

        return view
    }
}
