package com.xiaopo.flying.artist.twowayseek

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xiaopo.flying.artist.R
import com.xiaopo.flying.artist.base.log
import kotlinx.android.synthetic.main.activity_two_way.*

class TwoWayActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_two_way)

    // MODE_INT or MODE_FLOAT
    two_way_seek.mode = TwoWaySeekBar.MODE_INT
    two_way_seek.onIntSeekChangeListener = {
      "seek int : $it".log()
    }

    two_way_seek.onFloatSeekChangeListener = {
      "seek float : $it".log()
    }
  }
}
