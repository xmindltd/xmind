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
import org.xmind.gef.draw2d.ReferencedLayoutData;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.ui.branch.IBranchStructure;
import org.xmind.ui.mindmap.IBranchPart;

public class BranchLayout extends MindMapLayoutBase {

    public BranchLayout(IBranchPart branch) {
        super(branch);
    }

    protected IBranchPart getBranch() {
        return (IBranchPart) super.getPart();
    }

    protected void fillLayoutData(IFigure container, ReferencedLayoutData data) {
        IStructure sa = getStructureAlgorithm();
        if (sa instanceof IBranchStructure) {
            ((IBranchStructure) sa).fillLayoutData(getBranch(), data);
        }
    }

}