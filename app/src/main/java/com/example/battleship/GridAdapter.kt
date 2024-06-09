package com.example.battleship

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView

class GridAdapter(private val context: Context, private val gridItems: List<GridItem>) : BaseAdapter() {

    override fun getCount(): Int = gridItems.size

    override fun getItem(position: Int): Any = gridItems[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.gridItemImage)
        val gridItem = gridItems[position]
        imageView.setImageResource(gridItem.drawable)
        return view
    }
}
