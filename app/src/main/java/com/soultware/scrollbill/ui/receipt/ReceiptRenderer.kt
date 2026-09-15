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
        canvas.drawRoundRect(
            RectF(RECEIPT_LEFT, RECEIPT_TOP, RECEIPT_RIGHT, RECEIPT_BOTTOM),
            RECEIPT_RADIUS,
            RECEIPT_RADIUS,
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFFE1E1E1.toInt()
                style = Paint.Style.STROKE
                strokeWidth = 3f
            },
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
        val summaryValuePaint = paint(
            color = INK_COLOR,
            textSize = 34f,
            typeface = Typeface.create("monospace", Typeface.BOLD),
        )
        val footerPaint = paint(
            color = MUTED_INK_COLOR,
            textSize = 24f,
            typeface = Typeface.create("monospace", Typeface.NORMAL),
        )

        canvas.drawRect(CONTENT_LEFT, 110f, CONTENT_LEFT + 54f, 118f, paintFill(ACCENT_COLOR))
        canvas.drawText("SCROLLBILL", CONTENT_LEFT, 166f, headingPaint)
        canvas.drawText("WEEKLY PHONE RECEIPT", CONTENT_LEFT, 214f, monoHeadingPaint)
        canvas.drawText(
            formatReceiptDateRange(snapshot.startDate, snapshot.endDateInclusive),
            CONTENT_LEFT,
            264f,
            monoHeadingPaint,
        )
        drawDivider(canvas, 310f)

        canvas.drawText(formatReceiptDuration(snapshot.totalUsageMinutes), CONTENT_LEFT, 446f, heroPaint)
        canvas.drawText("TOTAL APP TIME THIS WEEK", CONTENT_LEFT, 492f, heroLabelPaint)
        drawDivider(canvas, 556f)

        canvas.drawText("APP TIME", CONTENT_LEFT, 650f, sectionPaint)
        val rows = snapshot.topApps.map { it.displayLabel to it.durationMinutes }.toMutableList()
        if (snapshot.otherAppsUsageMinutes > 0L) {
            rows += "OTHER APPS" to snapshot.otherAppsUsageMinutes
        }
        rows.forEachIndexed { index, row ->
            drawReceiptRow(canvas, row.first, row.second, 724f + index * ROW_HEIGHT, monoPaint)
        }

        val summaryDividerY = 724f + rows.size * ROW_HEIGHT + 36f
        drawDivider(canvas, summaryDividerY)
        drawSummaryLine(
            canvas,
            "TOTAL",
            formatReceiptDuration(snapshot.totalUsageMinutes),
            summaryDividerY + 92f,
            sectionPaint,
            summaryValuePaint,
        )
        drawSummaryLine(
            canvas,
            "DAILY AVERAGE",
            formatReceiptDuration(snapshot.dailyAverageMinutes),
            summaryDividerY + 164f,
            monoPaint,
            monoPaint,
        )
        drawSummaryLine(
            canvas,
            "YEARLY PACE",
            formatYearlyPace(snapshot.projectedAnnualDays),
            summaryDividerY + 236f,
            monoPaint,
            monoPaint,
        )

        drawDivider(canvas, FOOTER_DIVIDER_Y)
        canvas.drawText("TIME IS NON-REFUNDABLE.", CONTENT_LEFT, 1_606f, sectionPaint)
        canvas.drawText("Made with ScrollBill", CONTENT_LEFT, 1_724f, footerPaint)

        return bitmap
    }

    private fun drawReceiptRow(
        canvas: Canvas,
        label: String,
        durationMinutes: Long,
        baseline: Float,
        textPaint: Paint,
    ) {
        val duration = formatReceiptDuration(durationMinutes)
        val durationWidth = textPaint.measureText(duration)
        val durationX = CONTENT_RIGHT
        val maxLabelWidth = durationX - durationWidth - LABEL_DURATION_GAP
        val fittedLabel = ellipsizeToWidth(
            text = label.uppercase(Locale.US),
            maxWidth = maxLabelWidth,
            measureText = textPaint::measureText,
        )
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
        labelPaint: Paint,
        valuePaint: Paint,
    ) {
        canvas.drawText(label, CONTENT_LEFT, baseline, labelPaint)
        valuePaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(value, CONTENT_RIGHT, baseline, valuePaint)
        valuePaint.textAlign = Paint.Align.LEFT
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

    private fun paintFill(color: Int): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { this.color = color }

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
        private const val ROW_HEIGHT = 78f
        private const val FOOTER_DIVIDER_Y = 1_500f
    }
}
