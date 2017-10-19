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

package org.xmind.ui.tabfolder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;

/**
 * @author Frank Shaka
 * 
 */
public abstract class PageBookPage extends Page implements
        IPageChangedListener, IPageClosedListener {

    private IPageChangeProvider provider;

    private PageBook pageBook;

    private Object activeSourcePage;

    private Control defaultPage;

    private Map<Object, Page> pageMap = new HashMap<Object, Page>();

    private DelegatedSelectionProvider selectionProvider = new DelegatedSelectionProvider();

    private IPropertyChangeListener actionBarPropListener = new IPropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent event) {
            if (event.getProperty().equals(SubActionBars.P_ACTION_HANDLERS)) {
                Page page = getActivePage();
                if (page != null
                        && event.getSource() == page.getSite().getActionBars()) {
                    refreshGlobalActionHandlers();
                }
            }
        }
    };

    /**
     * 
     */
    public PageBookPage(IPageChangeProvider sourcePageProvider) {
        this.provider = sourcePageProvider;
    }

    /**
     * @return the provider
     */
    public IPageChangeProvider getSourcePageProvider() {
        return provider;
    }

    @Override
    public void init(IPageSite pageSite) {
        super.init(pageSite);
        pageSite.setSelectionProvider(selectionProvider);
    }

    protected Object getActiveSourcePage() {
        return activeSourcePage;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.part.Page#createControl(org.eclipse.swt.widgets.Composite
     * )
     */
    @Override
    public void createControl(Composite parent) {
        pageBook = new PageBook(parent, SWT.NONE);
        provider.addPageChangedListener(this);
        Object page = provider.getSelectedPage();
        if (page != null) {
            setActivePage(page);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.Page#getControl()
     */
    @Override
    public Control getControl() {
        return pageBook;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.part.Page#setFocus()
     */
    @Override
    public void setFocus() {
        pageBook.setFocus();
    }

    @Override
    public void dispose() {
        provider.removePageChangedListener(this);

        Object[] pages = pageMap.entrySet().toArray();
        for (int i = 0; i < pages.length; i++) {
            Entry entry = (Entry) pages[i];
            Object sourcePage = entry.getKey();
            Page nestedPge = (Page) entry.getValue();
            disposeNestedPage(nestedPge, sourcePage);
        }
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.tabfolder.IPageClosedListener#pageClosed(java.lang.Object )
     */
    public void pageClosed(Object page) {
        Page nestedPage = pageMap.remove(page);
        if (nestedPage == null)
            return;
        disposeNestedPage(nestedPage, page);
    }

    protected void disposeNestedPage(Page nestedPage, Object sourcePage) {
        IPageSite site = nestedPage.getSite();
        Control control = nestedPage.getControl();
        if (control != null && !control.isDisposed()) {
            control.dispose();
        }
        nestedPage.dispose();
        ((SubActionBars) site.getActionBars())
                .removePropertyChangeListener(actionBarPropListener);
        ((NestedPageSite) site).dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jface.dialogs.IPageChangedListener#pageChanged(org.eclipse
     * .jface.dialogs.PageChangedEvent)
     */
    public void pageChanged(PageChangedEvent event) {
        setActivePage(event.getSelectedPage());
    }

    private void setActivePage(Object sourcePage) {
        if (pageBook == null || pageBook.isDisposed())
            return;

        this.activeSourcePage = sourcePage;
        if (sourcePage != null) {
            showNestedPage(sourcePage);
        } else {
            showDefaultPage();
        }
        refreshGlobalActionHandlers();

        /*
         * The first update is for parent PageBookView to collect global action
         * handlers.
         */
        getSite().getActionBars().updateActionBars();
        /* The seconds update is for RetargetActions to refresh their handlers. */
        getSite().getActionBars().updateActionBars();
    }

    protected void showNestedPage(Object sourcePage) {
        Page activePage = pageMap.get(sourcePage);
        if (activePage == null) {
            activePage = createNestedPage(sourcePage);
            pageMap.put(sourcePage, activePage);
        }
        Control pageControl = activePage.getControl();
        if (pageControl != null && !pageControl.isDisposed()) {
            pageBook.showPage(pageControl);
            selectionProvider.setDelegate(activePage.getSite()
                    .getSelectionProvider());
        }
    }

    private void showDefaultPage() {
        if (defaultPage == null || defaultPage.isDisposed()) {
            defaultPage = createDefaultPage(pageBook);
        }
        pageBook.showPage(defaultPage);
    }

    protected abstract Control createDefaultPage(Composite parent);

    protected Page createNestedPage(Object sourcePage) {
        Page page = doCreateNestedPage(sourcePage);
        NestedPageSite site = new NestedPageSite(getSite());
        page.init(site);
        if (pageBook != null && !pageBook.isDisposed()) {
            page.createControl(pageBook);
        }
        ((SubActionBars) site.getActionBars())
                .addPropertyChangeListener(actionBarPropListener);
        return page;
    }

    protected abstract Page doCreateNestedPage(Object sourcePage);

    protected Page getActivePage() {
        return activeSourcePage == null ? null : pageMap.get(activeSourcePage);
    }

    protected void refreshGlobalActionHandlers() {
        // Clear old actions.
        IActionBars bars = getSite().getActionBars();
        bars.clearGlobalActionHandlers();

        // Set new actions from active nested page.
        Page page = getActivePage();
        if (page != null) {
            Map newActionHandlers = ((SubActionBars) page.getSite()
                    .getActionBars()).getGlobalActionHandlers();
            if (newActionHandlers != null) {
                Set keys = newActionHandlers.entrySet();
                Iterator iter = keys.iterator();
                while (iter.hasNext()) {
                    Map.Entry entry = (Map.Entry) iter.next();
                    bars.setGlobalActionHandler((String) entry.getKey(),
                            (IAction) entry.getValue());
                }
            }
        }
    }

}
