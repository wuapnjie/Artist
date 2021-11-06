package com.xiaopo.flying.artist.waterfall

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xiaopo.flying.artist.R
import com.xiaopo.flying.artist.base.strings
import com.xiaopo.flying.recyclerkit.AnotherAdapter
import com.xiaopo.flying.recyclerkit.itemBinder
import com.xiaopo.flying.recyclerkit.with
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_text.view.*

class WaterfallActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_waterfall)

    val adapter = AnotherAdapter()
        .with(String::class.java,
            itemBinder<String>(R.layout.item_text, render = {
              tv_text.text = it
            })
        )

    adapter.refresh(strings(50))

    recycler_view.with(adapter)

    waterfall.bind(recycler_view)
  }
}
