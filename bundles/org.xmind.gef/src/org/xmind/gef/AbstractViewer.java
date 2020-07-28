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
package org.xmind.gef;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Widget;
import org.xmind.gef.acc.AccessibleRegistry;
import org.xmind.gef.acc.IAccessible;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IPartFactory;
import org.xmind.gef.part.IRootPart;
import org.xmind.gef.part.PartRegistry;
import org.xmind.gef.service.IViewerService;
import org.xmind.gef.service.IViewerService2;
import org.xmind.gef.util.EventListenerSupport;
import org.xmind.gef.util.IEventDispatcher;
import org.xmind.gef.util.Properties;

/**
 * @author Brian Sun
 */
public abstract class AbstractViewer extends Viewer implements IViewer {

    private static final List<IPart> EMPTY_PART_SELECTION = Collections
            .emptyList();

    private static final String PRE_SELECTION_CHANGED_KEY = "preSelectionChanged"; //$NON-NLS-1$

    private static final String POST_SELECTION_CHANGED_KEY = "postSelectionChanged"; //$NON-NLS-1$

    private static final String FOCUSED_PART_CHANGED_KEY = "focusedPartChanged"; //$NON-NLS-1$

    protected class SelectionSupport implements ISelectionSupport {

        private ArrayList<IPart> partSelection = null;

        private ITextSelection textSelection = null;

        public List<IPart> getPartSelection() {
            return partSelection == null ? EMPTY_PART_SELECTION : partSelection;
        }

        protected boolean trimValidParts() {
            if (partSelection == null || partSelection.isEmpty())
                return false;
            boolean changed = true;
            Iterator<IPart> it = partSelection.iterator();
            while (it.hasNext()) {
                IPart p = it.next();
                if (!isPartSelected(p)) {
                    changed = true;
                    it.remove();
                }
            }
            return changed;
        }

        protected boolean isPartSelected(IPart p) {
            return p != null && p.getStatus().isActive()
                    && p.hasRole(GEF.ROLE_SELECTABLE)
                    && p.getStatus().isSelected();
        }

        public void appendSelection(Object element) {
            boolean changed = trimValidParts();
            changed |= internalAppendSelection(findSelectablePart(element));
            if (changed) {
                partSelectionChanged(getPartSelection(), true);
                notifyViewerSelectionChanged();
            }
        }

        public void appendSelection(List<?> elements) {
            boolean changed = trimValidParts();
            changed |= internalAppendSelection(elements);
            if (changed) {
                partSelectionChanged(getPartSelection(), true);
                notifyViewerSelectionChanged();
            }
        }

        protected boolean internalAppendSelection(List<?> elements) {
            boolean changed = false;
            for (Object element : elements.toArray()) {
                changed |= internalAppendSelection(findSelectablePart(element));
            }
            return changed;
        }

        protected boolean internalAppendSelection(IPart p) {
            if (p == null)
                return false;
            if (partSelection != null && partSelection.contains(p))
                return false;

            if (partSelection == null) {
                partSelection = new ArrayList<IPart>();
            } else if (partSelection.size() >= 1) {
                int selectionConstraint = getSelectionConstraint();
                if ((selectionConstraint & GEF.SEL_MULTI) == 0)
                    return false;
            }
            boolean appended = partSelection.add(p);
            if (appended) {
                setSelected(p);
            }
            return appended;
        }

        protected void setSelected(IPart p) {
            p.getStatus().select();
        }

        public void deselect(Object element) {
            boolean changed = trimValidParts();
            changed |= internalDeselect(findSelectablePart(element), false);
            if (changed) {
                partSelectionChanged(getPartSelection(), true);
                notifyViewerSelectionChanged();
            }
        }

        protected boolean internalDeselect(IPart p, boolean force) {
            if (p == null)
                return false;
            if (partSelection == null || !partSelection.contains(p))
                return false;

            if (!force) {
                if (partSelection.size() <= 1) {
                    int selectionConstraint = getSelectionConstraint();
                    if ((selectionConstraint & GEF.SEL_EMPTY) == 0) {
                        return false;
                    }
                }
            }
            boolean deselected = partSelection.remove(p);
            if (deselected) {
                setDeselected(p);
            }
            return deselected;
        }

        protected void setDeselected(IPart p) {
            p.getStatus().deSelect();
        }

        public void deselectAll(List<?> elements) {
            boolean changed = trimValidParts();
            changed |= internalDeselectAll(elements, true, false);
            if (changed) {
                partSelectionChanged(getPartSelection(), true);
                notifyViewerSelectionChanged();
            }
        }

        protected boolean internalDeselectAll(List<?> elements,
                boolean toCheckSelectable, boolean force) {
            boolean changed = false;
            for (Object element : elements.toArray()) {
                IPart part = findPart(element);
                if (toCheckSelectable) {
                    part = findSelectablePart(element);
                }
                changed |= internalDeselect(part, force);
            }
            return changed;
        }

        public void deselectAll() {
            boolean changed = trimValidParts();
            changed |= internalDeselectAll(false);
            if (changed) {
                partSelectionChanged(getPartSelection(), true);
                notifyViewerSelectionChanged();
            }
        }

        protected boolean internalDeselectAll(boolean force) {
            if (partSelection == null || partSelection.isEmpty())
                return false;
            return internalDeselectAll(partSelection, false, force);
        }

        public ISelection getModelSelection() {
            if (textSelection != null)
                return textSelection;
            if (partSelection == null)
                return StructuredSelection.EMPTY;
            if (usePartsInSelection()) {
                return new StructuredSelection(partSelection);
            }
            return new StructuredSelection(getModels(partSelection));
        }

        protected List<Object> getModels(List<IPart> parts) {
            ArrayList<Object> list = new ArrayList<Object>(parts.size());
            for (IPart p : parts) {
                Object model = getModel(p);
                if (model != null && !list.contains(model))
                    list.add(model);
            }
            return list;
        }

        protected Object getModel(IPart p) {
            if (p == null)
                return null;
            return p.getModel();
        }

        protected boolean usePartsInSelection() {
            return false;
        }

        public void selectAll(List<?> elements) {
            boolean changed = trimValidParts();
            changed |= internalSelectAll(elements);
            if (changed) {
                partSelectionChanged(getPartSelection(), true);
                notifyViewerSelectionChanged();
            }
        }

        protected boolean internalSelectAll(List<?> elements) {
            boolean changed = internalDeselectAll(!elements.isEmpty());
            changed |= internalAppendSelection(elements);
            return changed;
        }

        public void selectAll() {
            boolean changed = trimValidParts();
            IRootPart rootPart = getRootPart();
            if (rootPart != null) {
                List<IPart> toSelect = collectAllSelectableParts(rootPart,
                        new ArrayList<IPart>());
                changed |= internalSelectAll(toSelect);
            }
            if (changed) {
                partSelectionChanged(getPartSelection(), true);
                notifyViewerSelectionChanged();
            }
        }

        protected List<IPart> collectAllSelectableParts(IPart parent,
                List<IPart> toReturn) {
            if (parent != null) {
                for (IPart p : parent.getChildren()) {
                    if (isSelectable(p)) {
                        toReturn.add(p);
                        collectAllSelectableParts(p, toReturn);
                    }
                }
            }
            return toReturn;
        }

        public void selectSingle(Object element) {
            boolean changed = trimValidParts();
            changed |= internalDeselectAll(true);
            changed |= internalAppendSelection(findSelectablePart(element));
            if (changed) {
                partSelectionChanged(getPartSelection(), true);
                notifyViewerSelectionChanged();
            }
        }

        public void setSelection(ISelection selection, boolean reveal) {
            boolean changed = trimValidParts();
            if (selection instanceof ITextSelection) {
                ITextSelection newTextSelection = (ITextSelection) selection;
                changed |= internalSetTextSelection(newTextSelection);
                if (changed) {
                    textSelectionChanged(newTextSelection, reveal);
                }
            } else if (selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection) selection;
                List<IPart> toSelect = collectAllSelectableParts(ss.toArray(),
                        new ArrayList<IPart>(), reveal);
                changed |= internalSelectAll(toSelect);
                if (changed) {
                    partSelectionChanged(getPartSelection(), reveal);
                }
            }
            if (changed) {
                notifyViewerSelectionChanged();
            }
        }

        protected List<IPart> collectAllSelectableParts(Object[] elements,
                List<IPart> toReturn, boolean reveal) {
            for (Object element : elements) {
                IPart p = findSelectablePart(element);
                if (p != null && !toReturn.contains(p)) {
                    toReturn.add(p);
                }
            }
            return toReturn;
        }

        protected void partSelectionChanged(List<? extends IPart> parts,
                boolean reveal) {
            if (reveal) {
                reveal(parts.toArray());
            }
        }

        protected boolean internalSetTextSelection(
                ITextSelection newTextSelection) {
            if (newTextSelection == this.textSelection
                    || (newTextSelection != null
                            && newTextSelection.equals(this.textSelection)))
                return false;
            this.textSelection = newTextSelection;
            return true;
        }

        protected void textSelectionChanged(ITextSelection newTextSelection,
                boolean reveal) {
            // subclass may implement
        }

        public IPart findSelectablePart(Object element) {
            IPart p = findPart(element);
            if (p == null || !isSelectable(p))
                return null;
            return p;
        }

        public boolean isSelectable(IPart p) {
            return p != null && p.getStatus().isActive()
                    && p.hasRole(GEF.ROLE_SELECTABLE);
        }

        protected void notifyViewerSelectionChanged() {
            fireSelectionChanged(new SelectionChangedEvent(AbstractViewer.this,
                    getModelSelection()));
            getControl().getAccessible().selectionChanged();
            firePostSelectionChanged();
        }

        public void refresh() {
            if (textSelection != null) {
                textSelectionChanged(textSelection, true);
                return;
            }

            if (partSelection != null) {
                refreshPartSelection(getRootPart(), partSelection);
            }
        }

        protected void refreshPartSelection(IPart parent,
                List<? extends IPart> selectedParts) {
            if (parent == null)
                return;
            for (IPart p : parent.getChildren()) {
                if (selectedParts.contains(p)) {
                    setSelected(p);
                } else {
                    setDeselected(p);
                }
                refreshPartSelection(p, selectedParts);
            }
        }

        protected int getSelectionConstraint() {
            Object value = getProperties().get(GEF.SelectionConstraint);
            if (value instanceof Integer)
                return ((Integer) value).intValue();
            return GEF.SEL_DEFAULT;
        }

    }

    private Control control = null;

    private IRootPart rootPart = null;

    private IPartFactory partFactory = null;

    private PartRegistry partRegistry = null;

    private EditDomain domain = null;

    private Object input = null;

    private List<ViewerFilter> filters = null;

    private ViewerSorter sorter = null;

    private ViewerComparator comparator = null;

    private ISelectionSupport selectionSupport = null;

    private Properties properties = null;

    private IDndSupport dndSupport = null;

    private AccessibleRegistry accRegistry = null;

    private IPart preSelected = null;

    private IPart focused = null;

    private EventListenerSupport listenerSupport = new EventListenerSupport();

    private Map<Class<? extends IViewerService>, IViewerService> serviceRegistry = null;

    private IPartSearchCondition partSearchCondition = null;

    private boolean postSelectionChangedEventScheduled = false;

    protected AbstractViewer() {
    }

    @SuppressWarnings("unchecked")
    public <T> T getAdapter(Class<T> adapter) {
        if (Control.class.equals(adapter) || Widget.class.equals(adapter))
            return adapter.cast(getControl());
        if (IRootPart.class.equals(adapter))
            return adapter.cast(getRootPart());
        if (IPartFactory.class.equals(adapter))
            return adapter.cast(getPartFactory());
        if (PartRegistry.class.equals(adapter))
            return adapter.cast(getPartRegistry());
        if (EditDomain.class.equals(adapter))
            return adapter.cast(getEditDomain());
        if (ISelectionSupport.class.equals(adapter))
            return adapter.cast(getSelectionSupport());
        if (IDndSupport.class.equals(adapter))
            return adapter.cast(getDndSupport());
        if (Properties.class.equals(adapter))
            return adapter.cast(getProperties());
        return null;
    }

    public Control createControl(Composite parent) {
        return createControl(parent, SWT.NONE);
    }

    public Control createControl(Composite parent, int style) {
        control = internalCreateControl(parent, style);
        Assert.isNotNull(control);
        hookControl(control);
        return control;
    }

    protected abstract Control internalCreateControl(Composite parent,
            int style);

    protected void hookControl(Control control) {
        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose(e);
            }
        });
        if (serviceRegistry != null) {
            for (IViewerService service : serviceRegistry.values()) {
                service.setControl(control);
            }
        }
        IRootPart rootPart = getRootPart();
        if (rootPart != null) {
            rootPart.getStatus().activate();
        }
    }

    public Control getControl() {
        return control;
    }

    public IRootPart getRootPart() {
        return rootPart;
    }

    public void setRootPart(IRootPart rootPart) {
        Assert.isNotNull(rootPart);
        IRootPart oldRootPart = this.rootPart;
        if (oldRootPart != null) {
            oldRootPart.getStatus().deactivate();
            oldRootPart.setViewer(null);
        }
        this.rootPart = rootPart;
        rootPart.setViewer(this);
        if (getControl() != null && !getControl().isDisposed()) {
            rootPart.getStatus().activate();
        }
        //inputChanged(getInput(), getInput());
    }

    public IPartFactory getPartFactory() {
        return partFactory;
    }

    public void setPartFactory(IPartFactory partFactory) {
        if (partFactory == this.partFactory)
            return;
        Assert.isNotNull(partFactory);
        this.partFactory = partFactory;
        inputChanged(getInput(), getInput());
    }

    public EditDomain getEditDomain() {
        return domain;
    }

    public void setEditDomain(EditDomain editDomain) {
        this.domain = editDomain;
    }

    public Object getInput() {
        return input;
    }

    public void setInput(Object input) {
        Object oldInput = this.input;
        this.input = input;
        inputChanged(input, oldInput);
    }

    protected void inputChanged(Object input, Object oldInput) {
        Map<IViewerService, Object> preservedDataList = new HashMap<IViewerService, Object>();
        List<IViewerService> activeServices = getActiveServices();
        if (activeServices != null) {
            for (IViewerService service : activeServices) {
                if (service instanceof IViewerService2) {
                    preservedDataList.put(service,
                            ((IViewerService2) service).preserveData());
                }
                service.setActive(false);
            }
        }
        super.inputChanged(input, oldInput);
        contentsChanged(input, oldInput);
        if (serviceRegistry != null) {
            for (IViewerService service : serviceRegistry.values()) {
                service.inputChanged(oldInput, input);
            }
        }
        if (activeServices != null) {
            for (IViewerService service : activeServices) {
                service.setActive(true);
                if (service instanceof IViewerService2) {
                    ((IViewerService2) service)
                            .restoreData(preservedDataList.get(service));
                }
            }
        }
        fireInputChanged(input, oldInput);
    }

    protected void contentsChanged(Object input, Object oldInput) {
        IRootPart rootPart = getRootPart();
        if (rootPart != null) {
            rootPart.setModel(input);
            rootPart.setContents(createContents(rootPart, input));
        }
    }

    protected IPart createContents(IRootPart root, Object input) {
        if (input == null || getPartFactory() == null)
            return null;
        return getPartFactory().createPart(root, input);
    }

    public boolean setFocus() {
        if (getControl() == null || getControl().isDisposed())
            return false;
        return getControl().setFocus();
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.IViewer#setCursor(org.eclipse.swt.graphics.Cursor)
     */
    public void setCursor(Cursor cursor) {
        if (getControl() == null || getControl().isDisposed())
            return;
        getControl().setCursor(cursor);
    }

    protected void handleDispose(DisposeEvent e) {
        if (serviceRegistry != null) {
            for (IViewerService service : serviceRegistry.values()) {
                service.dispose();
            }
            serviceRegistry.clear();
            serviceRegistry = null;
        }
        IRootPart rootPart = getRootPart();
        if (rootPart != null) {
            rootPart.getStatus().deactivate();
        }
    }

    public void addFilter(ViewerFilter filter) {
        if (filter == null)
            return;
        if (filters != null && filters.contains(filter))
            return;
        if (filters == null)
            filters = new ArrayList<ViewerFilter>();
        filters.add(filter);
        refresh();
    }

    public ViewerFilter[] getFilters() {
        return filters == null ? new ViewerFilter[0]
                : filters.toArray(new ViewerFilter[filters.size()]);
    }

    public ViewerSorter getSorter() {
        return sorter;
    }

    public ViewerComparator getComparator() {
        return comparator;
    }

    public void removeFilter(ViewerFilter filter) {
        if (filter == null || filters == null || filters.isEmpty())
            return;
        filters.remove(filter);
        refresh();
    }

    public void setFilters(ViewerFilter[] filters) {
        if (isFiltersEqual(filters, getFilters()))
            return;
        if (filters == null || filters.length == 0)
            this.filters = null;
        else
            this.filters = new ArrayList<ViewerFilter>(Arrays.asList(filters));
        refresh();
    }

    protected boolean isFiltersEqual(ViewerFilter[] fs1, ViewerFilter[] fs2) {
        if (fs1 == null)
            return fs2 == null || fs2.length == 0;
        if (fs2 == null)
            return fs1.length == 0;
        return Arrays.equals(fs1, fs2);
    }

    public void setSorter(ViewerSorter sorter) {
        if (sorter == this.sorter
                || (sorter != null && sorter.equals(this.sorter)))
            return;
        this.sorter = sorter;
        refresh();
    }

    public void setComparator(ViewerComparator comparator) {
        if (comparator == this.comparator
                || (comparator != null && comparator.equals(this.comparator)))
            return;
        this.comparator = comparator;
        refresh();
    }

    public ISelectionSupport getSelectionSupport() {
        if (selectionSupport == null)
            selectionSupport = createSelectionSupport();
        return selectionSupport;
    }

    protected ISelectionSupport createSelectionSupport() {
        return new SelectionSupport();
    }

    public ISelection getSelection() {
        return getSelectionSupport().getModelSelection();
    }

    public void setSelection(ISelection selection, boolean reveal) {
        getSelectionSupport().setSelection(selection, reveal);
    }

    public void reveal(Object[] elements) {
        if (elements == null || elements.length == 0)
            return;

        ISelectionSupport sd = getSelectionSupport();
        if (sd != null) {
            List<IPart> parts = new ArrayList<IPart>();
            for (Object element : elements) {
                IPart p = sd.findSelectablePart(element);
                if (p != null && !parts.contains(p))
                    parts.add(p);
            }
            if (!parts.isEmpty()) {
                revealParts(parts);
            }
        }
    }

    protected void revealParts(List<? extends IPart> parts) {
    }

    /**
     * @see org.xmind.gef.IViewer#refresh()
     */
    public void refresh() {
        IRootPart rootPart = getRootPart();
        if (rootPart != null && rootPart.getStatus().isActive())
            treeRefresh(rootPart);
    }

    protected void treeRefresh(IPart parent) {
        for (IPart child : parent.getChildren()) {
            child.refresh();
            treeRefresh(child);
        }
    }

    public void updateToolTip() {
    }

    /**
     * @return the partRegistry
     */
    public PartRegistry getPartRegistry() {
        if (partRegistry == null)
            partRegistry = new PartRegistry();
        return partRegistry;
    }

    public IPart findPart(Object element) {
        if (element instanceof IPart)
            return (IPart) element;
        if (partRegistry == null)
            return null;
        return partRegistry.getPartByModel(element);
    }

    public IPart findPart(int x, int y) {
        return null;
    }

    /*
     * (non-Javadoc)
     * @seeorg.xmind.gef.IViewer#setPartSearchCondition(org.xmind.gef.IViewer.
     * IPartSearchCondition)
     */
    public void setPartSearchCondition(IPartSearchCondition condition) {
        this.partSearchCondition = condition;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.IViewer#getPartSearchCondition()
     */
    public IPartSearchCondition getPartSearchCondition() {
        return this.partSearchCondition;
    }

    public void setPartRegistry(PartRegistry partRegistry) {
        this.partRegistry = partRegistry;
    }

    public AccessibleRegistry getAccessibleRegistry() {
        if (accRegistry == null)
            accRegistry = new AccessibleRegistry();
        return accRegistry;
    }

    protected void setAccessibleRegistry(AccessibleRegistry accRegistry) {
        this.accRegistry = accRegistry;
    }

//    public IModelContentProvider getContentProvider() {
//        return modelContentProvider;
//    }
//
//    public void setContentProvider(IModelContentProvider modelContentProvider) {
//        Assert.isNotNull(modelContentProvider);
//        IModelContentProvider oldContentProvider = this.modelContentProvider;
//        this.modelContentProvider = modelContentProvider;
//        if (oldContentProvider != null) {
//            oldContentProvider.inputChanged(this, getInput(), null);
//            oldContentProvider.dispose();
//        }
//        modelContentProvider.inputChanged(this, null, getInput());
//        inputChanged(getInput(), getInput());
//    }

    public Properties getProperties() {
        if (properties == null)
            properties = new Properties(this);
        return properties;
    }

    public void setProperties(Properties properties) {
        if (this.properties != null) {
            this.properties.clear();
        }
        if (properties != null) {
            getProperties().putAll(properties);
        }
    }

    public IDndSupport getDndSupport() {
        return dndSupport;
    }

    public void setDndSupport(IDndSupport dndSupport) {
        this.dndSupport = dndSupport;
    }

    /**
     * @return the listenerSupport
     */
    protected EventListenerSupport getListenerSupport() {
        return listenerSupport;
    }

    public void addPreSelectionChangedListener(
            ISelectionChangedListener listener) {
        getListenerSupport().addListener(PRE_SELECTION_CHANGED_KEY, listener);
    }

    public void removePreSelectionChangedListener(
            ISelectionChangedListener listener) {
        getListenerSupport().removeListener(PRE_SELECTION_CHANGED_KEY,
                listener);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IPostSelectionProvider#
     * addPostSelectionChangedListener
     * (org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void addPostSelectionChangedListener(
            ISelectionChangedListener listener) {
        getListenerSupport().addListener(POST_SELECTION_CHANGED_KEY, listener);
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.jface.viewers.IPostSelectionProvider#
     * removePostSelectionChangedListener
     * (org.eclipse.jface.viewers.ISelectionChangedListener)
     */
    public void removePostSelectionChangedListener(
            ISelectionChangedListener listener) {
        getListenerSupport().removeListener(POST_SELECTION_CHANGED_KEY,
                listener);
    }

    protected void firePreSelectionChanged() {
        ISelection selection = createPreSelection();
        final SelectionChangedEvent event = new SelectionChangedEvent(this,
                selection);
        getListenerSupport().fireEvent(PRE_SELECTION_CHANGED_KEY,
                new IEventDispatcher() {
                    public void dispatch(Object listener) {
                        ((ISelectionChangedListener) listener)
                                .selectionChanged(event);
                    }
                });
    }

    protected void firePostSelectionChanged() {
        if (getControl() == null || getControl().isDisposed())
            return;

        if (postSelectionChangedEventScheduled)
            return;
        postSelectionChangedEventScheduled = true;
        getControl().getDisplay().asyncExec(new Runnable() {
            public void run() {
                if (getControl() == null || getControl().isDisposed())
                    return;

                ISelection selection = getSelection();
                final SelectionChangedEvent event = new SelectionChangedEvent(
                        AbstractViewer.this, selection);
                getListenerSupport().fireEvent(POST_SELECTION_CHANGED_KEY,
                        new IEventDispatcher() {
                            public void dispatch(Object listener) {
                                ((ISelectionChangedListener) listener)
                                        .selectionChanged(event);
                            }
                        });
                postSelectionChangedEventScheduled = false;
            }
        });
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.IViewer#addFocusedChangedListener(org.eclipse.jface.viewers
     * .ISelectionChangedListener)
     */
    public void addFocusedPartChangedListener(
            ISelectionChangedListener listener) {
        getListenerSupport().addListener(FOCUSED_PART_CHANGED_KEY, listener);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.IViewer#removeFocusedChangedListener(org.eclipse.jface.
     * viewers.ISelectionChangedListener)
     */
    public void removeFocusedPartChangedListener(
            ISelectionChangedListener listener) {
        getListenerSupport().removeListener(FOCUSED_PART_CHANGED_KEY, listener);
    }

    protected void fireFocusedPartChanged() {
        IPart focusedPart = getFocusedPart();
        ISelection selection;
        if (focusedPart == null) {
            selection = StructuredSelection.EMPTY;
        } else {
            selection = new StructuredSelection(focusedPart);
        }
        final SelectionChangedEvent event = new SelectionChangedEvent(this,
                selection);
        getListenerSupport().fireEvent(FOCUSED_PART_CHANGED_KEY,
                new IEventDispatcher() {
                    public void dispatch(Object listener) {
                        ((ISelectionChangedListener) listener)
                                .selectionChanged(event);
                    }
                });
    }

    public void addInputChangedListener(IInputChangedListener listener) {
        getListenerSupport().addListener(IInputChangedListener.class, listener);
    }

    public void removeInputChangedListener(IInputChangedListener listener) {
        getListenerSupport().removeListener(IInputChangedListener.class,
                listener);
    }

    protected void fireInputChanged(final Object newInput,
            final Object oldInput) {
        getListenerSupport().fireEvent(IInputChangedListener.class,
                new IEventDispatcher() {
                    public void dispatch(Object listener) {
                        ((IInputChangedListener) listener).inputChanged(
                                AbstractViewer.this, newInput, oldInput);
                    }
                });
    }

    private ISelection createPreSelection() {
        return getPreselected() == null ? StructuredSelection.EMPTY
                : new StructuredSelection(getPreselected());
    }

    public Object getPreselected() {
        return getPartRegistry().getModelByPart(preSelected);
    }

    public IPart getPreselectedPart() {
        return preSelected;
    }

    public void setPreselected(Object element) {
        IPart oldPreselected = this.preSelected;
        IPart newPreselected = getSelectionSupport()
                .findSelectablePart(element);
        if (newPreselected == oldPreselected)
            return;

        this.preSelected = newPreselected;
        if (oldPreselected != null) {
            oldPreselected.getStatus().dePreSelect();
        }
        if (newPreselected != null) {
            newPreselected.getStatus().preSelect();
        }
        firePreSelectionChanged();
    }

    public Object getFocused() {
        return focused;
    }

    public IPart getFocusedPart() {
        return getSelectionSupport().findSelectablePart(getFocused());
    }

    public void setFocused(Object element) {
        IPart oldFocused = this.focused;
        IPart newFocused = getSelectionSupport().findSelectablePart(element);
        if (newFocused == oldFocused)
            return;

        this.focused = newFocused;
        if (oldFocused != null) {
            oldFocused.getStatus().lostFocus();
        }
        if (newFocused != null) {
            newFocused.getStatus().setFocus();
        }

        Control c = getControl();
        if (c != null && !c.isDisposed()) {
            IAccessible acc;
            if (this.focused != null) {
                this.focused.getStatus().setFocus();
                acc = (IAccessible) this.focused.getAdapter(IAccessible.class);
            } else {
                acc = null;
            }
            if (acc != null) {
                c.getAccessible().setFocus(acc.getAccessibleId());
            } else {
                c.getAccessible().setFocus(ACC.CHILDID_SELF);
            }
        }
        fireFocusedPartChanged();
    }

    public IViewerService getService(
            Class<? extends IViewerService> serviceType) {
        if (serviceType == null || serviceRegistry == null)
            return null;
        return serviceRegistry.get(serviceType);
    }

    public boolean hasService(Class<? extends IViewerService> serviceType) {
        return serviceType != null && serviceRegistry != null
                && serviceRegistry.containsKey(serviceType);
    }

    public void installService(Class<? extends IViewerService> type,
            IViewerService service) {
        if (type == null || service == null)
            return;
        if (serviceRegistry == null)
            serviceRegistry = new HashMap<Class<? extends IViewerService>, IViewerService>();
        serviceRegistry.put(type, service);
        if (getControl() != null && !getControl().isDisposed()) {
            service.setControl(getControl());
        }
    }

    public void uninstallService(Class<? extends IViewerService> type) {
        if (type == null || serviceRegistry == null)
            return;
        IViewerService service = serviceRegistry.remove(type);
        if (service != null) {
            service.dispose();
        }
    }

    private List<IViewerService> getActiveServices() {
        if (serviceRegistry != null) {
            ArrayList<IViewerService> list = new ArrayList<IViewerService>();
            for (IViewerService service : serviceRegistry.values()) {
                if (service.isActive())
                    list.add(service);
            }
            return list;
        }
        return null;
    }

}
