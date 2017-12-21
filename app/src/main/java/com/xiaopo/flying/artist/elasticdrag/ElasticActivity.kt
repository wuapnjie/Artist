package com.xiaopo.flying.artist.elasticdrag

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xiaopo.flying.artist.R
import com.xiaopo.flying.artist.base.strings
import com.xiaopo.flying.recyclerkit.AnotherAdapter
import com.xiaopo.flying.recyclerkit.itemBinder
import com.xiaopo.flying.recyclerkit.with
import kotlinx.android.synthetic.main.activity_elastic.*
import kotlinx.android.synthetic.main.item_text.view.*

class ElasticActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_elastic)

    val adapter = AnotherAdapter()
        .with(String::class.java,
            itemBinder<String>(R.layout.item_text, render = {
              tv_text.text = it
            })
        )

    adapter.refresh(strings(50))

    recycler_view.with(adapter)
  }
}
