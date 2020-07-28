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
package org.xmind.ui.internal.mindmap;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.graphics.Point;
import org.xmind.gef.IViewer;
import org.xmind.gef.acc.AbstractGraphicalAccessible;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISheetPart;

public class SheetAccessible extends AbstractGraphicalAccessible {

    public SheetAccessible(ISheetPart host) {
        super(host);
    }

    public ISheetPart getHost() {
        return (ISheetPart) super.getHost();
    }

    public String getName() {
        return getHost().getSheet().getTitleText();
    }

    @Override
    public Rectangle getLocation() {
        IViewer viewer = getHost().getSite().getViewer();
        Rectangle r = new Rectangle(viewer.getControl().getBounds());
        Point p = viewer.getControl().toDisplay(r.x, r.y);
        r.x = p.x;
        r.y = p.y;
        return r;
    }

    @Override
    protected List<? extends IPart> getChildrenParts() {
        IBranchPart central = getHost().getCentralBranch();
        List<IBranchPart> floatingBranches = getHost().getFloatingBranches();
        ArrayList<IPart> topics = new ArrayList<IPart>(floatingBranches.size()
                + (central == null ? 0 : 1));
        if (central != null) {
            topics.add(central.getTopicPart());
        }
        for (IBranchPart branch : floatingBranches) {
            topics.add(branch.getTopicPart());
        }
        return topics;
    }

    @Override
    public int getRole() {
        return ACC.ROLE_TREE;
    }

}