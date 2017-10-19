/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.layouts;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.ui.internal.mindmap.LegendSeparatorPart;
import org.xmind.ui.mindmap.ILegendItemPart;
import org.xmind.ui.mindmap.ILegendPart;
import org.xmind.ui.mindmap.ITitleTextPart;

public class LegendLayout extends MindMapLayoutBase {

    public LegendLayout(ILegendPart part) {
        super(part);
    }

    protected void fillLayoutData(IFigure container, ReferencedLayoutData data) {
        ILegendPart legend = (ILegendPart) getPart();

        ITitleTextPart title = legend.getTitle();
        if (title != null) {
            IFigure titleFigure = title.getFigure();
            add(titleFigure, titleFigure.getPreferredSize(), 0, data);

            int spacing = 5;
            int width = -1;
            Rectangle area = data.getClientArea();
            if (area != null) {
                width = area.width;
            }
            List<ILegendItemPart> items = legend.getItems();
            for (ILegendItemPart item : items) {
                IFigure itemFigure = item.getFigure();
                int w = itemFigure.getPreferredSize().width;
                if (width < 0) {
                    width = w;
                } else {
                    width = Math.max(width, w);
                }
            }

            LegendSeparatorPart sep = (LegendSeparatorPart) legend
                    .getAdapter(LegendSeparatorPart.class);
            if (sep != null) {
                IFigure sepFigure = sep.getFigure();
                add(sepFigure, sepFigure.getPreferredSize(width, -1), 1, data);
            }

            if (items.isEmpty()) {
                add(null, new Dimension(5, 15), 1, data);
            } else {
                for (ILegendItemPart item : items) {
                    IFigure itemFigure = item.getFigure();
                    add(itemFigure, itemFigure.getPreferredSize(), spacing,
                            data);
                }
            }
        }
    }

    private void add(IFigure figure, Dimension size, int spacing,
            ReferencedLayoutData data) {
        Rectangle r = data.getClientArea();
        if (r == null) {
            r = data.createInitBounds();
            r.translate(-(size.width / 2), -(size.height / 2));
            r.setSize(size);
        } else {
            int x = r.width < size.width ? (r.x - (size.width - r.width) / 2)
                    : r.x;
            r = new Rectangle(x, r.bottom() + spacing, size.width, size.height);
        }
        if (figure != null) {
            data.put(figure, r);
        } else {
            data.add(r);
        }
    }

}