/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.ui.gallery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.xmind.gef.EditDomain;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.util.Properties;
import org.xmind.ui.viewers.CategorizedViewer;
import org.xmind.ui.viewers.IGraphicalToolTipProvider;
import org.xmind.ui.viewers.IToolTipProvider;

public class CategorizedGalleryViewer extends CategorizedViewer {

    private static class DelegatingLabelProvider extends BaseLabelProvider
            implements ILabelProvider, IColorProvider, IFontProvider,
            IToolTipProvider, IGraphicalToolTipProvider, ILabelDecorator {

        private IBaseLabelProvider labelProvider;

        public DelegatingLabelProvider(IBaseLabelProvider labelProvider) {
            this.labelProvider = labelProvider;
        }

        public boolean isLabelProperty(Object element, String property) {
            return labelProvider.isLabelProperty(element, property);
        }

        public void addListener(ILabelProviderListener listener) {
            super.addListener(listener);
            labelProvider.addListener(listener);
        }

        public void removeListener(ILabelProviderListener listener) {
            super.removeListener(listener);
            labelProvider.removeListener(listener);
        }

        public void dispose() {
            Object[] listeners = getListeners();
            for (int i = 0; i < listeners.length; i++) {
                labelProvider
                        .removeListener((ILabelProviderListener) listeners[i]);
            }
            super.dispose();
        }

        public Font getFont(Object element) {
            if (labelProvider instanceof IFontProvider)
                return ((IFontProvider) labelProvider).getFont(element);
            return null;
        }

        public Color getForeground(Object element) {
            if (labelProvider instanceof IColorProvider)
                return ((IColorProvider) labelProvider).getForeground(element);
            return null;
        }

        public Color getBackground(Object element) {
            if (labelProvider instanceof IColorProvider)
                return ((IColorProvider) labelProvider).getBackground(element);
            return null;
        }

        public Image getImage(Object element) {
            if (labelProvider instanceof ILabelProvider)
                return ((ILabelProvider) labelProvider).getImage(element);
            return null;
        }

        public String getText(Object element) {
            if (labelProvider instanceof ILabelProvider)
                return ((ILabelProvider) labelProvider).getText(element);
            return ""; //$NON-NLS-1$
        }

        public String getToolTip(Object element) {
            if (labelProvider instanceof IToolTipProvider)
                return ((IToolTipProvider) labelProvider).getToolTip(element);
            return null;
        }

        public IFigure getToolTipFigure(Object element) {
            if (labelProvider instanceof IGraphicalToolTipProvider)
                return ((IGraphicalToolTipProvider) labelProvider)
                        .getToolTipFigure(element);
            return null;
        }

        @Override
        public IFigure decorateFigure(IFigure figure, Object element,
                IDecorationContext context) {
            if (labelProvider instanceof ILabelDecorator)
                return ((ILabelDecorator) labelProvider).decorateFigure(figure,
                        element, context);
            return figure;
        }

    }

    private Map<Object, GalleryViewer> viewers = new HashMap<Object, GalleryViewer>();

    private Properties properties = new Properties();

    private ISelectionChangedListener viewerSelectionChangedListener = new ISelectionChangedListener() {

        public void selectionChanged(SelectionChangedEvent event) {
            if (settingViewerSelections)
                return;

            for (GalleryViewer viewer : viewers.values()) {
                if (viewer != event.getSelectionProvider()) {
                    setSelectionToNestedViewer(viewer,
                            StructuredSelection.EMPTY, false);
                }
            }

            fireSelectionChanged(new SelectionChangedEvent(
                    CategorizedGalleryViewer.this, getSelection()));
        }

    };

    private IOpenListener viewerOpenListener = new IOpenListener() {

        public void open(OpenEvent event) {
            fireOpen(new OpenEvent(CategorizedGalleryViewer.this,
                    getSelection()));
        }

    };

    private boolean settingViewerSelections = false;

    private EditDomain editDomain = null;

    private Listener selectionClearer = new Listener() {
        public void handleEvent(Event event) {
            setSelection(StructuredSelection.EMPTY);
        }
    };

    public EditDomain getEditDomain() {
        return editDomain;
    }

    public void setEditDomain(EditDomain editDomain) {
        EditDomain oldEditDomain = this.editDomain;
        if (oldEditDomain != null) {
            oldEditDomain.dispose();
        }
        this.editDomain = editDomain;
        for (GalleryViewer viewer : viewers.values()) {
            viewer.setEditDomain(editDomain);
        }
    }

    public Properties getProperties() {
        return properties;
    }

    protected final void hookSelectionClearer(Control control) {
        control.addListener(SWT.MouseDown, selectionClearer);
    }

    protected void hookControl(Control control) {
        super.hookControl(control);
        hookSelectionClearer(control);
        hookSelectionClearer(((ScrolledForm) control).getBody());
    }

    protected Control createSectionContent(Composite parent, Object category) {
        hookSelectionClearer(parent);
        Composite wrap = getWidgetFactory().createComposite(parent, SWT.WRAP);
        hookSelectionClearer(wrap);
        GridLayout wrapLayout = new GridLayout(1, false);
        wrapLayout.marginWidth = 0;
        wrapLayout.marginHeight = 0;
        wrapLayout.verticalSpacing = 0;
        wrapLayout.horizontalSpacing = 0;
        wrap.setLayout(wrapLayout);

        GalleryViewer viewer = createNestedViewer();
        configureNestedViewer(viewer, category);
        Control control = viewer.createControl(wrap);
        getWidgetFactory().adapt(control, false, false);
        control.setMenu(control.getParent().getMenu());
        hookNestedViewerControl(viewer, category);
        control.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        viewer.setSelection(StructuredSelection.EMPTY);

        viewers.put(category, viewer);

        return wrap;
    }

    protected void disposeSectionContent(Composite parent, Object category) {
        GalleryViewer viewer = viewers.remove(category);
        if (viewer != null) {
            Control control = viewer.getControl();
            if (control != null && !control.isDisposed()) {
                control.getParent().setMenu(null);
                control.setMenu(null);
                control.dispose();
            }
        }
    }

    protected void refreshSectionContent(Control content, Object category,
            Object element) {
        GalleryViewer viewer = viewers.get(category);
        if (viewer != null && viewer.getControl() != null
                && !viewer.getControl().isDisposed()) {
            if (element != null) {
                IPart part = viewer.findPart(element);
                if (part != null) {
                    part.refresh();
                }
            } else {
                viewer.setInput(getElements(category));
            }
        }
    }

    protected GalleryViewer createNestedViewer() {
        return new GalleryViewer();
    }

    protected void configureNestedViewer(GalleryViewer viewer,
            Object category) {
        viewer.setProperties(properties);
        viewer.setContentProvider(new ArrayContentProvider());
        viewer.setLabelProvider(
                new DelegatingLabelProvider(getLabelProvider()));
        viewer.setFilters(getFilters());
        viewer.setSorter(getSorter());
        viewer.addSelectionChangedListener(viewerSelectionChangedListener);
        viewer.addOpenListener(viewerOpenListener);
        viewer.setEditDomain(getEditDomain());
    }

    protected void hookNestedViewerControl(GalleryViewer viewer,
            Object category) {
        // Disable scrolling feature of this viewer so that all
        // scroll events will pass through to the scrolled form:
        viewer.getCanvas().setScrollBarVisibility(FigureCanvas.NEVER);
        ScrollBar hBar = viewer.getCanvas().getHorizontalBar();
        if (hBar != null) {
            hBar.setEnabled(false);
            hBar.setVisible(false);
        }
        ScrollBar vBar = viewer.getCanvas().getVerticalBar();
        if (vBar != null) {
            vBar.setEnabled(false);
            vBar.setVisible(false);
        }
    }

    protected void reveal(Object category, final Object element) {
        if (element != null) {
            final GalleryViewer viewer = viewers.get(category);
            if (viewer != null && viewer.getControl() != null
                    && !viewer.getControl().isDisposed()) {
                final IPart part = viewer.findPart(element);
                if (part != null && part instanceof IGraphicalPart) {
                    Display.getCurrent().asyncExec(new Runnable() {
                        public void run() {
                            if (viewer.getControl() == null
                                    || viewer.getControl().isDisposed())
                                return;

                            IFigure fig = ((IGraphicalPart) part).getFigure();
                            Point loc = viewer.computeToDisplay(
                                    fig.getBounds().getLocation(), true);
                            loc = new Point(
                                    getContainer().toControl(loc.x, loc.y));
                            reveal(loc.x, loc.y - 10);
                        }
                    });
                    return;
                }
            }
        }

        super.reveal(category, element);
    }

    @SuppressWarnings("unchecked")
    protected void fillSelection(Object category, List selection) {
        GalleryViewer viewer = viewers.get(category);
        if (viewer != null) {
            ISelection sel = viewer.getSelection();
            if (sel instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection) sel;
                selection.addAll(ss.toList());
            }
        }
    }

    protected void setSelectionToCategory(Object category, ISelection selection,
            boolean reveal) {
        GalleryViewer viewer = viewers.get(category);
        if (viewer != null) {
            setSelectionToNestedViewer(viewer, selection, reveal);
        }
    }

    private void setSelectionToNestedViewer(GalleryViewer viewer,
            ISelection selection, boolean reveal) {
        settingViewerSelections = true;
        try {
            viewer.setSelection(selection, reveal);
        } finally {
            settingViewerSelections = false;
        }
    }

    public GalleryViewer getNestedViewer(Object category) {
        return viewers.get(category);
    }

    protected void handleDispose(DisposeEvent event) {
        super.handleDispose(event);
        viewers.clear();
    }

    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        super.setLabelProvider(labelProvider);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.setLabelProvider(labelProvider);
        }
    }

    public void setSorter(ViewerSorter sorter) {
        super.setSorter(sorter);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.setSorter(sorter);
        }
    }

    public void addFilter(ViewerFilter filter) {
        super.addFilter(filter);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.addFilter(filter);
        }
    }

    public void removeFilter(ViewerFilter filter) {
        super.removeFilter(filter);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.removeFilter(filter);
        }
    }

    public void setFilters(ViewerFilter... filters) {
        super.setFilters(filters);
        for (GalleryViewer viewer : viewers.values()) {
            viewer.setFilters(filters);
        }
    }

    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        super.doUpdateItem(item, element, fullMap);
        Object category = getCategory(element);
        if (category != null) {
            GalleryViewer viewer = viewers.get(category);
            if (viewer != null) {
                viewer.update(new Object[] { element });
            }
        }
    }

}
