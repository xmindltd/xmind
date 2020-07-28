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
package org.xmind.ui.viewers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.xmind.ui.forms.WidgetFactory;

public abstract class CategorizedViewer extends StructuredViewer {

    public static final Object DEFAULT_CATEGORY = new String(
            Messages.CategorizedViewer_UnknownCategory);

    private static final Object[] EMPTY_ARRAY = new Object[0];

    private static final List<Object> EMPTY_LIST = Collections.emptyList();

    private Object[] categories = EMPTY_ARRAY;

    private Map<Object, List<Object>> categorizedElements = new HashMap<Object, List<Object>>();

    private Map<Object, Section> sections = new HashMap<Object, Section>();

    private ScrolledForm container = null;

    private WidgetFactory factory;

    private int sectionStyle = Section.TITLE_BAR | Section.TWISTIE
            | Section.EXPANDED | Section.NO_TITLE_FOCUS_BOX;

    public int getSectionStyle() {
        return sectionStyle;
    }

    public void setSectionStyle(int sectionStyle) {
        this.sectionStyle = sectionStyle;
    }

    protected ScrolledForm getContainer() {
        return container;
    }

    public void createControl(Composite parent, int style) {
        container = createContainer(parent, style);
        hookControl(container);
    }

    private ScrolledForm createContainer(Composite parent, int style) {
        if (factory == null)
            factory = createWidgetFactory(parent.getDisplay());

        ScrolledForm form = new ScrolledForm(parent,
                SWT.V_SCROLL | SWT.LEFT_TO_RIGHT);
        form.setExpandHorizontal(true);
        form.setExpandVertical(true);
        form.setBackground(parent.getBackground());
        form.setForeground(parent.getForeground());
        form.setFont(JFaceResources.getHeaderFont());
        addHorizontalScrollSupport(form);
        container = form;

        configureContainer(container);
        return container;
    }

    // add horizontal scroll support for windows
    private void addHorizontalScrollSupport(final ScrolledForm form) {
        if (Util.isWindows()) {
            form.addListener(SWT.MouseHorizontalWheel, new Listener() {

                public void handleEvent(Event event) {
                    if (!form.isDisposed()) {
                        int offset = event.count;
                        offset = -(int) (Math.sqrt(Math.abs(offset)) * offset);

                        Point origin = form.getOrigin();
                        form.setOrigin(origin.x + offset, origin.y);
                    }
                }
            });
        }
    }

    protected WidgetFactory createWidgetFactory(Display display) {
        return new WidgetFactory(display);
    }

    protected WidgetFactory getWidgetFactory() {
        return factory;
    }

    protected void configureContainer(ScrolledForm container) {
        container.getBody().setLayout(createFormLayout());
        container.setMinWidth(1);
    }

    protected Layout createFormLayout() {
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 3;
        layout.marginHeight = 3;
        return layout;
    }

    protected void hookControl(Control control) {
        super.hookControl(control);
        control.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                relayout();
            }
        });
    }

    private void relayout() {
        int width = getContainer().getClientArea().width;
        for (Section section : sections.values()) {
            resetWidth(width, section);
        }
        container.reflow(true);
    }

    private void resetWidth(int width, Control control) {
        Object ld = control.getLayoutData();
        if (ld instanceof GridData) {
            GridData gd = (GridData) ld;
            GridLayout gl = (GridLayout) control.getParent().getLayout();
            gd.widthHint = width - gl.marginWidth * 2 - gl.marginLeft
                    - gl.marginRight;
        } else if (ld instanceof RowData) {
            RowData rd = (RowData) ld;
            RowLayout rl = (RowLayout) control.getParent().getLayout();
            rd.width = width - rl.marginWidth * 2 - rl.marginLeft
                    - rl.marginRight;
        }
    }

    protected List<Object> getCategories() {
        return Arrays.asList(categories);
    }

    protected List<Object> getElements(Object category) {
        List<Object> elements = categorizedElements.get(category);
        if (elements == null)
            return EMPTY_LIST;
        return Collections.unmodifiableList(elements);
    }

    protected boolean hasCategory(Object category) {
        return categorizedElements.containsKey(category);
    }

    protected Composite getSection(Object category) {
        return sections.get(category);
    }

    protected Widget doFindInputItem(Object element) {
        return getControl();
    }

    protected Widget doFindItem(Object element) {
        if (hasCategory(element))
            return getSection(element);
        return getControl();
    }

    protected void inputChanged(Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        refresh(true);
    }

    protected void internalRefresh(Object element) {
        internalRefresh(element, true);
    }

    protected void internalRefresh(Object element, boolean updateLabels) {
        if (element == getInput()) {
            Map<Object, Boolean> expansionStates = new HashMap<Object, Boolean>();
            for (Object category : getCategories()) {
                Boolean exp = getExpanded(category);
                if (exp != null) {
                    expansionStates.put(category, exp);
                }
            }
            rebuildMap();
            refreshSections();
            for (Object category : getCategories()) {
                Boolean exp = expansionStates.get(category);
                if (exp != null) {
                    setExpanded(category, exp.booleanValue());
                }
            }
            if (getContainer() != null && !getContainer().isDisposed()) {
                relayout();
            }
        } else if (hasCategory(element)) {
            rebuildMapForCategory(element);
            if (updateLabels)
                updateSection(element, getSection(element));
            refreshSectionContent(getSectionContent(element), element, null);
        } else {
            Object category = getCategory(element);
            if (category != null && hasCategory(category)) {
                refreshSectionContent(getSectionContent(category), category,
                        element);
            }
        }
    }

    public Boolean getExpanded(Object category) {
        Composite section = getSection(category);
        if (section instanceof Section) {
            return Boolean.valueOf(((Section) section).isExpanded());
        }
        return null;
    }

    public void setExpanded(Object category, boolean expanded) {
        Composite section = getSection(category);
        if (section instanceof Section) {
            ((Section) section).setExpanded(expanded);
        }
    }

    public void setAllExpanded(boolean expanded) {
        for (Section section : sections.values()) {
            section.setExpanded(expanded);
        }
    }

    private void rebuildMap() {
        categorizedElements.clear();
        if (getContentProvider() instanceof ITreeContentProvider) {
            categories = getSortedChildren(getInput());
            for (int i = 0; i < categories.length; i++) {
                Object category = categories[i];
                Object[] children = ((ITreeContentProvider) getContentProvider())
                        .getChildren(category);
                ViewerFilter[] filters = getFilters();
                if (filters != null && filters.length > 0) {
                    for (ViewerFilter f : filters) {
                        Object[] filteredChildren = f.filter(this, getInput(),
                                children);
                        if (filteredChildren != null)
                            children = filteredChildren;
                    }
                }
                if (getSorter() != null) {
                    getSorter().sort(this, children);
                }
                categorizedElements.put(category, Arrays.asList(children));
            }
        } else if (getContentProvider() instanceof ICategorizedContentProvider) {
            Object[] elements = getSortedChildren(getInput());
            List<Object> rawCategories = new ArrayList<Object>(elements.length);
            for (Object element : elements) {
                Object category = getCategory(element);
                List<Object> list = categorizedElements.get(category);
                if (list == null) {
                    list = new ArrayList<Object>(elements.length);
                    categorizedElements.put(category, list);
                    rawCategories.add(category);
                }
                list.add(element);
            }
            categories = rawCategories.toArray();
            ViewerFilter[] filters = getFilters();
            if (filters != null && filters.length > 0) {
                for (ViewerFilter f : filters) {
                    Object[] filteredCategories = f.filter(this, getInput(),
                            categories);
                    if (filteredCategories != null)
                        categories = filteredCategories;
                }
            }
            if (getSorter() != null) {
                getSorter().sort(this, categories);
            }
        }
    }

    private void rebuildMapForCategory(Object category) {
        if (getContentProvider() instanceof ITreeContentProvider) {
            Object[] children = ((ITreeContentProvider) getContentProvider())
                    .getChildren(category);
            ViewerFilter[] filters = getFilters();
            if (filters != null && filters.length > 0) {
                for (ViewerFilter f : filters) {
                    Object[] filteredChildren = f.filter(this, getInput(),
                            children);
                    if (filteredChildren != null)
                        children = filteredChildren;
                }
            }
            if (getSorter() != null) {
                getSorter().sort(this, children);
            }
            categorizedElements.put(category, Arrays.asList(children));
        } else if (getContentProvider() instanceof ICategorizedContentProvider) {
            Object[] elements = getSortedChildren(getInput());
            List<Object> elementsInCategory = new ArrayList<Object>(
                    elements.length);
            for (Object element : elements) {
                if (getCategory(element).equals(category)) {
                    elementsInCategory.add(element);
                }
            }
            categorizedElements.put(category, elementsInCategory);
        }
    }

    protected Object getCategory(Object element) {
        Object category = null;
        if (getContentProvider() instanceof ICategorizedContentProvider) {
            category = ((ICategorizedContentProvider) getContentProvider())
                    .getCategory(element);
        } else if (getContentProvider() instanceof ITreeContentProvider) {
            category = ((ITreeContentProvider) getContentProvider())
                    .getParent(element);
        }
        if (category != null)
            return category;
        return DEFAULT_CATEGORY;
    }

    private void refreshSections() {
        if (getContainer() == null || getContainer().isDisposed())
            return;

        getContainer().setRedraw(false);
        try {
            Set<Section> sectionsToRemove = new HashSet<Section>(
                    sections.values());
            Section lastSection = null;

            Composite parent = container.getBody();
            for (int i = 0; i < categories.length; i++) {
                Object category = categories[i];
                Section section = sections.get(category);
                if (section == null) {
                    section = createSection(parent, category);
                    sections.put(category, section);
                }
                updateSection(category, section);
                refreshSectionContent(section.getClient(), category, null);
                section.moveBelow(lastSection);
                lastSection = section;
                sectionsToRemove.remove(section);
            }

            for (Section section : sectionsToRemove) {
                disposeSection(section);
            }
        } finally {
            if (getContainer() != null && !getContainer().isDisposed()) {
                getContainer().setRedraw(true);
            }
        }
    }

    private void disposeSection(Section section) {
        Object category = section.getData();
        sections.remove(category);
        disposeSectionContent(section, category);
        section.setMenu(null);
        section.dispose();
    }

    private Section createSection(Composite parent, Object category) {
        Section section = factory.createSection(parent, getSectionStyle());
        section.setData(category);
        section.setLayoutData(getSectionLayoutData(section, category));

        Control content = createSectionContent(section, category);
        Assert.isNotNull(content);
        section.setClient(content);
        configureSection(section, category);

        return section;
    }

    protected void configureSection(Section section, Object category) {
        section.setTitleBarBackground(
                section.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        section.setTitleBarBorderColor(
                section.getDisplay().getSystemColor(SWT.COLOR_WHITE));
    }

    protected Object getSectionLayoutData(Section section, Object category) {
        return new GridData(SWT.FILL, SWT.FILL, true, false);
    }

    private Control getSectionContent(Object category) {
        Composite section = getSection(category);
        if (section instanceof Section && !section.isDisposed())
            return ((Section) section).getClient();
        return null;
    }

    protected String getText(Object element) {
        if (getLabelProvider() instanceof ILabelProvider) {
            return ((ILabelProvider) getLabelProvider()).getText(element);
        }
        return element == null ? null : element.toString();
    }

    protected Image getImage(Object element) {
        if (getLabelProvider() instanceof ILabelProvider) {
            return ((ILabelProvider) getLabelProvider()).getImage(element);
        }
        return null;
    }

    protected Color getForeground(Object element) {
        if (getLabelProvider() instanceof IColorProvider) {
            return ((IColorProvider) getLabelProvider()).getForeground(element);
        }
        return null;
    }

    protected Color getBackground(Object element) {
        if (getLabelProvider() instanceof IColorProvider) {
            return ((IColorProvider) getLabelProvider()).getBackground(element);
        }
        return null;
    }

    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        if (hasCategory(element)) {
            updateSection(element, getSection(element));
        }
    }

    protected void updateSection(Object category, Composite section) {
        section.setData(category);
        if (section instanceof Section) {
            Section s = (Section) section;
            String text = getText(category);
            if (text != null)
                s.setText(text);

            Color foreground = getForeground(category);
            if (foreground != null)
                s.setTitleBarForeground(foreground);

            Color background = getBackground(category);
            if (background != null)
                s.setTitleBarBackground(background);
        }
    }

    public void reveal(Object element) {
        if (hasCategory(element)) {
            reveal(element, null);
        } else {
            Object category = getCategory(element);
            reveal(category, element);
        }
    }

    protected void reveal(Object category, Object element) {
        Composite section = getSection(category);
        if (section != null && !section.isDisposed()) {
            if (section instanceof Section) {
                ((Section) section).setExpanded(true);
            }
            Point loc = Display.getCurrent().map(section, getContainer(), 0, 0);
            reveal(loc.x, loc.y);
        }
    }

    protected void reveal(int x, int y) {
        Point origin = container.getOrigin();
        origin.x += x;
        origin.y += y;
        container.setOrigin(origin);
    }

    public Control getControl() {
        return container;
    }

    protected List getSelectionFromWidget() {
        List list = new ArrayList();
        for (Object category : getCategories()) {
            fillSelection(category, list);
        }
        return list;
    }

    protected void setSelectionToWidget(List l, boolean reveal) {
    }

    protected void setSelectionToWidget(ISelection selection, boolean reveal) {
        for (Object category : getCategories()) {
            setSelectionToCategory(category, selection, reveal);
        }
        if (reveal) {
            Object element = ((IStructuredSelection) getSelection())
                    .getFirstElement();
            if (element != null) {
                reveal(element);
            }
        }
    }

    protected void handleDispose(DisposeEvent event) {
        super.handleDispose(event);
        sections.clear();
        categories = EMPTY_ARRAY;
        categorizedElements.clear();
        if (factory != null) {
            factory.dispose();
            factory = null;
        }
    }

    public void setFocus() {
        for (Object category : getCategories()) {
            Composite section = getSection(category);
            if (section instanceof Section) {
                if (((Section) section).getClient().setFocus())
                    return;
            }
        }
    }

    protected abstract Control createSectionContent(Composite parent,
            Object category);

    protected abstract void disposeSectionContent(Composite section,
            Object category);

    protected abstract void refreshSectionContent(Control content,
            Object category, Object element);

    protected abstract void fillSelection(Object category, List selection);

    protected abstract void setSelectionToCategory(Object category,
            ISelection selection, boolean reveal);

}
