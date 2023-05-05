package com.mikifus.padland.Utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout

class HSVColorPickerDialog(private val context: Context, private var selectedColor: Int, private val listener: OnColorSelectedListener) : AlertDialog(context) {
    interface OnColorSelectedListener {
        /**
         * @param color The color code selected, or null if no color. No color is only
         * possible if [setNoColorButton()][HSVColorPickerDialog.setNoColorButton]
         * has been called on the dialog before showing it
         */
        fun colorSelected(color: Int)
    }

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    fun makeView() {
        colorWheel = HSVColorWheel(context)
        valueSlider = HSVValueSlider(context)
        val padding = (context.resources.displayMetrics.density * PADDING_DP).toInt()
        val borderSize = (context.resources.displayMetrics.density * BORDER_DP).toInt()
        val orientationRule = if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) RelativeLayout.BELOW else RelativeLayout.RIGHT_OF
        val layout = RelativeLayout(context)
        var lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        lp.bottomMargin = (context.resources.displayMetrics.density * CONTROL_SPACING_DP).toInt()
        colorWheel!!.setListener(object : OnColorSelectedListener {
            override fun colorSelected(color: Int) {
                valueSlider!!.setColor(color, true)
            }
        })
        colorWheel!!.setColor(selectedColor)
        colorWheel!!.id = 1
        layout.addView(colorWheel, lp)
        val selectedColorHeight = (context.resources.displayMetrics.density * SELECTED_COLOR_HEIGHT_DP).toInt()
        val valueSliderBorder = FrameLayout(context)
        valueSliderBorder.setBackgroundColor(BORDER_COLOR)
        valueSliderBorder.setPadding(borderSize, borderSize, borderSize, borderSize)
        valueSliderBorder.id = 2
        lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, selectedColorHeight + 2 * borderSize)
        lp.bottomMargin = (context.resources.displayMetrics.density * CONTROL_SPACING_DP).toInt()
        lp.addRule(orientationRule, 1)
        layout.addView(valueSliderBorder, lp)
        valueSlider!!.setColor(selectedColor, false)
        valueSlider!!.setListener(object : OnColorSelectedListener {
            override fun colorSelected(color: Int) {
                selectedColor = color
                selectedColorView!!.setBackgroundColor(color)
            }
        })
        valueSliderBorder.addView(valueSlider)
        val selectedColorborder = FrameLayout(context)
        selectedColorborder.setBackgroundColor(BORDER_COLOR)
        lp = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, selectedColorHeight + 2 * borderSize)
        selectedColorborder.setPadding(borderSize, borderSize, borderSize, borderSize)
        lp.addRule(orientationRule, 1)
        lp.addRule(RelativeLayout.BELOW, 2)
        layout.addView(selectedColorborder, lp)
        selectedColorView = View(context)
        selectedColorView!!.setBackgroundColor(selectedColor)
        selectedColorborder.addView(selectedColorView)
        setButton(BUTTON_NEGATIVE, context.getString(android.R.string.cancel), clickListener)
        setButton(BUTTON_POSITIVE, context.getString(android.R.string.ok), clickListener)
        setView(layout, padding, padding, padding, padding)
    }

    private val clickListener = DialogInterface.OnClickListener { dialog, which ->
        when (which) {
            BUTTON_NEGATIVE -> dialog.dismiss()
            BUTTON_NEUTRAL -> {
                dialog.dismiss()
                listener.colorSelected(-1)
            }

            BUTTON_POSITIVE -> listener.colorSelected(selectedColor)
        }
    }
    private var colorWheel: HSVColorWheel? = null
    private var valueSlider: HSVValueSlider? = null
    private var selectedColorView: View? = null

    init {
        makeView()
    }

    /**
     * Adds a button to the dialog that allows a user to select "No color",
     * which will call the listener's [colorSelected(Integer)][OnColorSelectedListener.colorSelected] callback
     * with null as its parameter
     * @param res A string resource with the text to be used on this button
     */
    fun setNoColorButton(res: Int) {
        setButton(BUTTON_NEUTRAL, getContext().getString(res), clickListener)
    }

    private class HSVColorWheel : View {
        private val context: Context
        private var listener: OnColorSelectedListener? = null

        constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
            this.context = context
            init()
        }

        constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
            this.context = context
            init()
        }

        constructor(context: Context) : super(context) {
            this.context = context
            init()
        }

        private var scale = 0
        private var pointerLength = 0
        private var innerPadding = 0
        private val pointerPaint = Paint()
        private fun init() {
            val density = context.resources.displayMetrics.density
            scale = (density * SCALE).toInt()
            pointerLength = (density * POINTER_LENGTH_DP).toInt()
            pointerPaint.strokeWidth = (density * POINTER_LINE_WIDTH_DP).toInt().toFloat()
            innerPadding = pointerLength / 2
        }

        fun setListener(listener: OnColorSelectedListener?) {
            this.listener = listener
        }

        var colorHsv = floatArrayOf(0f, 0f, 1f)
        fun setColor(color: Int) {
            Color.colorToHSV(color, colorHsv)
            invalidate()
        }

        override fun onDraw(canvas: Canvas) {
            if (bitmap != null) {
                canvas.drawBitmap(bitmap!!, null, rect!!, null)
                val hueInPiInterval = colorHsv[0] / 180f * Math.PI.toFloat()
                selectedPoint.x = rect!!.left + (-Math.cos(hueInPiInterval.toDouble()) * colorHsv[1] * innerCircleRadius + fullCircleRadius).toInt()
                selectedPoint.y = rect!!.top + (-Math.sin(hueInPiInterval.toDouble()) * colorHsv[1] * innerCircleRadius + fullCircleRadius).toInt()
                canvas.drawLine((selectedPoint.x - pointerLength).toFloat(), selectedPoint.y.toFloat(), (selectedPoint.x + pointerLength).toFloat(), selectedPoint.y.toFloat(), pointerPaint)
                canvas.drawLine(selectedPoint.x.toFloat(), (selectedPoint.y - pointerLength).toFloat(), selectedPoint.x.toFloat(), (selectedPoint.y + pointerLength).toFloat(), pointerPaint)
            }
        }

        private var rect: Rect? = null
        private var bitmap: Bitmap? = null
        private var pixels: IntArray
        private var innerCircleRadius = 0f
        private var fullCircleRadius = 0f
        private var scaledWidth = 0
        private var scaledHeight = 0
        private var scaledPixels: IntArray
        private var scaledInnerCircleRadius = 0f
        private var scaledFullCircleRadius = 0f
        private var scaledFadeOutSize = 0f
        private val selectedPoint = Point()
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            rect = Rect(innerPadding, innerPadding, w - innerPadding, h - innerPadding)
            bitmap = Bitmap.createBitmap(w - 2 * innerPadding, h - 2 * innerPadding, Bitmap.Config.ARGB_8888)
            fullCircleRadius = (Math.min(rect!!.width(), rect!!.height()) / 2).toFloat()
            innerCircleRadius = fullCircleRadius * (1 - FADE_OUT_FRACTION)
            scaledWidth = rect!!.width() / scale
            scaledHeight = rect!!.height() / scale
            scaledFullCircleRadius = (Math.min(scaledWidth, scaledHeight) / 2).toFloat()
            scaledInnerCircleRadius = scaledFullCircleRadius * (1 - FADE_OUT_FRACTION)
            scaledFadeOutSize = scaledFullCircleRadius - scaledInnerCircleRadius
            scaledPixels = IntArray(scaledWidth * scaledHeight)
            pixels = IntArray(rect!!.width() * rect!!.height())
            createBitmap()
        }

        private fun createBitmap() {
            val w = rect!!.width()
            val h = rect!!.height()
            val hsv = floatArrayOf(0f, 0f, 1f)
            var alpha = 255
            var x = -scaledFullCircleRadius.toInt()
            var y = -scaledFullCircleRadius.toInt()
            for (i in scaledPixels.indices) {
                if (i % scaledWidth == 0) {
                    x = -scaledFullCircleRadius.toInt()
                    y++
                } else {
                    x++
                }
                val centerDist = Math.sqrt((x * x + y * y).toDouble())
                if (centerDist <= scaledFullCircleRadius) {
                    hsv[0] = (Math.atan2(y.toDouble(), x.toDouble()) / Math.PI * 180f).toFloat() + 180
                    hsv[1] = (centerDist / scaledInnerCircleRadius).toFloat()
                    alpha = if (centerDist <= scaledInnerCircleRadius) {
                        255
                    } else {
                        255 - ((centerDist - scaledInnerCircleRadius) / scaledFadeOutSize * 255).toInt()
                    }
                    scaledPixels[i] = Color.HSVToColor(alpha, hsv)
                } else {
                    scaledPixels[i] = 0x00000000
                }
            }
            var scaledX: Int
            var scaledY: Int
            x = 0
            while (x < w) {
                scaledX = x / scale
                if (scaledX >= scaledWidth) scaledX = scaledWidth - 1
                y = 0
                while (y < h) {
                    scaledY = y / scale
                    if (scaledY >= scaledHeight) scaledY = scaledHeight - 1
                    pixels[x * h + y] = scaledPixels[scaledX * scaledHeight + scaledY]
                    y++
                }
                x++
            }
            bitmap!!.setPixels(pixels, 0, w, 0, 0, w, h)
            invalidate()
        }

        override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
            val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
            val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
            val width: Int
            val height: Int
            /*
			 * Make the view quadratic, with height and width equal and as large as possible
			 */height = Math.min(maxWidth, maxHeight)
            width = height
            setMeasuredDimension(width, height)
        }

        fun getColorForPoint(x: Int, y: Int, hsv: FloatArray): Int {
            var x = x
            var y = y
            x -= fullCircleRadius.toInt()
            y -= fullCircleRadius.toInt()
            val centerDist = Math.sqrt((x * x + y * y).toDouble())
            hsv[0] = (Math.atan2(y.toDouble(), x.toDouble()) / Math.PI * 180f).toFloat() + 180
            hsv[1] = Math.max(0f, Math.min(1f, (centerDist / innerCircleRadius).toFloat()))
            return Color.HSVToColor(hsv)
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    if (listener != null) {
                        listener!!.colorSelected(getColorForPoint(event.x.toInt(), event.y.toInt(), colorHsv))
                    }
                    invalidate()
                    return true
                }
            }
            return super.onTouchEvent(event)
        }

        companion object {
            private const val SCALE = 2f
            private const val FADE_OUT_FRACTION = 0.03f
            private const val POINTER_LINE_WIDTH_DP = 2
            private const val POINTER_LENGTH_DP = 10
        }
    }

    private class HSVValueSlider : View {
        constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {}
        constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {}
        constructor(context: Context?) : super(context) {}

        private var listener: OnColorSelectedListener? = null
        fun setListener(listener: OnColorSelectedListener?) {
            this.listener = listener
        }

        var colorHsv = floatArrayOf(0f, 0f, 1f)
        fun setColor(color: Int, keepValue: Boolean) {
            val oldValue = colorHsv[2]
            Color.colorToHSV(color, colorHsv)
            if (keepValue) {
                colorHsv[2] = oldValue
            }
            if (listener != null) {
                listener!!.colorSelected(Color.HSVToColor(colorHsv))
            }
            createBitmap()
        }

        override fun onDraw(canvas: Canvas) {
            if (bitmap != null) {
                canvas.drawBitmap(bitmap!!, srcRect, dstRect!!, null)
            }
        }

        private var srcRect: Rect? = null
        private var dstRect: Rect? = null
        private var bitmap: Bitmap? = null
        private var pixels: IntArray
        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            var w = w
            super.onSizeChanged(w, h, oldw, oldh)
            if (w == 0) {
                w = oldh
            }
            srcRect = Rect(0, 0, w, 1)
            dstRect = Rect(0, 0, w, h)
            bitmap = Bitmap.createBitmap(w, 1, Bitmap.Config.ARGB_8888)
            pixels = IntArray(w)
            createBitmap()
        }

        private fun createBitmap() {
            if (bitmap == null) {
                return
            }
            val w = width
            val hsv = floatArrayOf(colorHsv[0], colorHsv[1], 1f)
            val selectedX = (colorHsv[2] * w).toInt()
            var value = 0f
            val valueStep = 1f / w
            for (x in 0 until w) {
                value += valueStep
                if (x >= selectedX - 1 && x <= selectedX + 1) {
                    val intVal = 0xFF - (value * 0xFF).toInt()
                    val color = intVal * 0x010101 + -0x1000000
                    pixels[x] = color
                } else {
                    hsv[2] = value
                    pixels[x] = Color.HSVToColor(hsv)
                }
            }
            bitmap!!.setPixels(pixels, 0, w, 0, 0, w, 1)
            invalidate()
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            val action = event.actionMasked
            when (action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    val x = Math.max(0, Math.min(bitmap!!.width - 1, event.x.toInt()))
                    val value = x / bitmap!!.width.toFloat()
                    if (colorHsv[2] != value) {
                        colorHsv[2] = value
                        if (listener != null) {
                            listener!!.colorSelected(Color.HSVToColor(colorHsv))
                        }
                        createBitmap()
                        invalidate()
                    }
                    return true
                }
            }
            return super.onTouchEvent(event)
        }
    }

    companion object {
        private const val PADDING_DP = 20
        private const val CONTROL_SPACING_DP = 20
        private const val SELECTED_COLOR_HEIGHT_DP = 50
        private const val BORDER_DP = 1
        private const val BORDER_COLOR = Color.BLACK
    }
}