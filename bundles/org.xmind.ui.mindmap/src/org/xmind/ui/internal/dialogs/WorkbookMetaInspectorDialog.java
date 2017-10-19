package org.xmind.ui.internal.dialogs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IOpenListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.OpenEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
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
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.internal.forms.widgets.FormUtil;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.IFileEntry;
import org.xmind.core.IImage;
import org.xmind.core.IManifest;
import org.xmind.core.IMeta;
import org.xmind.core.IModifiable;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.IRelationship;
import org.xmind.core.IRevision;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.core.internal.dom.NumberUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.ModifyMetadataCommand;
import org.xmind.ui.forms.WidgetFactory;
import org.xmind.ui.internal.AttachmentImageDescriptor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.mindmap.ImageDownloader;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.internal.views.Messages;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.viewers.FileUtils;
import org.xmind.ui.viewers.IListLayout;
import org.xmind.ui.viewers.IListRenderer;
import org.xmind.ui.viewers.ImageLabel;
import org.xmind.ui.viewers.MListViewer;
import org.xmind.ui.viewers.StraightListLayout;

public class WorkbookMetaInspectorDialog extends Dialog {

    private static WorkbookMetaInspectorDialog instance;

    private static final int PROP_CONTROL = 1;

    private static final int PROP_CONTENT = 3;

    private static final int PROP_OUTGOING_SELECTION = 4;

    private static final String KEY_WIDGET_FACTORY = WorkbookMetaInspectorDialog.class
            .getName() + ".widgetFactory"; //$NON-NLS-1$

    private WidgetFactory factory;

    private Composite composite;

    private ScrolledForm form;

    private IEditorPart sourceEditor;

    private IWorkbook workbook;

    private CoreEventRegister eventRegister;

    private boolean reflowScheduled = false;

    private List<IWorkbookMetadataPart> parts = new ArrayList<IWorkbookMetadataPart>();

    private Map<IWorkbookMetadataPart, Section> sections = new HashMap<IWorkbookMetadataPart, Section>();

    private WorkbookMetaInspectorDialog(Shell shell) {
        super(shell);
        setShellStyle(
                SWT.RESIZE | SWT.CLOSE | SWT.MODELESS | SWT.BORDER | SWT.TITLE);
    }

    private static interface IWorkbookMetadataPart {

        // section text
        String getTitle();

        // controls visibility of this part
        boolean isVisible();

        // layout on the parent should be set in this method
        void createControl(Composite parent);

        // when triggered upon workbook change, the event is null
        // when triggered upon core event, the event is not null
        void refresh(IWorkbook workbook, CoreEvent event);

        void addPropertyListener(IPropertyListener listener);

        void removePropertyListener(IPropertyListener listener);

        Control getFocusControl();

    }

    private static interface IModifiableWorkbookMetadataPart {
        Command createModificationCommand(IWorkbook workbook);
    }

    protected static class WorkbookMetadata {

        private final Map<String, Object> delegate = new HashMap<String, Object>();

        public boolean set(String key, Object value) {
            Object oldValue = delegate.put(key, value);
            return value != oldValue
                    && (value == null || !value.equals(oldValue));
        }

        public Object get(String key) {
            return delegate.get(key);
        }

        public boolean delete(String key) {
            boolean hadValue = delegate.containsKey(key);
            delegate.remove(key);
            return hadValue;
        }

        public boolean deleteAll() {
            boolean hadValues = !delegate.isEmpty();
            delegate.clear();
            return hadValues;
        }

        // throws ClassCastException if value is not of Integer
        public int getInt(String key) {
            Object value = delegate.get(key);
            if (value != null)
                return ((Integer) value).intValue();
            return 0;
        }

        public boolean setInt(String key, int value) {
            if (value == 0)
                return delete(key);
            return set(key, Integer.valueOf(value));
        }

        public boolean increaseInt(String key, int delta) {
            int value = getInt(key);
            return setInt(key, value + delta);
        }

        public boolean decreaseInt(String key, int delta) {
            int value = getInt(key);
            return setInt(key, value - delta);
        }

        public long getLong(String key) {
            Object value = delegate.get(key);
            if (value != null)
                return ((Long) value).longValue();
            return 0;
        }

        public boolean setLong(String key, long value) {
            if (value == 0)
                return delete(key);
            return set(key, Long.valueOf(value));
        }

        public boolean increaseLong(String key, long delta) {
            long value = getLong(key);
            return setLong(key, value + delta);
        }

        public boolean decreaseLong(String key, long delta) {
            long value = getLong(key);
            return setLong(key, value - delta);
        }

        // throws ClassCastException if value is not of String
        public String getString(String key) {
            return (String) delegate.get(key);
        }

        public boolean setString(String key, String value) {
            if (value == null)
                return delete(key);
            return set(key, value);
        }

        // throws ClassCastException if value is not of Set
        @SuppressWarnings("unchecked")
        public <T> Set<T> getSet(String key) {
            Object value = delegate.get(key);
            if (value != null)
                return (Set<T>) value;
            return Collections.emptySet();
        }

        @SuppressWarnings("unchecked")
        public boolean addToSet(String key, Object object) {
            boolean changed = false;
            @SuppressWarnings("unchecked")
            Set set = (Set) delegate.get(key);
            if (set == null) {
                set = new HashSet();
                delegate.put(key, set);
                changed = true;
            }
            changed |= set.add(object);
            return changed;
        }

        public boolean removeFromSet(String key, Object object) {
            boolean changed = false;
            @SuppressWarnings("unchecked")
            Set set = (Set) delegate.get(key);
            if (set != null) {
                changed |= set.remove(object);
                if (set.isEmpty()) {
                    delegate.remove(key);
                    changed = true;
                }
            }
            return changed;
        }

        public int sizeOfSet(String key) {
            Set set = (Set) delegate.get(key);
            return set == null ? 0 : set.size();
        }

        public boolean containsInSet(String key, Object object) {
            Set set = (Set) delegate.get(key);
            return set != null && set.contains(object);
        }

        public boolean contains(String key) {
            return delegate.containsKey(key);
        }

        public Collection<String> keys() {
            return delegate.keySet();
        }

        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @SuppressWarnings("unchecked")
        public WorkbookMetadata copy() {
            WorkbookMetadata that = new WorkbookMetadata();
            for (String key : keys()) {
                Object value = get(key);
                if (value instanceof Set) {
                    value = new HashSet((Set) value);
                }
                that.set(key, value);
            }
            return that;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this)
                return true;
            if (obj == null || !(obj instanceof WorkbookMetadata))
                return false;
            WorkbookMetadata that = (WorkbookMetadata) obj;
            return this.delegate.equals(that.delegate);
        }

        @Override
        public int hashCode() {
            return this.delegate.hashCode();
        }

        @Override
        public String toString() {
            return this.delegate.toString();
        }

    }

    private static final String METADATA_AUTHOR_EMAIL = "author.email"; //$NON-NLS-1$
    private static final String METADATA_AUTHOR_NAME = "author.name"; //$NON-NLS-1$
    private static final String METADATA_AUTHOR_ORG = "author.org"; //$NON-NLS-1$

    private static final String METADATA_ESTIMATED_SIZE = "estimatedSize"; //$NON-NLS-1$
    private static final String METADATA_TOPIC_COUNT = "topicCount"; //$NON-NLS-1$
    private static final String METADATA_WORD_COUNT = "wordCount"; //$NON-NLS-1$
    private static final String METADATA_REVISION_COUNT = "revisionCount"; //$NON-NLS-1$
    private static final String METADATA_MODIFICATION_TIME = "modificationTime"; //$NON-NLS-1$
    private static final String METADATA_MODIFIER_NAME = "modifierName"; //$NON-NLS-1$
    private static final String METADATA_CREATION_TIME = "creationTime"; //$NON-NLS-1$

    private static final String METADATA_ATTACHMENTS = "attachments"; //$NON-NLS-1$
    private static final String METADATA_EXTERNAL_FILES = "externalFiles"; //$NON-NLS-1$
    private static final String METADATA_HYPERLINKS = "hyperlinks"; //$NON-NLS-1$
    private static final String METADATA_IMAGES = "images"; //$NON-NLS-1$

    private static abstract class AbstractWorkbookMetadataPart
            implements IWorkbookMetadataPart {

        private ListenerList listeners = new ListenerList();

        private Control focusControl = null;

//        private Listener focusControlUpdater = new Listener() {
//            public void handleEvent(Event event) {
//                focusControl = (Control) event.widget;
//                firePropertyChange(PROP_FOCUS_CONTROL);
//            }
//        };

        private boolean refreshScheduled = false;

        protected final WorkbookMetadata metadata = new WorkbookMetadata();

        public boolean isVisible() {
            return !metadata.isEmpty();
        }

        public void addPropertyListener(IPropertyListener listener) {
            listeners.add(listener);
        }

        public void removePropertyListener(IPropertyListener listener) {
            listeners.remove(listener);
        }

        public Control getFocusControl() {
            return focusControl;
        }

        protected void firePropertyChange(final int propId) {
            final Object source = this;
            for (final Object listener : listeners.getListeners()) {
                SafeRunner.run(new SafeRunnable() {
                    public void run() throws Exception {
                        ((IPropertyListener) listener).propertyChanged(source,
                                propId);
                    }
                });
            }
        }

        protected void updateText(Text control, String text) {
            if (control == null || control.isDisposed())
                return;

            if (text == null)
                text = ""; //$NON-NLS-1$
            if (text.equals(control.getText()))
                return;

            Point selection = new Point(text.length(), text.length());
            if (control.isFocusControl()) {
                selection.x = Math.min(selection.x, control.getSelection().x);
                selection.y = Math.min(selection.y, control.getSelection().y);
            }
            control.setText(text);
            if (control.isFocusControl()) {
                control.setSelection(selection);
            }
            firePropertyChange(PROP_CONTROL);
        }

        protected abstract void refreshControls();

        protected void refreshAsynchronously() {
            if (refreshScheduled)
                return;

            Display display = Display.getCurrent();
            if (display == null)
                return;

            display.asyncExec(new Runnable() {
                public void run() {
                    refreshScheduled = false;
                    refreshControls();
                }
            });
            refreshScheduled = true;
        }

    }

    private static class WorkbookMetadataListRow extends Composite {

        private ImageLabel imageLabel;

        private Label textLabel;

        private boolean selected;

        private Color background;

        private Listener listener = new Listener() {
            public void handleEvent(Event event) {
                if (event.type == SWT.MouseDown) {
                    getParent().setFocus();
                    handleMouseDown(event);
                } else if (event.type == SWT.MouseDoubleClick) {
                    handleMouseDoubleClick(event);
                }
            }
        };

        public WorkbookMetadataListRow(Composite parent,
                WidgetFactory factory) {
            super(parent, SWT.NO_FOCUS);
            selected = false;
            background = super.getBackground();
            factory.adapt(this, true, true);
            setMenu(parent.getMenu());

            hookControl(this);

            GridLayout layout = new GridLayout(2, false);
            layout.marginWidth = 2;
            layout.marginHeight = 1;
            layout.horizontalSpacing = 2;
            layout.verticalSpacing = 0;
            setLayout(layout);

            imageLabel = new ImageLabel(this, SWT.NO_FOCUS);
            factory.adapt(imageLabel, true, true);
            imageLabel.setScaleHint(ImageLabel.SCALE_TO_FIT);
            imageLabel.setHorizontalAlignment(SWT.CENTER);
            imageLabel.setVerticalAlignment(SWT.CENTER);
            GridData imageLayoutData = new GridData(SWT.CENTER, SWT.CENTER,
                    false, true);
            imageLayoutData.exclude = true;
            imageLabel.setLayoutData(imageLayoutData);
            imageLabel.setVisible(false);
            imageLabel.setBackground(
                    parent.getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
            imageLabel.setMenu(getMenu());
            hookControl(imageLabel);

            textLabel = factory.createLabel(this, "", SWT.NO_FOCUS); //$NON-NLS-1$
            textLabel.setLayoutData(
                    new GridData(SWT.FILL, SWT.CENTER, true, true));
            textLabel.setBackground(
                    parent.getDisplay().getSystemColor(SWT.COLOR_TRANSPARENT));
            textLabel.setMenu(getMenu());
            hookControl(textLabel);
        }

        public void setImageSizeHint(Point hint) {
            GridData layoutData = (GridData) imageLabel.getLayoutData();
            layoutData.widthHint = hint.x;
            layoutData.heightHint = hint.y;
            layout(true);
        }

        private void hookControl(Control c) {
            c.addListener(SWT.MouseDown, listener);
            c.addListener(SWT.MouseDoubleClick, listener);
        }

        public void setImage(Image image) {
            checkWidget();
            imageLabel.setImage(image);
            boolean visible = image != null;
            imageLabel.setVisible(visible);
            ((GridData) imageLabel.getLayoutData()).exclude = !visible;
            layout(true);
        }

        public void setText(String text) {
            checkWidget();
            textLabel.setText(text);
            layout(true);
        }

        public boolean getSelection() {
            return selected;
        }

        public void setSelection(boolean selection) {
            checkWidget();
            if (selection == this.selected)
                return;

            this.selected = selection;
            updateBackground();
        }

        @Override
        public Color getBackground() {
            return background;
        }

        @Override
        public void setBackground(Color color) {
            checkWidget();
            this.background = color;
            updateBackground();
        }

        private void updateBackground() {
            if (getSelection()) {
                super.setBackground(
                        getDisplay().getSystemColor(SWT.COLOR_LIST_SELECTION));
            } else {
                super.setBackground(background);
            }
        }

        private void handleMouseDown(Event event) {
            Control[] siblings = getParent().getChildren();
            for (int i = 0; i < siblings.length; i++) {
                Control item = siblings[i];
                if (item instanceof WorkbookMetadataListRow) {
                    ((WorkbookMetadataListRow) item).setSelection(false);
                }
            }
            setSelection(true);
            getParent().notifyListeners(SWT.Selection, new Event());
        }

        private void handleMouseDoubleClick(Event event) {
            getParent().notifyListeners(SWT.DefaultSelection, new Event());
        }

    }

    private static class WorkbookMetadataListLabelProvider extends LabelProvider
            implements IListRenderer {

        private StraightListLayout layout = new StraightListLayout(
                SWT.VERTICAL);

        public IListLayout getListLayout(MListViewer viewer) {
            return layout;
        }

        public Control createListItemForElement(MListViewer viewer,
                Composite parent, Object element) {
            WidgetFactory factory = (WidgetFactory) viewer.getControl()
                    .getData(KEY_WIDGET_FACTORY);
            WorkbookMetadataListRow row = new WorkbookMetadataListRow(parent,
                    factory);
            return row;
        }

        public void updateListItem(MListViewer viewer, Object element,
                Control item) {
            WorkbookMetadataListRow row = (WorkbookMetadataListRow) item;
            row.setText(getText(element));
            row.setImage(getImage(element));
        }

        public int getListItemState(MListViewer viewer, Control item) {
            int state = STATE_NONE;
            if (item instanceof WorkbookMetadataListRow) {
                WorkbookMetadataListRow row = (WorkbookMetadataListRow) item;
                if (row.getSelection()) {
                    state |= STATE_SELECTED;
                }
            }
            return state;
        }

        public void setListItemState(MListViewer viewer, Control item,
                int state) {
            if (item instanceof WorkbookMetadataListRow) {
                WorkbookMetadataListRow row = (WorkbookMetadataListRow) item;
                row.setSelection((state & STATE_SELECTED) != 0);
            }
        }

    }

    private static abstract class AbstractWorkbookMetadataListPart
            extends AbstractWorkbookMetadataPart implements IAdaptable {

        private MListViewer viewer;

        protected abstract Object[] getElements(WorkbookMetadata metadata);

        protected abstract String getText(Object element);

        protected abstract Image getImage(Object element,
                ResourceManager resourceManager);

        protected Point getImageSizeHint() {
            return new Point(SWT.DEFAULT, SWT.DEFAULT);
        }

        protected void update(Object element) {
            viewer.update(element, null);
            firePropertyChange(PROP_CONTROL);
        }

        protected MListViewer getViewer() {
            return viewer;
        }

        public void createControl(final Composite parent) {
            WidgetFactory factory = (WidgetFactory) parent
                    .getData(KEY_WIDGET_FACTORY);

            GridLayout layout = new GridLayout();
            layout.numColumns = 1;
            layout.makeColumnsEqualWidth = false;
            layout.marginWidth = 2;
            layout.marginHeight = 2;
            layout.horizontalSpacing = 3;
            layout.verticalSpacing = 3;
            parent.setLayout(layout);

            viewer = new MListViewer(parent, SWT.NONE);
            factory.adapt(viewer.getControl(), true, true);
            viewer.getControl().setMenu(parent.getMenu());
            viewer.getControl().setData(KEY_WIDGET_FACTORY, factory);
            viewer.getControl().setLayoutData(
                    new GridData(SWT.FILL, SWT.FILL, true, true));
//            hookFocusableControl(viewer.getControl());

            viewer.setContentProvider(createContentProvider());
            viewer.setLabelProvider(createLabelProvider());

            viewer.setInput(metadata);

            viewer.addOpenListener(new IOpenListener() {
                public void open(OpenEvent event) {
                    firePropertyChange(PROP_OUTGOING_SELECTION);
                }
            });

            final ScrolledComposite scrolledComposite = FormUtil
                    .getScrolledComposite(parent);
            if (scrolledComposite != null) {
                viewer.getControl().addListener(SWT.FocusOut, new Listener() {
                    public void handleEvent(Event event) {
                        Display.getCurrent().asyncExec(new Runnable() {
                            public void run() {
                                Display display = Display.getCurrent();
                                if (display == null || display.isDisposed()
                                        || scrolledComposite.isDisposed())
                                    return;

                                Control focusControl = display
                                        .getFocusControl();
                                if (containsControl(scrolledComposite,
                                        focusControl)
                                        && !containsControl(parent,
                                                focusControl)) {
                                    viewer.setSelection(
                                            StructuredSelection.EMPTY);
                                }
                            }
                        });
                    }

                    private boolean containsControl(Composite composite,
                            Control c) {
                        while (c != null) {
                            if (c == composite)
                                return true;
                            c = c.getParent();
                        }
                        return false;
                    }
                });
            }
        }

        @Override
        protected void refreshControls() {
            if (viewer == null || viewer.getControl() == null
                    || viewer.getControl().isDisposed())
                return;

            viewer.refresh();
            firePropertyChange(PROP_CONTROL);
        }

        protected IContentProvider createContentProvider() {
            return new IStructuredContentProvider() {

                public void inputChanged(Viewer viewer, Object oldInput,
                        Object newInput) {
                }

                public void dispose() {
                }

                public Object[] getElements(Object inputElement) {
                    WorkbookMetadata metadata = (WorkbookMetadata) inputElement;
                    return AbstractWorkbookMetadataListPart.this
                            .getElements(metadata);
                }
            };
        }

        private IBaseLabelProvider createLabelProvider() {
            return new WorkbookMetadataListLabelProvider() {

                ResourceManager rm = new LocalResourceManager(
                        JFaceResources.getResources());

                @Override
                public String getText(Object element) {
                    return AbstractWorkbookMetadataListPart.this
                            .getText(element);
                }

                @Override
                public Image getImage(Object element) {
                    return AbstractWorkbookMetadataListPart.this
                            .getImage(element, rm);
                }

                @Override
                public Control createListItemForElement(MListViewer viewer,
                        Composite parent, Object element) {
                    Control item = super.createListItemForElement(viewer,
                            parent, element);
                    if (item instanceof WorkbookMetadataListRow) {
                        ((WorkbookMetadataListRow) item)
                                .setImageSizeHint(getImageSizeHint());
                    }
                    return item;
                }

                @Override
                public void dispose() {
                    super.dispose();
                    rm.dispose();
                }
            };
        }

        public <T> T getAdapter(Class<T> adapter) {
            if (adapter == ISelectionProvider.class)
                return adapter.cast(viewer);
            return null;
        }

    }

    private static class TopicViewerComparator extends ViewerComparator {

        @Override
        public int compare(Viewer viewer, Object e1, Object e2) {
            return Core.getTopicComparator().compare((ITopic) e1, (ITopic) e2);
        }
    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    ////  Author Info Section
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    private class AuthorInfoPart extends AbstractWorkbookMetadataPart
            implements IModifiableWorkbookMetadataPart {

        private Text nameInput;
        private Text emailInput;
        private Text orgInput;

        private Listener modifyListener = new Listener() {
            public void handleEvent(Event event) {
                firePropertyChange(PROP_CONTENT);
            }
        };

        public boolean isVisible() {
            return true;
        }

        public String getTitle() {
            return Messages.AuthorInfoInspectorSection_title;
        }

        public void createControl(Composite parent) {
            WidgetFactory factory = (WidgetFactory) parent
                    .getData(KEY_WIDGET_FACTORY);

            GridLayout layout = new GridLayout();
            layout.marginWidth = 2;
            layout.marginHeight = 2;
            layout.horizontalSpacing = 5;
            layout.verticalSpacing = 3;
            layout.numColumns = 2;
            parent.setLayout(layout);

            createLabel(parent, factory,
                    Messages.AuthorInfoInspectorSection_Name);
            nameInput = createText(parent, factory);

            createLabel(parent, factory,
                    Messages.AuthorInfoInspectorSection_Email);
            emailInput = createText(parent, factory);

            createLabel(parent, factory,
                    Messages.AuthorInfoInspectorSection_Organization);
            orgInput = createText(parent, factory);
        }

        private Label createLabel(Composite parent, WidgetFactory factory,
                String text) {
            Label label = factory.createLabel(parent, text);
            label.setLayoutData(
                    new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            return label;
        }

        private Text createText(Composite parent, WidgetFactory factory) {
            Text text = factory.createText(parent, "", SWT.SINGLE); //$NON-NLS-1$
            text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            text.addListener(SWT.DefaultSelection, modifyListener);
            text.addListener(SWT.FocusOut, modifyListener);
            return text;
        }

        public void refresh(IWorkbook workbook, CoreEvent event) {
            boolean changed = false;
            if (workbook == null) {
                changed |= metadata.setString(METADATA_AUTHOR_NAME, ""); //$NON-NLS-1$
                changed |= metadata.setString(METADATA_AUTHOR_EMAIL, ""); //$NON-NLS-1$
                changed |= metadata.setString(METADATA_AUTHOR_ORG, ""); //$NON-NLS-1$
            } else if (event == null) {
                IMeta meta = workbook.getMeta();
                changed |= metadata.setString(METADATA_AUTHOR_NAME,
                        meta.getValue(IMeta.AUTHOR_NAME));
                changed |= metadata.setString(METADATA_AUTHOR_EMAIL,
                        meta.getValue(IMeta.AUTHOR_EMAIL));
                changed |= metadata.setString(METADATA_AUTHOR_ORG,
                        meta.getValue(IMeta.AUTHOR_ORG));
            } else if (Core.Metadata.equals(event.getType())) {
                if (IMeta.AUTHOR_NAME.equals(event.getTarget())) {
                    changed |= metadata.setString(METADATA_AUTHOR_NAME,
                            (String) event.getNewValue());
                } else if (IMeta.AUTHOR_EMAIL.equals(event.getTarget())) {
                    changed |= metadata.setString(METADATA_AUTHOR_EMAIL,
                            (String) event.getNewValue());
                } else if (IMeta.AUTHOR_ORG.equals(event.getTarget())) {
                    changed |= metadata.setString(METADATA_AUTHOR_ORG,
                            (String) event.getNewValue());
                }
            }

            if (changed) {
                refreshAsynchronously();
            }
        }

        @Override
        protected void refreshControls() {
            String name = metadata.getString(METADATA_AUTHOR_NAME);
            if (name == null || "".equals(name)) //$NON-NLS-1$
                name = System.getProperty("user.name"); //$NON-NLS-1$
            updateText(nameInput, name);
            updateText(emailInput, metadata.getString(METADATA_AUTHOR_EMAIL));
            updateText(orgInput, metadata.getString(METADATA_AUTHOR_ORG));
        }

        public Command createModificationCommand(IWorkbook workbook) {
            String newName = nameInput.getText();
            String newEmail = emailInput.getText();
            String newOrg = orgInput.getText();
            Command command = new CompoundCommand( //
                    new ModifyMetadataCommand(workbook, IMeta.AUTHOR_NAME,
                            newName), //
                    new ModifyMetadataCommand(workbook, IMeta.AUTHOR_EMAIL,
                            newEmail), //
                    new ModifyMetadataCommand(workbook, IMeta.AUTHOR_ORG,
                            newOrg) //
            );
            if (!command.canExecute())
                return null;

            command.setLabel(MindMapMessages.WorkbookMetadata_ModifyAuthorInfo);
            return command;
        }

    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    ////  Summary Section
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    private static final int PRIMARY_BYTES = 5300;
    private static final int TOPIC_DEFAULT_BYTES = 160;

    private static class WorkbookSummaryPart
            extends AbstractWorkbookMetadataPart {

        private Text estimatedSizeText;
        private Text topicCountText;
        private Text wordCountText;
        private Text revisionCountText;
        private Text modificationTimeText;
        private Text modifierNameText;
        private Text creationTimeText;

        public boolean isVisible() {
            return true;
        }

        public String getTitle() {
            return Messages.FileInfoInspectorSection_title;
        }

        public void createControl(Composite parent) {
            WidgetFactory factory = (WidgetFactory) parent
                    .getData(KEY_WIDGET_FACTORY);

            GridLayout layout = new GridLayout();
            layout.marginWidth = 2;
            layout.marginHeight = 2;
            layout.horizontalSpacing = 5;
            layout.verticalSpacing = 3;
            layout.numColumns = 2;
            parent.setLayout(layout);

            estimatedSizeText = createTextWithLabel(parent, factory,
                    Messages.FileInfoEstimateSize_label);

            topicCountText = createTextWithLabel(parent, factory,
                    Messages.FileInfoTopics_label);

            wordCountText = createTextWithLabel(parent, factory,
                    Messages.FileInfoWords_label);

            revisionCountText = createTextWithLabel(parent, factory,
                    Messages.FileInfoRevisions_label);

            modificationTimeText = createTextWithLabel(parent, factory,
                    Messages.FileInfoModifiedTime_label);

            modifierNameText = createTextWithLabel(parent, factory,
                    Messages.FileInfoModifiedBy_label);

            creationTimeText = createTextWithLabel(parent, factory,
                    Messages.FileInfoCreatedTime_label);
        }

        private Text createTextWithLabel(Composite parent,
                WidgetFactory factory, String labelText) {
            Label label = factory.createLabel(parent, labelText);
            label.setLayoutData(
                    new GridData(SWT.BEGINNING, SWT.CENTER, false, false));

            Text text = new Text(parent,
                    SWT.READ_ONLY | SWT.SINGLE | factory.getOrientation());
            factory.adapt(text, true, false);
            text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            return text;
        }

        public void refresh(IWorkbook workbook, CoreEvent event) {
            WorkbookMetadata oldMetadata = metadata.copy();

            if (workbook == null) {
                metadata.deleteAll();
            } else if (event == null) {
                workbookChanged(workbook);
            } else if (Core.SheetAdd.equals(event.getType())) {
                sheetAdded((ISheet) event.getTarget());
            } else if (Core.SheetRemove.equals(event.getType())) {
                sheetRemoved((ISheet) event.getTarget());
            } else if (Core.TopicAdd.equals(event.getType())) {
                topicAdded((ITopic) event.getTarget());
            } else if (Core.TopicRemove.equals(event.getType())) {
                topicRemoved((ITopic) event.getTarget());
            } else if (Core.TitleText.equals(event.getType())) {
                titleTextChanged((String) event.getOldValue(),
                        (String) event.getNewValue());
            } else if (Core.TopicNotes.equals(event.getType())) {
                if (INotes.PLAIN.equals(event.getTarget())) {
                    notesChanged((INotesContent) event.getOldValue(),
                            (INotesContent) event.getNewValue());
                }
            } else if (Core.FileEntryAdd.equals(event.getType())) {
                fileEntryAdded((IFileEntry) event.getTarget());
            } else if (Core.FileEntryRemove.equals(event.getType())) {
                fileEntryRemoved((IFileEntry) event.getTarget());
            } else if (Core.RelationshipAdd.equals(event.getType())) {
                relationshipAdded((IRelationship) event.getTarget());
            } else if (Core.RelationshipRemove.equals(event.getType())) {
                relationshipRemoved((IRelationship) event.getTarget());
            } else if (Core.BoundaryAdd.equals(event.getType())) {
                boundaryAdded((IBoundary) event.getTarget());
            } else if (Core.BoundaryRemove.equals(event.getType())) {
                boundaryRemoved((IBoundary) event.getTarget());
            } else if (Core.RevisionAdd.equals(event.getType())) {
                metadata.increaseInt(METADATA_REVISION_COUNT, 1);
            } else if (Core.RevisionRemove.equals(event.getType())) {
                metadata.decreaseInt(METADATA_REVISION_COUNT, 1);
            } else if (Core.ModifyTime.equals(event.getType())
                    || Core.WorkbookSave.equals(event.getType())) {
                IModifiable source = (IModifiable) event.getSource();
                metadata.setLong(METADATA_MODIFICATION_TIME,
                        source.getModifiedTime());
                metadata.setString(METADATA_MODIFIER_NAME,
                        source.getModifiedBy());
            }

            if (!metadata.equals(oldMetadata)) {
                refreshAsynchronously();
            }
        }

        @Override
        protected void refreshControls() {
            updateText(topicCountText,
                    String.valueOf(metadata.getInt(METADATA_TOPIC_COUNT)));
            updateText(wordCountText,
                    String.valueOf(metadata.getInt(METADATA_WORD_COUNT)));
            updateText(estimatedSizeText, FileUtils.fileLengthToString(
                    metadata.getLong(METADATA_ESTIMATED_SIZE)));
            updateText(revisionCountText,
                    String.valueOf(metadata.getInt(METADATA_REVISION_COUNT)));
            updateText(modificationTimeText, NumberUtils
                    .formatDate(metadata.getLong(METADATA_MODIFICATION_TIME)));
            updateText(modifierNameText,
                    metadata.getString(METADATA_MODIFIER_NAME));
            updateText(creationTimeText,
                    metadata.getString(METADATA_CREATION_TIME));
        }

        private void workbookChanged(IWorkbook workbook) {
            metadata.deleteAll();

            metadata.setLong(METADATA_MODIFICATION_TIME,
                    workbook.getModifiedTime());
            String name = workbook.getModifiedBy();
            if (name == null || "".equals(name)) //$NON-NLS-1$
                name = System.getProperty("user.name"); //$NON-NLS-1$
            metadata.setString(METADATA_MODIFIER_NAME, name);
            metadata.setString(METADATA_CREATION_TIME,
                    workbook.getMeta().getValue(IMeta.CREATED_TIME));

            metadata.setLong(METADATA_ESTIMATED_SIZE, PRIMARY_BYTES);

            for (ISheet sheet : workbook.getSheets()) {
                sheetAdded(sheet);
            }
            Iterator<IFileEntry> entryIter = workbook.getManifest()
                    .iterFileEntries();
            while (entryIter.hasNext()) {
                fileEntryAdded(entryIter.next());
            }
        }

        private void sheetAdded(ISheet sheet) {
            titleTextChanged(null, sheet.getTitleText());
            topicAdded(sheet.getRootTopic());
            metadata.increaseInt(METADATA_REVISION_COUNT,
                    sheet.getOwnedWorkbook().getRevisionRepository()
                            .getRevisionManager(sheet.getId(), IRevision.SHEET)
                            .getRevisions().size());
            for (IRelationship r : sheet.getRelationships()) {
                relationshipAdded(r);
            }
        }

        private void sheetRemoved(ISheet sheet) {
            for (IRelationship r : sheet.getRelationships()) {
                relationshipRemoved(r);
            }

            metadata.decreaseInt(METADATA_REVISION_COUNT,
                    sheet.getOwnedWorkbook().getRevisionRepository()
                            .getRevisionManager(sheet.getId(), IRevision.SHEET)
                            .getRevisions().size());
            topicRemoved(sheet.getRootTopic());
            titleTextChanged(sheet.getTitleText(), null);
        }

        private void topicAdded(ITopic topic) {
            metadata.increaseInt(METADATA_TOPIC_COUNT, 1);

            metadata.increaseLong(METADATA_ESTIMATED_SIZE, TOPIC_DEFAULT_BYTES);

            titleTextChanged(null, topic.getTitleText());
            notesChanged(null, topic.getNotes().getContent(INotes.PLAIN));

            Iterator<ITopic> childIter = topic.getAllChildrenIterator();
            while (childIter.hasNext()) {
                topicAdded(childIter.next());
            }

            for (IBoundary boundary : topic.getBoundaries()) {
                boundaryAdded(boundary);
            }
        }

        private void topicRemoved(ITopic topic) {
            for (IBoundary boundary : topic.getBoundaries()) {
                boundaryRemoved(boundary);
            }

            Iterator<ITopic> childIter = topic.getAllChildrenIterator();
            while (childIter.hasNext()) {
                topicRemoved(childIter.next());
            }

            notesChanged(topic.getNotes().getContent(INotes.PLAIN), null);
            titleTextChanged(topic.getTitleText(), null);

            metadata.decreaseLong(METADATA_ESTIMATED_SIZE, TOPIC_DEFAULT_BYTES);

            metadata.decreaseInt(METADATA_TOPIC_COUNT, 1);
        }

        private void titleTextChanged(String oldTitle, String newTitle) {
            if (oldTitle != null) {
                metadata.decreaseLong(METADATA_ESTIMATED_SIZE,
                        oldTitle.length());
                metadata.decreaseInt(METADATA_WORD_COUNT, countWords(oldTitle));
            }
            if (newTitle != null) {
                metadata.increaseLong(METADATA_ESTIMATED_SIZE,
                        newTitle.length());
                metadata.increaseInt(METADATA_WORD_COUNT, countWords(newTitle));
            }
        }

        private void notesChanged(INotesContent oldNotes,
                INotesContent newNotes) {
            if (oldNotes instanceof IPlainNotesContent) {
                String content = ((IPlainNotesContent) oldNotes)
                        .getTextContent();
                if (content != null) {
                    metadata.decreaseInt(METADATA_WORD_COUNT,
                            countWords(content));
                }
            }

            if (newNotes instanceof IPlainNotesContent) {
                String content = ((IPlainNotesContent) newNotes)
                        .getTextContent();
                if (content != null) {
                    metadata.increaseInt(METADATA_WORD_COUNT,
                            countWords(content));
                }
            }
        }

        private void fileEntryAdded(IFileEntry entry) {
            metadata.increaseLong(METADATA_ESTIMATED_SIZE, entry.getSize());
        }

        private void fileEntryRemoved(IFileEntry entry) {
            metadata.decreaseLong(METADATA_ESTIMATED_SIZE, entry.getSize());
        }

        private void relationshipAdded(IRelationship r) {
            titleTextChanged(null, r.getTitleText());
        }

        private void relationshipRemoved(IRelationship r) {
            titleTextChanged(r.getTitleText(), null);
        }

        private void boundaryAdded(IBoundary b) {
            titleTextChanged(null, b.getTitleText());
        }

        private void boundaryRemoved(IBoundary b) {
            titleTextChanged(b.getTitleText(), null);
        }

        private static int countWords(String s) {
            int total = s.length();
            int count = 0;
            boolean inWord = false;
            char c;
            for (int i = 0; i < total; i++) {
                c = s.charAt(i);
                if (isOneWordCharacter(c)) {
                    if (inWord)
                        count++;
                    count++;
                    inWord = false;
                } else if (Character.isLetter(c) || Character.isDigit(c)) {
                    inWord = true;
                } else {
                    if (inWord)
                        count++;
                    inWord = false;
                }
            }
            if (inWord)
                count++;
            return count;
        }

        private static boolean isOneWordCharacter(char c) {
            Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
            if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                    || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                    || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                    || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
                    || ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
                    || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
                return true;
            }
            return false;
        }

    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    ////  Attachments Section
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    private static class AttachmentListPart
            extends AbstractWorkbookMetadataListPart {

        public String getTitle() {
            return NLS.bind(Messages.AttachmentsInspectorSection_title,
                    metadata.sizeOfSet(METADATA_ATTACHMENTS));
        }

        public void refresh(IWorkbook workbook, CoreEvent event) {
            WorkbookMetadata oldMetadata = metadata.copy();
            Object toUpdate = null;

            if (workbook == null) {
                metadata.deleteAll();
            } else if (event == null) {
                metadata.deleteAll();
                for (ISheet sheet : workbook.getSheets()) {
                    topicAdded(sheet.getRootTopic());
                }
            } else if (Core.TopicAdd.equals(event.getType())) {
                topicAdded((ITopic) event.getTarget());
            } else if (Core.TopicRemove.equals(event.getType())) {
                topicRemoved((ITopic) event.getTarget());
            } else if (Core.SheetAdd.equals(event.getType())) {
                topicAdded(((ISheet) event.getTarget()).getRootTopic());
            } else if (Core.SheetRemove.equals(event.getType())) {
                topicRemoved(((ISheet) event.getTarget()).getRootTopic());
            } else if (Core.TopicHyperlink.equals(event.getType())) {
                ITopic source = (ITopic) event.getSource();
                linkChanged(source, (String) event.getOldValue(),
                        (String) event.getNewValue());
                if (metadata.containsInSet(METADATA_ATTACHMENTS, source)) {
                    toUpdate = source;
                }
            } else if (Core.TitleText.equals(event.getType())) {
                Object source = event.getSource();
                if (metadata.containsInSet(METADATA_ATTACHMENTS, source)) {
                    toUpdate = source;
                }
            }

            if (!metadata.equals(oldMetadata)) {
                refreshAsynchronously();
            } else if (toUpdate != null) {
                update(toUpdate);
            }
        }

        private void topicAdded(ITopic topic) {
            linkChanged(topic, null, topic.getHyperlink());

            Iterator<ITopic> childIter = topic.getAllChildrenIterator();
            while (childIter.hasNext()) {
                topicAdded(childIter.next());
            }
        }

        private void topicRemoved(ITopic topic) {
            Iterator<ITopic> childIter = topic.getAllChildrenIterator();
            while (childIter.hasNext()) {
                topicRemoved(childIter.next());
            }

            linkChanged(topic, topic.getHyperlink(), null);
        }

        private void linkChanged(ITopic topic, String oldLink, String newLink) {
            IFileEntry oldEntry = findFileEntry(topic, oldLink);
            if (oldEntry != null) {
                metadata.removeFromSet(METADATA_ATTACHMENTS, topic);
            }

            IFileEntry newEntry = findFileEntry(topic, newLink);
            if (newEntry != null) {
                metadata.addToSet(METADATA_ATTACHMENTS, topic);
            }
        }

        private IFileEntry findFileEntry(ITopic topic, String link) {
            if (link == null || !HyperlinkUtils.isAttachmentURL(link))
                return null;

            IManifest manifest = topic.getOwnedWorkbook().getManifest();
            if (manifest == null)
                return null;

            return manifest.getFileEntry(HyperlinkUtils.toAttachmentPath(link));
        }

        @Override
        public void createControl(Composite parent) {
            super.createControl(parent);
            getViewer().setComparator(new TopicViewerComparator());
        }

        @Override
        protected Object[] getElements(WorkbookMetadata metadata) {
            return metadata.getSet(METADATA_ATTACHMENTS).toArray();
        }

        @Override
        protected String getText(Object element) {
            if (!(element instanceof ITopic))
                return ""; //$NON-NLS-1$

            ITopic topic = (ITopic) element;
            String fileName = topic.getTitleText();
            IFileEntry entry = findFileEntry(topic, topic.getHyperlink());
            if (entry == null)
                return fileName;

            long size = entry.getSize();
            return String.format("%s (%s)", fileName, //$NON-NLS-1$
                    FileUtils.fileLengthToString(size));
        }

        @Override
        protected Image getImage(Object element,
                ResourceManager resourceManager) {
            if (!(element instanceof ITopic))
                return null;

            ITopic topic = (ITopic) element;
            String fileName = topic.getTitleText();
            ImageDescriptor icon = MindMapUI.getImages().getFileIcon(fileName,
                    true);
            if (icon == null) {
                icon = MindMapUI.getImages().get(IMindMapImages.UNKNOWN_FILE,
                        true);
            }
            return (Image) resourceManager.get(icon);
        }

    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    ////  External Files Section
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    private static class ExternalFileListPart
            extends AbstractWorkbookMetadataListPart {

        public String getTitle() {
            return NLS.bind(Messages.ExternalFilesInspectorSection_title,
                    metadata.sizeOfSet(METADATA_EXTERNAL_FILES));
        }

        public void refresh(IWorkbook workbook, CoreEvent event) {
            WorkbookMetadata oldMetadata = this.metadata.copy();
            Object toUpdate = null;

            if (workbook == null) {
                metadata.deleteAll();
            } else if (event == null) {
                metadata.deleteAll();
                workbookChanged(workbook);
            } else if (Core.SheetAdd.equals(event.getType())) {
                topicAdded(((ISheet) event.getTarget()).getRootTopic());
            } else if (Core.SheetRemove.equals(event.getType())) {
                topicRemoved(((ISheet) event.getTarget()).getRootTopic());
            } else if (Core.TopicAdd.equals(event.getType())) {
                topicAdded((ITopic) event.getTarget());
            } else if (Core.TopicRemove.equals(event.getType())) {
                topicRemoved((ITopic) event.getTarget());
            } else if (Core.TopicHyperlink.equals(event.getType())) {
                ITopic source = (ITopic) event.getSource();
                linkChanged(source, (String) event.getOldValue(),
                        (String) event.getNewValue());
                if (metadata.containsInSet(METADATA_EXTERNAL_FILES, source)) {
                    toUpdate = source;
                }
            } else if (Core.TitleText.equals(event.getType())) {
                Object source = event.getSource();
                if (metadata.containsInSet(METADATA_EXTERNAL_FILES, source)) {
                    toUpdate = source;
                }
            }

            if (!metadata.equals(oldMetadata)) {
                refreshAsynchronously();
            } else if (toUpdate != null) {
                update(toUpdate);
            }

        }

        @Override
        public void createControl(Composite parent) {
            super.createControl(parent);
            getViewer().setComparator(new TopicViewerComparator());
        }

        @Override
        protected Object[] getElements(WorkbookMetadata metadata) {
            return metadata.getSet(METADATA_EXTERNAL_FILES).toArray();
        }

        @Override
        protected Image getImage(Object element,
                ResourceManager resourceManager) {
            if (!(element instanceof ITopic))
                return null;

            ITopic topic = (ITopic) element;
            File file = getFile(topic.getHyperlink());
            if (file == null)
                return null;

            ImageDescriptor icon = MindMapUI.getImages()
                    .getFileIcon(file.getAbsolutePath(), true);
            if (icon == null) {
                icon = MindMapUI.getImages().get(IMindMapImages.UNKNOWN_FILE,
                        true);
            }
            return (Image) resourceManager.get(icon);
        }

        @Override
        protected String getText(Object element) {
            if (!(element instanceof ITopic))
                return ""; //$NON-NLS-1$

            ITopic topic = (ITopic) element;
            File file = getFile(topic.getHyperlink());
            if (file == null)
                return ""; //$NON-NLS-1$

            return file.getName();
        }

        private void workbookChanged(IWorkbook workbook) {
            for (ISheet sheet : workbook.getSheets()) {
                topicAdded(sheet.getRootTopic());
            }
        }

        private void topicAdded(ITopic topic) {
            linkChanged(topic, null, topic.getHyperlink());

            Iterator<ITopic> childrenIterator = topic.getAllChildrenIterator();
            while (childrenIterator.hasNext()) {
                topicAdded(childrenIterator.next());
            }
        }

        private void topicRemoved(ITopic topic) {
            Iterator<ITopic> childrenIterator = topic.getAllChildrenIterator();
            while (childrenIterator.hasNext()) {
                topicRemoved(childrenIterator.next());
            }

            linkChanged(topic, topic.getHyperlink(), null);
        }

        private void linkChanged(ITopic topic, String oldLink, String newLink) {
            File oldFile = getFile(oldLink);
            if (oldFile != null) {
                metadata.removeFromSet(METADATA_EXTERNAL_FILES, topic);
            }

            File newFile = getFile(newLink);
            if (newFile != null) {
                metadata.addToSet(METADATA_EXTERNAL_FILES, topic);
            }
        }

        private File getFile(String link) {
            if (!FilePathParser.isFileURI(link))
                return null;
            return new File(FilePathParser.toPath(link));
        }

    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    ////  Hyperlinks Section
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    private static class HyperlinkListPart
            extends AbstractWorkbookMetadataListPart {

        public String getTitle() {
            return NLS.bind(Messages.HyperlinkInspectorSection_title,
                    metadata.sizeOfSet(METADATA_HYPERLINKS));
        }

        public void refresh(IWorkbook workbook, CoreEvent event) {
            WorkbookMetadata oldMetadata = this.metadata.copy();
            Object toUpdate = null;

            if (workbook == null) {
                metadata.deleteAll();
            } else if (event == null) {
                metadata.deleteAll();
                workbookChanged(workbook);
            } else if (Core.SheetAdd.equals(event.getType())) {
                topicAdded(((ISheet) event.getTarget()).getRootTopic());
            } else if (Core.SheetRemove.equals(event.getType())) {
                topicRemoved(((ISheet) event.getTarget()).getRootTopic());
            } else if (Core.TopicAdd.equals(event.getType())) {
                topicAdded((ITopic) event.getTarget());
            } else if (Core.TopicRemove.equals(event.getType())) {
                topicRemoved((ITopic) event.getTarget());
            } else if (Core.TopicHyperlink.equals(event.getType())) {
                ITopic source = (ITopic) event.getSource();
                linkChanged(source, (String) event.getOldValue(),
                        (String) event.getNewValue());
                if (metadata.containsInSet(METADATA_HYPERLINKS, source)) {
                    toUpdate = source;
                }
            } else if (Core.TitleText.equals(event.getType())) {
                Object source = event.getSource();
                if (metadata.containsInSet(METADATA_HYPERLINKS, source)) {
                    toUpdate = source;
                }
            }

            if (!metadata.equals(oldMetadata)) {
                refreshAsynchronously();
            } else if (toUpdate != null) {
                update(toUpdate);
            }

        }

        @Override
        public void createControl(Composite parent) {
            super.createControl(parent);
            getViewer().setComparator(new TopicViewerComparator());
        }

        @Override
        protected Object[] getElements(WorkbookMetadata metadata) {
            return metadata.getSet(METADATA_HYPERLINKS).toArray();
        }

        @Override
        protected Image getImage(Object element,
                ResourceManager resourceManager) {
            return null;
        }

        @Override
        protected String getText(Object element) {
            if (!(element instanceof ITopic))
                return ""; //$NON-NLS-1$

            ITopic topic = (ITopic) element;
            return topic.getHyperlink();
        }

        private void workbookChanged(IWorkbook workbook) {
            for (ISheet sheet : workbook.getSheets()) {
                topicAdded(sheet.getRootTopic());
            }
        }

        private void topicAdded(ITopic topic) {
            linkChanged(topic, null, topic.getHyperlink());

            Iterator<ITopic> childrenIterator = topic.getAllChildrenIterator();
            while (childrenIterator.hasNext()) {
                topicAdded(childrenIterator.next());
            }
        }

        private void topicRemoved(ITopic topic) {
            Iterator<ITopic> childrenIterator = topic.getAllChildrenIterator();
            while (childrenIterator.hasNext()) {
                topicRemoved(childrenIterator.next());
            }

            linkChanged(topic, topic.getHyperlink(), null);
        }

        private void linkChanged(ITopic topic, String oldLink, String newLink) {
            if (isNormalHyperlink(oldLink)) {
                metadata.removeFromSet(METADATA_HYPERLINKS, topic);
            }

            if (isNormalHyperlink(newLink)) {
                metadata.addToSet(METADATA_HYPERLINKS, topic);
            }
        }

        private boolean isNormalHyperlink(String link) {
            return link != null && !HyperlinkUtils.isAttachmentURL(link)
                    && !HyperlinkUtils.isInternalURL(link)
                    && !FilePathParser.isFileURI(link);
        }

    }

    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////
    ////  Images Section
    //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////

    private static class ImageListPart
            extends AbstractWorkbookMetadataListPart {

        public String getTitle() {
            return NLS.bind(Messages.ImageInspectorSection_title,
                    metadata.sizeOfSet(METADATA_IMAGES));
        }

        public void refresh(IWorkbook workbook, CoreEvent event) {
            WorkbookMetadata oldMetadata = this.metadata.copy();
            Object toUpdate = null;

            if (workbook == null) {
                metadata.deleteAll();
            } else if (event == null) {
                metadata.deleteAll();
                workbookChanged(workbook);
            } else if (Core.SheetAdd.equals(event.getType())) {
                topicAdded(((ISheet) event.getTarget()).getRootTopic());
            } else if (Core.SheetRemove.equals(event.getTarget())) {
                topicRemoved(((ISheet) event.getTarget()).getRootTopic());
            } else if (Core.TopicAdd.equals(event.getType())) {
                topicAdded((ITopic) event.getTarget());
            } else if (Core.TopicRemove.equals(event.getType())) {
                topicRemoved((ITopic) event.getTarget());
            } else if (Core.ImageSource.equals(event.getType())) {
                ITopic source = ((IImage) event.getSource()).getParent();
                imageSourceChanged(source, (String) event.getOldValue(),
                        (String) event.getNewValue());
                if (metadata.containsInSet(METADATA_IMAGES, source)) {
                    toUpdate = source;
                }
            } else if (Core.TitleText.equals(event.getType())) {
                Object source = event.getSource();
                if (metadata.containsInSet(METADATA_IMAGES, source)) {
                    toUpdate = source;
                }
            }

            if (!metadata.equals(oldMetadata)) {
                refreshAsynchronously();
            } else if (toUpdate != null) {
                update(toUpdate);
            }
        }

        @Override
        public void createControl(Composite parent) {
            super.createControl(parent);
            getViewer().setComparator(new TopicViewerComparator());
        }

        @Override
        protected Object[] getElements(WorkbookMetadata metadata) {
            return metadata.getSet(METADATA_IMAGES).toArray();
        }

        @Override
        protected String getText(Object element) {
            if (!(element instanceof ITopic))
                return ""; //$NON-NLS-1$

            ITopic topic = (ITopic) element;
            String imageURI = topic.getImage().getSource();
            if (HyperlinkUtils.isAttachmentURL(imageURI)) {
                String path = HyperlinkUtils.toAttachmentPath(imageURI);
                IFileEntry entry = topic.getOwnedWorkbook().getManifest()
                        .getFileEntry(path);
                if (entry != null) {
                    return String.format("%s (%s)", topic.getTitleText(), //$NON-NLS-1$
                            FileUtils.fileLengthToString(entry.getSize()));
                }
            }
            return topic.getTitleText();
        }

        @Override
        protected Image getImage(final Object element,
                final ResourceManager resourceManager) {
            if (!(element instanceof ITopic))
                return null;

            ITopic topic = (ITopic) element;
            String imageURI = topic.getImage().getSource();
            if (HyperlinkUtils.isAttachmentURL(imageURI)) {
                String path = HyperlinkUtils.toAttachmentPath(imageURI);
                IWorkbook workbook = topic.getOwnedWorkbook();
                IFileEntry entry = workbook.getManifest().getFileEntry(path);
                ImageDescriptor descriptor = AttachmentImageDescriptor
                        .createFromEntry(workbook, entry);
                return (Image) resourceManager.get(descriptor);
            } else if (FilePathParser.isFileURI(imageURI)) {
                String path = FilePathParser.toPath(imageURI);
                ImageDescriptor descriptor = ImageDescriptor
                        .createFromFile(null, path);
                return (Image) resourceManager.get(descriptor);
            } else {
                ImageDescriptor descriptor = ImageDownloader.getInstance()
                        .getImage(imageURI);
                if (descriptor != null)
                    return (Image) resourceManager.get(descriptor);
                ImageDownloader.getInstance().register(imageURI,
                        new Runnable() {
                            public void run() {
                                if (getViewer() == null
                                        || getViewer().getControl() == null
                                        || getViewer().getControl()
                                                .isDisposed())
                                    return;

                                Display display = getViewer().getControl()
                                        .getDisplay();
                                if (display == null || display.isDisposed())
                                    return;

                                display.asyncExec(new Runnable() {
                                    public void run() {
                                        update(element);
                                    }
                                });
                            }
                        });
            }
            return null;
        }

        @Override
        protected Point getImageSizeHint() {
            return new Point(32, 32);
        }

        private void workbookChanged(IWorkbook workbook) {
            for (ISheet sheet : workbook.getSheets()) {
                topicAdded(sheet.getRootTopic());
            }
        }

        private void topicAdded(ITopic topic) {
            imageSourceChanged(topic, null, topic.getImage().getSource());

            Iterator<ITopic> childrenIterator = topic.getAllChildrenIterator();
            while (childrenIterator.hasNext()) {
                topicAdded(childrenIterator.next());
            }
        }

        private void topicRemoved(ITopic topic) {
            Iterator<ITopic> childrenIterator = topic.getAllChildrenIterator();
            while (childrenIterator.hasNext()) {
                topicRemoved(childrenIterator.next());
            }

            imageSourceChanged(topic, topic.getImage().getSource(), null);
        }

        private void imageSourceChanged(ITopic topic, String oldSrc,
                String newSrc) {
            if (oldSrc != null) {
                metadata.removeFromSet(METADATA_IMAGES, topic);
            }
            if (newSrc != null) {
                metadata.addToSet(METADATA_IMAGES, topic);
            }
        }

    }

    private ICoreEventListener coreEventHandler = new ICoreEventListener() {
        public void handleCoreEvent(final CoreEvent event) {
            Display display = getDisplay();
            if (display == null)
                return;

            display.asyncExec(new Runnable() {
                public void run() {
                    refreshParts(event);
                }
            });
        }

        private Display getDisplay() {
            Display display = Display.getCurrent();
            if (display != null)
                return display;

            Control control = getControl();
            if (control == null || control.isDisposed())
                return null;

            return control.getDisplay();
        }
    };

    private IPropertyListener propertyChangeHandler = new IPropertyListener() {
        public void propertyChanged(Object source, int propId) {
            if (source instanceof IWorkbookMetadataPart) {
                if (propId == PROP_CONTROL) {
                    updateSection((IWorkbookMetadataPart) source);
                    scheduleReflow();
                } else if (propId == PROP_CONTENT) {
                    if (source instanceof IModifiableWorkbookMetadataPart) {
                        executeCommand(
                                ((IModifiableWorkbookMetadataPart) source)
                                        .createModificationCommand(workbook));
                    }
                } else if (propId == PROP_OUTGOING_SELECTION) {
                    revealSelectionInEditor((IWorkbookMetadataPart) source);
                }
            }
        }
    };

    private IPartListener partListenerHandler = new IPartListener() {

        public void partOpened(IWorkbenchPart part) {
        }

        public void partDeactivated(IWorkbenchPart part) {
            if (part == sourceEditor) {

            }
        }

        public void partClosed(IWorkbenchPart part) {
            if (part == sourceEditor) {
                setSourceEditor(null);
            }
        }

        public void partBroughtToTop(IWorkbenchPart part) {
        }

        public void partActivated(IWorkbenchPart part) {

            if (part == sourceEditor || !(part instanceof IEditorPart))
                return;

            if (part instanceof IEditorPart) {
                setSourceEditor((IEditorPart) part);
            }
        }
    };

    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(DialogMessages.WorkbookMetaInspectorDialog_title);
        newShell.setSize(500, 445);
        newShell.setLocation(
                Display.getCurrent().getClientArea().width / 2
                        - newShell.getShell().getSize().x / 2,
                Display.getCurrent().getClientArea().height / 2
                        - newShell.getSize().y / 2);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 14;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 0;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        createDescriptionArea(composite);

        Composite content = new Composite(composite, SWT.BORDER);
        this.composite = content;
        content.setLayoutData(new GridData(GridData.FILL_BOTH));
        StackLayout stack = new StackLayout();
        content.setLayout(stack);

        factory = new WidgetFactory(parent.getDisplay());
        factory.getHyperlinkGroup()
                .setHyperlinkUnderlineMode(HyperlinkGroup.UNDERLINE_HOVER);
        form = factory.createScrolledForm(content);
        stack.topControl = form;

        final Composite body = form.getBody();

        TableWrapLayout layout = new TableWrapLayout();
        layout.topMargin = 0;
        layout.leftMargin = 0;
        layout.rightMargin = 0;
        layout.bottomMargin = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 3;
        body.setLayout(layout);
        createSections(body);

        form.getBody().addListener(SWT.MouseDown, new Listener() {
            public void handleEvent(Event event) {
                form.getBody().setFocus();
            }
        });

        form.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });

        setWorkbook(sourceEditor == null ? null
                : MindMapUIPlugin.getAdapter(sourceEditor, IWorkbook.class));
        return composite;
    }

    @Override
    protected Control createButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        // create a layout with spacing and margins appropriate for the font
        // size.
        GridLayout layout = new GridLayout();
        layout.numColumns = 0; // this is incremented by createButton
        layout.makeColumnsEqualWidth = true;
        layout.marginWidth = 13;
        layout.marginHeight = 23;
        layout.horizontalSpacing = 18;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        GridData data = new GridData(
                GridData.HORIZONTAL_ALIGN_END | GridData.VERTICAL_ALIGN_CENTER);
        composite.setLayoutData(data);
        composite.setFont(parent.getFont());

        // Add the buttons to the button bar.
        createButtonsForButtonBar(composite);
        return composite;
    }

    private void createDescriptionArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 21;
        gridLayout.verticalSpacing = 0;
        composite.setLayout(gridLayout);

        Label discriptionLabel = new Label(composite, SWT.WRAP);
        GridData discriptionLabelData = new GridData(SWT.FILL, SWT.CENTER, true,
                true);
        discriptionLabel.setLayoutData(discriptionLabelData);
        discriptionLabel.setAlignment(SWT.LEFT);
        discriptionLabel
                .setText(MindMapMessages.WorkbookMetaInspectorDialog_message);
    }

    private void handleDispose() {
        setWorkbook(null);

        for (IWorkbookMetadataPart part : parts) {
            part.removePropertyListener(propertyChangeHandler);
        }
        parts.clear();
        sections.clear();

        if (factory != null) {
            factory.dispose();
            factory = null;
        }

        form = null;
        composite = null;
    }

    private void setWorkbook(IWorkbook workbook) {
        IWorkbook oldWorkbook = this.workbook;
        if (workbook == oldWorkbook)
            return;

        this.workbook = workbook;
        workbookChanged(workbook, oldWorkbook);
    }

    private void workbookChanged(IWorkbook workbook2, IWorkbook oldWorkbook) {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
        }

        if (getControl() != null && !getControl().isDisposed()) {
            for (IWorkbookMetadataPart part : parts) {
                part.refresh(workbook, null);
            }
            scheduleReflow();
        }

        if (workbook != null) {
            ICoreEventSupport support = (ICoreEventSupport) workbook
                    .getAdapter(ICoreEventSupport.class);
            if (support != null) {
                if (eventRegister == null) {
                    eventRegister = new CoreEventRegister(coreEventHandler);
                }
                eventRegister.setNextSupport(support);
                registerCoreEventTypes(eventRegister);
            }
        }
    }

    private void registerCoreEventTypes(CoreEventRegister register) {
        register.register(Core.Metadata);
        register.register(Core.SheetAdd);
        register.register(Core.SheetRemove);
        register.register(Core.TopicAdd);
        register.register(Core.TopicRemove);
        register.register(Core.FileEntryAdd);
        register.register(Core.FileEntryRemove);
        register.register(Core.TopicHyperlink);
        register.register(Core.TitleText);
        register.register(Core.ImageSource);
        register.register(Core.RevisionAdd);
        register.register(Core.RevisionRemove);
        register.register(Core.ModifyTime);
        register.register(Core.WorkbookSave);
        register.register(Core.RelationshipAdd);
        register.register(Core.RelationshipRemove);
        register.register(Core.BoundaryAdd);
        register.register(Core.BoundaryRemove);
        register.register(Core.TopicNotes);
    }

    private void createSections(Composite parent) {
        addSection(parent, new AuthorInfoPart());
        addSection(parent, new WorkbookSummaryPart());
        addSection(parent, new AttachmentListPart());
        addSection(parent, new ExternalFileListPart());
        addSection(parent, new HyperlinkListPart());
        addSection(parent, new ImageListPart());
    }

    private void addSection(Composite parent, IWorkbookMetadataPart part) {
        Section section = factory.createSection(parent,
                Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED
                        | Section.NO_TITLE_FOCUS_BOX);
        section.setText(part.getTitle());
        section.setLayoutData(
                new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL));
        section.setTitleBarBackground(
                section.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        section.setTitleBarBorderColor(
                section.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        Composite client = factory.createComposite(section, SWT.WRAP);
        client.setData(KEY_WIDGET_FACTORY, factory);
        section.setClient(client);
        part.createControl(client);
        part.refresh(workbook, null);
        part.addPropertyListener(propertyChangeHandler);
        parts.add(part);
        sections.put(part, section);

        updateSectionVisibility(part, section, part.isVisible());
    }

    private void refreshParts(final CoreEvent event) {
        for (IWorkbookMetadataPart part : parts) {
            part.refresh(workbook, event);
        }
    }

    private void updateSectionVisibility(IWorkbookMetadataPart part,
            Section section, boolean visible) {
        if (visible) {
            section.setParent(form.getBody());

            Section lastVisibleSection = null;
            for (IWorkbookMetadataPart p : parts) {
                if (p == part)
                    break;
                Section s = sections.get(p);
                if (s.getVisible()) {
                    lastVisibleSection = s;
                } else {
                    section.moveBelow(lastVisibleSection);
                }
            }
        } else {
            section.setParent(composite);
        }
        section.setVisible(visible);
    }

    private void revealSelectionInEditor(IWorkbookMetadataPart part) {
        ISelectionProvider partSelectionProvider = MindMapUIPlugin
                .getAdapter(part, ISelectionProvider.class);
        if (partSelectionProvider == null)
            return;

        ISelectionProvider editorSelectionProvider = sourceEditor.getSite()
                .getSelectionProvider();
        if (editorSelectionProvider == null)
            return;

        editorSelectionProvider
                .setSelection(partSelectionProvider.getSelection());
        sourceEditor.getSite().getPage().activate(sourceEditor);
    }

    private void executeCommand(Command command) {
        ICommandStack commandStack = MindMapUIPlugin.getAdapter(sourceEditor,
                ICommandStack.class);
        if (commandStack != null) {
            commandStack.execute(command);
        } else {
            command.execute();
        }
    }

    private synchronized void scheduleReflow() {
        if (form == null || form.isDisposed())
            return;

        if (reflowScheduled)
            return;

        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                reflowScheduled = false;

                if (form == null || form.isDisposed())
                    return;
                form.reflow(true);
            }
        });
        reflowScheduled = true;
    }

    private void updateSection(IWorkbookMetadataPart part) {
        Section section = sections.get(part);
        if (section != null && !section.isDisposed()) {
            String title = part.getTitle();
            String oldTitle = section.getText();
            if (!title.equals(oldTitle)) {
                section.setText(title);
            }

            boolean visible = part.isVisible();
            boolean oldVisible = section.getVisible();
            if (visible != oldVisible) {
                updateSectionVisibility(part, section, visible);
            }

            scheduleReflow();
        }
    }

    private Composite getControl() {
        return composite;
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.CLOSE_ID,
                IDialogConstants.CLOSE_LABEL, true);
    }

    @Override
    protected void buttonPressed(int buttonId) {
        super.buttonPressed(buttonId);
        if (IDialogConstants.CLOSE_ID == buttonId)
            close();
    }

    public void setSourceEditor(IEditorPart sourceEditor) {
        Assert.isNotNull(instance);
        this.sourceEditor = sourceEditor;
        setWorkbook(sourceEditor == null ? null
                : MindMapUIPlugin.getAdapter(sourceEditor, IWorkbook.class));
        PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
                .addPartListener(partListenerHandler);
    }

    public static WorkbookMetaInspectorDialog getInstance(Shell parentShell) {
        if (instance == null) {
            instance = new WorkbookMetaInspectorDialog(parentShell);
        }
        return instance;
    }

}
