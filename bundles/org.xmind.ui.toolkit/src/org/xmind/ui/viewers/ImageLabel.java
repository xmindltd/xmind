/* ******************************************************************************
 * Copyright (c) 2006-2015 XMind Ltd. and others.
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
package org.xmind.ui.viewers;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Instances of this class are controls that draws an image as its content.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>(none)</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * 
 * @author Frank Shaka
 * @since 3.6.0
 */
public class ImageLabel extends Composite {

    /**
     * Does not scale image.
     */
    public static final int SCALE_NONE = 0;

    /**
     * Scales the image so that the whole image can be shown in the control.
     */
    public static final int SCALE_TO_FIT = 1 << 0;

    /**
     * Does not scale image when it is needed to expand the image.
     * <p>
     * Only valid when SCALE_TO_FIT is specified.
     * </p>
     */
    public static final int SCALE_NO_EXPAND = 1 << 10;

    private Image image = null;

    private int hAlignment = SWT.CENTER;

    private int vAlignment = SWT.CENTER;

    private int scaleHint = SCALE_NONE;

    public ImageLabel(Composite parent, int style) {
        super(parent, style);
        addListener(SWT.Paint, new Listener() {
            public void handleEvent(Event event) {
                onPaint(event);
            }
        });
    }

    private void onPaint(Event event) {
        paintImageLabel(event.gc);
    }

    protected void paintImageLabel(GC gc) {
        if (image == null)
            return;

        Rectangle imgArea = image.getBounds();
        if (imgArea.width == 0 || imgArea.height == 0)
            return;

        Rectangle target = computePaintArea(imgArea, getClientArea());

        gc.setAntialias(SWT.ON);
        gc.drawImage(image, imgArea.x, imgArea.y, imgArea.width, imgArea.height,
                target.x, target.y, target.width, target.height);
    }

    protected Rectangle computePaintArea(Rectangle imgArea,
            Rectangle clientArea) {
        Rectangle target = new Rectangle(clientArea.x, clientArea.y,
                imgArea.width, imgArea.height);

        if ((scaleHint & SCALE_TO_FIT) != 0) {
            // Let scales be
            //     hScale = ctlSize.width / imgSize.width
            //     vScale = ctlSize.height / imgSize.height
            // We will choose the control height as the target height if the
            // vScale is smaller than the hScale, so we get the inequation:
            //     ctlSize.width / imgSize.width > ctlSize.height / imgSize.height
            // To avoid float point calculations, we transform the above 
            // inequation into an equivalent one:
            if (clientArea.width * imgArea.height > clientArea.height
                    * imgArea.width) {
                // use control height as target height, calculate target width
                target.height = clientArea.height;
                target.width = imgArea.width * clientArea.height
                        / imgArea.height;
            } else {
                // use control width as target width, calculate target height
                target.width = clientArea.width;
                target.height = imgArea.height * clientArea.width
                        / imgArea.width;
            }

            if ((scaleHint & SCALE_NO_EXPAND) != 0
                    && (target.width > imgArea.width
                            || target.height > imgArea.height)) {
                target.width = imgArea.width;
                target.height = imgArea.height;
            }
        }

        if ((hAlignment & SWT.CENTER) != 0) {
            target.x += (clientArea.width - target.width) / 2;
        } else if ((hAlignment & SWT.TRAIL) != 0) {
            target.x += clientArea.width - target.width;
        }

        if ((vAlignment & SWT.CENTER) != 0) {
            target.y += (clientArea.height - target.height) / 2;
        } else if ((vAlignment & SWT.TRAIL) != 0) {
            target.y += clientArea.height - target.height;
        }
        return target;
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point size;
        if (wHint != SWT.DEFAULT && hHint != SWT.DEFAULT) {
            size = new Point(wHint, hHint);
        } else if (image == null) {
            size = new Point(0, 0);
        } else {
            Rectangle b = image.getBounds();
            if (b.width == 0 || b.height == 0) {
                size = new Point(0, 0);
            } else {
                size = new Point(b.width, b.height);
                if (wHint != SWT.DEFAULT) {
                    size.x = wHint;
                    size.y = b.height * wHint / b.width;
                } else if (hHint != SWT.DEFAULT) {
                    size.y = hHint;
                    size.x = b.width * hHint / b.height;
                }
            }
        }
        Rectangle trimmed = computeTrim(0, 0, size.x, size.y);
        return new Point(trimmed.width, trimmed.height);
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        checkWidget();
        if (image == this.image)
            return;
        this.image = image;
        redraw();
    }

    public int getHorizontalAlignment() {
        return hAlignment;
    }

    /**
     * 
     * @param alignment
     *            one of SWT.LEAD, SWT.CENTER, SWT.TRAIL
     */
    public void setHorizontalAlignment(int alignment) {
        checkWidget();
        if (alignment == this.hAlignment)
            return;
        this.hAlignment = alignment;
        redraw();
    }

    public int getVerticalAlignment() {
        return vAlignment;
    }

    public void setVerticalAlignment(int alignment) {
        checkWidget();
        if (alignment == this.vAlignment)
            return;
        this.vAlignment = alignment;
        redraw();
    }

    public int getScaleHint() {
        return scaleHint;
    }

    /**
     * Specifies how to scale the image to fit in the control.
     * 
     * <p>
     * The hint value is either one of the <code>SCALE_*</code> hint constants
     * defined in this class, or must be built by <em>bitwise OR</em>'ing
     * together (that is, using the <code>int</code> "|" operator) two or more
     * of those hint constants.
     * </p>
     * 
     * @param hint
     *            one of SCALE_NONE, SCALE_TO_FIT, SCALE_TO_FILL,
     *            SCALE_TO_EXPAND, SCALE_TO_SHRINK
     */
    public void setScaleHint(int hint) {
        checkWidget();

        if ((hint & SCALE_TO_FIT) == 0) {
            hint = SCALE_NONE;
        }

        if (hint == this.scaleHint)
            return;
        this.scaleHint = hint;
        redraw();
    }

}
