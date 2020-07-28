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

import org.eclipse.draw2d.Layer;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.IUseTransparency;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.internal.dialogs.WallpaperDialog;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.MButton;
import org.xmind.ui.viewers.SWTUtils;
import org.xmind.ui.viewers.SliderViewer;

public class WallpaperPropertySectionPart extends StyledPropertySectionPart {

    private class OpacityLabelProvider extends LabelProvider {
        public String getText(Object element) {
            int value = getOpacityValueFromScaleElement(element);
            if (value >= 0) {
                return NLS.bind(PropertyMessages.WallpaperOpacity_pattern,
                        value);
            }
            return super.getText(element);
        }
    }

    private class OpacitySelectionChangedListener
            implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            int value = getOpacityValueFromScaleSelection(event.getSelection());
            if (value >= 0) {
                opacityInput.setText(String.valueOf(value));
                previewOpacity(value);
            }
        }

    }

    private class OpacityPostSelectionChangedListener
            implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            int value = getOpacityValueFromScaleSelection(event.getSelection());
            if (value >= 0) {
                changeOpacity(value);
                selectAllOpacityText(true);
            }
        }

    }

    private class RemoveWallpaperAction extends Action {

        public RemoveWallpaperAction() {
            super(null, AS_PUSH_BUTTON);
            setImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.REMOVE, true));
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.REMOVE, false));
            setToolTipText(PropertyMessages.RemoveWallpaper_toolTip);
        };

        public void run() {
            removeWallpaper();
        }
    }

    private MButton selectWallpaperWidget;

    private WallpaperDialog selectWallpaperDialog;

    private IAction removeWallpaperAction;

    private Composite opacityGroup;

    private Text opacityInput;

    private SliderViewer opacityScale;

    private Image selectWallpaperImage;

    private Image opacityImage;

    protected void createContent(Composite parent) {
        Composite line1 = new Composite(parent, SWT.NONE);
        line1.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        GridLayout layout1 = new GridLayout(2, false);
        layout1.marginWidth = 0;
        layout1.marginHeight = 0;
        layout1.horizontalSpacing = 3;
        line1.setLayout(layout1);
        createLineContent1(line1);

        opacityGroup = new Composite(parent, SWT.NONE);
        opacityGroup.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        GridLayout layout2 = new GridLayout(4, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.horizontalSpacing = 3;
        opacityGroup.setLayout(layout2);
        createLineContent2(opacityGroup);

        parent.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                releaseImages();
            }
        });
    }

    private void createLineContent1(Composite parent) {
        selectWallpaperWidget = new MButton(parent, MButton.NORMAL);
        selectWallpaperWidget.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        selectWallpaperWidget.setImage(getSelectWallpaperImage());
        selectWallpaperWidget.setText(PropertyMessages.SelectWallpaper_text);
        selectWallpaperWidget.getControl()
                .setToolTipText(PropertyMessages.SelectWallpaper_toolTip);
        selectWallpaperWidget.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                openSelectWallpaperDialog();
            }
        });

        removeWallpaperAction = new RemoveWallpaperAction();
        ToolBarManager removeWallpaperBar = new ToolBarManager(SWT.FLAT);
        removeWallpaperBar.add(removeWallpaperAction);
        removeWallpaperBar.createControl(parent);
        removeWallpaperBar.getControl().setLayoutData(
                new GridData(GridData.END, GridData.CENTER, false, false));
    }

    private void createLineContent2(Composite parent) {
        Label opacityLabel = new Label(parent, SWT.NONE);
        opacityLabel.setImage(getOpacityImage());
        opacityLabel.setLayoutData(new GridData(GridData.BEGINNING,
                GridData.CENTER, false, false));

        opacityInput = new Text(parent, SWT.BORDER | SWT.SINGLE | SWT.TRAIL);
        SWTUtils.makeNumeralInput(opacityLabel, false, false);
        GridData inputData = new GridData(GridData.FILL, GridData.CENTER, false,
                false);
        inputData.widthHint = 25;
        opacityInput.setLayoutData(inputData);
        opacityInput.setTextLimit(3);
        opacityInput.setToolTipText(PropertyMessages.WallpaperOpacity_toolTip);
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
        percentageLabel.setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, false, false));

        opacityScale = new SliderViewer(parent, SWT.HORIZONTAL);
        opacityScale.setLabelProvider(new OpacityLabelProvider());
        opacityScale.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, true, false));
        opacityScale.getControl().setBackground(parent.getBackground());
        opacityScale.addSelectionChangedListener(
                new OpacitySelectionChangedListener());
        opacityScale.addPostSelectionChangedListener(
                new OpacityPostSelectionChangedListener());
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

    private Image getSelectWallpaperImage() {
        if (selectWallpaperImage == null || selectWallpaperImage.isDisposed()) {
            ImageDescriptor icon = MindMapUI.getImages()
                    .get(IMindMapImages.INSERT_IMAGE, true);
            if (icon != null) {
                selectWallpaperImage = icon.createImage(false);
            }
        }
        return selectWallpaperImage;
    }

    private Image getOpacityImage() {
        if (opacityImage == null) {
            ImageDescriptor icon = MindMapUI.getImages()
                    .get(IMindMapImages.OPAQUE);
            if (icon != null) {
                opacityImage = icon.createImage(false);
            }
        }
        return opacityImage;
    }

    private void releaseImages() {
        if (selectWallpaperImage != null) {
            selectWallpaperImage.dispose();
            selectWallpaperImage = null;
        }
        if (opacityImage != null) {
            opacityImage.dispose();
            opacityImage = null;
        }
    }

    public void setFocus() {
        if (selectWallpaperWidget != null
                && !selectWallpaperWidget.getControl().isDisposed()) {
            selectWallpaperWidget.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        selectWallpaperDialog = null;
        selectWallpaperWidget = null;
        removeWallpaperAction = null;
        opacityGroup = null;
        opacityInput = null;
        opacityScale = null;
    }

    protected void doRefresh() {
        String value = getStyleValue(Styles.Background, null);
        boolean hasWallpaper = value != null;
        if (removeWallpaperAction != null)
            removeWallpaperAction.setEnabled(hasWallpaper);
        if (opacityGroup != null && !opacityGroup.isDisposed())
            Utils.setAllEnabled(opacityGroup, hasWallpaper);
        updateOpacityToWidget();
    }

    private void updateOpacityToWidget() {
        String value = getStyleValue(Styles.Opacity, null);
        double doubleValue = parseOpacityValue(value);
        doubleValue = Math.max(0, Math.min(1, doubleValue));
        if (opacityInput != null && !opacityInput.isDisposed()) {
            opacityInput.setText(
                    String.valueOf((int) Math.round(doubleValue * 100)));
        }
        if (opacityScale != null && !opacityScale.getControl().isDisposed()) {
            opacityScale.setSelection(
                    new StructuredSelection(Double.valueOf(doubleValue)));
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

    private void openSelectWallpaperDialog() {
        if (selectWallpaperWidget != null
                && selectWallpaperWidget.getControl() != null
                && !selectWallpaperWidget.getControl().isDisposed()) {
            if (selectWallpaperDialog == null) {
                Control handle = selectWallpaperWidget.getControl();
                selectWallpaperDialog = new WallpaperDialog(handle.getShell(),
                        handle);
            }
            selectWallpaperDialog.setMindMapViewer(getActiveViewer());
            selectWallpaperDialog.open();

            Shell shell = selectWallpaperDialog.getShell();
            if (shell != null && !shell.isDisposed()) {
                selectWallpaperWidget.setForceFocus(true);
                shell.addListener(SWT.Dispose, new Listener() {
                    public void handleEvent(Event event) {
                        if (selectWallpaperWidget != null
                                && !selectWallpaperWidget.getControl()
                                        .isDisposed()) {
                            selectWallpaperWidget.setForceFocus(false);
                        }
                    }
                });
            }
        }
    }

    private void removeWallpaper() {
        Request request = createStyleRequest(
                CommandMessages.Command_ModifyWallpaper);
        addStyle(request, Styles.Background, ""); //$NON-NLS-1$
        sendRequest(request);
    }

    private void changeOpacity(int opacityValue) {
        Request request = createStyleRequest(
                CommandMessages.Command_ModifyWallpaperOpacity);
        String value = String.valueOf((double) opacityValue * 1.0 / 100);
        addStyle(request, Styles.Opacity, value);
        sendRequest(request);
    }

    private void previewOpacity(int value) {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer != null) {
            Layer layer = viewer.getLayer(GEF.LAYER_BACKGROUND);
            if (layer != null && layer instanceof IUseTransparency) {
                int alpha = value * 255 / 100;
                ((IUseTransparency) layer).setSubAlpha(alpha);
            }
        }
    }

}
