package com.example.aplapollo.adapter//package com.example.aplapollo.adapter
//
//import android.graphics.drawable.GradientDrawable
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.LinearLayout
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.example.aplapollo.view.SummaryItem
//import com.example.apolloapl.R
//
//class SummaryAdapter(
//    private val list: List<SummaryItem>
//) : RecyclerView.Adapter<SummaryAdapter.ViewHolder>() {
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val title: TextView = view.findViewById(R.id.tvTitle)
//        val count: TextView = view.findViewById(R.id.tvCount)
//        val icon: ImageView = view.findViewById(R.id.imgIcon)
//        val backgroundLayout: LinearLayout = view.findViewById(R.id.llBackground)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.item_summary, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//
//        val item = list[position]
//
//        holder.title.text = item.title
//        holder.count.text = item.count.toString()
//        holder.icon.setImageResource(item.icon)
//
//        val gradientDrawable = GradientDrawable(
//            GradientDrawable.Orientation.LEFT_RIGHT,
//            intArrayOf(item.startColor, item.endColor)
//        )
//        gradientDrawable.cornerRadius = 15f
//
//        holder.backgroundLayout.background = gradientDrawable
//    }
//
//    override fun getItemCount() = list.size
//}