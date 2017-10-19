package org.xmind.cathy.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.xmind.core.util.FileUtils;

/**
 * 
 * @author Shawn Liu
 * @since 3.6.50
 */
public class ShowWelcomeService implements IStartup {

    private static final boolean DEBUG_NOT_SHOW_WELCOME = CathyPlugin
            .getDefault().isDebugging("/debug/notshowwelcome"); //$NON-NLS-1$

    private static final String NO_FIRST_START = System
            .getProperty("org.xmind.product.buildid") //$NON-NLS-1$
            + ".noFirstStart"; //$NON-NLS-1$

    public void earlyStartup() {
        if (DEBUG_NOT_SHOW_WELCOME) {
            return;
        }

        final File propertiesFile = new File(getPropertiesFilePath());
        if (!propertiesFile.exists()) {
            FileUtils.ensureFileParent(propertiesFile);
            try {
                propertiesFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        boolean isNotFirstStart = Boolean
                .valueOf(properties.getProperty(NO_FIRST_START));

        if (!isNotFirstStart) {
            final IWorkbench workbench = PlatformUI.getWorkbench();
            workbench.getDisplay().asyncExec(new Runnable() {
                public void run() {
                    IWorkbenchWindow window = workbench
                            .getActiveWorkbenchWindow();
                    if (window != null) {
                        new WelcomeDialog(window.getShell()).open();

                        //set value to properties file.
                        properties.setProperty(NO_FIRST_START,
                                Boolean.toString(true));
                        try {
                            properties.store(
                                    new FileOutputStream(propertiesFile), null);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    private String getPropertiesFilePath() {
        return CathyPlugin.getDefault().getStateLocation()
                .append("start.properties").toString(); //$NON-NLS-1$
    }

}
