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
import app.web.realcanvas.models.DrawingData


class PaintView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private var isDrawing = false
    private val defaultWidth = 20f
    private val pathList = mutableListOf<DrawPath>()
    private val drawingPoints = mutableListOf<DrawPoints>()
    private val currentBrush = Color.BLACK
    private val paint = Paint()
    private val path = Path()
    private var previouslyPointsSentIndex = 0
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

    private fun updateDrawing(list: List<DrawPoints>) {
        path.reset()
        list.forEach {
            when (it.what) {
                "move" -> {
                    path.moveTo(it.x, it.y)
                    pathList.add(DrawPath(currentBrush, path))
                }
                "line" -> {
                    path.lineTo(it.x, it.y)
                }
            }
        }
        invalidate()
    }

    fun undo(shouldUpdateOthers: Boolean = false) {
        drawingPoints.dropLast(20).also {
            drawingPoints.clear()
            drawingPoints.addAll(it)
        }
        if (drawingPoints.isEmpty())
            previouslyPointsSentIndex = 0
        else
            previouslyPointsSentIndex -= 20
        updateDrawing(drawingPoints)
        if (shouldUpdateOthers)
            fragment.sendDrawingToOthers(drawingPoints, DoWhatWhenDrawing.UNDO)
    }

    fun clear(shouldUpdateOthers: Boolean = false) {
        reset()
        previouslyPointsSentIndex = 0
        updateDrawing(drawingPoints)
        if (shouldUpdateOthers)
            fragment.sendDrawingToOthers(drawingPoints, DoWhatWhenDrawing.CLEAR)
    }

    fun add(
        drawingData: DrawingData,
        currentWidth: Int,
        currentHeight: Int,
    ) {
        drawingData.list.forEach {
            it.x = currentWidth * it.x / drawingData.width
            it.y = currentHeight * it.y / drawingData.height
            drawingPoints.add(it)
        }
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
                updateOthers()
                true
            }
            else -> false
        }
    }

    private fun updateOthers() {
        fragment.sendDrawingToOthers(
            drawingPoints.subList(
                previouslyPointsSentIndex,
                drawingPoints.size - 1
            ), DoWhatWhenDrawing.ADD
        )
        previouslyPointsSentIndex = drawingPoints.size - 1
    }
}