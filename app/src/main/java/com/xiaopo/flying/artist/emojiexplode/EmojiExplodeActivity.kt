package com.xiaopo.flying.artist.emojiexplode

import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.xiaopo.flying.artist.R
import kotlinx.android.synthetic.main.activity_emoji_explode.*

class EmojiExplodeActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_emoji_explode)

    val emojis = listOf<Int>(
        R.drawable.blush,
        R.drawable.gem,
        R.drawable.heart,
        R.drawable.rocket,
        R.drawable.rose,
        R.drawable.satisfied
    )

    emojis.forEach {
      emoji_explode.fillEmoji(BitmapDrawable(resources, BitmapFactory.decodeResource(resources, it)))
    }

    btn_explode.setOnClickListener {
      emoji_explode.explode()
    }
  }
}
