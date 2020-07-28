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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.xmind.core.Core;
import org.xmind.core.ILegend;
import org.xmind.core.ISheet;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.Request;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;

public class LegendPropertySectionPart extends StyledPropertySectionPart {

    private class BackgroundColorOpenListener implements IOpenListener {

        public void open(OpenEvent event) {
            MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                    UserDataConstants.CATEGORY_FORMAT,
                    UserDataConstants.CHANGE_LEGEND_BACKGROUND);
            changeBackgroundColor((IColorSelection) event.getSelection());
        }

    }

    private Button visibilityCheck;

    private Control bar;

    private ColorPicker backgroundColorPicker;

    protected void createContent(Composite parent) {
        visibilityCheck = new Button(parent, SWT.CHECK);
        visibilityCheck.setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, true, false));
        visibilityCheck.setText(PropertyMessages.ShowLegend_text);
        visibilityCheck.setToolTipText(PropertyMessages.ShowLegend_toolTip);
        visibilityCheck.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                changeLegendVisibility(visibilityCheck.getSelection());
            }
        });

        createBackgroundPart(parent);
    }

    private void createBackgroundPart(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(22, false);
        layout.horizontalSpacing = 7;
        composite.setLayout(layout);

        Label caption = new Label(composite, SWT.NONE);
        caption.setText(PropertyMessages.BackgroundColor_label);
        caption.setLayoutData(
                new GridData(GridData.FILL, GridData.CENTER, false, false));

        backgroundColorPicker = new ColorPicker(
                ColorPicker.AUTO | ColorPicker.CUSTOM,
                PaletteContents.getDefault());
        backgroundColorPicker.getAction()
                .setToolTipText(PropertyMessages.LegendBackground_toolTip);
        backgroundColorPicker
                .addOpenListener(new BackgroundColorOpenListener());
        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(backgroundColorPicker);
        bar = colorBar.createControl(composite);
        bar.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
                false, false));
    }

    protected void registerEventListener(Object source,
            ICoreEventRegister register) {
        if (source instanceof ISheet) {
            ILegend legend = ((ISheet) source).getLegend();
            register.setNextSourceFrom(legend);
            register.register(Core.Visibility);
        }
    }

    protected void doRefresh() {
        if (visibilityCheck != null && !visibilityCheck.isDisposed()) {
            visibilityCheck.setSelection(isLegendVisible());
        }
        updateColorPicker(backgroundColorPicker, Styles.LegendFillColor, null);
    }

    private boolean isLegendVisible() {
        for (Object o : getSelectedElements()) {
            if (o instanceof ISheet) {
                ILegend legend = ((ISheet) o).getLegend();
                if (!legend.isVisible())
                    return false;
            }
        }
        return true;
    }

    public void setFocus() {
        if (visibilityCheck != null && !visibilityCheck.isDisposed()) {
            visibilityCheck.setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        visibilityCheck = null;
        bar = null;
        backgroundColorPicker = null;
    }

    protected void changeLegendVisibility(boolean visible) {
        Request request = new Request(visible ? MindMapUI.REQ_SHOW_LEGEND
                : MindMapUI.REQ_HIDE_LEGEND);
        sendRequest(fillTargets(request));
    }

    private void changeBackgroundColor(IColorSelection selection) {
        changeColor(selection, Styles.LegendFillColor,
                CommandMessages.Command_ModifyLegendBackgroundColor);
    }

}
