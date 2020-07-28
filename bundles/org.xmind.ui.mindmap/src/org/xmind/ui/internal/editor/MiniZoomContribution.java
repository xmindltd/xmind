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
package org.xmind.ui.internal.editor;

import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.xmind.gef.EditDomain;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IZoomListener;
import org.xmind.gef.Request;
import org.xmind.gef.ZoomManager;
import org.xmind.gef.ZoomObject;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.ActionConstants;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.viewers.ISliderContentProvider;
import org.xmind.ui.viewers.SliderViewer;

public class MiniZoomContribution extends ContributionItem
        implements IPageChangedListener, IZoomListener {

    protected static final int SLIDER_WIDTH = 88;

    private static class ZoomPopup extends PopupDialog {

        private static final double[] directSelectorInput = { 2.0, 1.5, 1.2,
                1.0, 0.8, 0.5 };

        private static class ZoomSliderContentProvider
                implements ISliderContentProvider {

            private static final int intervalNum = directSelectorInput.length
                    - 1;

            public double getRatio(Object input, Object value) {
                if (value instanceof Double) {
                    double doubleValue = ((Double) value).doubleValue();
                    return transformValueToRatio(doubleValue);
                }
                return 0;
            }

            private double transformValueToRatio(double value) {
                if (value > directSelectorInput[0])
                    return 1.0;

                if (value < directSelectorInput[intervalNum])
                    return 0;

                for (int index = intervalNum; index > 0; index--) {
                    if (isInternal(value, directSelectorInput[index],
                            directSelectorInput[index - 1])) {
                        double minRatio = (intervalNum - index) * 1.0
                                / intervalNum;
                        double maxRatio = minRatio + 1.0 / intervalNum;
                        return calRatio(value, directSelectorInput[index],
                                directSelectorInput[index - 1], minRatio,
                                maxRatio);
                    }
                }
                return 0;
            }

            private boolean isInternal(double value, double minValue,
                    double maxValue) {
                return value <= maxValue && value >= minValue;
            }

            private double calRatio(double value, double minValue,
                    double maxValue, double minRatio, double maxRatio) {
                return minRatio + (value - minValue) / (maxValue - minValue)
                        * (maxRatio - minRatio);
            }

            private double calValue(double ratio, double minRatio,
                    double maxRatio, double minValue, double maxValue) {
                return minValue + (ratio - minRatio) / (maxRatio - minRatio)
                        * (maxValue - minValue);
            }

            public Object getValue(Object input, double ratio) {
                return transformRatioToValue(ratio);
            }

            private double transformRatioToValue(double ratio) {
                if (ratio > 1)
                    return directSelectorInput[0] * ratio;

                if (ratio < 0)
                    return 0;

                for (int index = intervalNum; index > 0; index--) {
                    double minRatio = (intervalNum - index) * 1.0 / intervalNum;
                    double maxRatio = minRatio + 1.0 / intervalNum;
                    if (isInternal(ratio, minRatio, maxRatio)) {
                        return calValue(ratio, minRatio, maxRatio,
                                directSelectorInput[index],
                                directSelectorInput[index - 1]);
                    }
                }
                return 0.5;
            }

            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

            public void dispose() {
            }

        }

        private static class ZoomSliderLabelProvider extends LabelProvider {

            public String getText(Object element) {
                if (element instanceof Double) {
                    double scale = ((Double) element).doubleValue();
                    return String.format("%.0f%%", Double.valueOf(scale * 100)); //$NON-NLS-1$
                }
                return super.getText(element);
            }

        }

        private Text valueInput;

        private SliderViewer slider;

        private IGraphicalEditorPage page;

        private ZoomManager zoomManager;

        private Point triggerPosition = null;

        private boolean internalModifying = false;

        private Listener directSelectorItemListener = new Listener() {
            public void handleEvent(Event event) {
                if (event.widget instanceof ToolItem) {
                    Double value = (Double) ((ToolItem) event.widget).getData();
                    confirmValue(value.doubleValue());
                }
            }
        };

        public ZoomPopup(Shell parent) {
            super(parent, SWT.TOOL, true, false, false, false, false, null,
                    null);
        }

        public void update(IGraphicalEditorPage page, ZoomManager zoomManager,
                Point pos) {
            this.page = page;
            this.zoomManager = zoomManager;
            this.triggerPosition = pos;
        }

        protected Control createDialogArea(Composite parent) {
            Composite composite = (Composite) super.createDialogArea(parent);

            fillValueInput(composite);

            Composite composite2 = new Composite(composite, SWT.NONE);
            GridLayout layout2 = new GridLayout(2, false);
            layout2.marginWidth = 0;
            layout2.marginHeight = 0;
            layout2.verticalSpacing = 0;
            layout2.horizontalSpacing = 0;
            composite2.setLayout(layout2);
            composite2.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));

            fillDirectSelector(composite2);
            fillSlider(composite2);

            return composite;
        }

        private void fillValueInput(Composite parent) {
            valueInput = new Text(parent, SWT.BORDER | SWT.SINGLE);
            valueInput.setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, false));

            valueInput.addListener(SWT.Modify, new Listener() {
                public void handleEvent(Event event) {
                    if (internalModifying)
                        return;

                    internalModifying = true;
                    try {
                        int intValue = Integer.parseInt(valueInput.getText(),
                                10);
                        updateSlider(intValue / 100.0);
                    } catch (NumberFormatException e) {
                        // ignore
                    } finally {
                        internalModifying = false;
                    }
                }
            });
            Listener inputConfirmListener = new Listener() {
                public void handleEvent(Event event) {
                    if (event.type == SWT.DefaultSelection
                            || (event.type == SWT.Traverse
                                    && event.detail == SWT.TRAVERSE_RETURN)) {
                        try {
                            int intValue = Integer
                                    .parseInt(valueInput.getText(), 10);
                            confirmValue(intValue / 100.0);
                        } catch (NumberFormatException e) {
                            // ignore
                        }
                        event.doit = false;
                    }
                }
            };
            valueInput.addListener(SWT.DefaultSelection, inputConfirmListener);
            valueInput.addListener(SWT.Traverse, inputConfirmListener);

            valueInput.addFocusListener(new FocusListener() {

                @Override
                public void focusLost(FocusEvent e) {
                    if (e.display.getActiveShell() != getShell()) {
                        close();
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });
        }

        private void fillDirectSelector(Composite parent) {
            ToolBar toolbar = new ToolBar(parent, SWT.VERTICAL);
            toolbar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

            addDirectSelectorItems(toolbar, directSelectorInput);
        }

        private void addDirectSelectorItems(ToolBar parent, double[] values) {
            for (double value : values) {
                addDirectSelectorItem(parent, value);
            }
        }

        private void addDirectSelectorItem(ToolBar parent, double value) {
            ToolItem item = new ToolItem(parent, SWT.PUSH);
            item.setText(String.valueOf((int) Math.round(value * 100)));
            item.setData(Double.valueOf(value));
            item.addListener(SWT.Selection, directSelectorItemListener);
        }

        private void fillSlider(Composite parent) {
            slider = new SliderViewer(parent, SWT.VERTICAL);
            slider.getControl().setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, false, true));
            ((GridData) slider.getControl().getLayoutData()).heightHint = 40;
            slider.setContentProvider(new ZoomSliderContentProvider());
            slider.setLabelProvider(new ZoomSliderLabelProvider());
            slider.setInput(this);
            slider.addSelectionChangedListener(new ISelectionChangedListener() {
                public void selectionChanged(SelectionChangedEvent event) {
                    if (internalModifying)
                        return;

                    internalModifying = true;
                    try {
                        Double value = (Double) slider.getSelectionValue();
                        updateValueInput(value.doubleValue());
                    } catch (NumberFormatException e) {
                        // ignore
                    } finally {
                        internalModifying = false;
                    }
                }
            });
            slider.addPostSelectionChangedListener(
                    new ISelectionChangedListener() {
                        public void selectionChanged(
                                SelectionChangedEvent event) {
                            if (internalModifying)
                                return;

                            Double value = (Double) slider.getSelectionValue();
                            if (value != null) {
                                confirmValue(value.doubleValue());
                            }
                        }
                    });

            slider.getControl().addFocusListener(new FocusListener() {

                @Override
                public void focusLost(FocusEvent e) {
                    if (e.display.getActiveShell() != getShell()) {
                        close();
                    }
                }

                @Override
                public void focusGained(FocusEvent e) {
                }
            });
        }

        protected Control getFocusControl() {
            return valueInput;
        }

        protected void adjustBounds() {
            internalModifying = true;
            try {
                double doubleValue = zoomManager.getScale();
                updateValueInput(doubleValue);
                updateSlider(doubleValue);
            } finally {
                internalModifying = false;
            }

            getShell().pack();
            Point size = getShell().getSize();
            if (triggerPosition != null) {
                getShell().setLocation(triggerPosition.x,
                        triggerPosition.y - size.y);
            }
        }

        private void confirmValue(final double newValue) {
            close();
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    if (page == null)
                        return;
                    EditDomain domain = page.getEditDomain();
                    if (domain == null)
                        return;

                    Request request = new Request(GEF.REQ_ZOOM);
                    request.setViewer(page.getViewer());
                    request.setParameter(GEF.PARAM_ZOOM_SCALE,
                            Double.valueOf(newValue));
                    domain.handleRequest(request);
                }
            });
        }

        private void updateValueInput(double doubleValue) {
            valueInput.setText(
                    String.valueOf((int) Math.round(doubleValue * 100)));
            valueInput.setSelection(valueInput.getText().length());
        }

        private void updateSlider(double doubleValue) {
            slider.setSelection(
                    new StructuredSelection(Double.valueOf(doubleValue)));
        }

        @SuppressWarnings("unchecked")
        protected List getBackgroundColorExclusions() {
            List list = super.getBackgroundColorExclusions();
            list.add(valueInput);
            return list;
        }

    }

    private ZoomManager zoomManager = null;

    private IGraphicalEditor editor;

    private ToolItem zoomOutItem;

    private ToolItem zoomValueItem;

    private ToolItem zoomInItem;

    private ToolItem zoomDefaultItem;

    private ZoomPopup zoomPopup = null;

    private ToolBar toolBar;

    public MiniZoomContribution(IGraphicalEditor editor) {
        this(ActionConstants.MINI_ZOOM, editor);
    }

    public MiniZoomContribution(String id, IGraphicalEditor editor) {
        super(id);
        this.editor = editor;
        editor.addPageChangedListener(this);
    }

    public boolean isDynamic() {
        return true;
    }

    public void dispose() {
        if (zoomOutItem != null) {
            zoomOutItem.dispose();
            zoomOutItem = null;
        }
        if (zoomValueItem != null) {
            zoomValueItem.dispose();
            zoomValueItem = null;
        }
        if (zoomInItem != null) {
            zoomInItem.dispose();
            zoomInItem = null;
        }
        if (zoomDefaultItem != null) {
            zoomDefaultItem.dispose();
            zoomDefaultItem = null;
        }
        setZoomManager(null);
        if (editor != null) {
            editor.removePageChangedListener(this);
            editor = null;
        }
        super.dispose();
    }

    public void fill(ToolBar parent, int index) {
        toolBar = parent;
        if (index < 0) {
            zoomOutItem = new ToolItem(parent, SWT.PUSH);
            zoomValueItem = new ToolItem(parent, SWT.PUSH);
            zoomInItem = new ToolItem(parent, SWT.PUSH);
            zoomDefaultItem = new ToolItem(parent, SWT.PUSH);
        } else {
            zoomOutItem = new ToolItem(parent, SWT.PUSH, index++);
            zoomValueItem = new ToolItem(parent, SWT.PUSH, index++);
            zoomInItem = new ToolItem(parent, SWT.PUSH, index++);
            zoomDefaultItem = new ToolItem(parent, SWT.PUSH, index++);
        }

        zoomValueItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                Rectangle r = zoomValueItem.getBounds();
                showZoomPopup(event,
                        zoomValueItem.getParent().toDisplay(r.x, r.y));
            }
        });
        zoomOutItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                sendRequest(GEF.REQ_ZOOMOUT);
            }
        });
        zoomInItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                sendRequest(GEF.REQ_ZOOMIN);
            }
        });
        zoomDefaultItem.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                sendRequest(GEF.REQ_ACTUALSIZE);
            }
        });

        initItemLabel(zoomOutItem, IMindMapImages.ZOOMOUT_SMALL,
                MindMapMessages.ZoomOut_toolTip);
        initItemLabel(zoomInItem, IMindMapImages.ZOOMIN_SMALL,
                MindMapMessages.ZoomIn_toolTip);
        initItemLabel(zoomDefaultItem, IMindMapImages.ACTUAL_SIZE_SMALL,
                MindMapMessages.ActualSize_toolTip);

        update(null);
    }

    private void initItemLabel(final ToolItem item, String iconPath,
            String fallbackText) {
        Image image = createImage(iconPath, true);
        if (image != null) {
            item.setImage(image);
            item.setDisabledImage(createImage(iconPath, false));
            item.addDisposeListener(new DisposeListener() {
                public void widgetDisposed(DisposeEvent e) {
                    Image image = item.getImage();
                    if (image != null) {
                        image.dispose();
                    }
                    image = item.getDisabledImage();
                    if (image != null) {
                        image.dispose();
                    }
                }
            });
        } else {
            item.setText(fallbackText);
        }
    }

    private Image createImage(String iconPath, boolean enabled) {
        ImageDescriptor imageDescriptor = MindMapUI.getImages().get(iconPath,
                enabled);
        return imageDescriptor == null ? null
                : imageDescriptor.createImage(false);
    }

    public void update() {
        update(null);
    }

    public void update(String id) {
        if (zoomValueItem != null && !zoomValueItem.isDisposed()) {
            if (zoomManager == null) {
                zoomValueItem.setText("100%"); //$NON-NLS-1$
            } else {
                double scale = zoomManager.getScale();
                int s = (int) Math.round(scale * 100);
                zoomValueItem.setText(
                        String.format("%4s", String.format("%d%%", s))); //$NON-NLS-1$ //$NON-NLS-2$
            }

            if (toolBar != null && !toolBar.isDisposed()) {
                toolBar.pack();
            }
        }
    }

    private void refresh() {
        update(null);
        getParent().update(true);
    }

    public void pageChanged(PageChangedEvent event) {
        setZoomManager(getZoomManager(event.getSelectedPage()));
    }

    public void scaleChanged(ZoomObject source, double oldValue,
            double newValue) {
        refresh();
    }

    private ZoomManager getZoomManager(Object page) {
        if (page == null || !(page instanceof IGraphicalEditorPage))
            return null;

        IGraphicalViewer viewer = ((IGraphicalEditorPage) page).getViewer();
        if (viewer == null)
            return null;

        return viewer.getZoomManager();
    }

    private void setZoomManager(ZoomManager zoomManager) {
        if (zoomManager == this.zoomManager)
            return;

        if (this.zoomManager != null) {
            this.zoomManager.removeZoomListener(this);
        }
        this.zoomManager = zoomManager;
        if (zoomManager != null) {
            zoomManager.addZoomListener(this);
        }
        refresh();
    }

    private void showZoomPopup(Event event, Point itemLocation) {
        if (zoomManager == null)
            return;

        if (zoomPopup == null) {
            zoomPopup = new ZoomPopup(editor.getSite().getShell());
        }
        zoomPopup.update(editor.getActivePageInstance(), zoomManager,
                itemLocation);
        zoomPopup.open();
    }

    private void sendRequest(final String reqType) {
        if (editor == null)
            return;

        final IGraphicalEditorPage page = editor.getActivePageInstance();
        if (page == null)
            return;

        final EditDomain domain = page.getEditDomain();
        if (domain == null)
            return;

        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                domain.handleRequest(reqType, page.getViewer());
            }
        });
    }

}
