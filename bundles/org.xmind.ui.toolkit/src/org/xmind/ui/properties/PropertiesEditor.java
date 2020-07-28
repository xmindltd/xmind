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
package org.xmind.ui.properties;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.IInputSelectionProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.xmind.ui.util.Chainability;
import org.xmind.ui.util.IChained;

public class PropertiesEditor implements IInputSelectionProvider {

    public static final String COLOR_BACKGROUND = "org.xmind.ui.color.PropertiesEditor.background"; //$NON-NLS-1$

    public static final String COLOR_CATEGORY_TITLE = "org.xmind.ui.color.PropertiesEditor.categoryTitle.foreground"; //$NON-NLS-1$

    public static final String COLOR_ENTRY_FOREGROUND = "org.xmind.ui.color.PropertiesEditor.entry.foreground"; //$NON-NLS-1$

    public static final String COLOR_ENTRY_SELECTED_BACKGROUND = "org.xmind.ui.color.PropertiesEditor.entry.selected.background"; //$NON-NLS-1$

    public static final String COLOR_ENTRY_SELECTED_FOREGROUND = "org.xmind.ui.color.PropertiesEditor.entry.selected.foreground"; //$NON-NLS-1$

    public static final String FONT_CATEGORY_TITLE = "org.xmind.ui.font.PropertiesEditor.categoryTitle"; //$NON-NLS-1$

    public static final String FONT_ENTRY = "org.xmind.ui.font.PropertiesEditor.entry"; //$NON-NLS-1$

    public static final String FONT_ENTRY_SELECTED = "org.xmind.ui.font.PropertiesEditor.entry.selected"; //$NON-NLS-1$

    private static final class Section
            implements PropertyChangeListener, IChained<Section> {

        private final PropertiesEditor editor;

        private final String title;

        private PropertyEditingEntry firstEntry = null;

        private PropertyEditingEntry lastEntry = null;

        private PropertyEditingSection section = null;

        private Section prev = null;

        private Section next = null;

        public Section(PropertiesEditor editor, String title) {
            this.editor = editor;
            this.title = title;
        }

        public void create(Composite parent) {
            section = new PropertyEditingSection(parent);
            section.setTitleText(title == null ? "" : title); //$NON-NLS-1$
            section.getEventSupport().addPropertyChangeListener(this);

            Composite client = section.getClient();
            GridLayout gridLayout = new GridLayout(1, true);
            gridLayout.marginWidth = 5;
            gridLayout.marginHeight = 5;
            gridLayout.verticalSpacing = 3;
            gridLayout.horizontalSpacing = 0;
            client.setLayout(gridLayout);

            Iterator<PropertyEditingEntry> it = entries();
            while (it.hasNext()) {
                PropertyEditingEntry entry = it.next();
                entry.createControl(client);
                GridData gridData = new GridData(SWT.FILL, SWT.FILL, true,
                        false);
                gridData.widthHint = SWT.DEFAULT;
                gridData.heightHint = SWT.DEFAULT;
                entry.getControl().setLayoutData(gridData);
            }
        }

        public void dispose() {
            Iterator<PropertyEditingEntry> it = entries();
            while (it.hasNext()) {
                it.next().dispose();
            }
            if (section != null) {
                section.getEventSupport().removePropertyChangeListener(this);
                if (section.getControl() != null
                        && !section.getControl().isDisposed())
                    section.getControl().dispose();
                section = null;
            }
            firstEntry = null;
            lastEntry = null;
        }

        public Iterator<PropertyEditingEntry> entries() {
            return Chainability.iterate(firstEntry, lastEntry);
        }

        public void propertyChange(java.beans.PropertyChangeEvent evt) {
            if (PropertyEditingSection.PROP_EXPANDED
                    .equals(evt.getPropertyName())) {
                editor.reflow();
            }
        }

        public Section getPrevious() {
            return prev;
        }

        public Section getNext() {
            return next;
        }

        public void setPrevious(Section element) {
            this.prev = element;
        }

        public void setNext(Section element) {
            this.next = element;
        }

    }

    private ScrolledComposite container = null;

    private IPropertySource source = null;

    private Map<String, Section> sectionRegistry = new HashMap<String, Section>();

    private Map<String, PropertyEditingEntry> entryRegistry = new HashMap<String, PropertyEditingEntry>();

    private Map<String, String> colorFontOverrides = new HashMap<String, String>();

    private boolean reflowing = false;

    private Section firstSection = null;

    private Section lastSection = null;

    private PropertyEditingEntry selectedEntry = null;

    private ListenerList listeners = new ListenerList();

    private Menu popupMenu = null;

    private IPropertyTransfer transfer = null;

    private List<String> filtedDescriptorIds = new ArrayList<String>();

    public PropertiesEditor() {
        super();
    }

    public Object getInput() {
        return source;
    }

    public void setInput(IPropertySource source) {
        IPropertySource oldSource = this.source;
        this.source = source;
        if (oldSource != source) {
            refresh();
        }
    }

    public ISelection getSelection() {
        if (selectedEntry == null)
            return StructuredSelection.EMPTY;
        return new StructuredSelection(selectedEntry);
    }

    public void setSelection(ISelection selection) {
    }

    public void addSelectionChangedListener(
            ISelectionChangedListener listener) {
        listeners.add(listener);
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        listeners.remove(listener);
    }

    public void create(Composite parent) {
        Assert.isTrue(container == null);
        container = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
        container.setExpandHorizontal(true);
        container.setExpandVertical(true);
        container.setMinWidth(200);
        container.setMinHeight(40);
        addHorizontalScrollSupport(container);
        container.addControlListener(new ControlListener() {

            public void controlResized(ControlEvent e) {
                reflow();
            }

            public void controlMoved(ControlEvent e) {
                reflow();
            }
        });

        Composite contents = new Composite(container, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 3;
        gridLayout.marginHeight = 3;
        gridLayout.verticalSpacing = 3;
        gridLayout.horizontalSpacing = 0;
        contents.setLayout(gridLayout);

        container.setContent(contents);
        initColorsFonts();
        refresh();
    }

    // add horizontal scroll support for windows
    private void addHorizontalScrollSupport(final ScrolledComposite sc) {
        if (Util.isWindows()) {
            sc.addListener(SWT.MouseHorizontalWheel, new Listener() {

                public void handleEvent(Event event) {
                    if (!sc.isDisposed()) {
                        int offset = event.count;
                        offset = -(int) (Math.sqrt(Math.abs(offset)) * offset);

                        Point origin = sc.getOrigin();
                        sc.setOrigin(origin.x + offset, origin.y);
                    }
                }
            });
        }
    }

    public void setColorFontOverrides(String id, String overridedId) {
        if (overridedId == null) {
            colorFontOverrides.remove(id);
        } else {
            colorFontOverrides.put(id, overridedId);
        }
    }

    private String getColorFontId(String id) {
        String overridedId = colorFontOverrides.get(id);
        if (overridedId != null)
            return overridedId;
        return id;
    }

    private Composite getContents() {
        return (Composite) container.getContent();
    }

    private void initColorsFonts() {
        final IPropertyChangeListener colorChangeListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String colorId = event.getProperty();
                if (getColorFontId(COLOR_BACKGROUND).equals(colorId)) {
                    updateBackgroundColor();
                } else if (getColorFontId(COLOR_CATEGORY_TITLE)
                        .equals(colorId)) {
                    updateCategoryTitlesColor();
                } else if (getColorFontId(COLOR_ENTRY_FOREGROUND)
                        .equals(colorId)) {
                    updateEntriesForegroundColor();
                } else if (getColorFontId(COLOR_ENTRY_SELECTED_BACKGROUND)
                        .equals(colorId)) {
                    updateEntriesSelectedBackgroundColor();
                } else if (getColorFontId(COLOR_ENTRY_SELECTED_FOREGROUND)
                        .equals(colorId)) {
                    updateEntriesSelectedForegroundColor();
                }
            }
        };

        final IPropertyChangeListener fontChangeListener = new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                String fontId = event.getProperty();
                if (getColorFontId(FONT_CATEGORY_TITLE).equals(fontId)) {
                    updateCategoryTitlesFont();
                } else if (getColorFontId(FONT_ENTRY).equals(fontId)) {
                    updateEntriesFont();
                } else if (getColorFontId(FONT_ENTRY_SELECTED).equals(fontId)) {
                    updateEntriesSelectedFont();
                }
            }
        };

        JFaceResources.getColorRegistry().addListener(colorChangeListener);
        JFaceResources.getFontRegistry().addListener(fontChangeListener);
        container.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                JFaceResources.getColorRegistry()
                        .removeListener(colorChangeListener);
                JFaceResources.getFontRegistry()
                        .removeListener(fontChangeListener);
            }
        });

        updateColorsFonts();
    }

    protected void updateColorsFonts() {
        updateBackgroundColor();
        updateCategoryTitlesColor();
        updateCategoryTitlesFont();
        updateEntriesForegroundColor();
        updateEntriesFont();
        updateEntriesSelectedBackgroundColor();
        updateEntriesSelectedForegroundColor();
        updateEntriesSelectedFont();
    }

    private void updateBackgroundColor() {
        Color color = JFaceResources.getColorRegistry()
                .get(getColorFontId(COLOR_BACKGROUND));
        container.setBackground(color);
        getContents().setBackground(color);
        Iterator<Section> sections = sections();
        while (sections.hasNext()) {
            Section section = sections.next();
            if (section.section != null)
                section.section.setBackground(color);
        }
    }

    private void updateCategoryTitlesColor() {
        Color color = JFaceResources.getColorRegistry()
                .get(getColorFontId(COLOR_CATEGORY_TITLE));
        Iterator<Section> sections = sections();
        while (sections.hasNext()) {
            Section section = sections.next();
            if (section.section != null) {
                section.section.setTitleColor(color);
            }
        }
    }

    private void updateEntriesForegroundColor() {
        Color color = JFaceResources.getColorRegistry()
                .get(getColorFontId(COLOR_ENTRY_FOREGROUND));
        Iterator<PropertyEditingEntry> entries = entries();
        while (entries.hasNext()) {
            entries.next().setForeground(color);
        }
    }

    private void updateCategoryTitlesFont() {
        Font font = JFaceResources.getFontRegistry()
                .get(getColorFontId(FONT_CATEGORY_TITLE));
        Iterator<Section> sections = sections();
        while (sections.hasNext()) {
            Section section = sections.next();
            if (section.section != null) {
                section.section.setTitleFont(font);
            }
        }
    }

    private void updateEntriesFont() {
        Font font = JFaceResources.getFontRegistry()
                .get(getColorFontId(FONT_ENTRY));
        Iterator<PropertyEditingEntry> entries = entries();
        while (entries.hasNext()) {
            entries.next().setFont(font);
        }
    }

    private void updateEntriesSelectedBackgroundColor() {
        Color color = JFaceResources.getColorRegistry()
                .get(getColorFontId(COLOR_ENTRY_SELECTED_BACKGROUND));
        Iterator<PropertyEditingEntry> entries = entries();
        while (entries.hasNext()) {
            entries.next().setSelectedBackground(color);
        }
    }

    private void updateEntriesSelectedForegroundColor() {
        Color color = JFaceResources.getColorRegistry()
                .get(getColorFontId(COLOR_ENTRY_SELECTED_FOREGROUND));
        Iterator<PropertyEditingEntry> entries = entries();
        while (entries.hasNext()) {
            entries.next().setSelectedForeground(color);
        }
    }

    private void updateEntriesSelectedFont() {
        Font font = JFaceResources.getFontRegistry()
                .get(getColorFontId(FONT_ENTRY_SELECTED));
        Iterator<PropertyEditingEntry> entries = entries();
        while (entries.hasNext()) {
            entries.next().setSelectedFont(font);
        }
    }

    protected Iterator<PropertyEditingEntry> entries() {
        return Chainability.iterate(
                firstSection == null ? null : firstSection.firstEntry, null);
    }

    protected Iterator<Section> sections() {
        return Chainability.iterate(firstSection, lastSection);
    }

    public void setFilter(List<String> filtedDescriptorIds) {
        this.filtedDescriptorIds = filtedDescriptorIds;
    }

    public void refresh() {
        if (container == null || container.isDisposed())
            return;

        container.setRedraw(false);
        try {
            // Remove all sections and entries:
            Iterator<Section> sectionIt = sections();
            while (sectionIt.hasNext()) {
                sectionIt.next().dispose();
            }
            firstSection = null;
            lastSection = null;
            selectedEntry = null;
            sectionRegistry.clear();
            entryRegistry.clear();
            Control[] controls = getContents().getChildren();
            for (int i = 0; i < controls.length; i++) {
                controls[i].dispose();
            }

            if (source != null) {
                IPropertyDescriptor[] descs = source.getPropertyDescriptors();
                for (int i = 0; i < descs.length; i++) {
                    IPropertyDescriptor descriptor = descs[i];
                    if (filtedDescriptorIds != null
                            && filtedDescriptorIds.contains(descriptor.getId()))
                        continue;
                    addEditingEntry(descriptor);
                }
            }

            createSectionControls();

            updateColorsFonts();
            reflow();
        } finally {
            container.setRedraw(true);
        }
    }

    public void updateAll() {
        Iterator<PropertyEditingEntry> entries = entries();
        while (entries.hasNext()) {
            entries.next().update();
        }
    }

    public void update(String propertyId) {
        PropertyEditingEntry entry = entryRegistry.get(propertyId);
        if (entry != null) {
            entry.update();
        }
    }

    protected void addEditingEntry(IPropertyDescriptor descriptor) {
        PropertyEditingEntry entry = new PropertyEditingEntry(this, source,
                descriptor);
        entryRegistry.put(descriptor.getId(), entry);

        String category = descriptor.getCategory();
        Section section = sectionRegistry.get(category);
        if (section == null) {
            section = new Section(this, category);
            sectionRegistry.put(category, section);
            if (firstSection == null || lastSection == null) {
                firstSection = section;
            } else {
                Chainability.insertAfter(lastSection, section);
            }
            lastSection = section;
        }
        if (section.firstEntry == null || section.lastEntry == null) {
            section.firstEntry = entry;
            if (section.getPrevious() != null) {
                Chainability.insertAfter(section.getPrevious().lastEntry,
                        entry);
            }
        } else {
            Chainability.insertAfter(section.lastEntry, entry);
        }
        section.lastEntry = entry;
    }

    private void createSectionControls() {
        if (firstSection == null)
            return;

        Composite parent = getContents();
        if (firstSection.getNext() == null && (firstSection.title == null
                || "".equals(firstSection.title))) { //$NON-NLS-1$
            Iterator<PropertyEditingEntry> it = firstSection.entries();
            while (it.hasNext()) {
                PropertyEditingEntry entry = it.next();
                entry.createControl(parent);
                entry.getControl().setLayoutData(createSectionLayoutData());
            }
        } else {
            Iterator<Section> sectionIt = sections();
            while (sectionIt.hasNext()) {
                Section section = sectionIt.next();
                section.create(parent);
                if (section.section != null) {
                    section.section.getControl()
                            .setLayoutData(createSectionLayoutData());
                }
            }
        }

        if (popupMenu != null) {
            Iterator<PropertyEditingEntry> entries = entries();
            while (entries.hasNext()) {
                entries.next().setPopupMenu(popupMenu);
            }
        }
    }

    private GridData createSectionLayoutData() {
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.widthHint = SWT.DEFAULT;
        gridData.heightHint = SWT.DEFAULT;
        return gridData;
    }

    public Control getControl() {
        return container;
    }

    public void setFocus() {
        if (selectedEntry != null) {
            selectedEntry.setFocus();
        } else if (firstSection != null && firstSection.firstEntry != null) {
            firstSection.firstEntry.setFocus();
        } else if (container != null && !container.isDisposed()) {
            container.setFocus();
        }
    }

    public void reflow() {
        if (container == null || container.isDisposed())
            return;
        if (reflowing)
            return;
        reflowing = true;
        container.getDisplay().asyncExec(new Runnable() {
            public void run() {
                try {
                    if (container != null && !container.isDisposed()) {
                        container.layout(true, true);
                        container.setMinHeight(container.getContent()
                                .computeSize(container.getClientArea().width,
                                        SWT.DEFAULT).y);
                    }
                } finally {
                    reflowing = false;
                }
            }
        });
    }

    public void select(String propertyId) {
        PropertyEditingEntry entry = entryRegistry.get(propertyId);
        if (entry != null) {
            select(entry);
        }
    }

    protected boolean select(PropertyEditingEntry entry) {
        if (entry == null)
            return false;

        PropertyEditingEntry oldEntry = this.selectedEntry;
        this.selectedEntry = entry;
        if (oldEntry != null) {
            oldEntry.setSelected(false);
        }
        entry.setSelected(true);
        entry.setFocus();
        reveal(entry.getControl());
        fireSelectionChanged();
        return true;
    }

    private void reveal(Control control) {
        if (container != null && !container.isDisposed())
            container.showControl(control);
    }

    protected void fireSelectionChanged() {
        ISelection selection = getSelection();
        for (Object listener : listeners.getListeners()) {
            if (listener instanceof ISelectionChangedListener) {
                ((ISelectionChangedListener) listener).selectionChanged(
                        new SelectionChangedEvent(this, selection));
            }
        }
    }

    public void setPopupMenu(Menu menu) {
        this.popupMenu = menu;
        Iterator<PropertyEditingEntry> entries = entries();
        while (entries.hasNext()) {
            entries.next().setPopupMenu(menu);
        }
        if (container != null && !container.isDisposed()) {
            getContents().setMenu(menu);
        }
    }

    public void dispose() {
        Iterator<Section> sectionIt = sections();
        while (sectionIt.hasNext()) {
            sectionIt.next().dispose();
        }
        sectionRegistry.clear();
        firstSection = null;
        lastSection = null;
        source = null;
        if (container != null && !container.isDisposed()) {
            container.dispose();
        }
        container = null;
    }

    /**
     * @return the transfer
     */
    public IPropertyTransfer getTransfer() {
        return transfer;
    }

    /**
     * @param transfer
     *            the transfer to set
     */
    public void setTransfer(IPropertyTransfer transfer) {
        this.transfer = transfer;
    }

}
