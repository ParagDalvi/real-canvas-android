package app.web.realcanvas.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import app.web.realcanvas.models.DrawPath
import app.web.realcanvas.models.DrawPoints


class PaintView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var isDrawing = false
    private val defaultWidth = 20f
    private val pathList = ArrayList<DrawPath>()
    private val drawingPoints = ArrayList<DrawPoints>()
    private val currentBrush = Color.BLACK
    private val paint = Paint()
    private val path = Path()
    private lateinit var sendData: (List<DrawPoints>) -> Unit

    fun init(sendData: (List<DrawPoints>) -> Unit) {
        paint.isAntiAlias = true
        paint.color = currentBrush
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = defaultWidth
        this.sendData = sendData
    }

    fun setIsDrawing(b: Boolean) {
        isDrawing = b
    }

    fun updateDrawing(list: List<DrawPoints>?) {
        reset()
        list?.forEach {
            when (it.what) {
                "move" -> {
                    path.moveTo(it.x, it.y)
                    pathList.add(DrawPath(currentBrush, path))
                    invalidate()
                }
                "line" -> {
                    path.lineTo(it.x, it.y)
                    invalidate()
                }
            }
        }
    }

    fun reset() {
        pathList.clear()
        drawingPoints.clear()
        path.reset()
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        pathList.forEach {
            paint.color = it.color
            canvas.drawPath(it.path, paint)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isDrawing) return true

        val x = event.x
        val y = event.y

        return when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(x, y)
                pathList.add(DrawPath(currentBrush, path))
                drawingPoints.add(DrawPoints("move", currentBrush, x, y))
                invalidate()
                true
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(x, y)
                drawingPoints.add(DrawPoints("line", currentBrush, x, y))
                invalidate()
                true
            }
            MotionEvent.ACTION_UP -> {
                path.lineTo(x, y)
                drawingPoints.add(DrawPoints("line", currentBrush, x, y))
                invalidate()
                sendData(drawingPoints)
                true
            }
            else -> false
        }
    }
}