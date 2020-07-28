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

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IDecoratedFigure;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.draw2d.decoration.IShapeDecoration;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.decorations.IDecorationDescriptor;
import org.xmind.ui.decorations.IDecorationManager;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.DecorationLabelProvider;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.MComboViewer;
import org.xmind.ui.viewers.SWTUtils;
import org.xmind.ui.viewers.SliderViewer;

public class BoundaryShapePropertySectionPart extends StyledPropertySectionPart {

    private class FillColorOpenListener implements IOpenListener {
        public void open(OpenEvent event) {
            changeFillColor((IColorSelection) event.getSelection());
        }
    }

    private class ShapeSelectionChangedListener implements
            ISelectionChangedListener {
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

    private class OpacityLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int value = getOpacityValueFromScaleElement(element);
            if (value >= 0) {
                return NLS
                        .bind(PropertyMessages.BoundaryOpacity_pattern, value);
            }
            return super.getText(element);
        }
    }

    private class OpacitySelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            int value = getOpacityValueFromScaleSelection(event.getSelection());
            if (value >= 0) {
                opacityInput.setText(String.valueOf(value));
                previewOpacity(value);
            }
        }

    }

    private class OpacityPostSelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            int value = getOpacityValueFromScaleSelection(event.getSelection());
            if (value >= 0) {
                changeOpacity(value);
                selectAllOpacityText(true);
            }
        }

    }

    private static List<IDecorationDescriptor> BoundaryShapes;

    private MComboViewer shapeViewer;

    private ColorPicker fillColorPicker;

    private Text opacityInput;

    private SliderViewer opacityScale;

    private Image opacityImage;

    protected void createContent(Composite parent) {
        Composite line1 = new Composite(parent, SWT.NONE);
        line1.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout1 = new GridLayout(2, false);
        layout1.marginWidth = 0;
        layout1.marginHeight = 0;
        layout1.horizontalSpacing = 3;
        layout1.verticalSpacing = 3;
        line1.setLayout(layout1);
        createLineContent1(line1);

        Composite line2 = new Composite(parent, SWT.NONE);
        line2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout2 = new GridLayout(4, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.horizontalSpacing = 3;
        layout2.verticalSpacing = 3;
        line2.setLayout(layout2);
        createLineContent2(line2);
    }

    private void createLineContent1(Composite parent) {
        shapeViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        shapeViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        shapeViewer.getControl().setToolTipText(
                PropertyMessages.BoundaryShape_toolTip);
        shapeViewer.setContentProvider(new ArrayContentProvider());
        shapeViewer.setLabelProvider(new DecorationLabelProvider());
        shapeViewer.setInput(getBoundaryShapes());
        shapeViewer
                .addSelectionChangedListener(new ShapeSelectionChangedListener());

        fillColorPicker = new ColorPicker(ColorPicker.AUTO | ColorPicker.CUSTOM
                | ColorPicker.NONE, PaletteContents.getDefault());
        fillColorPicker.getAction().setToolTipText(
                PropertyMessages.BoundaryFillColor_toolTip);
        fillColorPicker.addOpenListener(new FillColorOpenListener());

        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(fillColorPicker);
        ToolBar barControl = colorBar.createControl(parent);
        barControl.setLayoutData(new GridData(GridData.END, GridData.CENTER,
                false, false));
    }

    private void createLineContent2(Composite parent) {
        Label opacityLabel = new Label(parent, SWT.NONE);
        opacityLabel.setImage(getOpacityImage());
        opacityLabel.setLayoutData(new GridData(GridData.BEGINNING,
                GridData.CENTER, false, false));
        opacityLabel.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                releaseImages();
            }
        });

        opacityInput = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.TRAIL);
        SWTUtils.makeNumeralInput(opacityLabel, false, false);
        GridData inputData = new GridData(GridData.FILL, GridData.CENTER,
                false, false);
        inputData.widthHint = 25;
        opacityInput.setLayoutData(inputData);
        opacityInput.setTextLimit(3);
        opacityInput.setToolTipText(PropertyMessages.BoundaryOpacity_toolTip);
        Listener inputEventHandler = new Listener() {
            public void handleEvent(Event event) {
                if (event.type == SWT.DefaultSelection) {
                    int value = getOpacityValueFromTextWidget();
                    if (value >= 0) {
                        changeOpacity(value);
                        selectAllOpacityText(false);
                    }
                } else if (event.type == SWT.KeyDown) {
                    if (event.character == SWT.ESC) {
                        updateOpacityToWidget();
                    }
                } else if (event.type == SWT.FocusIn) {
                    selectAllOpacityText(false);
                }
            }
        };
        opacityInput.addListener(SWT.DefaultSelection, inputEventHandler);
        opacityInput.addListener(SWT.KeyDown, inputEventHandler);
        opacityInput.addListener(SWT.FocusIn, inputEventHandler);

        Label percentageLabel = new Label(parent, SWT.NONE);
        percentageLabel.setText("%"); //$NON-NLS-1$
        percentageLabel.setLayoutData(new GridData(GridData.FILL,
                GridData.CENTER, false, false));

        opacityScale = new SliderViewer(parent, SWT.HORIZONTAL);
        opacityScale.setLabelProvider(new OpacityLabelProvider());
        opacityScale.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, true, false));
        opacityScale.getControl().setBackground(parent.getBackground());
        opacityScale
                .addSelectionChangedListener(new OpacitySelectionChangedListener());
        opacityScale
                .addPostSelectionChangedListener(new OpacityPostSelectionChangedListener());
    }

    private Image getOpacityImage() {
        if (opacityImage == null) {
            ImageDescriptor icon = MindMapUI.getImages().get(
                    IMindMapImages.OPAQUE);
            if (icon != null) {
                opacityImage = icon.createImage(false);
            }
        }
        return opacityImage;
    }

    private void releaseImages() {
        if (opacityImage != null) {
            opacityImage.dispose();
            opacityImage = null;
        }
    }

    private void selectAllOpacityText(final boolean takeFocus) {
        if (opacityInput != null && !opacityInput.isDisposed()) {
            opacityInput.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    if (opacityInput != null && !opacityInput.isDisposed()) {
                        if (takeFocus)
                            opacityInput.setFocus();
                        opacityInput.selectAll();
                    }
                }
            });
        }
    }

    private int getOpacityValueFromTextWidget() {
        if (opacityInput != null && !opacityInput.isDisposed()) {
            try {
                return Math.min(100, Integer.parseInt(opacityInput.getText()));
            } catch (NumberFormatException e) {
            }
        }
        return -1;
    }

    private int getOpacityValueFromScaleSelection(ISelection selection) {
        Object o = ((IStructuredSelection) selection).getFirstElement();
        return getOpacityValueFromScaleElement(o);
    }

    private int getOpacityValueFromScaleElement(Object o) {
        if (o instanceof Double) {
            double value = ((Double) o).doubleValue();
            return (int) Math.round(value * 100);
        }
        return -1;
    }

    public void setFocus() {
        if (shapeViewer != null && !shapeViewer.getControl().isDisposed()) {
            shapeViewer.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        shapeViewer = null;
        fillColorPicker = null;
        opacityInput = null;
        opacityScale = null;
    }

    protected void doRefresh() {
        String shapeId = null;
        if (shapeViewer != null && !shapeViewer.getControl().isDisposed()) {
            shapeId = getStyleValue(Styles.ShapeClass, null);
            IDecorationDescriptor element = getSelectableShape(shapeId);
            if (element == null) {
                shapeViewer.setSelection(StructuredSelection.EMPTY);
            } else {
                shapeViewer.setSelection(new StructuredSelection(element));
            }
        }
        if (fillColorPicker != null) {
            if (shapeId == null)
                shapeId = getStyleValue(Styles.ShapeClass, null);
            updateColorPicker(fillColorPicker, Styles.FillColor, shapeId);
        }
        updateOpacityToWidget();
    }

    private void updateOpacityToWidget() {
        String value = getStyleValue(Styles.Opacity, null);
        double doubleValue = parseOpacityValue(value);
        doubleValue = Math.max(0, Math.min(1, doubleValue));
        if (opacityInput != null && !opacityInput.isDisposed()) {
            opacityInput.setText(String.valueOf((int) Math
                    .round(doubleValue * 100)));
        }
        if (opacityScale != null && !opacityScale.getControl().isDisposed()) {
            opacityScale.setSelection(new StructuredSelection(Double
                    .valueOf(doubleValue)));
        }
    }

    private double parseOpacityValue(String value) {
        if (value != null) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
            }
        }
        return 1;
    }

    private IDecorationDescriptor getSelectableShape(String shapeId) {
        if (shapeId == null)
            return null;
        IDecorationDescriptor descriptor = MindMapUI.getDecorationManager()
                .getDecorationDescriptor(shapeId);
        if (!getBoundaryShapes().contains(descriptor))
            return null;
        return descriptor;
    }

    private void changeShape(String newShape) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyBoundaryShape);
        addStyle(request, Styles.ShapeClass, newShape);
        sendRequest(request);
    }

    protected void changeFillColor(IColorSelection selection) {
        changeColor(selection, Styles.FillColor,
                CommandMessages.Command_ModifyFillColor);
    }

    private void changeOpacity(int opacityValue) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyWallpaperOpacity);
        String value = String.valueOf((double) opacityValue * 1.0 / 100);
        addStyle(request, Styles.Opacity, value);
        sendRequest(request);
    }

    private void previewOpacity(int value) {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer != null) {
            int alpha = -1;
            for (Object o : getSelectedElements()) {
                IPart p = viewer.findPart(o);
                if (p instanceof IGraphicalPart) {
                    IFigure figure = ((IGraphicalPart) p).getFigure();
                    if (figure instanceof IDecoratedFigure) {
                        IDecoration decoration = ((IDecoratedFigure) figure)
                                .getDecoration();
                        if (decoration instanceof IShapeDecoration) {
                            if (alpha < 0) {
                                alpha = value * 255 / 100;
                            }
                            ((IShapeDecoration) decoration).setFillAlpha(
                                    figure, alpha);
                        }
                    }
                }
            }
        }
    }

    private static List<IDecorationDescriptor> getBoundaryShapes() {
        if (BoundaryShapes == null) {
            BoundaryShapes = MindMapUI.getDecorationManager().getDescriptors(
                    IDecorationManager.CATEGORY_BOUNDARY_SHAPE);
        }
        return BoundaryShapes;
    }

}