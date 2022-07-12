package com.ma.development.runnertracker.adapters

import android.content.Context
import android.graphics.Bitmap
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.ma.development.runnertracker.common.TrackingUtility.getFormattedDateFromTimeStamp
import com.ma.development.runnertracker.common.TrackingUtility.getFormattedStopWatchTime
import kotlin.math.round

@BindingAdapter("date")
fun bindRunDate(textView: TextView, dateInMillis: Long) {
    textView.text = getFormattedDateFromTimeStamp(dateInMillis)
}

@BindingAdapter("durationInMillis")
fun bindRunDuration(textView: TextView, durationInMillis: Long) {
    textView.text = getFormattedStopWatchTime(durationInMillis)
}

@BindingAdapter("distance")
fun bindRunDistanceInKm(textView: TextView, distance: Int) {
    textView.text = "${round(distance / 1000f * 10) / 10} Km"
}

@BindingAdapter(value = ["bitmap", "context"], requireAll = true)
fun bindImgSrc(imageView: ImageView, bitmap: Bitmap, context: Context) {
    Glide.with(context).load(bitmap).into(imageView)
}

