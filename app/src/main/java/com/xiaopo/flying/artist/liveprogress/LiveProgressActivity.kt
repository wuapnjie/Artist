package com.xiaopo.flying.artist.liveprogress

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.xiaopo.flying.artist.R
import com.xiaopo.flying.artist.base.logd
import kotlinx.android.synthetic.main.activity_live_progress.*
import kotlinx.android.synthetic.main.activity_two_way.*

class LiveProgressActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_live_progress)

    live_enter_progress.start(5)

  }
}
