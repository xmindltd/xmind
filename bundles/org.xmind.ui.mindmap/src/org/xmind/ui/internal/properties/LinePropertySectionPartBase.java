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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.xmind.gef.Request;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.MComboViewer;

public class LinePropertySectionPartBase extends StyledPropertySectionPart {

    private class LineColorOpenListener implements IOpenListener {
        public void open(OpenEvent event) {
            changeLineColor((IColorSelection) event.getSelection());
        }
    }

    private class LineWidthSelectionChangedListener implements
            ISelectionChangedListener {

        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof LineWidth) {
                changeLineWidth((LineWidth) o);
            }
        }
    }

    private ColorPicker lineColorPicker;

    private MComboViewer lineWidthViewer;

    protected void doRefresh() {
        String lineShapeId = getLineShapeId();
        refreshWithShapeId(lineShapeId);
    }

    protected String getLineShapeId() {
        return getStyleValue(Styles.LineClass, null);
    }

    protected void refreshWithShapeId(String lineShapeId) {
        if (lineWidthViewer != null
                && !lineWidthViewer.getControl().isDisposed()) {
            String lineWidth = getStyleValue(Styles.LineWidth, lineShapeId);
            LineWidth element = LineWidth.findByValue(lineWidth);
            if (element == null)
                element = LineWidth.Thinnest;
            lineWidthViewer.setSelection(new StructuredSelection(element));
        }
        if (lineColorPicker != null) {
            updateColorPicker(lineColorPicker, Styles.LineColor, lineShapeId);
        }
    }

    protected void createContent(Composite parent) {
        Composite line = new Composite(parent, SWT.NONE);
        line.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true,
                false));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 3;
        layout.verticalSpacing = 3;
        line.setLayout(layout);

        createLineContent(line);
    }

    private void createLineContent(Composite parent) {
        lineWidthViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        lineWidthViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        lineWidthViewer.getControl().setToolTipText(
                PropertyMessages.LineWidth_toolTip);
        lineWidthViewer.setContentProvider(new ArrayContentProvider());
        lineWidthViewer.setLabelProvider(new LineWidthLabelProvider());
        lineWidthViewer.setInput(LineWidth.values());
        lineWidthViewer
                .addSelectionChangedListener(new LineWidthSelectionChangedListener());

        lineColorPicker = new ColorPicker(
                ColorPicker.AUTO | ColorPicker.CUSTOM, PaletteContents
                        .getDefault());
        lineColorPicker.getAction().setToolTipText(
                PropertyMessages.LineColor_toolTip);
        lineColorPicker.addOpenListener(new LineColorOpenListener());
        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(lineColorPicker);
        ToolBar barControl = colorBar.createControl(parent);
        barControl.setLayoutData(new GridData(GridData.END, GridData.CENTER,
                false, false));
    }

    public void setFocus() {
        if (lineWidthViewer != null
                && !lineWidthViewer.getControl().isDisposed()) {
            lineWidthViewer.getControl().setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        lineWidthViewer = null;
        lineColorPicker = null;
    }

    protected void changeLineColor(IColorSelection selection) {
        changeColor(selection, Styles.LineColor,
                CommandMessages.Command_ModifyLineColor);
    }

    protected void changeLineWidth(LineWidth lineWidth) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyLineShape);
        String value = lineWidth == null ? null : lineWidth.getValue();
        addStyle(request, Styles.LineWidth, value);
        sendRequest(request);
    }

}