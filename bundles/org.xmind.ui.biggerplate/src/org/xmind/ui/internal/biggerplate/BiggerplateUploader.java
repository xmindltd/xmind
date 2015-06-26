package org.xmind.ui.internal.biggerplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.xmind.core.Core;
import org.xmind.core.IMeta;
import org.xmind.core.IWorkbook;
import org.xmind.gef.GEF;
import org.xmind.ui.biggerplate.BiggerplateMessages;
import org.xmind.ui.biggerplate.BiggerplatePlugin;
import org.xmind.ui.dialogs.Notification;
import org.xmind.ui.internal.biggerplate.dialog.BiggerplateUploaderDialog;
import org.xmind.ui.internal.biggerplate.jobs.AuthoriseJob;
import org.xmind.ui.internal.biggerplate.jobs.BiggerplateUploadJob;
import org.xmind.ui.internal.biggerplate.jobs.CancelableJob;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.MindMap;
import org.xmind.ui.mindmap.MindMapExtractor;
import org.xmind.ui.mindmap.MindMapImageExporter;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.util.TextFormatter;

public class BiggerplateUploader extends JobChangeAdapter {

    private static BiggerplateUploader uploader = new BiggerplateUploader();

    private Shell parentShell;

    private IMindMapViewer sourceViewer;

    private IWorkbook workbook;

    private MindMapExtractor extractor;

    private Info info;

    private Point origin;

    private Image previewImage;

    private File file;

    private BiggerplateUploaderDialog dialog;

    private boolean isRunning = false;

    private BiggerplateUploader() {
    }

//    public BiggerplateUploader(Shell parentShell, IMindMapViewer sourceViewer) {
//        this.parentShell = parentShell;
//        this.sourceViewer = sourceViewer;
//    }

    public static BiggerplateUploader getUploader(Shell parentShell,
            IMindMapViewer sourceViewer) {
        uploader.parentShell = parentShell;
        uploader.sourceViewer = sourceViewer;

        return uploader;
    }

    public void upload() {
        if (isRunning) {
            try {
                PlatformUI.getWorkbench().getActiveWorkbenchWindow()
                        .getActivePage()
                        .showView("org.eclipse.ui.views.ProgressView"); //$NON-NLS-1$
            } catch (PartInitException e) {
                e.printStackTrace();
            }
            return;
        }
        isRunning = true;
        info = new Info();

        CancelableJob authoriseJob = new AuthoriseJob(info);
        authoriseJob.addJobChangeListener(new JobChangeAdapter() {

            @Override
            public void done(IJobChangeEvent event) {
                final IStatus result = event.getResult();
                if (result == Status.OK_STATUS) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            doUpload();
                        }
                    });
                } else if (result.matches(IStatus.ERROR | IStatus.WARNING)) {
                    Display.getDefault().asyncExec(new Runnable() {
                        public void run() {
                            promptError(result);
                        }
                    });
                    isRunning = false;
                } else {
                    isRunning = false;
                }
            }

        });
        authoriseJob.schedule();
    }

    private void doUpload() {
        boolean prepared = false;
        try {
            prepared = prepare();
        } catch (OutOfMemoryError e) {
            StatusManager
                    .getManager()
                    .handle(new Status(
                            IStatus.ERROR,
                            BiggerplatePlugin.PLUGIN_ID,
                            BiggerplateMessages.ErrorDialog_OutOfMemory_message,
                            e), StatusManager.SHOW);
        } catch (Throwable e) {
            StatusManager
                    .getManager()
                    .handle(new Status(
                            IStatus.ERROR,
                            BiggerplatePlugin.PLUGIN_ID,
                            BiggerplateMessages.ErrorDialog_UnexpectedError_message,
                            e), StatusManager.SHOW);
        }

        if (!prepared) {
            cancel();
            return;
        }

        BiggerplateUploadJob uploadJob = new BiggerplateUploadJob(info);
        uploadJob.setSystem(false);
        uploadJob.setUser(true);
        uploadJob.addJobChangeListener(this);
        uploadJob.schedule();
    }

    private boolean prepare() throws Exception {
        info.setBoolean(Info.MULTISHEETS, multiSheets(sourceViewer));
        extractor = new MindMapExtractor(sourceViewer);
        workbook = extractor.extract();
        generatePreview(parentShell.getDisplay());

        if (previewImage == null || origin == null) {
            throw new RuntimeException(
                    BiggerplateMessages.failedToGenerateThumbnail);
        }

        info.setProperty(Info.TITLE, getDefaultMapTitle());
        info.setProperty(Info.FULL_IMAGE, previewImage);
        info.setInt(IMeta.ORIGIN_X, origin.x);
        info.setInt(IMeta.ORIGIN_Y, origin.y);
        info.setProperty(IMeta.BACKGROUND_COLOR, getBackgroundColor());

        dialog = createUploadDialog();
        int ret = dialog.open();
        if (ret != BiggerplateUploaderDialog.OK)
            return false;
        dialog = null;

        int x = info.getInt(Info.X, 0);
        int y = info.getInt(Info.Y, 0);
        double scale = info.getDouble(Info.SCALE, 1.0d);
        scale /= 2;

        IMeta meta = workbook.getMeta();
        meta.setValue(Info.DESCRIPTION, info.getString(Info.DESCRIPTION));
        meta.setValue(Info.X, String.valueOf(x));
        meta.setValue(Info.Y, String.valueOf(y));
        meta.setValue(Info.SCALE, String.valueOf(scale));
        meta.setValue(IMeta.ORIGIN_X, String.valueOf(origin.x));
        meta.setValue(IMeta.ORIGIN_Y, String.valueOf(origin.y));
        meta.setValue(IMeta.BACKGROUND_COLOR,
                info.getString(IMeta.BACKGROUND_COLOR));

        if (file == null) {
            String tempFile = Core.getWorkspace()
                    .getTempFile(
                            "biggerplate/" //$NON-NLS-1$
                                    + Core.getIdFactory().createId()
                                    + MindMapUI.FILE_EXT_XMIND);
            file = new File(tempFile);
        }

        String path = file.getAbsolutePath();
        workbook.saveTemp();
        workbook.save(path);
        BiggerplateUploader.validateUploadFile(path);

        if (!file.exists() || !file.canRead())
            throw new FileNotFoundException(
                    BiggerplateMessages.FailedToGenerateUploadFile);

        info.setProperty(Info.FILE, file);
        info.setProperty(Info.WORKBOOK, workbook);

        return true;
    }

    private void cancel() {
        clearTemp();
        isRunning = false;
    }

    private Boolean multiSheets(IMindMapViewer sourceViewer) {
        IWorkbook workbook = (IWorkbook) sourceViewer
                .getAdapter(IWorkbook.class);
        if (workbook.getSheets().size() > 1)
            return true;
        return false;
    }

    private void generatePreview(Display display) {
        MindMapImageExporter exporter = new MindMapImageExporter(display);
        exporter.setSource(new MindMap(workbook.getPrimarySheet()), null, null);
        exporter.setTargetWorkbook(workbook);
        previewImage = exporter.createImage();
        exporter.export(previewImage);
        origin = exporter.calcRelativeOrigin();
    }

    private String getDefaultMapTitle() {
        if (workbook != null) {
            return TextFormatter.removeNewLineCharacter(workbook
                    .getPrimarySheet().getRootTopic() != null ? workbook
                    .getPrimarySheet().getRootTopic().getTitleText() : ""); //$NON-NLS-1$
        }
        return null;
    }

    private String getBackgroundColor() {
        Layer layer = sourceViewer.getLayer(GEF.LAYER_BACKGROUND);
        if (layer != null) {
            Color color = layer.getBackgroundColor();
            if (color != null)
                return ColorUtils.toString(color);
        }
        return "#ffffff"; //$NON-NLS-1$
    }

    private BiggerplateUploaderDialog createUploadDialog() {
        BiggerplateUploaderDialog dialog = new BiggerplateUploaderDialog(
                parentShell);
        dialog.setInfo(info);
        return dialog;
    }

    private void clearTemp() {
        if (previewImage != null) {
            previewImage.dispose();
            previewImage = null;
        }

        if (extractor != null) {
            extractor.delete();
            extractor = null;
        }

        if (file != null) {
            file.delete();
            file = null;
        }

        workbook = null;
    }

    @Override
    public void done(IJobChangeEvent event) {
        clearTemp();

        final IStatus result = event.getResult();
        runInUI(new Runnable() {
            public void run() {
                if (result.isOK()) {
                    promptCompletion(info.getString(Info.RESULT_URL));
                } else if (result.matches(IStatus.ERROR | IStatus.WARNING)) {
                    promptError(result);
                }
            }
        });
        isRunning = false;
    }

    private void runInUI(final Runnable runnable) {
        Display display = parentShell.getDisplay();
        if (display == null || display.isDisposed())
            return;
        display.asyncExec(runnable);
    }

    private void promptCompletion(final String permalink) {
        if (permalink == null) {
            return;
        }
        IWorkbench workbench = PlatformUI.getWorkbench();
        final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
        if (window == null) {
            return;
        }
        IAction viewAction = new Action() {
            public void run() {
                try {
                    Program.launch(permalink);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                }
            }
        };
        viewAction.setText(BiggerplateMessages.Uploader_OpenAction_text);

        Notification popup = new Notification(window.getShell(), null,
                BiggerplateMessages.UploadJob_OpenMap_message, viewAction, null);
        popup.setGroupId(BiggerplatePlugin.PLUGIN_ID);
        popup.setCenterPopUp(true);
        popup.setDuration(20000);
        popup.popUp();
    }

    private void promptError(IStatus error) {
        MessageDialog.openWarning(null, BiggerplateMessages.ErrorDialog_title,
                error.getMessage());
    }

    public static void validateUploadFile(String path) {
        Set<String> entries = new HashSet<String>();
        try {
            ZipInputStream zin = new ZipInputStream(new FileInputStream(path));
            try {
                ZipEntry e;
                String name;
                while ((e = zin.getNextEntry()) != null) {
                    name = e.getName();
                    entries.add(name);
                }
            } finally {
                zin.close();
            }
        } catch (Throwable e) {
            throw new FileValidationException("File Validation Failed: " //$NON-NLS-1$
                    + e.getLocalizedMessage(), e);
        }
        if (!entries.contains("Thumbnails/thumbnail.png")) //$NON-NLS-1$
            throw new FileValidationException(
                    "File Validation Failed: missing entry 'Thumbnails/thumbnail.png'"); //$NON-NLS-1$
    }

}
