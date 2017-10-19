package org.xmind.ui.internal.notes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.util.SafeRunnable;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSupport;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.actions.DeleteNotesAction;
import org.xmind.ui.internal.e4models.IModelConstants;
import org.xmind.ui.internal.utils.E4Utils;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.util.TextFormatter;

public class SheetNotesViewer
        implements INotesContentViewer, ICoreEventListener {

//    private static final int TRUNCATE_LENGTH = 50;

    private static final int EXTRA_WIDTH = 28;

    private ISheet sheet;

    private IBoundary boundary;

    private IRelationship relationship;

    private Composite composite;

//    private ToolBarManager toolBarManager = null;

    private ScrolledComposite sc;

    private Composite contentComposite;

    private List<Composite> textComposites = new ArrayList<Composite>();

    private List<Control> textControls = new ArrayList<Control>();

    private Map<Composite, ITopic> map = new HashMap<Composite, ITopic>();

    private Composite currentTextComposite;

    private IGraphicalEditor editor;

    private int scClientWidth = 0;

    private DeleteNotesAction deleteNotesAction;

    private ICoreEventRegister notesEventRegister;

    private ICoreEventRegister titleEventRegister;

    private Color originalColor;

    private Color hoverColor = ColorUtils.getColor("#f9f9f9"); //$NON-NLS-1$

    private Color selectColor = ColorUtils.getColor("#f5f5f5"); //$NON-NLS-1$

    private Listener filter;

    //storage the control of each note, used to handle mouseEnter and mouseClick event
    private List<Control> controls = new ArrayList<Control>();

    private ResourceManager resources;

    public SheetNotesViewer(IGraphicalEditor editor) {
        this.editor = editor;
    }

    public Control createControl(Composite parent) {
        resetCollections();
        composite = new Composite(parent, SWT.NONE);
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
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createContentArea(composite);

//        composite.addDisposeListener(new DisposeListener() {
//            public void widgetDisposed(DisposeEvent e) {
//                handleControlDispose(e);
//            }
//        });
        return composite;
    }

//    private void handleControlDispose(DisposeEvent e) {
//        if (toolBarManager != null) {
//            toolBarManager.dispose();
//            toolBarManager = null;
//        }
//    }

    private void resetCollections() {
        if (textComposites != null) {
            textComposites.clear();
        }
        if (textControls != null) {
            textControls.clear();
        }
        if (map != null) {
            map.clear();
        }
        if (controls != null) {
            controls.clear();
        }
    }

    private void createContentArea(Composite parent) {
//        Control toolBar = createToolBar(parent);
//        if (toolBar != null) {
//            createSeparator(parent);
//        }
        contentComposite = createContentComposite(parent);
        deleteNotesAction = new DeleteNotesAction(this);

        //add mouse enter filter
        addMouseFilter();
        contentComposite.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                removeMouseFilter();
            }
        });
    }

//    private Control createToolBar(Composite parent) {
//        toolBarManager = new ToolBarManager(SWT.FLAT);
//        deleteNotesAction = new DeleteNotesAction(this);
//        toolBarManager.add(deleteNotesAction);
//        parent.addListener(SWT.Resize, new Listener() {
//            public void handleEvent(Event event) {
//                toolBarManager.update(true);
//            }
//        });
//        ToolBar toolBar = toolBarManager.createControl(parent);
//        toolBar.setBackground(parent.getDisplay().getSystemColor(
//                SWT.COLOR_WIDGET_BACKGROUND));
//        toolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//
//        return toolBar;
//    }

//    private void createSeparator(Composite parent) {
//        Label sep = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
//        sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        sep.setBackground(parent.getDisplay().getSystemColor(
//                SWT.COLOR_WIDGET_BACKGROUND));
//    }

    private Composite createContentComposite(Composite parent) {
        sc = new ScrolledComposite(parent, SWT.V_SCROLL);
//        sc.setAlwaysShowScrollBars(true);
        sc.setLayoutData(new GridData(GridData.FILL_BOTH));
        sc.setBackground(parent.getBackground());

        sc.setExpandHorizontal(true);
        sc.setMinSize(SWT.DEFAULT, SWT.DEFAULT);

        Composite composite = new Composite(sc, SWT.NONE);
        composite.setBackground(parent.getBackground());

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 10;
        gridLayout.marginTop = 0;
        gridLayout.verticalSpacing = 10;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        sc.setContent(composite);
        sc.getVerticalBar().setIncrement(17);

        return composite;
    }

    private void showAllNotes(Composite parent) {
        sc.setExpandVertical(false);
        List<ITopic> topics = NotesUtils.getAllTopicsWithNotes(sheet);
        if (topics.size() == 0) {
            sc.setExpandVertical(true);
            createNullContent(parent);
            return;
        }
        for (int i = 0; i < topics.size() - 1; i++) {
            showLabelAndNote(parent, topics.get(i));
            createSeparatorLine(parent);
        }
        showLabelAndNote(parent, topics.get(topics.size() - 1));

        if (sc.getClientArea().width > 0) {
            for (Control textControl : textControls) {
                ((GridData) textControl.getLayoutData()).widthHint = sc
                        .getClientArea().width - EXTRA_WIDTH;
            }
        }

        sc.addListener(SWT.Resize, new Listener() {

            public void handleEvent(Event event) {
                sc.getDisplay().asyncExec(new Runnable() {

                    public void run() {
                        if (sc.isDisposed() || contentComposite.isDisposed()) {
                            return;
                        }
                        if (sc.getClientArea().width > 0) {
                            if (scClientWidth != sc.getClientArea().width) {
                                scClientWidth = sc.getClientArea().width;
                                for (Control textControl : textControls) {
                                    if (textControl != null
                                            && !textControl.isDisposed()) {
                                        ((GridData) textControl
                                                .getLayoutData()).widthHint = sc
                                                        .getClientArea().width
                                                        - EXTRA_WIDTH;
                                    }
                                }
                                contentComposite.pack();
                            }
                        }
                    }
                });
            }
        });
    }

    private void createNullContent(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());
        composite.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, true));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 20;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setBackground(label.getParent().getBackground());
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label.setImage((Image) resources
                .get(MindMapUI.getImages().get("notes-empty-bg.png", true))); //$NON-NLS-1$

        Composite composite2 = new Composite(composite, SWT.NONE);
        composite2.setBackground(composite2.getParent().getBackground());
        composite2.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 0;
        layout2.marginHeight = 0;
        layout2.verticalSpacing = 0;
        composite2.setLayout(layout2);

        Label label2 = new Label(composite2, SWT.NONE);
        label2.setBackground(label2.getParent().getBackground());
        label2.setForeground(ColorUtils.getColor("#aaaaaa")); //$NON-NLS-1$
        label2.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label2.setText(""); //$NON-NLS-1$
        label2.setFont(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 2));

        Label label3 = new Label(composite2, SWT.NONE);
        label3.setBackground(label3.getParent().getBackground());
        label3.setForeground(ColorUtils.getColor("#aaaaaa")); //$NON-NLS-1$
        label3.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label3.setText(MindMapMessages.Comments_FirstAdd_text);
        label3.setFont(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 2));
    }

    private void addMouseFilter() {
        Display.getCurrent().addFilter(SWT.MouseEnter, getFilter());
        Display.getCurrent().addFilter(SWT.MouseExit, getFilter());
    }

    private void removeMouseFilter() {
        Display.getCurrent().removeFilter(SWT.MouseEnter, getFilter());
        Display.getCurrent().removeFilter(SWT.MouseExit, getFilter());
    }

    private Listener getFilter() {
        if (filter == null) {
            filter = new Listener() {

                public void handleEvent(Event event) {
                    if (contentComposite.isDisposed()) {
                        return;
                    }
                    if (event.widget instanceof Control) {
                        Control mouseControl = null;
                        for (Control control : controls) {
                            if (control == null || control.isDisposed()) {
                                continue;
                            }
                            boolean isAncestor = isAncestorOf(
                                    (Composite) control,
                                    (Control) event.widget);
                            if (isAncestor) {
                                mouseControl = control;
                                break;
                            }
                        }

                        if (mouseControl != null
                                && !mouseControl.isDisposed()) {
                            switch (event.type) {
                            case SWT.MouseEnter:
                                if (mouseControl.getBackground()
                                        .equals(originalColor)) {
                                    setRecursiveBackgroundColor(mouseControl,
                                            hoverColor, null);
                                    ((Composite) mouseControl).layout();
                                }
                                break;
                            case SWT.MouseExit:
                                if (mouseControl.getBackground()
                                        .equals(hoverColor)) {
                                    setRecursiveBackgroundColor(mouseControl,
                                            originalColor, null);
                                    ((Composite) mouseControl).layout();
                                }
                                break;
                            }
                        }
                    }
                }
            };
        }
        return filter;
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

    private void showLabelAndNote(Composite parent, ITopic topic) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        originalColor = composite.getBackground();
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 5;
        layout.marginLeft = 0;
        layout.marginRight = 0;
        layout.marginHeight = 8;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 5;
        composite.setLayout(layout);

        createTopicLabel(composite, topic);
        createSingleNotes(composite, topic);
        createContextMenu(composite, topic);
        controls.add(composite);
    }

    //recursion add contextMenu
    private void createContextMenu(Composite control, ITopic topic) {
        setRecursionContextMenu(control, getContextMenu(control, topic));
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

    private Menu getContextMenu(Control control, final ITopic topic) {
        MenuManager menuManager = new MenuManager();

        menuManager.add(new Action(MindMapMessages.Notes_Edit_text) {
            @Override
            public void run() {
                MindMapUtils.reveal(editor, topic);
            }
        });

        menuManager.add(new Action(MindMapMessages.Notes_Delete_text) {
            @Override
            public void run() {
                DeleteNotesAction deleteNotesAction2 = new DeleteNotesAction(
                        SheetNotesViewer.this);
                deleteNotesAction2.setSelection(new StructuredSelection(topic));
                if (deleteNotesAction2.isEnabled()) {
                    deleteNotesAction2.run();
                }
            }
        });

        return menuManager.createContextMenu(control.getShell());
    }

    private void createSeparatorLine(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 9;
        layout.marginLeft = 14;
        layout.marginRight = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Label sep = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sep.setBackground(ColorConstants.black);
    }

    private void createTopicLabel(Composite parent, ITopic topic) {
        Composite c = new Composite(parent, SWT.NONE);
        c.setBackground(parent.getBackground());
        c.setLayoutData(new GridData(GridData.FILL_BOTH));
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 3;
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
        label.setForeground(ColorUtils.getColor("#515151")); //$NON-NLS-1$
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalIndent = 0;
        label.setLayoutData(data);

        label.setFont(FontUtils.getBold(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 1)));
        label.setText(TextFormatter.removeNewLineCharacter(topic.getTitleText())
                + ":"); //$NON-NLS-1$s
    }

    private void createSingleNotes(Composite parent, ITopic topic) {
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());

        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalIndent = 18;
        composite.setLayoutData(data);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        Control text = createText(composite, topic);

        final Composite composite2 = parent;
        textComposites.add(composite2);
        map.put(composite2, topic);
        textControls.add(text);

        final MouseListener mouseListener = new MouseAdapter() {
            public void mouseDown(MouseEvent e) {
                if (composite2 == null || composite2.isDisposed()) {
                    return;
                }
                if (!composite2.isFocusControl()) {
                    composite2.forceFocus();
                }
            }

            public void mouseDoubleClick(MouseEvent e) {
                reveal(composite2);
            }
        };

        final KeyListener keyListener = new KeyListener() {

            public void keyPressed(KeyEvent e) {
                if (e.keyCode == SWT.ARROW_DOWN) {
                    moveDown();
                } else if (e.keyCode == SWT.ARROW_UP) {
                    moveUp();
                } else if (e.keyCode == SWT.CR) {
                    reveal(currentTextComposite);
                } else if (e.keyCode == SWT.DEL) {
                    deleteNotesAction.run();
                }
            }

            public void keyReleased(KeyEvent e) {
            }
        };

        FocusListener focusListener = new FocusListener() {

            public void focusLost(FocusEvent e) {
                setRecursiveBackgroundColor(composite2, originalColor, null);
                composite2.removeKeyListener(keyListener);
                setCurrentTextComposite(null);
            }

            public void focusGained(FocusEvent e) {
                setRecursiveBackgroundColor(composite2, selectColor, null);
                composite2.addKeyListener(keyListener);
                sc.showControl(composite2);

                setCurrentTextComposite(composite2);
            }
        };

        addMosuseListener(composite2, mouseListener);
        composite2.addFocusListener(focusListener);
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

    private void setCurrentTextComposite(Composite composite) {
        if (composite != currentTextComposite) {
            currentTextComposite = composite;
            updateDeleteAction();
        }
    }

    private void updateDeleteAction() {
        ITopic topic = getCurrentTopic(currentTextComposite);
        deleteNotesAction.setSelection(
                topic == null ? null : new StructuredSelection(topic));
    }

    private StyledText createText(Composite parent, ITopic topic) {
        StyledText text = new StyledText(parent,
                SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
        text.setBackground(parent.getBackground());
        text.setLayoutData(new GridData(GridData.FILL_BOTH));
        text.setEnabled(false);

        text.setFont(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 0));
        RichDocumentNotesAdapter adapter = new RichDocumentNotesAdapter(topic);
//        text.setText(truncate(adapter.getDocument().get()));
        text.setText(adapter.getDocument().get());

        text.setForeground(ColorUtils.getColor("#9a9a9a")); //$NON-NLS-1$
//        text.setEnabled(false);

        return text;
    }

//    private String truncate(String text) {
//        return text == null ? null
//                : (text.length() <= TRUNCATE_LENGTH ? text
//                        : text.substring(0, TRUNCATE_LENGTH) + "..."); //$NON-NLS-1$
//    }

    private void reveal(Composite composite) {
        MindMapUtils.reveal(editor, getCurrentTopic(composite));
        SafeRunner.run(new SafeRunnable() {

            @Override
            public void run() throws Exception {
                E4Utils.showPart(IModelConstants.COMMAND_SHOW_MODEL_PART,
                        PlatformUI.getWorkbench().getActiveWorkbenchWindow(),
                        IModelConstants.PART_ID_NOTES, null,
                        IModelConstants.PART_STACK_ID_RIGHT);
            }
        });
    }

    private void addMosuseListener(Control c, MouseListener ml) {
        c.addMouseListener(ml);
        if (c instanceof Composite) {
            for (final Control cc : ((Composite) c).getChildren()) {
                addMosuseListener(cc, ml);
            }
        }
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

    private ITopic getCurrentTopic(Composite currentComposite) {
        if (map == null || map.size() == 0) {
            return null;
        }

        return map.get(currentComposite);
    }

    public void setInput(Object input) {
        if (input instanceof ISheet || input instanceof IBoundary
                || input instanceof IRelationship) {
            ISheet sheet = null;
            if (input instanceof ISheet) {
                if (this.sheet == input)
                    return;
                sheet = (ISheet) input;
            } else if (input instanceof IBoundary) {
                if (this.boundary == input)
                    return;
                unhookBoundary();
                this.boundary = (IBoundary) input;
                sheet = boundary.getOwnedSheet();
                hookBoundary();
            } else if (input instanceof IRelationship) {
                if (this.relationship == input)
                    return;
                unhookRelationship();
                this.relationship = (IRelationship) input;
                sheet = relationship.getOwnedSheet();
                hookRelationship();
            }
            if (sheet != this.sheet) {
                unhookSheet();
                unhookTitle();
                this.sheet = sheet;
                hookTitle();
                hookSheet();
                update();
            }
        }
    }

    private void update() {
        if (contentComposite.isDisposed()) {
            return;
        }

        resetCollections();
        contentComposite.setRedraw(false);
        Control[] children = contentComposite.getChildren();
        for (Control child : children) {
            child.dispose();
        }
        showAllNotes(contentComposite);

        contentComposite.pack();
        contentComposite.layout(true, true);
        contentComposite.setRedraw(true);
    }

    private void hookSheet() {
        if (notesEventRegister == null)
            notesEventRegister = new CoreEventRegister(
                    sheet.getAdapter(ICoreEventSupport.class), this);
        notesEventRegister.register(Core.TopicNotes);
    }

    private void unhookSheet() {
        if (notesEventRegister != null) {
            notesEventRegister.unregisterAll();
            notesEventRegister = null;
        }
    }

    private void hookBoundary() {
        if (notesEventRegister == null)
            notesEventRegister = new CoreEventRegister(
                    boundary.getAdapter(ICoreEventSupport.class), this);
        notesEventRegister.register(Core.TopicNotes);
    }

    private void unhookBoundary() {
        if (notesEventRegister != null) {
            notesEventRegister.unregisterAll();
            notesEventRegister = null;
        }
    }

    private void hookRelationship() {
        if (notesEventRegister == null)
            notesEventRegister = new CoreEventRegister(
                    relationship.getAdapter(ICoreEventSupport.class), this);
        notesEventRegister.register(Core.TopicNotes);
    }

    private void unhookRelationship() {
        if (notesEventRegister != null) {
            notesEventRegister.unregisterAll();
            notesEventRegister = null;
        }
    }

    private void hookTitle() {
        if (titleEventRegister == null)
            titleEventRegister = new CoreEventRegister(
                    sheet.getAdapter(ICoreEventSupport.class), this);
        titleEventRegister.register(Core.TitleText);
    }

    private void unhookTitle() {
        if (titleEventRegister != null) {
            titleEventRegister.unregisterAll();
            titleEventRegister = null;
        }
    }

    public void handleCoreEvent(final CoreEvent event) {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                String eventType = event.getType();
                if (Core.TopicNotes.equals(eventType)
                        || Core.TitleText.endsWith(eventType)) {
                    update();
                }
            }
        });
    }

    public void dispose() {
        unhookSheet();
        unhookTitle();
        composite.dispose();
    }

    public void setEditor(IGraphicalEditor editor) {
        this.editor = editor;
    }

    public IGraphicalEditor getEditor() {
        return editor;
    }

    public Control getControl() {
        return composite;
    }

}
