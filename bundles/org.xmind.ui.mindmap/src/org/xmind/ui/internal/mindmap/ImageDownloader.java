package org.xmind.ui.internal.mindmap;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.xmind.core.Core;
import org.xmind.core.util.FileUtils;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.io.DownloadJob;
import org.xmind.ui.io.UIJobChangeListener;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.MindMapUI;

public class ImageDownloader {

    private class ImageDownload extends UIJobChangeListener {

        private String url;

        private IStatus status;

        private ImageDescriptor image;

        private File tempFile;

        private DownloadJob job;

        private List<Runnable> notifiers = new ArrayList<Runnable>();

        public ImageDownload(String url) {
            this.url = url;
            startDownload();
        }

        private void startDownload() {
            String fileExtension;
            try {
                URI uri = new URI(url);
                fileExtension = FileUtils.getExtension(uri.getPath());
            } catch (URISyntaxException e) {
                fileExtension = FileUtils.getExtension(url);
            }
            this.tempFile = Core.getWorkspace().createTempFile(
                    "imageDownload", "", fileExtension); //$NON-NLS-1$ //$NON-NLS-2$
            this.job = new DownloadJob(NLS.bind("Download Image From ''{0}''", //$NON-NLS-1$
                    url), url, tempFile.getAbsolutePath(), MindMapUI.PLUGIN_ID);
            this.job.addJobChangeListener(this);
            this.job.schedule(50);
            this.status = new Status(IStatus.INFO, MindMapUI.PLUGIN_ID,
                    NLS.bind("Downloading image from ''{0}''", url)); //$NON-NLS-1$
            this.image = getCurrentBusyImage();
        }

        public void addNotifier(Runnable notifier) {
            notifiers.add(notifier);
            if (notifiers.size() > 0) {
                if (status.getSeverity() != IStatus.OK
                        && status.getSeverity() != IStatus.INFO) {
                    startDownload();
                }
                if (status.getSeverity() == IStatus.INFO) {
                    busyDownloads.add(this);
                }
            }
        }

        public void removeNotifier(Runnable notifier) {
            notifiers.remove(notifier);
            if (status.getSeverity() != IStatus.INFO || notifiers.size() <= 0) {
                busyDownloads.remove(this);
            }
        }

        protected void doDone(IJobChangeEvent event) {
            busyDownloads.remove(this);
            int code = event.getResult().getSeverity();
            if (code == IStatus.OK) {
                onSuccess();
            } else if (code == IStatus.CANCEL) {
                onCancel();
            } else {
                onFailed(event.getResult());
            }
        }

        private void onSuccess() {
            try {
                this.status = new Status(IStatus.OK, MindMapUI.PLUGIN_ID,
                        NLS.bind("Image downloaded from ''{0}''", url)); //$NON-NLS-1$
                setImage(ImageDescriptor
                        .createFromURL(tempFile.toURI().toURL()));
            } catch (MalformedURLException e) {
                this.status = new Status(
                        IStatus.ERROR,
                        MindMapUI.PLUGIN_ID,
                        NLS.bind(
                                "Failed to parse the temp file path for the downloaded image ''{0}''", //$NON-NLS-1$
                                url), e);
                MindMapUIPlugin.getDefault().getLog().log(this.status);
                setImage(getWarningImage());
            }
        }

        private void onFailed(IStatus result) {
            this.status = new Status(
                    IStatus.ERROR,
                    MindMapUI.PLUGIN_ID,
                    NLS.bind(
                            "Failed to download image from ''{0}'' due to ''{1}''", //$NON-NLS-1$
                            url, result.getMessage()), result.getException());
            setImage(getWarningImage());
        }

        private void onCancel() {
            this.status = new Status(IStatus.CANCEL, MindMapUI.PLUGIN_ID,
                    NLS.bind("Image download canceled from ''{0}''", url)); //$NON-NLS-1$
            setImage(getWarningImage());
        }

        public void setImage(ImageDescriptor newImage) {
            this.image = newImage;
            callNotifiers();
        }

        private void callNotifiers() {
            Object[] notifierArray = notifiers.toArray();
            for (final Object notifier : notifierArray) {
                try {
                    ((Runnable) notifier).run();
                } catch (Throwable e) {
                    MindMapUIPlugin
                            .getDefault()
                            .getLog()
                            .log(new Status(IStatus.WARNING,
                                    MindMapUI.PLUGIN_ID,
                                    "Failed to call one notifier when image downloaded from '" //$NON-NLS-1$
                                            + url + "'", e)); //$NON-NLS-1$
                }
            }
        }

        public ImageDescriptor getImage() {
            return image;
        }

        public IStatus getStatus() {
            return status;
        }

    }

    private static final int ROTATION_INTERVALS = 200;

    private static final ImageDownloader instance = new ImageDownloader();

    private Map<String, ImageDownload> downloads = new HashMap<String, ImageDownload>();

    private Set<ImageDownload> busyDownloads = new HashSet<ImageDownload>();

    private List<ImageDescriptor> busyImages = null;

    private ImageDescriptor warningImage = null;

    private int busyImageIndex = 0;

    private boolean rotatingBusyImages = false;

    private Runnable rotateBusyImages = new Runnable() {
        public void run() {
            rotateBusyImages();
        }
    };

    private ImageDownloader() {
    }

    public void register(String url, Runnable notifier) {
        ImageDownload download = downloads.get(url);
        if (download == null) {
            download = new ImageDownload(url);
            downloads.put(url, download);
        }
        download.addNotifier(notifier);
        checkBusyImageRotation();
    }

    public void unregister(String url, Runnable notifier) {
        ImageDownload download = downloads.get(url);
        if (download != null) {
            download.removeNotifier(notifier);
        }
        checkBusyImageRotation();
    }

    public ImageDescriptor getImage(String url) {
        ImageDownload download = downloads.get(url);
        if (download != null)
            return download.getImage();
        return null;
    }

    public IStatus getStatus(String url) {
        ImageDownload download = downloads.get(url);
        if (download != null)
            return download.getStatus();
        return null;
    }

    private void checkBusyImageRotation() {
        if (busyDownloads.size() > 0) {
            if (!rotatingBusyImages) {
                asyncRunInUI(new Runnable() {
                    public void run() {
                        Display.getCurrent().timerExec(ROTATION_INTERVALS,
                                rotateBusyImages);
                    }
                });
            }
            rotatingBusyImages = true;
        } else {
            rotatingBusyImages = false;
        }
    }

    private void rotateBusyImages() {
        if (!rotatingBusyImages)
            return;

        busyImageIndex++;
        if (busyImageIndex >= busyImages.size()) {
            busyImageIndex = 0;
        }
        Object[] busyDownloadsArray = busyDownloads.toArray();
        for (Object download : busyDownloadsArray) {
            ((ImageDownload) download).setImage(getCurrentBusyImage());
        }
        Display.getCurrent().timerExec(ROTATION_INTERVALS, rotateBusyImages);
    }

    private void asyncRunInUI(final Runnable runnable) {
        Display display = Display.getCurrent();
        if (display == null)
            display = Display.getDefault();
        if (display != null) {
            display.asyncExec(runnable);
        }
    }

    private List<ImageDescriptor> getBusyImages() {
        if (busyImages == null) {
            busyImages = findBusyImages();
        }
        return busyImages;
    }

    private ImageDescriptor getWarningImage() {
        if (warningImage == null)
            warningImage = ImageDescriptor.createFromImage(Display.getCurrent()
                    .getSystemImage(SWT.ICON_WARNING));
        return warningImage;
    }

    private List<ImageDescriptor> findBusyImages() {
        List<ImageDescriptor> list = new ArrayList<ImageDescriptor>();
        for (int index = 1; index <= 12; index++) {
            String path = String.format("/icons/busy/busy_f%02d.gif", index); //$NON-NLS-1$
            ImageDescriptor img = AbstractUIPlugin.imageDescriptorFromPlugin(
                    "org.xmind.ui.browser", path); //$NON-NLS-1$
            if (img != null) {
                list.add(img);
            }
        }
        if (list.isEmpty()) {
            list.add(MindMapUI.getImages().get(IMindMapImages.STOP, true));
        }
        return list;
    }

    private ImageDescriptor getCurrentBusyImage() {
        return getBusyImages().get(busyImageIndex);
    }

    public static ImageDownloader getInstance() {
        return instance;
    }

}
