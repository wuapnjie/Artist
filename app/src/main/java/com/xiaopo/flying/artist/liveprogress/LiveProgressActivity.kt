package com.xiaopo.flying.artist.liveprogress

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.xiaopo.flying.artist.R
import kotlinx.android.synthetic.main.activity_live_progress.*

class LiveProgressActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_live_progress)

    live_enter_progress.start(5)

  }
}
