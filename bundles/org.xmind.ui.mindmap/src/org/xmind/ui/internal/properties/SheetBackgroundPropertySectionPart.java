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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.color.PaletteContents;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;

public class SheetBackgroundPropertySectionPart extends
        StyledPropertySectionPart {

    private class BackgroundColorOpenListener implements IOpenListener {

        public void open(OpenEvent event) {
            changeBackgroundColor((IColorSelection) event.getSelection());
        }

    }

    private Control bar;

    private ColorPicker backgroundColorPicker;

    protected GridLayout createLayout(Composite parent) {
        GridLayout layout = super.createLayout(parent);
        layout.numColumns = 2;
        layout.horizontalSpacing = 7;
        return layout;
    }

    protected void createContent(Composite parent) {
        Label caption = new Label(parent, SWT.NONE);
        caption.setText(PropertyMessages.BackgroundColor_label);
        caption.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                false, false));

        backgroundColorPicker = new ColorPicker(ColorPicker.AUTO
                | ColorPicker.CUSTOM, PaletteContents.getDefault());
        backgroundColorPicker.getAction().setToolTipText(
                PropertyMessages.BackgroundColor_toolTip);
        backgroundColorPicker
                .addOpenListener(new BackgroundColorOpenListener());
        ToolBarManager colorBar = new ToolBarManager(SWT.FLAT);
        colorBar.add(backgroundColorPicker);
        bar = colorBar.createControl(parent);
        bar.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER,
                false, false));
    }

    protected void doRefresh() {
        updateColorPicker(backgroundColorPicker, Styles.FillColor, null);
    }

    public void setFocus() {
        if (bar != null && !bar.isDisposed()) {
            bar.setFocus();
        }
    }

    public void dispose() {
        super.dispose();
        bar = null;
        backgroundColorPicker = null;
    }

    private void changeBackgroundColor(IColorSelection selection) {
        changeColor(selection, Styles.FillColor,
                CommandMessages.Command_ModifySheetBackgroundColor);
    }

}