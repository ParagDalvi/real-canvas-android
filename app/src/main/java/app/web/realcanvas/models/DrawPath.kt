package app.web.realcanvas.models

import android.graphics.Path
import kotlinx.serialization.Serializable

data class DrawPath(
    val color: Int,
    val path: Path
)

enum class DoWhatWhenDrawing {
    ADD, UNDO, CLEAR
}

@Serializable
data class DrawPoints(
    val what: String,
    val color: Int,
    var x: Float,
    var y: Float
)