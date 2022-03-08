package com.river.imagepicker.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.river.imagepicker.entry.LocalMedia

/**

 * @Author river
 * @Date 2021/11/1-10:24
 */
class PreviewImageAdapter(val context: Context, val data: List<LocalMedia>) : PagerAdapter() {

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val imageView = ImageView(container.context)
        container.addView(imageView)
        Glide.with(context).load(data[position].uri).into(imageView)
        return imageView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getCount() = data.size

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`
}

