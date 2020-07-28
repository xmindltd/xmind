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
package org.xmind.gef.ui.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.forms.WidgetFactory;

public abstract class GraphicalPropertySheetPage extends Page
        implements ISelectionChangedListener, IPropertyPartContainer {

    protected static class SectionRec {

        String id;

        IPropertySectionPart section;

        Section control;

        boolean visible;

        public SectionRec(String id, IPropertySectionPart section) {
            this.id = id;
            this.section = section;
        }

    }

    private static final int DEFAULT_SECTION_WIDTH = 200;

    private final IGraphicalEditor editor;

    private List<SectionRec> sections = new ArrayList<SectionRec>();

    private Composite composite;

    private WidgetFactory widgetFactory;

    private ScrolledForm form;

    private Label titleBar;

    private Control titleSeparator;

    private String title;

    public GraphicalPropertySheetPage(IGraphicalEditor editor) {
        this.editor = editor;
    }

    public void init(IPageSite pageSite) {
        super.init(pageSite);
        for (SectionRec rec : sections) {
            rec.section.init(this, getContributedEditor());
        }
        if (editor != null) {
            editor.getSite().getSelectionProvider()
                    .addSelectionChangedListener(this);

            final ISelection selection = editor.getSite().getSelectionProvider()
                    .getSelection();
            if (selection != null && !selection.isEmpty()) {
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        selectionChanged(new SelectionChangedEvent(
                                editor.getSite().getSelectionProvider(),
                                selection));
                    }
                });
            }
        }
    }

    public IGraphicalEditor getContributedEditor() {
        return editor;
    }

    protected void addSection(String id, IPropertySectionPart section) {
        Assert.isNotNull(id);
        Assert.isNotNull(section);
        removeSection(id);
        SectionRec rec = new SectionRec(id, section);
        sections.add(rec);
        section.init(this, editor);
        if (form != null && !form.isDisposed()) {
            createSectionControl(form.getBody(), rec);
        }
    }

    protected void removeSection(String id) {
        SectionRec rec = getRec(id);
        if (rec == null)
            return;

        if (sections.remove(rec)) {
            rec.section.dispose();
            if (rec.control != null && !rec.control.isDisposed()) {
                rec.control.dispose();
            }
        }
    }

    protected boolean hasSection(String id) {
        return getRec(id) != null;
    }

    protected IPropertySectionPart getSection(String id) {
        SectionRec rec = getRec(id);
        return rec == null ? null : rec.section;
    }

    protected List<String> getSectionIds() {
        ArrayList<String> list = new ArrayList<String>(sections.size());
        for (SectionRec rec : sections) {
            list.add(rec.id);
        }
        return list;
    }

    protected List<String> getVisibleSectionIds() {
        ArrayList<String> list = new ArrayList<String>(sections.size());
        for (SectionRec rec : sections) {
            if (rec.visible)
                list.add(rec.id);
        }
        return list;
    }

    protected boolean isSectionVisible(String id) {
        SectionRec rec = getRec(id);
        return rec != null && rec.visible;
    }

    protected void setSectionVisible(String id, boolean visible) {
        SectionRec rec = getRec(id);
        if (rec == null || rec.visible == visible)
            return;

        rec.visible = visible;
        if (rec.control != null && !rec.control.isDisposed()) {
            GridData gd = (GridData) rec.control.getLayoutData();
            gd.exclude = !visible;
            rec.control.setVisible(visible);
        }
    }

    protected void reflow() {
        if (form != null && !form.isDisposed()) {
            form.reflow(true);
            form.getParent().layout();
        }
    }

    protected void moveSectionFirst(String id) {
        SectionRec rec = getRec(id);
        if (rec == null)
            return;

        moveSectionFirst(rec);
    }

    private void moveSectionFirst(SectionRec rec) {
        if (rec.control != null && !rec.control.isDisposed()) {
            rec.control.moveAbove(null);
            rec.control.getParent().layout();
        }
    }

    protected void moveSectionAfter(String id, String lastId) {
        SectionRec rec = getRec(id);
        if (rec == null)
            return;
        SectionRec lastRec = getRec(lastId);
        if (lastRec == null) {
            moveSectionFirst(rec);
        } else {
            if (rec.control != null && !rec.control.isDisposed()
                    && lastRec.control != null
                    && !lastRec.control.isDisposed()) {
                rec.control.moveBelow(lastRec.control);
                rec.control.getParent().layout();
            }
        }
    }

    private SectionRec getRec(String id) {
        if (id == null)
            return null;

        for (SectionRec rec : sections) {
            if (id.equals(rec.id))
                return rec;
        }
        return null;
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
        titleBar.setBackground(
                composite.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        titleSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        titleSeparator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.widgetFactory = new WidgetFactory(composite.getDisplay());

        form = widgetFactory.createScrolledForm(composite);
        addHorizontalScrollSupport(form);
        form.setLayoutData(new GridData(GridData.FILL_BOTH));
        form.setMinWidth(DEFAULT_SECTION_WIDTH); // TODO this not working???
        form.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                if (widgetFactory != null) {
                    widgetFactory.dispose();
                    widgetFactory = null;
                }
            }
        });

        createSectionControls(form, form.getBody());

        Composite internalComposite = new Composite(form.getBody(), SWT.NONE);
        internalComposite.setBackground(form.getBody().getBackground());
        internalComposite.setLayout(new GridLayout(1, false));
        internalComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, true));
        createExtendSectionControls(widgetFactory, internalComposite);
        form.reflow(true);
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

    protected void createExtendSectionControls(WidgetFactory widgetFactory,
            Composite parent) {
    }

    protected void createSectionControls(final ScrolledForm form,
            final Composite formBody) {
        GridLayout layout = new GridLayout(1, true);
        formBody.setLayout(layout);
        for (SectionRec rec : sections) {
            createSectionControl(formBody, rec);
        }
        form.addControlListener(new ControlListener() {
            public void controlResized(ControlEvent e) {
                relayout(form, formBody);
            }

            public void controlMoved(ControlEvent e) {
            }
        });
    }

    private void relayout(ScrolledForm form, Composite formBody) {
        Rectangle area = form.getClientArea();
        GridLayout layout = (GridLayout) formBody.getLayout();
        int newNumColumns = Math.max(1, area.width / DEFAULT_SECTION_WIDTH);
        boolean change = newNumColumns != layout.numColumns
                && newNumColumns >= 0
                && newNumColumns <= formBody.getChildren().length;
        if (change) {
            layout.numColumns = newNumColumns;
            formBody.layout();
        }
    }

    private void createSectionControl(Composite parent, SectionRec rec) {
        rec.control = widgetFactory.createSection(parent,
                Section.TITLE_BAR | SWT.BORDER);
        Composite client = widgetFactory.createComposite(rec.control,
                SWT.NO_FOCUS | SWT.WRAP);
        rec.control.setClient(client);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.verticalAlignment = GridData.BEGINNING;
        data.widthHint = DEFAULT_SECTION_WIDTH;
        rec.control.setLayoutData(data);
        rec.section.createControl(client);
        rec.visible = true;
        updateSectionTitle(rec);
    }

    public void updateSectionTitle(IPropertySectionPart section) {
        SectionRec rec = findRecord(section);
        if (rec != null) {
            updateSectionTitle(rec);
        }
    }

    private SectionRec findRecord(IPropertySectionPart section) {
        for (SectionRec rec : sections) {
            if (rec.section == section)
                return rec;
        }
        return null;
    }

    private void updateSectionTitle(SectionRec rec) {
        if (rec.control == null || rec.control.isDisposed())
            return;

        String title = rec.section.getTitle();
        if (title == null) {
            title = ""; //$NON-NLS-1$
        }
        rec.control.setText(title);
    }

    public Control getControl() {
        return composite;
    }

    public void setFocus() {
        if (sections.isEmpty()) {
            if (form != null && !form.isDisposed())
                form.setFocus();
            else if (composite != null && !composite.isDisposed())
                composite.setFocus();
        } else {
            sections.get(0).section.setFocus();
        }
    }

    public void selectionChanged(SelectionChangedEvent event) {
        ISelection selection = event.getSelection();

        selectionChanged(selection);

        if (composite != null && !composite.isDisposed())
            composite.setRedraw(false);
        setSelectionToSections(selection);
        if (form != null && !form.isDisposed()) {
            refresh();
        }
        if (composite != null && !composite.isDisposed())
            composite.setRedraw(true);
    }

    protected abstract void selectionChanged(ISelection selection);

    private void setSelectionToSections(ISelection selection) {
        for (SectionRec rec : sections) {
            if (rec.visible) {
                rec.section.setSelection(selection);
                updateSectionTitle(rec);
            }
        }
    }

    private void updateTitleBar() {
        if (titleBar == null || titleBar.isDisposed())
            return;
        String title = getTitle();
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
        titleBar.getParent().layout();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title == this.title || (title != null && title.equals(this.title)))
            return;
        this.title = title;
        updateTitleBar();
    }

    public IPageSite getContainerSite() {
        return getSite();
    }

    public void refresh() {
        for (SectionRec rec : sections) {
            if (rec.visible) {
                rec.section.refresh();
            }
        }
        if (form != null && !form.isDisposed()) {
            form.reflow(true);
        }
    }

    public void dispose() {
        if (editor != null) {
            editor.getSite().getSelectionProvider()
                    .removeSelectionChangedListener(this);
        }

        for (SectionRec rec : sections) {
            rec.section.dispose();
            rec.visible = false;
            rec.control = null;
        }
        if (composite != null) {
            composite.dispose();
            composite = null;
        }
        form = null;
        title = null;
        titleBar = null;
        titleSeparator = null;
        super.dispose();
    }

}
