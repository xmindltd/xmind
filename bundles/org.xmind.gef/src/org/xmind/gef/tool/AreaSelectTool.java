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
package org.xmind.gef.tool;

import static org.xmind.gef.GEF.ST_CONTROL_PRESSED;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.StructuredSelection;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;

/**
 * @author Brian Sun
 * @version 2005
 */
public abstract class AreaSelectTool extends AreaCreateTool {

    private List<IPart> lastSelected = null;
    private List<IPart> newSelected = null;

    protected void start() {
        super.start();
        if (getStatus().isStatus(ST_CONTROL_PRESSED)) {
            lastSelected = new ArrayList<IPart>(
                    getSelectedParts(getTargetViewer()));
        } else {
            lastSelected = new ArrayList<IPart>();
        }
        newSelected = new ArrayList<IPart>();
        getTargetViewer().setFocused(null);
    }

    protected void end() {
        lastSelected = null;
        newSelected = null;
    }

    protected void updateArea(Rectangle area, Point currentPos) {
        super.updateArea(area, currentPos);
        IPart rootPart = getTargetViewer().getRootPart();
        areaSelect(rootPart, area);
        List<IPart> selection = getMergedSelection();
        updateSelectionStatus(rootPart, selection);
        IPart toFocus = selection.isEmpty() ? null : selection.get(selection
                .size() - 1);
        getTargetViewer().setFocused(toFocus);
    }

    protected void updateSelectionStatus(IPart part, List<IPart> selection) {
        if (selection.contains(part)) {
            part.getStatus().select();
        } else {
            part.getStatus().deSelect();
        }
        for (IPart child : part.getChildren()) {
            updateSelectionStatus(child, selection);
        }
    }

    protected void areaSelect(IPart part, Rectangle area) {
        select(part, area);
        for (IPart child : part.getChildren()) {
            areaSelect(child, area);
        }
    }

    protected void select(IPart part, Rectangle area) {
        if (isSelectable(part, area)) {
            if (!newSelected.contains(part)) {
                newSelected.add(part);
            }
        } else {
            newSelected.remove(part);
        }
    }

    protected boolean isSelectable(IPart part, Rectangle area) {
        if (getTargetViewer().getSelectionSupport().isSelectable(part)) {
            if (part instanceof IGraphicalPart) {
                IGraphicalPart gp = (IGraphicalPart) part;
                return area.intersects(gp.getFigure().getBounds());
            }
        }
        return false;
    }

    protected List<IPart> getMergedSelection() {
        Set<IPart> inter = new HashSet<IPart>(newSelected);
        inter.retainAll(lastSelected);
        List<IPart> selection = new ArrayList<IPart>(lastSelected);
        selection.addAll(newSelected);
        selection.removeAll(inter);
        return selection;
    }

    public void finish() {
        applySelection();
        super.finish();
    }

    protected void applySelection() {
        getTargetViewer().setSelection(
                new StructuredSelection(getMergedSelection()), true);
    }

    public void cancel() {
        revertSelection();
        super.cancel();
    }

    protected void revertSelection() {
        getTargetViewer().getSelectionSupport().refresh();
    }

}