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

import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.core.Core;
import org.xmind.core.IImage;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.Request;
import org.xmind.gef.part.IPart;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.properties.MindMapPropertySectionPartBase;
import org.xmind.ui.viewers.MButton;
import org.xmind.ui.viewers.SWTUtils;

public class ImageSizePropertySectionPart extends
        MindMapPropertySectionPartBase {

    protected class InplaceSizeInput {

        private Control hostControl;

        private Spinner inputWidget;

        public InplaceSizeInput(Control hostControl) {
            this.hostControl = hostControl;
        }

        private void createInputWidget(Composite parent) {
            inputWidget = new Spinner(parent, SWT.BORDER);
            inputWidget.setMinimum(2);
            inputWidget.setMaximum(9999);
            inputWidget.setIncrement(5);
            Listener sizeListener = new Listener() {
                public void handleEvent(Event event) {
                    int value = inputWidget.getSelection();
                    if (hostControl == widthInput.getControl()) {
                        modifySize(value, Integer.parseInt(heightInput
                                .getText()));
                    } else {
                        modifySize(Integer.parseInt(widthInput.getText()),
                                value);
                    }
                    close();
                }
            };
            inputWidget.addListener(SWT.DefaultSelection, sizeListener);
            inputWidget.addListener(SWT.FocusOut, sizeListener);
            inputWidget.addListener(SWT.KeyDown, new Listener() {
                public void handleEvent(Event event) {
                    if (SWTUtils.matchKey(0, SWT.ESC, event.stateMask,
                            event.keyCode)) {
                        close();
                    }
                }
            });
            inputWidget.addListener(SWT.Traverse, new Listener() {
                public void handleEvent(Event event) {
                    if (event.detail == SWT.TRAVERSE_ESCAPE) {
                        close();
                    }
                }
            });
            if (hostControl == widthInput.getControl()) {
                inputWidget
                        .setSelection(Integer.parseInt(widthInput.getText()));
            } else {
                inputWidget.setSelection(Integer
                        .parseInt(heightInput.getText()));
            }
        }

        public void open() {
            if (hostControl instanceof Composite) {
                createInputWidget((Composite) hostControl);
                inputWidget.setLocation(0, 0);
                Point size = hostControl.getSize();
                inputWidget.setSize(size);
                inputWidget.setFocus();
            }
        }

        public void close() {
            inputWidget.dispose();
        }

        protected Point getInitialLocation(Point initialSize) {
            return hostControl.toDisplay(0, 0);
        }

        protected Point getInitialSize() {
            return hostControl.getSize();
        }

    }

    private MButton widthInput;

    private MButton heightInput;

    private Hyperlink resetSizeButton;

    protected void createContent(Composite parent) {
        createWidthHeightInput(parent);
        createResetSizeButton(parent);
    }

    protected GridLayout createLayout(Composite parent) {
        GridLayout layout = super.createLayout(parent);
        layout.verticalSpacing = 10;
        return layout;
    }

    private void createWidthHeightInput(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 5;
        composite.setLayout(gridLayout);

        createWidthInput(composite);
        createHeightInput(composite);
    }

    private void createWidthInput(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        label.setText(PropertyMessages.ImageWidth_label);

        widthInput = new MButton(parent, MButton.NO_IMAGE);
        widthInput.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, false));
        widthInput.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                showSizeInput(widthInput.getControl());
            }
        });
    }

    private void createHeightInput(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false));
        label.setText(PropertyMessages.ImageHeight_label);

        heightInput = new MButton(parent, MButton.NO_IMAGE);
        heightInput.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, false));
        heightInput.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                showSizeInput(heightInput.getControl());
            }
        });
    }

    private void showSizeInput(Control hostControl) {
        new InplaceSizeInput(hostControl).open();
    }

    private void createResetSizeButton(Composite parent) {
        resetSizeButton = new Hyperlink(parent, SWT.NONE);
        resetSizeButton.setHref(this);
        resetSizeButton.setText(PropertyMessages.ResetImageSize_text);
        resetSizeButton.setForeground(parent.getDisplay().getSystemColor(
                SWT.COLOR_BLUE));
        resetSizeButton.addHyperlinkListener(new IHyperlinkListener() {

            public void linkActivated(HyperlinkEvent e) {
                resetImageSize();
            }

            public void linkEntered(HyperlinkEvent e) {
                resetSizeButton.setUnderlined(true);
            }

            public void linkExited(HyperlinkEvent e) {
                resetSizeButton.setUnderlined(false);
            }

        });
    }

    protected void doRefresh() {
        IImage image = getImageModel();
        IImagePart part = getPart(image);
        boolean defaultWidth = false;
        boolean defaultHeight = false;
        if (widthInput != null && !widthInput.getControl().isDisposed()) {
            int width = image.getWidth();
            if (width == IImage.UNSPECIFIED) {
                defaultWidth = true;
                if (part != null) {
                    width = part.getFigure().getBounds().width;
                }
            }
            if (width == IImage.UNSPECIFIED) {
                width = 0;
            }
            widthInput.setText(String.valueOf(width));
        }
        if (heightInput != null && !heightInput.getControl().isDisposed()) {
            int height = image.getHeight();
            if (height == IImage.UNSPECIFIED) {
                defaultHeight = true;
                if (part != null) {
                    height = part.getFigure().getBounds().height;
                }
            }
            if (height == IImage.UNSPECIFIED)
                height = 0;
            heightInput.setText(String.valueOf(height));
        }
        resetSizeButton.setEnabled(!defaultWidth || !defaultHeight);
    }

    private IImagePart getPart(IImage model) {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer != null) {
            IPart p = viewer.findPart(model);
            if (p instanceof IImagePart)
                return (IImagePart) p;
        }
        return null;
    }

    private IImage getImageModel() {
        Object[] elements = getSelectedElements();
        if (elements.length > 0 && elements[0] instanceof IImage)
            return (IImage) elements[0];
        return null;
    }

    protected void registerEventListener(Object source,
            ICoreEventRegister register) {
        register.register(Core.ImageWidth);
        register.register(Core.ImageHeight);
    }

    public void setFocus() {
        if (widthInput != null && !widthInput.getControl().isDisposed())
            widthInput.getControl().setFocus();
    }

    private void resetImageSize() {
        getActiveDomain().handleRequest(
                new Request(GEF.REQ_RESIZE).setViewer(getActiveViewer())
                        .setParameter(GEF.PARAM_SIZE, null));
    }

    private void modifySize(int width, int height) {
        getActiveDomain().handleRequest(
                new Request(GEF.REQ_RESIZE).setViewer(getActiveViewer())
                        .setParameter(GEF.PARAM_SIZE,
                                new Dimension(width, height)));
    }

}