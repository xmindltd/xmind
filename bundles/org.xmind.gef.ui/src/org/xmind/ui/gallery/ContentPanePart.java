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

import java.util.Collection;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.xmind.gef.part.GraphicalEditPart;

public class ContentPanePart extends GraphicalEditPart {

    public ContentPanePart(Object input) {
        setModel(input);
        setDecorator(ContentPaneDecorator.DEFAULT);
    }

    protected IFigure createFigure() {
        return new ContentPane();
    }

    protected Object[] getModelChildren(Object model) {
        if (model instanceof Object[])
            return (Object[]) model;
        if (model instanceof Collection)
            return ((Collection) model).toArray();
        IStructuredContentProvider contentProvider = (IStructuredContentProvider) getSite()
                .getViewer().getAdapter(IStructuredContentProvider.class);
        if (contentProvider != null) {
            return contentProvider.getElements(model);
        }
        return new Object[] { model };
    }

}