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

import org.xmind.ui.branch.BranchStructureData;
import org.xmind.ui.mindmap.IBranchPart;

public class Item extends BranchStructureData {

    private Chart ownedChart;

    private Cell ownedCell;

    private ColumnHead prefColumnHead = null;

    public Item(Chart ownedChart, IBranchPart branch) {
        super(branch);
        this.ownedChart = ownedChart;
    }

    public IBranchPart getBranch() {
        return super.getBranch();
    }

    public Chart getOwnedChart() {
        return ownedChart;
    }

    public Cell getOwnedCell() {
        return ownedCell;
    }

    void setOwnedCell(Cell ownedCell) {
        this.ownedCell = ownedCell;
    }

    public ColumnHead getPrefColumnHead() {
        if (prefColumnHead == null) {
            prefColumnHead = calcPrefColumnHead();
        }
        return prefColumnHead;
    }

    private ColumnHead calcPrefColumnHead() {
        return new ColumnHead(getBranch().getTopic().getLabels());
    }

    public Item getPreviousItem() {
        if (ownedCell != null)
            return ownedCell.getPreviousItem(this);
        return null;
    }

    public Item getNextItem() {
        if (ownedCell != null)
            return ownedCell.getNextItem(this);
        return null;
    }

}