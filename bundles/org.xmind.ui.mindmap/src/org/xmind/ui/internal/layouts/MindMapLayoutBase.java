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

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.AbstractReferencedLayout;
import org.xmind.gef.draw2d.IReferencedFigure;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.part.IGraphicalPart;

public abstract class MindMapLayoutBase extends AbstractReferencedLayout {

    private IGraphicalPart part;

    public MindMapLayoutBase(IGraphicalPart part) {
        this.part = part;
        IFigure figure = part.getFigure();
        if (figure instanceof IReferencedFigure) {
            ((IReferencedFigure) figure).setReferenceDescriptor(this);
        }
    }

    public IGraphicalPart getPart() {
        return part;
    }

    public void invalidate() {
        super.invalidate();

        // invalidate structure algorithm
        IStructure sa = getStructureAlgorithm();
        if (sa != null) {
            sa.invalidate(part);
        }
    }

    protected IStructure getStructureAlgorithm() {
        return (IStructure) part.getAdapter(IStructure.class);
//        IGraphicalPolicy policy = part.getGraphicalSite().getGraphicalPolicy();
//        if (policy != null)
//            return policy.getStructureAlgorithm(part);
//        return null;
    }

}