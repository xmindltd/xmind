package org.xmind.ui.internal.comments;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.ITextListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.TextViewerUndoManager;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Caret;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.core.Core;
import org.xmind.core.IComment;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegistration;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.ICommandStack;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.commands.AddCommentCommand;
import org.xmind.ui.commands.DeleteCommentCommand;
import org.xmind.ui.commands.ModifyCommentCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.e4models.CommentsPart;
import org.xmind.ui.internal.spelling.SpellingPlugin;
import org.xmind.ui.internal.spellsupport.SpellingSupport;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.richtext.RichTextDamagerRepairer;
import org.xmind.ui.richtext.RichTextScanner;
import org.xmind.ui.texteditor.ISpellingActivation;
import org.xmind.ui.util.TextFormatter;

public class CommentTextViewer {

    private static class LessLatencyTextViewer extends TextViewer {

        private LessLatencyTextViewer(Composite parent, int styles) {
            super(parent, styles);
        }

        @Override
        protected int getEmptySelectionChangedEventDelay() {
            return 100;
        }
    }

    private static final int EXTRA_WIDTH = 22;

    private static int DEFAULT_STYLE = SWT.MULTI | SWT.WRAP | SWT.NO_SCROLL;

    private Color originalColor;

    private Color hoverColor;

    private Color selectColor;

    private IComment comment;

    private String objectId;

    private IWorkbook workbook;

    private ICommentsActionBarContributor contributor;

    private ISelectionProvider selectionProvider;

    private ICommentTextViewerContainer container;

    private ISpellingActivation spellingActivation;

    private IPreferenceStore spellingPreferences;

    private boolean editable = (DEFAULT_STYLE & SWT.READ_ONLY) == 0;

    private Composite control;

    private TextViewer textViewer;

    private MenuManager textContextMenuManager;

    private Menu textContextMenu;

    private IDocument document;

    private int textHeight = 17;

    private ICoreEventRegistration saveCommentReg;

    private MouseListener mouseListener;

    private ISelectionChangedListener selectionChangedListener;

    private FocusListener textFocusListener;

    private CaretListener caretListener;

    private ISelectionChangedListener postSelectionChangedListener;

    private IPropertyChangeListener propertyChangeListener;

    private ControlListener controlListener;

    private ITextListener textListener;

    private ModifyListener modifyListener;

    private IGraphicalEditor targetEditor;

    private StyleRange styleRange;

    private Label timeLabel;

    private Control modifyCommentActionBar;

    private boolean isLinkHovering;

    private Listener listener;

    private Menu contextMenu;

    private ResourceManager resources;

    public CommentTextViewer(IComment comment, String objectId,
            IWorkbook workbook, ICommentsActionBarContributor contributor,
            ISelectionProvider selectionProvider,
            ICommentTextViewerContainer container,
            IGraphicalEditor targetEditor) {
        this.comment = comment;
        this.objectId = objectId;
        this.workbook = workbook;
        this.contributor = contributor;
        this.selectionProvider = selectionProvider;
        this.container = container;
        this.targetEditor = targetEditor;

        resources = new LocalResourceManager(JFaceResources.getResources(),
                container.getContentComposite());

        initColors();
    }

    private void initColors() {
        if (container instanceof CommentsPopup) {
            originalColor = CommentsPopup.BG_COLOR;
            hoverColor = (Color) resources
                    .get(ColorUtils.toDescriptor("#f0f0f0")); //$NON-NLS-1$
            selectColor = (Color) resources
                    .get(ColorUtils.toDescriptor("#eaeaea")); //$NON-NLS-1$
        } else if (container instanceof CommentsPart) {
            originalColor = (Color) resources
                    .get(ColorUtils.toDescriptor(CommentsPart.BG_COLOR));
            hoverColor = (Color) resources
                    .get(ColorUtils.toDescriptor("#f9f9f9")); //$NON-NLS-1$
            selectColor = (Color) resources
                    .get(ColorUtils.toDescriptor("#f5f5f5")); //$NON-NLS-1$
        }
    }

    public Control createControl(Composite parent) {
        control = new Composite(parent, SWT.NONE);
        control.setBackground(parent.getBackground());
        control.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 7;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        control.setLayout(layout);

        createContentArea(control);

        control.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleControlDispose(e);
            }
        });

        //restore last editing.
        if (comment != null && comment == container.getEditingComment()) {
            startEditing();
            container.setModified(false);
            container.setEditingComment(null);
        }

        return control;
    }

    private void createContentArea(Composite parent) {
        if (comment == null) {
            createNullContentArea(parent);
        } else {
            createNotNullContentArea(parent);
        }
    }

    private void createNullContentArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 10;
        layout.marginHeight = 0;
        layout.verticalSpacing = 5;
        composite.setLayout(layout);

        Composite marginComposite = new Composite(composite, SWT.NONE);
        marginComposite.setBackground(parent.getBackground());
        marginComposite.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
        marginComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 1;
        layout2.marginHeight = 1;
        marginComposite.setLayout(layout2);

        createNullTextControl(marginComposite);
        createAddCommentActionBar(composite);

        marginComposite.setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#298fca"))); //$NON-NLS-1$
    }

    private void createNullTextControl(Composite parent) {
        textViewer = createTextViewer(parent, DEFAULT_STYLE);
        initTextViewer(textViewer);
        setEditable(true);

        StyledText text = textViewer.getTextWidget();
        text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
        text.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#000000"))); //$NON-NLS-1$
        text.setFont(JFaceResources.getDefaultFont());

        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
        gridData.horizontalIndent = 0;
        gridData.verticalIndent = 0;
        gridData.minimumHeight = 110;

        IDocument contentDocument = new Document(null);
        setDocument(contentDocument);

        ScrolledComposite sc = container.getScrolledComposite();
        if (sc.getClientArea().width != 0) {
            gridData.widthHint = sc.getClientArea().width - EXTRA_WIDTH;
        }
        text.setLayoutData(gridData);
        sc.addControlListener(getControlListener());

        text.addModifyListener(getModifyListener());
        text.addFocusListener(getFocusListener());

        text.setFocus();
    }

    private void createAddCommentActionBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        composite.setBackground(parent.getBackground());
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        IAction cancelAddCommentAction = new Action() {

            @Override
            public void run() {
                resetModified();
                isLinkHovering = false;
                container.cancelCreateComment();
            }
        };

        Hyperlink cancelLink = createLink(composite,
                MindMapMessages.Comment_Cancel_text,
                MindMapMessages.Comment_Cancel_tooltip, cancelAddCommentAction);
        cancelLink.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

        IAction addCommentAction = new Action() {

            @Override
            public void run() {
                addComment();
                MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                        UserDataConstants.CATEGORY_COMMENTS,
                        UserDataConstants.ADD_COMMENT);
            }
        };
        Hyperlink addLink = createLink(composite,
                MindMapMessages.AddCommentLink_text,
                MindMapMessages.AddCommentLink_tooltip, addCommentAction);
        addLink.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
    }

    private Hyperlink createLink(Composite parent, String text, String toolTip,
            final IAction action) {
        final Hyperlink link = new Hyperlink(parent, SWT.NONE);
        link.setBackground(link.getParent().getBackground());
        link.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#0082F9"))); //$NON-NLS-1$
        link.setFont((Font) resources.get(
                JFaceResources.getDefaultFontDescriptor().increaseHeight(1)));
        link.setText(text);
        link.setToolTipText(toolTip);
        link.setUnderlined(false);
        link.addHyperlinkListener(new IHyperlinkListener() {
            public void linkEntered(HyperlinkEvent e) {
                link.setUnderlined(true);
                isLinkHovering = true;
            }

            public void linkExited(HyperlinkEvent e) {
                link.setUnderlined(false);
                isLinkHovering = false;
            }

            public void linkActivated(HyperlinkEvent e) {
                Display.getCurrent().asyncExec(new Runnable() {
                    public void run() {
                        try {
                            action.run();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        return link;
    }

    private void createNotNullContentArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        composite.setBackground(parent.getBackground());

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 7;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setBackground(parent.getBackground());
        composite2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout4 = new GridLayout(1, false);
        layout4.marginWidth = 10 - layout.marginWidth;
        layout4.marginHeight = 0;
        composite2.setLayout(layout4);

        Composite marginComposite = new Composite(composite2, SWT.NONE);
        marginComposite.setBackground(parent.getBackground());
        marginComposite.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#ffffff"))); //$NON-NLS-1$
        marginComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 1;
        layout2.marginHeight = 1;
        marginComposite.setLayout(layout2);

        createNotNullTextControl(marginComposite);

        Composite composite3 = new Composite(parent, SWT.NONE);
        composite3.setBackground(parent.getBackground());
        composite3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout3 = new GridLayout(1, false);
        layout3.marginWidth = 10;
        layout3.marginHeight = 0;
        composite3.setLayout(layout3);

        createTimeLabel(composite3);
        createModifyCommentActionBar(composite3);

        modifyCommentActionBar.setVisible(false);
        ((GridData) modifyCommentActionBar.getLayoutData()).exclude = true;

        addMouseFilter();
        CommentsUtils.addRecursiveMouseListener(control, getMouseListener(),
                null);
        addContextMenu();
        selectionProvider
                .addSelectionChangedListener(getSelectionChangedListener());
    }

    private void createNotNullTextControl(Composite parent) {
        textViewer = createTextViewer(parent, DEFAULT_STYLE);
        initTextViewer(textViewer);
        setEditable(false);

        StyledText text = textViewer.getTextWidget();
        text.setBackground(parent.getBackground());
        text.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#45464a"))); //$NON-NLS-1$
        text.setFont(JFaceResources.getDefaultFont());

        GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, true);
        gridData.minimumHeight = 22;

        String author = comment.getAuthor() + " : "; //$NON-NLS-1$
        String commentContent = comment.getContent();
        IDocument contentDocument = new Document(author + commentContent);
        setDocument(contentDocument);

        styleRange = new StyleRange();
        styleRange.start = 0;
        styleRange.length = author.lastIndexOf(':');
        styleRange.foreground = null;
        styleRange.background = null;
        styleRange.fontStyle = SWT.BOLD;
        text.setStyleRange(styleRange);

        ScrolledComposite sc = container.getScrolledComposite();
        if (sc.getClientArea().width != 0) {
            gridData.widthHint = sc.getClientArea().width - EXTRA_WIDTH;
        }
        text.setLayoutData(gridData);
        sc.addControlListener(getControlListener());

        text.addModifyListener(getModifyListener());
        text.addFocusListener(getFocusListener());
    }

    private void createTimeLabel(Composite parent) {
        timeLabel = new Label(parent, SWT.NONE);
        timeLabel.setAlignment(SWT.LEFT);
        timeLabel.setBackground(parent.getBackground());
        timeLabel.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#999999"))); //$NON-NLS-1$
        timeLabel.setFont((Font) resources.get(
                JFaceResources.getDefaultFontDescriptor().increaseHeight(-1)));

        GridData layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        layoutData.horizontalIndent = 3;
        layoutData.verticalIndent = 0;
        timeLabel.setLayoutData(layoutData);

        if (comment != container.getLatestCreatedComment()) {
            long timeMillisString = comment.getTime();
            String dateString = TextFormatter.formatTimeMillis(timeMillisString,
                    CommentsConstants.DATE_FORMAT_PATTERN);
            String timeString = TextFormatter.formatTimeMillis(timeMillisString,
                    CommentsConstants.TIME_FORMAT_PATTERN);
            timeLabel.setText(dateString + " at " + timeString); //$NON-NLS-1$
        } else {
            container.setLatestCreatedComment(null);
            timeLabel.setText(MindMapMessages.Comment_JustNow_text);
        }
    }

    private void createModifyCommentActionBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.verticalIndent = 5;
        composite.setLayoutData(layoutData);

        composite.setBackground(parent.getBackground());
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        IAction cancelModifyAction = new Action() {

            @Override
            public void run() {
                cancelEditing();
            }
        };
        Hyperlink cancelLink = createLink(composite,
                MindMapMessages.Comment_Cancel_text,
                MindMapMessages.Comment_Cancel_tooltip, cancelModifyAction);
        cancelLink.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false));

        IAction modifyCommentAction = new Action() {

            @Override
            public void run() {
                saveComment();
            }
        };
        Hyperlink addLink = createLink(composite,
                MindMapMessages.ModifyComment_text,
                MindMapMessages.ModifyComment_tooltip, modifyCommentAction);
        addLink.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));

        modifyCommentActionBar = composite;
    }

    //recursion add contextMenu
    private void addContextMenu() {
        setRecursionContextMenu(control, getContextMenu());
    }

    //recursion remove contextMenu
    private void removeContextMenu() {
        setRecursionContextMenu(control, null);
    }

    private void setRecursionContextMenu(Control control, Menu contextMenu) {
        if (control != null && !control.isDisposed()) {
            control.setMenu(contextMenu);
        }
        if (control instanceof Composite) {
            Control[] children = ((Composite) control).getChildren();
            for (Control child : children) {
                setRecursionContextMenu(child, contextMenu);
            }
        }
    }

    private Menu getContextMenu() {
        if (contextMenu == null || contextMenu.isDisposed()) {
            MenuManager menuManager = new MenuManager();

            menuManager.add(new Action(MindMapMessages.Comment_Edit_label) {
                @Override
                public void run() {
                    startEditing();
                }
            });

            menuManager.add(new Action(MindMapMessages.Comment_Delete_label) {
                @Override
                public void run() {
                    DeleteCommentCommand cmd = new DeleteCommentCommand(
                            comment.getOwnedWorkbook().getElementById(
                                    comment.getObjectId()),
                            comment);
                    ICommandStack cs = targetEditor.getCommandStack();
                    cs.execute(cmd);
                }
            });

            menuManager.add(new Action(MindMapMessages.Comment_Reply_label) {
                @Override
                public void run() {
                    container.createComment(comment.getObjectId());
                }
            });

            contextMenu = menuManager.createContextMenu(control.getShell());
        }
        return contextMenu;
    }

    private TextViewer createTextViewer(Composite parent, int style) {
        return new LessLatencyTextViewer(parent, style);
    }

    private void initTextViewer(TextViewer textViewer) {
        Control control = textViewer.getTextWidget();
        createContentPopupMenu(control);
        textViewer.setTextDoubleClickStrategy(
                new DefaultTextDoubleClickStrategy(),
                IDocument.DEFAULT_CONTENT_TYPE);

        textViewer.setUndoManager(new TextViewerUndoManager(25));
        textViewer.activatePlugins();
        addHyperlinkListener(textViewer);
    }

    private void createContentPopupMenu(Control control) {
        textContextMenuManager = new MenuManager();
        textContextMenuManager.setRemoveAllWhenShown(true);
        textContextMenuManager.addMenuListener(new IMenuListener() {
            public void menuAboutToShow(IMenuManager manager) {
                fillContextMenu(manager);
            }
        });
        textContextMenu = textContextMenuManager.createContextMenu(control);
        control.setMenu(textContextMenu);
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

    private void addMouseFilter() {
        Display.getCurrent().addFilter(SWT.MouseEnter, getListener());
        Display.getCurrent().addFilter(SWT.MouseExit, getListener());
    }

    private void removeMouseFilter() {
        Display.getCurrent().removeFilter(SWT.MouseEnter, getListener());
        Display.getCurrent().removeFilter(SWT.MouseExit, getListener());
    }

    private Listener getListener() {
        if (listener == null) {
            listener = new Listener() {

                public void handleEvent(Event event) {
                    if (control.isDisposed()) {
                        removeMouseFilter();
                        return;
                    }
                    if (event.widget instanceof Control) {
                        boolean isAncestor = isAncestorOf((Composite) control,
                                (Control) event.widget);
                        switch (event.type) {
                        case SWT.MouseEnter:
                            if (isAncestor) {
                                setRecursiveBackgroundColor(control, hoverColor,
                                        null);
                                control.layout();
                            }
                            break;
                        case SWT.MouseExit:
                            if (!isAncestor) {
                                if (textViewer.getTextWidget()
                                        .isFocusControl()) {
                                    setRecursiveBackgroundColor(control,
                                            originalColor, getTextWidget());
                                } else {
                                    setRecursiveBackgroundColor(control,
                                            originalColor, null);
                                }
                                control.layout();
                            }
                            break;
                        }
                    }
                }
            };
        }
        return listener;
    }

    private boolean isAncestorOf(Control composite, Control control) {
        if (control == composite) {
            return true;
        }
        Composite parent = control.getParent();
        while (parent != null && parent != composite
                && !(parent instanceof Shell)) {
            parent = parent.getParent();
        }
        return parent == composite;
    }

    private void setRecursiveBackgroundColor(Control control, Color background,
            Control excludeControl) {
        if (control == excludeControl) {
            return;
        }
        control.setBackground(background);
        if (control instanceof Composite) {
            Control[] children = ((Composite) control).getChildren();
            for (Control child : children) {
                setRecursiveBackgroundColor(child, background, excludeControl);
            }
        }
    }

    private MouseListener getMouseListener() {
        if (mouseListener == null) {
            mouseListener = new MouseAdapter() {

                public void mouseDown(MouseEvent e) {
                    if (getSelection() instanceof IStructuredSelection
                            && ((IStructuredSelection) getSelection())
                                    .getFirstElement() == CommentTextViewer.this) {
                        if (e.button == 1) {
                            selectionProvider.removeSelectionChangedListener(
                                    getSelectionChangedListener());
                            setSelection(null);
                            selectionProvider.addSelectionChangedListener(
                                    getSelectionChangedListener());

                            startEditing();
                        }
                    } else {
                        setSelection(new StructuredSelection(
                                CommentTextViewer.this));
                    }
                }
            };
        }

        return mouseListener;
    }

    private ISelectionChangedListener getSelectionChangedListener() {
        if (selectionChangedListener == null) {
            selectionChangedListener = new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    ISelection oldSelection = ((CommentsSelectionProvider) selectionProvider)
                            .getOldSelection();
                    if (oldSelection instanceof IStructuredSelection) {
                        Object obj = ((IStructuredSelection) oldSelection)
                                .getFirstElement();
                        if (obj == CommentTextViewer.this
                                && !getTextWidget().isFocusControl()) {
                            ((CommentTextViewer) obj).commentDeselected();
                        }
                    }

                    ISelection selection = event.getSelection();
                    if (selection instanceof IStructuredSelection) {
                        Object obj = ((IStructuredSelection) selection)
                                .getFirstElement();
                        if (obj == CommentTextViewer.this) {
                            commentSelected();
                        }
                    }
                }
            };
        }

        return selectionChangedListener;
    }

    private void commentSelected() {
//        if (container instanceof CommentsView) {
//            CommentsUtils.reveal(targetEditor, comment.getOwnedWorkbook()
//                    .getElementById(comment.getObjectId()));
//        }
        removeMouseFilter();
        setRecursiveBackgroundColor(control, selectColor, null);

        container.getContentComposite().pack();
        container.getContentComposite().layout(true, true);
    }

    private void commentDeselected() {
        addMouseFilter();

        if (textViewer.getTextWidget().isFocusControl()) {
            setRecursiveBackgroundColor(control, originalColor,
                    getTextWidget());
        } else {
            setRecursiveBackgroundColor(control, originalColor, null);
        }

        container.getContentComposite().pack();
        container.getContentComposite().layout(true, true);
    }

    private FocusListener getFocusListener() {
        if (textFocusListener == null) {
            textFocusListener = new FocusListener() {

                public void focusGained(FocusEvent e) {
                    textViewer.getTextWidget().setBackground(
                            e.display.getSystemColor(SWT.COLOR_WHITE));

                    getTextWidget().addCaretListener(getCaretListener());
                    getTextViewer().addTextListener(getTextListener());
                    getTextViewer().addPostSelectionChangedListener(
                            getPostSelectionChangedListener());

                    addSpellChecker(textViewer);
                    showControl();
                    updateActions();

                    container.setSelectedComment(comment);
                }

                public void focusLost(FocusEvent e) {
                    textViewer.getTextWidget().setBackground(originalColor);

                    getTextWidget().removeCaretListener(getCaretListener());
                    getTextViewer().removePostSelectionChangedListener(
                            getPostSelectionChangedListener());
                    setEditable(false);
                    removeSpellChecker();

                    container.setSelectedComment(null);

                    if (!isLinkHovering) {
                        if (comment == null) {
                            addComment();
                            container.setModified(true);
                        } else {
                            boolean modified = saveComment();
                            container.setModified(modified);
                        }
                    }
                }
            };
        }

        return textFocusListener;
    }

    private void startEditing() {
        container.getContentComposite().forceFocus();

        //store last editing.
        if (container.isModified()) {
            container.setModified(false);
            container.setEditingComment(comment);
            return;
        }
        if (control.isDisposed()) {
            return;
        }

        removeMouseFilter();
        setRecursiveBackgroundColor(control, originalColor, null);
        getTextWidget().setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#000000"))); //$NON-NLS-1$

        getTextWidget().getParent().setBackground(
                (Color) resources.get(ColorUtils.toDescriptor("#298fca"))); //$NON-NLS-1$

        CommentsUtils.removeRecursiveMouseListener(control, getMouseListener(),
                null);
        removeContextMenu();
        textViewer.getControl().setMenu(textContextMenu);

        textViewer.getTextWidget().setStyleRange(null);

        String commentContent = comment.getContent();
        IDocument contentDocument = new Document(commentContent);
        setDocument(contentDocument);

        timeLabel.setVisible(false);
        ((GridData) timeLabel.getLayoutData()).exclude = true;

        modifyCommentActionBar.setVisible(true);
        ((GridData) modifyCommentActionBar.getLayoutData()).exclude = false;

        ((GridData) textViewer.getControl()
                .getLayoutData()).minimumHeight = 110;
        setEditable(true);

        textViewer.getControl().setFocus();

        container.getContentComposite().pack();
        container.getContentComposite().layout(true, true);
    }

    private void cancelEditing() {
        setRecursiveBackgroundColor(control, originalColor, null);
        getTextWidget().setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#45464a"))); //$NON-NLS-1$

        CommentsUtils.addRecursiveMouseListener(control, getMouseListener(),
                null);
        addContextMenu();

        String author = comment.getAuthor() + " : "; //$NON-NLS-1$
        String commentContent = comment.getContent();
        IDocument contentDocument = new Document(author + commentContent);
        setDocument(contentDocument);

        textViewer.getTextWidget().setStyleRange(styleRange);

        resetModified();

        timeLabel.setVisible(true);
        ((GridData) timeLabel.getLayoutData()).exclude = false;

        modifyCommentActionBar.setVisible(false);
        ((GridData) modifyCommentActionBar.getLayoutData()).exclude = true;

        ((GridData) textViewer.getControl().getLayoutData()).minimumHeight = 22;
        setEditable(false);
        addMouseFilter();

        if (container != null && container.getContentComposite() != null
                && !container.getContentComposite().isDisposed()) {
            container.getContentComposite().pack();
            container.getContentComposite().layout(true, true);
        }
    }

    private void showControl() {
        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                if (!container.getScrolledComposite().isDisposed()
                        && !control.isDisposed()) {
                    container.getScrolledComposite().showControl(control);
                }
            }
        });
    }

    private void updateActions() {
        Display.getCurrent().asyncExec(new Runnable() {

            public void run() {
                if (control.isDisposed() || getTextWidget().isDisposed()) {
                    return;
                }
                IComment selectedComment = (control.isFocusControl()
                        || getTextWidget().isFocusControl()) ? comment : null;
                contributor.selectedCommentChanged(selectedComment);
            }
        });
    }

    private void addSpellChecker(ITextViewer textViewer) {
        spellingActivation = SpellingSupport.getInstance()
                .activateSpelling(textViewer);
        spellingPreferences = SpellingPlugin.getDefault().getPreferenceStore();
        if (spellingPreferences != null) {
            spellingPreferences
                    .addPropertyChangeListener(getPropertyChangeListener());
        }
        contributor.setSpellingActivation(spellingActivation);
    }

    private void removeSpellChecker() {
        if (spellingPreferences != null) {
            spellingPreferences
                    .removePropertyChangeListener(getPropertyChangeListener());
            spellingPreferences = null;
        }
        if (spellingActivation != null) {
            spellingActivation.getSpellingSupport()
                    .deactivateSpelling(spellingActivation);
            spellingActivation = null;
        }
    }

    //for SpellChecker
    private IPropertyChangeListener getPropertyChangeListener() {
        if (propertyChangeListener == null) {
            propertyChangeListener = new IPropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent event) {
                    if (SpellingPlugin.SPELLING_CHECK_ENABLED
                            .equals(event.getProperty())) {
                        if (spellingActivation != null) {
                            spellingActivation.getSpellingSupport()
                                    .deactivateSpelling(spellingActivation);
                            spellingActivation = null;
                        }
                        if (spellingPreferences.getBoolean(
                                SpellingPlugin.SPELLING_CHECK_ENABLED)) {
                            spellingActivation = SpellingSupport.getInstance()
                                    .activateSpelling(textViewer);
                        }
                    }
                }
            };
        }

        return propertyChangeListener;
    }

    //sc.setNewOrigin when caret move(editing)
    private CaretListener getCaretListener() {
        if (caretListener == null) {
            caretListener = new CaretListener() {

                public void caretMoved(CaretEvent event) {
                    Display.getCurrent().asyncExec(new Runnable() {

                        public void run() {
                            if (getTextWidget() == null
                                    || getTextWidget().isDisposed()) {
                                return;
                            }

                            Caret caret = getTextWidget().getCaret();
                            ScrolledComposite sc = container
                                    .getScrolledComposite();
                            Rectangle caretBounds = caret.getBounds();
                            Rectangle scClientArea = sc.getClientArea();
                            int caretToDisplayY = getTextWidget()
                                    .toDisplay(caret.getLocation()).y;
                            Point scToDisplayPoint = sc.getParent()
                                    .toDisplay(sc.getLocation());
                            int scToDisplayY = scToDisplayPoint.y;
                            Point scOrigin = sc.getOrigin();
                            if (caretToDisplayY
                                    + caretBounds.height > scToDisplayY
                                            + scClientArea.height) {
                                scOrigin.y += (caretToDisplayY
                                        + caretBounds.height)
                                        - (scToDisplayY + scClientArea.height);
                                sc.setOrigin(scOrigin);
                            } else if (caretToDisplayY < scToDisplayY) {
                                scOrigin.y -= scToDisplayY - caretToDisplayY;
                                sc.setOrigin(scOrigin);
                            }
                        }
                    });
                }
            };
        }

        return caretListener;
    }

    //update text popup menus' selection
    private ISelectionChangedListener getPostSelectionChangedListener() {
        if (postSelectionChangedListener == null) {
            postSelectionChangedListener = new ISelectionChangedListener() {

                public void selectionChanged(SelectionChangedEvent event) {
                    contributor.update(textViewer);
                }
            };
        }

        return postSelectionChangedListener;
    }

    //set text.widthHint -- then pack in CommentsView / CommentsPopup.
    private ControlListener getControlListener() {
        if (controlListener == null) {
            controlListener = new ControlListener() {

                public void controlMoved(ControlEvent e) {
                }

                public void controlResized(ControlEvent e) {
                    if (container == null
                            || container.getContentComposite().isDisposed()) {
                        return;
                    }
                    Composite contentComposite = container
                            .getContentComposite();
                    StyledText text = getTextWidget();
                    if (text != null && !text.isDisposed()) {
                        ((GridData) text
                                .getLayoutData()).widthHint = contentComposite
                                        .getParent().getClientArea().width
                                        - EXTRA_WIDTH;
                    }
                }
            };
        }

        return controlListener;
    }

    //register Core.WorkbookPreSaveOnce when editing -- make workbook dirty.
    private ITextListener getTextListener() {
        if (textListener == null) {
            textListener = new ITextListener() {

                public void textChanged(
                        org.eclipse.jface.text.TextEvent event) {
                    activateJob();
                }
            };
        }

        return textListener;
    }

    //pack contentComposite when styledText be modified.
    private ModifyListener getModifyListener() {
        if (modifyListener == null) {
            modifyListener = new ModifyListener() {

                public void modifyText(ModifyEvent event) {
                    Composite contentComposite = container
                            .getContentComposite();
                    int realTextlHeight = contentComposite
                            .computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                    if (textHeight != realTextlHeight) {
                        textHeight = realTextlHeight;
                        contentComposite.pack();
                    }
                }
            };
        }

        return modifyListener;
    }

    private void activateJob() {
        if (saveCommentReg != null && saveCommentReg.isValid()) {
            return;
        }
        saveCommentReg = null;

        if (workbook instanceof ICoreEventSource2) {
            saveCommentReg = ((ICoreEventSource2) workbook)
                    .registerOnceCoreEventListener(Core.WorkbookPreSaveOnce,
                            ICoreEventListener.NULL);
        }
    }

    private void deactivateJob() {
        if (saveCommentReg != null) {
            saveCommentReg.unregister();
            saveCommentReg = null;
        }
    }

    private void resetModified() {
        deactivateJob();
        getTextViewer().removeTextListener(getTextListener());
    }

    private void handleControlDispose(DisposeEvent e) {
        if (textContextMenuManager != null) {
            textContextMenuManager.dispose();
            textContextMenuManager = null;
        }
        if (textContextMenu != null) {
            textContextMenu.dispose();
            textContextMenu = null;
        }
        if (contextMenu != null) {
            contextMenu.dispose();
            contextMenu = null;
        }
        if (document != null) {
            document = null;
        }
        removeMouseFilter();
        getTextWidget().removeFocusListener(getFocusListener());
        ScrolledComposite sc = container.getScrolledComposite();
        if (sc != null && !sc.isDisposed()) {
            sc.removeControlListener(getControlListener());
        }
        if (selectionProvider != null) {
            selectionProvider.removeSelectionChangedListener(
                    getSelectionChangedListener());
        }

        getTextWidget().removeModifyListener(getModifyListener());
    }

    private boolean addComment() {
        resetModified();
        isLinkHovering = false;

        final IDocument document = (IDocument) textViewer.getInput();
        if (document == null || document.get() == null
                || document.get().equals("")) { //$NON-NLS-1$
            Display.getCurrent().asyncExec(new Runnable() {

                @Override
                public void run() {
                    container.cancelCreateComment();
                }
            });
            return false;
        }

        String author = System.getProperty(DOMConstants.AUTHOR_NAME);
        author = (author != null ? author : System.getProperty("user.name")); //$NON-NLS-1$
        long time = System.currentTimeMillis();
        IComment comment = workbook.getCommentManager().createComment(author,
                time, objectId);
        final AddCommentCommand cmd = new AddCommentCommand(author, time,
                objectId, document.get(), workbook, comment);

        container.setLatestCreatedComment(comment);

        final ICommandStack cs = targetEditor.getCommandStack();
        Display.getCurrent().asyncExec(new Runnable() {
            public void run() {
                cs.execute(cmd);
            }
        });
        return true;
    }

    private boolean saveComment() {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_COMMENTS,
                UserDataConstants.ADD_COMMENT);

        resetModified();
        isLinkHovering = false;

        String oldContent = (comment == null ? null : comment.getContent());
        IDocument document = (IDocument) textViewer.getDocument();
        String newContent = document.get();
        if (newContent != null && newContent.equals(oldContent)) {
            cancelEditing();
            return false;
        }

        final ICommandStack cs = targetEditor.getCommandStack();
        Command command = null;

        if (document == null || document.get() == null
                || document.get().equals("")) { //$NON-NLS-1$
            command = new DeleteCommentCommand(comment.getOwnedWorkbook()
                    .getElementById(comment.getObjectId()), comment);
        } else {
            command = new ModifyCommentCommand(comment.getOwnedWorkbook()
                    .getElementById(comment.getObjectId()), comment,
                    document.get());
        }

        final Command command_0 = command;
        Display.getCurrent().asyncExec(new Runnable() {

            @Override
            public void run() {
                cs.execute(command_0);
            }
        });

        return true;
    }

    private void setEditable(boolean editable) {
        if (editable == this.editable) {
            return;
        }
        this.editable = editable;
        updateTextControl();
    }

    private void setDocument(IDocument document) {
        this.document = document;

        textViewer.setDocument(document == null ? new Document() : document);
        updateTextControl();
        // move the caret to the end of document
        if (document != null) {
            textViewer.setSelectedRange(document.getLength(), 0);
        }
    }

    private void updateTextControl() {
        textViewer.setEditable(editable);
        if (textViewer.getTextWidget() != null
                && !textViewer.getTextWidget().isDisposed()) {
            textViewer.getTextWidget().setEnabled(editable);
        }
    }

    public TextViewer getTextViewer() {
        return textViewer;
    }

    private StyledText getTextWidget() {
        return textViewer.getTextWidget();
    }

    private ISelection getSelection() {
        return selectionProvider.getSelection();
    }

    private void setSelection(ISelection selection) {
        selectionProvider.setSelection(selection);
    }

    public void setTargetEditor(IGraphicalEditor targetEditor) {
        this.targetEditor = targetEditor;
    }

}
