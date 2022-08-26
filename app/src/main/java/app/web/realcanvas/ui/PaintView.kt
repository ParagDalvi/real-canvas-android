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
import app.web.realcanvas.models.DoWhatWhenDrawing
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
    private lateinit var fragment: GameFragment

    fun init(fragment: GameFragment) {
        paint.isAntiAlias = true
        paint.color = currentBrush
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = defaultWidth
        this.fragment = fragment
    }

    fun setIsDrawing(b: Boolean) {
        isDrawing = b
    }

    private fun updateDrawing(list: List<DrawPoints>?) {
        path.reset()
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

    //todo: undo not working
    fun undo(shouldUpdateOthers: Boolean = false) {
        if (drawingPoints.size >= 20)
            drawingPoints.dropLast(20)
        else
            drawingPoints.clear()
        updateDrawing(drawingPoints)
        if (shouldUpdateOthers)
            fragment.sendDrawingToOthers(drawingPoints, DoWhatWhenDrawing.UNDO)
    }

    fun clear(shouldUpdateOthers: Boolean = false) {
        reset()
        updateDrawing(drawingPoints)
        if (shouldUpdateOthers)
            fragment.sendDrawingToOthers(drawingPoints, DoWhatWhenDrawing.CLEAR)
    }

    fun add(newList: List<DrawPoints>) {
        drawingPoints.addAll(newList)
        updateDrawing(drawingPoints)
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
                fragment.sendDrawingToOthers(drawingPoints, DoWhatWhenDrawing.ADD)
                drawingPoints.clear()
                true
            }
            else -> false
        }
    }
}