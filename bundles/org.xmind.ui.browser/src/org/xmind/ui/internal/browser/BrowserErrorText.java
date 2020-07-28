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
package org.xmind.ui.internal.browser;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.xmind.ui.browser.IBrowserViewerContainer;

public class BrowserErrorText {
    private String url;

    private FallbackScrolledComposite scomp;

    private Label title;

    private Label exTitle;

    private Label text;

    private Label sep;

    protected Link link;

    private BrowserViewer viewer;

    private Button button;

    private Text exception;

    private boolean expanded;

    private Throwable ex;

    class ReflowScrolledComposite extends FallbackScrolledComposite {
        public ReflowScrolledComposite(Composite parent, int style) {
            super(parent, style);
        }

        @Override
        public void reflow(boolean flushCache) {
            updateWidth(this);
            super.reflow(flushCache);
        }
    }

    public BrowserErrorText(Composite parent, BrowserViewer viewer, Throwable ex) {
        this.viewer = viewer;
        this.ex = ex;
        Color bg = parent.getDisplay()
                .getSystemColor(SWT.COLOR_LIST_BACKGROUND);
        scomp = new ReflowScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
        Composite client = new Composite(scomp, SWT.NULL);
        fillContent(client, bg);
        scomp.setContent(client);
        scomp.setBackground(bg);
    }

    private void fillContent(Composite parent, Color bg) {
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 10;
        parent.setLayout(layout);
        title = new Label(parent, SWT.WRAP);
        title.setText(BrowserMessages.BrowserText_EmbeddedBrowserUnavailableInfo);
        title.setFont(JFaceResources.getHeaderFont());
        title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        title.setBackground(bg);

        link = new Link(parent, SWT.WRAP);
        link.setText(""); //$NON-NLS-1$
        link.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        link
                .setToolTipText("<a href=\"open\">" + BrowserMessages.BrowserText_OpenFileLinkToolTip + "</a>"); //$NON-NLS-1$ //$NON-NLS-2$
        link.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BusyIndicator.showWhile(link.getDisplay(), new Runnable() {
                    public void run() {
                        doOpenExternal();
                    }
                });
            }
        });
        link.setBackground(bg);
        sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        exTitle = new Label(parent, SWT.NULL);
        exTitle.setBackground(bg);
        exTitle.setFont(JFaceResources.getBannerFont());
        exTitle.setText(BrowserMessages.BrowserText_ProblemDetailsTitle);
        exTitle.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text = new Label(parent, SWT.WRAP);
        text.setText(BrowserMessages.BrowserText_ProblemDetails);
        text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text.setBackground(bg);
        button = new Button(parent, SWT.PUSH);
        updateButtonText();
        button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                toggleException();
            }
        });
        exception = new Text(parent, SWT.MULTI);
        loadExceptionText();
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.exclude = true;
        exception.setLayoutData(gd);
    }

    private void loadExceptionText() {
        StringWriter swriter = new StringWriter();
        PrintWriter writer = new PrintWriter(swriter);
        writer.println(ex.getMessage());
        ex.printStackTrace(writer);
        writer.close();
        exception.setText(swriter.toString());
    }

    protected void toggleException() {
        expanded = !expanded;
        updateButtonText();
        GridData gd = (GridData) exception.getLayoutData();
        gd.exclude = !expanded;
        exception.setVisible(expanded);
        refresh();
    }

    private void updateButtonText() {
        if (expanded)
            button.setText(BrowserMessages.BrowserText_Details + "<<"); //$NON-NLS-1$
        else
            button.setText(BrowserMessages.BrowserText_Details + ">>"); //$NON-NLS-1$
    }

    protected void updateWidth(Composite parent) {
        Rectangle area = parent.getClientArea();
        updateWidth(title, area.width);
        updateWidth(text, area.width);
        updateWidth(sep, area.width);
        updateWidth(link, area.width);
        updateWidth(exTitle, area.width);
        updateWidth(exception, area.width);
    }

    private void updateWidth(Control c, int width) {
        GridData gd = (GridData) c.getLayoutData();
        if (gd != null)
            gd.widthHint = width - 10;
    }

    protected void doOpenExternal() {
        IBrowserViewerContainer container = viewer.getContainer();
        if (container != null)
            container.openInExternalBrowser(url);
    }

    public Control getControl() {
        return scomp;
    }

    public boolean setUrl(String url) {
        this.url = url;
        return true;
    }

    public void setFocus() {
        link.setFocus();
    }

    public String getUrl() {
        return url;
    }

    public void refresh() {
        scomp.reflow(true);
    }
}