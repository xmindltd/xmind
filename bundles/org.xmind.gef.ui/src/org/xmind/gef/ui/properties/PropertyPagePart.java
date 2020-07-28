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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ISelection;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.IPageSite;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.forms.WidgetFactory;

public abstract class PropertyPagePart implements IPropertyPagePart {

    private static final int DEFAULT_SECTION_WIDTH = 200;

    private static class SectionRec {

        IPropertySectionPart section;

        Section control;

        public SectionRec(IPropertySectionPart section) {
            this.section = section;
        }

    }

    private IPropertyPartContainer container;

    private IGraphicalEditor editor;

    private WidgetFactory widgetFactory;

    private List<SectionRec> sections = new ArrayList<SectionRec>();

    private ScrolledForm form;

    public void init(IPropertyPartContainer container,
            IGraphicalEditor editor) {
        this.container = container;
        this.editor = editor;
        for (SectionRec rec : sections) {
            rec.section.init(this, editor);
        }
    }

    public IPageSite getContainerSite() {
        return container.getContainerSite();
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

    public IGraphicalEditor getContributedEditor() {
        return editor;
    }

    protected void addSection(IPropertySectionPart section) {
        Assert.isNotNull(section);
        SectionRec rec = new SectionRec(section);
        sections.add(rec);
        section.init(container, editor);
        if (form != null && !form.isDisposed()) {
            createSectionControl(form.getBody(), rec);
        }
    }

    public List<IPropertySectionPart> getSections() {
        List<IPropertySectionPart> list = new ArrayList<IPropertySectionPart>(
                sections.size());
        for (SectionRec rec : sections) {
            list.add(rec.section);
        }
        return list;
    }

    public void createControl(Composite parent) {
        this.widgetFactory = new WidgetFactory(parent.getDisplay());
        form = widgetFactory.createScrolledForm(parent);
        addHorizontalScrollSupport(form);
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

    protected void createSectionControls(final ScrolledForm form,
            final Composite parent) {
        GridLayout layout = new GridLayout(1, true);
        parent.setLayout(layout);
        for (SectionRec rec : sections) {
            createSectionControl(parent, rec);
        }

        form.addControlListener(new ControlListener() {
            public void controlResized(ControlEvent e) {
                Rectangle area = form.getClientArea();
                GridLayout layout = (GridLayout) parent.getLayout();
                int newNumColumns = area.width / DEFAULT_SECTION_WIDTH;
                boolean change = newNumColumns != layout.numColumns
                        && newNumColumns >= 0
                        && newNumColumns <= parent.getChildren().length;
                if (change) {
                    layout.numColumns = newNumColumns;
                    parent.layout();
                }
            }

            public void controlMoved(ControlEvent e) {
            }
        });
    }

    private void createSectionControl(Composite parent, SectionRec rec) {
        rec.control = widgetFactory.createSection(parent, Section.TITLE_BAR
                | Section.TWISTIE | Section.EXPANDED | SWT.BORDER);
        Composite client = widgetFactory.createComposite(rec.control,
                SWT.NO_FOCUS | SWT.WRAP);
        rec.control.setClient(client);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.verticalAlignment = GridData.BEGINNING;
        data.widthHint = DEFAULT_SECTION_WIDTH;
        rec.control.setLayoutData(data);
        rec.section.createControl(client);
        updateSectionTitle(rec);
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
        return form;
    }

    public abstract String getTitle();

    public void dispose() {
        for (SectionRec rec : sections) {
            rec.section.dispose();
        }
        if (form != null) {
            form.dispose();
            form = null;
        }
    }

    public void setSelection(ISelection selection) {
        for (SectionRec rec : sections) {
            rec.section.setSelection(selection);
        }
    }

    public IPropertyPartContainer getContainer() {
        return container;
    }

    public void refresh() {
        for (SectionRec rec : sections) {
            rec.section.refresh();
        }
        if (form != null && !form.isDisposed()) {
            form.reflow(true);
        }
    }

    public void setFocus() {
        if (sections.isEmpty()) {
            form.setFocus();
        } else {
            sections.get(0).section.setFocus();
        }
    }

}
