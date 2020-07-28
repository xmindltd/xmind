package org.xmind.ui.internal.comments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.core.Core;
import org.xmind.core.IComment;
import org.xmind.core.ICommentManager;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.util.TopicIterator;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.e4models.CommentsPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.tabfolder.DelegatedSelectionProvider;
import org.xmind.ui.util.MindMapUtils;
import org.xmind.ui.util.TextFormatter;

public class SheetCommentsViewer
        implements ICoreEventListener, ISelectionChangedListener {

    private ResourceManager resources;

    private ISheet input;

    private ICommentsActionBarContributor contributor;

    private ISelectionProvider selectionProvider;

    private ICommentTextViewerContainer container;

    private IGraphicalEditor targetEditor;

    private TopicCommentsViewer topicViewer;

    private Label titleLabel;

    private ICoreEventRegister eventRegister;

    private List<CommentTextViewer> controls = new ArrayList<CommentTextViewer>();

    private List<CommentTextViewer> implementations = new ArrayList<CommentTextViewer>();

    private Map<ITopic, TopicCommentsViewer> topicViewers = new HashMap<ITopic, TopicCommentsViewer>();

    private Composite sheetCommentsComposite;

    private Control newCommentControl;

    private Button insertButton;

    private Hyperlink insertHyperlink;

    private ITopic select;

    private String creatingTargetId;

    private Composite content;

    private Composite parent;

    public SheetCommentsViewer(ISheet input,
            ICommentsActionBarContributor contributor,
            ISelectionProvider selectionProvider,
            ICommentTextViewerContainer container,
            IGraphicalEditor targetEditor) {
        this.input = input;
        this.contributor = contributor;
        this.selectionProvider = selectionProvider;
        this.container = container;
        setTargetEditor(targetEditor);
    }

    public void create(Composite parent) {
        this.parent = parent;
        init();
        createContent(parent, input);
        restoreEditing();
    }

    private void init() {
        if (controls != null) {
            controls.clear();
        } else {
            controls = new ArrayList<CommentTextViewer>();
        }
        if (implementations != null) {
            implementations.clear();
        } else {
            implementations = new ArrayList<CommentTextViewer>();
        }
        if (topicViewers != null) {
            topicViewers.clear();
        } else {
            topicViewers = new HashMap<ITopic, TopicCommentsViewer>();
        }
    }

    private Composite createContent(Composite parent, ISheet sheet) {
        Composite composite = new Composite(parent, SWT.NONE);
        this.content = composite;
        resources = new LocalResourceManager(JFaceResources.getResources(),
                composite);
        composite.setBackground(composite.getParent().getBackground());
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(layoutData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);

        if (sheet == null || !existComment(sheet)) {
            //If have no comment, create null comment content.
            container.getScrolledComposite().setExpandVertical(true);
            createNullContentArea(composite);
        } else {
            createAllComments(composite, sheet);
        }

        composite.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                handleControlDisposed(e);
            }
        });

        return composite;
    }

    private void restoreEditing() {
        //restore last editing.
        final CommentsPart part = (CommentsPart) container;
        final String objectId = part.getInsertTarget();
        if (objectId != null) {
            Display.getCurrent().asyncExec(new Runnable() {

                @Override
                public void run() {
                    insertComment(objectId);
                    container.setModified(false);
                    part.setInsertTarget(null);
                }
            });
        }
    }

    private boolean existComment(ISheet sheet) {
        if (sheet == null) {
            return false;
        }

        if (creatingTargetId != null) {
            return true;
        }

        ICommentManager commentManager = sheet.getOwnedWorkbook()
                .getCommentManager();
        if (commentManager.isEmpty()) {
            return false;
        }
        if (commentManager.hasComments(sheet.getId())) {
            return true;
        }

        TopicIterator ite = new TopicIterator(sheet.getRootTopic());
        while (ite.hasNext()) {
            ITopic topic = ite.next();
            if (commentManager.hasComments(topic.getId())) {
                return true;
            }
        }
        return false;
    }

    private Control createAllComments(Composite parent, ISheet sheet) {
        container.getScrolledComposite().setExpandVertical(false);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout gridLayout = new GridLayout(1, false);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginTop = 9;
        gridLayout.marginBottom = 29;
        gridLayout.verticalSpacing = 18;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        boolean showTopicsComments = createTopicsComments(composite,
                sheet.getRootTopic());
        if (showTopicsComments) {
            createSeparatorLine(composite);
        }

        createSheetComments(composite, sheet);
        boolean showSheetComments = sheet.getOwnedWorkbook().getCommentManager()
                .getComments(sheet.getId()).size() != 0;
        if (showSheetComments) {
            createSeparatorLine(composite);
        }

        createInsertCommentHyperlink(composite);

        return composite;
    }

    /**
     * @param parent
     * @param sheet
     * @return true if create not less than one comment, false otherwise.
     */
    private boolean createTopicsComments(Composite parent, ITopic root) {
        boolean hasContent = false;
        Iterator<ITopic> topicIt = new TopicIterator(root);
        while (topicIt.hasNext()) {
            ITopic topic = topicIt.next();
            if (topic.getOwnedWorkbook().getCommentManager().hasComments(
                    topic.getId()) || topic.getId().equals(creatingTargetId)) {
                if (hasContent) {
                    createSeparatorLine(parent);
                }
                createTopicLabelAndComments(parent, topic);
                hasContent = true;
            }
        }

        return hasContent;
    }

    private void createSeparatorLine(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 10;
        composite.setLayout(layout);

        Label sep = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        sep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        sep.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_BLACK));
    }

    private void createTopicLabelAndComments(Composite parent, ITopic topic) {
        topicViewer = new TopicCommentsViewer(topic, contributor,
                selectionProvider, container, true, targetEditor);
        topicViewer.create(parent);

        if (topicViewer.getControls() != null) {
            controls.addAll(topicViewer.getControls());
        }
        if (topicViewer.getImplementations() != null) {
            implementations.addAll(topicViewer.getImplementations());
        }
        topicViewers.put(topic, topicViewer);
    }

    private void createSheetComments(Composite parent, ISheet sheet) {
        Set<IComment> comments = new TreeSet<IComment>(sheet.getOwnedWorkbook()
                .getCommentManager().getComments(sheet.getId()));
        if (comments.isEmpty()) {
            return;
        }

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 5;
        composite.setLayout(layout);
        this.sheetCommentsComposite = composite;

        createSheetLabel(composite, sheet);

        for (IComment comment : comments) {
            createCommentControl(composite, comment);
        }
    }

    private void createSheetLabel(Composite parent, final ISheet sheet) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 10;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        titleLabel = new Label(composite, SWT.LEFT | SWT.HORIZONTAL);
        titleLabel.setBackground(parent.getBackground());
        titleLabel.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#353535"))); //$NON-NLS-1$
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        data.horizontalIndent = 2;
        titleLabel.setLayoutData(data);
        titleLabel.setFont((Font) resources
                .get(FontDescriptor.createFrom(FontUtils.relativeHeight(
                        titleLabel.getFont().getFontData(), 1))));

        titleLabel.setText(MindMapMessages.Comment_SHEET_text
                + TextFormatter.removeNewLineCharacter(sheet.getTitleText()));
        hookSheetTitle();

        titleLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                CommentsUtils.reveal(targetEditor, sheet);
            }
        });
    }

    private void createCommentControl(Composite parent, IComment comment) {
        CommentTextViewer implementation = new CommentTextViewer(comment,
                input.getId(), input.getOwnedWorkbook(), contributor,
                selectionProvider, container, targetEditor);
        implementation.createControl(parent);

        registerControl(implementation);
        registerImplementation(implementation);
    }

    private void createNullContentArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());
        GridData layoutData = new GridData(GridData.FILL_BOTH);

        composite.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        createNullContent(composite);
    }

    private void createNullContent(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());
        composite.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, true, true));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 25;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setBackground(label.getParent().getBackground());
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label.setImage((Image) resources
                .get(MindMapUI.getImages().get("comment-empty-bg.png", true))); //$NON-NLS-1$

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
        label2.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#aaaaaa"))); //$NON-NLS-1$
        label2.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label2.setText(""); //$NON-NLS-1$
        label2.setFont((Font) resources.get(FontDescriptor.createFrom(
                FontUtils.relativeHeight(label2.getFont().getFontData(), 2))));

        Label label3 = new Label(composite2, SWT.NONE);
        label3.setBackground(label3.getParent().getBackground());
        label3.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#aaaaaa"))); //$NON-NLS-1$
        label3.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label3.setText(MindMapMessages.Comment_FirstAdd_text);
        label3.setFont((Font) resources.get(FontDescriptor.createFrom(
                FontUtils.relativeHeight(label3.getFont().getFontData(), 2))));

        createInsertButtonSection(composite);
    }

    private void createInsertButtonSection(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginTop = 30;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        insertButton = new Button(composite, SWT.PUSH);
        insertButton.setBackground(composite.getBackground());
        GridData layoutData = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        layoutData.widthHint = 90;
        insertButton.setLayoutData(layoutData);
        insertButton.setText(MindMapMessages.SheetCommentViewer_Insert_button);

        insertButton.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                insertComment();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });

        //add selection listener.
        if (this.targetEditor != null) {
            setSelection(targetEditor.getSite().getSelectionProvider()
                    .getSelection());
        } else {
            setSelection(null);
        }
    }

    private void createInsertCommentHyperlink(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
        composite.setLayoutData(gridData);

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.marginLeft = 15;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        insertHyperlink = new Hyperlink(composite, SWT.NONE);
        insertHyperlink.setBackground(composite.getBackground());
        insertHyperlink.setForeground(
                (Color) resources.get(ColorUtils.toDescriptor("#0082F9"))); //$NON-NLS-1$
        GridData layoutData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        insertHyperlink.setLayoutData(layoutData);
        insertHyperlink
                .setText(MindMapMessages.SheetCommentViewer_Insert_hyperlink);

        insertHyperlink.addListener(SWT.MouseDown, new Listener() {

            @Override
            public void handleEvent(Event event) {
                insertComment();
            }
        });

        //add selection listener.
        if (this.targetEditor != null) {
            setSelection(targetEditor.getSite().getSelectionProvider()
                    .getSelection());
        } else {
            setSelection(null);
        }
    }

    private void insertComment() {
        if (select == null) {
            return;
        }
        String targetId = select.getId();

        //store last insert state.
        if (container.isModified()) {
            container.setModified(false);
            ((CommentsPart) container).setInsertTarget(targetId);
            return;
        }

        insertComment(targetId);
    }

    private void insertComment(String targetId) {
        creatingTargetId = targetId;

        Composite contentComposite = container.getContentComposite();
        contentComposite.setRedraw(false);

        content.dispose();

        create(parent);

        createNewComment(targetId);

        contentComposite.pack();
        contentComposite.setRedraw(true);

        creatingTargetId = null;
    }

    private void update() {
        Composite contentComposite = container.getContentComposite();
        if (contentComposite == null || contentComposite.isDisposed()) {
            return;
        }

        contentComposite.setRedraw(false);

        content.dispose();

        create(parent);

        contentComposite.pack(true);
        contentComposite.layout(true, true);
        contentComposite.setRedraw(true);
    }

    private void registerControl(CommentTextViewer control) {
        controls.add(control);
    }

    private void registerImplementation(CommentTextViewer implementation) {
        implementations.add(implementation);
    }

    private void hookSheetTitle() {
        if (eventRegister == null) {
            eventRegister = new CoreEventRegister(input, this);
        }
        eventRegister.register(Core.TitleText);
    }

    private void unhookSheetTitle() {
        if (eventRegister != null) {
            eventRegister.unregisterAll();
            eventRegister = null;
        }
    }

    public void handleCoreEvent(final CoreEvent event) {
        PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
            public void run() {
                if (Core.TitleText.equals(event.getType())) {
                    if (titleLabel != null && !titleLabel.isDisposed()) {
                        titleLabel.setText(MindMapMessages.Comment_SHEET_text
                                + TextFormatter.removeNewLineCharacter(
                                        input.getTitleText()));
                        titleLabel.getParent().layout(true, true);
                    }
                }
            }
        });
    }

    private void handleControlDisposed(DisposeEvent e) {
        unhookSheetTitle();
        if (controls != null) {
            controls.clear();
            controls = null;
        }
        if (implementations != null) {
            implementations.clear();
            implementations = null;
        }
        if (topicViewers != null) {
            topicViewers.clear();
            topicViewers = null;
        }
    }

    public void setTargetEditor(IGraphicalEditor targetEditor) {
        if (targetEditor == this.targetEditor) {
            return;
        }
        if (this.targetEditor != null) {
            ((DelegatedSelectionProvider) this.targetEditor.getSite()
                    .getSelectionProvider())
                            .removeAsyncSelectionChangedListener(this);
        }

        this.targetEditor = targetEditor;

        if (this.targetEditor != null) {
            ((DelegatedSelectionProvider) this.targetEditor.getSite()
                    .getSelectionProvider())
                            .addAsyncSelectionChangedListener(this);
            setSelection(targetEditor.getSite().getSelectionProvider()
                    .getSelection());
        } else {
            setSelection(null);
        }

        if (topicViewer != null) {
            topicViewer.setTargetEditor(targetEditor);
        }
        if (implementations != null) {
            for (CommentTextViewer implementation : implementations) {
                implementation.setTargetEditor(targetEditor);
            }
        }
    }

    public List<CommentTextViewer> getControls() {
        return controls;
    }

    public List<CommentTextViewer> getImplementations() {
        return controls;
    }

    public void createNewComment(String objectId) {
        if (newCommentControl != null && !newCommentControl.isDisposed()) {
            newCommentControl.dispose();
        }

        Object object = input.getOwnedWorkbook().getElementById(objectId);
        if (object instanceof ITopic) {
            newCommentControl = topicViewers.get((ITopic) object)
                    .createNewComment();
        }
        if (object instanceof ISheet) {
            CommentTextViewer implementation = new CommentTextViewer(null,
                    input.getId(), input.getOwnedWorkbook(), contributor,
                    selectionProvider, container, targetEditor);
            newCommentControl = implementation
                    .createControl(sheetCommentsComposite);
            newCommentControl
                    .moveAbove((sheetCommentsComposite.getChildren())[0]);
            container.getContentComposite().pack();

            implementation.getTextViewer().getTextWidget().forceFocus();
        }
    }

    public void cancelCreateNewComment() {
        if (newCommentControl != null && !newCommentControl.isDisposed()) {
            newCommentControl.dispose();
            newCommentControl = null;
            update();
        }
    }

    public void save() {
        Control contentComposite = container.getContentComposite();
        if (contentComposite != null && !contentComposite.isDisposed()) {
            contentComposite.forceFocus();
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event) {
        setSelection(event.getSelection());
    }

    private void setSelection(ISelection selection) {
        boolean isSingleTopic = MindMapUtils.isSingleTopic(selection);
        if (isSingleTopic) {
            select = (ITopic) MindMapUtils
                    .getAllSuchElements(selection, MindMapUI.CATEGORY_TOPIC)
                    .get(0);
        } else {
            select = null;
        }

        if (insertButton != null && !insertButton.isDisposed()) {
            insertButton.setEnabled(isSingleTopic);
        }
        if (insertHyperlink != null && !insertHyperlink.isDisposed()) {
            insertHyperlink.setEnabled(isSingleTopic);
        }

        container.setModified(false);
    }

    public void dispose() {
        setTargetEditor(null);
    }

}
