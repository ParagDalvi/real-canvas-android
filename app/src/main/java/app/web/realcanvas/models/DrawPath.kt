package app.web.realcanvas.models

import android.graphics.Path
import kotlinx.serialization.Serializable

data class DrawPath(
    val color: Int,
    val path: Path
)

@Serializable
data class DrawPoints(
    val what: String,
    val color: Int,
    val x: Float,
    val y: Float
)