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

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.Viewport;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.Decorator;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.ui.internal.SpaceCollaborativeEngine;
import org.xmind.gef.util.Properties;

public class ContentPaneDecorator extends Decorator {

    public static final ContentPaneDecorator DEFAULT = new ContentPaneDecorator();

    private static final GalleryLayout DEFAULT_LAYOUT = new GalleryLayout();

    public void decorate(IGraphicalPart part, IFigure figure) {
        super.decorate(part, figure);
        ContentPane contentPane = (ContentPane) part.getFigure();
        IViewer viewer = part.getSite().getViewer();
        Properties properties = viewer.getProperties();
        boolean horizontal = properties.getBoolean(GalleryViewer.Horizontal,
                false);
        boolean wrap = properties.getBoolean(GalleryViewer.Wrap, false);
        GalleryLayout layout = (GalleryLayout) properties
                .get(GalleryViewer.Layout);
        if (layout == null)
            layout = DEFAULT_LAYOUT;
        contentPane.setMajorAlignment(layout.majorAlignment);
        contentPane.setMinorAlignment(layout.minorAlignment);
        contentPane.setMajorSpacing(layout.majorSpacing);
        contentPane.setMinorSpacing(layout.minorSpacing);
        contentPane.setHorizontal(horizontal);
        contentPane.setWrap(wrap);
        contentPane.setBorder(new MarginBorder(layout.getMargins()));

        SpaceCollaborativeEngine sce = (SpaceCollaborativeEngine) properties
                .get(GalleryViewer.ContentPaneSpaceCollaborativeEngine);
        if (sce != null) {
            sce.register(viewer, contentPane);
            contentPane.setSpaceCollaborativeEngine(sce);
        }

        Viewport viewport = findViewport(contentPane);
        if (viewport != null) {
            boolean fill = layout.minorAlignment == GalleryLayout.ALIGN_FILL;
            if (horizontal) {
                viewport.setContentsTracksWidth(wrap);
                viewport.setContentsTracksHeight(fill);
            } else {
                viewport.setContentsTracksHeight(wrap);
                viewport.setContentsTracksWidth(fill);
            }
        }
    }

    protected Viewport findViewport(IFigure figure) {
        if (figure == null)
            return null;
        if (figure instanceof Viewport)
            return (Viewport) figure;
        return findViewport(figure.getParent());
    }

}
