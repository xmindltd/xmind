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
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.xmind.core.Core;
import org.xmind.core.INumbering;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.INumberFormatDescriptor;
import org.xmind.ui.mindmap.INumberSeparatorDescriptor;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.MindMapPropertySectionPartBase;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.viewers.MComboViewer;

public class NumberingPropertySectionPart
        extends MindMapPropertySectionPartBase {

    private static final Object INHERIT = new Object();

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

    private class NumberDepthLabelProvider extends LabelProvider {
        @Override
        public String getText(Object element) {
            if (INHERIT.equals(element))
                return MindMapMessages.NumberingProperty_NumberDepthLabelProvider_Inherit_text;
            if (element instanceof String) {
                String depth = (String) element;
                return NLS.bind("{0} {1}", depth, //$NON-NLS-1$
                        MindMapMessages.NumberingProperty_NumberDepthLabelProvider_Levels_text);
            }
            return super.getText(element);
        }
    }

    private class NumberFormatSelectionChangedListener
            implements ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof INumberFormatDescriptor) {
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_FORMAT,
                        String.format(UserDataConstants.NUMBERING_TYPE_S,
                                ((INumberFormatDescriptor) o).getId()));
                changeNumberFormat(((INumberFormatDescriptor) o).getId());
            }
        }

    }

    private class SeparatorFormatSelectionChangedListener
            implements ISelectionChangedListener {

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

    private class NumberDepthSelectionChangedListener
            implements ISelectionChangedListener {

        @Override
        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();

            if (INHERIT == o) {
                changeNumberDepth(null);
            } else if (o instanceof String) {
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_FORMAT,
                        String.format(UserDataConstants.NUMBERING_DEPTH_S, o));
                changeNumberDepth((String) o);
            }
        }
    }

    private MComboViewer formatViewer;

    private Button tieredCheck;

    private MComboViewer depthViewer;

    private MComboViewer separatorViewer;

    private Text prefixInput;

    private Text suffixInput;

    private Text numberLabel;

    protected void createContent(Composite parent) {
        Composite line1 = createLine(parent);
        line1.setLayout(generateGridLayout(1));
        createNumberingFormatLine(line1);

        Composite line2 = createLine(parent);
        line2.setLayout(generateGridLayout(1));
        createTieredCheckLine(line2);

        Composite line3 = createLine(parent);
        line3.setLayout(generateGridLayout(2));
        createNumberingDepthLine(line3);

        Composite line4 = createLine(parent);
        line4.setLayout(generateGridLayout(2));
        createNumberingSeparatorLine(line4);

        Composite line5 = createLine(parent);
        line5.setLayout(generateGridLayout(3));
        createPrefixAndSuffixLine(line5);
        prefixInput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        numberLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, true));
        suffixInput.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
    }

    private GridLayout generateGridLayout(int cols) {
        GridLayout gridLayout = new GridLayout(cols, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 3;
        gridLayout.verticalSpacing = 3;
        return gridLayout;
    }

    private Composite createLine(Composite parent) {
        Composite line = new Composite(parent, SWT.NONE);
        line.setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        return line;
    }

    private void createNumberingFormatLine(Composite parent) {
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
                .getNumberFormatManager()
                .getDescriptor(MindMapUI.DEFAULT_NUMBER_FORMAT);
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
        formatViewer.addSelectionChangedListener(
                new NumberFormatSelectionChangedListener());
    }

    private void createNumberingSeparatorLine(Composite parent) {
        Label separatorLabel = new Label(parent, SWT.NONE);
        separatorLabel.setText(PropertyMessages.Separator_label);

        separatorViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        separatorViewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        separatorViewer.setContentProvider(new ArrayContentProvider());
        separatorViewer.setLabelProvider(new NumberSeparatorLabelProvider());
        List<INumberSeparatorDescriptor> descriptions = MindMapUI
                .getNumberSeparatorManager().getDescriptors();
        List<Object> list = new ArrayList<Object>(descriptions.size() + 1);
        Object separator = new Object();
        INumberSeparatorDescriptor defautDescriptor = MindMapUI
                .getNumberSeparatorManager()
                .getDescriptor(MindMapUI.DEFAULT_NUMBER_SEPARATOR);
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
        separatorViewer.addSelectionChangedListener(
                new SeparatorFormatSelectionChangedListener());

    }

    private void createPrefixAndSuffixLine(Composite parent) {
        prefixInput = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        prefixInput.setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, true, false));
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

        numberLabel = new Text(parent,
                SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
        numberLabel.setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, false, false));
        numberLabel.setEditable(false);
        numberLabel.setBackground(numberLabel.getDisplay()
                .getSystemColor(SWT.COLOR_LIST_BACKGROUND));

        suffixInput = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.CENTER);
        suffixInput.setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, true, false));
        suffixInput.setToolTipText(PropertyMessages.Suffix_toolTip);
        suffixInput.addListener(SWT.DefaultSelection, eventHandler);
        suffixInput.addListener(SWT.FocusOut, eventHandler);
        suffixInput.addListener(SWT.FocusIn, eventHandler);
    }

    private void createTieredCheckLine(Composite parent) {
        tieredCheck = new Button(parent, SWT.CHECK);
        tieredCheck.setText(MindMapMessages.NumberingProperty_TieredCheck_text);
        tieredCheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                changePrepending(tieredCheck.getSelection());
            }
        });
    }

    private void createNumberingDepthLine(Composite parent) {
        Label label = new Label(parent, SWT.NONE);
        label.setText(PropertyMessages.Depth_label);

        depthViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        depthViewer.getControl()
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        depthViewer.setContentProvider(new ArrayContentProvider());
        depthViewer.setLabelProvider(new NumberDepthLabelProvider());
        @SuppressWarnings("nls")
        String[] values = new String[] { "1", "2", "3", "4", "5", "6", "7", "8",
                "9", "10" };
        List<Object> input = new ArrayList<Object>(values.length + 1);
        input.addAll(Arrays.asList(values));
        Object sep = new Object();
        input.add(sep);
        input.add(INHERIT);
        depthViewer.setSeparatorImitation(sep);
        depthViewer.setInput(input);
        depthViewer.addSelectionChangedListener(
                new NumberDepthSelectionChangedListener());
    }

    public void setFocus() {
        if (formatViewer != null && !formatViewer.getControl().isDisposed()) {
            formatViewer.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        formatViewer = null;
        prefixInput = null;
        numberLabel = null;
        suffixInput = null;
        separatorViewer = null;
        depthViewer = null;
        tieredCheck = null;
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
            if (formatViewer != null
                    && !formatViewer.getControl().isDisposed()) {
                String format = numbering == null ? null
                        : numbering.getComputedFormat();
                if (format == null) {
                    format = MindMapUI.DEFAULT_NUMBER_FORMAT;
                } else if (parent.getNumbering().getNumberFormat() == null
                        && !topic.getNumbering().isInherited(0)) {
                    format = MindMapUI.DEFAULT_NUMBER_FORMAT;
                } else {
                    hasFormat = !MindMapUI.DEFAULT_NUMBER_FORMAT.equals(format);
                }
                INumberFormatDescriptor descriptor = MindMapUI
                        .getNumberFormatManager().getDescriptor(format);
                formatViewer.setSelection(
                        descriptor == null ? StructuredSelection.EMPTY
                                : new StructuredSelection(descriptor));
            }
            if (depthViewer != null && !depthViewer.getControl().isDisposed()) {
                Object select = INHERIT;
                if (numbering != null) {
                    if (numbering.getDepth() != null)
                        select = numbering.getDepth();
                    else if (numbering.getNumberFormat() != null
                            && !numbering.isInherited(1))
                        select = "3"; //$NON-NLS-1$

                    depthViewer.setSelection(new StructuredSelection(select));

                    if (MindMapUI.DEFAULT_NUMBER_FORMAT
                            .equals(numbering.getNumberFormat())) {
                        depthViewer.setEnabled(false);
                    } else {
                        depthViewer
                                .setEnabled(numbering.getNumberFormat() != null
                                        || topic.getNumbering().isInherited(0));
                    }
                }
            }
            if (separatorViewer != null
                    && !separatorViewer.getControl().isDisposed()) {
                String separator = numbering == null ? null
                        : numbering.getComputedSeparator();
                if (separator == null)
                    separator = MindMapUI.DEFAULT_NUMBER_SEPARATOR;
                INumberSeparatorDescriptor descriptor = MindMapUI
                        .getNumberSeparatorManager().getDescriptor(separator);
                separatorViewer.setSelection(
                        descriptor == null ? StructuredSelection.EMPTY
                                : new StructuredSelection(descriptor));
            }
            if (tieredCheck != null) {
                tieredCheck.setSelection(
                        numbering != null && numbering.prependsParentNumbers());
            }
            if (prefixInput != null && !prefixInput.isDisposed()) {
                String prefix = numbering == null ? null
                        : numbering.getPrefix();
                prefixInput.setText(prefix == null ? "" : prefix); //$NON-NLS-1$
            }
            if (suffixInput != null && !suffixInput.isDisposed()) {
                String suffix = numbering == null ? null
                        : numbering.getSuffix();
                suffixInput.setText(suffix == null ? "" : suffix); //$NON-NLS-1$
            }
            if (numberLabel != null && !numberLabel.isDisposed()) {
                String number;
                number = MindMapUtils.getNumberingText(topic,
                        hasFormat ? null : MindMapUI.PREVIEW_NUMBER_FORMAT,
                        hasFormat ? null : MindMapUI.DEFAULT_NUMBER_SEPARATOR);
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
            register.register(Core.NumberingDepth);
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
                .setParameter(MindMapUI.PARAM_NUMBERING_SEPARATOR,
                        separatorId));
    }

    private void changeNumberDepth(String depth) {
        sendRequest(fillTargets(new Request(MindMapUI.REQ_MODIFY_NUMBERING))
                .setParameter(MindMapUI.PARAM_NUMBERING_DEPTH, depth));
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
