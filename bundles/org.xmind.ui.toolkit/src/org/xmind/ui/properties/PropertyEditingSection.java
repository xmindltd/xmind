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

import java.beans.PropertyChangeSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

public class PropertyEditingSection {

    public static final String PROP_TITLE = "title"; //$NON-NLS-1$

    public static final String PROP_EXPANDED = "expanded"; //$NON-NLS-1$

    private Composite composite = null;

    private Composite client = null;

    private Composite title = null;

    private Label titleLabel = null;

//    private Composite chevron = null;

    private Control separator = null;

    private boolean expanded = true;

    private String titleText = ""; //$NON-NLS-1$

    private Color titleColor = null;

    private PropertyChangeSupport eventSupport = new PropertyChangeSupport(this);

    public PropertyEditingSection(Composite parent) {
        composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        Control titleControl = createTitle(composite);
        GridData titleLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        titleLayoutData.widthHint = SWT.DEFAULT;
        titleLayoutData.heightHint = SWT.DEFAULT;
        titleControl.setLayoutData(titleLayoutData);

        Control separatorControl = createSeparator(composite);
        GridData separatorLayoutData = new GridData(SWT.FILL, SWT.FILL, true,
                false);
        separatorLayoutData.widthHint = SWT.DEFAULT;
        separatorLayoutData.heightHint = SWT.DEFAULT;
        separatorControl.setLayoutData(separatorLayoutData);

        Control bodyControl = createClientBody(composite);
        GridData bodyLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        bodyLayoutData.widthHint = SWT.DEFAULT;
        bodyLayoutData.heightHint = SWT.DEFAULT;
        bodyControl.setLayoutData(bodyLayoutData);
    }

    private Control createTitle(Composite parent) {
        title = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 5;
        gridLayout.marginHeight = 5;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 3;
        title.setLayout(gridLayout);

//        Control chevronControl = createChevron(title);
//        GridData chevronLayoutData = new GridData(SWT.FILL, SWT.FILL, false,
//                true);
//        chevronLayoutData.widthHint = SWT.DEFAULT;
//        chevronLayoutData.heightHint = SWT.DEFAULT;
//        chevronControl.setLayoutData(chevronLayoutData);

        Control labelControl = createTitleLabel(title);
        GridData labelLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
        labelLayoutData.widthHint = SWT.DEFAULT;
        labelLayoutData.heightHint = SWT.DEFAULT;
        labelControl.setLayoutData(labelLayoutData);

        enableClickListener(title, new Listener() {
            public void handleEvent(Event event) {
//                toggleExpanded();
            }
        });

        return title;
    }

//    private Control createChevron(Composite parent) {
//        chevron = new Composite(parent, SWT.NO_FOCUS);
//        chevron.setLayout(new Layout() {
//            protected void layout(Composite composite, boolean flushCache) {
//            }
//
//            protected Point computeSize(Composite composite, int wHint,
//                    int hHint, boolean flushCache) {
//                return new Point(12, hHint < 0 ? 12 : hHint);
//            }
//        });
//        chevron.addPaintListener(new PaintListener() {
//            public void paintControl(PaintEvent e) {
//                Rectangle bounds = chevron.getBounds();
//                paintChevron(e.display, e.gc, 0, 0, bounds.width,
//                        bounds.height, chevron.getForeground());
//            }
//        });
//        return chevron;
//    }

    private Control createTitleLabel(Composite parent) {
        titleLabel = new Label(parent, SWT.WRAP);
        titleLabel.setText(titleText);
        return titleLabel;
    }

    private Control createSeparator(Composite parent) {
        separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        return separator;
    }

    private Control createClientBody(Composite parent) {
        client = new Composite(parent, SWT.NONE);
        return client;
    }

    private void enableClickListener(Control control, Listener listener) {
        control.addListener(SWT.MouseDown, listener);
        control.setCursor(control.getDisplay().getSystemCursor(SWT.CURSOR_HAND));
        if (control instanceof Composite) {
            Control[] children = ((Composite) control).getChildren();
            for (int i = 0; i < children.length; i++) {
                enableClickListener(children[i], listener);
            }
        }
    }

    protected void paintChevron(Display display, GC gc, int x, int y,
            int width, int height, Color color) {
        if (color != null) {
            gc.setBackground(color);
        } else {
            gc.setBackground(display
                    .getSystemColor(SWT.COLOR_WIDGET_FOREGROUND));
        }
        int cx = x + width / 2, cy = y + height / 2;
        if (isExpanded()) {
            gc.fillPolygon(new int[] { cx - 5, cy - 3, cx + 3, cy - 3, cx - 1,
                    cy + 7 });
        } else {
            gc.fillPolygon(new int[] { cx - 4, cy - 4, cx + 6, cy, cx - 4,
                    cy + 4 });
        }
    }

    public Control getControl() {
        return composite;
    }

    public Composite getClient() {
        return client;
    }

    public void setBackground(Color color) {
        if (composite == null || composite.isDisposed())
            return;
        composite.setBackground(color);
        client.setBackground(color);
        title.setBackground(color);
        titleLabel.setBackground(color);
//        chevron.setBackground(color);
        separator.setBackground(color);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void toggleExpanded() {
        setExpanded(!isExpanded());
    }

    public void setExpanded(boolean expanded) {
        boolean oldExpanded = isExpanded();
        if (oldExpanded == expanded)
            return;

        this.expanded = expanded;

        if (composite == null || composite.isDisposed())
            return;

        client.setVisible(expanded);
        separator.setVisible(expanded);
        ((GridData) client.getLayoutData()).exclude = !expanded;
        ((GridData) separator.getLayoutData()).exclude = !expanded;
        composite.layout(true);
//        chevron.redraw();
        eventSupport.firePropertyChange(PROP_EXPANDED, oldExpanded, expanded);
    }

    public String getTitleText() {
        return titleText;
    }

    public void setTitleText(String titleText) {
        this.titleText = titleText == null ? "" : titleText; //$NON-NLS-1$
        if (titleLabel == null || titleLabel.isDisposed())
            return;
        if ("".equals(this.titleText)) { //$NON-NLS-1$
            titleLabel
                    .setText(Messages.PropertyEditingSection_UntitledCategory);
            titleLabel.setForeground(Display.getCurrent().getSystemColor(
                    SWT.COLOR_GRAY));
        } else {
            titleLabel.setText(getTitleText());
            titleLabel.setForeground(titleColor);
        }
    }

    public void setTitleColor(Color color) {
        this.titleColor = color;
        if (titleLabel != null && !titleLabel.isDisposed()) {
            if ("".equals(titleText)) { //$NON-NLS-1$
                titleLabel.setForeground(Display.getCurrent().getSystemColor(
                        SWT.COLOR_GRAY));
            } else {
                titleLabel.setForeground(color);
            }
        }
//        if (chevron != null && !chevron.isDisposed()) {
//            chevron.setForeground(color);
//        }
    }

    public void setTitleFont(Font font) {
        if (titleLabel != null && !titleLabel.isDisposed()) {
            titleLabel.setFont(font);
        }
    }

    public PropertyChangeSupport getEventSupport() {
        return eventSupport;
    }

}
