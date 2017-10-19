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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.geometry.Point;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.decorators.RelTitleTextDecorator;
import org.xmind.ui.mindmap.IRelationshipPart;

public class RelTitleTextPart extends TitleTextPart {

    public RelTitleTextPart() {
        setDecorator(RelTitleTextDecorator.getInstance());
    }

    public void setParent(IPart parent) {
        IRelationshipPart boundaryPart = getRelationshipPart();
        if (boundaryPart instanceof RelationshipPart
                && ((RelationshipPart) boundaryPart).getTitle() == this) {
            ((RelationshipPart) boundaryPart).setTitle(null);
        }
        super.setParent(parent);
        boundaryPart = getRelationshipPart();
        if (boundaryPart instanceof RelationshipPart) {
            ((RelationshipPart) boundaryPart).setTitle(this);
        }
    }

    public IRelationshipPart getRelationshipPart() {
        if (getParent() instanceof IRelationshipPart)
            return (IRelationshipPart) getParent();
        return null;
    }

    public IPart findAt(Point position) {
        IPart ret = super.findAt(position);
        if (ret == this) {
            IRelationshipPart boundaryPart = getRelationshipPart();
            if (boundaryPart != null)
                return boundaryPart;
        }
        return ret;
    }

}