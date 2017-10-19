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
package org.xmind.gef.draw2d;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Image;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.draw2d.geometry.PrecisionDimension;
import org.xmind.gef.draw2d.geometry.PrecisionRectangle;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;

/**
 * @author Frank Shaka
 */
public class RotatableSizeableImageFigure extends SizeableImageFigure implements
        IRotatableFigure {

    private PrecisionRotator rotator = new PrecisionRotator();

    private PrecisionDimension normalPrefSize = null;

    private Dimension rotatedPrefSize = null;

    public RotatableSizeableImageFigure() {
    }

    public RotatableSizeableImageFigure(Image image) {
        super(image);
    }

    public PrecisionDimension getNormalPreferredSize(int wHint, int hHint) {
        if (normalPrefSize == null) {
            normalPrefSize = new PrecisionDimension(super.getPreferredSize(
                    wHint, hHint));
        }
        return normalPrefSize;
    }

    public Dimension getPreferredSize(int wHint, int hHint) {
        if (rotatedPrefSize == null) {
            rotatedPrefSize = rotator.td(getNormalPreferredSize(wHint, hHint))
                    .toDraw2DDimension();
        }
        return rotatedPrefSize;
    }

    public void invalidate() {
        super.invalidate();
        normalPrefSize = null;
        rotatedPrefSize = null;
    }

    public double getRotationDegrees() {
        return rotator.getAngle();
    }

    public void setRotationDegrees(double angle) {
        double oldAngle = getRotationDegrees();
        if (Math.abs(oldAngle - angle) < 0.00000001d)
            return;
        rotator.setAngle(angle);
        revalidate();
        repaint();
    }

    private boolean isRotated() {
        return !Geometry.isSameAngleDegree(getRotationDegrees(), 0, 0.0000001);
    }

    protected void paintImage(Graphics graphics, Image image,
            Dimension imageSize, Rectangle clientArea) {
        if (!isRotated()) {
            super.paintImage(graphics, image, imageSize, clientArea);
            return;
        }
//        Dimension realClientSize = rotator.a(clientArea.getSize(),
//                imageSize.width, -1);
        Point center = clientArea.getCenter();
        rotator.setOrigin(center.x, center.y);
        PrecisionRectangle r = rotator.r(new PrecisionRectangle(clientArea),
                imageSize.width, -1).translate(-center.x, -center.y);
//        Rectangle realClientArea = new Rectangle();
//        realClientArea.setSize(realClientSize);
//        Dimension delta = realClientSize.getScaled(0.5);
//        realClientArea.translate(-delta.width, -delta.height);

        graphics.pushState();
        try {
            graphics.translate(center.x, center.y);
            graphics.rotate((float) getRotationDegrees());
            super.paintImage(graphics, image, imageSize, r.toDraw2DRectangle());
            graphics.translate(-center.x, -center.y);
        } finally {
            graphics.popState();
            graphics.restoreState();
        }
    }

}