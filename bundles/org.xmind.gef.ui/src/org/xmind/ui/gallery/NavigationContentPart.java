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

package org.xmind.ui.gallery;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Layer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.xmind.gef.part.GraphicalEditPart;

/**
 * @author Frank Shaka
 * 
 */
public class NavigationContentPart extends GraphicalEditPart {

    /**
     * 
     */
    public NavigationContentPart(Object model) {
        setModel(model);
        getFigure();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.part.GraphicalEditPart#createFigure()
     */
    @Override
    protected IFigure createFigure() {
        IFigure figure = new Layer();
        figure.setOpaque(true);
        figure.setBackgroundColor(ColorConstants.black);
        figure.setLayoutManager(new NavigationItemLayout());
        return figure;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.part.Part#getModelChildren(java.lang.Object)
     */
    @Override
    protected Object[] getModelChildren(Object model) {
        IStructuredContentProvider contentProvider = (IStructuredContentProvider) getSite()
                .getViewer().getAdapter(IStructuredContentProvider.class);
        if (contentProvider != null) {
            return contentProvider.getElements(model);
        }
        return super.getModelChildren(model);
    }

    protected void onItemSelection(NavigationItemPart item) {
        getFigure().setConstraint(item.getFigure(), item.getFigure());
    }

    public void addScrollOffset(int offset) {
        ((NavigationItemLayout) getFigure().getLayoutManager()).addOffset(
                getFigure(), offset);
        getFigure().revalidate();
    }

    public void resetScrollOffset() {
        ((NavigationItemLayout) getFigure().getLayoutManager())
                .resetOffset(getFigure());
        getFigure().revalidate();
    }

}
