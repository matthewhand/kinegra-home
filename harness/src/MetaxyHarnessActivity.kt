package tv.metaxy.proof

import android.app.Activity
import android.os.Bundle
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.KeyEvent
import android.view.View
import android.content.Context

/**
 * Offline TV harness. Draws synthetic bars INSIDE ITS OWN ACTIVITY.
 * No system alert window, Video APIs, MediaProjection, WebView, network
 * or notification/accessibility service is constructed.
 */
class MetaxyHarnessActivity : Activity() {
    private lateinit var surface: SyntheticSurface
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        surface = SyntheticSurface(this)
        setContentView(surface)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_LEFT -> { surface.previous(); return true }
            KeyEvent.KEYCODE_DPAD_RIGHT -> { surface.next(); return true }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                // Gate A is fully permissionless; CENTER performs no query.
                surface.showPermissionlessStatus()
                return true
            }
            else -> return super.onKeyDown(keyCode, event)
        }
    }


}

/** All graphical regions are inside this app. Never overlays another app. */
private class SyntheticSurface(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mode = HarnessMode.PILLARBOX
    private var margin = SyntheticSceneModel.margins(mode)
    private var accessNote = "USAGE ACCESS DISABLED / NO PERMISSION"

    fun next() {
        mode = HarnessMode.entries[(mode.ordinal + 1) % HarnessMode.entries.size]
        margin = SyntheticSceneModel.margins(mode)
        accessNote = "USAGE ACCESS DISABLED / NO PERMISSION"
        invalidate()
    }

    fun previous() {
        mode = HarnessMode.entries[(mode.ordinal + HarnessMode.entries.size - 1) %
            HarnessMode.entries.size]
        margin = SyntheticSceneModel.margins(mode)
        accessNote = "USAGE ACCESS DISABLED / NO PERMISSION"
        invalidate()
    }

    fun showPermissionlessStatus() {
        // No Android special-access API invocation or background task.
        accessNote = "CENTER PRESSED / NO ACCESS / NO QUERY"
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.scale(width / 1280f, height / 720f)
        rect(canvas, 0f, 0f, 1280f, 720f, Color.rgb(11, 18, 30))
        text(canvas, "METAXY TV / OFFLINE TEST HARNESS", 36f, 58f, 30f, Color.WHITE)
        text(canvas, "NO YOUTUBE PIXELS / NO SYSTEM OVERLAY", 36f, 90f, 19f, 0xff70cfdb.toInt())
        val top = 135f
        val bottom = 564f
        rect(canvas, 0f, top, 1280f, bottom, Color.BLACK)
        val visibleLeft = if (mode == HarnessMode.PILLARBOX) 160f else 0f
        val visibleRight = if (mode == HarnessMode.PILLARBOX) 1120f else 1280f
        if (mode != HarnessMode.UNKNOWN) {
            rect(canvas, visibleLeft, top, visibleRight, bottom, Color.rgb(25, 64, 104))
            for (i in 0 until 8) {
                val x0 = visibleLeft + (visibleRight-visibleLeft)*i/8f
                val x1 = visibleLeft + (visibleRight-visibleLeft)*(i+1)/8f
                rect(canvas, x0, top+120f, x1, bottom-100f,
                    Color.rgb(40+i*21, 60+(i%4)*27, 130+(i%3)*29))
            }
            text(canvas, "SYNTHETIC VIDEO REGION", visibleLeft+35f, top+64f, 23f, Color.WHITE)
        } else {
            rect(canvas, 0f, top, 1280f, bottom, Color.rgb(31, 34, 42))
            text(canvas, "UNKNOWN - PANES HIDDEN", 370f, 330f, 30f, Color.WHITE)
        }
        if (margin != null && mode == HarnessMode.PILLARBOX) {
            val leftPanelWidth = margin!!.left / 320f * 1280f
            val rightPanelWidth = margin!!.right / 320f * 1280f
            rect(canvas, 12f, top+12f, leftPanelWidth-12f, bottom-12f, 0xff286777.toInt())
            rect(canvas, 1280f-rightPanelWidth+12f, top+12f, 1268f, bottom-12f,
                0xff286777.toInt())
            text(canvas, "LEFT", 28f, top+185f, 24f, Color.WHITE)
            text(canvas, "RIGHT", 1144f, top+185f, 24f, Color.WHITE)
        }
        val readiness = if (margin != null) "READY FOR SYNTHETIC PANES" else "HIDDEN"
        text(canvas, "MODE: " + mode.name + " / " + readiness, 34f, 612f, 21f, Color.WHITE)
        text(canvas, accessNote,
            34f, 644f, 18f, 0xffb6c6d3.toInt())
        text(canvas, "LEFT/RIGHT: change mode   CENTER: NO QUERY   BACK: exit",
            34f, 680f, 20f, 0xff70cfdb.toInt())
        canvas.restore()
    }

    private fun rect(canvas: Canvas, x0: Float, y0: Float, x1: Float, y1: Float, color: Int) {
        paint.color = color
        paint.style = Paint.Style.FILL
        canvas.drawRect(x0, y0, x1, y1, paint)
    }

    private fun text(canvas: Canvas, s: String, x: Float, y: Float, size: Float, color: Int) {
        paint.color = color
        paint.style = Paint.Style.FILL
        paint.textSize = size
        canvas.drawText(s, x, y, paint)
    }
}
