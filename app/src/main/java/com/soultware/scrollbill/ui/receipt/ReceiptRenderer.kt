package com.soultware.scrollbill.ui.receipt

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.soultware.scrollbill.domain.receipt.ReceiptSnapshot
import java.util.Locale

fun interface ReceiptRenderer {
    fun render(snapshot: ReceiptSnapshot): Bitmap
}

class ClassicReceiptRenderer : ReceiptRenderer {
    override fun render(snapshot: ReceiptSnapshot): Bitmap {
        val bitmap = Bitmap.createBitmap(CANVAS_WIDTH, CANVAS_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(BACKGROUND_COLOR)
        canvas.drawRoundRect(
            RectF(RECEIPT_LEFT, RECEIPT_TOP, RECEIPT_RIGHT, RECEIPT_BOTTOM),
            RECEIPT_RADIUS,
            RECEIPT_RADIUS,
            Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE },
        )

        val headingPaint = paint(
            color = INK_COLOR,
            textSize = 42f,
            typeface = Typeface.create("sans-serif", Typeface.BOLD),
        )
        val monoHeadingPaint = paint(
            color = MUTED_INK_COLOR,
            textSize = 22f,
            typeface = Typeface.create("monospace", Typeface.BOLD),
        )
        val monoPaint = paint(
            color = INK_COLOR,
            textSize = 25f,
            typeface = Typeface.create("monospace", Typeface.NORMAL),
        )
        val heroPaint = paint(
            color = INK_COLOR,
            textSize = 100f,
            typeface = Typeface.create("sans-serif", Typeface.BOLD),
        )
        val heroLabelPaint = paint(
            color = ACCENT_COLOR,
            textSize = 20f,
            typeface = Typeface.create("monospace", Typeface.BOLD),
        )
        val sectionPaint = paint(
            color = INK_COLOR,
            textSize = 27f,
            typeface = Typeface.create("monospace", Typeface.BOLD),
        )
        val footerPaint = paint(
            color = MUTED_INK_COLOR,
            textSize = 24f,
            typeface = Typeface.create("monospace", Typeface.NORMAL),
        )

        canvas.drawText("SCROLLBILL", CONTENT_LEFT, 154f, headingPaint)
        canvas.drawText("WEEKLY PHONE RECEIPT", CONTENT_LEFT, 198f, monoHeadingPaint)
        canvas.drawText(
            formatReceiptDateRange(snapshot.startDate, snapshot.endDateInclusive),
            CONTENT_LEFT,
            244f,
            monoHeadingPaint,
        )
        drawDivider(canvas, 286f)

        canvas.drawText(formatReceiptDuration(snapshot.totalUsageMillis), CONTENT_LEFT, 406f, heroPaint)
        canvas.drawText("TOTAL WEEKLY APP TIME", CONTENT_LEFT, 450f, heroLabelPaint)
        drawDivider(canvas, 510f)

        canvas.drawText("APP TIME", CONTENT_LEFT, 590f, sectionPaint)
        val rows = snapshot.topApps.map { it.displayLabel to it.durationMillis }.toMutableList()
        if (snapshot.otherAppsUsageMillis > 0L) {
            rows += "OTHER APPS" to snapshot.otherAppsUsageMillis
        }
        rows.forEachIndexed { index, row ->
            drawReceiptRow(canvas, row.first, row.second, 665f + index * ROW_HEIGHT, monoPaint)
        }

        val summaryDividerY = 665f + rows.size * ROW_HEIGHT + 35f
        drawDivider(canvas, summaryDividerY)
        drawSummaryLine(canvas, "TOTAL", formatReceiptDuration(snapshot.totalUsageMillis), summaryDividerY + 86f, monoPaint)
        drawSummaryLine(canvas, "DAILY AVERAGE", formatReceiptDuration(snapshot.dailyAverageMillis), summaryDividerY + 148f, monoPaint)
        drawSummaryLine(canvas, "YEARLY PACE", formatYearlyPace(snapshot.projectedAnnualUsageMillis), summaryDividerY + 210f, monoPaint)

        drawDivider(canvas, summaryDividerY + 275f)
        canvas.drawText("TIME IS NON-REFUNDABLE.", CONTENT_LEFT, summaryDividerY + 365f, sectionPaint)
        canvas.drawText("Made with ScrollBill", CONTENT_LEFT, summaryDividerY + 445f, footerPaint)

        return bitmap
    }

    private fun drawReceiptRow(
        canvas: Canvas,
        label: String,
        durationMillis: Long,
        baseline: Float,
        textPaint: Paint,
    ) {
        val duration = formatReceiptDuration(durationMillis)
        val durationWidth = textPaint.measureText(duration)
        val durationX = CONTENT_RIGHT
        val maxLabelWidth = durationX - durationWidth - LABEL_DURATION_GAP
        val fittedLabel = ellipsizeToWidth(label.uppercase(Locale.US), textPaint, maxLabelWidth)
        canvas.drawText(fittedLabel, CONTENT_LEFT, baseline, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(duration, durationX, baseline, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawSummaryLine(
        canvas: Canvas,
        label: String,
        value: String,
        baseline: Float,
        textPaint: Paint,
    ) {
        canvas.drawText(label, CONTENT_LEFT, baseline, textPaint)
        textPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, CONTENT_RIGHT, baseline, textPaint)
        textPaint.textAlign = Paint.Align.LEFT
    }

    private fun drawDivider(canvas: Canvas, y: Float) {
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = DIVIDER_COLOR
            strokeWidth = 2f
        }
        canvas.drawLine(CONTENT_LEFT, y, CONTENT_RIGHT, y, dividerPaint)
    }

    private fun paint(color: Int, textSize: Float, typeface: Typeface): Paint =
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            this.textSize = textSize
            this.typeface = typeface
            textAlign = Paint.Align.LEFT
            isSubpixelText = true
        }

    companion object {
        const val CANVAS_WIDTH = 1_080
        const val CANVAS_HEIGHT = 1_920

        private const val BACKGROUND_COLOR = 0xFF171717.toInt()
        private const val INK_COLOR = 0xFF151515.toInt()
        private const val MUTED_INK_COLOR = 0xFF5F5F5F.toInt()
        private const val ACCENT_COLOR = 0xFFB24A2E.toInt()
        private const val DIVIDER_COLOR = 0xFFBEBEBE.toInt()
        private const val RECEIPT_LEFT = 68f
        private const val RECEIPT_TOP = 68f
        private const val RECEIPT_RIGHT = 1_012f
        private const val RECEIPT_BOTTOM = 1_852f
        private const val CONTENT_LEFT = 122f
        private const val CONTENT_RIGHT = 958f
        private const val RECEIPT_RADIUS = 18f
        private const val LABEL_DURATION_GAP = 32f
        private const val ROW_HEIGHT = 64f
    }
}

private fun ellipsizeToWidth(text: String, paint: Paint, maxWidth: Float): String {
    if (maxWidth <= 0f) return ""
    if (paint.measureText(text) <= maxWidth) return text

    val ellipsis = "…"
    val availableWidth = maxWidth - paint.measureText(ellipsis)
    if (availableWidth <= 0f) return ellipsis

    var end = text.length
    while (end > 0 && paint.measureText(text, 0, end) > availableWidth) {
        end--
    }
    return text.substring(0, end).trimEnd() + ellipsis
}
