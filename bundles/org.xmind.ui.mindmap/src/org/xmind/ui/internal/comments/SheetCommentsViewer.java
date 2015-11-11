package org.xmind.ui.internal.comments;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.comment.IComment;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.internal.dom.SheetImpl;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.TextFormatter;

public class SheetCommentsViewer implements ICoreEventListener {

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

    public SheetCommentsViewer(ISheet input,
            ICommentsActionBarContributor contributor,
            ISelectionProvider selectionProvider,
            ICommentTextViewerContainer container,
            IGraphicalEditor targetEditor) {
        this.input = input;
        this.contributor = contributor;
        this.selectionProvider = selectionProvider;
        this.container = container;
        this.targetEditor = targetEditor;
    }

    public void create(Composite parent) {
        init();
        createAllComments(parent, input);
    }

    private void init() {
        if (controls != null) {
            controls.clear();
        }
        if (implementations != null) {
            implementations.clear();
        }
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
        gridLayout.verticalSpacing = 18;
        gridLayout.horizontalSpacing = 0;
        composite.setLayout(gridLayout);

        boolean hasTopicsCommentsCreated = createTopicsComments(composite,
                sheet);
        boolean shouldSheetCommentsCreated = sheet.getOwnedWorkbook()
                .getCommentManager().getComments(sheet).size() != 0;
        if (hasTopicsCommentsCreated && shouldSheetCommentsCreated) {
            createSeparatorLine(composite);
        }
        createSheetComments(composite, sheet);

        //If have no comment, create null comment content.
        if (!hasTopicsCommentsCreated && !shouldSheetCommentsCreated) {
            container.getScrolledComposite().setExpandVertical(true);
            createNullContentArea(parent);
        }

        composite.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                handleControlDisposed(e);
            }
        });

        return composite;
    }

    private void createNullContentArea(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(composite.getParent().getBackground());
        final GridData layoutData = new GridData(GridData.FILL_BOTH);
        composite.setLayoutData(layoutData);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

//        createNullContentToolbar(composite);
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
        layout.verticalSpacing = 20;
        composite.setLayout(layout);

        Label label = new Label(composite, SWT.NONE);
        label.setBackground(label.getParent().getBackground());
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label.setImage(MindMapUI.getImages().get("comment-empty-bg.png", true) //$NON-NLS-1$
                .createImage());

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
        label2.setText(MindMapMessages.Comment_NoComments_text);
        label2.setFont(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 2));

        Label label3 = new Label(composite2, SWT.NONE);
        label3.setBackground(label3.getParent().getBackground());
        label3.setForeground(ColorUtils.getColor("#aaaaaa")); //$NON-NLS-1$
        label3.setLayoutData(
                new GridData(SWT.CENTER, SWT.CENTER, false, false));
        label3.setText(MindMapMessages.Comment_FirstAdd_text);
        label3.setFont(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 2));
    }

    /**
     * 
     * @param parent
     * @param sheet
     * @return true if create not less than one comment, false otherwise.
     */
    private boolean createTopicsComments(Composite parent, ISheet sheet) {
        List<ITopic> topics = CommentsUtils.getAllTopicsWithComments(sheet);
        if (topics.size() == 0) {
            return false;
        } else {
            for (ITopic topic : topics) {
                createTopicLabelAndComments(parent, topic);
                if (topics.indexOf(topic) != topics.size() - 1) {
                    createSeparatorLine(parent);
                }
            }
            return true;
        }
    }

    private void createTopicLabelAndComments(Composite parent, ITopic topic) {
        topicViewer = new TopicCommentsViewer(topic, contributor,
                selectionProvider, container, true, targetEditor, false);
        topicViewer.create(parent);
        controls.addAll(topicViewer.getControls());
        implementations.addAll(topicViewer.getImplementations());
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

    private void createSheetComments(Composite parent, ISheet sheet) {
        List<IComment> comments = sheet.getOwnedWorkbook().getCommentManager()
                .getComments(sheet);
        if (comments.size() == 0) {
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

        createSheetLabel(composite, sheet);

        for (int i = comments.size() - 1; i >= 0; i--) {
            createCommentControl(composite, comments.get(i));
        }
    }

    private void createSheetLabel(Composite parent, final ISheet sheet) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//        GridLayout layout = new GridLayout(2, false);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 10;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        composite.setLayout(layout);

        titleLabel = new Label(composite, SWT.LEFT | SWT.HORIZONTAL);
        titleLabel.setBackground(parent.getBackground());
        titleLabel.setForeground(ColorUtils.getColor("#353535")); //$NON-NLS-1$
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        data.horizontalIndent = 2;
        titleLabel.setLayoutData(data);

        titleLabel.setFont(FontUtils.getBold(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 1)));

        titleLabel.setText(MindMapMessages.Comment_SHEET_text
                + TextFormatter.removeNewLineCharacter(sheet.getTitleText()));
        hookSheetTitle();

//        final Label createButton = new Label(composite, SWT.NONE);
//        createButton.setBackground(createButton.getParent().getBackground());
//
//        final CommentAction addCommentAction = contributor
//                .getAction("org.xmind.ui.action.addComment"); //$NON-NLS-1$
//        createButton
//                .setImage(addCommentAction.getImageDescriptor().createImage());
//
//        GridData layoutData = new GridData(SWT.RIGHT, SWT.TOP, false, false);
//        layoutData.widthHint = createButton.getImage().getBounds().width;
//        layoutData.heightHint = createButton.getImage().getBounds().height;
//        createButton.setLayoutData(layoutData);
//
//        createButton.addMouseListener(new MouseAdapter() {
//
//            @Override
//            public void mouseDown(MouseEvent e) {
//                ((Control) e.widget).getParent().forceFocus();
//                if (addCommentAction.isEnabled()) {
//                    addCommentAction.selectionChanged(sheet);
//                    addCommentAction.run();
//                }
//            }
//        });

        titleLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                CommentsUtils.reveal(targetEditor, sheet);
            }
        });
    }

    private void createCommentControl(Composite parent, IComment comment) {
        CommentTextViewer implementation = new CommentTextViewer(comment,
                contributor, selectionProvider, container, targetEditor, true);
        implementation.createControl(parent);

        registerControl(implementation);
        registerImplementation(implementation);
    }

    private void registerControl(CommentTextViewer control) {
        controls.add(control);
    }

    private void registerImplementation(CommentTextViewer implementation) {
        implementations.add(implementation);
    }

    private void hookSheetTitle() {
        if (eventRegister == null) {
            eventRegister = new CoreEventRegister(
                    ((SheetImpl) input).getCoreEventSupport(), this);
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
    }

    public void setTargetEditor(IGraphicalEditor targetEditor) {
        if (this.targetEditor != targetEditor) {
            if (topicViewer != null) {
                topicViewer.setTargetEditor(targetEditor);
            }
            if (implementations != null) {
                for (CommentTextViewer implementation : implementations) {
                    implementation.setTargetEditor(targetEditor);
                }
            }
        }
    }

    public List<CommentTextViewer> getControls() {
        return controls;
    }

    public List<CommentTextViewer> getImplementations() {
        return controls;
    }

}
