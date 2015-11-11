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
package org.xmind.ui.internal.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.core.Core;
import org.xmind.core.INumbering;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.INumberFormatDescriptor;
import org.xmind.ui.mindmap.INumberSeparatorDescriptor;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.MindMapPropertySectionPartBase;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.viewers.MComboViewer;

public class NumberingPropertySectionPart extends
        MindMapPropertySectionPartBase {

//    private static class BalanceLayout extends Layout {
//
//        public Control left;
//
//        public Control center;
//
//        public Control right;
//
//        public int spacing = 3;
//
//        protected Point computeSize(Composite composite, int wHint, int hHint,
//                boolean flushCache) {
//            Point size = new Point(0, 0);
//            if (wHint >= 0)
//                size.x = wHint;
//            if (hHint >= 0)
//                size.y = hHint;
//            if (wHint < 0 || hHint < 0) {
//                Point centerSize;
//                if (center == null) {
//                    centerSize = null;
//                } else {
//                    centerSize = center.computeSize(SWT.DEFAULT, hHint,
//                            flushCache);
//                }
//                Point leftSize;
//                Point rightSize;
//                if (left != null || right != null) {
//                    leftSize = left == null ? null : left.computeSize(
//                            SWT.DEFAULT, hHint, flushCache);
//                    rightSize = right == null ? null : right.computeSize(
//                            SWT.DEFAULT, hHint, flushCache);
//                } else {
//                    leftSize = null;
//                    rightSize = null;
//                }
//                if (hHint < 0) {
//                    if (centerSize != null)
//                        size.y = Math.max(size.y, centerSize.y);
//                    if (leftSize != null)
//                        size.y = Math.max(size.y, leftSize.y);
//                    if (rightSize != null)
//                        size.y = Math.max(size.y, rightSize.y);
//                }
//                if (wHint < 0) {
//                    int width = 0;
//                    if (leftSize != null)
//                        width = Math.max(width, leftSize.x);
//                    if (rightSize != null)
//                        width = Math.max(width, rightSize.x);
//                    if (centerSize != null)
//                        width += centerSize.x;
//                    size.x = Math.max(size.x, width);
//                }
//            }
//
//            // avoid using default width or height
//            size.x = Math.max(1, size.x);
//            size.y = Math.max(1, size.y);
//            return size;
//        }
//
//        protected void layout(Composite composite, boolean flushCache) {
//            Rectangle area = composite.getClientArea();
//            if (center != null) {
//                if (left != null || right != null) {
//                    Point centerSize = center.computeSize(SWT.DEFAULT,
//                            area.height, flushCache);
//                    int maxCenterWidth = area.width - spacing * 2 - 20;
//                    int centerWidth = Math.min(centerSize.x, maxCenterWidth);
//                    center.setBounds(area.x + (area.width - centerWidth) / 2,
//                            area.y, centerWidth, area.height);
//                    int sideWidth = (area.width - spacing * 2 - centerSize.x) / 2;
//                    if (left != null)
//                        left.setBounds(area.x, area.y, sideWidth, area.height);
//                    if (right != null)
//                        right.setBounds(area.x + area.width - sideWidth,
//                                area.y, sideWidth, area.height);
//                } else {
//                    center.setBounds(area);
//                }
//            } else if (left != null) {
//                int sideWidth = Math.max(0, area.width
//                        - (right == null ? 0 : spacing)) / 2;
//                left.setBounds(area.x, area.y, sideWidth, area.height);
//                if (right != null)
//                    right.setBounds(area.x + area.width - sideWidth, area.y,
//                            sideWidth, area.height);
//            } else if (right != null) {
//                int rightWidth = Math.max(0, area.width / 2);
//                right.setBounds(area.x + area.width - rightWidth, area.y,
//                        rightWidth, area.height);
//            }
//        }
//
//    }

    private class NumberFormatLabelProvider extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof INumberFormatDescriptor) {
                INumberFormatDescriptor desc = (INumberFormatDescriptor) element;
                String name = desc.getName();
                String description = desc.getDescription();
                if (description == null || "".equals(description)) //$NON-NLS-1$
                    return name;
                return NLS.bind("{0} ({1})", name, description); //$NON-NLS-1$
            }
            return super.getText(element);
        }
    }

    private class NumberSeparatorLabelProvider extends LabelProvider {
        public String getText(Object element) {
            if (element instanceof INumberSeparatorDescriptor) {
                INumberSeparatorDescriptor desc = (INumberSeparatorDescriptor) element;
                String name = desc.getName();
                String description = desc.getDescription();
                if (description == null || "".equals(description)) //$NON-NLS-1$
                    return name;
                return NLS.bind("{0} ({1})", name, description); //$NON-NLS-1$
            }
            return super.getText(element);
        }
    }

    private class NumberFormatSelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof INumberFormatDescriptor) {
                changeNumberFormat(((INumberFormatDescriptor) o).getId());
            }
        }

    }

    private class SeparatorFormatSelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof INumberSeparatorDescriptor) {
                changeNumberSeparator(((INumberSeparatorDescriptor) o).getId());
            }

        }

    }

    private class PrependingAction extends Action {

        public PrependingAction() {
            super(null, AS_CHECK_BOX);
            setImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.NUMBERING_INHERIT, true));
            setDisabledImageDescriptor(MindMapUI.getImages().get(
                    IMindMapImages.NUMBERING_INHERIT, false));
            setToolTipText(PropertyMessages.PrependNumbering_toolTip);
            setChecked(true);
        }

        public void run() {
            changePrepending(isChecked());
        }
    }

    private MComboViewer formatViewer;

    private IAction prependingAction;

    private Label separatorLabel;

    private MComboViewer separatorViewer;

    private Composite line3;

    private Text prefixInput;

    private Text suffixInput;

    private Text numberLabel;

    private Composite line2;

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

        line3 = new Composite(parent, SWT.NONE);
        line3.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout3 = new GridLayout(2, false);
        layout3.marginWidth = 0;
        layout3.marginHeight = 0;
        layout3.verticalSpacing = 0;
        layout3.horizontalSpacing = 3;
        line3.setLayout(layout3);
        createLineContent3(line3);

        line2 = new Composite(parent, SWT.NONE);
        line2.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout2 = new GridLayout(3, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.verticalSpacing = 0;
        layout2.horizontalSpacing = 3;
        line2.setLayout(layout2);
        createLineContent2(line2);
        prefixInput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        numberLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        suffixInput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    private void createLineContent1(Composite parent) {
        formatViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        formatViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        formatViewer.setContentProvider(new ArrayContentProvider());
        formatViewer.setLabelProvider(new NumberFormatLabelProvider());
        List<INumberFormatDescriptor> descriptors = MindMapUI
                .getNumberFormatManager().getDescriptors();
        List<Object> list = new ArrayList<Object>(descriptors.size() + 1);
        Object separator = new Object();
        INumberFormatDescriptor defaultDescriptor = MindMapUI
                .getNumberFormatManager().getDescriptor(
                        MindMapUI.DEFAULT_NUMBER_FORMAT);
        for (INumberFormatDescriptor desc : descriptors) {
            if (desc != null && defaultDescriptor != null
                    && desc != defaultDescriptor) {
                list.add(desc);
            }
        }
        if (defaultDescriptor != null) {
            list.add(separator);
            list.add(defaultDescriptor);
        }
        formatViewer.setSeparatorImitation(separator);
        formatViewer.setInput(list);
        formatViewer
                .addSelectionChangedListener(new NumberFormatSelectionChangedListener());

        prependingAction = new PrependingAction();
        ToolBarManager bar = new ToolBarManager(SWT.FLAT);
        bar.add(prependingAction);
        ToolBar barControl = bar.createControl(parent);
        barControl.setLayoutData(new GridData(GridData.END, GridData.CENTER,
                false, false));
    }

    private void createLineContent3(Composite parent) {
        separatorLabel = new Label(parent, SWT.NONE);
        separatorLabel.setText(PropertyMessages.Separator_label);

        separatorViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        separatorViewer.getControl().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, false));
        separatorViewer.setContentProvider(new ArrayContentProvider());
        separatorViewer.setLabelProvider(new NumberSeparatorLabelProvider());
        List<INumberSeparatorDescriptor> descriptions = MindMapUI
                .getNumberSeparatorManager().getDescriptors();
        List<Object> list = new ArrayList<Object>(descriptions.size() + 1);
        Object separator = new Object();
        INumberSeparatorDescriptor defautDescriptor = MindMapUI
                .getNumberSeparatorManager().getDescriptor(
                        MindMapUI.DEFAULT_NUMBER_SEPARATOR);
        if (defautDescriptor != null) {
            list.add(defautDescriptor);
            list.add(separator);
        }
        for (INumberSeparatorDescriptor desc : descriptions) {
            if (desc != null && defautDescriptor != null
                    && desc != defautDescriptor) {
                list.add(desc);
            }
        }
        separatorViewer.setSeparatorImitation(separator);
        separatorViewer.setInput(list);
        separatorViewer
                .addSelectionChangedListener(new SeparatorFormatSelectionChangedListener());

    }

    private void createLineContent2(Composite parent) {
        prefixInput = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        prefixInput.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                true, false));
        prefixInput.setToolTipText(PropertyMessages.Prefix_toolTip);
        Listener eventHandler = new Listener() {
            public void handleEvent(Event event) {
                if (event.type == SWT.FocusIn) {
                    if (event.widget == prefixInput)
                        prefixInput.selectAll();
                    else
                        suffixInput.selectAll();
                } else {
                    if (event.widget == prefixInput)
                        changePrefix(prefixInput.getText());
                    else
                        changeSuffix(suffixInput.getText());
                }
            }
        };
        prefixInput.addListener(SWT.DefaultSelection, eventHandler);
        prefixInput.addListener(SWT.FocusOut, eventHandler);
        prefixInput.addListener(SWT.FocusIn, eventHandler);

        numberLabel = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY
                | SWT.CENTER);
        numberLabel.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                false, false));
        numberLabel.setEditable(false);
        numberLabel.setBackground(numberLabel.getDisplay().getSystemColor(
                SWT.COLOR_LIST_BACKGROUND));

        suffixInput = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        suffixInput.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                true, false));
        suffixInput.setToolTipText(PropertyMessages.Suffix_toolTip);
        suffixInput.addListener(SWT.DefaultSelection, eventHandler);
        suffixInput.addListener(SWT.FocusOut, eventHandler);
        suffixInput.addListener(SWT.FocusIn, eventHandler);
    }

    public void setFocus() {
        if (formatViewer != null && !formatViewer.getControl().isDisposed()) {
            formatViewer.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        formatViewer = null;
        prependingAction = null;
        prefixInput = null;
        numberLabel = null;
        suffixInput = null;
        line2 = null;
        separatorLabel = null;
        separatorViewer = null;
        line3 = null;
    }

    protected void doRefresh() {
        Object o = ((IStructuredSelection) getCurrentSelection())
                .getFirstElement();
        if (o instanceof ITopic) {
            ITopic topic = (ITopic) o;
            ITopic parent = topic.getParent();
            if (parent == null)
                parent = topic;
            INumbering numbering;
            if (ITopic.ATTACHED.equals(topic.getType())) {
                numbering = parent.getNumbering();
            } else {
                numbering = null;
            }
            boolean hasFormat = false;
            if (formatViewer != null && !formatViewer.getControl().isDisposed()) {
                String format = numbering == null ? null : numbering
                        .getComputedFormat();
                if (format == null) {
                    format = MindMapUI.DEFAULT_NUMBER_FORMAT;
                } else {
                    hasFormat = !MindMapUI.DEFAULT_NUMBER_FORMAT.equals(format);
                }
                INumberFormatDescriptor descriptor = MindMapUI
                        .getNumberFormatManager().getDescriptor(format);
                formatViewer
                        .setSelection(descriptor == null ? StructuredSelection.EMPTY
                                : new StructuredSelection(descriptor));
            }
            if (separatorViewer != null
                    && !separatorViewer.getControl().isDisposed()) {
                String separator = numbering == null ? null : numbering
                        .getComputedSeparator();
                if (separator == null)
                    separator = MindMapUI.DEFAULT_NUMBER_SEPARATOR;
                INumberSeparatorDescriptor descriptor = MindMapUI
                        .getNumberSeparatorManager().getDescriptor(separator);
                separatorViewer
                        .setSelection(descriptor == null ? StructuredSelection.EMPTY
                                : new StructuredSelection(descriptor));
            }
            if (prependingAction != null) {
                prependingAction.setChecked(numbering != null
                        && numbering.prependsParentNumbers());
            }
            if (prefixInput != null && !prefixInput.isDisposed()) {
                String prefix = numbering == null ? null : numbering
                        .getPrefix();
                prefixInput.setText(prefix == null ? "" : prefix); //$NON-NLS-1$
            }
            if (suffixInput != null && !suffixInput.isDisposed()) {
                String suffix = numbering == null ? null : numbering
                        .getSuffix();
                suffixInput.setText(suffix == null ? "" : suffix); //$NON-NLS-1$
            }
            if (numberLabel != null && !numberLabel.isDisposed()) {
                String number;
                number = MindMapUtils.getNumberingText(topic, hasFormat ? null
                        : MindMapUI.PREVIEW_NUMBER_FORMAT, hasFormat ? null
                        : MindMapUI.DEFAULT_NUMBER_SEPARATOR);
                if (number == null || "".equals(number)) { //$NON-NLS-1$
                    numberLabel.setText(" "); //$NON-NLS-1$
                } else {
                    number = GraphicsUtils.getNormal().constrain(number, 100,
                            JFaceResources.getDefaultFont(),
                            GraphicsUtils.TRAIL);
                    numberLabel.setText(number);
                }
                if (hasFormat) {
                    numberLabel.setForeground(numberLabel.getDisplay()
                            .getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                } else {
                    numberLabel.setForeground(numberLabel.getDisplay()
                            .getSystemColor(SWT.COLOR_DARK_GRAY));
                }
            }
            if (line2 != null && !line2.isDisposed()) {
                line2.layout();
            }
        }
    }

    protected void registerEventListener(Object source,
            ICoreEventRegister register) {
        if (source instanceof ITopic) {
            ITopic parent = ((ITopic) source).getParent();
            if (parent == null)
                parent = (ITopic) source;
            register.setNextSourceFrom(parent);
            register.register(Core.TopicAdd);
            register.register(Core.TopicRemove);
            INumbering numbering = parent.getNumbering();
            register.setNextSourceFrom(numbering);
            register.register(Core.NumberFormat);
            register.register(Core.NumberingPrefix);
            register.register(Core.NumberingSuffix);
            register.register(Core.NumberPrepending);
            register.register(Core.NumberingSeparator);
        }
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TopicAdd.equals(type) || Core.TopicRemove.equals(type)) {
            if (!ITopic.ATTACHED.equals(event.getData()))
                return;
        }
        super.handleCoreEvent(event);
    }

    private void changeNumberFormat(String formatId) {
        if (formatId != null) {
            Object o = ((IStructuredSelection) getCurrentSelection())
                    .getFirstElement();
            if (o instanceof ITopic) {
                ITopic topic = ((ITopic) o).getParent();
                if (topic == null)
                    topic = (ITopic) o;
//                if (formatId.equals(topic.getNumbering().getParentFormat()))
                if (formatId.equals(topic.getNumbering().getNumberFormat()))
                    formatId = null;
            }
        }
        sendRequest(fillTargets(new Request(MindMapUI.REQ_MODIFY_NUMBERING))
                .setParameter(MindMapUI.PARAM_NUMBERING_FORMAT, formatId));
    }

    private void changeNumberSeparator(String separatorId) {
        if (separatorId != null) {
            Object o = ((IStructuredSelection) getCurrentSelection())
                    .getFirstElement();
            if (o instanceof ITopic) {
                ITopic topic = ((ITopic) o).getParent();
                if (topic == null)
                    topic = (ITopic) o;
                if (separatorId.equals(topic.getNumbering().getSeparator()))
                    separatorId = null;
            }
        }
        sendRequest(fillTargets(new Request(MindMapUI.REQ_MODIFY_NUMBERING))
                .setParameter(MindMapUI.PARAM_NUMBERING_SEPARATOR, separatorId));
    }

    private void changePrepending(boolean prepend) {
        sendRequest(fillTargets(new Request(MindMapUI.REQ_MODIFY_NUMBERING))
                .setParameter(MindMapUI.PARAM_NUMBERING_PREPENDING,
                        Boolean.valueOf(prepend)));
    }

    private void changePrefix(String newPrefix) {
        sendRequest(fillTargets(new Request(MindMapUI.REQ_MODIFY_NUMBERING)
                .setParameter(MindMapUI.PARAM_NUMBERING_PREFIX, newPrefix)));
    }

    private void changeSuffix(String newSuffix) {
        sendRequest(fillTargets(new Request(MindMapUI.REQ_MODIFY_NUMBERING)
                .setParameter(MindMapUI.PARAM_NUMBERING_SUFFIX, newSuffix)));
    }
}
