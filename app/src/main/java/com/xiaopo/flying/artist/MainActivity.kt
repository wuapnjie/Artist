package com.xiaopo.flying.artist

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.xiaopo.flying.artist.base.Work
import com.xiaopo.flying.artist.base.intentTo
import com.xiaopo.flying.artist.elasticdrag.ElasticActivity
import com.xiaopo.flying.artist.emojiexplode.EmojiExplodeActivity
import com.xiaopo.flying.artist.interactionimage.InteractionImageActivity
import com.xiaopo.flying.artist.twowayseek.TwoWayActivity
import com.xiaopo.flying.artist.waterfall.WaterfallActivity
import com.xiaopo.flying.recyclerkit.AnotherAdapter
import com.xiaopo.flying.recyclerkit.itemBinder
import com.xiaopo.flying.recyclerkit.with
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.item_text.view.*

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val works = arrayListOf<Any>(
        Work("Waterfall Layout", WaterfallActivity::class.java),
        Work("Elastic Drag Layout", ElasticActivity::class.java),
        Work("Two Way Seek Bar", TwoWayActivity::class.java),
        Work("Emoji Explode View", EmojiExplodeActivity::class.java),
        Work("Interaction Image View", InteractionImageActivity::class.java)
    )

    val adapter = AnotherAdapter()
        .with(Work::class.java,
            itemBinder<Work>(R.layout.item_text, render = {
              tv_text.text = it.name
            }).clickWith { item, _ -> intentTo(item) }
        )

    adapter.refresh(works)

    recycler_view.with(adapter)

    waterfall.bind(recycler_view)

  }
}
