package com.xiaopo.flying.artist.liveprogress

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import com.xiaopo.flying.artist.base.UIUtils

class LiveProgressbar : View {

    private val mRoundedRectPath: Path = Path()
    private val mClipRect: RectF = RectF()
    private val mProgressRect: RectF = RectF()
    private val highlightRect = RectF()

    var maxCount = 100f
    private var currentCount = 0f
    private var mWidth = 0
    private var mHeight = 0
    private var mDuration = 0
    private var mInterval = 0L
    private val marginLeft = UIUtils.dip2Px(context, 2f)
    private val marginRight = UIUtils.dip2Px(context, 2f)
    private val delay = 50L

    private var isAnimRunning = false

    private var mHandler = Handler(Looper.getMainLooper())
    private var liveProgressListener: LiveProgressListener? = null
    private val highlightColor = Color.parseColor("#FFFFFF")
    private val progressColor = Color.parseColor("#3DFFFFFF")

    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = progressColor
        strokeWidth = UIUtils.dip2Px(context, 1f)
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


    fun setCurrentCount(currentCount: Float) {
        this.currentCount = if (currentCount > maxCount) maxCount else currentCount
        invalidate()
    }


    fun setLiveProgressListener(liveProgressListener: LiveProgressListener) {
        this.liveProgressListener = liveProgressListener
    }

    private val runnable: Runnable = object : Runnable {
        override fun run() {
            if (!isAnimRunning) {
                return
            }
            if (mInterval == 0L) {
                liveProgressListener?.onStart()
            }

            if (mInterval > mDuration) {
                liveProgressListener?.onEnd()
                return
            }
            setCurrentCount(mInterval / mDuration.toFloat() * maxCount)
            mInterval += delay
            mHandler.postDelayed(this, delay)
        }
    }

    fun start(duration: Int) {
        isAnimRunning = true
        mInterval = 0
        mDuration = duration * 1000
        mHandler.postDelayed(runnable, delay)
    }

    fun stop() {
        isAnimRunning = false
        mHandler.removeCallbacks(runnable)
        mInterval = 0
        liveProgressListener = null
    }

    fun isAutoRunning(): Boolean {
        return isAnimRunning
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        mClipRect.set(marginLeft,
            marginLeft,
            mWidth.toFloat() - marginRight,
            mHeight.toFloat() - marginRight)

        val round = mWidth.toFloat() / 2
        mRoundedRectPath.reset()
        mRoundedRectPath.addRoundRect(mClipRect, round, round, Path.Direction.CCW)

        paint.color = progressColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = UIUtils.dip2Px(context, 1f)
        canvas.drawPath(mRoundedRectPath, paint)


        canvas.save()
        canvas.clipPath(mRoundedRectPath)

        val section = currentCount / maxCount
        mProgressRect.set(marginLeft, marginLeft, mWidth * section, mHeight.toFloat())

        paint.color = progressColor
        paint.style = Paint.Style.FILL
        canvas.drawRect(mProgressRect, paint)

        canvas.restore()

        canvas.save()
        highlightRect.set(0f, 0f, mProgressRect.right, mHeight.toFloat())
        canvas.clipRect(highlightRect)

        paint.color = highlightColor
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = UIUtils.dip2Px(context, 2f)
        canvas.drawPath(mRoundedRectPath, paint)

        canvas.restore()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mWidth = measuredWidth
        mHeight = measuredHeight
    }

    interface LiveProgressListener {
        fun onStart()
        fun onEnd()
    }
}