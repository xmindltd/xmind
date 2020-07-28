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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;

public class CheckListViewer extends StructuredViewer implements ICheckable {

    protected static class CheckItem {

        private CheckListViewer listViewer;

        private Object data;

        private Composite bar;

        private Button checkbox;

        private Label imageLabel;

        private Label textLabel;

        private boolean selected;

        private boolean internalSetChecked = false;

        public CheckItem(CheckListViewer listViewer, Composite parent) {
            this.listViewer = listViewer;
            bar = new Composite(parent, SWT.NO_FOCUS);
            bar.setBackground(parent.getBackground());
            bar.setData(this);
            GridLayoutFactory.fillDefaults().numColumns(3)
                    .extendedMargins(5, 5, 1, 1).applyTo(bar);

            checkbox = new Button(bar, SWT.CHECK);
            checkbox.setText(""); //$NON-NLS-1$
            checkbox.setBackground(bar.getBackground());
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
                    .applyTo(checkbox);

            imageLabel = new Label(bar, SWT.NONE);
            imageLabel.setImage(null);
            imageLabel.setBackground(bar.getBackground());
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
                    .applyTo(imageLabel);

            textLabel = new Label(bar, SWT.NONE);
            textLabel.setText(""); //$NON-NLS-1$
            textLabel.setBackground(bar.getBackground());
            GridDataFactory.fillDefaults().grab(true, false)
                    .align(SWT.FILL, SWT.CENTER).applyTo(textLabel);

            addControlListeners();
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public CheckListViewer getListViewer() {
            return listViewer;
        }

        public Control getControl() {
            return bar;
        }

        protected Composite getBarComposite() {
            return bar;
        }

        protected void addControlListeners() {
            Listener listener = new Listener() {
                public void handleEvent(Event event) {
                    int type = event.type;
                    switch (type) {
                    case SWT.Paint:
                        paintBackground(event.gc);
                        break;
                    case SWT.MouseDown:
                        if (event.widget == checkbox) {
                            selectSingle();
                        } else {
                            userSelect(event);
                            if ((OpenStrategy.getOpenMethod()
                                    & OpenStrategy.SINGLE_CLICK) != 0) {
                                userOpen(event);
                            }
                        }
                        break;
                    case SWT.MouseDoubleClick:
                        if (OpenStrategy
                                .getOpenMethod() == OpenStrategy.DOUBLE_CLICK) {
                            userOpen(event);
                        }
                        break;
                    case SWT.MouseHover:
                        if ((OpenStrategy.getOpenMethod()
                                & OpenStrategy.SELECT_ON_HOVER) != 0) {
                            userSelect(event);
                        }
                        break;
                    case SWT.Selection:
                        if (!internalSetChecked) {
                            userCheck();
                        }
                        break;
                    }
                }
            };
            bar.addListener(SWT.Paint, listener);

            bar.addListener(SWT.MouseDown, listener);
            bar.addListener(SWT.MouseDoubleClick, listener);
            bar.addListener(SWT.MouseHover, listener);

            imageLabel.addListener(SWT.MouseDown, listener);
            imageLabel.addListener(SWT.MouseDoubleClick, listener);
            imageLabel.addListener(SWT.MouseHover, listener);

            textLabel.addListener(SWT.MouseDown, listener);
            textLabel.addListener(SWT.MouseDoubleClick, listener);
            textLabel.addListener(SWT.MouseHover, listener);

            checkbox.addListener(SWT.MouseDown, listener);
            checkbox.addListener(SWT.Selection, listener);
        }

        protected void userCheck() {
            getListViewer().fireChecked(this);
        }

        protected void paintBackground(GC gc) {
            if (isSelected()) {
                Rectangle bounds = bar.getBounds();
                bounds.x = 0;
                bounds.y = 0;
                //Rectangle labelBounds = label.getBounds();
                gc.setBackground(getSelectionBackground());
                gc.fillRectangle(bounds);
                //gc.fillRectangle( labelBounds.x, bounds.y, bounds.x + bounds.width - labelBounds.x, bounds.height );
            }
        }

        protected void userSelect(Event e) {
            selectSingle();
            getListViewer().getControl().setFocus();
            getListViewer().handleSelect(new SelectionEvent(e));
        }

        protected void userOpen(Event e) {
            selectSingle();
            getListViewer().getControl().setFocus();
            getListViewer().handleOpen(new SelectionEvent(e));
        }

        protected void selectSingle() {
            getListViewer().setSelectedItem(this);
        }

        public Button getCheckbox() {
            return checkbox;
        }

        public Label getLabel() {
            return textLabel;
        }

        public boolean isChecked() {
            return checkbox.getSelection();
        }

        public void setChecked(boolean checked) {
            internalSetChecked = true;
            checkbox.setSelection(checked);
            internalSetChecked = false;
        }

        public String getText() {
            return textLabel.getText();
        }

        public void setText(String text) {
            textLabel.setText(text);
        }

        public Image getImage() {
            return imageLabel.getImage();
        }

        public void setImage(Image image) {
            imageLabel.setImage(image);
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelection(boolean selected) {
            if (selected == this.selected)
                return;
            this.selected = selected;
            bar.redraw();
            textLabel.redraw();
            if (selected) {
                checkbox.setBackground(getSelectionBackground());
                imageLabel.setBackground(getSelectionBackground());
                textLabel.setBackground(getSelectionBackground());
                textLabel.setForeground(getSelectionTextColor());
            } else {
                Color background = bar.getBackground();
                checkbox.setBackground(background);
                imageLabel.setBackground(background);
                textLabel.setBackground(background);
                textLabel.setForeground(null);
            }
        }

        public void dispose() {
            bar.dispose();
        }

        public boolean isDisposed() {
            return bar.isDisposed();
        }
    }

    private static Color selectionBackground = null;

    private static Color selectionTextColor = null;

    protected static Color getSelectionBackground() {
        if (selectionBackground == null) {
            selectionBackground = ColorConstants.lightBlue;
        }
        return selectionBackground;
    }

    protected static Color getSelectionTextColor() {
        if (selectionTextColor == null) {
            selectionTextColor = Display.getCurrent()
                    .getSystemColor(SWT.COLOR_BLACK);
        }
        return selectionTextColor;
    }

    private Composite list;

    /**
     * A list of viewer elements (element type: <code>Object</code>).
     */
    private List<Object> listMap = new ArrayList<Object>();

    private List<CheckItem> items = new ArrayList<CheckItem>();

    private List<ICheckStateListener> checkStateListeners = new ArrayList<ICheckStateListener>();

    private CheckItem selectedItem = null;

    public CheckListViewer(Composite parent, int style) {
        list = new Composite(parent, style) {
            @Override
            public boolean setFocus() {
                return super.forceFocus();
            }

            @Override
            public Point computeSize(int wHint, int hHint, boolean changed) {
                Point size;
                if (getItemCount() == 0) {
                    size = new Point(0, 0);
                    if (wHint != SWT.DEFAULT)
                        size.x = wHint;
                    if (hHint != SWT.DEFAULT)
                        size.y = hHint;
                    Rectangle trim = computeTrim(0, 0, size.x, size.y);
                    size = new Point(trim.width, trim.height);
                } else {
                    size = super.computeSize(wHint, hHint, changed);
                }
                return size;
            }
        };
//        list.addListener( SWT.MouseDown, new Listener() {
//            public void handleEvent( Event event ) {
//            }
//        } );
        list.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event event) {
                int keyCode = event.keyCode;
                int stateMask = event.stateMask;
                if (keyCode == SWT.TAB) {
                    if (stateMask == SWT.MOD2) {
                        list.traverse(SWT.TRAVERSE_TAB_PREVIOUS);
                    } else if (stateMask == 0) {
                        list.traverse(SWT.TRAVERSE_TAB_NEXT);
                    }
                } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.CR)) {
                    CheckItem item = getSelectedItem();
                    if (item != null)
                        item.userOpen(event);
                } else if (SWTUtils.matchKey(stateMask, keyCode, 0, ' ')) {
                    if (selectedItem != null) {
                        selectedItem.setChecked(!selectedItem.isChecked());
                        fireChecked(selectedItem);
                    }
                } else if (SWTUtils.matchKey(stateMask, keyCode, 0,
                        SWT.ARROW_UP)) {
                    if (selectedItem != null) {
                        userSelectPrevious(selectedItem, event);
                    } else if (!items.isEmpty()) {
                        userSelectItem(0, event);
                    }
                } else if (SWTUtils.matchKey(stateMask, keyCode, 0,
                        SWT.ARROW_DOWN)) {
                    if (selectedItem != null) {
                        userSelectNext(selectedItem, event);
                    } else if (!items.isEmpty()) {
                        userSelectItem(0, event);
                    }
                }
            }
        });
        list.setTabList(new Control[0]);
        list.setBackground(parent.getBackground());
        GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(list);
        //hookControl( list );
        list.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose(e);
            }
        });
    }

    protected void userSelectPrevious(CheckItem item, Event event) {
        int index = items.indexOf(item);
        if (index >= 1) {
            index--;
            userSelectItem(index, event);
        }
    }

    private void userSelectItem(int index, Event event) {
        setSelectedItem(items.get(index));
        handleSelect(new SelectionEvent(event));
    }

    protected void userSelectNext(CheckItem item, Event event) {
        int index = items.indexOf(item);
        if (index < listGetItemCount() - 1) {
            index++;
            userSelectItem(index, event);
        }
    }

    protected void setSelectedItem(CheckItem item) {
        if (item == this.selectedItem)
            return;
        this.selectedItem = item;
        for (CheckItem it : items) {
            it.setSelection(it == item);
        }
    }

    protected CheckItem getSelectedItem() {
        return selectedItem;
    }

    @Override
    public Control getControl() {
        return list;
    }

    protected Composite getListComposite() {
        return list;
    }

    protected List<CheckItem> getItems() {
        return items;
    }

    public int getItemCount() {
        return listGetItemCount();
    }

    protected CheckItem listAdd(String string, Image image, int index) {
        CheckItem newItem = new CheckItem(this, list);
        GridDataFactory.fillDefaults().grab(true, false)
                .applyTo(newItem.getControl());
        newItem.setImage(image);
        newItem.setText(string);
        list.layout();
        if (index < 0 || index >= listGetItemCount()) {
            items.add(newItem);
        } else {
            CheckItem oldItem = items.get(index);
            items.add(index, newItem);
            newItem.getControl().moveAbove(oldItem.getControl());
        }
        return newItem;
    }

    protected void listDeselectAll() {
        setSelectedItem(null);
//        for ( CheckboxItem item : items ) {
//            item.setSelection( false );
//        }
    }

    protected int listGetItemCount() {
        return items.size();
    }

    protected int[] listGetSelectionIndices() {
        int total = listGetItemCount();
        int[] counts = new int[total];
        int num = 0;
        int j = 0;
        for (int i = 0; i < total; i++) {
            CheckItem item = items.get(i);
            if (item.isSelected()) {
                counts[j] = i;
                num++;
                j++;
            }
        }
        int[] ixs = new int[num];
        System.arraycopy(counts, 0, ixs, 0, num);
        return ixs;
    }

    protected CheckItem listRemove(int index) {
        if (index < 0 || index >= listGetItemCount())
            return null;
        CheckItem item = items.remove(index);
        if (item != null)
            item.dispose();
        return item;
    }

    protected void listRemoveAll() {
        for (CheckItem item : items) {
            item.dispose();
        }
        items.clear();
    }

    protected void listSetItem(int index, String string, Image image) {
        if (index < 0 || index >= listGetItemCount())
            return;
        CheckItem item = items.get(index);
        if (!item.isDisposed()) {
            item.setImage(image);
            item.setText(string);
        }
    }

//    protected void listSetItems( String[] labels ) {
//        if ( labels != null && labels.length > 0 ) {
//            for ( String label : labels ) {
//                listAdd( label, null, -1 );
//            }
//        }
//    }

    protected void listSetSelection(int[] ixs) {
        listDeselectAll();
        int total = listGetItemCount();
        for (int index : ixs) {
            if (index >= 0 && index < total) {
                CheckItem item = items.get(index);
                if (selectedItem == null)
                    selectedItem = item;
                item.setSelection(true);
            }
        }
    }

    protected void listShowSelection() {
    }

    public void reveal(Object element) {
    }

    public void addCheckStateListener(ICheckStateListener listener) {
        checkStateListeners.add(listener);
    }

    public boolean getChecked(Object element) {
        int index = getElementIndex(element);
        if (index >= 0) {
            CheckItem item = items.get(index);
            return !item.isDisposed() && item.isChecked();
        }
        return false;
    }

    public Object[] getCheckedElements() {
        int count = getItemCount();
        ArrayList<Object> checked = new ArrayList<Object>(count);
        for (int i = 0; i < count; i++) {
            CheckItem item = items.get(i);
            if (item.isChecked())
                checked.add(listMap.get(i));
        }
        return checked.toArray();
    }

//    private int findIndex( Object element ) {
//        int total = listGetItemCount();
//        for ( int i = 0; i < total; i++ ) {
//            if ( getElementAt( i ) == element )
//                return i;
//        }
//        return -1;
//    }

    public void removeCheckStateListener(ICheckStateListener listener) {
        checkStateListeners.remove(listener);
    }

    public boolean setChecked(Object element, boolean state) {
        int index = getElementIndex(element);
        if (index >= 0) {
            CheckItem item = items.get(index);
            if (!item.isDisposed())
                item.setChecked(state);
        }
        return false;
    }

//    protected Object findElementByCheckbox( Widget checkboxWidget ) {
//        int total = listGetItemCount();
//        for ( int i = 0; i < total; i++ ) {
//            CheckboxItem item = items.get( i );
//            if ( !item.isDisposed() ) {
//                if ( item.getCheckbox() == checkboxWidget ) {
//                    return getElementAt( i );
//                }
//            }
//        }
//        return null;
//    }

    protected void fireChecked(CheckItem item) {
        Object element = getElementAt(items.indexOf(item));
        if (element == null)
            return;
        final CheckStateChangedEvent event = new CheckStateChangedEvent(this,
                element, item.isChecked());
        for (final Object o : checkStateListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((ICheckStateListener) o).checkStateChanged(event);
                }
            });
        }
    }

    @Override
    protected void preservingSelection(Runnable updateCode) {
        Map<Object, Boolean> checkStateMap = new HashMap<Object, Boolean>();
        int total = listGetItemCount();
        for (int i = 0; i < total; i++) {
            Object element = getElementAt(i);
            boolean checked = items.get(i).isChecked();
            checkStateMap.put(element, checked);
        }
        super.preservingSelection(updateCode);
        total = listGetItemCount();
        for (int i = 0; i < total; i++) {
            Object element = getElementAt(i);
            Boolean checked = checkStateMap.get(element);
            if (checked != null) {
                items.get(i).setChecked(checked);
            }
        }
    }

    /**
     * Adds the given elements to this list viewer. If this viewer does not have
     * a sorter, the elements are added at the end in the order given; otherwise
     * the elements are inserted at appropriate positions.
     * <p>
     * This method should be called (by the content provider) when elements have
     * been added to the model, in order to cause the viewer to accurately
     * reflect the model. This method only affects the viewer, not the model.
     * </p>
     * 
     * @param elements
     *            the elements to add
     */
    public void add(Object[] elements) {
        assertElementsNotNull(elements);
        Object[] filtered = filter(elements);
        ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
        for (int i = 0; i < filtered.length; i++) {
            Object element = filtered[i];
            int ix = indexForElement(element);
            insertItem(labelProvider, element, ix);
        }
    }

    private void insertItem(ILabelProvider labelProvider, Object element,
            int index) {
        String label = getLabelProviderText(labelProvider, element);
        Image image = getImage(labelProvider, element);
        CheckItem item = listAdd(label, image, index);
        listMap.add(index, element);
        mapElement(element, item.getControl()); // must map it, since findItem only looks in map, if enabled
    }

    /**
     * Inserts the given element into this list viewer at the given position. If
     * this viewer has a sorter, the position is ignored and the element is
     * inserted at the correct position in the sort order.
     * <p>
     * This method should be called (by the content provider) when elements have
     * been added to the model, in order to cause the viewer to accurately
     * reflect the model. This method only affects the viewer, not the model.
     * </p>
     * 
     * @param element
     *            the element
     * @param position
     *            a 0-based position relative to the model, or -1 to indicate
     *            the last position
     * @since 3.3
     */
    public void insert(Object element, int position) {
        if (getComparator() != null || hasFilters()) {
            add(element);
            return;
        }

        insertItem((ILabelProvider) getLabelProvider(), element, position);
    }

    /**
     * Return the text for the element from the labelProvider. If it is null
     * then return the empty String.
     * 
     * @param labelProvider
     *            ILabelProvider
     * @param element
     * @return String. Return the emptyString if the labelProvider returns null
     *         for the text.
     * @since 3.1
     */
    private String getLabelProviderText(ILabelProvider labelProvider,
            Object element) {
        String text = labelProvider.getText(element);
        if (text == null) {
            return "";//$NON-NLS-1$
        }
        return text;
    }

    private Image getImage(ILabelProvider labelProvider, Object element) {
        return labelProvider.getImage(element);
    }

    /**
     * Adds the given element to this list viewer. If this viewer does not have
     * a sorter, the element is added at the end; otherwise the element is
     * inserted at the appropriate position.
     * <p>
     * This method should be called (by the content provider) when a single
     * element has been added to the model, in order to cause the viewer to
     * accurately reflect the model. This method only affects the viewer, not
     * the model. Note that there is another method for efficiently processing
     * the simultaneous addition of multiple elements.
     * </p>
     * 
     * @param element
     *            the element
     */
    public void add(Object element) {
        add(new Object[] { element });
    }

    /*
     * (non-Javadoc) Method declared on StructuredViewer. Since SWT.List doesn't
     * use items we always return the List itself.
     */
    protected Widget doFindInputItem(Object element) {
        if (element != null && equals(element, getRoot())) {
            return getControl();
        }
        return null;
    }

    /*
     * (non-Javadoc) Method declared on StructuredViewer. Since SWT.List doesn't
     * use items we always return the List itself.
     */
    protected Widget doFindItem(Object element) {
        if (element != null) {
            int index = getElementIndex(element);
            if (index >= 0) {
                return items.get(index).getControl();
            }
//            if (listMapContains(element)) {
//                return getControl();
//            }
        }
        return null;
    }

    /*
     * (non-Javadoc) Method declared on StructuredViewer.
     */
    protected void doUpdateItem(Widget data, Object element, boolean fullMap) {
        if (element != null) {
            int ix = getElementIndex(element);
            if (ix >= 0) {
                ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
                String label = getLabelProviderText(labelProvider, element);
                Image image = getImage(labelProvider, element);
                listSetItem(ix, label, image);
            }
        }
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
        if (index >= 0 && index < listMap.size()) {
            return listMap.get(index);
        }
        return null;
    }

    /**
     * The list viewer implementation of this <code>Viewer</code> framework
     * method returns the label provider, which in the case of list viewers will
     * be an instance of <code>ILabelProvider</code>.
     */
    public IBaseLabelProvider getLabelProvider() {
        return super.getLabelProvider();
    }

    /*
     * (non-Javadoc) Method declared on Viewer.
     */
    /*
     * (non-Javadoc) Method declared on StructuredViewer.
     */
    protected List getSelectionFromWidget() {
        int[] ixs = listGetSelectionIndices();
        List<Object> list = new ArrayList<Object>(ixs.length);
        for (int i = 0; i < ixs.length; i++) {
            Object e = getElementAt(ixs[i]);
            if (e != null) {
                list.add(e);
            }
        }
        return list;
    }

    /**
     * @param element
     *            the element to insert
     * @return the index where the item should be inserted.
     */
    protected int indexForElement(Object element) {
        ViewerComparator comparator = getComparator();
        if (comparator == null) {
            return listGetItemCount();
        }
        int count = listGetItemCount();
        int min = 0, max = count - 1;
        while (min <= max) {
            int mid = (min + max) / 2;
            Object data = listMap.get(mid);
            int compare = comparator.compare(this, data, element);
            if (compare == 0) {
                // find first item > element
                while (compare == 0) {
                    ++mid;
                    if (mid >= count) {
                        break;
                    }
                    data = listMap.get(mid);
                    compare = comparator.compare(this, data, element);
                }
                return mid;
            }
            if (compare < 0) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }
        return min;
    }

    /*
     * (non-Javadoc) Method declared on Viewer.
     */
    protected void inputChanged(Object input, Object oldInput) {
        listMap.clear();
        Object[] children = getSortedChildren(getRoot());
        int size = children.length;

        listRemoveAll();
        //String[] labels = new String[size];
        for (int i = 0; i < size; i++) {
            Object el = children[i];
            String label = getLabelProviderText(
                    (ILabelProvider) getLabelProvider(), el);
            Image image = getImage((ILabelProvider) getLabelProvider(), el);
            CheckItem item = listAdd(label, image, -1);
            listMap.add(el);
            mapElement(el, item.getControl()); // must map it, since findItem only looks in map, if enabled
        }
        //listSetItems(labels);
    }

    /*
     * (non-Javadoc) Method declared on StructuredViewer.
     */
    protected void internalRefresh(Object element) {
        Control list = getControl();
        if (element == null || equals(element, getRoot())) {
            // the parent
            if (listMap != null) {
                listMap.clear();
            }
            unmapAllElements();
            List selection = getSelectionFromWidget();

            int topIndex = -1;
            if (selection == null || selection.isEmpty()) {
                topIndex = listGetTopIndex();
            }

            list.setRedraw(false);
            listRemoveAll();

            Object[] children = getSortedChildren(getRoot());
            //String[] items = new String[children.length];

            ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();

            for (int i = 0; i < children.length; i++) {
                Object el = children[i];
                String label = getLabelProviderText(labelProvider, el);
                Image image = getImage(labelProvider, el);
                CheckItem item = listAdd(label, image, -1);
                listMap.add(el);
                mapElement(el, item.getControl()); // must map it, since findItem only looks in map, if enabled
            }

            //listSetItems(items);
            list.setRedraw(true);

            if (topIndex == -1) {
                setSelectionToWidget(selection, false);
            } else {
                listSetTopIndex(Math.min(topIndex, children.length));
            }
        } else {
            doUpdateItem(list, element, true);
        }
    }

    /**
     * Returns the index of the item currently at the top of the viewable area.
     * <p>
     * Default implementation returns -1.
     * </p>
     * 
     * @return index, -1 for none
     * @since 3.3
     */
    protected int listGetTopIndex() {
        return -1;
    }

    /**
     * Sets the index of the item to be at the top of the viewable area.
     * <p>
     * Default implementation does nothing.
     * </p>
     * 
     * @param index
     *            the given index. -1 for none. index will always refer to a
     *            valid index.
     * @since 3.3
     */
    protected void listSetTopIndex(int index) {
    }

    /**
     * Removes the given elements from this list viewer.
     * 
     * @param elements
     *            the elements to remove
     */
    private void internalRemove(final Object[] elements) {
        Object input = getInput();
        for (int i = 0; i < elements.length; ++i) {
            if (equals(elements[i], input)) {
                setInput(null);
                return;
            }
            int ix = getElementIndex(elements[i]);
            if (ix >= 0) {
                CheckItem item = listRemove(ix);
                listMap.remove(ix);
                unmapElement(elements[i], item.getControl());
            }
        }
    }

    /**
     * Removes the given elements from this list viewer. The selection is
     * updated if required.
     * <p>
     * This method should be called (by the content provider) when elements have
     * been removed from the model, in order to cause the viewer to accurately
     * reflect the model. This method only affects the viewer, not the model.
     * </p>
     * 
     * @param elements
     *            the elements to remove
     */
    public void remove(final Object[] elements) {
        assertElementsNotNull(elements);
        if (elements.length == 0) {
            return;
        }
        preservingSelection(new Runnable() {
            public void run() {
                internalRemove(elements);
            }
        });
    }

    /**
     * Removes the given element from this list viewer. The selection is updated
     * if necessary.
     * <p>
     * This method should be called (by the content provider) when a single
     * element has been removed from the model, in order to cause the viewer to
     * accurately reflect the model. This method only affects the viewer, not
     * the model. Note that there is another method for efficiently processing
     * the simultaneous removal of multiple elements.
     * </p>
     * 
     * @param element
     *            the element
     */
    public void remove(Object element) {
        remove(new Object[] { element });
    }

    /**
     * The list viewer implementation of this <code>Viewer</code> framework
     * method ensures that the given label provider is an instance of
     * <code>ILabelProvider</code>. <b>The optional interfaces
     * {@link IColorProvider} and {@link IFontProvider} have no effect for this
     * type of viewer</b>
     */
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof ILabelProvider);
        super.setLabelProvider(labelProvider);
    }

    /*
     * (non-Javadoc) Method declared on StructuredViewer.
     */
    protected void setSelectionToWidget(List in, boolean reveal) {
        if (in == null || in.size() == 0) { // clear selection
            listDeselectAll();
        } else {
            int n = in.size();
            int[] ixs = new int[n];
            int count = 0;
            for (int i = 0; i < n; ++i) {
                Object el = in.get(i);
                int ix = getElementIndex(el);
                if (ix >= 0) {
                    ixs[count++] = ix;
                }
            }
            if (count < n) {
                System.arraycopy(ixs, 0, ixs = new int[count], 0, count);
            }
            listSetSelection(ixs);
            if (reveal) {
                listShowSelection();
            }
        }
    }

    /**
     * Returns the index of the given element in listMap, or -1 if the element
     * cannot be found. As of 3.3, uses the element comparer if available.
     * 
     * @param element
     * @return the index
     */
    protected int getElementIndex(Object element) {
        IElementComparer comparer = getComparer();
        if (comparer == null) {
            return listMap.indexOf(element);
        }
        int size = listMap.size();
        for (int i = 0; i < size; i++) {
            if (comparer.equals(element, listMap.get(i)))
                return i;
        }
        return -1;
    }

//    /**
//     * @param element
//     * @return true if listMap contains the given element
//     * 
//     * @since 3.3
//     */
//    private boolean listMapContains(Object element) {
//        return getElementIndex(element) != -1;
//    }

}
