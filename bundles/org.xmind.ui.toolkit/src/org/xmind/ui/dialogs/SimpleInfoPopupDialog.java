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
package org.xmind.ui.dialogs;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.ui.resources.FontUtils;

public class SimpleInfoPopupDialog extends SmoothPopupDialog {

    private String infoText;

    private IAction leftAction;

    private IAction rightAction;

    private int iconId;

    private Image icon;

    private boolean closeOnAction = false;

    public SimpleInfoPopupDialog(Shell parent, String title, String infoText) {
        this(parent, title, infoText, 0, null, null);
    }

    public SimpleInfoPopupDialog(Shell parent, String title, String infoText,
            int iconId) {
        this(parent, title, infoText, iconId, null, null);
    }

    public SimpleInfoPopupDialog(Shell parent, String title, String infoText,
            int iconId, IAction leftAction, IAction rightAction) {
        super(parent, true, title);
        this.infoText = infoText;
        this.iconId = iconId;
        this.leftAction = leftAction;
        this.rightAction = rightAction;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout layout = ((GridLayout) composite.getLayout());
        layout.marginWidth = 5;
        layout.marginHeight = 5;

        boolean hasIcon = hasIcon();
        boolean hasInfo = hasInfo();

        if (hasIcon || hasInfo) {
            Composite infoArea = new Composite(composite, SWT.NO_FOCUS);
            int numColumns = hasIcon && hasInfo ? 2 : 1;
            GridLayout infoAreaLayout = new GridLayout(numColumns, false);
            infoAreaLayout.marginWidth = 0;
            infoAreaLayout.marginHeight = 0;
            infoArea.setLayout(infoAreaLayout);
            GridData infoAreaLayoutData = new GridData(GridData.FILL,
                    GridData.FILL, true, true);
            infoAreaLayoutData.widthHint = 180;
            infoAreaLayoutData.heightHint = 100;
            infoArea.setLayoutData(infoAreaLayoutData);

            if (hasIcon) {
                Label iconLabel = new Label(infoArea, SWT.CENTER);
                iconLabel.setImage(getIcon());
                iconLabel.setLayoutData(new GridData(GridData.FILL,
                        GridData.CENTER, !hasInfo, true));
            }

            if (hasInfo) {
                Label info = new Label(infoArea, SWT.WRAP | SWT.CENTER);
                info.setText(getInfoText());
                info.setFont(getInfoFont());
                info.setLayoutData(new GridData(GridData.FILL, GridData.CENTER,
                        true, true));
            }
        }

        boolean hasLeftAction = leftAction != null;
        boolean hasRightAction = rightAction != null;
        if (hasLeftAction || hasRightAction) {
            Composite actionBar = new Composite(composite, SWT.NO_FOCUS);
            int numColumns = hasLeftAction && hasRightAction ? 2 : 1;
            GridLayout infoAreaLayout = new GridLayout(numColumns, false);
            infoAreaLayout.marginWidth = 0;
            infoAreaLayout.marginHeight = 0;
            actionBar.setLayout(infoAreaLayout);
            actionBar.setLayoutData(new GridData(GridData.FILL, GridData.FILL,
                    true, !hasInfo && !hasIcon));

            if (hasLeftAction) {
                Control left = createHyperlink(actionBar, leftAction);
                GridData leftLayoutData = new GridData(SWT.BEGINNING, SWT.END,
                        true, !hasInfo && !hasIcon);
                left.setLayoutData(leftLayoutData);
            }

            if (hasRightAction) {
                Control right = createHyperlink(actionBar, rightAction);
                GridData rightLayoutData = new GridData(SWT.END, SWT.END, true,
                        !hasInfo && !hasIcon);
                right.setLayoutData(rightLayoutData);
            }
        }

        return composite;
    }

    protected Control createHyperlink(Composite parent, final IAction action) {
        Hyperlink hyperlink = new Hyperlink(parent, SWT.NONE);
        hyperlink.setText(getHyperlinkText(action));
        hyperlink.setUnderlined(true);
        hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
            public void linkActivated(HyperlinkEvent e) {
                openHyperlink(action);
                if (closeOnAction) {
                    close();
                }
            }
        });
        return hyperlink;
    }

    protected String getHyperlinkText(IAction action) {
        String text = action.getText();
        if (text != null) {
            text = Action.removeAcceleratorText(text);
            text = Action.removeMnemonics(text);
            return text;
        }
        return ""; //$NON-NLS-1$
    }

    public IAction getLeftAction() {
        return leftAction;
    }

    public IAction getRightAction() {
        return rightAction;
    }

    protected boolean hasHyperlink() {
        return leftAction != null;
    }

    protected void openHyperlink(final IAction action) {
        SafeRunner.run(new SafeRunnable() {
            public void run() throws Exception {
                action.run();
            }
        });
    }

    protected Font getInfoFont() {
        return FontUtils.getNewHeight(JFaceResources.DEFAULT_FONT,
                Util.isMac() ? 14 : 10);
    }

    public String getInfoText() {
        return infoText;
    }

    protected boolean hasInfo() {
        return infoText != null;
    }

    protected Image getIcon() {
        if (icon == null && iconId > 0) {
            icon = getShell().getDisplay().getSystemImage(iconId);
        }
        return icon;
    }

    protected boolean hasIcon() {
        return iconId > 0 && getIcon() != null;
    }

    /**
     * Turn on/off "close popup dialog on left/right action triggered" feature.
     * 
     * @param enabled
     */
    public void setCloseOnAction(boolean enabled) {
        this.closeOnAction = enabled;
    }

}