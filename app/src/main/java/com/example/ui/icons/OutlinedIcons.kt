package androidx.compose.material.icons.outlined

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

val Icons.Outlined.RadioButtonUnchecked: ImageVector
    get() = _radioButtonUnchecked ?: materialIcon(name = "Outlined.RadioButtonUnchecked") {
        materialPath {
            moveTo(12.0f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
            reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
            reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
            close()
            moveTo(12.0f, 20.0f)
            curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
            reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
            reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
            reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
            close()
        }
    }.also { _radioButtonUnchecked = it }
private var _radioButtonUnchecked: ImageVector? = null

val Icons.Outlined.CheckCircle: ImageVector
    get() = _checkCircleOutlined ?: materialIcon(name = "Outlined.CheckCircle") {
        materialPath {
            moveTo(12.0f, 2.0f)
            curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
            reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
            reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
            reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
            close()
            moveTo(12.0f, 20.0f)
            curveToRelative(-4.41f, 0.0f, -8.0f, -3.59f, -8.0f, -8.0f)
            reflectiveCurveToRelative(3.59f, -8.0f, 8.0f, -8.0f)
            reflectiveCurveToRelative(8.0f, 3.59f, 8.0f, 8.0f)
            reflectiveCurveToRelative(-3.59f, 8.0f, -8.0f, 8.0f)
            close()
            moveTo(16.59f, 7.58f)
            lineTo(10.0f, 14.17f)
            lineToRelative(-2.59f, -2.58f)
            lineTo(6.0f, 13.0f)
            lineToRelative(4.0f, 4.0f)
            lineToRelative(8.0f, -8.0f)
            close()
        }
    }.also { _checkCircleOutlined = it }
private var _checkCircleOutlined: ImageVector? = null
