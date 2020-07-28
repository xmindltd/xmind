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
import org.xmind.ui.internal.spreadsheet.structures.Chart;
import org.xmind.ui.internal.spreadsheet.structures.Column;
import org.xmind.ui.internal.spreadsheet.structures.ColumnHead;
import org.xmind.ui.texteditor.FloatingTextEditorHelperBase;

public class ColumnHeadEditorHelper extends FloatingTextEditorHelperBase {

    private Chart chart;

    private Column column;

    private ColumnHead columnHead;

    private Rectangle bounds;

    public ColumnHeadEditorHelper() {
        super();
    }

    public ColumnHeadEditorHelper(boolean extend) {
        super(extend);
    }

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public Column getColumn() {
        return column;
    }

    public void setColumn(Column column) {
        this.column = column;
    }

    public ColumnHead getColumnHead() {
        return columnHead;
    }

    public void setColumnHead(ColumnHead columnHead) {
        this.columnHead = columnHead;
    }

    public void activate() {
        bounds = null;
        super.activate();

        if (getEditor() != null && getViewer() != null && getColumn() != null
                && getChart() != null && getColumnHead() != null) {
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
        Dimension size = columnHead.getPrefSize();
        int width = column.getWidth();
        int height = chart.getColumnHeadHeight() + chart.getMajorSpacing();
        int x = column.getLeft() + (width - size.width) / 2;
        int y = chart.getTitle().getTopicPart().getFigure().getBounds().bottom()
                + chart.getLineWidth() + (height - size.height) / 2;
        if (chart.getTitle().getInfoPart() != null) {
            y += chart.getTitle().getInfoPart().getFigure().getBounds().height;
        }
        return new Rectangle(x, y, size.width, size.height);
    }

    protected Font getPreferredFont() {
        return columnHead.getFont();
    }

}