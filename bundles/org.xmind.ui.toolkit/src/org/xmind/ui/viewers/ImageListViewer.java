package org.xmind.ui.viewers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.RowDataFactory;
import org.eclipse.jface.layout.RowLayoutFactory;
import org.eclipse.jface.util.OpenStrategy;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ImageListViewer extends StructuredViewer {

    protected static class ImageItem {

        private ImageListViewer listViewer;

        private Object data;

        private Composite bar;

        private Label imageLabel;

        public ImageItem(ImageListViewer listViewer, Composite parent) {
            this.listViewer = listViewer;
            bar = new Composite(parent, SWT.NONE);
            bar.setBackground(parent.getBackground());
            bar.setData(this);
            RowLayoutFactory.fillDefaults().wrap(true)
                    .extendedMargins(5, 5, 1, 1).applyTo(bar);

            imageLabel = new Label(bar, SWT.NONE);
            imageLabel.setImage(null);
            imageLabel.setBackground(bar.getBackground());
            RowDataFactory.swtDefaults().hint(SWT.DEFAULT, 80)
                    .applyTo(imageLabel);

            addControlListeners();

        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public ImageListViewer getListViewer() {
            return listViewer;
        }

        public Control getControl() {
            return bar;
        }

        protected Composite getBarComposite() {
            return bar;
        }

        public Image getImage() {
            return imageLabel.getImage();
        }

        public void setImage(Image image) {
            imageLabel.setImage(image);
        }

        private void addControlListeners() {
            Listener listener = new Listener() {
                public void handleEvent(Event event) {
                    int type = event.type;
                    switch (type) {
                    case SWT.MouseExit:
                        userExit(event);
                        break;
                    case SWT.MouseDoubleClick:
                        if (OpenStrategy.getOpenMethod() == OpenStrategy.DOUBLE_CLICK) {
                            userOpen(event);
                        }
                        break;
                    case SWT.MouseMove:
                        userSelect(event);
                        break;
                    case SWT.MouseDown:
                        userSelect(event);
                        break;
                    }
                }
            };

            bar.addListener(SWT.MouseDoubleClick, listener);
            bar.addListener(SWT.MouseMove, listener);
            bar.addListener(SWT.MouseExit, listener);
            bar.addListener(SWT.MouseDown, listener);

            imageLabel.addListener(SWT.MouseDoubleClick, listener);
            imageLabel.addListener(SWT.MouseMove, listener);
            imageLabel.addListener(SWT.MouseExit, listener);
            imageLabel.addListener(SWT.MouseDown, listener);
        }

        protected void userOpen(Event e) {
            getListViewer().reveal(data);
        }

        protected void userSelect(Event e) {
            bar.redraw();
            bar.setBackground(getSelectionBackground());
            imageLabel.setBackground(getSelectionBackground());
        }

        protected void userExit(Event event) {
            bar.redraw();
            Color background = bar.getParent().getBackground();
            bar.setBackground(background);
            imageLabel.setBackground(background);
        }

        public void dispose() {
            bar.dispose();
        }

        public boolean isDisposed() {
            return bar.isDisposed();
        }

    }

    private static Color selectionBackground = null;

    protected static Color getSelectionBackground() {
        if (selectionBackground == null) {
            selectionBackground = Display.getCurrent().getSystemColor(
                    SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
        }
        return selectionBackground;
    }

    private Composite list;

    private List<Object> listMap = new ArrayList<Object>();

    private List<ImageItem> items = new ArrayList<ImageItem>();

    public ImageListViewer(final Composite parent, int style) {
        list = new Composite(parent, style) {
            @Override
            public boolean setFocus() {
                return super.setFocus();
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
                    size = super.computeSize(parent.getBounds().width,
                            SWT.DEFAULT, true);
                }
                return size;
            }
        };

        list.setTabList(new Control[0]);
        list.setBackground(parent.getBackground());
        RowLayoutFactory.fillDefaults().spacing(0).wrap(true).applyTo(list);

        list.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose(e);
            }
        });
    }

    public int getItemCount() {
        return listGetItemCount();
    }

    protected int listGetItemCount() {
        return items.size();
    }

    protected ImageItem listAdd(Image image, Object data, int index) {
        ImageItem newItem = new ImageItem(this, list);

        RowDataFactory.swtDefaults().hint(SWT.DEFAULT, 80)
                .applyTo(newItem.getControl());

        newItem.setImage(image);
        newItem.setData(data);

        list.layout();
        if (index < 0 || index >= listGetItemCount()) {
            items.add(newItem);
        } else {
            ImageItem oldItem = items.get(index);
            items.add(index, newItem);
            newItem.getControl().moveAbove(oldItem.getControl());
        }

        return newItem;
    }

    protected ImageItem listRemove(int index) {
        if (index < 0 || index >= listGetItemCount())
            return null;

        ImageItem item = items.remove(index);
        if (item != null)
            item.dispose();
        return item;
    }

    protected void listRemoveAll() {
        for (ImageItem item : items)
            item.dispose();
        items.clear();
    }

    protected void listSetItem(Image image, Object data, int index) {
        if (index < 0 || index >= listGetItemCount())
            return;
        ImageItem item = items.get(index);
        if (!item.isDisposed()) {
            item.setImage(image);
            item.setData(data);
        }
    }

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

    public Object getElementAt(int index) {
        if (index >= 0 && index < listMap.size())
            return listMap.get(index);
        return null;
    }

    public int indexForElement(Object element) {
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

    public void inputChanged(Object input, Object oldInput) {
        listMap.clear();
        Object[] children = getSortedChildren(getRoot());
        int size = children.length;

        listRemoveAll();

        for (int i = 0; i < size; i++) {
            Object el = children[i];
            Image image = getImage((ILabelProvider) getLabelProvider(), el);
            ImageItem item = listAdd(image, el, -1);
            listMap.add(el);
            mapElement(el, item.getControl());
        }
    }

    public IBaseLabelProvider getLabelProvider() {
        return super.getLabelProvider();
    }

    @Override
    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        Assert.isTrue(labelProvider instanceof ILabelProvider);
        super.setLabelProvider(labelProvider);
    }

    public void remove(Object element) {
        remove(new Object[] { element });
    }

    public void remove(final Object[] elements) {
        assertElementsNotNull(elements);
        if (elements.length == 0)
            return;

        preservingSelection(new Runnable() {
            public void run() {
                internalRemove(elements);
            }
        });
    }

    private void internalRemove(final Object[] elements) {
        Object input = getInput();
        for (int i = 0; i < elements.length; ++i) {
            if (equals(elements[i], input)) {
                setInput(null);
                return;
            }
            int ix = getElementIndex(elements[i]);
            if (ix >= 0) {
                ImageItem item = listRemove(ix);
                listMap.remove(ix);
                unmapElement(elements[i], item.getControl());
            }
        }
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
        if (element != null) {
            int index = getElementIndex(element);
            if (index >= 0) {
                return items.get(index).getControl();
            }
        }
        return null;
    }

    @Override
    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        if (element != null) {
            int ix = getElementIndex(element);
            if (ix >= 0) {
                ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
                Image image = getImage(labelProvider, element);
                listSetItem(image, element, ix);
            }
        }
    }

    @Override
    protected List getSelectionFromWidget() {
        List<Object> list = new ArrayList<Object>();
        return list;
    }

    @Override
    protected void internalRefresh(Object element) {
        Control list = getControl();
        if (element == null || equals(element, getRoot())) {
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

            ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();

            for (int i = 0; i < children.length; i++) {
                Object el = children[i];
                Image image = getImage(labelProvider, el);
                ImageItem item = listAdd(image, el, -1);
                listMap.add(el);
                mapElement(el, item.getControl());
            }

            list.setRedraw(true);

            if (topIndex == -1) {
                setSelectionToWidget(selection, false);
            }
        } else {
            doUpdateItem(list, element, true);
        }

    }

    @Override
    public void reveal(Object element) {
        if (element == null)
            return;

        IEditorPart activeEditor = null;
        IWorkbenchWindow window = PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow();

        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null)
                activeEditor = page.getActiveEditor();
        }

        if (activeEditor == null)
            return;

        activeEditor.getSite().getPage().activate(activeEditor);
        ISelectionProvider selectionProvider = activeEditor.getSite()
                .getSelectionProvider();
        if (selectionProvider != null)
            selectionProvider.setSelection(new StructuredSelection(element));
    }

    @Override
    protected void setSelectionToWidget(List in, boolean reveal) {
        if (in != null && in.size() != 0) { // clear selection
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
        }
    }

    @Override
    public Control getControl() {
        return list;
    }

    public Composite getListComposite() {
        return list;
    }

    protected int listGetTopIndex() {
        return -1;
    }

    private Image getImage(ILabelProvider labelProvider, Object element) {
        Image image = labelProvider.getImage(element);

        ImageData imageData = image.getImageData();

        if (imageData.height > 80) {
            return new Image(null, imageData.scaledTo(imageData.width * 80
                    / imageData.height, 80));
        }

        return image;
    }

}
