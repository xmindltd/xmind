package org.xmind.ui.internal.comments;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.Core;
import org.xmind.core.IComment;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.ui.editor.IGraphicalEditor;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.TextFormatter;

public class TopicCommentsViewer implements ICoreEventListener {

    private ITopic input;

    private ICommentsActionBarContributor contributor;

    private ISelectionProvider selectionProvider;

    private ICommentTextViewerContainer container;

    private boolean showTopicLabel;

    private IGraphicalEditor targetEditor;

    private Label titleLabel;

    private ICoreEventRegister eventRegister;

    private List<CommentTextViewer> controls = new ArrayList<CommentTextViewer>();

    private List<CommentTextViewer> implementations = new ArrayList<CommentTextViewer>();

    private Control addCommentControl;

    private Composite commentsComposite;

    private Control newCommentControl;

    public TopicCommentsViewer(ITopic input,
            ICommentsActionBarContributor contributor,
            ISelectionProvider selectionProvider,
            ICommentTextViewerContainer container, boolean showTopicLabel,
            IGraphicalEditor targetEditor) {
        this.input = input;
        this.contributor = contributor;
        this.selectionProvider = selectionProvider;
        this.container = container;
        this.showTopicLabel = showTopicLabel;
        this.targetEditor = targetEditor;
    }

    public void create(Composite parent) {
        init();
        createControl(input, parent);
    }

    private void init() {
        if (controls != null) {
            controls.clear();
        }
        if (implementations != null) {
            implementations.clear();
        }
    }

    private Control createControl(ITopic topic, Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);

        if (showTopicLabel) {
            createTopicLabel(composite, topic);
        }

        if (!showTopicLabel) {
            addCommentControl = createAddCommentControl(composite);
        }

        commentsComposite = createComments(topic, composite);

        composite.addDisposeListener(new DisposeListener() {

            public void widgetDisposed(DisposeEvent e) {
                handleControlDisposed(e);
            }
        });

        return composite;
    }

    private void createTopicLabel(Composite parent, final ITopic topic) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 10;
        layout.marginHeight = 0;
        layout.horizontalSpacing = 0;
        layout.marginBottom = 6;
        composite.setLayout(layout);

        titleLabel = new Label(composite, SWT.LEFT | SWT.HORIZONTAL);
        titleLabel.setBackground(parent.getBackground());
        titleLabel.setForeground(ColorUtils.getColor("#353535")); //$NON-NLS-1$
        GridData data = new GridData(SWT.CENTER, SWT.CENTER, true, false);
        data.horizontalIndent = 2;
        titleLabel.setLayoutData(data);

        titleLabel.setFont(FontUtils.getBold(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 1)));

        titleLabel.setText(MindMapMessages.Comment_TOPIC_text
                + TextFormatter.removeNewLineCharacter(topic.getTitleText()));
        hookTopicTitle();

        titleLabel.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                CommentsUtils.reveal(targetEditor, topic);
            }
        });
    }

    private Control createAddCommentControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 10;
        layout.marginHeight = 0;
        layout.marginTop = 10;
        layout.marginBottom = 10;
        composite.setLayout(layout);

        Composite marginComposite = new Composite(composite, SWT.NONE);
        marginComposite
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        GridLayout layout3 = new GridLayout(1, false);
        layout3.marginHeight = 1;
        layout3.marginWidth = 1;
        marginComposite.setLayout(layout3);
        marginComposite.setBackground(ColorUtils.getColor("#a0a0a0")); //$NON-NLS-1$

        Composite roundRectangleComposite = new Composite(marginComposite,
                SWT.NONE);
        roundRectangleComposite.setForeground(ColorUtils.getColor("#ffffff")); //$NON-NLS-1$
        roundRectangleComposite.setBackground(
                parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));

        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
        layoutData.heightHint = 40;
        roundRectangleComposite.setLayoutData(layoutData);

        GridLayout layout2 = new GridLayout(1, false);
        layout2.marginWidth = 2;
        layout2.marginHeight = 2;
        roundRectangleComposite.setLayout(layout2);

        Text text = new Text(roundRectangleComposite,
                SWT.LEFT | SWT.SINGLE | SWT.READ_ONLY);
        text.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true));
        text.setBackground(text.getParent().getForeground());
        text.setForeground(ColorUtils.getColor("#aaaaaa")); //$NON-NLS-1$

        text.setFont(FontUtils.getItalic(
                FontUtils.getRelativeHeight(JFaceResources.DEFAULT_FONT, 1)));
        text.setText(MindMapMessages.Comment_Add_text);

        text.setEnabled(false);

        roundRectangleComposite.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                createNewComment();
            }
        });

        return composite;
    }

    public Control createNewComment() {
        if (newCommentControl != null && !newCommentControl.isDisposed()) {
            return newCommentControl;
        }

        if (addCommentControl != null && !addCommentControl.isDisposed()) {
            addCommentControl.setVisible(false);
            ((GridData) addCommentControl.getLayoutData()).exclude = true;
        }

        CommentTextViewer implementation = new CommentTextViewer(null,
                input.getId(), input.getOwnedWorkbook(), contributor,
                selectionProvider, container, targetEditor);
        newCommentControl = implementation.createControl(commentsComposite);
        newCommentControl.moveAbove((commentsComposite.getChildren())[0]);
        container.getContentComposite().pack();

        implementation.getTextViewer().getTextWidget().forceFocus();

        return newCommentControl;
    }

    public void cancelCreateNewComment() {
        if (newCommentControl != null && !newCommentControl.isDisposed()) {
            newCommentControl.dispose();
            newCommentControl = null;
        }

        if (addCommentControl != null && !addCommentControl.isDisposed()) {
            addCommentControl.setVisible(true);
            ((GridData) addCommentControl.getLayoutData()).exclude = false;
        }

        container.getContentComposite().pack();
    }

    private Composite createComments(ITopic topic, Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setBackground(parent.getBackground());
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.horizontalSpacing = 0;
        layout.verticalSpacing = 3;
        composite.setLayout(layout);

        Set<IComment> comments = new TreeSet<IComment>(topic.getOwnedWorkbook()
                .getCommentManager().getComments(topic.getId()));
        for (IComment comment : comments) {
            createCommentControl(composite, comment);
        }

        return composite;
    }

    private void createCommentControl(Composite parent, IComment comment) {
        CommentTextViewer implementation = new CommentTextViewer(comment,
                input.getId(), input.getOwnedWorkbook(), contributor,
                selectionProvider, container, targetEditor);
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

    private void hookTopicTitle() {
        if (eventRegister == null) {
            eventRegister = new CoreEventRegister(input, this);
        }
        eventRegister.register(Core.TitleText);
    }

    private void unhookTopicTitle() {
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
                        titleLabel.setText(MindMapMessages.Comment_TOPIC_text
                                + TextFormatter.removeNewLineCharacter(
                                        input.getTitleText()));
                        titleLabel.getParent().layout(true, true);
                    }
                }
            }
        });
    }

    private void handleControlDisposed(DisposeEvent e) {
        unhookTopicTitle();
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
            this.targetEditor = targetEditor;
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
        return implementations;
    }

    public void save() {
        Control contentComposite = container.getContentComposite();
        if (contentComposite != null && !contentComposite.isDisposed()) {
            contentComposite.forceFocus();
        }
    }

}
