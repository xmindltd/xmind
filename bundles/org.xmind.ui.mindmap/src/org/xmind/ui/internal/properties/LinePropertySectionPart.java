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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.xmind.gef.Request;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.style.Styles;
import org.xmind.ui.viewers.MComboViewer;

public class LinePropertySectionPart extends LinePropertySectionPartBase {

    private class LinePatternSelectionChangedListener implements
            ISelectionChangedListener {
        public void selectionChanged(SelectionChangedEvent event) {
            if (isRefreshing())
                return;

            Object o = ((IStructuredSelection) event.getSelection())
                    .getFirstElement();
            if (o instanceof LinePattern) {
                changeLinePattern((LinePattern) o);
            }
        }
    }

    private MComboViewer linePatternViewer;

    protected void refreshWithShapeId(String lineShapeId) {
        if (linePatternViewer != null
                && !linePatternViewer.getControl().isDisposed()) {
            String lineWidth = getStyleValue(Styles.LinePattern, lineShapeId);
            LinePattern element = LinePattern.findByValue(lineWidth);
            if (element == null)
                element = LinePattern.Solid;
            linePatternViewer.setSelection(new StructuredSelection(element));
        }
        super.refreshWithShapeId(lineShapeId);
    }

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
        createLineContent1(line1);

        super.createContent(parent);
    }

    private void createLineContent1(Composite parent) {
        linePatternViewer = new MComboViewer(parent, MComboViewer.NORMAL);
        linePatternViewer.getControl().setLayoutData(
                new GridData(GridData.FILL, GridData.FILL, true, false));
        linePatternViewer.getControl().setToolTipText(
                PropertyMessages.LinePattern_toolTip);
        linePatternViewer.setContentProvider(new ArrayContentProvider());
        linePatternViewer.setLabelProvider(new LinePatternLabelProvider());
        linePatternViewer.setInput(LinePattern.values());
        linePatternViewer
                .addSelectionChangedListener(new LinePatternSelectionChangedListener());
    }

    public void setFocus() {
        if (linePatternViewer != null
                && !linePatternViewer.getControl().isDisposed()) {
            linePatternViewer.getControl().setFocus();
        } else {
            super.setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        linePatternViewer = null;
    }

    private void changeLinePattern(LinePattern linePattern) {
        Request request = createStyleRequest(CommandMessages.Command_ModifyLineShape);
        String value = linePattern == null ? null : linePattern.getValue();
        addStyle(request, Styles.LinePattern, value);
        sendRequest(request);
    }

}