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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.gef.Request;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.prefs.PrefMessages;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.properties.StyledPropertySectionPart;
import org.xmind.ui.style.Styles;

public class SheetAdvancedStylePropertySectionPart
        extends StyledPropertySectionPart {

    private Button taperedLinesCheck;

    private Button gradientColorCheck;

    protected void createContent(Composite parent) {

        taperedLinesCheck = new Button(parent, SWT.CHECK);
        taperedLinesCheck.setText(PropertyMessages.TaperedLines_text);
        taperedLinesCheck.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_FORMAT,
                        UserDataConstants.TOGGLE_TAPERED_LINE);
                changeTaperedLines();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        gradientColorCheck = new Button(parent, SWT.CHECK);
        gradientColorCheck
                .setText(PrefMessages.EditorPage_UndoRedo_gradientColor);
        gradientColorCheck.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_FORMAT,
                        UserDataConstants.TOGGLE_GRADIENT_COLOR);
                changeGradientColor();
            }

            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
    }

    protected void changeGradientColor() {
        Request request = createStyleRequest(
                CommandMessages.Command_ToggleGradientColor);
        if (gradientColorCheck.getSelection()) {
            addStyle(request, Styles.GradientColor, Styles.GRADIENT);
        } else {
            addStyle(request, Styles.GradientColor, Styles.NONE);
        }
        sendRequest(request);
    }

    private void changeTaperedLines() {
        Request request = createStyleRequest(
                CommandMessages.Command_ToggleTaperedLines);
        if (taperedLinesCheck.getSelection()) {
            addStyle(request, Styles.LineTapered, Styles.TAPERED);
        } else {
            addStyle(request, Styles.LineTapered, Styles.NONE);
        }
        sendRequest(request);
    }

    protected void doRefresh() {

        if (taperedLinesCheck != null && !taperedLinesCheck.isDisposed()) {
            String value = getStyleValue(Styles.LineTapered, null);
            taperedLinesCheck
                    .setSelection(value != null && !Styles.NONE.equals(value));
        }
        if (gradientColorCheck != null && !gradientColorCheck.isDisposed()) {
            boolean hasGradient = MindMapUIPlugin.getDefault()
                    .getPreferenceStore()
                    .getBoolean(PrefConstants.GRADIENT_COLOR);
            String value = getStyleValue(Styles.GradientColor, null);
            if (Styles.NONE.equals(value))
                hasGradient = false;
            else if (Styles.GRADIENT.equals(value))
                hasGradient = true;
            gradientColorCheck.setSelection(hasGradient);
        }
    }

    public void dispose() {
        super.dispose();
        taperedLinesCheck = null;
        gradientColorCheck = null;
    }

    public void setFocus() {
        taperedLinesCheck.setFocus();
    }

}
