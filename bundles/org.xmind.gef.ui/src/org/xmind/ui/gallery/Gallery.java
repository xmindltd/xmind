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
package org.xmind.ui.gallery;

import static org.xmind.ui.gallery.GalleryLayout.ALIGN_FILL;

import java.util.List;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.RangeModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * <pre>
 *   Gallery
 *   +---------------------------------------+
 *   |  +-------+ +-------+ +-------+ +-...  |
 *   |  | Frame | | Frame | | Frame | | ...  |
 *   |  +-------+ +-------+ +-------+ +-...  |
 *   +---------------------------------------+
 * </pre>
 * 
 * @author Frank Shaka
 */
public class Gallery {

    private FigureCanvas fc;

    private boolean horizontal;

    private boolean wrap;

    private ContentPane contentPane;

    private GalleryLayout layout = new GalleryLayout();

    /**
     * Style: SWT.HORIZONTAL, SWT.VERTICAL, SWT.WRAP
     * 
     * @param parent
     * @param style
     */
    public Gallery(Composite parent, int style) {
        try {
            fc = new FigureCanvas(parent, checkStyle(style)
                    | SWT.DOUBLE_BUFFERED);
        } catch (Exception e) {
            fc = new FigureCanvas(parent, SWT.DOUBLE_BUFFERED);
        }
        horizontal = ((style & SWT.HORIZONTAL) != 0);
        wrap = ((style & SWT.WRAP) != 0);
        hookControl(fc);
        createContents();
    }

    private void createContents() {
        contentPane = new ContentPane(horizontal, false, wrap);
        fc.setContents(contentPane);
        relayout();
    }

    protected void hookControl(Control c) {
        c.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose(e);
            }
        });
    }

    private static int checkStyle(int style) {
        int mask = SWT.HORIZONTAL | SWT.VERTICAL | SWT.WRAP;
        return style &= ~mask;
    }

    public FigureCanvas getControl() {
        return fc;
    }

    /**
     * @return the view
     */
    public ContentPane getContentPane() {
        return contentPane;
    }

//    public double getScale() {
//        return contentPane.getScale();
//    }
//
//    public void setScale(double scale) {
//        contentPane.setScale(scale);
//    }

    public GalleryLayout getLayout() {
        return layout;
    }

    public void setLayout(GalleryLayout layout) {
        if (layout == null || layout == this.layout)
            return;
        this.layout = layout;
        relayout();
    }

    public boolean isHorizontal() {
        return horizontal;
    }

    public boolean isWrap() {
        return wrap;
    }

    public void clearContents() {
        contentPane.removeAll();
    }

    public int indexOf(IFigure frame) {
        return contentPane.getChildren().indexOf(frame);
    }

    public void addFrame(IFigure frame) {
        addFrame(frame, -1);
    }

    public void addFrame(IFigure frame, int index) {
        contentPane.add(frame, index);
    }

    public void removeFrame(IFigure frame) {
        contentPane.remove(frame);
    }

    public void removeFrame(int index) {
        IFigure slide = getFrame(index);
        if (slide != null)
            removeFrame(slide);
    }

    public IFigure getFrame(int index) {
        if (index < 0)
            return null;
        List slides = contentPane.getChildren();
        if (index >= slides.size())
            return null;
        return (IFigure) slides.get(index);
    }

    public boolean isStretchMinorAxis() {
        return layout.minorAlignment == ALIGN_FILL;
    }

    public void refresh() {
        relayout();
    }

    protected void relayout() {
        contentPane.setMajorAlignment(layout.majorAlignment);
        contentPane.setMinorAlignment(layout.minorAlignment);
        contentPane.setMajorSpacing(layout.majorSpacing);
        contentPane.setMinorSpacing(layout.minorSpacing);
        contentPane.setBorder(new MarginBorder(layout.getMargins()));
        contentPane.revalidate();
        if (horizontal) {
            fc.getViewport().setContentsTracksWidth(wrap);
            fc.getViewport().setContentsTracksHeight(isStretchMinorAxis());
        } else {
            fc.getViewport().setContentsTracksHeight(wrap);
            fc.getViewport().setContentsTracksWidth(isStretchMinorAxis());
        }
    }

    public void centerHorizontal() {
        if (fc.isDisposed())
            return;
        RangeModel horizontal = fc.getViewport().getHorizontalRangeModel();
        int h = (horizontal.getMaximum() - horizontal.getExtent() + horizontal
                .getMinimum()) / 2;
        fc.scrollToX(h);
    }

    protected void handleDispose(DisposeEvent e) {
    }

}