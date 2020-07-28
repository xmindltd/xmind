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
package org.xmind.ui.internal.layouts;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.xmind.gef.draw2d.DecoratedConnectionFigure;
import org.xmind.ui.mindmap.IConnectionPart;
import org.xmind.ui.mindmap.IRelationshipPart;

public class RelationshipLayout extends AbstractLayout {

    private IRelationshipPart part;

    public RelationshipLayout(IRelationshipPart part) {
        this.part = part;
    }

    public IConnectionPart getPart() {
        return part;
    }

    protected Dimension calculatePreferredSize(IFigure container, int wHint,
            int hHint) {
        IFigure figure = part.getFigure();
        if (figure instanceof DecoratedConnectionFigure) {
            return ((DecoratedConnectionFigure) figure).getPreferredBounds()
                    .getSize();
        }
        return figure.getSize();
    }

    public void layout(IFigure container) {
        // RelationshipFigure lays out itself.
    }

}