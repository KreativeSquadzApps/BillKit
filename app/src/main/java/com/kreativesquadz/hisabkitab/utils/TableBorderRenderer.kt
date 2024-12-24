package com.kreativesquadz.hisabkitab.utils

import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.renderer.TableRenderer
import com.itextpdf.layout.element.Table
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.layout.renderer.DrawContext

class TableBorderRenderer(modelElement: Table) : TableRenderer(modelElement) {

    override fun getNextRenderer(): TableBorderRenderer {
        return TableBorderRenderer(modelElement as Table)
    }

    override fun drawBorders(drawContext: DrawContext) {
        val rect = occupiedAreaBBox
        val currentPage: PdfPage = drawContext.document.getPage(occupiedArea.pageNumber)
        val aboveCanvas = PdfCanvas(currentPage.newContentStreamAfter(), currentPage.resources, drawContext.document)

        val lineWidth = 0.5f
        rect.applyMargins(lineWidth, lineWidth, lineWidth, lineWidth, false)

        // Draw the white border first
        aboveCanvas.saveState()
            .setLineWidth(lineWidth)
            .setStrokeColor(DeviceRgb(255, 255, 255))
            .rectangle(rect.left.toDouble(), rect.bottom.toDouble(), rect.width.toDouble(), rect.height.toDouble())
            .stroke()
            .restoreState()

        // Draw the red rounded border
        aboveCanvas.saveState()
            .setLineWidth(lineWidth)
            .setStrokeColor(DeviceRgb(255, 0, 0))
            .roundRectangle(rect.left.toDouble(), rect.bottom.toDouble(), rect.width.toDouble(), rect.height.toDouble(), 5f.toDouble())
            .stroke()
            .restoreState()

        super.drawBorders(drawContext)
    }

    override fun drawChildren(drawContext: DrawContext) {
        val rect = occupiedAreaBBox
        val lineWidth = 0.5f
        rect.applyMargins(lineWidth, lineWidth, lineWidth, lineWidth, false)

        val canvas = drawContext.canvas
        canvas.saveState()
            .roundRectangle(rect.left.toDouble(), rect.bottom.toDouble(), rect.width.toDouble(), rect.height.toDouble(), 5f.toDouble())
            .clip()
            .endPath()

        super.drawChildren(drawContext)

        canvas.restoreState()
    }
}
