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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.service.BendPointsFeedback;
import org.xmind.gef.service.CompositeFeedback;
import org.xmind.gef.service.IBendPointsFeedback;
import org.xmind.gef.service.IRectangleProvider;
import org.xmind.gef.service.RectangleFeedback;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;

public class ImageFeedback extends CompositeFeedback implements
        IBendPointsFeedback {

    private ImagePart part;

    private RectangleFeedback border;

    private BendPointsFeedback points;

    public ImageFeedback(ImagePart part) {
        this.part = part;

        IRectangleProvider boundsProvider = new IRectangleProvider() {
            public Rectangle getRectangle() {
                return getPart().getFigure().getBounds();
            }
        };

        border = new RectangleFeedback();
        border.setBorderColor(ColorUtils.getColor("#0040f0")); //$NON-NLS-1$
        border.setFillColor(ColorUtils.getColor("#0040f0")); //$NON-NLS-1$
        border.setBoundsProvider(boundsProvider);
        border.setBorderAlpha(0xff);
        border.setFillAlpha(0);

        points = new BendPointsFeedback();
        points.setBorderColor(ColorUtils.getColor("#0040ff")); //$NON-NLS-1$
        points.setFillColor(ColorUtils.getColor("#30a0f0")); //$NON-NLS-1$
        points.setBoundsProvider(boundsProvider);
        points.setAlpha(0xff);
        points.setHidePointLength(MindMapUI.HIDE_BEND_POINT_LENGTH);
        points.setPointHeight(MindMapUI.HEIGHT_SQUARE);

        addFeedback(border);
        addFeedback(points);
    }

    public IImagePart getPart() {
        return part;
    }

    public RectangleFeedback getBorder() {
        return border;
    }

    public BendPointsFeedback getPoints() {
        return points;
    }

    public IRectangleProvider getBoundsProvider() {
        return points.getBoundsProvider();
    }

    public int getOrientation(Point point) {
        return points.getOrientation(point);
    }

    public void setBoundsProvider(IRectangleProvider boundsProvider) {
        points.setBoundsProvider(boundsProvider);
        border.setBoundsProvider(boundsProvider);
    }
}