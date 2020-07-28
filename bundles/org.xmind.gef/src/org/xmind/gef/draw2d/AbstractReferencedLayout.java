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
package org.xmind.gef.draw2d;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Insets;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.xmind.gef.draw2d.geometry.Geometry;

public abstract class AbstractReferencedLayout extends AbstractLayout implements
        IReferencedLayout, IReferenceDescriptor {

    private ReferencedLayoutData layoutData = null;

    private boolean calculating = false;

    private Insets referenceDescription = null;

    private Rectangle prefClientArea = null;

    protected Dimension calculatePreferredSize(IFigure container, int wHint,
            int hHint) {
        Insets ins = container.getInsets();
        return getPreferredClientArea(container).getSize().expand(
                ins.getWidth(), ins.getHeight());
    }

    public Rectangle getPreferredClientArea(IFigure container) {
        ReferencedLayoutData data = getLayoutData(container);
        Rectangle clientArea = data.getClientArea();
        if (clientArea != null)
            return clientArea;
        if (prefClientArea == null) {
            prefClientArea = data.createInitBounds();
        }
        return prefClientArea;
    }

    public Insets getReferenceDescription(IFigure figure) {
        if (referenceDescription == null) {
            referenceDescription = calculateReferenceDescription(figure);
        }
        return referenceDescription;
    }

    protected Insets calculateReferenceDescription(IFigure figure) {
        Point reference = getLayoutData(figure).getReference();
        Insets description = Geometry.getInsets(reference,
                getPreferredClientArea(figure));
        return description.add(figure.getInsets());
    }

    public void layout(IFigure container) {
        Point offset;
        if (container instanceof IReferencedFigure) {
            offset = ((IReferencedFigure) container).getReference();
        } else {
            offset = container.getBounds().getLocation();
        }
        ReferencedLayoutData layoutData = getLayoutData(container);
        for (Object child : container.getChildren()) {
            Rectangle bounds = layoutData.get(child);
            if (bounds != null) {
                bounds = bounds.getTranslated(offset);
                layoutChild((IFigure) child, bounds, offset);
            }
        }
    }

    protected void layoutChild(IFigure child, Rectangle bounds, Point offset) {
        child.setBounds(bounds);
    }

    public ReferencedLayoutData getLayoutData(IFigure container) {
        if (layoutData == null) {
            layoutData = createLayoutData(container);
            calculating = true;
            fillLayoutData(container, layoutData);
            calculating = false;
        }
        return layoutData;
    }

    protected ReferencedLayoutData createLayoutData(IFigure container) {
        return new ReferencedLayoutData();
    }

    protected abstract void fillLayoutData(IFigure container,
            ReferencedLayoutData data);

    public void invalidate() {
        super.invalidate();
        if (!calculating)
            layoutData = null;
        referenceDescription = null;
        prefClientArea = null;
    }

}