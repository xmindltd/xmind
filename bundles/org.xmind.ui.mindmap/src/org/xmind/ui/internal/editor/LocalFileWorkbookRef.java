package org.xmind.ui.internal.editor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.dialogs.ErrorSupportProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.AbstractHyperlink;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.statushandlers.AbstractStatusAreaProvider;
import org.eclipse.ui.statushandlers.StatusAdapter;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IDeserializer;
import org.xmind.core.IFileEntry;
import org.xmind.core.IMeta;
import org.xmind.core.IRevision;
import org.xmind.core.IRevisionManager;
import org.xmind.core.IRevisionRepository;
import org.xmind.core.ISerializer;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.io.DirectoryInputSource;
import org.xmind.core.io.DirectoryOutputTarget;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.CloneHandler;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.ProgressReporter;
import org.xmind.ui.blackbox.BlackBox;
import org.xmind.ui.blackbox.IBlackBoxMap;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.BlackBoxDialog;
import org.xmind.ui.internal.handlers.OpenBlackBoxDialogHandler;
import org.xmind.ui.internal.protocols.FilePathParser;
import org.xmind.ui.internal.utils.CommandUtils;
import org.xmind.ui.internal.zen.ZenConstants;
import org.xmind.ui.internal.zen.ZenDeserializer;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.util.Logger;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class LocalFileWorkbookRef extends AbstractWorkbookRef {

    private static boolean DEBUG_BACKUP = MindMapUIPlugin
            .isDebugging(MindMapUIPlugin.OPTION_LOCAL_FILE_BACKUP);

    private static class PreParser {

        private IInputSource inputSource;

        private InputStream inputStream;

        private IStorage storage;

        public PreParser() {
        }

        public void setInputSource(IInputSource source) {
            if (source == null)
                throw new IllegalArgumentException("input source is null"); //$NON-NLS-1$
            this.inputSource = source;
            this.inputStream = null;
        }

        public void setInputStream(InputStream stream) {
            if (stream == null)
                throw new IllegalArgumentException("input stream is null"); //$NON-NLS-1$
            this.inputStream = stream;
            this.inputSource = null;
        }

        public void setWorkbookStorage(IStorage storage) {
            if (storage == null)
                throw new IllegalArgumentException("storage is null"); //$NON-NLS-1$
            this.storage = storage;
        }

        public boolean isJsonFormat() {
            try {
                if (inputStream != null) {
                    ZipInputStream zin = new ZipInputStream(inputStream);
                    try {
                        FileUtils.extractZipFile(zin,
                                storage.getOutputTarget());
                    } finally {
                        zin.close();
                    }
                } else if (inputSource != null) {
                    FileUtils.transfer(inputSource, storage.getOutputTarget());
                }
            } catch (IOException e) {
                return false;
            }

            if (storage != null) {
                IInputSource source = storage.getInputSource();
                if (source != null && source.hasEntry(ZenConstants.CONTENT_JSON)
                        && source.isEntryAvailable(ZenConstants.CONTENT_JSON)) {
                    return true;
                }
            }
            return false;
        }
    }

    private static class LocalFileBackup {

        private final File file;

        private final File tempFile;

        /**
         * 
         */
        public LocalFileBackup(File file) {
            this.file = file;
            this.tempFile = new File(Core.getWorkspace().getTempFile(
                    "saving/" + UUID.randomUUID() + MindMapUI.FILE_EXT_XMIND)); //$NON-NLS-1$
        }

        public void makeBackup() {
            if (!file.exists() || !file.canRead()) {
                tempFile.delete();
                return;
            }

            if (DEBUG_BACKUP) {
                System.out.println("Making local file backup for: " //$NON-NLS-1$
                        + file.getAbsolutePath());
                System.out.println("    to: " + tempFile.getAbsolutePath()); //$NON-NLS-1$
            }
            try {
                FileUtils.transfer(file, tempFile);
            } catch (IOException e) {
                MindMapUIPlugin.getDefault().getLog()
                        .log(new Status(IStatus.WARNING,
                                MindMapUIPlugin.PLUGIN_ID,
                                "Failed to make backup of local file: " //$NON-NLS-1$
                                        + file.getAbsolutePath(),
                                e));
            }
        }

        public void restoreBackup() {
            if (!tempFile.exists() || !tempFile.canRead()) {
                return;
            }

            if (DEBUG_BACKUP) {
                System.out.println("Restoring local file backup for: " //$NON-NLS-1$
                        + file.getAbsolutePath());
                System.out.println("    from: " + tempFile.getAbsolutePath()); //$NON-NLS-1$
            }
            try {
                FileUtils.transfer(tempFile, file);
            } catch (IOException e) {
                MindMapUIPlugin.getDefault().getLog()
                        .log(new Status(IStatus.WARNING,
                                MindMapUIPlugin.PLUGIN_ID,
                                "Failed to restore backup of local file: " //$NON-NLS-1$
                                        + file.getAbsolutePath(),
                                e));
            }
        }

        public void deleteBackup() {
            if (DEBUG_BACKUP) {
                System.out.println("Deleting local file backup for: " //$NON-NLS-1$
                        + file.getAbsolutePath());
                System.out.println("    at: " + tempFile.getAbsolutePath()); //$NON-NLS-1$
            }
            tempFile.delete();
        }

    }

    private class LocalFileErrorSupportProvider
            extends AbstractStatusAreaProvider {

        /*
         * (non-Javadoc)
         * @see org.eclipse.ui.statushandlers.AbstractStatusAreaProvider#
         * createSupportArea(org.eclipse.swt.widgets.Composite,
         * org.eclipse.ui.statushandlers.StatusAdapter)
         */
        @Override
        public Control createSupportArea(Composite parent,
                StatusAdapter statusAdapter) {
            IBlackBoxMap[] blackBoxMaps = BlackBox.getMaps();
            if (!MindMapUIPlugin.getDefault().getPreferenceStore()
                    .getBoolean(PrefConstants.AUTO_BACKUP_ENABLE)
                    || (blackBoxMaps == null || blackBoxMaps.length == 0))
                return null;
            Composite hyperParent = new Composite(parent, SWT.NONE);
            GridLayout layout = new GridLayout(2, false);
            layout.marginWidth = 0;
            layout.marginHeight = 0;
            hyperParent.setLayout(layout);
            hyperParent.setBackground(parent.getBackground());

            Label preLink = new Label(hyperParent, SWT.NONE);
            preLink.setText(
                    MindMapMessages.LoadWorkbookJob_errorDialog_Pre_message);
            preLink.setLayoutData(
                    new GridData(SWT.FILL, SWT.CENTER, false, false));
            preLink.setBackground(parent.getBackground());

            Hyperlink hyperlink = new Hyperlink(hyperParent, SWT.NONE);
            hyperlink.setBackground(parent.getBackground());
            hyperlink.setUnderlined(true);
            hyperlink.setText(
                    MindMapMessages.LoadWorkbookJob_errorDialog_GoToBackup_message);
            hyperlink.setLayoutData(
                    new GridData(SWT.FILL, SWT.CENTER, true, false));
            hyperlink.addHyperlinkListener(new HyperlinkAdapter() {
                @Override
                public void linkActivated(HyperlinkEvent e) {
                    showBlackBoxView();
                }
            });
            hyperlink.setFont(
                    FontUtils.getBoldRelative(JFaceResources.DEFAULT_FONT, 0));
            /* Prevent focus box from being painted: */
            try {
                Field fPaintFocus = AbstractHyperlink.class
                        .getDeclaredField("paintFocus"); //$NON-NLS-1$
                fPaintFocus.setAccessible(true);
                fPaintFocus.set(hyperlink, false);
            } catch (Throwable e) {
                // ignore
            }

            return hyperParent;
        }

        private void showBlackBoxView() {
            final File damagedFile = getFile();
            if (PlatformUI.isWorkbenchRunning()) {
                final IWorkbenchWindow window = PlatformUI.getWorkbench()
                        .getActiveWorkbenchWindow();
                if (window != null) {
                    final IWorkbenchPage page = window.getActivePage();
                    if (page != null) {
                        SafeRunner.run(new SafeRunnable() {
                            public void run() throws Exception {
                                CommandUtils.executeCommand(
                                        "org.xmind.ui.dialog.openBlackBoxDialog", //$NON-NLS-1$
                                        window);

                                //set damaged file.
                                Object data = Display.getCurrent()
                                        .getActiveShell().getData(
                                                OpenBlackBoxDialogHandler.BLACK_BOX_DIALOG_DATA_KEY);
                                if (data instanceof BlackBoxDialog) {
                                    BlackBoxDialog dialog = (BlackBoxDialog) data;
                                    dialog.setDamagedFile(damagedFile);
                                }
                            }
                        });
                    }
                }
            }
        }

    }

    private final LocalFileBackup backup;

    private long timestamp;

    private final ErrorSupportProvider errorSupportProvider;

    protected LocalFileWorkbookRef(URI uri, IMemento memento) {
        super(uri, memento);
        Assert.isNotNull(uri);
        Assert.isLegal(FilePathParser.URI_SCHEME.equals(uri.getScheme()),
                "Invalid file URI: " + uri.toString()); //$NON-NLS-1$
        this.backup = new LocalFileBackup(new File(uri));
        this.errorSupportProvider = new LocalFileErrorSupportProvider();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.editor.AbstractWorkbookRef#getAdapter(java.lang.
     * Class)
     */
    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (ErrorSupportProvider.class.equals(adapter)) {
            return adapter.cast(errorSupportProvider);
        }
        return super.getAdapter(adapter);
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.ui.internal.editor.AbstractWorkbookRef#getSaveWizardId()
     */
    @Override
    public String getSaveWizardId() {
        return LocalFileSaveWizard.ID;
    }

    private File getFile() {
        return new File(getURI());
    }

    @Override
    public String getName() {
        String name = getFile().getName();
        if (name != null) {
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex < 0)
                return name;
            return name.substring(0, dotIndex);
        }
        return super.getName();
    }

    @Override
    public String getDescription() {
        return getFile().getAbsolutePath();
    }

    @Override
    public int hashCode() {
        return getFile().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof LocalFileWorkbookRef))
            return false;
        LocalFileWorkbookRef that = (LocalFileWorkbookRef) obj;
        File thisFile = this.getFile();
        File thatFile = that.getFile();
        return thisFile == thatFile
                || (thisFile != null && thisFile.equals(thatFile));
    }

    @Override
    public String toString() {
        return getFile().toString();
    }

    @Override
    public boolean canSave() {
        return true;
    }

    @Override
    public boolean canImportFrom(IWorkbookRef source) {
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.xmind.gef.ui.editor.Editable#getModificationTime()
     */
    @Override
    public long getModificationTime() {
        return getFile().lastModified();
    }

    @Override
    protected IWorkbook doLoadWorkbookFromURI(IProgressMonitor monitor, URI uri)
            throws InterruptedException, InvocationTargetException {
        File file = new File(uri);
        IStorage storage = getTempStorage();
        if (isJsonFormat(file, storage)) {
            return doLoadWorkbookFromJson(monitor, storage);
        } else {
            return doLoadWorkbookFromXml(monitor, storage);
        }
    }

    private boolean isJsonFormat(File file, IStorage storage)
            throws InvocationTargetException {
        PreParser parser = new PreParser();
        parser.setWorkbookStorage(storage);
        InputStream stream = null;
        try {
            try {
                if (file.isDirectory()) {
                    parser.setInputSource(new DirectoryInputSource(file));
                } else {
                    stream = new FileInputStream(file);
                    parser.setInputStream(stream);
                }
                return parser.isJsonFormat();

            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        }
    }

    private IWorkbook doLoadWorkbookFromJson(IProgressMonitor monitor,
            IStorage storage)
            throws InvocationTargetException, InterruptedException {
        try {
            IDeserializer deserializer = new ZenDeserializer(storage);
            deserializer.setEntryStreamNormalizer(getEncryptionHandler());
            deserializer.setWorkbookStorageAsInputSource();
            ProgressReporter reporter = new ProgressReporter(monitor);
            deserializer.deserializeManifest(reporter);
            String passwordHint = deserializer.getManifest().getPasswordHint();
            getEncryptable().setPasswordHint(passwordHint);
            deserializer.deserialize(reporter);
            return deserializer.getWorkbook();
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } catch (CoreException e) {
            if (e.getType() == Core.ERROR_CANCELLATION)
                throw new InterruptedException();
            if (e.getType() == Core.ERROR_WRONG_PASSWORD) {
                if (getEncryptable() != null) {
                    getEncryptable().reset();
                }
            }
            throw new InvocationTargetException(e);
        }
    }

    private IWorkbook doLoadWorkbookFromXml(IProgressMonitor monitor,
            IStorage storage)
            throws InvocationTargetException, InterruptedException {
        try {
            IDeserializer deserializer = Core.getWorkbookBuilder()
                    .newDeserializer();
            deserializer.setEntryStreamNormalizer(getEncryptionHandler());
            deserializer.setWorkbookStorage(storage);
            deserializer.setWorkbookStorageAsInputSource();
            ProgressReporter reporter = new ProgressReporter(monitor);
            deserializer.deserializeManifest(reporter);
            String passwordHint = deserializer.getManifest().getPasswordHint();
            getEncryptable().setPasswordHint(passwordHint);
            deserializer.deserialize(reporter);
            return deserializer.getWorkbook();
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } catch (CoreException e) {
            if (e.getType() == Core.ERROR_CANCELLATION)
                throw new InterruptedException();
            if (e.getType() == Core.ERROR_WRONG_PASSWORD) {
                if (getEncryptable() != null) {
                    getEncryptable().reset();
                }
            }
            throw new InvocationTargetException(e);
        }
    }

    @Override
    protected void doSaveWorkbookToURI(IProgressMonitor monitor,
            IWorkbook workbook, URI uri)
            throws InterruptedException, InvocationTargetException {
        doSaveWorkbookToURIFromSource(monitor, workbook, uri, null, null);
    }

    protected void doSaveWorkbookToURIFromSource(IProgressMonitor monitor,
            IWorkbook workbook, URI uri, IWorkbookRef source,
            IWorkbook sourceWorkbook)
            throws InterruptedException, InvocationTargetException {
        appendLog("[save] doSaveWorkbookToURIFromSource start..."); //$NON-NLS-1$

        final Set<String> encryptionIgnoredEntries = new HashSet<String>();

        boolean previewSaved = false;
        if (source != null) {
            try {
                InputStream previewData = source.getPreviewImageData(
                        sourceWorkbook.getPrimarySheet().getId(), null);
                if (previewData != null) {
                    try {
                        IFileEntry previewEntry = workbook.getManifest()
                                .createFileEntry(MindMapImageExporter
                                        .toThumbnailArchivePath(
                                                ImageFormat.PNG));
                        previewEntry.decreaseReference();
                        previewEntry.increaseReference();

                        OutputStream output = previewEntry.openOutputStream();
                        try {
                            FileUtils.transfer(previewData, output, false);
                        } finally {
                            output.close();
                        }
                        encryptionIgnoredEntries.add(previewEntry.getPath());
                    } finally {
                        previewData.close();
                    }
                    previewSaved = true;
                }
            } catch (IOException e) {
                Logger.log(e, "Failed to import preview image"); //$NON-NLS-1$
                appendLog("[save] failed to import preview image..."); //$NON-NLS-1$
            }
        }

        if (!previewSaved) {
            try {
                savePreviewImage(workbook, encryptionIgnoredEntries);
            } catch (InvocationTargetException e) {
                Logger.log(e, "Failed to save preview image"); //$NON-NLS-1$
                appendLog("[save] failed to save preview image..."); //$NON-NLS-1$
            }
        }

        //save revisions, and then trim them.
        try {
            saveRevisions(workbook);
        } catch (CoreException e) {
            Logger.log(e, "Failed to save revisions"); //$NON-NLS-1$
            appendLog("[save] failed to save revisions..."); //$NON-NLS-1$
        }

        appendLog("[save] new file..."); //$NON-NLS-1$
        File file = new File(uri);

        appendLog("[save] serialize start..."); //$NON-NLS-1$
        ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
        serializer.setWorkbook(workbook);
        serializer.setEntryStreamNormalizer(getEncryptionHandler());
        serializer.setEncryptionIgnoredEntries(encryptionIgnoredEntries
                .toArray(new String[encryptionIgnoredEntries.size()]));

        /// set password hint to manifest
        String passwordHint = getEncryptable().getPasswordHint();
        if (passwordHint == null && source != null) {
            IEncryptable encryptable = source.getAdapter(IEncryptable.class);
            if (encryptable != null) {
                passwordHint = encryptable.getPasswordHint();
            }
        }
        if (passwordHint != null)
            workbook.getManifest().setPasswordHint(passwordHint);

        OutputStream stream = null;
        boolean deleteTarget = false;
        long oldLastModified = file.lastModified();
        try {
            if (file.isDirectory()) {
                backup.deleteBackup();
                appendLog("[save] setOutputTarget..."); //$NON-NLS-1$
                serializer.setOutputTarget(new DirectoryOutputTarget(file));
            } else {
                backup.makeBackup();
                appendLog("[save] new FileOutputStream..."); //$NON-NLS-1$
                stream = new FileOutputStream(file);
                appendLog("[save] setOutputStream..."); //$NON-NLS-1$
                serializer.setOutputStream(stream);
            }
            serializer.serialize(new ProgressReporter(monitor));
            appendLog("[save] serialize success..."); //$NON-NLS-1$

            backup.deleteBackup();

            appendLog("[save] doSaveWorkbookToURIFromSource over..."); //$NON-NLS-1$

        } catch (FileNotFoundException e) {
            appendLog("[save] FileNotFoundException..."); //$NON-NLS-1$
            backup.restoreBackup();
            deleteTarget = true;
            promptDialog(MindMapMessages.LocalFileWorkbookRef_saveFailed_title,
                    MindMapMessages.LocalFileWorkbookRef_saveFailed_description);
            throw new InvocationTargetException(e);

        } catch (SecurityException e) {
            appendLog("[save] SecurityException..."); //$NON-NLS-1$
            backup.restoreBackup();
            deleteTarget = true;
            promptDialog(MindMapMessages.LocalFileWorkbookRef_saveFailed_title,
                    MindMapMessages.LocalFileWorkbookRef_saveFailed_description);
            throw new InvocationTargetException(e);

        } catch (IOException e) {
            appendLog("[save] IOException..."); //$NON-NLS-1$
            backup.restoreBackup();
            deleteTarget = true;
            promptDialog(MindMapMessages.LocalFileWorkbookRef_saveFailed_title,
                    MindMapMessages.LocalFileWorkbookRef_saveFailed_description2);
            throw new InvocationTargetException(e);

        } catch (CoreException e) {
            appendLog("[save] CoreException..."); //$NON-NLS-1$
            backup.restoreBackup();
            deleteTarget = true;

            if (e.getType() == Core.ERROR_CANCELLATION) {
                Logger.log(e, "Canceled to save file"); //$NON-NLS-1$
                throw new InterruptedException();
            }

            if (e.getType() == Core.ERROR_WRONG_PASSWORD) {
                if (getEncryptable() != null) {
                    getEncryptable().reset();
                }
            } else {
                promptDialog(
                        MindMapMessages.LocalFileWorkbookRef_saveFailed_title,
                        MindMapMessages.LocalFileWorkbookRef_saveFailed_description2);
            }
            throw new InvocationTargetException(e);

        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    Logger.log(e, "Failed to close stream after save file"); //$NON-NLS-1$
                }
            }
            if (deleteTarget) {
                deleteTarget = false;
                if (source == null) {
                    file.setLastModified(oldLastModified);
                } else {
                    file.delete();
                }
            }
        }
    }

    /**
     * @param workbook
     * @param encryptionIgnoredEntries
     * @throws InvocationTargetException
     */
    private void savePreviewImage(IWorkbook workbook,
            final Set<String> encryptionIgnoredEntries)
            throws InvocationTargetException {
        IMindMapPreviewGenerator previewGenerator = findPreviewGenerator();
        if (previewGenerator != null) {
            ImageFormat previewFormat = ImageFormat.PNG;
            IFileEntry entry = workbook.getManifest().createFileEntry(
                    MindMapImageExporter.toThumbnailArchivePath(previewFormat),
                    previewFormat.getMediaType());
            entry.decreaseReference();
            entry.increaseReference();

            final Properties previewProperties;
            try {
                OutputStream previewOutput = entry.openOutputStream();
                try {
                    previewProperties = previewGenerator.generateMindMapPreview(
                            this, workbook.getPrimarySheet(), previewOutput,
                            null);
                } finally {
                    previewOutput.close();
                }
            } catch (IOException e) {
                throw new InvocationTargetException(e);
            }
            workbook.getMeta().setValue(IMeta.ORIGIN_X, previewProperties
                    .getProperty(IMindMapPreviewGenerator.PREVIEW_ORIGIN_X));
            workbook.getMeta().setValue(IMeta.ORIGIN_Y, previewProperties
                    .getProperty(IMindMapPreviewGenerator.PREVIEW_ORIGIN_Y));
            workbook.getMeta().setValue(IMeta.BACKGROUND_COLOR,
                    previewProperties.getProperty(
                            IMindMapPreviewGenerator.PREVIEW_BACKGROUND));

            encryptionIgnoredEntries.add(entry.getPath());
        }
    }

    private void saveRevisions(IWorkbook workbook) throws CoreException {
        if (!isContentDirty(workbook) || !shouldSaveNewRevisions(workbook))
            return;

        IRevisionRepository repo = workbook.getRevisionRepository();
        for (ISheet sheet : workbook.getSheets()) {
            IRevisionManager manager = repo.getRevisionManager(sheet.getId(),
                    IRevision.SHEET);
            IRevision latestRevision = manager.getLatestRevision();
            if (latestRevision == null || sheet.getModifiedTime() == 0 || sheet
                    .getModifiedTime() > latestRevision.getTimestamp()) {
                try {
                    manager.addRevision(sheet);
                } catch (Throwable e) {
                    throw new CoreException(Core.ERROR_INVALID_ARGUMENT,
                            "Invalid content for revisions."); //$NON-NLS-1$
                }
            }
        }
    }

    private boolean isContentDirty(IWorkbook workbook) {
        if (workbook == null)
            return false;
        if (getCommandStack() != null && getCommandStack().isDirty())
            return true;
        return workbook instanceof ICoreEventSource2
                && ((ICoreEventSource2) workbook)
                        .hasOnceListeners(Core.WorkbookPreSaveOnce);
    }

    private boolean shouldSaveNewRevisions(IWorkbook workbook) {
        String value = workbook.getMeta()
                .getValue(IMeta.CONFIG_AUTO_REVISION_GENERATION);
        return value == null || IMeta.V_YES.equalsIgnoreCase(value);
    }

    private void promptDialog(final String title, final String message) {
        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                MessageDialog.openWarning(Display.getDefault().getActiveShell(),
                        title, message);
            }
        });
    }

    @Override
    protected void doImportFrom(IProgressMonitor monitor, IWorkbookRef source)
            throws InterruptedException, InvocationTargetException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);

        IWorkbook sourceWorkbook = source.getWorkbook();
        Assert.isTrue(sourceWorkbook != null);
        URI targetURI = getURI();
        Assert.isTrue(targetURI != null);

        IStorage storage = getTempStorage();

        IEncryptable sourceEncryptable = source.getAdapter(IEncryptable.class);
        if (sourceEncryptable != null && sourceEncryptable.hasPassword()
                && sourceEncryptable instanceof WorkbookRefEncryptable) {
//            getEncryptable()
//                    .setEncryptor(((WorkbookRefEncryptable) sourceEncryptable)
//                            .getEncryptor());
            getEncryptable().setPassword(sourceEncryptable.getPassword());
            getEncryptable()
                    .setPasswordHint(sourceEncryptable.getPasswordHint());
        }

        doClearTempStorageBeforeImport(subMonitor.newChild(5), storage);

        try {
            IWorkbook workbook = doCloneWorkbookForImport(
                    subMonitor.newChild(40), sourceWorkbook, source.getURI(),
                    storage);

            doSaveWorkbookToURIFromSource(subMonitor.newChild(50), workbook,
                    targetURI, source, sourceWorkbook);

        } finally {
            subMonitor.setWorkRemaining(5);
            doClearTempStorageAfterImport(subMonitor.newChild(5), storage);
        }

    }

    protected void doClearTempStorageBeforeImport(IProgressMonitor monitor,
            IStorage storage)
            throws InterruptedException, InvocationTargetException {
        storage.clear();
    }

    protected void doClearTempStorageAfterImport(IProgressMonitor monitor,
            IStorage storage) {
        storage.clear();
    }

    protected IWorkbook doCloneWorkbookForImport(IProgressMonitor monitor,
            IWorkbook sourceWorkbook, URI sourceURI, IStorage storage)
            throws InterruptedException, InvocationTargetException {
        try {
            IWorkbook workbook = Core.getWorkbookBuilder()
                    .createWorkbook(storage);
            ISerializer serializer = Core.getWorkbookBuilder().newSerializer();
            serializer.setWorkbook(workbook);
            serializer.setWorkbookStorageAsOutputTarget();
            serializer.setEntryStreamNormalizer(getEncryptionHandler());
            serializer.serialize(null);

            new CloneHandler().withWorkbooks(sourceWorkbook, workbook)
                    .copyWorkbookContents();

            String oldFilePath;
            if (sourceURI != null && FilePathParser.URI_SCHEME
                    .equals(sourceURI.getScheme())) {
                oldFilePath = new File(sourceURI).getAbsolutePath();
            } else {
                oldFilePath = null;
            }
            updateAllRelativeFileHyperlinks(workbook, oldFilePath,
                    getFile().getAbsolutePath());

            return workbook;
        } catch (IOException e) {
            throw new InvocationTargetException(e);
        } catch (CoreException e) {
            if (e.getType() == Core.ERROR_CANCELLATION)
                throw new InterruptedException();
            throw new InvocationTargetException(e);
        }
    }

    private void updateAllRelativeFileHyperlinks(IWorkbook workbook,
            String oldFilePath, String newFilePath) {
        String oldBase;
        if (oldFilePath == null) {
            oldBase = FilePathParser.ABSTRACT_FILE_BASE;
        } else {
            oldBase = new File(oldFilePath).getParent();
        }

        String newBase = new File(newFilePath).getParent();
        if (oldBase.equals(newBase))
            return;

        List<ISheet> sheets = workbook.getSheets();
        for (ISheet sheet : sheets) {
            updateRelativeFileHyperlinks(sheet.getRootTopic(), oldBase,
                    newBase);
        }
    }

    private void updateRelativeFileHyperlinks(ITopic topic, String oldBase,
            String newBase) {
        String hyperlink = topic.getHyperlink();
        if (FilePathParser.isFileURI(hyperlink)) {
            String path = FilePathParser.toPath(hyperlink);
            if (FilePathParser.isPathRelative(path)) {
                String absolutePath = FilePathParser.toAbsolutePath(oldBase,
                        path);
                hyperlink = FilePathParser.toRelativePath(newBase,
                        absolutePath);
                topic.setHyperlink(FilePathParser.toURI(hyperlink, true));
            }
        }
        List<ITopic> topics = topic.getAllChildren();
        if (topics != null) {
            for (ITopic temptopic : topics) {
                updateRelativeFileHyperlinks(temptopic, oldBase, newBase);
            }
        }
    }

    @Override
    public boolean activateNotifier() {
        File file = getFile();
        if (!file.exists()) {
            fileRemoved(MindMapMessages.LocalFileWorkbookRef_removeDialog_title,
                    MindMapMessages.LocalFileWorkbookRef_removeDialog_message,
                    new String[] {
                            MindMapMessages.LocalFileWorkbookRef_removeDialog_saveAs_button,
                            MindMapMessages.LocalFileWorkbookRef_removeDialog_delete_button },
                    false);
            return true;
        } else if (file.lastModified() != timestamp) {
            fileChanged(MindMapMessages.LocalFileWorkbookRef_changeDialog_title,
                    MindMapMessages.LocalFileWorkbookRef_changeDialog_message,
                    new String[] {
                            MindMapMessages.LocalFileWorkbookRef_changeDialog_update_button,
                            MindMapMessages.LocalFileWorkbookRef_changeDialog_cancel_button });
            return true;
        }
        return false;
    }

    @Override
    public void open(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        timestamp = getFile().lastModified();
        super.open(monitor);
    }

    @Override
    public void save(IProgressMonitor monitor)
            throws InterruptedException, InvocationTargetException {
        super.save(monitor);
        timestamp = getFile().lastModified();
    }

    @Override
    public boolean exists() {
        return getFile().exists();
    }

    protected void log(Throwable e, String message) {
        Logger.log(e, message);
    }

}
