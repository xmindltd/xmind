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
package org.xmind.ui.browser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.browser.StatusTextEvent;
import org.eclipse.swt.browser.StatusTextListener;
import org.eclipse.swt.browser.TitleEvent;
import org.eclipse.swt.browser.TitleListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PartInitException;

/**
 * @author briansun
 */
public abstract class BrowserDialog extends Dialog {

    protected static final String COMMAND_PROTOCOL = "xmind://"; //$NON-NLS-1$

    protected static final String COMMAND_CANCEL = "cancel"; //$NON-NLS-1$

    protected static final String COMMAND_SKIP = "skip"; //$NON-NLS-1$

    protected static final String COMMAND_OPEN = "open"; //$NON-NLS-1$

    private class BrowserListener implements StatusTextListener, TitleListener,
            LocationListener {

        /**
         * @see org.eclipse.swt.browser.StatusTextListener#changed(org.eclipse.swt.browser.StatusTextEvent)
         */
        public void changed(StatusTextEvent event) {
            checkCommand(event.text);
        }

        /**
         * @see org.eclipse.swt.browser.TitleListener#changed(org.eclipse.swt.browser.TitleEvent)
         */
        public void changed(TitleEvent event) {
            String t = event.title;
            if (t != null && t.startsWith("xmind:")) //$NON-NLS-1$
                browser.getShell().setText(t.substring(6));
        }

        /**
         * @see org.eclipse.swt.browser.LocationListener#changed(org.eclipse.swt.browser.LocationEvent)
         */
        public void changed(LocationEvent event) {
        }

        /**
         * @see org.eclipse.swt.browser.LocationListener#changing(org.eclipse.swt.browser.LocationEvent)
         */
        public void changing(LocationEvent event) {
            if (checkCommand(event.location))
                event.doit = false;
        }

        private boolean checkCommand(String href) {
            if (href.startsWith(COMMAND_PROTOCOL)) {
                String commandLine = href.substring(COMMAND_PROTOCOL.length());
                return execCommandLine(commandLine);
            }
            return false;
        }

    }

    private Browser browser;

    private BrowserListener browserListener = new BrowserListener();

    /**
     * @param parentWindow
     */
    public BrowserDialog(Shell parent) {
        super(parent);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        try {
            browser = new Browser(parent, SWT.MOZILLA);
        } catch (SWTError e) {
            browser = new Browser(parent, SWT.NONE);
        }
        browser.setLayoutData(new GridData(GridData.FILL_BOTH));
        browser.addStatusTextListener(browserListener);
        browser.addTitleListener(browserListener);
        browser.addLocationListener(browserListener);

        String text = getInitialText();
        if (text != null)
            browser.setText(text);
        else {
            String url = getInitialURL();
            if (url != null)
                browser.setUrl(url);
        }

        if (Util.isMac())
            browser.refresh();
        return browser;
    }

    protected Browser getBrowser() {
        return browser;
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getInitialSize()
     */
    @Override
    protected Point getInitialSize() {
        return new Point(500, 325);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#getInitialLocation(org.eclipse.swt.graphics.Point)
     */
    @Override
    protected Point getInitialLocation(Point initialSize) {
        Rectangle r = Display.getCurrent().getClientArea();
        return new Point((r.width - initialSize.x) / 2,
                (r.height - initialSize.y) / 2);
    }

    /**
     * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        // use html buttons
    }

    protected String getInitialText() {
        return null;
    }

    protected String getInitialURL() {
        return null;
    }

    protected boolean execCommandLine(String commandLine) {
        String[] commands = commandLine.split("/"); //$NON-NLS-1$
        if (commands.length > 1) {
            String commandName = commands[0];
            String[] params = new String[commands.length - 1];
            System.arraycopy(commands, 1, params, 0, commands.length - 1);
            return execCommand(commandName, params);
        }
        return false;
    }

    protected boolean execCommand(String commandName, String[] params) {
        if (COMMAND_CANCEL.equals(commandName)) {
            return performCancel();
        } else if (COMMAND_SKIP.equals(commandName)) {
            return performSkip();
        } else if (COMMAND_OPEN.equals(commandName)) {
            return performOpen(params);
        }
        return false;
    }

    protected boolean performOpen(String[] params) {
        if (params.length > 0) {
            String url = params[0];
            try {
                url = URLDecoder.decode(url, "utf-8"); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e) {
            }
            try {
                BrowserSupport.getInstance().createBrowser().openURL(url);
                return true;
            } catch (PartInitException e) {
                // TODO handle this
            }
        }
        return false;
    }

    protected boolean performSkip() {
        setReturnCode(IDialogConstants.CLOSE_ID);
        return super.close();
    }

    protected boolean performCancel() {
        setReturnCode(CANCEL);
        return super.close();
    }

}