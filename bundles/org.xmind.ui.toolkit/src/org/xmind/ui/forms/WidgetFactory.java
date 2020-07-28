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
package org.xmind.ui.forms;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Frank Shaka
 */
public class WidgetFactory extends FormToolkit {

    /**
     * @param display
     */
    public WidgetFactory(Display display) {
        super(display);
    }

    public Text createText(Composite parent, String value, int style) {
        Text text = super.createText(parent, value, style);
        text.setBackground(null);
        return text;
    }

    public Composite createComposite(Composite parent, int style) {
        Composite c = super.createComposite(parent, style);
        paintBordersFor(c);
        return c;
    }

    public Composite createComposite(Composite parent) {
        Composite c = createComposite(parent, SWT.NO_FOCUS);
        return c;
    }

    /**
     * Creates a plain composite as a part of the form.
     * 
     * @param parent
     *            the composite parent.
     * @param style
     *            the composite style.
     * @return the composite.
     */
    public Composite createPlainComposite(Composite parent, int style) {
        Composite c = super.createComposite(parent, style);
        c.setBackground(parent.getBackground());
        paintBordersFor(c);
        return c;
    }

    /**
     * Creates a scrolled composite as a part of the form.
     * 
     * @param parent
     *            the composite parent.
     * @param style
     *            the composite style.
     * @return the composite.
     */
    public ScrolledComposite createScrolledComposite(Composite parent, int style) {
        ScrolledComposite scrolledComposite = new ScrolledComposite(parent,
                style);
        adapt(scrolledComposite);
        scrolledComposite.getHorizontalBar().setIncrement(10);
        scrolledComposite.getVerticalBar().setIncrement(10);
        return scrolledComposite;
    }

    public Control createEmptyControl(Composite parent) {
        return new Composite(parent, SWT.NONE) {
            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                return super.computeSize(0, 0, changed);
            }
        };
    }

    /**
     * Creates a group as a part of the form.
     * 
     * @param parent
     *            the group parent.
     * @param text
     *            the group title.
     * @return the composite.
     */
    public Group createGroup(Composite parent, String text) {
        Group group = new Group(parent, SWT.NO_FOCUS | SWT.SHADOW_NONE);
        group.setText(text);
        group.setBackground(getColors().getBackground());
        group.setForeground(getColors().getForeground());
        return group;
    }

    public GridLayoutFactory createGroupLayout() {
        return GridLayoutFactory.swtDefaults().margins(2, 2).spacing(2, 2);
    }

    public Combo createCombo(Composite parent, int style) {
        Combo combo = new Combo(parent, style);
        return combo;
    }

    public ToolBar createToolBar(Composite parent, int style) {
        ToolBar toolBar = new ToolBar(parent, style);
        adapt(toolBar);
        return toolBar;
    }

    public ToolBar createFlatToolBar(Composite parent) {
        return createToolBar(parent, SWT.FLAT | SWT.RIGHT);
    }

    public Spinner createSpinner(Composite parent, int style) {
        Spinner spinner = new Spinner(parent, style);
        return spinner;
    }

    public Spinner createSpinner(Composite parent, int style, int selection,
            int min, int max, int digits, int increment, int pageIncrement) {
        Spinner spinner = new Spinner(parent, style);
        spinner
                .setValues(selection, min, max, digits, increment,
                        pageIncrement);
        return spinner;
    }

    public Composite createSectionContent(Composite parent, String title,
            int style, Object sectionLayoutData) {
        Section section = createSection(parent, style);
        section.setText(title);
        sectionLayoutData = (sectionLayoutData == null) ? new GridData(
                SWT.FILL, SWT.FILL, true, false) : sectionLayoutData;
        section.setLayoutData(sectionLayoutData);
        Composite client = createComposite(section, SWT.NO_FOCUS | SWT.WRAP);
        section.setClient(client);
        return client;
    }

    /**
     * @param bar
     */
    public void createSpacingItem(ToolBar bar, int width) {
        ToolItem spacingItem = new ToolItem(bar, SWT.SEPARATOR);
        Composite emptyComposite = new Composite(bar, SWT.NONE);
        spacingItem.setControl(emptyComposite);
        spacingItem.setWidth(width);
    }

    /**
     * @param bar
     * @param text
     * @param width
     */
    public void createLabel(ToolBar bar, String text, int width) {
        ToolItem labelItem = new ToolItem(bar, SWT.SEPARATOR);
        Composite labelContainer = new Composite(bar, SWT.NONE);
        labelContainer.setLayout(new GridLayout(1, false));
        Label label = new Label(labelContainer, SWT.NONE);
        label.setLayoutData(new GridData(GridData.CENTER, GridData.CENTER,
                false, false));
        label.setText(text);
        label.pack();
        labelContainer.pack();
        labelItem.setControl(labelContainer);
        labelItem.setWidth(width);
    }

    public void adaptNumberalInput(Control c, final boolean minusPermitted) {
        c.addListener(SWT.KeyDown, new Listener() {

            public void handleEvent(Event event) {
                if (event.character < 0x20 || event.character > 0x7e) {
                    event.doit = true;
                    return;
                }
                if ((event.character >= '0' && event.character <= '9')
                        || (minusPermitted && event.character == '-')) {
                    event.doit = true;
                } else {
                    event.doit = false;
                }
            }

        });
    }

    public void dispose() {
        if (getColors() != null) {
            super.dispose();
        }
    }

}