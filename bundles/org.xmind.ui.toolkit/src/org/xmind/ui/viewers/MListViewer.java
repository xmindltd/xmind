/* ******************************************************************************
 * Copyright (c) 2006-2015 XMind Ltd. and others.
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
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

public class MListViewer extends StructuredViewer {

    private class MListLayout extends Layout {

        @Override
        protected Point computeSize(Composite composite, int wHint, int hHint,
                boolean flushCache) {
            IListLayout layout = getListLayout();
            if (layout != null)
                return layout.computeSize(MListViewer.this, composite, wHint,
                        hHint, flushCache);
            return new Point(0, 0);
        }

        @Override
        protected void layout(Composite composite, boolean flushCache) {
            IListLayout layout = getListLayout();
            if (layout != null)
                layout.layout(MListViewer.this, composite, flushCache);
        }

    }

    private Composite composite;

    private Listener eventListener = new Listener() {
        public void handleEvent(Event event) {
            if (event.type == SWT.Selection) {
                handleItemSelect(event);
            } else if (event.type == SWT.DefaultSelection) {
                handleOpen(new SelectionEvent(event));
            } else if (event.type == SWT.MouseDown) {
                handleMouseDown(event);
            } else if (event.type == SWT.KeyDown) {
                handleKeyDown(event);
            } else if (event.type == SWT.Traverse) {
                handleKeyTraversed(event);
            }
        }
    };

    private int keyDownSelectionEventId = 0;

    public MListViewer(Composite parent, int style) {
        this.composite = new Composite(parent, style);

        this.composite.setLayout(new MListLayout());

        this.composite.addListener(SWT.Selection, eventListener);
        this.composite.addListener(SWT.DefaultSelection, eventListener);
        this.composite.addListener(SWT.MouseDown, eventListener);
        this.composite.addListener(SWT.KeyDown, eventListener);
        this.composite.addListener(SWT.Traverse, eventListener);

        hookControl(this.composite);
    }

//    public void setMargins(int left, int top, int right, int bottom) {
//        GridLayout layout = ((GridLayout) this.composite.getLayout());
//        layout.marginLeft = left;
//        layout.marginTop = top;
//        layout.marginRight = right;
//        layout.marginBottom = bottom;
//        this.composite.layout(true);
//    }
//
//    public void setSpacing(int spacing) {
//        GridLayout layout = ((GridLayout) this.composite.getLayout());
//        layout.horizontalSpacing = spacing;
//        layout.verticalSpacing = spacing;
//        this.composite.layout(true);
//    }
//
//    public void setItemHints(int wHint, int hHint) {
//        if (wHint == itemHints.x && hHint == itemHints.y)
//            return;
//
//        itemHints.x = wHint;
//        itemHints.y = hHint;
//
//        for (Control item : getItems()) {
//            ((GridData) item.getLayoutData()).widthHint = itemHints.x;
//            ((GridData) item.getLayoutData()).heightHint = itemHints.y;
//        }
//        this.composite.layout(true);
//    }

    @Override
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof IListRenderer);
        super.setLabelProvider(labelProvider);
    }

    @Override
    protected Widget doFindInputItem(Object element) {
        if (element != null && equals(element, getRoot())) {
            return getControl();
        }
        return null;
    }

    @Override
    protected Widget doFindItem(Object element) {
        Control[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            Control item = items[i];
            Object data = item.getData();
            if (data != null && equals(data, element)) {
                return item;
            }
        }
        return null;
    }

    @Override
    protected void doUpdateItem(Widget widget, Object element,
            boolean fullMap) {
        if (widget instanceof Control) {
            Control item = (Control) widget;
            // remember element we are showing
            if (fullMap) {
                associate(element, item);
            } else {
                Object data = item.getData();
                if (data != null) {
                    unmapElement(data, item);
                }
                item.setData(element);
                mapElement(element, item);
            }

            IListRenderer renderer = (IListRenderer) getLabelProvider();
            if (renderer != null) {
                renderer.updateListItem(this, element, item);
            }

            if (item.isDisposed()) {
                unmapElement(element, item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected List getSelectionFromWidget() {
        List list = new ArrayList();
        IListRenderer renderer = (IListRenderer) getLabelProvider();
        if (renderer == null)
            return list;

        Control[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            Control item = items[i];
            int state = renderer.getListItemState(this, item);
            if ((state & IListRenderer.STATE_SELECTED) != 0) {
                Object e = item.getData();
                if (e != null) {
                    list.add(e);
                }
            }
        }
        return list;
    }

    @Override
    protected void internalRefresh(Object element) {
        if (element == null || equals(element, getRoot())) {
            internalRefreshAll(element);
        } else {
            Widget item = findItem(element);
            if (item != null) {
                updateItem(item, element);
            }
        }
    }

    private void internalRefreshAll(Object inputElement) {
        Object[] elements = getSortedChildren(inputElement);
        List<Control> items = new ArrayList<Control>(Arrays.asList(getItems()));

        Control item;
        Object element;
        int itemIndex;
        int i;
        for (i = 0; i < elements.length; i++) {
            element = elements[i];

            // search for matching item
            itemIndex = -1;
            for (int j = i; j < items.size(); j++) {
                item = items.get(j);
                if (equals(element, item.getData())) {
                    itemIndex = j;
                    break;
                }
            }

            if (itemIndex < 0) {
                // item not found, create a new one
                item = createItem(element);
                Assert.isTrue(item != null && !item.isDisposed());
                items.add(i, item);
                if (i == 0) {
                    item.moveAbove(null);
                } else {
                    item.moveBelow(items.get(i - 1));
                }
            } else {
                item = items.get(itemIndex);
                if (itemIndex > i) {
                    // item is after element, move it
                    if (i == 0) {
                        item.moveAbove(null);
                    } else {
                        item.moveBelow(items.get(i - 1));
                    }
                    items.remove(itemIndex);
                    items.add(i, item);
                }
            }

            IListRenderer renderer = (IListRenderer) getLabelProvider();
            if (renderer != null) {
                renderer.updateListItem(this, element, item);
            }

        }

        for (; i < items.size(); i++) {
            // remove unused items
            item = items.get(i);
            disassociate(item);
            IListLayout layout = getListLayout();
            if (layout != null)
                layout.itemRemoved(this, composite, item);
            item.dispose();
        }

        composite.layout(true);
    }

    private Control createItem(Object element) {
        Control item;

        IListRenderer renderer = (IListRenderer) getLabelProvider();
        if (renderer != null) {
            item = renderer.createListItemForElement(this, composite, element);
        } else {
            item = new Composite(composite, SWT.NONE);
        }

        IListLayout layout = getListLayout();
        if (layout != null)
            layout.itemAdded(this, composite, item);

        associate(element, item);

        return item;
    }

    private IListLayout getListLayout() {
        IListRenderer renderer = (IListRenderer) getLabelProvider();
        return renderer.getListLayout(this);
    }

    @Override
    public void reveal(Object element) {
    }

    @Override
    protected void setSelectionToWidget(List list, boolean reveal) {
        IListRenderer renderer = (IListRenderer) getLabelProvider();
        if (renderer == null)
            return;

        @SuppressWarnings("unchecked")
        List<Object> elementsToSelect = new ArrayList<Object>(list);
        Control[] items = getItems();
        for (int i = 0; i < items.length; i++) {
            Control item = items[i];
            Object data = item.getData();
            int elementIndex = -1;
            if (data != null) {
                for (int j = 0; j < elementsToSelect.size(); j++) {
                    if (equals(data, elementsToSelect.get(j))) {
                        elementIndex = j;
                    }
                }
            }
            int state = renderer.getListItemState(this, item);
            if (elementIndex < 0) {
                state &= ~IListRenderer.STATE_SELECTED;
            } else {
                state |= IListRenderer.STATE_SELECTED;
                elementsToSelect.remove(elementIndex);
            }
            renderer.setListItemState(this, item, state);
        }
    }

    @Override
    public Control getControl() {
        return this.composite;
    }

    /**
     * Associates the given element with the given widget. Sets the given item's
     * data to be the element, and maps the element to the item in the element
     * map (if enabled).
     *
     * @param element
     *            the element
     * @param item
     *            the widget
     */
    protected void associate(Object element, Control item) {
        Object data = item.getData();
        if (data != element) {
            if (data != null) {
                disassociate(item);
            }
            item.setData(element);
            mapElement(element, item);
        } else {
            // Always map the element, even if data == element,
            // since unmapAllElements() can leave the map inconsistent
            // See bug 2741 for details.
            mapElement(element, item);
        }
    }

    /**
     * Disassociates the given SWT item from its corresponding element. Sets the
     * item's data to <code>null</code> and removes the element from the element
     * map (if enabled).
     *
     * @param item
     *            the widget
     */
    protected void disassociate(Control item) {
        Object element = item.getData();
        Assert.isNotNull(element);
        //Clear the map before we clear the data
        unmapElement(element, item);
        item.setData(null);
    }

    /**
     * Returns the element with the given index from this list viewer. Returns
     * <code>null</code> if the index is out of range.
     * 
     * @param index
     *            the zero-based index
     * @return the element at the given index, or <code>null</code> if the index
     *         is out of range
     */
    public Object getElementAt(int index) {
        Control[] items = getItems();
        if (index >= 0 && index < items.length) {
            return items[index].getData();
        }
        return null;
    }

    protected Control[] getItems() {
        return this.composite.getChildren();
    }

    @Override
    protected void inputChanged(final Object input, Object oldInput) {
        super.inputChanged(input, oldInput);
        preservingSelection(new Runnable() {
            public void run() {
                final Control control = getControl();
                control.setRedraw(false);
                try {
                    internalRefreshAll(input);
                } finally {
                    control.setRedraw(true);
                }
            }
        });
    }

    private void handleItemSelect(final Event event) {
        handleSelect(new SelectionEvent(event));

        if (event.detail == SWT.ARROW_DOWN || event.detail == SWT.ARROW_UP
                || event.detail == SWT.ARROW_LEFT
                || event.detail == SWT.ARROW_RIGHT) {
            keyDownSelectionEventId++;
            final int id = keyDownSelectionEventId;
            Display.getCurrent().timerExec(OpenStrategy.getPostSelectionDelay(),
                    new Runnable() {
                        public void run() {
                            if (id != keyDownSelectionEventId)
                                return;

                            handlePostSelect(new SelectionEvent(event));
                        }
                    });
        } else {
            Display.getCurrent().asyncExec(new Runnable() {
                public void run() {
                    handlePostSelect(new SelectionEvent(event));
                }
            });
        }
    }

    private void handleMouseDown(Event event) {
        getControl().setFocus();
    }

    private void handleKeyTraversed(Event event) {
        switch (event.detail) {
        case SWT.TRAVERSE_ARROW_NEXT:
            selectNextItem();
            event.doit = false;
            return;
        case SWT.TRAVERSE_ARROW_PREVIOUS:
            selectPreviousItem();
            event.doit = false;
            return;
        case SWT.TRAVERSE_PAGE_NEXT:
        case SWT.TRAVERSE_PAGE_PREVIOUS:
        case SWT.TRAVERSE_RETURN:
            event.doit = false;
            return;
        }
        event.doit = true;
    }

    private void handleKeyDown(Event event) {
        if (event.character == '\r') {
            handleOpen(new SelectionEvent(event));
        }
    }

    private void selectNextItem() {
        IListRenderer renderer = (IListRenderer) getLabelProvider();
        if (renderer == null)
            return;

        Control[] items = getItems();
        if (items.length == 0)
            return;

        Control nextItem = null;
        for (int i = items.length - 1; i >= 0; i--) {
            Control item = items[i];
            int state = renderer.getListItemState(this, item);
            if ((state & IListRenderer.STATE_SELECTED) != 0)
                break;
            nextItem = item;
        }

        if (nextItem == null) {
            nextItem = items[items.length - 1];
        }

        selectSingle(renderer, items, nextItem);
    }

    private void selectPreviousItem() {
        IListRenderer renderer = (IListRenderer) getLabelProvider();
        if (renderer == null)
            return;

        Control[] items = getItems();
        if (items.length == 0)
            return;

        Control prevItem = null;
        for (int i = 0; i < items.length; i++) {
            Control item = items[i];
            int state = renderer.getListItemState(this, item);
            if ((state & IListRenderer.STATE_SELECTED) != 0)
                break;
            prevItem = item;
        }

        if (prevItem == null) {
            prevItem = items[0];
        }
        selectSingle(renderer, items, prevItem);
    }

    private void selectSingle(IListRenderer renderer, Control[] items,
            Control itemToSelect) {
        for (int i = 0; i < items.length; i++) {
            Control item = items[i];
            int state = renderer.getListItemState(this, item);
            if (item != itemToSelect) {
                state &= ~IListRenderer.STATE_SELECTED;
            } else {
                state |= IListRenderer.STATE_SELECTED;
            }
            renderer.setListItemState(this, item, state);
        }
    }

}
