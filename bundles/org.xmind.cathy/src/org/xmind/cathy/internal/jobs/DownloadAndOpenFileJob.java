package org.xmind.cathy.internal.jobs;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.xmind.cathy.internal.WorkbenchMessages;
import org.xmind.core.Core;
import org.xmind.core.IWorkbook;
import org.xmind.core.event.ICoreEventListener;
import org.xmind.core.event.ICoreEventSource2;
import org.xmind.core.io.DirectoryStorage;
import org.xmind.core.io.IStorage;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.imports.freemind.FreeMindImporter;
import org.xmind.ui.internal.imports.mm.MindManagerImporter;
import org.xmind.ui.io.DownloadJob;
import org.xmind.ui.mindmap.LazyPasswordBasedEncryptor;
import org.xmind.ui.mindmap.MindMapUI;

public class DownloadAndOpenFileJob extends Job {

    private IWorkbench workbench;

    private String url;

    private String targetName;

    private File tempFile;

    public DownloadAndOpenFileJob(IWorkbench workbench, String url,
            String targetName) {
        super(WorkbenchMessages.DownloadAndOpenFileJob_jobName);
        Assert.isNotNull(workbench);
        Assert.isNotNull(url);
        this.workbench = workbench;
        this.url = url;
        this.targetName = targetName;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;

        monitor.beginTask(null, 2);

        IStatus downloaded = download(monitor);
        if (!downloaded.isOK())
            return downloaded;

        monitor.worked(1);

        IStatus opened = open(monitor);
        if (!opened.isOK())
            return opened;

        monitor.done();

        return Status.OK_STATUS;
    }

    private IStatus download(IProgressMonitor monitor) {
        if (monitor.isCanceled())
            return Status.CANCEL_STATUS;

        monitor.subTask(NLS.bind(
                WorkbenchMessages.DownloadAndOpenFileJob_Task_Download_with_url,
                url));
        tempFile = createTempPath(url);
        if (tempFile.getParentFile() == null
                || !tempFile.getParentFile().isDirectory()) {
            return new Status(IStatus.ERROR, MindMapUI.PLUGIN_ID,
                    WorkbenchMessages.DownloadAndOpenFileJob_Error_FailedToCreateTempFile);
        }

        DownloadJob downloadJob = new DownloadJob(
                WorkbenchMessages.DownloadAndOpenFileJob_DownloadJob_jobName,
                url, tempFile.getAbsolutePath(), MindMapUI.PLUGIN_ID);
        downloadJob.setUser(true);
        downloadJob.schedule();
        try {
            downloadJob.join();
        } catch (InterruptedException e) {
        }

        if (monitor.isCanceled()) {
            downloadJob.cancel();
            return Status.CANCEL_STATUS;
        }

        IStatus downloaded = downloadJob.getResult();
        if (downloaded == null)
            // This should never happen?
            return new Status(IStatus.ERROR, MindMapUI.PLUGIN_ID,
                    "No result retrieved from download job."); //$NON-NLS-1$
        return downloaded;
    }

    private IStatus open(IProgressMonitor monitor) {
        monitor.subTask(NLS.bind(
                WorkbenchMessages.DownloadAndOpenFileJob_Task_OpenDownloadedFile_with_url,
                url));

        try {
            IStorage tempStorage = createTempStorage();
            final IWorkbook workbook = loadWorkbook(monitor, tempStorage);
            if (workbook != null) {
                return openMindMapEditor(monitor, workbook);
            }
        } catch (Throwable e) {
            return new Status(IStatus.ERROR, MindMapUI.PLUGIN_ID,
                    NLS.bind(
                            WorkbenchMessages.DownloadAndOpenFileJob_Error_FailedToLoadWorkbook_with_url,
                            url),
                    e);
        }
        return Status.CANCEL_STATUS;
    }

    private IWorkbook loadWorkbook(IProgressMonitor monitor,
            IStorage tempStorage) throws Exception {
        String ext = FileUtils.getExtension(tempFile.getAbsolutePath());
        if (MindMapUI.FILE_EXT_XMIND.equalsIgnoreCase(ext)) {
            return loadWorkbookFromXMindFile(monitor, tempStorage);
        } else if (MindMapUI.FILE_EXT_TEMPLATE.equalsIgnoreCase(ext)) {
            return loadWorkbookFromTemplate(monitor, tempStorage);
        } else if (".mmap".equalsIgnoreCase(ext)) { //$NON-NLS-1$
            return loadWorkbookFromMindManagerFile(monitor, tempStorage);
        } else if (".mm".equalsIgnoreCase(ext)) { //$NON-NLS-1$
            return loadWorkbookFromFreeMindFile(monitor, tempStorage);
        }
        return null;
    }

    private IWorkbook loadWorkbookFromXMindFile(IProgressMonitor monitor,
            IStorage tempStorage) throws Exception {
        Core.getWorkbookBuilder()
                .setEntryStreamNormalizer(new LazyPasswordBasedEncryptor());
        return Core.getWorkbookBuilder().loadFromFile(tempFile, tempStorage,
                null);
    }

    private IWorkbook loadWorkbookFromTemplate(IProgressMonitor monitor,
            IStorage tempStorage) throws Exception {
        Core.getWorkbookBuilder()
                .setEntryStreamNormalizer(new LazyPasswordBasedEncryptor());
        return Core.getWorkbookBuilder().loadFromFile(tempFile, tempStorage,
                null);
    }

    private IWorkbook loadWorkbookFromMindManagerFile(IProgressMonitor monitor,
            IStorage tempStorage) throws Exception {
        MindManagerImporter importer = new MindManagerImporter(
                tempFile.getAbsolutePath());
        importer.build();
        return importer.getTargetWorkbook();
    }

    private IWorkbook loadWorkbookFromFreeMindFile(IProgressMonitor monitor,
            IStorage tempStorage) throws Exception {
        FreeMindImporter importer = new FreeMindImporter(
                tempFile.getAbsolutePath());
        importer.build();
        return importer.getTargetWorkbook();
    }

    private IStatus openMindMapEditor(final IProgressMonitor monitor,
            final IWorkbook workbook) {
        final IStatus[] result = new IStatus[1];
        result[0] = null;
        Display display = workbench.getDisplay();
        if (display == null || display.isDisposed()) {
            monitor.setCanceled(true);
            return Status.CANCEL_STATUS;
        }
        display.syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
                if (window == null)
                    return;

                IWorkbenchPage page = window.getActivePage();
                if (page == null)
                    return;

                String name = targetName == null ? getFileName(url)
                        : targetName;
                IEditorInput input = MindMapUI.getEditorInputFactory()
                        .createEditorInputForPreLoadedWorkbook(workbook, name);
                try {
                    page.openEditor(input, MindMapUI.MINDMAP_EDITOR_ID, true);
                } catch (PartInitException e) {
                    result[0] = e.getStatus();
                    return;
                }

                if (workbook instanceof ICoreEventSource2) {
                    ((ICoreEventSource2) workbook)
                            .registerOnceCoreEventListener(
                                    Core.WorkbookPreSaveOnce,
                                    ICoreEventListener.NULL);
                }

                result[0] = Status.OK_STATUS;
            }
        });
        if (result[0] == null)
            return Status.CANCEL_STATUS;
        return result[0];
    }

    @Override
    protected void canceling() {
        super.canceling();
        Thread thread = getThread();
        if (thread != null) {
            thread.interrupt();
        }
    }

    private static File createTempPath(String url) {
        String fileName = getFileName(url);
        String ext = FileUtils.getExtension(fileName);
        String prefix = fileName.substring(0, fileName.length() - ext.length());
        return Core.getWorkspace().createTempFile("download", prefix + "_", //$NON-NLS-1$//$NON-NLS-2$
                ext);
    }

    private static IStorage createTempStorage() {
        File tempDir = Core.getWorkspace()
                .createTempFile("openFromDownloadedFile", "", ".temp"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        tempDir.mkdirs();
        return new DirectoryStorage(tempDir);
    }

    private static String getFileName(String url) {
        String path;
        try {
            path = new URI(url).getPath();
        } catch (URISyntaxException e) {
            int j = url.lastIndexOf('?');
            if (j < 0)
                path = url;
            else
                path = url.substring(0, j);
        }
        int i = path.lastIndexOf('/');
        if (i < 0)
            return path;
        return path.substring(i + 1);
    }

}
