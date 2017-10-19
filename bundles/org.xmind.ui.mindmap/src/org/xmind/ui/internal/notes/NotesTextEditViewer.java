package org.xmind.ui.internal.notes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.hyperlink.DefaultHyperlinkPresenter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.UIPlugin;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.IRichTextActionBarContributor;
import org.xmind.ui.richtext.IRichTextEditViewer;
import org.xmind.ui.richtext.IRichTextRenderer;
import org.xmind.ui.richtext.RichTextActionBarContributor;
import org.xmind.ui.richtext.RichTextDamagerRepairer;
import org.xmind.ui.richtext.RichTextRenderer;
import org.xmind.ui.richtext.RichTextScanner;
import org.xmind.ui.richtext.RichTextViewerUndoManager;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.util.TextFormatter;

public class NotesTextEditViewer implements IRichTextEditViewer, KeyListener {

    public static final int WIDTH = 4;

    private class SelectionProvider implements ISelectionProvider {

        private List<ISelectionChangedListener> selectionChangedListeners = null;

        private ISelection selection;

        public void addSelectionChangedListener(
                ISelectionChangedListener listener) {
            if (selectionChangedListeners == null)
                selectionChangedListeners = new ArrayList<ISelectionChangedListener>();
            selectionChangedListeners.add(listener);
        }

        public ISelection getSelection() {
            return selection == null ? StructuredSelection.EMPTY : selection;
        }

        public void removeSelectionChangedListener(
                ISelectionChangedListener listener) {
            if (selectionChangedListeners == null)
                return;
            selectionChangedListeners.remove(listener);
        }

        public void setSelection(ISelection selection) {
            if (this.selection == selection || (this.selection != null
                    && this.selection.equals(selection))) {
                return;
            }
            this.selection = selection;

            fireSelectionChanged(
                    new SelectionChangedEvent(this, getSelection()));
        }

        private void fireSelectionChanged(SelectionChangedEvent event) {
            if (selectionChangedListeners == null)
                return;
            for (Object o : selectionChangedListeners.toArray()) {
                ((ISelectionChangedListener) o).selectionChanged(event);
            }
        }

    }

    private IRichDocument document;

    private Composite control;

    private NotesTextViewer textViewer;

    private NotesTextViewer oldTextViewer;

    private boolean editable;

    private IRichTextRenderer renderer;

    private IRichTextActionBarContributor contributor;

    private ToolBarManager toolBarManager = null;

    private MenuManager contextMenu = null;

    private ScrolledComposite sc;

    private Composite contentComposite;

    private Composite composite;

    private Object input;

    private List<Composite> textComposites = new ArrayList<Composite>();

    private List<Control> textControls = new ArrayList<Control>();

    private Map<Composite, ITopic> maps = new HashMap<Composite, ITopic>();

    private Composite currentTextComposite;

    private ISelectionProvider selectionProvider;

    private IGraphicalEditor editor;

    private int width = 0;

    private ResourceManager resources;

    public NotesTextEditViewer(Composite parent,
            IRichTextActionBarContributor contributor) {
        this.contributor = contributor;
        this.control = createControl(parent);
    }

    protected Composite createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        resources = new LocalResourceManager(JFaceResources.getResources(),
                composite);

        composite.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        createContentArea(composite);

        composite.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleControlDispose(e);
            }
        });
        return composite;
    }

    protected void createContentArea(Composite parent) {
        Control toolBar = createToolBar(parent);
        if (toolBar != null) {
            createSeparator(parent);
        }
        composite = createContentComposite(parent);
    }

    protected Control createToolBar(Composite parent) {
        if (contributor == null)
            return null;

        contributor.init(this);
        toolBarManager = new ToolBarManager(SWT.FLAT);
        contributor.fillToolBar(toolBarManager);
        parent.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event event) {
                toolBarManager.update(true);
            }
        });
        ToolBar toolBar = toolBarManager.createControl(parent);
        toolBar.setBackground(parent.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        return toolBar;
    }

    protected void createSeparator(Composite parent) {
        Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sep.setBackground(parent.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
    }

    private Composite createContentComposite(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setBackground(parent.getBackground());

        return composite;
    }

    protected NotesTextViewer createTextViewer(Composite parent, int style) {
        return new NotesTextViewer(parent, style);
    }

    protected RichTextRenderer createRenderer(TextViewer textViewer) {
        return new RichTextRenderer(textViewer);
    }

    private void initTextViewer(final TextViewer textViewer) {
        Control control = textViewer.getTextWidget();
        createContentPopupMenu(control);

        textViewer.setTextDoubleClickStrategy(
                new DefaultTextDoubleClickStrategy(),
                IDocument.DEFAULT_CONTENT_TYPE);

        textViewer.setUndoManager(new RichTextViewerUndoManager(25));
        textViewer.activatePlugins();

        addHyperlinkListener(textViewer);
    }

    private void createContentPopupMenu(Control control) {
        contextMenu = new MenuManager();
        contextMenu.setRemoveAllWhenShown(true);
        contextMenu.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        control.setMenu(contextMenu.createContextMenu(control));
    }

    private void fillContextMenu(IMenuManager menu) {
        if (contributor != null)
            contributor.fillContextMenu(menu);
    }

    private void addHyperlinkListener(TextViewer viewer) {
        PresentationReconciler reconciler = new PresentationReconciler();
        RichTextDamagerRepairer dr = new RichTextDamagerRepairer(
                new RichTextScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setDocumentPartitioning(
                IDocumentExtension3.DEFAULT_PARTITIONING);
        reconciler.install(viewer);
    }

    protected void handleControlDispose(DisposeEvent e) {
        if (contributor != null) {
            contributor.dispose();
        }
        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
        }
        if (toolBarManager != null) {
            toolBarManager.dispose();
            toolBarManager = null;
        }
        if (document != null) {
            unhookDocument(document);
            document = null;
        }
    }

    public Control getControl() {
        return control;
    }

    public Control getFocusControl() {
        if (textViewer == null) {
            return null;
        }
        return textViewer.getControl();
    }

    public IRichDocument getDocument() {
        return document;
    }

    public TextViewer getTextViewer() {
        return textViewer;
    }

    protected void hookDocument(IRichDocument document) {
    }

    protected void unhookDocument(IRichDocument document) {
    }

    public Object getInput() {
        return input;
    }

    public void refresh() {
    }

    public IRichTextActionBarContributor getContributor() {
        return contributor;
    }

    public void update() {
        Point origin = null;
        if (sc != null && !(sc.isDisposed())) {
            origin = sc.getOrigin();
        }

        updateNotes();
        updateDeleteAction();

        if (sc != null && !(sc.isDisposed()) && origin != null) {
            sc.setOrigin(origin);
        }
    }

    public void setInput(Object input) {
        updateView(input);
    }

    private void updateView(Object input) {
        if (!(this.input instanceof IRichDocument
                && input instanceof IRichDocument)) {
            this.input = input;
            if (input == null) {
                this.input = getCurrentSheet();
            }
            updateNotes();
            updateToolBar();
        }
    }

    private void updateNotes() {
        if (composite == null || composite.isDisposed()) {
            return;
        }
        composite.setRedraw(false);
        resetContent();

        if (input instanceof IRichDocument) {
            showSingleNotes(composite, null);
        } else if (input instanceof ISheet) {
            showAllNotes(composite);
        } else {
            toolBarManager.getControl().setEnabled(false);
        }
//        if (contentComposite != null && !contentComposite.isDisposed()) {
//            contentComposite.pack();
//        }
        composite.layout();
        composite.setRedraw(true);
    }

    private void resetContent() {
        Control[] controls = composite.getChildren();
        if (controls != null) {
            for (Control control : controls) {
                if (control != null && !control.isDisposed()) {
                    control.dispose();
                    control = null;
                }
            }
        }

        textComposites.clear();
        textControls.clear();
        maps.clear();

        toolBarManager.getControl().setEnabled(true);

        width = 0;

        setTextViewer(null);
        setSelection(null);
    }

    protected void showSingleNotes(Composite parent, ITopic topic) {

        textViewer = createTextViewer(parent,
                SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
        initTextViewer(textViewer);
        renderer = createRenderer(textViewer);

        final Control text = textViewer.getControl();
        text.setBackground(parent.getBackground());
        text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));

        GridData gridData = new GridData(GridData.FILL_BOTH);
        gridData.heightHint = 160;
        gridData.horizontalIndent = 2;
        gridData.verticalIndent = 2;
        text.setLayoutData(gridData);

        RGB red = new RGB(183, 0, 91);
        textViewer.setHyperlinkPresenter(new DefaultHyperlinkPresenter(red));
        textViewer.setHyperlinkDetectors(
                new IHyperlinkDetector[] { new NotesHyperlinkDetector() },
                SWT.MOD1);

        setTextViewer(textViewer);
        setSelection(textViewer);

        composite.addListener(SWT.Resize, new Listener() {

            public void handleEvent(Event event) {
                if (text != null && !(text.isDisposed())) {
                    ((GridData) text.getLayoutData()).widthHint = composite
                            .getSize().x - 22;
                }
            }
        });

    }

    private void showAllNotes(Composite parent) {
        ISheet sheet = (ISheet) input;
        List<ITopic> topics = NotesUtils.getAllTopicsWithNotes(sheet);
        if (topics == null || topics.size() == 0) {
            return;
        }

        contentComposite = createTextControl(parent);
        for (ITopic topic : topics) {
            showLabelAndNote(contentComposite, topic);
        }

        if (sc.getClientArea().width > 0) {
            for (Control textControl : textControls) {
                ((GridData) textControl.getLayoutData()).widthHint = sc
                        .getClientArea().width - WIDTH;
            }
        }

        sc.addListener(SWT.Resize, new Listener() {

            public void handleEvent(Event event) {
                if (sc.getClientArea().width <= 0) {
                    return;
                }

                if (width != sc.getClientArea().width) {
                    width = sc.getClientArea().width;
                    for (Control textControl : textControls) {
                        if (textControl != null && !textControl.isDisposed()) {
                            ((GridData) textControl
                                    .getLayoutData()).widthHint = sc
                                            .getClientArea().width - WIDTH;
                        }
                    }
                    contentComposite.pack();
                }
            }
        });
    }

    protected Composite createTextControl(Composite parent) {
        sc = new ScrolledComposite(parent, SWT.V_SCROLL);
        sc.setAlwaysShowScrollBars(true);
        sc.setLayoutData(new GridData(GridData.FILL_BOTH));
        sc.setBackground(parent.getBackground());

        sc.setExpandHorizontal(true);
        sc.setMinSize(SWT.DEFAULT, SWT.DEFAULT);

        Composite composite = new Composite(sc, SWT.NONE);
        composite.setBackground(parent.getBackground());

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.verticalSpacing = 5;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        sc.setContent(composite);
        sc.getVerticalBar().setIncrement(17);

        return composite;
    }

    private void showLabelAndNote(Composite parent, ITopic topic) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        createTopicLabel(composite, topic);
        createSingleNotes(composite, topic);

        createSeparatorLine(parent);
    }

    private void createTopicLabel(Composite parent, ITopic topic) {
        Composite c = new Composite(parent, SWT.NONE);
        c.setBackground(parent.getBackground());
        c.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        c.setLayout(layout);

        Label imageLabel = new Label(c, SWT.LEFT);
        GridData data1 = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        imageLabel.setLayoutData(data1);
        imageLabel.setBackground(c.getBackground());
        Image image = (Image) resources
                .get(MindMapUI.getImages().getTopicIcon(topic, true));
        imageLabel.setImage(image);

        Label label = new Label(c, SWT.LEFT | SWT.HORIZONTAL);
        label.setBackground(parent.getBackground());
        label.setForeground(ColorConstants.black);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalIndent = 2;
        label.setLayoutData(data);

        FontData[] fontData = label.getFont().getFontData();
        for (int i = 0; i < fontData.length; i++) {
            fontData[i].setHeight(fontData[i].getHeight() + 2);
        }

        final Font newFont = new Font(label.getDisplay(), fontData);
        label.setFont(newFont);

        label.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                newFont.dispose();
            }
        });

        label.setText(TextFormatter.removeNewLineCharacter(topic.getTitleText())
                + ":"); //$NON-NLS-1$s
    }

    private void createSingleNotes(Composite parent, ITopic topic) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());

        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 1;
        layout.marginHeight = 1;
        composite.setLayout(layout);

        textComposites.add(composite);
        maps.put(composite, topic);

        createContentText(composite, topic);

        final MouseListener ml = new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (composite == null || composite.isDisposed()) {
                    return;
                }
                if (!composite.isFocusControl()) {
                    composite.forceFocus();
                }
            }

            public void mouseDoubleClick(MouseEvent e) {
                reveal(composite);
            }
        };

        addMosuseListener(composite, ml);

        composite.addFocusListener(new FocusListener() {

            private Color originalBgColor = null;

            public void focusLost(FocusEvent e) {
                composite.setBackground(originalBgColor);

                composite.removeKeyListener(NotesTextEditViewer.this);
                currentTextComposite = null;
                updateDeleteAction();
            }

            public void focusGained(FocusEvent e) {
                originalBgColor = composite.getBackground();
                composite.setBackground(ColorConstants.lightBlue);

                currentTextComposite = composite;
                composite.addKeyListener(NotesTextEditViewer.this);

                sc.showControl(composite.getParent());

                updateDeleteAction();
            }
        });
    }

    protected void updateDeleteAction() {
        if (contributor == null) {
            return;
        }
        IAction deleteNotesAction = ((RichTextActionBarContributor) contributor)
                .getRichTextAction("org.xmind.ui.action.deleteNotes"); //$NON-NLS-1$
        deleteNotesAction.setEnabled(currentTextComposite != null);
    }

    private Control createContentText(Composite parent, ITopic topic) {
        Text text = new Text(parent, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        text.setBackground(parent.getBackground());
        text.setForeground(ColorConstants.black);
        text.setLayoutData(new GridData(GridData.FILL_BOTH));

        RichDocumentNotesAdapter adapter = new RichDocumentNotesAdapter(topic);
        text.setText(adapter.getDocument().get());

        textControls.add(text);

        return text;
    }

    private void createSeparatorLine(Composite parent) {
        Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
        sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sep.setBackground(ColorConstants.black);
    }

    private void reveal(Composite composite) {
        MindMapUtils.reveal(editor, getCurrentTopic(composite));
        E4Utils.showPart(IModelConstants.COMMAND_SHOW_MODEL_PART,
                PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                IModelConstants.PART_ID_NOTES, null,
                IModelConstants.PART_STACK_ID_RIGHT);
    }

    private void addMosuseListener(Control c, MouseListener ml) {
        c.addMouseListener(ml);
        if (c instanceof Composite) {
            for (final Control cc : ((Composite) c).getChildren()) {
                addMosuseListener(cc, ml);
            }
        }
    }

    public void updateToolBar() {
        if (toolBarManager == null) {
            return;
        }

        if (contributor != null) {
            contributor.selectionChanged(getSelection(), true);
        }

        toolBarManager.update(false);
    }

    public void setDocument(IRichDocument document) {

        IRichDocument oldDocument = this.document;
        this.document = document;
        documentChanged(document, oldDocument);

        textViewer.setDocument(document == null ? new Document() : document);
        // move the caret to the end of document
        if (document != null) {
            textViewer.setSelectedRange(document.getLength(), 0);
        }

    }

    protected void documentChanged(IRichDocument newDocument,
            IRichDocument oldDocument) {
        if (newDocument != oldDocument) {
            if (oldDocument != null) {
                unhookDocument(oldDocument);
            }
            if (newDocument != null) {
                hookDocument(newDocument);
            }
        }
    }

    public IRichTextRenderer getRenderer() {
        return renderer;
    }

    public Point getSelectedRange() {
        return textViewer.getSelectedRange();
    }

    public void setSelection(ISelection selection, boolean reveal) {
        textViewer.setSelection(selection, reveal);
    }

    public void setSelectedRange(int selectionOffset, int selectionLength) {
        textViewer.setSelectedRange(selectionOffset, selectionLength);
    }

    public void addPostSelectionChangedListener(
            ISelectionChangedListener listener) {
        if (textViewer != null) {
            textViewer.addPostSelectionChangedListener(listener);
        }
    }

    public void removePostSelectionChangedListener(
            ISelectionChangedListener listener) {
        textViewer.removePostSelectionChangedListener(listener);
    }

    public void addTextInputListener(ITextInputListener listener) {
        textViewer.addTextInputListener(listener);
    }

    public void addTextListener(ITextListener listener) {
        textViewer.addTextListener(listener);
    }

    public void removeTextInputListener(ITextInputListener listener) {
        textViewer.removeTextInputListener(listener);
    }

    public void removeTextListener(ITextListener listener) {
        textViewer.removeTextListener(listener);
    }

    public boolean isSelectedRangeEmpty() {
        Point p = getSelectedRange();
        return p.y <= 0;
    }

    public StyledText getTextWidget() {
        return textViewer.getTextWidget();
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        if (editable == this.editable)
            return;
        this.editable = editable;
    }

    public void keyPressed(KeyEvent e) {
        if (e.keyCode == SWT.ARROW_DOWN) {
            moveDown();
        } else if (e.keyCode == SWT.ARROW_UP) {
            moveUp();
        } else if (e.keyCode == SWT.CR) {
            reveal(currentTextComposite);
        } else if (e.keyCode == SWT.DEL) {
            IAction action = ((RichTextActionBarContributor) contributor)
                    .getRichTextAction("org.xmind.ui.action.deleteNotes"); //$NON-NLS-1$
            if (action != null && action.isEnabled()) {
                action.run();
            }
        }

    }

    public void keyReleased(KeyEvent e) {

    }

    private void moveDown() {
        if (currentTextComposite == null) {
            return;
        }

        int index = textComposites.indexOf(currentTextComposite);
        if (index < 0 || index >= textComposites.size() - 1) {
            return;
        }

        textComposites.get(index + 1).forceFocus();
    }

    private void moveUp() {
        if (currentTextComposite == null) {
            return;
        }

        int index = textComposites.indexOf(currentTextComposite);
        if (index <= 0 || index > textComposites.size() - 1) {
            return;
        }

        textComposites.get(index - 1).forceFocus();
    }

    public Composite getCurrentTextComposite() {
        return currentTextComposite;
    }

    public void setCurrentTextComposite(Composite currentTextComposite) {
        this.currentTextComposite = currentTextComposite;
    }

    public ITopic getCurrentTopic(Composite currentComposite) {
        if (maps == null || maps.size() == 0) {
            return null;
        }

        return maps.get(currentComposite);
    }

    public ISelectionProvider getSelectionProvider() {
        if (selectionProvider == null) {
            selectionProvider = new SelectionProvider();
        }

        return selectionProvider;
    }

    public void setSelection(ISelection selection) {
        getSelectionProvider().setSelection(selection);
    }

    public ISelection getSelection() {
        return getSelectionProvider().getSelection();
    }

    public void addSelectionChangedListener(
            ISelectionChangedListener listener) {
        getSelectionProvider().addSelectionChangedListener(listener);
    }

    public void removeSelectionChangedListener(
            ISelectionChangedListener listener) {
        getSelectionProvider().removeSelectionChangedListener(listener);
    }

    private void setTextViewer(NotesTextViewer textViewer) {
        oldTextViewer = this.textViewer;
        this.textViewer = textViewer;
    }

    public NotesTextViewer getOldTextViewer() {
        return oldTextViewer;
    }

    public List<Control> getTextControls() {
        return textControls;
    }

    public IGraphicalEditor getEditor() {
        return editor;
    }

    public void setEditor(IGraphicalEditor editor) {
        this.editor = editor;
    }

    private ISheet getCurrentSheet() {
        IEditorPart activeEditor = UIPlugin.getDefault().getWorkbench()
                .getActiveWorkbenchWindow().getActivePage().getActiveEditor();

        if (activeEditor instanceof IGraphicalEditor) {
            IGraphicalEditor editor = (IGraphicalEditor) activeEditor;
            if (editor.getActivePageInstance() != null) {
                ISheet sheet = (ISheet) editor.getActivePageInstance()
                        .getAdapter(ISheet.class);
                return sheet;
            }
        }
        return null;
    }

}
