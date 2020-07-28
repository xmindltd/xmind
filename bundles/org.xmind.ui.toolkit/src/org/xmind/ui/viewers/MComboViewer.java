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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;
import org.xmind.ui.dialogs.PopupFilteredList;
import org.xmind.ui.dialogs.PopupFilteredList.PatternFilter;
import org.xmind.ui.resources.ImageDescriptorProvider;

public class MComboViewer extends StructuredViewer {

    /**
     * Style bit: Create handle control and drop-down widget with default
     * behaviours, i.e. showing text, showing image, using menu as drop-down
     * widget.
     */
    public static final int NORMAL = MButton.NORMAL;

    /**
     * Style bit: Don't show text.
     */
    public static final int NO_TEXT = MButton.NO_TEXT;

    /**
     * Style bit: Don't show image.
     */
    public static final int NO_IMAGE = MButton.NO_IMAGE;

    /**
     * Style bit: Use filtered list as drop-down widget.
     */
    public static final int FILTERED = 1 << 10;

    private class SelectionAction extends Action {

        private Object element;

        public SelectionAction(Object element) {
            this.element = element;
        }

        public void run() {
            super.run();
            setSelection(new StructuredSelection(element));
        }
    }

    private static final List EMPTY_LIST = Collections.emptyList();

    private MButton dropDownHandle;

    private final boolean filtered;

    private List<Object> elementList = new ArrayList<Object>();

    private Object selection = null;

    private MenuManager popupMenu = null;

    private Map<Object, IAction> actionMap = null;

    private PopupFilteredList popupList = null;

    private PatternFilter patternFilter = null;

    private boolean permitsUnprovidedElement = false;

    private Object emptySelectionImitation = null;

    private Object separatorImitation = null;

    private ILabelProvider handleLabelProvider = null;

    private ILabelProviderListener handleLabelProviderListener = new ILabelProviderListener() {

        public void labelProviderChanged(LabelProviderChangedEvent event) {
            if (getControl() == null || getControl().isDisposed())
                return;

            MComboViewer.this.handleLabelProviderChanged(event);
        }
    };

    /**
     * Constructs a new instance of this class given its parent and a style
     * value describing its behavior and appearance.
     * 
     * @param parent
     *            a composite control which will be the parent of the new
     *            instance (cannot be null)
     * @param style
     *            the style of control to construct
     * @see #NORNAL
     * @see #NO_TEXT
     * @see #NO_IMAGE
     * @see #FILTERED
     */
    public MComboViewer(Composite parent, int style) {
        this.dropDownHandle = createDropDownHandle(parent, style);
        hookControl(dropDownHandle.getControl());
        dropDownHandle.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                openPopup();
            }
        });
        this.filtered = (style & FILTERED) != 0;
    }

    protected MButton createDropDownHandle(Composite parent, int style) {
        return new MButton(parent, style);
    }

    protected void hookControl(Control control) {
        super.hookControl(control);
        Listener listener = new Listener() {
            public void handleEvent(Event event) {
                switch (event.type) {
                case SWT.KeyDown:
                    handleKeyPress(event);
                    break;
                }
            }
        };
        control.addListener(SWT.KeyDown, listener);
    }

    protected void handleKeyPress(Event e) {
        if (!dropDownHandle.getControl().isEnabled())
            return;

        int keyCode = e.keyCode;
        int stateMask = e.stateMask;
        if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.ARROW_UP)) {
            selectPrevious();
        } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.ARROW_DOWN)) {
            selectNext();
        } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.HOME)) {
            selectFirst();
        } else if (SWTUtils.matchKey(stateMask, keyCode, 0, SWT.END)) {
            selectLast();
        }
    }

    protected void selectPrevious() {
        if (hasNoElement())
            return;
        int index = getSelectionIndex();
        if (index <= 0 || index >= getItemCount()) {
            //selectLast();
        } else {
            setSelection(new StructuredSelection(elementList.get(index - 1)));
        }
    }

    protected void selectNext() {
        if (hasNoElement())
            return;
        int index = getSelectionIndex();
        if (index < 0 || index >= getItemCount() - 1) {
            //selectFirst();
        } else {
            setSelection(new StructuredSelection(elementList.get(index + 1)));
        }
    }

    protected void selectFirst() {
        if (hasNoElement())
            return;
        setSelection(new StructuredSelection(elementList.get(0)));
    }

    protected void selectLast() {
        if (hasNoElement())
            return;
        setSelection(
                new StructuredSelection(elementList.get(getItemCount() - 1)));
    }

    protected boolean hasNoElement() {
        return elementList.isEmpty();
    }

    protected int getItemCount() {
        return elementList.size();
    }

    protected int getSelectionIndex() {
        if (getCurrentSelection() == null || hasNoElement())
            return -1;
        return indexForElement(getCurrentSelection());
    }

    protected int indexForElement(Object element) {
        ViewerComparator comparator = getComparator();
        if (comparator == null) {
            return elementList.indexOf(element);
        }
        int count = getItemCount();
        int min = 0, max = count - 1;
        while (min <= max) {
            int mid = (min + max) / 2;
            Object data = elementList.get(mid);
            int compare = comparator.compare(this, data, element);
            if (compare == 0) {
                // find first item > element
                while (compare == 0) {
                    ++mid;
                    if (mid >= count) {
                        break;
                    }
                    data = elementList.get(mid);
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

    protected void inputChanged(Object input, Object oldInput) {
        closePopup();
        elementList.clear();
        Object[] children = getSortedChildren(getRoot());
        int size = children.length;

        Object newSelection = null;
        for (int i = 0; i < size; i++) {
            Object el = children[i];
            elementList.add(el);
            mapElement(el, getControl()); // must map it, since findItem only looks in map, if enabled
            if (selection != null && equals(selection, el)) {
                newSelection = el;
            }
        }
        selection = newSelection;
        updateDropDown();
        if (popupMenu != null) {
            refreshPopupMenu(popupMenu);
        } else if (popupList != null) {
            refreshPopupList(popupList);
        }
    }

    protected Widget doFindInputItem(Object element) {
        return dropDownHandle.getControl();
    }

    protected Widget doFindItem(Object element) {
        return dropDownHandle.getControl();
    }

    protected void doUpdateItem(Widget item, Object element, boolean fullMap) {
        if (equals(element, getRoot())
                || equals(element, getCurrentSelection())) {
            updateDropDown();
        }
        if (popupMenu != null) {
            updateAction(element);
        }
    }

    protected void updateAction(Object element) {
        if (actionMap == null)
            return;
        IAction action = actionMap.get(element);
        if (action == null)
            return;
        ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
        updateAction(action, labelProvider, element);
    }

    protected void updateAction(IAction action, ILabelProvider labelProvider,
            Object element) {
        String text = labelProvider.getText(element);
        if (text == null)
            text = ""; //$NON-NLS-1$
        action.setText(text);

        if (labelProvider instanceof ImageDescriptorProvider) {
            action.setImageDescriptor(((ImageDescriptorProvider) labelProvider)
                    .getImageDescriptor(element));

        } else {
            final Image image = labelProvider.getImage(element);
            if (image != null)
                action.setImageDescriptor(new ImageDescriptor() {
                    @Override
                    public ImageData getImageData() {
                        return image.getImageData();
                    }
                });
            else
                action.setImageDescriptor(null);
        }

    }

    protected List getSelectionFromWidget() {
        return getCurrentSelection() == null ? EMPTY_LIST
                : Collections.singletonList(getCurrentSelection());
    }

    protected void internalRefresh(Object element) {
        if (equals(element, getRoot())) {
            updateDropDown();
            if (popupMenu != null) {
                refreshPopupMenu(popupMenu);
            } else if (popupList != null) {
                refreshPopupList(popupList);
            }
        } else {
            if (equals(element, getCurrentSelection())) {
                updateDropDown();
            }
            if (popupMenu != null) {
                updateAction(element);
            }
        }
    }

    protected void updateDropDown() {
        String text = null;
        Image image = null;
        Color textFg = null;
        Color textBg = null;
//        Point textSize = null;
        Point imageSize = null;
        Object currentSelection = getCurrentSelection();

        ILabelProvider labelProvider = (ILabelProvider) getWorkingHandleLabelProvider(
                ILabelProvider.class);
        if (labelProvider != null) {
            if (currentSelection == null)
                currentSelection = emptySelectionImitation;
            if (currentSelection != null) {
                text = labelProvider.getText(currentSelection);
                image = labelProvider.getImage(currentSelection);
            }

            if (!hasNoElement()) {
                for (Object element : elementList) {
                    Image i = labelProvider.getImage(element);
                    if (i != null) {
                        Rectangle b = i.getBounds();
                        imageSize = union(imageSize, b.width, b.height);
                    }
                }
            }
        }
        if (currentSelection != null) {
            IColorProvider colorProvider = (IColorProvider) getWorkingHandleLabelProvider(
                    IColorProvider.class);
            if (colorProvider != null) {
                textFg = colorProvider.getForeground(currentSelection);
                textBg = colorProvider.getBackground(currentSelection);
            }
            IToolTipProvider toolTipProvider = (IToolTipProvider) getWorkingHandleLabelProvider(
                    IToolTipProvider.class);
            if (toolTipProvider != null) {
                String tooltip = toolTipProvider.getToolTip(currentSelection);
                dropDownHandle.getControl().setToolTipText(tooltip);
            }
        }
        dropDownHandle.setText(text);
        dropDownHandle.setImage(image);
        dropDownHandle.setImageSize(imageSize);
        dropDownHandle.setTextForeground(textFg);
        dropDownHandle.setTextBackground(textBg);
    }

    private Object getWorkingHandleLabelProvider(Class<?> type) {
        if (handleLabelProvider != null && type.isInstance(handleLabelProvider))
            return handleLabelProvider;
        if (getLabelProvider() != null && type.isInstance(getLabelProvider()))
            return getLabelProvider();
        return null;
    }

    public void reveal(Object element) {
    }

    protected void setSelectionToWidget(List l, boolean reveal) {
        if (l != null && !l.isEmpty())
            selection = l.get(0);
        else
            selection = null;
        updateDropDown();
        if (popupMenu != null) {
            setSelectionToMenu(popupMenu);
        } else if (popupList != null) {
            popupList.setDefaultSelection(selection);
        }
    }

    public Control getControl() {
        return dropDownHandle.getControl();
    }

    public Object getCurrentSelection() {
        return selection;
    }

    public PopupFilteredList getPopupList() {
        return popupList;
    }

    public Menu getMenu() {
        return popupMenu == null ? null : popupMenu.getMenu();
    }

    public void open() {
        openPopup();
    }

    protected void openPopup() {
        if (filtered) {
            if (popupList == null) {
                popupList = createPopupList();
            }
            if (popupList != null) {
                openPopupList(popupList);
            }
        } else {
            if (popupMenu == null) {
                popupMenu = createPopupMenu();
            }
            if (popupMenu != null) {
                openPopupMenu(popupMenu);
            }
        }
    }

    protected void openPopupMenu(MenuManager menuManager) {
        Menu menu = menuManager.getMenu();
        if (menu != null && !menu.isDisposed()) {
            if (menu.isVisible()) {
                menu.setVisible(false);
            } else {
                locatePopupMenu(menu);
                menu.setVisible(true);
            }
        }
    }

    protected MenuManager createPopupMenu() {
        MenuManager popupMenu = new MenuManager();
        refreshPopupMenu(popupMenu);
        popupMenu.createContextMenu(getControl());
        return popupMenu;
    }

    protected void refreshPopupMenu(MenuManager menuManager) {
        menuManager.removeAll();
        if (actionMap == null) {
            actionMap = new HashMap<Object, IAction>();
        } else {
            actionMap.clear();
        }
        ILabelProvider labelProvider = (ILabelProvider) getLabelProvider();
        for (Object element : elementList) {
            if (separatorImitation != null && element == separatorImitation) {
                menuManager.add(new Separator());
            } else {
                IAction action = actionMap.get(element);
                if (action == null) {
                    action = new SelectionAction(element);
                    action.setChecked(false);
                    actionMap.put(element, action);
                }
                updateAction(action, labelProvider, element);
                menuManager.add(action);
            }
        }
        setSelectionToMenu(menuManager);
    }

    protected void locatePopupMenu(Menu menu) {
        Rectangle r;
        if (getControl() instanceof Composite) {
            r = ((Composite) getControl()).getClientArea();
        } else {
            r = getControl().getBounds();
            r.x = r.y = 0;
        }
        Point loc = getControl().toDisplay(r.x, r.y);
        loc.y += r.height;
        menu.setLocation(loc);
    }

    protected PopupFilteredList createPopupList() {
        PopupFilteredList list = new PopupFilteredList(getControl().getShell());
        refreshPopupList(list);
        list.addOpenListener(new IOpenListener() {
            public void open(OpenEvent event) {
                setSelection(event.getSelection());
            }
        });
        return list;
    }

    protected void refreshPopupList(PopupFilteredList list) {
        list.setPermitsUnprovidedElement(permitsUnprovidedElement());
        list.setPatternFilter(getPatternFilter());
        list.setContentProvider(getContentProvider());
        list.setLabelProvider(getLabelProvider());
        list.setFilters(getFilters());
        list.setSorter(getSorter());
        list.setComparator(getComparator());
        list.setComparer(getComparer());
        list.setDefaultSelection(getCurrentSelection());
        list.setInput(getInput());
    }

    protected void openPopupList(PopupFilteredList list) {
        locatePopupList(list);
        list.open();
        Shell shell = list.getShell();
        if (shell != null && !shell.isDisposed()) {
            dropDownHandle.setForceFocus(true);
            shell.addListener(SWT.Dispose, new Listener() {
                public void handleEvent(Event event) {
                    if (!dropDownHandle.getControl().isDisposed()) {
                        dropDownHandle.setForceFocus(false);
                    }
                }
            });
        }
    }

    protected void locatePopupList(PopupFilteredList list) {
        Rectangle r;
        if (getControl() instanceof Composite) {
            r = ((Composite) getControl()).getClientArea();
        } else {
            r = getControl().getBounds();
            r.x = r.y = 0;
        }
        Point loc = getControl().toDisplay(r.x, r.y);
        r.x = loc.x;
        r.y = loc.y;
        list.setBoundsReference(r);
    }

    protected void closePopup() {
        if (popupMenu != null) {
            Menu menu = popupMenu.getMenu();
            if (menu != null && !menu.isDisposed()) {
                menu.setVisible(false);
            }
        }
        if (popupList != null) {
            popupList.close();
        }
    }

    protected void setSelectionToMenu(MenuManager menuManager) {
        int index = getSelectionIndex();
        Menu menu = menuManager.getMenu();
        if (menu != null && !menu.isDisposed()) {
            if (index < 0 || index >= menu.getItemCount()) {
                menu.setDefaultItem(null);
            } else {
                menu.setDefaultItem(menu.getItem(index));
            }
        }
        if (actionMap != null) {
            for (Object element : elementList) {
                IAction action = actionMap.get(element);
                if (action != null) {
                    action.setChecked(equals(element, getCurrentSelection()));
                }
            }
        }
    }

    protected void handleDispose(DisposeEvent event) {
        if (handleLabelProvider != null) {
            handleLabelProvider.removeListener(handleLabelProviderListener);
            handleLabelProvider.dispose();
            handleLabelProvider = null;
        }
        super.handleDispose(event);
        closePopup();
        popupList = null;
        if (popupMenu != null) {
            popupMenu.dispose();
            popupMenu = null;
        }
        if (actionMap != null) {
            actionMap.clear();
            actionMap = null;
        }
    }

    public boolean permitsUnprovidedElement() {
        return permitsUnprovidedElement;
    }

    public void setPermitsUnprovidedElement(boolean permitsUnprovidedElement) {
        if (permitsUnprovidedElement == this.permitsUnprovidedElement)
            return;
        this.permitsUnprovidedElement = permitsUnprovidedElement;
        if (popupList != null) {
            popupList.setPermitsUnprovidedElement(permitsUnprovidedElement);
        }
    }

    public PatternFilter getPatternFilter() {
        return patternFilter;
    }

    public void setPatternFilter(PatternFilter patternFilter) {
        if (patternFilter == this.patternFilter)
            return;
        this.patternFilter = patternFilter;
        if (popupList != null) {
            popupList.setPatternFilter(patternFilter);
        }
    }

    protected static Point union(Point size, int width, int height) {
        if (size == null)
            return new Point(width, height);
        size.x = Math.max(size.x, width);
        size.y = Math.max(size.y, height);
        return size;
    }

    public Object getEmptySelectionImitation() {
        return emptySelectionImitation;
    }

    public void setEmptySelectionImitation(Object emptySelectionSubstitution) {
        this.emptySelectionImitation = emptySelectionSubstitution;
    }

    public Object getSeparatorImitation() {
        return separatorImitation;
    }

    public void setSeparatorImitation(Object separatorImitation) {
        this.separatorImitation = separatorImitation;
    }

    public void setEnabled(boolean enabled) {
        dropDownHandle.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return dropDownHandle.isEnabled();
    }

    public boolean isDropDownVisible() {
        if (filtered) {
            return popupList != null && popupList.getShell() != null
                    && !popupList.getShell().isDisposed()
                    && popupList.getShell().isVisible();
        }
        return popupMenu != null && popupMenu.getMenu() != null
                && !popupMenu.getMenu().isDisposed()
                && popupMenu.getMenu().isVisible();
    }

    public ILabelProvider getHandleLabelProvider() {
        return handleLabelProvider;
    }

    public void setHandleLabelProvider(ILabelProvider labelProvider) {
        ILabelProvider oldLabelProvider = this.handleLabelProvider;
        if (oldLabelProvider == labelProvider)
            return;

        if (oldLabelProvider != null) {
            oldLabelProvider.removeListener(handleLabelProviderListener);
        }
        this.handleLabelProvider = labelProvider;
        if (labelProvider != null) {
            labelProvider.addListener(handleLabelProviderListener);
        }
        refresh();

        if (oldLabelProvider != null) {
            oldLabelProvider.dispose();
        }
    }

    public void setHandlePaddings(int leftPadding, int topPadding,
            int rightPadding, int bottomPadding) {
        if (dropDownHandle != null) {
            dropDownHandle.setPaddings(leftPadding, topPadding, rightPadding,
                    bottomPadding);
        }
    }

    public void setHandleHorizontalPaddings(int leftPadding, int rightPadding) {
        if (dropDownHandle != null) {
            dropDownHandle.setHorizontalPaddings(leftPadding, rightPadding);
        }
    }

    public void setHandleVerticalPaddings(int topPadding, int bottomPadding) {
        if (dropDownHandle != null) {
            dropDownHandle.setVerticalPaddings(topPadding, bottomPadding);
        }
    }

    public int getHandleLeftPadding() {
        int leftPadding = 0;
        if (dropDownHandle != null) {
            leftPadding = dropDownHandle.getLeftPadding();
        }
        return leftPadding;
    }

    public int getHandleTopPadding() {
        int topPadding = 0;
        if (dropDownHandle != null) {
            topPadding = dropDownHandle.getTopPadding();
        }
        return topPadding;
    }

    public int getHandleRightPadding() {
        int rightPadding = 0;
        if (dropDownHandle != null) {
            rightPadding = dropDownHandle.getRightPadding();
        }
        return rightPadding;
    }

    public int getHandleBottomPadding() {
        int bottomPadding = 0;
        if (dropDownHandle != null) {
            bottomPadding = dropDownHandle.getBottomPadding();
        }
        return bottomPadding;
    }

}
