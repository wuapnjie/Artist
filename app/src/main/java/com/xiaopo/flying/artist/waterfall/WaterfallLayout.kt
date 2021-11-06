package com.xiaopo.flying.artist.waterfall

import android.content.Context
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.widget.ScrollView
import com.xiaopo.flying.artist.R
import com.xiaopo.flying.artist.base.clamp
import com.xiaopo.flying.artist.base.dpInt

/**
 * @author wupanjie
 */
class WaterfallLayout @JvmOverloads constructor(context: Context,
                                                attr: AttributeSet? = null,
                                                defStyle: Int = 0)
  : CardView(context, attr, defStyle) {

  var initialElevation: Int = 0.dpInt
    set(value) {
      field = value
      withTargetScroll()
    }

  var finalElevation: Int = 6.dpInt
    set(value) {
      field = value
      withTargetScroll()
    }

  var scrollFinalElevation: Int = 300.dpInt
    set(value) {
      field = value
      withTargetScroll()
    }

  private var recyclerView: RecyclerView? = null
  private var scrollView: ScrollView? = null

  private var scrolledPosition = 0

  init {
    radius = 0f
    attr?.let {
      val typedArray = context.obtainStyledAttributes(attr, R.styleable.WaterfallLayout)
      initialElevation = typedArray.getDimensionPixelSize(R.styleable.WaterfallLayout_initial_elevation, 0.dpInt)
      finalElevation = typedArray.getDimensionPixelSize(R.styleable.WaterfallLayout_final_elevation, 6.dpInt)
      scrollFinalElevation = typedArray.getInteger(R.styleable.WaterfallLayout_final_elevation, 300.dpInt)
      typedArray.recycle()
    }
  }

  fun bind(recyclerView: RecyclerView) {
    this.recyclerView = recyclerView
    recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
      override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        scrolledPosition += dy
        withTargetScroll()
      }
    })
  }

  fun bind(scrollView: ScrollView) {
    this.scrollView = scrollView
    scrollView.viewTreeObserver.addOnScrollChangedListener {
      scrolledPosition = scrollView.scrollY
      withTargetScroll()
    }
  }

  private fun withTargetScroll() {
    val newElevation = calculateElevation()
    if (cardElevation != newElevation) cardElevation = newElevation
  }

  private fun calculateElevation(): Float {
    var newElevation = (finalElevation.toFloat() * scrolledPosition) / scrollFinalElevation
    newElevation = clamp(initialElevation.toFloat(), newElevation, finalElevation.toFloat())
    return newElevation
  }

}