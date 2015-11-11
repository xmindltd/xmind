/*
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and above are dual-licensed
 * under the Eclipse Public License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors: XMind Ltd. - initial API and implementation
 */
package net.xmind.signin.internal;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.OpenWindowListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.json.JSONException;
import org.xmind.core.net.IDataStore;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.browser.IBrowser;
import org.xmind.ui.browser.IBrowserSupport;
import org.xmind.ui.internal.browser.InternalBrowser;
import org.xmind.ui.internal.browser.InternalBrowserEditor;
import org.xmind.ui.internal.browser.InternalBrowserView;

import net.xmind.signin.ISignInDialogExtension;

/**
 * @author briansun
 * @deprecated {@link SignInDialog3} uses local widgets instead of web page.
 */
public class SignInDialog extends Dialog
        implements StatusTextListener, OpenWindowListener {

    //private static final String SIGN_IN_URL = "http://www.xmind.net/xmind/signin/"; //$NON-NLS-1$
    private static final String SIGN_IN_URL = "http://www.xmind.net/xmind/go?r=http%3A%2F%2Fwww.xmind.net%2Fxmind%2Fsignin2%2F"; //$NON-NLS-1$

    private Browser browser;

    private IDataStore data = null;

    private String message;

    private ISignInDialogExtension extension;

    private boolean infoRetrieved = false;

    /**
     * @param parentWindow
     */
    public SignInDialog(Shell parent) {
        this(parent, null, null);
    }

    public SignInDialog(Shell parent, String message,
            ISignInDialogExtension extension) {
        super(parent);
        setBlockOnOpen(true);
        setShellStyle(
                SWT.TITLE | SWT.CLOSE | SWT.RESIZE | SWT.APPLICATION_MODAL);
        this.message = message;
        this.extension = extension;
    }

    public String getUserID() {
        return data == null ? null : data.getString(XMindNetAccount.USER);
    }

    public String getToken() {
        return data == null ? null : data.getString(XMindNetAccount.TOKEN);
    }

    public boolean shouldRemember() {
        return data == null ? false : data.getBoolean(SignInJob.REMEMBER);
    }

    public IDataStore getData() {
        return data;
    }

    protected Browser getBrowser() {
        return browser;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        if (message != null) {
            createMessageArea(composite);
            createSeparator(composite);
        }
        createBrowser(composite);

        if (extension != null) {
            createExtension(composite);
        }
        return composite;
    }

    private void createExtension(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        extension.contributeToOptions(this, composite);
    }

    private void createBrowser(Composite parent) {
//        try {
//            browser = new Browser(parent, SWT.MOZILLA);
//        } catch (SWTError e) {
//        }
        browser = new Browser(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        gridData.widthHint = 560;
        gridData.heightHint = "gtk".equals(SWT.getPlatform()) ? 278 : 252; //$NON-NLS-1$
        gridData.minimumWidth = 560;
        gridData.minimumHeight = 242;
        browser.setLayoutData(gridData);
        browser.addStatusTextListener(this);
        browser.addOpenWindowListener(this);

        browser.setUrl(SIGN_IN_URL);

        if (Util.isMac())
            browser.refresh();

        browser.setFocus();
    }

    private void createMessageArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = 540;
        gridData.heightHint = SWT.DEFAULT;
        composite.setLayoutData(gridData);

        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = 15;
        gridLayout.marginHeight = 10;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 10;
        composite.setLayout(gridLayout);
        composite.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        createMessageIcon(composite);
        createMessageLabel(composite);
    }

    private void createSeparator(Composite parent) {
        Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        sep.setLayoutData(gridData);
    }

    private void createMessageIcon(Composite parent) {
        Label icon = new Label(parent, SWT.NONE);
        GridData gridData = new GridData(SWT.BEGINNING, SWT.CENTER, false,
                false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        icon.setLayoutData(gridData);
        icon.setBackground(parent.getBackground());
        icon.setImage(parent.getDisplay().getSystemImage(SWT.ICON_INFORMATION));
    }

    private void createMessageLabel(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        label.setLayoutData(gridData);
        label.setBackground(parent.getBackground());
        label.setText(message);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // use html buttons
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(Messages.SignInDialog_title);
    }

    protected Point getInitialLocation(Point initialSize) {
        return super.getInitialLocation(initialSize);
//        Rectangle area = Display.getCurrent().getClientArea();
//        return new Point(area.x + (area.width - initialSize.x) / 2, area.y
//                + (area.height - initialSize.y) / 2);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
     */
    @Override
    protected Point getInitialSize() {
        return super.getInitialSize();//new Point(540, 365);
    }

    protected IDialogSettings getDialogBoundsSettings() {
        return null;//Activator.getDefault().getDialogSettings();
    }

    /**
     * @see org.eclipse.swt.browser.StatusTextListener#changed(org.eclipse.swt.browser.StatusTextEvent)
     */
    public void changed(StatusTextEvent event) {
        if (infoRetrieved)
            return;
        infoRetrieved = checkCommand(event.text);
    }

    private boolean checkCommand(String text) {
        XMindNetCommand command = XMindNetCommand.createFromText(text);
        if (command == null)
            return false;
        if ("200".equals(command.getCode())) { //$NON-NLS-1$
            return executeJSON(command.getContent());
        }
        return false;
    }

    /**
     * @param json2
     * @return
     */
    private boolean executeJSON(IDataStore json) {
        if (json == null)
            return false;
        try {
            return setUserNameAndToken(json);
        } catch (JSONException e) {
        }
        return false;
    }

    private boolean setUserNameAndToken(IDataStore json) throws JSONException {
        String userID = json.getString(XMindNetAccount.USER);
        String token = json.getString(XMindNetAccount.TOKEN);
        if (userID == null || token == null || "".equals(userID) //$NON-NLS-1$
                || "".equals(token)) //$NON-NLS-1$
            return false;

        this.data = json;
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                setReturnCode(OK);
                close();
            }
        });
        return true;
    }

    public void open(WindowEvent event) {
        IBrowser browser = BrowserSupport.getInstance()
                .createBrowser(IBrowserSupport.AS_EDITOR);
        try {
            browser.openURL(""); //$NON-NLS-1$
            if (browser instanceof InternalBrowser) {
                IWorkbenchPart part = ((InternalBrowser) browser).getPart();
                if (part instanceof InternalBrowserEditor) {
                    event.browser = ((InternalBrowserEditor) part).getViewer()
                            .getBrowser();
                } else if (part instanceof InternalBrowserView) {
                    event.browser = ((InternalBrowserView) part).getViewer()
                            .getBrowser();
                }
            }
        } catch (PartInitException e) {
            Activator.log(e);
        }

        event.display.asyncExec(new Runnable() {
            public void run() {
                setReturnCode(CANCEL);
                close();
            }
        });
    }

}