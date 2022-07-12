package com.ma.development.runnertracker.adapters

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.ma.development.runnertracker.R
import com.ma.development.runnertracker.data.pojo.Run
import com.ma.development.runnertracker.databinding.RunItemLayoutBinding
import java.util.zip.Inflater

class RunAdapter (private val context: Context) : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(private val binding: RunItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
       fun bindData (run : Run){
           binding.run = run
           binding.context = context
       }
    }

    val diffCallBack = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    // differ tool to get the difference between the old list and new list
    val differ = AsyncListDiffer(this, diffCallBack)

    fun submitDataList(list: List<Run>) = differ.submitList(list)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding  = RunItemLayoutBinding.inflate(inflater,parent,false)
        return RunViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
       val run = differ.currentList[position]
       holder.bindData(run)
    }

    override fun getItemCount(): Int = differ.currentList.size


}