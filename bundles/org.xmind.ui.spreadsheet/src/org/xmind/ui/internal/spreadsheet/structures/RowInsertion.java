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
package org.xmind.ui.internal.spreadsheet.structures;

import org.eclipse.draw2d.geometry.Dimension;
import org.xmind.ui.branch.AbstractInsertion;
import org.xmind.ui.internal.spreadsheet.Spreadsheet;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.MindMapUtils;

public class RowInsertion extends AbstractInsertion {

    public RowInsertion(IBranchPart parent, int index, Dimension size) {
        super(parent, index, size);
    }

    public void pullOut() {
        MindMapUtils.flushCache(getParent(), Spreadsheet.CACHE_ROW_INSERTION);
        getParent().getFigure().revalidate();
        getParent().getFigure().repaint();
    }

    public void pushIn() {
        MindMapUtils.setCache(getParent(), Spreadsheet.CACHE_ROW_INSERTION,
                this);
        getParent().getFigure().revalidate();
        getParent().getFigure().repaint();
    }

}