package org.xmind.ui.internal.protocols;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.core.IAdaptable;
import org.xmind.core.ITopic;
import org.xmind.core.event.ICoreEventSource;
import org.xmind.core.net.util.LinkUtils;
import org.xmind.core.util.FileUtils;
import org.xmind.gef.image.ImageExportUtils;
import org.xmind.ui.browser.BrowserSupport;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.browser.BrowserUtil;
import org.xmind.ui.io.WebImageManager;
import org.xmind.ui.io.WebImageManager.WebImageCallback;
import org.xmind.ui.mindmap.IHyperlinked;
import org.xmind.ui.mindmap.IMindMapImages;
import org.xmind.ui.mindmap.IProtocol;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ImageUtils;
import org.xmind.ui.util.JobPool;
import org.xmind.ui.util.MindMapUtils;

public class WebProtocol implements IProtocol {

    private static interface Callback {

        void handleWith(ImageDescriptor icon);
    }

    public static final String WEB_ICON_EVENT_TYPE = "webIcon"; //$NON-NLS-1$

    private static String DEFAULT_BROWSER_ID = "org.xmind.ui.defaultProtocol.browser"; //$NON-NLS-1$

    private static final String PATH_FAVICONS = "favicons/"; //$NON-NLS-1$

    private static JobPool jobPool = new JobPool();

    private static class OpenURLAction extends Action implements IHyperlinked {

        private String url;

        public OpenURLAction(String url) {
            super(MindMapMessages.OpenHyperlink_text,
                    MindMapUI.getImages().get(IMindMapImages.HYPERLINK, true));
            this.url = url;
            setDisabledImageDescriptor(
                    MindMapUI.getImages().get(IMindMapImages.HYPERLINK, false));
            setToolTipText(url);
        }

        public void run() {
            SafeRunner.run(new SafeRunnable() {
                public void run() throws Exception {
                    String theURL = url;
                    try {
                        URI uri = new URI(theURL);
                        if (LinkUtils.HOST_NET.equals(uri.getHost())
                                || LinkUtils.HOST_CN.equals(uri.getHost())) {
                            theURL = BrowserUtil.makeRedirectURL(theURL);
                        }
                    } catch (Exception ignored) {
                    }
                    BrowserSupport.getInstance()
                            .createBrowser(DEFAULT_BROWSER_ID).openURL(theURL);
                }
            });
        }

        @Override
        public String getHyperlink() {
            return url;
        }
    }

    public IAction createOpenHyperlinkAction(final Object context,
            final String url) {
        final IAction action = new OpenURLAction(url);
        ImageDescriptor image = getWebIcon(url, new Callback() {

            public void handleWith(final ImageDescriptor icon) {
                if (icon == null) {
                    return;
                }

                Display.getDefault().asyncExec(new Runnable() {

                    public void run() {
                        ImageDescriptor oldImage = action.getImageDescriptor();
                        action.setImageDescriptor(icon);

                        ITopic topic = null;
                        if (context instanceof ITopic) {
                            topic = (ITopic) context;
                        } else if (context instanceof IAdaptable) {
                            topic = (ITopic) ((IAdaptable) context)
                                    .getAdapter(ITopic.class);
                        }

                        if (topic != null) {
                            ICoreEventSource source = topic
                                    .getAdapter(ICoreEventSource.class);
                            if (source != null) {
                                source.getCoreEventSupport()
                                        .dispatchValueChange(source,
                                                WebProtocol.WEB_ICON_EVENT_TYPE,
                                                oldImage, icon);
                            }
                        }
                    }
                });
            }
        });

        if (image != null) {
            action.setImageDescriptor(image);
        }

        return action;
    }

    private ImageDescriptor getWebIcon(String url, final Callback callback) {
        final String iconUrl = getWebIconUrl(url);
        if (iconUrl == null) {
            return null;
        }

        final String key = "org.xmind.ui.webIcon(" + iconUrl + ")"; //$NON-NLS-1$ //$NON-NLS-2$
        ImageDescriptor image = ImageUtils.getDescriptor(key);

        if (image != null) {
            return image;
        }

        image = getFaviconFromFile(iconUrl);
        if (image != null) {
            ImageUtils.putImageDescriptor(key, image);
            return image;
        }

        WebImageManager.getInstance().requestWebImage(iconUrl, jobPool,
                new WebImageCallback() {

                    public void handleWith(String imagePath) {
                        if (imagePath == null
                                || !new File(imagePath).exists()) {
                            return;
                        }

                        String newPath = saveFaviconToFile(iconUrl, imagePath);
                        if (newPath == null || !new File(newPath).exists()) {
                            return;
                        }

                        ImageDescriptor image = null;
                        try {
                            image = ImageDescriptor.createFromURL(
                                    new File(newPath).toURI().toURL());
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        if (image == null) {
                            return;
                        }
                        ImageUtils.putImageDescriptor(key, image);

                        if (callback != null) {
                            callback.handleWith(image);
                        }
                    }
                });

        return null;
    }

    private String getWebIconUrl(String url) {
        if (url == null) {
            return null;
        }

        int domainEnd = -1;
        if (url.contains("://")) { //$NON-NLS-1$
            int start = url.indexOf("://") + "://".length(); //$NON-NLS-1$ //$NON-NLS-2$
            domainEnd = url.indexOf("/", start); //$NON-NLS-1$
        } else {
            domainEnd = url.indexOf("/"); //$NON-NLS-1$
        }
        String domain = url.substring(0,
                domainEnd == -1 ? url.length() : domainEnd);

        return domain + "/favicon.ico"; //$NON-NLS-1$
    }

    private ImageDescriptor getFaviconFromFile(String key) {
        String fileName = getFileName(key);
        String path = Core.getWorkspace()
                .getAbsolutePath(PATH_FAVICONS + fileName);
        if (path == null) {
            return null;
        }

        File file = new File(path);
        if (file.exists()) {
            long lastModified = file.lastModified();
            long currentTime = System.currentTimeMillis();
            //the validity of image file is 30 days.
            if ((currentTime - lastModified) / (24 * 3600 * 1000) > 30) {
                file.delete();
            } else {
                try {
                    return ImageDescriptor
                            .createFromURL(new File(path).toURI().toURL());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    private String saveFaviconToFile(String key, String tempImagePath) {
        if (tempImagePath == null) {
            return null;
        }

        String fileName = getFileName(key);
        String path = Core.getWorkspace()
                .getAbsolutePath(PATH_FAVICONS + fileName);
        if (path == null) {
            return null;
        }

        File file = new File(path);
        if (file.exists()) {
            return path;
        }

        Image image = null;
        try {
            image = new Image(Display.getCurrent(), tempImagePath);
        } catch (Exception e) {
            return null;
        }
        Image image1x = ImageUtils.createScaledImage(image, 16, 16);
        Image image2x = ImageUtils.createScaledImage(image, 32, 32);

        FileUtils.ensureFileParent(file);
        FileOutputStream outputStream = null;
        FileOutputStream outputStream2x = null;

        try {
            //get @1x
            File file1x = new File(path);
            file1x.createNewFile();
            outputStream = new FileOutputStream(file1x);
            ImageExportUtils.saveImage(image1x, outputStream, SWT.IMAGE_PNG);

            //get @2x
            String path2x = Core.getWorkspace()
                    .getAbsolutePath(PATH_FAVICONS + get2xFileName(key));
            File file2x = new File(path2x);
            file2x.createNewFile();
            outputStream2x = new FileOutputStream(file2x);
            ImageExportUtils.saveImage(image2x, outputStream2x, SWT.IMAGE_PNG);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                outputStream.close();
                outputStream2x.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            new File(tempImagePath).delete();
            image.dispose();
            image1x.dispose();
            image2x.dispose();
        }

        return path;
    }

    private String getFileName(String key) {
        if (key == null) {
            return null;
        }

        key = key.substring(0, key.lastIndexOf(".ico")); //$NON-NLS-1$
        key = key.replace("://", "-"); //$NON-NLS-1$ //$NON-NLS-2$
        key = key.replace(".", "-"); //$NON-NLS-1$ //$NON-NLS-2$
        key = key.replace("/", "-"); //$NON-NLS-1$ //$NON-NLS-2$
        key += ".png"; //$NON-NLS-1$
        key = MindMapUtils.trimFileName(key);

        return key;
    }

    private String get2xFileName(String key) {
        String fileName = getFileName(key);
        if (fileName != null) {
            fileName = fileName.substring(0, fileName.length() - 4) + "@2x" //$NON-NLS-1$
                    + fileName.substring(fileName.length() - 4);
        }

        return fileName;
    }

    public boolean isHyperlinkModifiable(Object source, String uri) {
        return true;
    }

}
