/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.spreadsheet;

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Font;
import org.xmind.ui.internal.spreadsheet.structures.Chart2;
import org.xmind.ui.internal.spreadsheet.structures.Row2;
import org.xmind.ui.internal.spreadsheet.structures.RowHead;
import org.xmind.ui.texteditor.FloatingTextEditorHelperBase;

public class RowHeadEditorHelper extends FloatingTextEditorHelperBase {

    private Chart2 chart;

    private Row2 row;

    private RowHead rowHead;

    private Rectangle bounds;

    public RowHeadEditorHelper() {
        super();
    }

    public RowHeadEditorHelper(boolean extend) {
        super(extend);
    }

    public Chart2 getChart() {
        return chart;
    }

    public void setChart(Chart2 chart) {
        this.chart = chart;
    }

    public Row2 getRow() {
        return row;
    }

    public void setRow(Row2 row) {
        this.row = row;
    }

    public RowHead getRowHead() {
        return rowHead;
    }

    public void setRowHead(RowHead rowHead) {
        this.rowHead = rowHead;
    }

    public void activate() {
        bounds = null;
        super.activate();

        if (getEditor() != null && getViewer() != null && getRow() != null
                && getChart() != null && getRowHead() != null) {
            Rectangle b = getPreferredBounds();
            Point loc = getViewer().computeToControl(b.getLocation(), true);
            getEditor().setInitialLocation(
                    new org.eclipse.swt.graphics.Point(loc.x, loc.y));
            getEditor().setInitialSize(
                    new org.eclipse.swt.graphics.Point(b.width, b.height));
        }
    }

    public void deactivate() {
        bounds = null;
        super.deactivate();
    }

    protected Rectangle getPreferredBounds() {
        if (bounds == null) {
            bounds = calcBounds();
        }
        return bounds.getCopy();
    }

    private Rectangle calcBounds() {
        Dimension size = rowHead.getPrefSize();
        int width = chart.getRowHeadWidth() + chart.getMinorSpacing();
        int height = row.getHeight();
        int x = chart.getTitle().getTopicPart().getFigure().getBounds().x
                + (width - size.width) / 2;
        int y = row.getTop() + (height - size.height) / 2;
        return new Rectangle(x, y, size.width, size.height);
    }

    protected Font getPreferredFont() {
        return rowHead.getFont();
    }

}