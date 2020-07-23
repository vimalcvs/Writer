package com.alim.writeradmin.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alim.writeradmin.Model.UserModel
import com.alim.writeradmin.R
import com.google.firebase.database.DatabaseReference

class CategoryAdapter(context: Context, mData: ArrayList<String>)
    : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    private val data = mData
    private val mContext = context

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title = view.findViewById<TextView>(R.id.category_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapter.ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.category_home, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(holder: CategoryAdapter.ViewHolder, position: Int) {
        holder.title.text = data[position]
    }
}