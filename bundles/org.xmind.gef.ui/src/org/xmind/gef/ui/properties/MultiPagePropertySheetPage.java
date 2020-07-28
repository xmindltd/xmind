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
package org.xmind.gef.ui.properties;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.xmind.gef.ui.editor.IGraphicalEditor;

public abstract class MultiPagePropertySheetPage extends Page
        implements ISelectionListener, IPropertyPartContainer {

    private IGraphicalEditor editor;

    private Map<String, IPropertyPagePart> pages = new HashMap<String, IPropertyPagePart>();

    private IPropertyPagePart currentPage;

    private Composite composite;

    private PageBook pageBook;

    private Label titleBar;

    private Control titleSeparator;

    private Control defaultPage;

    public MultiPagePropertySheetPage(IGraphicalEditor editor) {
        this.editor = editor;
    }

    public void init(IPageSite pageSite) {
        super.init(pageSite);
        pageSite.getPage().addPostSelectionListener(this);
        for (IPropertyPagePart page : pages.values()) {
            page.init(this, getContributedEditor());
        }
    }

    protected void addPage(String pageId, IPropertyPagePart page) {
        pages.put(pageId, page);
        page.init(this, editor);
    }

    protected void removePage(String pageId, IPropertyPagePart page) {
        if (pages.remove(pageId) == page) {
            Control control = page.getControl();
            if (control != null)
                control.dispose();
            page.dispose();
        }
    }

    public IGraphicalEditor getContributedEditor() {
        return editor;
    }

    public void createControl(Composite parent) {
        composite = new Composite(parent, SWT.NO_FOCUS);
        GridLayout layout = new GridLayout(1, true);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        titleBar = new Label(composite, SWT.NONE);
        titleBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        titleSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        titleSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        pageBook = new PageBook(composite, SWT.NONE);
        pageBook.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private void showPage(IPropertyPagePart page) {
        if (pageBook == null || pageBook.isDisposed())
            return;

        if (page == null) {
            pageBook.showPage(getDefaultPage());
            return;
        }

        Control c = page.getControl();
        if (c == null || c.isDisposed()) {
            createPageControl(page);
            c = page.getControl();
        }
        if (c != null && !c.isDisposed()) {
            pageBook.showPage(c);
        }
    }

    protected void createPageControl(IPropertyPagePart page) {
        page.createControl(pageBook);
    }

    public Control getControl() {
        return composite;
    }

    public void setFocus() {
        if (currentPage != null) {
            currentPage.setFocus();
        } else if (pageBook != null && !pageBook.isDisposed()) {
            pageBook.setFocus();
        }
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if (part != editor)
            return;

        changeCurrentPage(selection);
        if (pageBook != null && !pageBook.isDisposed()) {
            showPage(currentPage);
            updateTitleBar();
            refresh();
        }
    }

    private void changeCurrentPage(ISelection selection) {
        IPropertyPagePart page = null;
        String pageId = getPageId(selection);
        if (pageId != null) {
            page = pages.get(pageId);
            if (page == null) {
                page = createPage(pageId, selection);
                if (page != null) {
                    addPage(pageId, page);
                }
            }
        }
        if (page != currentPage) {
            if (currentPage != null) {
                currentPage.setSelection(null);
            }
            currentPage = page;
        }
        if (currentPage != null) {
            currentPage.setSelection(selection);
        }
    }

    protected abstract IPropertyPagePart createPage(String pageId,
            ISelection selection);

    private Control getDefaultPage() {
        if (defaultPage == null || defaultPage.isDisposed()) {
            defaultPage = createDefaultPage(pageBook);
        }
        return defaultPage;
    }

    protected Control createDefaultPage(Composite parent) {
        Label label = new Label(parent, SWT.WRAP);
        label.setText(Messages.propertiesNotAvailable);
        return label;
    }

    public void updateSectionTitle(IPropertySectionPart section) {
        IPropertyPagePart page = findPage(section);
        if (page != null) {
            page.updateSectionTitle(section);
        }
    }

    private IPropertyPagePart findPage(IPropertySectionPart section) {
        for (IPropertyPagePart page : pages.values()) {
            if (page.getSections().contains(section))
                return page;
        }
        return null;
    }

    private void updateTitleBar() {
        if (titleBar == null || titleBar.isDisposed())
            return;
        String title = currentPage == null ? null : currentPage.getTitle();
        titleBar.setText(title == null ? "" : title); //$NON-NLS-1$
        setTitleVisible(title != null);
    }

    private void setTitleVisible(boolean visible) {
        if (titleBar == null || titleBar.isDisposed())
            return;
        if (titleBar.getVisible() == visible)
            return;
        titleBar.setVisible(visible);
        ((GridData) titleBar.getLayoutData()).exclude = !visible;
        titleSeparator.setVisible(visible);
        ((GridData) titleSeparator.getLayoutData()).exclude = !visible;
        composite.layout();
    }

    protected abstract String getPageId(ISelection selection);

    public void dispose() {
        getSite().getPage().removePostSelectionListener(this);
        for (IPropertyPagePart page : pages.values()) {
            page.dispose();
        }
        if (composite != null) {
            composite.dispose();
            composite = null;
        }
        pageBook = null;
        defaultPage = null;
        titleBar = null;
        titleSeparator = null;
        super.dispose();
    }

    public void refresh() {
        for (IPropertyPagePart page : pages.values()) {
            page.refresh();
        }
    }

    public IPageSite getContainerSite() {
        return getSite();
    }

}