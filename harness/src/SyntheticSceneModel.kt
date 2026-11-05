package tv.metaxy.proof

/**
 * Only freshly generated in-memory fictional pixels; independent of Android.
 * These signals are explicitly synthetic, not live capture or Usage Access.
 */
enum class HarnessMode { PILLARBOX, FULLSCREEN, UNKNOWN }

object SyntheticSceneModel {
    fun margins(mode: HarnessMode): SideMargins? {
        if (mode == HarnessMode.UNKNOWN) return null
        val width = 320
        val height = 180
        val side = if (mode == HarnessMode.PILLARBOX) 40 else 0
        val luma = ByteArray(width * height)
        for (y in 0 until height) {
            for (x in side until width-side) {
                luma[y * width + x] = (35 + ((x / 7 + y / 6) * 37) % 210).toByte()
            }
        }
        val gate = MarginGate()
        val frame = LumaFrame(width, height, luma)
        // Only fictional buffers; never update production Signals.
        for (index in 1L..3L) {
            gate.observe(true, true, true, false, frame, index)
        }
        return if (gate.state == PaneState.READY) gate.margins else null
    }
}
