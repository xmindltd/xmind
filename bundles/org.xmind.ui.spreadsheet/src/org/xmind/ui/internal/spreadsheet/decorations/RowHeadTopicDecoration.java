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
package org.xmind.ui.internal.spreadsheet.decorations;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.internal.spreadsheet.decorations.SpreadsheetBranchDecoration.Block;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.MindMapUtils;

public class RowHeadTopicDecoration extends HeadTopicDecoration {

    private IGraphicalPart part;

    public RowHeadTopicDecoration(String id, IGraphicalPart part) {
        setId(id);
        this.part = part;
    }

    protected void repaint(IFigure figure) {
        super.repaint(figure);

        IBranchPart branch = MindMapUtils.findBranch(part);
        if (branch != null) {
            IFigure parentBranchFigure = getParentBranchFigure(branch);
            Block block = findBlock(branch, parentBranchFigure);
            if (block != null) {
                parentBranchFigure.repaint(block.bounds.getOuterBounds());
            }
        }
    }

    private Block findBlock(IBranchPart branch, IFigure parentBranchFigure) {
        if (parentBranchFigure instanceof IDecoratedFigure) {
            IDecoration parentDecoration = ((IDecoratedFigure) parentBranchFigure)
                    .getDecoration();
            if (parentDecoration instanceof SpreadsheetBranchDecoration) {
                SpreadsheetBranchDecoration chartDecoration = (SpreadsheetBranchDecoration) parentDecoration;
                List<Block> blocks = chartDecoration.getBlocks();
                if (blocks != null) {
                    for (Block block : blocks) {
                        if (block.part == branch) {
                            return block;
                        }
                    }
                }
            }
        }
        return null;
    }

    private IFigure getParentBranchFigure(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            return parent.getFigure();
        }
        return null;
    }

}