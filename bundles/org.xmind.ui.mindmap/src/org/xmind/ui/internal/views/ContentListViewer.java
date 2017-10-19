package org.xmind.ui.internal.views;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
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
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IRevealService;
import org.xmind.gef.service.IRevealServiceListener;
import org.xmind.gef.service.RevealEvent;
import org.xmind.gef.service.ZoomingAndPanningRevealService;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.gef.ui.editor.IGraphicalEditorPage;

public class ContentListViewer extends StructuredViewer {

    private class CenteredRevealHelper implements IRevealServiceListener {

        private ZoomingAndPanningRevealService service;

        private boolean oldCentered;

        public CenteredRevealHelper(IViewer viewer) {
            Object service = viewer.getService(IRevealService.class);
            if (service != null
                    && service instanceof ZoomingAndPanningRevealService) {
                this.service = (ZoomingAndPanningRevealService) service;
                this.oldCentered = this.service.isCentered();
            } else {
                this.service = null;
                this.oldCentered = false;
            }
        }

        public void start(IGraphicalPart part) {
            if (this.service != null) {
                this.service.setCentered(true);
                this.service.reveal(new StructuredSelection(part));
                this.service.addRevealServiceListener(this);
            }
        }

        public void revealingStarted(RevealEvent event) {
        }

        public void revealingCanceled(RevealEvent event) {
            restore();
        }

        public void revealingFinished(RevealEvent event) {
            restore();
        }

        void restore() {
            this.service.removeRevealServiceListener(this);
            this.service.setCentered(this.oldCentered);
        }

    }

    protected static class ContentItem {

        private ContentListViewer listViewer;

        private Object data;

        private Composite bar;

        private Label imageLabel;

        private Label textLabel;

        private Hyperlink hyperlink;

        public ContentItem(ContentListViewer listViewer, Composite parent) {
            this.listViewer = listViewer;
            bar = new Composite(parent, SWT.NONE);
            bar.setBackground(parent.getBackground());
            bar.setData(this);
//            GridLayoutFactory.fillDefaults().numColumns(3)
//                    .extendedMargins(5, 5, 1, 1).applyTo(bar);

            imageLabel = new Label(bar, SWT.NONE);
            imageLabel.setImage(null);
            imageLabel.setBackground(bar.getBackground());
            GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER)
                    .applyTo(imageLabel);

            textLabel = new Label(bar, SWT.NONE);
            textLabel.setText(""); //$NON-NLS-1$
            textLabel.setBackground(bar.getBackground());
            GridDataFactory.fillDefaults().grab(false, false)
                    .align(SWT.FILL, SWT.CENTER).applyTo(textLabel);

            hyperlink = new Hyperlink(bar, SWT.NONE);
            hyperlink.setText(""); //$NON-NLS-1$
            hyperlink.setBackground(bar.getBackground());
            hyperlink.setForeground(Display.getCurrent().getSystemColor(
                    SWT.COLOR_BLUE));
            hyperlink.setUnderlined(true);
            GridDataFactory.fillDefaults().grab(true, false)
                    .align(SWT.FILL, SWT.CENTER).applyTo(hyperlink);

            hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
                public void linkActivated(HyperlinkEvent e) {
                    try {
                        PlatformUI.getWorkbench().getBrowserSupport()
                                .getExternalBrowser()
                                .openURL(new URL(getCompleteURL(e.getLabel())));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            });

            addControlListeners();
        }

        protected String getCompleteURL(String label) {
            if (!label.startsWith("http://") && !label.startsWith("https://")) //$NON-NLS-1$ //$NON-NLS-2$
                return "http://" + label; //$NON-NLS-1$

            return label;
        }

        protected void selectTopic(Event event) {
            getListViewer().getControl().setFocus();
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }

        public ContentListViewer getListViewer() {
            return listViewer;
        }

        public Control getControl() {
            return bar;
        }

        protected Composite getBarComposite() {
            return bar;
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

        public String getHyperink() {
            return hyperlink.getText();
        }

        public void setHyperlink(String link) {
            if (link == null) {
                hyperlink.dispose();
                GridLayoutFactory.fillDefaults().numColumns(2)
                        .extendedMargins(5, 5, 1, 1).applyTo(bar);
            } else {
                hyperlink.setText(link);
                GridLayoutFactory.fillDefaults().numColumns(3)
                        .extendedMargins(5, 5, 1, 1).applyTo(bar);
            }
        }

        protected void addControlListeners() {
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

            textLabel.addListener(SWT.MouseDoubleClick, listener);
            textLabel.addListener(SWT.MouseMove, listener);
            textLabel.addListener(SWT.MouseExit, listener);
            textLabel.addListener(SWT.MouseDown, listener);

        }

        protected void userOpen(Event e) {
            getListViewer().reveal(data);
        }

        protected void userSelect(Event e) {
            bar.redraw();
            textLabel.redraw();
//            bar.setBackground(getSelectionBackground());
            imageLabel.setBackground(getSelectionBackground());
            textLabel.setBackground(getSelectionBackground());
        }

        protected void userExit(Event event) {
            bar.redraw();
            textLabel.redraw();
            Color background = bar.getParent().getBackground();
            bar.setBackground(background);
            imageLabel.setBackground(background);
            textLabel.setBackground(background);
        }

        public void dispose() {
            bar.dispose();
        }

        public boolean isDisposed() {
            return bar.isDisposed();
        }

    }

    private static Color selectionBackground = null;

    public static final String SEP = "[topic+link]"; //$NON-NLS-1$

    protected static Color getSelectionBackground() {
        if (selectionBackground == null) {
            selectionBackground = Display.getCurrent().getSystemColor(
                    SWT.COLOR_TITLE_INACTIVE_BACKGROUND_GRADIENT);
        }
        return selectionBackground;
    }

    private Composite list;

    private List<Object> listMap = new ArrayList<Object>();

    private List<ContentItem> items = new ArrayList<ContentItem>();

    public ContentListViewer(Composite parent, int style) {
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
                    size = super.computeSize(wHint, hHint, changed);
                }
                return size;
            }

        };

        list.setTabList(new Control[0]);
        list.setBackground(parent.getBackground());
        GridLayoutFactory.fillDefaults().spacing(0, 0).applyTo(list);

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

    protected ContentItem listAdd(Image image, String text, Object data,
            int index) {
        ContentItem newItem = new ContentItem(this, list);

        GridDataFactory.fillDefaults().grab(true, false)
                .applyTo(newItem.getControl());

        setItem(image, text, data, newItem);

        list.layout();
        if (index < 0 || index >= listGetItemCount()) {
            items.add(newItem);
        } else {
            ContentItem oldItem = items.get(index);
            items.add(index, newItem);
            newItem.getControl().moveAbove(oldItem.getControl());
        }

        return newItem;
    }

    protected ContentItem listRemove(int index) {
        if (index < 0 || index >= listGetItemCount())
            return null;

        ContentItem item = items.remove(index);
        if (item != null)
            item.dispose();
        return item;
    }

    protected void listRemoveAll() {
        for (ContentItem item : items)
            item.dispose();
        items.clear();
    }

    protected void listSetItem(Image image, String text, Object data, int index) {
        if (index < 0 || index >= listGetItemCount())
            return;
        ContentItem item = items.get(index);
        setItem(image, text, data, item);
    }

    private void setItem(Image image, String text, Object data, ContentItem item) {
        String content = null;
        String link = null;
        if (text.contains(SEP)) {
            int index = text.indexOf(SEP);
            content = text.substring(0, index);
            link = text.substring(index + SEP.length(), text.length());
        } else {
            content = text;
        }

        if (!item.isDisposed()) {
            item.setImage(image);
            item.setText(content);
            item.setHyperlink(link);
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
            String text = getLabelProviderText(
                    (ILabelProvider) getLabelProvider(), el);
            ContentItem item = listAdd(image, text, el, -1);
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
                ContentItem item = listRemove(ix);
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
                String text = getLabelProviderText(labelProvider, element);
                listSetItem(image, text, element, ix);
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
                Image image = getImage(labelProvider, el);
                String text = getLabelProviderText(labelProvider, el);
                ContentItem item = listAdd(image, text, el, -1);
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

        if (activeEditor instanceof IGraphicalEditor) {
            Object selectedPage = ((IGraphicalEditor) activeEditor)
                    .getSelectedPage();
            if (selectedPage instanceof IGraphicalEditorPage) {
                IGraphicalEditorPage page = (IGraphicalEditorPage) selectedPage;
                IGraphicalViewer viewer = page.getViewer();
                if (viewer == null)
                    return;

                IPart selectedPart = viewer.getFocusedPart();
                if (selectedPart instanceof IGraphicalPart) {
                    IGraphicalPart part = (IGraphicalPart) selectedPart;
                    new CenteredRevealHelper(viewer).start(part);
                }
            }
        }

    }

    @Override
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

    private void listDeselectAll() {
    }

    protected void listSetSelection(int[] ixs) {
    }

    protected void listShowSelection() {
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

    protected void listSetTopIndex(int index) {
    }

    private Image getImage(ILabelProvider labelProvider, Object element) {
        return labelProvider.getImage(element);
    }

    private String getLabelProviderText(ILabelProvider labelProvider,
            Object element) {
        String text = labelProvider.getText(element);
        if (text == null) {
            return "";//$NON-NLS-1$
        }
        return text;
    }

}
