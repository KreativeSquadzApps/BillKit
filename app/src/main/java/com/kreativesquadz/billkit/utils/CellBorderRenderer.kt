package com.kreativesquadz.billkit.utils

import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.layout.renderer.CellRenderer
import com.itextpdf.layout.element.Cell
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.layout.renderer.DrawContext

class RoundedCellRenderer(modelElement: Cell) : CellRenderer(modelElement) {

    override fun getNextRenderer(): RoundedCellRenderer {
        return RoundedCellRenderer(modelElement as Cell)
    }

    override fun draw(drawContext: DrawContext) {
        // Get the occupied area of the cell
        val rect = occupiedAreaBBox
        val currentPage: PdfPage = drawContext.document.getPage(occupiedArea.pageNumber)
        val canvas = PdfCanvas(currentPage.newContentStreamAfter(), currentPage.resources, drawContext.document)

        val lineWidth = 1f
        rect.applyMargins(lineWidth, lineWidth, lineWidth, lineWidth, false)

        // Draw a white border first
        canvas.saveState()
            .setLineWidth(lineWidth)
            .setStrokeColor(DeviceRgb(255, 255, 255)) // White border for spacing
            .rectangle(rect.left.toDouble(), rect.bottom.toDouble(), rect.width.toDouble(), rect.height.toDouble())
            .stroke()
            .restoreState()

        // Draw the rounded border
        canvas.saveState()
            .setLineWidth(lineWidth)
            .setStrokeColor(DeviceRgb(0, 0, 0))
            .roundRectangle(
                rect.left.toDouble(),
                rect.bottom.toDouble(),
                rect.width.toDouble(),
                rect.height.toDouble(),
                10f.toDouble() // Radius for rounded corners
            )
            .stroke()
            .restoreState()

        // Call the parent draw method to render the cell content
        super.draw(drawContext)
    }
}
