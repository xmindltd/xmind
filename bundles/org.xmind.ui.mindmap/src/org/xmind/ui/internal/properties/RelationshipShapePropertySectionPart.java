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

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.xmind.gef.Request;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.decorations.IDecorationDescriptor;
import org.xmind.ui.decorations.IDecorationManager;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.DecorationLabelProvider;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.MComboViewer;

public class RelationshipShapePropertySectionPart extends
        StyledPropertySectionPart implements ISelectionChangedListener {

    private static List<IDecorationDescriptor> RelationshipShapes;

    private static List<IDecorationDescriptor> ArrowShapes;

    private MComboViewer shapeViewer;

    private MComboViewer beginArrowShapeViewer;

    private MComboViewer endArrowShapeViewer;

    protected void createContent(Composite parent) {
        Composite line1 = new Composite(parent, SWT.NONE);
        line1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 3;
        line1.setLayout(layout);

        createLine1Content(line1);

        Composite line2 = new Composite(parent, SWT.NONE);
        line2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout2 = new GridLayout(2, true);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.horizontalSpacing = 3;
        layout2.verticalSpacing = 3;
        line2.setLayout(layout2);

        createLine2Content(line2);
    }

    private void createLine1Content(Composite parent) {
        shapeViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        shapeViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        shapeViewer.getControl().setToolTipText(
                PropertyMessages.RelationshipShape_toolTip);
        shapeViewer.setContentProvider(new ArrayContentProvider());
        shapeViewer.setLabelProvider(new DecorationLabelProvider());
        shapeViewer.setInput(getRelationshipShapes());
        shapeViewer.addSelectionChangedListener(this);
    }

    private void createLine2Content(Composite parent) {
        beginArrowShapeViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        beginArrowShapeViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        beginArrowShapeViewer.getControl().setToolTipText(
                PropertyMessages.BeginArrowShape_toolTip);
        beginArrowShapeViewer.setContentProvider(new ArrayContentProvider());
        beginArrowShapeViewer.setLabelProvider(new DecorationLabelProvider());
        beginArrowShapeViewer.setInput(getArrowShapes());
        beginArrowShapeViewer.addSelectionChangedListener(this);

        endArrowShapeViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        endArrowShapeViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        endArrowShapeViewer.getControl().setToolTipText(
                PropertyMessages.EndArrowShape_toolTip);
        endArrowShapeViewer.setContentProvider(new ArrayContentProvider());
        endArrowShapeViewer.setLabelProvider(new DecorationLabelProvider());
        endArrowShapeViewer.setInput(getArrowShapes());
        endArrowShapeViewer.addSelectionChangedListener(this);
    }

    public void setFocus() {
        if (shapeViewer != null && !shapeViewer.getControl().isDisposed()) {
            shapeViewer.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        shapeViewer = null;
        beginArrowShapeViewer = null;
        endArrowShapeViewer = null;
    }

    protected void doRefresh() {
        if (shapeViewer != null && !shapeViewer.getControl().isDisposed()) {
            String shapeId = getStyleValue(Styles.ShapeClass, null);
            IDecorationDescriptor element = getSelectableDescriptor(shapeId,
                    getRelationshipShapes());
            if (element == null) {
                shapeViewer.setSelection(StructuredSelection.EMPTY);
            } else {
                shapeViewer.setSelection(new StructuredSelection(element));
            }
        }
        if (beginArrowShapeViewer != null
                && !beginArrowShapeViewer.getControl().isDisposed()) {
            String shapeId = getStyleValue(Styles.ArrowBeginClass, null);
            IDecorationDescriptor element = getSelectableDescriptor(shapeId,
                    getArrowShapes());
            if (element == null) {
                beginArrowShapeViewer.setSelection(StructuredSelection.EMPTY);
            } else {
                beginArrowShapeViewer.setSelection(new StructuredSelection(
                        element));
            }
        }
        if (endArrowShapeViewer != null
                && !endArrowShapeViewer.getControl().isDisposed()) {
            String shapeId = getStyleValue(Styles.ArrowEndClass, null);
            IDecorationDescriptor element = getSelectableDescriptor(shapeId,
                    getArrowShapes());
            if (element == null) {
                endArrowShapeViewer.setSelection(StructuredSelection.EMPTY);
            } else {
                endArrowShapeViewer.setSelection(new StructuredSelection(
                        element));
            }
        }
    }

    private IDecorationDescriptor getSelectableDescriptor(String shapeId,
            List<IDecorationDescriptor> descriptors) {
        if (shapeId == null)
            return null;
        IDecorationDescriptor descriptor = MindMapUI.getDecorationManager()
                .getDecorationDescriptor(shapeId);
        if (!descriptors.contains(descriptor))
            return null;
        return descriptor;
    }

    public void selectionChanged(SelectionChangedEvent event) {
        if (isRefreshing())
            return;

        Object o = ((IStructuredSelection) event.getSelection())
                .getFirstElement();
        if (o instanceof IDecorationDescriptor) {
            String decorationId = ((IDecorationDescriptor) o).getId();
            ISelectionProvider provider = event.getSelectionProvider();
            if (provider == shapeViewer) {
                changeRelationshipShape(decorationId);
            } else if (provider == beginArrowShapeViewer) {
                changeBeginArrowShape(decorationId);
            } else if (provider == endArrowShapeViewer) {
                changeEndArrowShape(decorationId);
            }
        }
    }

    private void changeRelationshipShape(String newShape) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyRelationshipShape);
        addStyle(request, Styles.ShapeClass, newShape);
        sendRequest(request);
    }

    private void changeBeginArrowShape(String newShape) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyBeginArrowShape);
        addStyle(request, Styles.ArrowBeginClass, newShape);
        sendRequest(request);
    }

    private void changeEndArrowShape(String newShape) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyEndArrowShape);
        addStyle(request, Styles.ArrowEndClass, newShape);
        sendRequest(request);
    }

    private static List<IDecorationDescriptor> getRelationshipShapes() {
        if (RelationshipShapes == null) {
            RelationshipShapes = MindMapUI.getDecorationManager()
                    .getDescriptors(IDecorationManager.CATEGORY_REL_SHAPE);
        }
        return RelationshipShapes;
    }

    private static List<IDecorationDescriptor> getArrowShapes() {
        if (ArrowShapes == null) {
            ArrowShapes = MindMapUI.getDecorationManager().getDescriptors(
                    IDecorationManager.CATEGORY_ARROW_SHAPE);
        }
        return ArrowShapes;
    }

}