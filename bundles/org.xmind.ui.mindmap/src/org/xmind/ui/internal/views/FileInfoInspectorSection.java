package org.xmind.ui.internal.views;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.xmind.core.Core;
import org.xmind.core.IFileEntry;
import org.xmind.core.IManifest;
import org.xmind.core.IMeta;
import org.xmind.core.INotes;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.CoreEventRegister;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.internal.dom.NumberUtils;
import org.xmind.ui.internal.utils.CommandUtils;
import org.xmind.ui.util.MindMapUtils;

public class FileInfoInspectorSection extends InspectorSection
        implements ICoreEventListener {

    private ICoreEventRegister register;

    private ICoreEventRegister revisionRegister;

    private ICoreEventRegister modifyTimeRegister;

    private List<ITopic> allTopics;

    private Label estimateSizeLabel;

    private Label topicsCountLabel;

    private Label wordsCountLabel;

    private Hyperlink revisions;

    private Label modifyTimeLabel;

    private Label modifyByLabel;

    private Label createdTimeLabel;

    public FileInfoInspectorSection() {
        setTitle(Messages.FileInfoInspectorSection_title);
    }

    @Override
    protected Composite createContent(Composite parent) {
        Composite composite = super.createContent(parent);

        createEstimateSizeItem(composite);
        createWordsItem(composite);
        createTopicsItem(composite);
        createRevisionsItem(composite);
        createModifiedTimeItem(composite);
        createModifiedByItem(composite);
        createCreatedTimeItem(composite);

        return composite;
    }

    private Composite createEstimateSizeItem(Composite parent) {
        Composite item = createItemComposite(parent);
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.FileInfoEstimateSize_label);

        if (estimateSizeLabel == null)
            estimateSizeLabel = new Label(item, SWT.NONE);
        estimateSizeLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        estimateSizeLabel.setText(getSize());
        return item;
    }

    private Composite createWordsItem(Composite parent) {
        Composite item = createItemComposite(parent);
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.FileInfoWords_label);

        if (wordsCountLabel == null)
            wordsCountLabel = new Label(item, SWT.NONE);
        wordsCountLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        wordsCountLabel.setText(getWordsCount());
        return item;
    }

    private Composite createTopicsItem(Composite parent) {
        Composite item = createItemComposite(parent);
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.FileInfoTopics_label);

        if (topicsCountLabel == null)
            topicsCountLabel = new Label(item, SWT.NONE);
        topicsCountLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        topicsCountLabel.setText(getTopicsCount());
        return item;
    }

    private Composite createRevisionsItem(Composite parent) {
        Composite item = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        item.setLayout(layout);
        item.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.FileInfoRevisions_label);

        if (revisions == null)
            revisions = new Hyperlink(item, SWT.NONE);
        revisions.setForeground(
                Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
        revisions.setUnderlined(true);
        revisions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        revisions.setText(getRevisions());
        addRevisionsListener();

        return item;
    }

    private void addRevisionsListener() {
        revisions.addHyperlinkListener(new HyperlinkAdapter() {
            @Override
            public void linkActivated(HyperlinkEvent e) {
                IWorkbenchWindow window = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow();
                if (window != null) {
                    IWorkbenchPage page = window.getActivePage();
                    if (page != null)
                        CommandUtils.executeCommand(
                                "org.xmind.ui.command.editingHistory", window); //$NON-NLS-1$
                }
            }
        });
    }

    private Composite createModifiedTimeItem(Composite parent) {
        Composite item = createItemComposite(parent);
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.FileInfoModifiedTime_label);

        if (modifyTimeLabel == null)
            modifyTimeLabel = new Label(item, SWT.NONE);
        modifyTimeLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        modifyTimeLabel.setText(getModifiedTime());
        return item;
    }

    private Composite createModifiedByItem(Composite parent) {
        Composite item = createItemComposite(parent);
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.FileInfoModifiedBy_label);

        if (modifyByLabel == null)
            modifyByLabel = new Label(item, SWT.NONE);
        modifyByLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        modifyByLabel.setText(getModifiedBy());
        return item;
    }

    private Composite createCreatedTimeItem(Composite parent) {
        Composite item = createItemComposite(parent);
        Label label = new Label(item, SWT.NONE);
        label.setText(Messages.FileInfoCreatedTime_label);

        if (createdTimeLabel == null)
            createdTimeLabel = new Label(item, SWT.NONE);
        createdTimeLabel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        createdTimeLabel.setText(getCreatedTime());
        return item;
    }

    private Composite createItemComposite(Composite parent) {
        Composite item = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        item.setLayout(layout);
        item.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        return item;
    }

    private String getSize() {
        IWorkbook workbook = getCurrentWorkbook();

        if (workbook == null)
            return "-1"; //$NON-NLS-1$

        IManifest manifest = workbook.getManifest();

        if (manifest == null)
            return "-1"; //$NON-NLS-1$

        List<IFileEntry> list = manifest.getFileEntries();

        double size = 0.0;
        for (IFileEntry entry : list) {
            size += entry.getSize();
        }

        if (!hasThumbnail(list)) {
            size += 1024 * 2.2;
        }

        size += manifest.getFileEntry("META-INF/manifest.xml").getSize(); //$NON-NLS-1$

        double size_K = (double) Math.round((size / 1024) * 100) / 100;

        if (size_K < 100)
            return size_K + "K"; //$NON-NLS-1$

        return (double) Math.round((size / 1024 / 1024) * 100) / 100 + "M"; //$NON-NLS-1$
    }

    private boolean hasThumbnail(List<IFileEntry> list) {
        for (IFileEntry entry : list) {
            if ("Thumbnails/thumbnail.png".equals(entry.getPath())) //$NON-NLS-1$
                return true;
        }
        return false;
    }

    private String getWordsCount() {
        List<ITopic> allTopics = getAllTopics();
        if (allTopics == null)
            return "-1"; //$NON-NLS-1$

        String allWords = getAllWords(allTopics);

        return String.valueOf(countWords(allWords));
    }

    private String getAllWords(List<ITopic> allTopics) {
        StringBuilder sb = new StringBuilder();
        for (ITopic topic : allTopics) {
            if (topic.getTitleText() != null)
                sb.append(" ").append(topic.getTitleText()); //$NON-NLS-1$
            if (topic.getLabels() != null && !topic.getLabels().isEmpty())
                sb.append(" ").append( //$NON-NLS-1$
                        MindMapUtils.getLabelText(topic.getLabels()));
            if (topic.getNotes() != null && !topic.getNotes().isEmpty()) {
                IPlainNotesContent planContent = (IPlainNotesContent) topic
                        .getNotes().getContent(INotes.PLAIN);
                if (planContent != null)
                    sb.append(" ").append(planContent.getTextContent()); //$NON-NLS-1$
            }
        }
        return sb.toString().replaceAll("/n", " "); //$NON-NLS-1$ //$NON-NLS-2$
    }

    private int countWords(String allWords) {
        char[] cs = allWords.toCharArray();
        int wordsCount = 0;
        boolean wordflag = false;
        for (int i = 0; i < cs.length; i++) {
            if (!isChinese(cs[i]) && cs[i] != ' ') {
                if (wordflag) {
                    continue;
                } else {
                    wordsCount++;
                }
                wordflag = true;
            } else {
                wordflag = false;
                if (isChinese(cs[i]))
                    wordsCount++;
            }
        }
        return wordsCount;
    }

    private boolean isChinese(char c) {
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

    private String getTopicsCount() {
        List<ITopic> allTopics = getAllTopics();
        if (allTopics == null)
            return "-1"; //$NON-NLS-1$

        setLabelRef(allTopics);

        return String.valueOf(this.allTopics.size());
    }

    private String getRevisions() {
        ISheet sheet = getCurrentSheet();
        if (sheet == null)
            return "-1"; //$NON-NLS-1$

        IRevisionManager revisionManager = sheet.getOwnedWorkbook()
                .getRevisionRepository()
                .getRevisionManager(sheet.getId(), IRevision.SHEET);

        if (revisionRegister != null) {
            revisionRegister.unregisterAll();
            revisionRegister = null;
        }
        revisionRegister = new CoreEventRegister(this);
        revisionRegister.setNextSourceFrom(revisionManager);
        revisionRegister.register(Core.RevisionAdd);
        revisionRegister.register(Core.RevisionRemove);

        if (revisionManager.getRevisions() == null
                || revisionManager.getRevisions().isEmpty())
            return "0"; //$NON-NLS-1$

        return String.valueOf(revisionManager.getRevisions().size());
    }

    private String getModifiedTime() {
        IWorkbook workbook = getCurrentWorkbook();
        if (workbook == null)
            return "-1"; //$NON-NLS-1$

        if (modifyTimeRegister != null) {
            modifyTimeRegister.unregisterAll();
            modifyTimeRegister = null;
        }
        modifyTimeRegister = new CoreEventRegister((ICoreEventSource) workbook,
                this);
        modifyTimeRegister.register(Core.ModifyTime);
        modifyTimeRegister.register(Core.WorkbookSave);

        return NumberUtils.formatDate(workbook.getModifiedTime());

    }

    private String getModifiedBy() {
        IWorkbook workbook = getCurrentWorkbook();
        if (workbook == null)
            return System.getProperty("user.name"); //$NON-NLS-1$

        String name = workbook.getModifiedBy();

        if (name == null || "".equals(name)) //$NON-NLS-1$
            name = System.getProperty("user.name"); //$NON-NLS-1$

        return name;
    }

    private String getCreatedTime() {
        IWorkbook workbook = getCurrentWorkbook();
        if (workbook == null)
            return "-1"; //$NON-NLS-1$

        IMeta meta = workbook.getMeta();
        if (meta == null)
            return "-1"; //$NON-NLS-1$

        String time = meta.getValue(IMeta.CREATED_TIME);
        if (time == null)
            return "-1"; //$NON-NLS-1$

        return time;
    }

    public void handleCoreEvent(final CoreEvent event) {
        Control c = getControl();
        if (c == null || c.isDisposed())
            return;

        c.getDisplay().syncExec(new Runnable() {
            public void run() {
                refreshFileInfo(event);
                reflow();
            }
        });
    }

    protected void refreshFileInfo(CoreEvent event) {
        String type = event.getType();
        if (Core.RevisionAdd.equals(type) || Core.RevisionRemove.equals(type))
            refreshRevisions();
        else if (Core.ModifyTime.equals(type)) {
            refreshModifyTime();
            refreshModifyBy();
            refreshEstimateSize();
        } else if (Core.WorkbookSave.equals(type)) {
            refreshEstimateSize();
        } else if (Core.TopicAdd.equals(type)
                || Core.TopicRemove.equals(type)) {
            refreshTopicsCount();
            refreshWordsCount();
        } else if (Core.TitleText.equals(type) || Core.TopicNotes.equals(type)
                || Core.Labels.equals(type))
            refreshWordsCount();
    }

    @Override
    protected void refreshFileInfo() {
        refreshEstimateSize();

        refreshTopicsCount();

        refreshWordsCount();

        refreshRevisions();

        refreshModifyTime();

        refreshModifyBy();

        refreshCreatedTime();

        reflow();
    }

    private void refreshCreatedTime() {
        if (createdTimeLabel != null && !createdTimeLabel.isDisposed())
            createdTimeLabel.setText(getCreatedTime());
    }

    private void refreshModifyTime() {
        if (modifyTimeLabel != null && !modifyTimeLabel.isDisposed())
            modifyTimeLabel.setText(getModifiedTime());
    }

    private void refreshModifyBy() {
        if (modifyByLabel != null && !modifyByLabel.isDisposed())
            modifyByLabel.setText(getModifiedBy());
    }

    private void refreshRevisions() {
        if (revisions != null && !revisions.isDisposed())
            revisions.setText(getRevisions());
    }

    private void refreshWordsCount() {
        if (wordsCountLabel != null && !wordsCountLabel.isDisposed())
            wordsCountLabel.setText(getWordsCount());

    }

    private void refreshTopicsCount() {
        if (topicsCountLabel != null && !topicsCountLabel.isDisposed())
            topicsCountLabel.setText(getTopicsCount());
    }

    private void refreshEstimateSize() {
        if (estimateSizeLabel != null && !estimateSizeLabel.isDisposed())
            estimateSizeLabel.setText(getSize());
    }

    private void setLabelRef(List<ITopic> allTopics) {
        if (this.allTopics == allTopics)
            return;

        if (register != null) {
            register.unregisterAll();
            register = null;
        }
        this.allTopics = allTopics;
        if (allTopics != null) {
            register = new CoreEventRegister(this);
            for (ITopic topic : allTopics) {
                register.setNextSourceFrom(topic);
                register.register(Core.TitleText);
                register.register(Core.TopicNotes);
                register.register(Core.Labels);
                register.register(Core.TopicAdd);
                register.register(Core.TopicRemove);
            }
        }
    }

    @Override
    protected void handleDispose() {
        if (register != null) {
            register.unregisterAll();
            register = null;
        }
        if (modifyTimeRegister != null) {
            modifyTimeRegister.unregisterAll();
            modifyTimeRegister = null;
        }
        if (revisionRegister != null) {
            revisionRegister.unregisterAll();
            revisionRegister = null;
        }
    }

}
