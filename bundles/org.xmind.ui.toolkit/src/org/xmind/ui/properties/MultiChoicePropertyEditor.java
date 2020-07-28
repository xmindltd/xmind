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
/**
 * 
 */
package org.xmind.ui.properties;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.xmind.ui.dialogs.PopupFilteredList;
import org.xmind.ui.dialogs.PopupFilteredList.IElementCreator;
import org.xmind.ui.dialogs.PopupFilteredList.PatternFilter;

/**
 * @author Frank Shaka
 */
public class MultiChoicePropertyEditor extends PropertyEditor {

    private boolean filterable;

    private boolean allowsCustomizedValues;

    private Object input = null;

    private IStructuredContentProvider contentProvider = null;

    private ILabelProvider labelProvider = null;

    private Menu menu = null;

    private PopupFilteredList filterableList = null;

    private PatternFilter patternFilter = null;

    private IElementCreator elementCreator = null;

    private Object selectedElement = null;

    private long stamp_valueChangedByMenuItemSelection = 0;

    private long stamp_valueChangedByListSelection = 0;

    private Listener menuItemListener = new Listener() {
        public void handleEvent(Event event) {
            final long stamp = System.currentTimeMillis();
            stamp_valueChangedByMenuItemSelection = stamp;
            Display.getCurrent().timerExec(50, new Runnable() {
                public void run() {
                    if (stamp_valueChangedByMenuItemSelection == stamp)
                        stamp_valueChangedByMenuItemSelection = 0;
                }
            });
            Object element = event.widget.getData();
            changeValue(element);
            fireApplyEditorValue();
        }
    };

    /**
     * 
     */
    public MultiChoicePropertyEditor() {
        this(false, false);
    }

    public MultiChoicePropertyEditor(boolean filterable,
            boolean allowsCustomizedValues) {
        this.filterable = filterable;
        this.allowsCustomizedValues = allowsCustomizedValues;
    }

    public void setInput(Object input) {
        this.input = input;
    }

    public void setContentProvider(IStructuredContentProvider contentProvider) {
        this.contentProvider = contentProvider;
    }

    public void setLabelProvider(ILabelProvider labelProvider) {
        if (this.labelProvider != null) {
            this.labelProvider.dispose();
        }
        this.labelProvider = labelProvider;
    }

    public void setPatternFilter(PatternFilter patternFilter) {
        this.patternFilter = patternFilter;
    }

    public void setElementCreator(IElementCreator elementCreator) {
        this.elementCreator = elementCreator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyEditor#createControl(org.eclipse.swt.
     * widgets.Composite)
     */
    @Override
    protected Control createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new FillLayout());
        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });
        return composite;
    }

    private void handleDispose() {
        if (labelProvider != null) {
            labelProvider.dispose();
            labelProvider = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyEditor#setValueToWidget(java.lang.Object)
     */
    @Override
    protected void setValueToWidget(Object value) {
        this.selectedElement = value;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.ui.properties.PropertyEditor#activateWidget()
     */
    @Override
    protected void activateWidget() {
        super.activateWidget();
        if (filterable) {
            showFilterableList();
        } else {
            showMenu();
        }
    }

    public void setFocus() {
        if (!isShowingWidget()) {
            super.setFocus();
        }
    }

    private boolean isShowingWidget() {
        if (menu != null && !menu.isDisposed() && menu.isVisible())
            return true;
        if (filterableList != null && filterableList.getShell() != null
                && !filterableList.getShell().isDisposed()
                && filterableList.getShell().isVisible())
            return true;
        return false;
    }

    private void showMenu() {
        if (getControl() == null || getControl().isDisposed())
            return;

        if (menu == null || menu.isDisposed()) {
            menu = createMenu();
        }
        updateMenu(menu);

        if (menu.getItemCount() == 0)
            return;

        menu.setLocation(calculateMenuLocation());
        menu.setVisible(true);
    }

    private Menu createMenu() {
        Menu menu = new Menu(getControl().getShell(), SWT.POP_UP);
        menu.addMenuListener(new MenuListener() {
            public void menuShown(MenuEvent e) {
                stamp_valueChangedByMenuItemSelection = 0;
            }

            public void menuHidden(MenuEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        if (getControl() == null || getControl().isDisposed())
                            return;

                        if (stamp_valueChangedByMenuItemSelection == 0) {
                            fireCancelEditing();
                        }
                        stamp_valueChangedByMenuItemSelection = 0;
                    }
                });
            }
        });
        return menu;
    }

    private void updateMenu(Menu menu) {
        Object[] elements = getElements();
        MenuItem[] oldItems = menu.getItems();
        for (int i = 0; i < elements.length; i++) {
            Object element = elements[i];
            MenuItem item = i < oldItems.length ? oldItems[i] : null;
            if (item != null) {
                Object data = item.getData();
                if (element != data
                        && (element == null || element.equals(data))) {
                    item.dispose();
                    item = null;
                }
            }
            if (item == null) {
                item = new MenuItem(menu, SWT.RADIO, i);
                item.setData(element);
                item.addListener(SWT.Selection, menuItemListener);
            }
            item.setText(getText(element));
            item.setImage(getImage(element));
            item.setSelection(element == selectedElement
                    || (element != null && element.equals(selectedElement)));
        }
    }

    private void showFilterableList() {
        if (getControl() == null || getControl().isDisposed())
            return;

        if (filterableList == null) {
            filterableList = new PopupFilteredList(getControl().getShell(),
                    SWT.RESIZE);
            filterableList.setPermitsUnprovidedElement(allowsCustomizedValues);
            filterableList.setElementCreator(elementCreator);
            filterableList.addOpenListener(new IOpenListener() {
                public void open(OpenEvent event) {
                    final long stamp = System.currentTimeMillis();
                    stamp_valueChangedByListSelection = stamp;
                    Display.getCurrent().timerExec(50, new Runnable() {
                        public void run() {
                            if (stamp_valueChangedByListSelection == stamp)
                                stamp_valueChangedByListSelection = 0;
                        }
                    });
                    Object element = ((IStructuredSelection) event
                            .getSelection()).getFirstElement();
                    changeValue(element);
                    fireApplyEditorValue();
                }
            });
        }
        filterableList.setPatternFilter(patternFilter);
        filterableList.setContentProvider(new IStructuredContentProvider() {
            public void inputChanged(Viewer viewer, Object oldInput,
                    Object newInput) {
            }

            public void dispose() {
            }

            public Object[] getElements(Object inputElement) {
                return MultiChoicePropertyEditor.this.getElements();
            }
        });
        filterableList.setLabelProvider(new LabelProvider() {
            public Image getImage(Object element) {
                return MultiChoicePropertyEditor.this.getImage(element);
            }

            public String getText(Object element) {
                return MultiChoicePropertyEditor.this.getText(element);
            }
        });
        filterableList.setDefaultSelection(selectedElement);
        filterableList.setInput(input);
        filterableList.setBoundsReference(calculateListReferenceBounds());
        filterableList.create();
        filterableList.getShell().addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        if (getControl() == null || getControl().isDisposed())
                            return;

                        if (stamp_valueChangedByListSelection == 0) {
                            fireCancelEditing();
                        }
                        stamp_valueChangedByListSelection = 0;
                    }
                });
            }
        });
        stamp_valueChangedByListSelection = 0;
        filterableList.open();
    }

    private String getText(Object element) {
        if (labelProvider != null)
            return labelProvider.getText(element);
        if (element == null)
            return ""; //$NON-NLS-1$
        return element.toString();
    }

    private Image getImage(Object element) {
        if (labelProvider != null)
            return labelProvider.getImage(element);
        return null;
    }

    private Object[] getElements() {
        if (contentProvider != null) {
            return contentProvider.getElements(input);
        } else {
            return new Object[0];
        }
    }

    private Point calculateMenuLocation() {
        return getControl().toDisplay(0, 0);
    }

    private Rectangle calculateListReferenceBounds() {
        Rectangle r = getControl().getBounds();
        Point loc = getControl().toDisplay(0, 0);
        return new Rectangle(loc.x, loc.y - r.height, r.width, r.height);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyEditor#setBackground(org.eclipse.swt.
     * graphics.Color)
     */
    @Override
    public void setBackground(Color color) {
        super.setBackground(color);
        if (getControl() != null && !getControl().isDisposed()) {
            getControl().setBackground(color);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyEditor#setForeground(org.eclipse.swt.
     * graphics.Color)
     */
    @Override
    public void setForeground(Color color) {
        super.setForeground(color);
        if (getControl() != null && !getControl().isDisposed()) {
            getControl().setForeground(color);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.ui.properties.PropertyEditor#setFont(org.eclipse.swt.graphics
     * .Font)
     */
    @Override
    public void setFont(Font font) {
        super.setFont(font);
        if (getControl() != null && !getControl().isDisposed()) {
            getControl().setFont(font);
        }
    }

}
