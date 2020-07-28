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
package org.xmind.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.internal.misc.StringMatcher;
import org.xmind.ui.viewers.SWTUtils;

public class PopupFilteredList extends PopupDialog {

    /**
     * An element creator creates an element using a user-input text. This class
     * is used when the popup filtered list permits unprovided values and need
     * to convert the user-input text into a proper value.
     * 
     * @author Frank Shaka
     */
    public static interface IElementCreator {

        /**
         * Creates an element according to the user-input text.
         * 
         * @param text
         *            a string inputted by user
         * @return an element created according to the specified text
         */
        Object createElement(String text);

    }

    public static class PatternFilter extends ViewerFilter {

        private String patternText = null;

        private StringMatcher matcher = null;

        private boolean ignoreCase;

        private boolean useWildCards;

        private boolean useWhiteSpacesAsWildCards;

        private boolean useWildCardOnStart;

        public PatternFilter() {
            this(true, true, true, true);
        }

        public PatternFilter(boolean ignoreCase, boolean useWildCards,
                boolean useWhiteSpacesAsWildCards, boolean useWildCardOnStart) {
            this.ignoreCase = ignoreCase;
            this.useWildCards = useWildCards;
            this.useWhiteSpacesAsWildCards = useWhiteSpacesAsWildCards;
            this.useWildCardOnStart = useWildCardOnStart;
        }

        public String getPatternText() {
            return patternText;
        }

        private void setPatternText(String pattern) {
            this.patternText = pattern;
            updateMatcher();
        }

        protected void updateMatcher() {
            if (patternText == null || patternText.length() == 0) {
                matcher = null;
            } else {
                String pattern = patternText;
                if (usesWhiteSpacesAsWildCards()) {
                    pattern = pattern.replaceAll(" ", "*"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                if (usesWildCardOnStart()) {
                    if (!pattern.startsWith("*")) //$NON-NLS-1$
                        pattern = "*" + pattern; //$NON-NLS-1$
                }
                if (!pattern.endsWith("*")) //$NON-NLS-1$
                    pattern = pattern + "*"; //$NON-NLS-1$
                matcher = new StringMatcher(pattern, ignoresCase(),
                        !usesWildCards());
            }
        }

        public boolean ignoresCase() {
            return ignoreCase;
        }

        public boolean usesWildCardOnStart() {
            return useWildCardOnStart;
        }

        public boolean usesWhiteSpacesAsWildCards() {
            return useWhiteSpacesAsWildCards;
        }

        public boolean usesWildCards() {
            return useWildCards;
        }

        public void setIgnoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            updateMatcher();
        }

        public void setUseWildCardOnStart(boolean useWildCardOnStart) {
            this.useWildCardOnStart = useWildCardOnStart;
            updateMatcher();
        }

        public void setUseWhiteSpacesAsWildCards(
                boolean usesWhiteSpacesAsWildCards) {
            this.useWhiteSpacesAsWildCards = usesWhiteSpacesAsWildCards;
            updateMatcher();
        }

        public void setUseWildCards(boolean usesWildCards) {
            this.useWildCards = usesWildCards;
            updateMatcher();
        }

        public boolean select(Viewer viewer, Object parentElement,
                Object element) {
            if (matcher == null)
                return true;

            if (isMatch(viewer, element))
                return true;

            return isLeafMatch(viewer, element);
        }

        private boolean isMatch(Viewer viewer, Object element) {
            String elementName = ((ILabelProvider) ((ContentViewer) viewer)
                    .getLabelProvider()).getText(element);
            if (elementName == null)
                return false;

            return matcher.match(elementName);

        }

        private boolean isLeafMatch(Viewer viewer, Object element) {
            if (isMatch(viewer, element))
                return true;

            Object[] children = ((ITreeContentProvider) ((AbstractTreeViewer) viewer)
                    .getContentProvider()).getChildren(element);
            if ((children != null) && (children.length > 0)) {
                for (Object ele : children) {
                    if (isLeafMatch(viewer, ele))
                        return true;
                }
            }

            return false;
        }

    }

    private static class DelegatingTreeContentProvider implements
            ITreeContentProvider {

        private IContentProvider delegate;

        public DelegatingTreeContentProvider(IContentProvider delegate) {
            this.delegate = delegate;
        }

        public Object[] getChildren(Object parentElement) {
            if (delegate instanceof ITreeContentProvider) {
                return ((ITreeContentProvider) delegate)
                        .getChildren(parentElement);
            }
            return new Object[0];
        }

        public Object getParent(Object element) {
            if (delegate instanceof ITreeContentProvider) {
                return ((ITreeContentProvider) delegate).getParent(element);
            }
            return null;
        }

        public boolean hasChildren(Object element) {
            if (delegate instanceof ITreeContentProvider) {
                return ((ITreeContentProvider) delegate).hasChildren(element);
            }
            return false;
        }

        public Object[] getElements(Object inputElement) {
            if (delegate instanceof IStructuredContentProvider) {
                return ((IStructuredContentProvider) delegate)
                        .getElements(inputElement);
            } else if (delegate instanceof ITreeContentProvider) {
                return ((ITreeContentProvider) delegate)
                        .getElements(inputElement);
            }
            return new Object[0];
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }

    }

    private class DelegatingLabelProvider extends LabelProvider implements
            IFontProvider, ILabelProviderListener {

        private IBaseLabelProvider delegate;

        public DelegatingLabelProvider(IBaseLabelProvider delegate) {
            this.delegate = delegate;
            if (delegate != null) {
                delegate.addListener(this);
            }
        }

        public String getText(Object element) {
            if (delegate instanceof ILabelProvider) {
                return ((ILabelProvider) delegate).getText(element);
            }
            return super.getText(element);
        }

        public Image getImage(Object element) {
            if (delegate instanceof ILabelProvider) {
                return ((ILabelProvider) delegate).getImage(element);
            }
            return super.getImage(element);
        }

        public Font getFont(Object element) {
            if (element == defaultSelection
                    || (element != null && element.equals(defaultSelection)))
                return JFaceResources.getFontRegistry().getBold(
                        JFaceResources.DEFAULT_FONT);
            return null;
        }

        public void labelProviderChanged(LabelProviderChangedEvent event) {
            LabelProviderChangedEvent e = new LabelProviderChangedEvent(this,
                    event.getElements());
            fireLabelProviderChanged(e);
        }

    }

    private Text filterText;

    private TreeViewer treeViewer;

    private List<IOpenListener> openListeners = null;

    private PatternFilter patternFilter = null;

    private Object input = null;

    private IContentProvider contentProvider = null;

    private IBaseLabelProvider labelProvider = null;

    private ViewerFilter[] filters = null;

    private ViewerSorter sorter = null;

    private ViewerComparator comparator = null;

    private IElementComparer comparer = null;

    private Object defaultSelection = null;

    private Rectangle boundsReference = null;

    private boolean permitsUnprovidedElement = false;

    private IElementCreator elementCreator = null;

    public PopupFilteredList(Shell parent) {
        this(parent, SWT.ON_TOP | SWT.RESIZE);
    }

    public PopupFilteredList(Shell parent, String titleText, String infoText) {
        super(parent, SWT.ON_TOP | SWT.RESIZE, true, false, false, false,
                false, titleText, infoText);
    }

    public PopupFilteredList(Shell parent, int shellStyle) {
        super(parent, shellStyle, true, false, false, false, false, null, null);
    }

    protected Control createDialogArea(Composite parent) {
        Display display = parent.getDisplay();
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setBackground(display
                .getSystemColor(SWT.COLOR_INFO_BACKGROUND));
        GridLayout layout2 = (GridLayout) composite.getLayout();
        layout2.verticalSpacing = 3;
        layout2.marginWidth = 3;
        layout2.marginTop = 4;

        filterText = createFilterText(composite);
        filterText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        treeViewer = createTreeViewer(composite);
        treeViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));

        configureFilterText(filterText);
        configureTreeViewer(treeViewer);

        return composite;
    }

    protected Text createFilterText(Composite parent) {
        return new Text(parent, SWT.SINGLE);
    }

    protected void configureFilterText(final Text filterText) {
        filterText.setText(""); //$NON-NLS-1$
        filterText.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) {
                /*
                 * Running in an asyncExec because the selectAll() does not
                 * appear to work when using mouse to give focus to text.
                 */
                Display display = getFilterText().getDisplay();
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (!getFilterText().isDisposed()) {
                            getFilterText().selectAll();
                        }
                    }
                });
            }
        });

        filterText.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                // on a Arrow Down we want to transfer focus to the list
                boolean hasItems = getViewer().getTree().getItemCount() > 0;
                if (hasItems && e.keyCode == SWT.ARROW_DOWN) {
                    treeViewer.getTree().setFocus();
                }
            }
        });

        // enter key set focus to tree
        filterText.addTraverseListener(new TraverseListener() {
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    fireOpen();
                }
            }
        });
        filterText.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                filterChanged();
            }
        });
    }

    protected TreeViewer createTreeViewer(Composite parent) {
        return new TreeViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL
                | SWT.FULL_SELECTION);
    }

    protected void configureTreeViewer(TreeViewer treeViewer) {
        final Tree tree = treeViewer.getTree();
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.KeyDown:
                    if (SWTUtils.matchKey(event.stateMask, event.keyCode, 0,
                            SWT.ESC)) {
                        close();
                    }
                    break;
                case SWT.DefaultSelection:
                    fireOpen();
                    break;
                case SWT.MouseUp:
                    handleMouseUp(event);
                    break;
                }
            }

            protected void handleMouseUp(Event e) {
                if (tree.getSelectionCount() < 1)
                    return;

                if (e.button == 1) {
                    if (tree.equals(e.widget)) {
                        Widget o = tree.getItem(new Point(e.x, e.y));
                        TreeItem selection = tree.getSelection()[0];
                        if (selection.equals(o)) {
                            fireOpen(selection.getData());
                        }
                    }
                }
            }
        };
        tree.addListener(SWT.KeyDown, listener);
        tree.addListener(SWT.DefaultSelection, listener);
        tree.addListener(SWT.MouseUp, listener);
        tree.addMouseMoveListener(new MouseMoveListener() {
            final int ignoreEventCount = "gtk".equals(SWT.getPlatform()) ? 4 : 1; //$NON-NLS-1$
            TreeItem fLastItem = null;
            int lastY = 0;
            int itemHeightdiv4 = tree.getItemHeight() / 4;
            int tableHeight = tree.getBounds().height;
            Point tableLoc = tree.toDisplay(0, 0);
            int divCount = 0;

            public void mouseMove(MouseEvent e) {
                if (divCount == ignoreEventCount) {
                    divCount = 0;
                }
                if (tree.equals(e.getSource()) & ++divCount == ignoreEventCount) {
                    Widget item = tree.getItem(new Point(e.x, e.y));
                    if (item instanceof TreeItem && lastY != e.y) {
                        lastY = e.y;
                        if (!item.equals(fLastItem)) {
                            fLastItem = (TreeItem) item;
                            tree.setSelection(new TreeItem[] { fLastItem });
                        } else if (e.y < itemHeightdiv4) {
                            // Scroll up
                            item = getViewer().scrollUp(e.x + tableLoc.x,
                                    e.y + tableLoc.y);
                            if (item instanceof TreeItem) {
                                fLastItem = (TreeItem) item;
                                tree.setSelection(new TreeItem[] { fLastItem });
                            }
                        } else if (e.y > tableHeight - itemHeightdiv4) {
                            // Scroll down
                            item = getViewer().scrollDown(e.x + tableLoc.x,
                                    e.y + tableLoc.y);
                            if (item instanceof TreeItem) {
                                fLastItem = (TreeItem) item;
                                tree.setSelection(new TreeItem[] { fLastItem });
                            }
                        }
                    }
                }
            }
        });

        treeViewer.setContentProvider(new DelegatingTreeContentProvider(
                getContentProvider()));
        treeViewer.setLabelProvider(new DelegatingLabelProvider(
                getLabelProvider()));
        if (getFilters() != null)
            treeViewer.setFilters(getFilters());
        getPatternFilter().setPatternText(null);
        treeViewer.addFilter(getPatternFilter());
        treeViewer.setSorter(getSorter());
        treeViewer.setComparator(getComparator());
        treeViewer.setComparer(getComparer());
        treeViewer.setInput(getInput());
        treeViewer.setAutoExpandLevel(AbstractTreeViewer.ALL_LEVELS);
        if (defaultSelection != null) {
            treeViewer.setSelection(new StructuredSelection(defaultSelection));
        } else if (tree.getItemCount() > 0) {
            tree.setSelection(tree.getItem(0));
        }
    }

    protected void filterChanged() {
        Control control = getViewer().getControl();
        control.setRedraw(false);
        getPatternFilter().setPatternText(getFilterText().getText());
        getViewer().refresh();
        control.setRedraw(true);
    }

    public Text getFilterText() {
        return filterText;
    }

    public TreeViewer getViewer() {
        return treeViewer;
    }

    public void addOpenListener(IOpenListener listener) {
        if (openListeners == null)
            openListeners = new ArrayList<IOpenListener>();
        openListeners.add(listener);
    }

    public void removeOpenListener(IOpenListener listener) {
        if (openListeners == null)
            return;
        openListeners.remove(listener);
    }

    protected void fireOpen() {
        ISelection selection = getViewer().getSelection();
        if (selection.isEmpty() && permitsUnprovidedElement()) {
            selection = new StructuredSelection(createElement(getFilterText()
                    .getText()));
        }
        fireOpenEvent(new OpenEvent(getViewer(), selection));
    }

    private Object createElement(String text) {
        if (elementCreator != null)
            return elementCreator.createElement(text);
        return text;
    }

    protected void fireOpen(Object element) {
        fireOpenEvent(new OpenEvent(getViewer(), new StructuredSelection(
                element)));
    }

    protected void fireOpenEvent(final OpenEvent e) {
//        if (input instanceof List<?>) {
//            TreeViewer viewer = getViewer();
//            ISelection selection = viewer.getSelection();
//            if (selection instanceof StructuredSelection) {
//                Object element = ((StructuredSelection) selection)
//                        .getFirstElement();
//                boolean unAction = FontUtils.LOADED_FONTS.equals(element)
//                        || FontUtils.RECOMMENDED_FONTS.equals(element)
//                        || FontUtils.SYSTEM_FONTS.equals(element);
//                if (unAction) {
//                    if (viewer.getExpandedState(element))
//                        viewer.collapseToLevel(element, 2);
//                    else
//                        viewer.expandToLevel(element, 2);
//                    return;
//                }
//            }
//        }
        close();
        if (openListeners == null || openListeners.isEmpty())
            return;
        for (final Object l : openListeners.toArray()) {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    ((IOpenListener) l).open(e);
                }
            });
        }
    }

    protected PatternFilter getPatternFilter() {
        if (patternFilter == null)
            patternFilter = new PatternFilter();
        return patternFilter;
    }

    public Object getInput() {
        return input;
    }

    public void setInput(Object input) {
        this.input = input;
    }

    public IContentProvider getContentProvider() {
        return contentProvider;
    }

    public void setContentProvider(IContentProvider contentProvider) {
        this.contentProvider = contentProvider;
    }

    public IBaseLabelProvider getLabelProvider() {
        return labelProvider;
    }

    public void setLabelProvider(IBaseLabelProvider labelProvider) {
        this.labelProvider = labelProvider;
    }

    public ViewerSorter getSorter() {
        return sorter;
    }

    public void setSorter(ViewerSorter sorter) {
        this.sorter = sorter;
    }

    public ViewerComparator getComparator() {
        return comparator;
    }

    public void setComparator(ViewerComparator comparator) {
        this.comparator = comparator;
    }

    public IElementComparer getComparer() {
        return comparer;
    }

    public void setComparer(IElementComparer comparer) {
        this.comparer = comparer;
    }

    public void setFilters(ViewerFilter[] filters) {
        this.filters = filters;
    }

    public ViewerFilter[] getFilters() {
        return filters;
    }

    public Object getDefaultSelection() {
        return defaultSelection;
    }

    public void setDefaultSelection(Object defaultSelection) {
        this.defaultSelection = defaultSelection;
    }

    public void setBoundsReference(Rectangle reference) {
        this.boundsReference = reference;
    }

//    @Override
//    protected Point getInitialSize() {
//        Point initialSize = super.getInitialSize();
//        initialSize.x = initialSize.x + 35;
//        return initialSize;
//    }

    protected Point getInitialLocation(Point initialSize) {
        Rectangle r = boundsReference;
        if (r != null) {
            return new Point(r.x, r.y + r.height);
        }
        return super.getInitialLocation(initialSize);
    }

    public void setPatternFilter(PatternFilter patternFilter) {
        if (patternFilter == this.patternFilter)
            return;
        this.patternFilter = patternFilter;
        if (getViewer() != null && !getViewer().getControl().isDisposed()) {
            filterChanged();
        }
    }

    public boolean permitsUnprovidedElement() {
        return permitsUnprovidedElement;
    }

    public void setPermitsUnprovidedElement(boolean permitsUnprovidedElement) {
        this.permitsUnprovidedElement = permitsUnprovidedElement;
    }

    public void setElementCreator(IElementCreator elementCreator) {
        this.elementCreator = elementCreator;
    }

    public IElementCreator getElementCreator() {
        return elementCreator;
    }

}