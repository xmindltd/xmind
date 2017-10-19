package org.xmind.ui.internal.print.multipage;

import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.figures.SheetFigure;
import org.xmind.ui.mindmap.IMindMap;

class PrintMultipageUtils {

    public static Rectangle getSheetFigureBounds(IGraphicalEditorPage page,
            IMindMap mindmap) {
        Rectangle extent = new Rectangle(
                getSheetFigure(page, mindmap).getFreeformExtent());
        int margin = getMargin(extent);

        return extent.expand(new Insets(margin));
    }

    private static SheetFigure getSheetFigure(IGraphicalEditorPage page,
            IMindMap mindmap) {
        return (SheetFigure) page.getViewer()
                .findGraphicalPart(mindmap.getSheet()).getContentPane();
    }

    public static int getMargin(Rectangle sourceArea) {
        int margin = Math.max(sourceArea.width, sourceArea.height) / 100;
        return Math.max(2, Math.min(margin, 10));
    }

}
