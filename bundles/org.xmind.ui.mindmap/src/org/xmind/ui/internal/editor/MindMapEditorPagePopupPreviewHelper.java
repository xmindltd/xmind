package org.xmind.ui.internal.editor;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.custom.CTabFolder;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.ui.editor.GraphicalEditorPagePopupPreviewHelper;
import org.xmind.gef.ui.editor.IGraphicalEditor;

public class MindMapEditorPagePopupPreviewHelper
        extends GraphicalEditorPagePopupPreviewHelper {

    private static final int MIN_PREVIEW_WIDTH = 600;

    private static final int MIN_PREVIEW_HEIGHT = 600;

    public MindMapEditorPagePopupPreviewHelper(IGraphicalEditor editor,
            CTabFolder tabFolder) {
        super(editor, tabFolder);
    }

    protected Rectangle calcContentsBounds(IFigure contents,
            IGraphicalViewer viewer) {
        Rectangle bounds = super.calcContentsBounds(contents, viewer);
        int max = Math.max(bounds.width, bounds.height) + 50;

        int newWidth = bounds.width;
        if (newWidth < MIN_PREVIEW_WIDTH) {
            newWidth = MIN_PREVIEW_WIDTH;
        }
        if (newWidth < max) {
            newWidth = max;
        }

        if (newWidth != bounds.width) {
            int ex = (newWidth - bounds.width) / 2;
            Rectangle b = contents.getBounds();
            int right = bounds.x + bounds.width;
            bounds.x = Math.max(b.x, bounds.x - ex);
            bounds.width = Math.min(b.x + b.width, right + ex) - bounds.x;
        }

        int newHeight = bounds.height;
        if (newHeight < MIN_PREVIEW_HEIGHT) {
            newHeight = MIN_PREVIEW_HEIGHT;
        }
        if (newHeight < max) {
            newHeight = max;
        }
        if (newHeight != bounds.height) {
            int ex = (newHeight - bounds.height) / 2;
            Rectangle b = contents.getBounds();
            int bottom = bounds.y + bounds.height;
            bounds.y = Math.max(b.y, bounds.y - ex);
            bounds.height = Math.min(b.y + b.height, bottom + ex) - bounds.y;
        }
        return bounds;
    }

}