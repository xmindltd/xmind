package org.xmind.cathy.internal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.splash.AbstractSplashHandler;

public class CathySplashHandler extends AbstractSplashHandler {

    private GC shellGC = null;

    private Image image = null;

    private Object resourceLock = new Object();

    public CathySplashHandler() {
    }

    public void init(final Shell splash) {
        super.init(splash);
        final ImageLoader loader = new ImageLoader();
        final ImageData[] imageDataArray = loadAnimationImageData(loader);
        if (imageDataArray == null)
            return;

        if (imageDataArray.length <= 1 || loader.repeatCount != 0) {
            CathyPlugin
                    .log("Invalid GIF format of application loading indicator."); //$NON-NLS-1$
            return;
        }

        shellGC = new GC(splash);

        splash.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                releaseResources();
            }
        });

        final Display display = splash.getDisplay();
        Thread animateThread = new Thread(new Runnable() {
            public void run() {
                showLoadingAnimation(display, imageDataArray);
            }
        }, "LoadingIndicatorAnimation"); //$NON-NLS-1$
        animateThread.setDaemon(true);
        animateThread.start();
    }

    private ImageData[] loadAnimationImageData(ImageLoader loader) {
        URL url = FileLocator.find(Platform.getBundle(CathyPlugin.PLUGIN_ID),
                new Path("icons/progress.gif"), null); //$NON-NLS-1$
        if (url == null)
            return null;
        InputStream imageStream = null;
        try {
            imageStream = url.openStream();
            return loader.load(imageStream);
        } catch (IOException e) {
            CathyPlugin
                    .log(e,
                            "There was an error loading GIF from " + url.toExternalForm()); //$NON-NLS-1$
            return null;
        } finally {
            try {
                imageStream.close();
            } catch (IOException e) {
            }
        }
    }

    private void releaseResources() {
        synchronized (resourceLock) {
            // Dispose of the image
            if (image != null) {
                if (!image.isDisposed()) {
                    image.dispose();
                }
                image = null;
            }

            // Dispose of the GC
            if (shellGC != null) {
                if (!shellGC.isDisposed()) {
                    shellGC.dispose();
                }
                shellGC = null;
            }
        }
    }

    private void showLoadingAnimation(final Display display,
            final ImageData[] imageDataArray) {
        int imageDataIndex = -1;
        ImageData imageData;

        while (true) {
            imageDataIndex = (imageDataIndex + 1) % imageDataArray.length;
            imageData = imageDataArray[imageDataIndex];

            synchronized (resourceLock) {
                if (display.isDisposed() || shellGC == null
                        || shellGC.isDisposed()) {
                    return;
                }

                if (image != null && !image.isDisposed()) {
                    image.dispose();
                    image = null;
                }
                image = new Image(display, imageData);
                shellGC.drawImage(image, 233, 215);
            }

            try {
                int ms = imageData.delayTime * 10;
                if (ms < 20)
                    ms += 30;
                if (ms < 30)
                    ms += 10;
                Thread.sleep(ms);
            } catch (InterruptedException e) {
            }
        }
    }

}
