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
package org.xmind.ui.internal.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.decorations.IDecorationDescriptor;
import org.xmind.ui.decorations.IDecorationManager;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISummaryPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.DecorationLabelProvider;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.MComboViewer;

public class SummaryLinePropertySectionPart extends LinePropertySectionPartBase {

    private class LineShapeSelectionChangedListener implements
            ISelectionChangedListener {
        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof IDecorationDescriptor) {
                changeLineShape(((IDecorationDescriptor) o).getId());
            }
        }
    }

    private static List<IDecorationDescriptor> LineShapes;

    private MComboViewer lineShapeViewer;

    protected void createContent(Composite parent) {
        createLineShapeBar(parent);
        super.createContent(parent);
    }

    private void createLineShapeBar(Composite parent) {
        Composite line1 = new Composite(parent, SWT.NONE);
        line1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 3;
        line1.setLayout(layout);

        lineShapeViewer = new MComboViewer(line1, MComboViewer.NORMAL);
        lineShapeViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        lineShapeViewer.getControl().setToolTipText(
                PropertyMessages.SummaryShape_toolTip);
        lineShapeViewer.setContentProvider(new ArrayContentProvider());
        lineShapeViewer.setLabelProvider(new DecorationLabelProvider());
        lineShapeViewer.setInput(getLineShapes());
        lineShapeViewer
                .addSelectionChangedListener(new LineShapeSelectionChangedListener());
    }

    protected List<IDecorationDescriptor> getLineShapes() {
        return getBranchConnectionShapes();
    }

    public void setFocus() {
        if (lineShapeViewer != null
                && !lineShapeViewer.getControl().isDisposed()) {
            lineShapeViewer.getControl().setFocus();
        } else {
            super.setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        lineShapeViewer = null;
    }

    protected Request fillTargets(Request request) {
        request = super.fillTargets(request);
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer != null) {
            List<IPart> targets = request.getTargets();
            List<IPart> realTargets = new ArrayList<IPart>(targets.size());
            for (IPart p : targets) {
                if (p instanceof ITopicPart) {
                    ISummaryPart summary = findSummaryPartByTopicPart((ITopicPart) p);
                    if (summary != null) {
                        realTargets.add(summary);
                    }
                }
            }
            request.setTargets(realTargets);
        }
        return request;
    }

    private ISummaryPart findSummaryPartByTopicPart(ITopicPart topic) {
        IBranchPart branch = topic.getOwnerBranch();
        if (branch != null) {
            IBranchPart parent = branch.getParentBranch();
            if (parent != null) {
                for (ISummaryPart summary : parent.getSummaries()) {
                    if (summary.getNode() == topic)
                        return summary;
                }
            }
        }
        return null;
    }

    protected IGraphicalPart getGraphicalPart(Object o, IViewer viewer) {
        IGraphicalPart part = super.getGraphicalPart(o, viewer);
        if (part instanceof ITopicPart) {
            part = findSummaryPartByTopicPart((ITopicPart) part);
        }
        return part;
    }

    protected String getLineShapeId() {
        return getStyleValue(Styles.ShapeClass, null);
    }

    protected void refreshWithShapeId(String lineShapeId) {
        if (lineShapeViewer != null
                && !lineShapeViewer.getControl().isDisposed()) {
            IDecorationDescriptor element = getSelectableLineShape(lineShapeId);
            if (element == null) {
                lineShapeViewer.setSelection(StructuredSelection.EMPTY);
            } else {
                lineShapeViewer.setSelection(new StructuredSelection(element));
            }
        }
        super.refreshWithShapeId(lineShapeId);
    }

    private IDecorationDescriptor getSelectableLineShape(String shapeId) {
        if (shapeId == null)
            return null;
        IDecorationDescriptor descriptor = MindMapUI.getDecorationManager()
                .getDecorationDescriptor(shapeId);
        if (!getLineShapes().contains(descriptor))
            return null;
        return descriptor;
    }

    private void changeLineShape(String lineShapeId) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyLineShape);
        addStyle(request, Styles.ShapeClass, lineShapeId);
        sendRequest(request);
    }

    private static List<IDecorationDescriptor> getBranchConnectionShapes() {
        if (LineShapes == null) {
            LineShapes = MindMapUI.getDecorationManager().getDescriptors(
                    IDecorationManager.CATEGORY_SUMMARY_SHAPE);
        }
        return LineShapes;
    }

}