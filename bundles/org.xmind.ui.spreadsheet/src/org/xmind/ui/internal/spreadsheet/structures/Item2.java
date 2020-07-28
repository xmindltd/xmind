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

public class Item2 extends BranchStructureData {

    private Chart2 ownedChart;

    private Cell2 ownedCell;

    private RowHead prefRowHead = null;

    public Item2(Chart2 ownedChart, IBranchPart branch) {
        super(branch);
        this.ownedChart = ownedChart;
    }

    public IBranchPart getBranch() {
        return super.getBranch();
    }

    public Chart2 getOwnedChart() {
        return ownedChart;
    }

    public Cell2 getOwnedCell() {
        return ownedCell;
    }

    void setOwnedCell(Cell2 ownedCell) {
        this.ownedCell = ownedCell;
    }

    public RowHead getPrefRowHead() {
        if (prefRowHead == null) {
            prefRowHead = calcPrefColumnHead();
        }
        return prefRowHead;
    }

    private RowHead calcPrefColumnHead() {
        return new RowHead(getBranch().getTopic().getLabels());
    }

    public Item2 getPreviousItem() {
        if (ownedCell != null)
            return ownedCell.getPreviousItem(this);
        return null;
    }

    public Item2 getNextItem() {
        if (ownedCell != null)
            return ownedCell.getNextItem(this);
        return null;
    }

}