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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.Request;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.ColorSelection;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.decorations.IDecorationDescriptor;
import org.xmind.ui.decorations.IDecorationManager;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.DecorationLabelProvider;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.MComboViewer;

public class TopicShapePropertySectionPart extends StyledPropertySectionPart {

    private class FillColorOpenListener implements IOpenListener {
        public void open(OpenEvent event) {
            changeFillColor((IColorSelection) event.getSelection());
        }
    }

    private class ShapeSelectionChangedListener
            implements ISelectionChangedListener {
        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof IDecorationDescriptor) {
                changeShape(((IDecorationDescriptor) o).getId());
            }
        }
    }

    private class BorderLineColorOpenListener implements IOpenListener {
        public void open(OpenEvent event) {
            changeBorderLineColor((IColorSelection) event.getSelection());
        }
    }

    private class BorderLineWidthSelectionChangedListener
            implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof LineWidth) {
                changeBorderLineWidth((LineWidth) o);
            }
        }
    }

    private static List<IDecorationDescriptor> TopicShapes;

    private static List<IDecorationDescriptor> CalloutTopicShapes;

    private MComboViewer shapeViewer;

    private ColorPicker fillColorPicker;

    private ColorPicker borderLineColorPicker;

    private MComboViewer borderLineWidthViewer;

    protected void createContent(Composite parent) {
        Composite line1 = new Composite(parent, SWT.NONE);
        line1.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 3;
        line1.setLayout(layout);

        createShapeLineContent(line1);

        Composite line2 = new Composite(parent, SWT.NONE);
        line2.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        GridLayout layout2 = new GridLayout(2, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.horizontalSpacing = 3;
        layout2.verticalSpacing = 3;
        line2.setLayout(layout2);

        createBorderLineContent(line2);
    }

    private void createShapeLineContent(Composite parent) {
        shapeViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        shapeViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        shapeViewer.getControl()
                .setToolTipText(PropertyMessages.TopicShape_toolTip);
        shapeViewer.setContentProvider(new ArrayContentProvider());
        shapeViewer.setLabelProvider(new DecorationLabelProvider());
        shapeViewer.setInput(getShapes());
        shapeViewer.addSelectionChangedListener(
                new ShapeSelectionChangedListener());

        fillColorPicker = new ColorPicker(
                ColorPicker.AUTO | ColorPicker.CUSTOM | ColorPicker.NONE,
                PaletteContents.getDefault());
        fillColorPicker.getAction()
                .setToolTipText(PropertyMessages.TopicFillColor_toolTip);
        fillColorPicker.addOpenListener(new FillColorOpenListener());

        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(fillColorPicker);
        ToolBar barControl = colorBar.createControl(parent);
        barControl.setLayoutData(
                new GridData(GridData.END, GridData.CENTER, false, false));
    }

    private void createBorderLineContent(Composite parent) {
        borderLineWidthViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        borderLineWidthViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        borderLineWidthViewer.getControl()
                .setToolTipText(PropertyMessages.LineWidth_toolTip);
        borderLineWidthViewer.setContentProvider(new ArrayContentProvider());
        borderLineWidthViewer.setLabelProvider(new LineWidthLabelProvider());
        borderLineWidthViewer.setInput(LineWidth.values());
        borderLineWidthViewer.addSelectionChangedListener(
                new BorderLineWidthSelectionChangedListener());

        borderLineColorPicker = new ColorPicker(
                ColorPicker.AUTO | ColorPicker.CUSTOM,
                PaletteContents.getDefault());
        borderLineColorPicker.getAction()
                .setToolTipText(PropertyMessages.LineColor_toolTip);
        borderLineColorPicker
                .addOpenListener(new BorderLineColorOpenListener());
        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(borderLineColorPicker);
        ToolBar barControl = colorBar.createControl(parent);
        barControl.setLayoutData(
                new GridData(GridData.END, GridData.CENTER, false, false));
    }

    public void setFocus() {
        if (shapeViewer != null && !shapeViewer.getControl().isDisposed()) {
            shapeViewer.getControl().setFocus();
        } else if (borderLineWidthViewer != null
                && !borderLineWidthViewer.getControl().isDisposed()) {
            borderLineWidthViewer.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        shapeViewer = null;
        fillColorPicker = null;
        borderLineWidthViewer = null;
        borderLineColorPicker = null;
    }

    protected void doRefresh() {
        List<IDecorationDescriptor> newInput = getShapes();
        Object oldInput = shapeViewer.getInput();
        if (oldInput != newInput) {
            shapeViewer.setInput(newInput);
        }
        String shapeId = null;
        String shapeClassKey = Styles.ShapeClass;
        if (ITopic.CALLOUT.equals(getCurrentTopicType()))
            shapeClassKey = Styles.CalloutShapeClass;

        if (shapeViewer != null && !shapeViewer.getControl().isDisposed()) {
            shapeId = getStyleValue(shapeClassKey, null);
            if (Styles.TOPIC_SHAPE_NO_BORDER.equals(shapeId))
                shapeId = Styles.TOPIC_SHAPE_RECT;
            IDecorationDescriptor element = getSelectableShape(shapeId);
            if (element == null) {
                shapeViewer.setSelection(StructuredSelection.EMPTY);
            } else {
                shapeViewer.setSelection(new StructuredSelection(element));
            }

            ITopicPart topicPart = (ITopicPart) getGraphicalPart(
                    getSelectedElements()[0]);
            if (topicPart != null) {
                IBranchPart part = topicPart.getOwnerBranch();
                String value = part.getBranchPolicy()
                        .getStyleSelector(topicPart).getOverridedValue(part,
                                Styles.ShapeClass,
                                Styles.LAYER_BEFORE_USER_VALUE);
                shapeViewer.getControl().setEnabled(value == null);
            }
        }
        if (fillColorPicker != null) {
            if (shapeId == null)
                shapeId = getStyleValue(shapeClassKey, null);
            updateColorPicker(fillColorPicker, Styles.FillColor, shapeId);
        }
        String lineShapeId = getLineShapeId();
        refreshWithShapeId(lineShapeId);
    }

    protected String getLineShapeId() {
        return getStyleValue(Styles.LineClass, null);
    }

    protected void refreshWithShapeId(String lineShapeId) {
        if (borderLineWidthViewer != null
                && !borderLineWidthViewer.getControl().isDisposed()) {
            String borderLineWidth = getStyleValue(Styles.BorderLineWidth,
                    lineShapeId);
            LineWidth element = LineWidth.findByValue(borderLineWidth);

            if (Styles.TOPIC_SHAPE_NO_BORDER
                    .equals(getStyleValue(Styles.ShapeClass, null)))
                element = LineWidth.None;

            if (element == null)
                element = LineWidth.findByValue(
                        getStyleValue(Styles.LineWidth, lineShapeId));
            if (element == null)
                element = LineWidth.Thinnest;

            borderLineWidthViewer
                    .setSelection(new StructuredSelection(element));
        }
        if (borderLineColorPicker != null) {
            updateColorPicker(borderLineColorPicker, Styles.BorderLineColor,
                    lineShapeId);
        }
    }

    protected void updateColorPicker(ColorPicker picker, String styleKey,
            String decorationId) {
        String autoColor = getAutoValue(styleKey, decorationId);
        if (autoColor == null)
            autoColor = getAutoValue(styleKey, decorationId);
        picker.setAutoColor(StyleUtils.convertRGB(styleKey, autoColor));
        String userColor = getUserValue(styleKey);
        if (userColor == null)
            userColor = getUserValue(styleKey);
        int type;
        if (userColor == null) {
            type = IColorSelection.AUTO;
            userColor = autoColor;
        } else {
            type = IColorSelection.CUSTOM;
        }
        if (type != IColorSelection.AUTO && Styles.NONE.equals(userColor)) {
            type = IColorSelection.NONE;
        }
        RGB color = StyleUtils.convertRGB(Styles.TextColor, userColor);
        picker.setSelection(new ColorSelection(type, color));
    }

    protected void registerEventListener(Object source,
            ICoreEventRegister register) {
        super.registerEventListener(source, register);
        if (source instanceof ITopic) {
            register.register(Core.StructureClass);
        }
    }

    private IDecorationDescriptor getSelectableShape(String shapeId) {
        if (shapeId == null)
            return null;
        IDecorationDescriptor descriptor = MindMapUI.getDecorationManager()
                .getDecorationDescriptor(shapeId);
        if (!getShapes().contains(descriptor))
            return null;
        return descriptor;
    }

    private void changeShape(String newShape) {
//        String autoValue = getAutoValue(Styles.SHAPE_CLASS, null);
//        if (newShape.equals(autoValue))
//            newShape = null;
        Request request = createStyleRequest(
                CommandMessages.Command_ModifyTopicShape);
        boolean isCallout = ITopic.CALLOUT.equals(getCurrentTopicType());
        addStyle(request,
                isCallout ? Styles.CalloutShapeClass : Styles.ShapeClass,
                newShape);
        sendRequest(request);
    }

    protected void changeFillColor(IColorSelection selection) {
        changeColor(selection, Styles.FillColor,
                CommandMessages.Command_ModifyFillColor);
    }

    private String getCurrentTopicType() {
        ISelection selection = getCurrentSelection();
        String wholeType = null;
        if (selection instanceof IStructuredSelection) {
            for (Object obj : ((IStructuredSelection) selection).toList()) {
                if (obj instanceof ITopic) {
                    String type = ((ITopic) obj).getType();
                    if (type == null
                            || (wholeType != null && !wholeType.equals(type)))
                        return null;
                    wholeType = type;

                }
            }
        }
        return wholeType;
    }

    private List<IDecorationDescriptor> getShapes() {
        if (ITopic.CALLOUT.equals(getCurrentTopicType()))
            return getCalloutTopicShapes();
        else
            return getTopicShapes();
    }

    private static List<IDecorationDescriptor> getTopicShapes() {
        if (TopicShapes == null) {
            TopicShapes = MindMapUI.getDecorationManager()
                    .getDescriptors(IDecorationManager.CATEGORY_TOPIC_SHAPE);
        }
        return TopicShapes;
    }

    private static List<IDecorationDescriptor> getCalloutTopicShapes() {
        if (CalloutTopicShapes == null) {
            CalloutTopicShapes = MindMapUI.getDecorationManager()
                    .getDescriptors(IDecorationManager.CATEGORY_CALLOUT_SHAPE);
        }
        return CalloutTopicShapes;
    }

    protected void changeBorderLineColor(IColorSelection selection) {
        changeColor(selection, Styles.BorderLineColor,
                CommandMessages.Command_ModifyBorderColor);
    }

    protected void changeBorderLineWidth(LineWidth lineWidth) {
        Request request = createStyleRequest(
                CommandMessages.Command_ModifyBorderShape);
        String value = lineWidth == null ? null : lineWidth.getValue();
        addStyle(request, Styles.BorderLineWidth, value);
        sendRequest(request);
    }

}