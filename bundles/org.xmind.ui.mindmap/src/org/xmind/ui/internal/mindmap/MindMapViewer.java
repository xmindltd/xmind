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

import java.util.List;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.ISelectionSupport;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.service.IRevealService;
import org.xmind.ui.internal.figures.BranchFigure;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IMindMap;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class MindMapViewer extends GraphicalViewer implements IMindMapViewer {

    protected class MindMapSelectionSupport extends GraphicalSelectionSupport {

        public IPart findSelectablePart(Object element) {
            if (element instanceof ISheet)
                return null;
            IPart p = super.findSelectablePart(element);
            if (p instanceof ITopicPart) {

                IBranchPart branch = MindMapUtils.findBranch(p);
                if (branch != null) {
                    BranchFigure branchFigure = (BranchFigure) branch
                            .getFigure();
                    if (branchFigure.isMinimized())
                        p = null;
                }
            }
            return p;
        }

        protected Object getModel(IPart p) {
            return MindMapUtils.getRealModel(p);
        }

        public ISelection getModelSelection() {
            ISelection selection = super.getModelSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection) selection;
                if (ss.isEmpty() && getInput() != null) {
                    selection = new StructuredSelection(
                            MindMapUtils.toRealModel(getInput()));
                }
            }
            return selection;
        }

    }

    private boolean inputChangedOnSelectionChanged = false;

    public MindMapViewer() {
        setDndSupport(MindMapUI.getMindMapDndSupport());
        setPartFactory(MindMapUI.getMindMapPartFactory());
        setRootPart(new MindMapRootPart());
        getProperties().set(VIEWER_RENDER_TEXT_AS_PATH, false);
    }

    public Object getAdapter(Class adapter) {
        if (adapter == IMindMap.class)
            return getMindMap();
        if (adapter == ISheet.class)
            return getSheet();
        if (adapter == ITopic.class)
            return getCentralTopic();
        if (adapter == ISheetPart.class)
            return getSheetPart();
        if (adapter == IBranchPart.class)
            return getCentralBranchPart();
        if (adapter == ITopicPart.class)
            return getCentralTopicPart();
        if (adapter == IWorkbook.class) {
            ISheet sheet = getSheet();
            return sheet == null ? null : sheet.getOwnedWorkbook();
        }
        return super.getAdapter(adapter);
    }

    protected Control internalCreateControl(Composite parent, int style) {
        FigureCanvas fc = (FigureCanvas) super.internalCreateControl(parent,
                style);
        fc.setScrollBarVisibility(FigureCanvas.ALWAYS);
        fc.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        return fc;
    }

    protected ISelectionSupport createSelectionSupport() {
        return new MindMapSelectionSupport();
    }

    public Object getPreselected() {
        Object o = super.getPreselected();
        if (o instanceof IPart) {
            o = ((IPart) o).getModel();
        }
        return MindMapUtils.toRealModel(o);
    }

    public Object getFocused() {
        Object o = super.getFocused();
        if (o instanceof IPart) {
            o = ((IPart) o).getModel();
        }
        return MindMapUtils.toRealModel(o);
    }

    protected void revealParts(List<? extends IPart> parts) {
        super.revealParts(parts);
        if (getFocusedPart() != null && parts.contains(getFocusedPart())) {
            IRevealService revealService = (IRevealService) getService(IRevealService.class);
            if (revealService != null) {
                revealService.reveal(new StructuredSelection(getFocusedPart()));
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.gef.AbstractViewer#fireFocusedPartChanged()
     */
    @Override
    protected void fireFocusedPartChanged() {
        super.fireFocusedPartChanged();
        if (getFocusedPart() != null) {
            IRevealService revealService = (IRevealService) getService(IRevealService.class);
            if (revealService != null) {
                revealService.reveal(new StructuredSelection(getFocusedPart()));
            }
        }
    }

    public void setSelection(ISelection selection) {
        super.setSelection(selection, true);
    }

    protected void ensureVisible(Rectangle box, Rectangle clientArea, int margin) {
        super.ensureVisible(box, clientArea, 10);
    }

//    protected void inputChanged(Object input, Object oldInput) {
//        ISelection oldSelection = getSelection();
//        super.inputChanged(input, oldInput);
//        if (!inputChangedOnSelectionChanged && getEditDomain() != null
//                && needSelectCentral(oldSelection)) {
//            getEditDomain().handleRequest(MindMapUI.REQ_SELECT_CENTRAL, this);
//        }
//    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.GraphicalViewer#setSelectionOnInputChanged(org.eclipse.
     * jface.viewers.ISelection)
     */
    @Override
    protected void setSelectionOnInputChanged(ISelection selection) {
        if (inputChangedOnSelectionChanged)
            return;
        setSelection(selection, false);
    }

//    private boolean needSelectCentral(ISelection oldSelection) {
//        if (oldSelection.isEmpty())
//            return true;
//        if (oldSelection instanceof IStructuredSelection) {
//            IStructuredSelection ss = (IStructuredSelection) oldSelection;
//            if (ss.size() == 1 && ss.getFirstElement() instanceof ISheet)
//                return true;
//        }
//        return false;
//    }

    public IMindMap getMindMap() {
        Object input = getInput();
        return input instanceof IMindMap ? (IMindMap) input : null;
    }

    public void setMindMap(IMindMap mindMap) {
        setInput(mindMap);
    }

    public IBranchPart getCentralBranchPart() {
        ISheetPart sheetPart = getSheetPart();
        return sheetPart == null ? null : sheetPart.getCentralBranch();
    }

    public ITopic getCentralTopic() {
        Object input = getInput();

        if (input instanceof IMindMap) {
            return ((IMindMap) input).getCentralTopic();
        } else if (input instanceof ISheet) {
            return ((ISheet) input).getRootTopic();
        } else if (input instanceof ITopic) {
            return (ITopic) input;
        }
        return null;
    }

    public ITopicPart getCentralTopicPart() {
        IBranchPart centralBranchPart = getCentralBranchPart();
        return centralBranchPart == null ? null : centralBranchPart
                .getTopicPart();
    }

    public ISheet getSheet() {
        Object input = getInput();
        if (input instanceof IMindMap) {
            return ((IMindMap) input).getSheet();
        } else if (input instanceof ISheet) {
            return (ISheet) input;
        } else if (input instanceof ITopic) {
            return ((ITopic) input).getOwnedSheet();
        }
        return null;
    }

    public ISheetPart getSheetPart() {
        IRootPart rootPart = getRootPart();
        if (rootPart != null) {
            IPart contents = rootPart.getContents();
            if (contents instanceof ISheetPart)
                return (ISheetPart) contents;
        }
        return null;
    }

    public boolean isPrimaryCentralTopic() {
        ISheet sheet = getSheet();
        ITopic centralTopic = getCentralTopic();
        return centralTopic != null && sheet != null
                && centralTopic.equals(sheet.getRootTopic());
    }

    public IPart findPart(Object element) {
        if (element instanceof ISummary)
            element = ((ISummary) element).getTopic();
        return super.findPart(element);
    }

}