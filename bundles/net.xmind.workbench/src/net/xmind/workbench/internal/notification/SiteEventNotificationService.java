/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 *
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL),
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 *
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package net.xmind.workbench.internal.notification;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.xmind.core.net.IDataStore;
import org.xmind.core.net.internal.XMindNetRequest;
import org.xmind.ui.dialogs.Notification;

import net.xmind.signin.IAccountInfo;
import net.xmind.signin.ILicenseInfo;
import net.xmind.signin.ILicenseKeyHeader;
import net.xmind.signin.ILicenseListener;
import net.xmind.signin.XMindNet;
import net.xmind.workbench.internal.Messages;
import net.xmind.workbench.internal.XMindNetWorkbench;

public class SiteEventNotificationService implements ILicenseListener {

    private static final String LOCAL_STORE_FILE_NAME = "site-events.xml"; //$NON-NLS-1$

    private static boolean DEBUGGING = XMindNetWorkbench
            .isDebugging("/debug/events"); //$NON-NLS-1$

    private static String HABIT_PROP_FILE = "habit.properties"; //$NON-NLS-1$

    private static String FIRST_START_TIMESTAMP = "firstStartTimestamp"; //$NON-NLS-1$

    private static String START_TIMESTAMP_1 = "startTimestamp1"; //$NON-NLS-1$

    private static String START_TIMESTAMP_2 = "startTimestamp2"; //$NON-NLS-1$

    private static String START_TIMESTAMP_3 = "startTimestamp3"; //$NON-NLS-1$

    private static int POPUP_DURATION = 60000;

    private class CheckSiteEventJob implements Runnable {

        private boolean startup;

        private Thread thread = null;

        private XMindNetRequest request = null;

        public CheckSiteEventJob(boolean startup) {
            this.startup = startup;
        }

        public synchronized void start() {
            if (this.thread != null)
                return;

            this.thread = new Thread(this, "CheckXMindSiteEvents"); //$NON-NLS-1$
            this.thread.setDaemon(true);
            this.thread.setPriority(Thread.MIN_PRIORITY);
            this.thread.start();
        }

        public synchronized void stop() {
            if (this.thread != null) {
                this.thread.interrupt();
                this.thread = null;
            }
            if (this.request != null) {
                this.request.abort();
                this.request = null;
            }
        }

        public boolean isRunning() {
            return this.thread != null;
        }

        public void run() {
            try {
                request = new XMindNetRequest();
                if (DEBUGGING)
                    request.debug();
                request.path("/_api/events"); //$NON-NLS-1$
                request.addParameter("version", productVersion); //$NON-NLS-1$
                String buildId = System
                        .getProperty("org.xmind.product.buildid"); //$NON-NLS-1$
                if (buildId != null && !"".equals(buildId)) { //$NON-NLS-1$
                    request.addParameter("buildid", buildId); //$NON-NLS-1$
                }
                request.addParameter("os", Platform.getOS()); //$NON-NLS-1$
                request.addParameter("arch", Platform.getOSArch()); //$NON-NLS-1$
                request.addParameter("nl", Platform.getNL()); //$NON-NLS-1$
                request.addParameter("distrib", getDistributionId()); //$NON-NLS-1$
                request.addParameter("account_type", getAccountType()); //$NON-NLS-1$
                request.addParameter("license_info", //$NON-NLS-1$
                        licenseKeyHeader == null ? "" //$NON-NLS-1$
                                : licenseKeyHeader.toEncoded());

                request.addParameter(FIRST_START_TIMESTAMP,
                        habitData.getProperty(FIRST_START_TIMESTAMP));
                request.addParameter(START_TIMESTAMP_1,
                        habitData.getProperty(START_TIMESTAMP_1));
                request.addParameter(START_TIMESTAMP_2,
                        habitData.getProperty(START_TIMESTAMP_2));
                request.addParameter(START_TIMESTAMP_3,
                        habitData.getProperty(START_TIMESTAMP_3));

                String xmindID = getXMindID();
                if (xmindID != null && !"".equals(xmindID)) { //$NON-NLS-1$
                    request.addParameter("xmind_id", xmindID); //$NON-NLS-1$
                }

                request.get();

                if (!isRunning() || request.isAborted())
                    return;

                int code = request.getStatusCode();
                IDataStore data = request.getData();
                if (code == XMindNetRequest.HTTP_OK) {
                    if (data != null) {
                        handleSiteEvents(data);
                    } else {
                        XMindNetWorkbench.log(request.getError(),
                                "Failed to parse response of site events."); //$NON-NLS-1$
                    }
                } else if (code == XMindNetRequest.HTTP_ERROR) {
                    XMindNetWorkbench.log(request.getError(),
                            "Failed to connect to xmind.net for site events."); //$NON-NLS-1$
                } else {
                    XMindNetWorkbench.log(request.getError(),
                            "Failed to retrieve site events: " + code); //$NON-NLS-1$
                }
            } catch (Throwable e) {
                XMindNetWorkbench.log(e,
                        "Failed to retrieve XMind site events."); //$NON-NLS-1$
            } finally {
                request = null;
            }
        }

        protected void handleSiteEvents(IDataStore data) {
            List<ISiteEvent> siteEvents = new ArrayList<ISiteEvent>();
            for (IDataStore siteData : data.getChildren("events")) { //$NON-NLS-1$
                siteEvents.add(new DataStoreSiteEvent(siteData));
            }
//            if (DEBUGGING) {
////             for test:
//                JSONObject js = new JSONObject();
//                try {
//                    js.put("id", "9999");
//                    js.put("prompt", "always");
//                    js.put("title", "XMind official group on Biggerplate");
//                    js.put("url",
//                            "xmind://www.xmind.net/checkForUpdates?skippable=yes");
//                    js.put("download",
//                            "http://blog.xmind.net/en/2012/02/biggerplate/");
//                    js.put("html", "Ren ren cool boy..");
//                    js.put("style", "go");
//                } catch (JSONException e1) {
//                    e1.printStackTrace();
//                }
//                siteEvents.add(new JSONSiteEvent(js));
//            }
            if (!isRunning())
                return;

            SiteEventStore localStore = getLocalEventStore();
            if (!isRunning())
                return;

            List<ISiteEvent> newEvents = localStore.calcNewEvents(siteEvents,
                    startup);
            if (!isRunning())
                return;

            if (!newEvents.isEmpty()) {
                showNotifications(newEvents);
            }
            if (!isRunning())
                return;

            setLocalEventStore(new SiteEventStore(siteEvents));
            if (!isRunning())
                return;

            try {
                saveLocalEventStore();
            } catch (IOException e) {
                XMindNetWorkbench.log(e, "Failed to save site events."); //$NON-NLS-1$
            }
        }

    }

    private static class URLAction extends Action {

        private String url;

        private boolean openExternal;

        public URLAction(String text, String url, boolean openExternal) {
            super(text == null ? Messages.SiteEventNotificationService_View_text
                    : text);
            this.url = url;
            this.openExternal = openExternal;
        }

        @Override
        public void run() {
            XMindNet.gotoURL(openExternal, url);
        }
    }

    private IWorkbench workbench;

    private CheckSiteEventJob job;

    private SiteEventStore localEventStore;

    private String licenseType = "free"; //$NON-NLS-1$

    private ILicenseKeyHeader licenseKeyHeader = null;

    private boolean startup = true;

    private IPreferenceStore prefStore;

    private String productVersion;

    private Properties habitData;

    private File habitFile;

    public SiteEventNotificationService(IWorkbench workbench) {
        Assert.isNotNull(workbench);
        this.workbench = workbench;
        prefStore = new ScopedPreferenceStore(InstanceScope.INSTANCE,
                "org.xmind.cathy"); //$NON-NLS-1$
        productVersion = System.getProperty("org.xmind.product.version"); //$NON-NLS-1$
        habitData = loadHabitData();
    }

    private SiteEventStore getLocalEventStore() {
        if (localEventStore == null) {
            File file = getLocalStoreFile();
            if (file.isFile()) {
                try {
                    localEventStore = new SiteEventStore(new InputStreamReader(
                            new FileInputStream(file), "UTF-8")); //$NON-NLS-1$
                } catch (IOException e) {
                    localEventStore = new SiteEventStore();
                }
            } else {
                localEventStore = new SiteEventStore();
            }
        }
        return localEventStore;
    }

    private void setLocalEventStore(SiteEventStore store) {
        this.localEventStore = store;
    }

    private void saveLocalEventStore() throws IOException {
        if (localEventStore == null)
            return;

        File file = getLocalStoreFile();
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        localEventStore.save(
                new OutputStreamWriter(new FileOutputStream(file), "UTF-8")); //$NON-NLS-1$
    }

    private static File getLocalStoreFile() {
        return new File(XMindNetWorkbench.getStatePath(LOCAL_STORE_FILE_NAME));
//        return new File(Core.getWorkspace().getAbsolutePath("site-events.xml")); //$NON-NLS-1$
    }

    private synchronized void checkEvent() {
        if (isNotificationAllowed()) {
            if (job != null && job.isRunning())
                return;
            job = new CheckSiteEventJob(startup);
            job.start();
            startup = false;
        } else {
            stop();
        }
    }

    private boolean isNotificationAllowed() {
        return prefStore.getBoolean("checkUpdatesOnStartup"); //$NON-NLS-1$
    }

    private void showNotifications(final List<ISiteEvent> events) {
        if (workbench == null)
            return;
        if (canShowNotifications()) {
            doShowNotifications(events);
        } else {
            Thread thread = new Thread(new Runnable() {
                public void run() {
                    try {
                        while (!canShowNotifications()) {
                            Thread.sleep(5000);
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                    doShowNotifications(events);
                }
            });
            thread.setPriority(Thread.MIN_PRIORITY);
            thread.setDaemon(true);
            thread.setName("WaitToShowNotifications"); //$NON-NLS-1$
            thread.start();
        }
    }

    private boolean canShowNotifications() {
        if (workbench == null)
            return false;

        final IWorkbench wb = workbench;
        final Display display = wb.getDisplay();
        final boolean[] active = new boolean[1];
        display.syncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
                active[0] = window != null
                        && display.getActiveShell() == window.getShell();
            }
        });
        return active[0];
    }

    private void doShowNotifications(final List<ISiteEvent> events) {
        if (workbench == null)
            return;
        final IWorkbench wb = workbench;
        final Display display = wb.getDisplay();
        if (display == null || display.isDisposed())
            return;
        display.asyncExec(new Runnable() {
            public void run() {
                IWorkbenchWindow window = wb.getActiveWorkbenchWindow();
                if (window == null)
                    return;

                final Shell shell = window.getShell();
                if (shell == null || shell.isDisposed())
                    return;

                for (ISiteEvent event : events) {
                    if (DEBUGGING)
                        System.out.println("Showing event: " + event); //$NON-NLS-1$

                    if (!isShowTime(event))
                        continue;

                    String text = event.getHTML();
                    if (text == null || "".equals(text)) { //$NON-NLS-1$
                        text = event.getText();
                    }
                    if (text != null) {
                        String url = event.getInternalUrl();
                        if (url == null) {
                            url = event.getEventUrl();
                        }
                        String style = event.getStyle();
                        if (style != null && "go".equals(style.toLowerCase())) { //$NON-NLS-1$
                            XMindNet.gotoURL(url);
                        } else {
//                            String caption = event.getCaption();
                            String moreUrl = event.getMoreUrl();
                            int duration = event.getDuration();
                            if (duration < 0) { // long time stay
                                duration = 0;
                            } else if (duration == 0) { // default value
                                duration = POPUP_DURATION;
                            }
                            IAction action = new URLAction(
                                    Messages.SiteEventNotificationService_openAction_text,
                                    url, event.isOpenExternal());
                            IAction moreAction = moreUrl == null ? null
                                    : new URLAction(
                                            Messages.SiteEventNotificationService_More_text,
                                            moreUrl, event.isOpenExternal());

                            Notification popup = new Notification(shell, null,
                                    text, action, moreAction);
                            popup.setGroupId(XMindNetWorkbench.PLUGIN_ID);
                            popup.setCenterPopUp(true);
                            popup.setDuration(duration);
                            popup.popUp();
                            recordShowEventTimestamp(event);
                        }
                    }
                }
            }
        });
    }

    private synchronized void stop() {
        if (job != null) {
            job.stop();
            job = null;
        }
    }

    protected void parseLicenseInfo(ILicenseInfo info) {
        int type = info.getType();
        if ((type & ILicenseInfo.VALID_PRO_LICENSE_KEY) != 0) {
            this.licenseType = "pro_license"; //$NON-NLS-1$
            this.licenseKeyHeader = info.getLicenseKeyHeader();
        } else if ((type & ILicenseInfo.VALID_PLUS_LICENSE_KEY) != 0) {
            this.licenseType = "plus_license"; //$NON-NLS-1$
            this.licenseKeyHeader = info.getLicenseKeyHeader();
        } else if ((type & ILicenseInfo.VALID_PRO_SUBSCRIPTION) != 0) {
            this.licenseType = "pro"; //$NON-NLS-1$
            this.licenseKeyHeader = null;
        } else {
            this.licenseType = "free"; //$NON-NLS-1$
            this.licenseKeyHeader = null;
        }
    }

    public void start() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            return;
        }

        recordFirstTimeStartXMindTimestamp();
        recordStartXMindTimestamp();

        if (productVersion != null && !"".equals(productVersion)) { //$NON-NLS-1$
            ILicenseInfo licenseInfo = XMindNet.getLicenseInfo();
            int type = licenseInfo.getType();
            if (type == ILicenseInfo.VERIFYING) {
                XMindNet.addLicenseListener(this);
            } else {
                parseLicenseInfo(licenseInfo);
                checkEvent();
            }
        }
    }

    public void shutdown() {
        stop();
        XMindNet.removeLicenseListener(this);

        saveHabitData();
    }

    public boolean preShutdown(IWorkbench workbench, boolean forced) {
        stop();
        return true;
    }

    private static final String getDistributionId() {
        String distribId = System
                .getProperty("org.xmind.product.distribution.id"); //$NON-NLS-1$
        if (distribId == null || "".equals(distribId)) { //$NON-NLS-1$
            distribId = "cathy_portable"; //$NON-NLS-1$
        }
        return distribId;
    }

    private String getAccountType() {
        return licenseType;
    }

    public void licenseVerified(ILicenseInfo info) {
        XMindNet.removeLicenseListener(this);
        parseLicenseInfo(info);
        checkEvent();
    }

    private Properties loadHabitData() {
        Properties prop = new Properties();
        File file = getHabitFile();
        if (file == null || !file.exists())
            return prop;

        try {
            FileInputStream fis = new FileInputStream(file);
            try {
                prop.load(fis);
            } finally {
                fis.close();
            }
        } catch (IOException e) {
        }
        return prop;
    }

    private void saveHabitData() {
        File file = getHabitFile();
        file.getParentFile().mkdirs();

        try {
            FileOutputStream fos = new FileOutputStream(file);
            try {
                habitData.store(fos, "Record user habit data."); //$NON-NLS-1$
            } finally {
                fos.close();
            }
        } catch (IOException e) {
        }
    }

    private File getHabitFile() {
        if (habitFile == null) {
            Bundle bundle = XMindNetWorkbench.getDefault().getBundle();
            if (bundle != null) {
                File root = Platform.getStateLocation(bundle).toFile();
                habitFile = new File(root, HABIT_PROP_FILE);
            }
        }
        return habitFile;
    }

    private void recordFirstTimeStartXMindTimestamp() {
        String timestamp = habitData.getProperty(FIRST_START_TIMESTAMP);
        if (timestamp == null) {
            habitData.setProperty(FIRST_START_TIMESTAMP,
                    String.valueOf(System.currentTimeMillis()));
        }
    }

    private void recordStartXMindTimestamp() {
        String[] timestamps = loadStartXMindTimestamp();

        long currentTimestamp = System.currentTimeMillis();
        timestamps[0] = timestamps[1];
        timestamps[1] = timestamps[2];
        timestamps[2] = String.valueOf(currentTimestamp);

        habitData.setProperty(START_TIMESTAMP_1, timestamps[0]);
        habitData.setProperty(START_TIMESTAMP_2, timestamps[1]);
        habitData.setProperty(START_TIMESTAMP_3, timestamps[2]);
    }

    private String[] loadStartXMindTimestamp() {
        String[] timestamps = new String[3];
        timestamps[0] = habitData.getProperty(START_TIMESTAMP_1, ""); //$NON-NLS-1$
        timestamps[1] = habitData.getProperty(START_TIMESTAMP_2, ""); //$NON-NLS-1$
        timestamps[2] = habitData.getProperty(START_TIMESTAMP_3, ""); //$NON-NLS-1$
        return timestamps;
    }

    private void recordShowEventTimestamp(ISiteEvent event) {
        String id = event.getId();
        if (id == null || "".equals(id)) //$NON-NLS-1$
            return;

        long currentTimestamp = System.currentTimeMillis();
        habitData.setProperty(id, Long.toString(currentTimestamp));
    }

    private String getXMindID() {
        IAccountInfo accountInfo = XMindNet.getAccountInfo();
        return accountInfo == null ? null : accountInfo.getUser();
    }

    private boolean isShowTime(ISiteEvent event) {
        String prompt = event.getPrompt();
        if (prompt == null || !prompt.contains("every")) //$NON-NLS-1$
            return true;

        String id = event.getId();
        if (id == null || "".equals(id)) //$NON-NLS-1$
            return true;

        String lastShowTime = habitData.getProperty(id, ""); //$NON-NLS-1$
        if ("".equals(lastShowTime)) //$NON-NLS-1$
            return true;

        try {
            long interval = getShowInterval(prompt);
            long current = System.currentTimeMillis();
            long lastTime = Long.valueOf(lastShowTime);

            return (current - lastTime) < interval;
        } catch (Exception e) {
        }

        return true;
    }

    private long getShowInterval(String prompt) {
        try {
            int day = Integer.valueOf(prompt.replaceAll("every", "")); //$NON-NLS-1$//$NON-NLS-2$
            return day > 0 ? day * 1000 * 3600 * 24 : 0;
        } catch (Exception e) {
        }
        return 0;
    }

    public static void migrateLocalStoreFile() {
        File newLocalStoreFile = getLocalStoreFile();
        if (newLocalStoreFile.exists())
            return;

        Location instanceLocation = Platform.getInstanceLocation();
        if (instanceLocation == null)
            return;

        URL instanceURL = instanceLocation.getURL();
        if (instanceURL == null)
            return;

        try {
            instanceURL = FileLocator.toFileURL(instanceURL);
        } catch (IOException e) {
        }
        File instanceDir = new File(instanceURL.getFile());
        if (!instanceDir.exists())
            return;

        File oldLocalStoreFile = new File(new File(instanceDir, ".xmind"), //$NON-NLS-1$
                LOCAL_STORE_FILE_NAME);
        if (oldLocalStoreFile.exists()) {
            moveLocalStoreFile(oldLocalStoreFile, newLocalStoreFile);
            return;
        }

        oldLocalStoreFile = new File(instanceDir, LOCAL_STORE_FILE_NAME);
        if (oldLocalStoreFile.exists()) {
            moveLocalStoreFile(oldLocalStoreFile, newLocalStoreFile);
            return;
        }
    }

    private static void moveLocalStoreFile(File oldFile, File newFile) {
        if (newFile.getParentFile() != null) {
            newFile.getParentFile().mkdirs();
        }
        boolean moved = oldFile.renameTo(newFile);
        if (!moved) {
            XMindNetWorkbench.getDefault().getLog().log(new Status(
                    IStatus.WARNING, XMindNetWorkbench.PLUGIN_ID,
                    "Failed to migrate old site event local store file: " //$NON-NLS-1$
                            + oldFile.getAbsolutePath()));
        }
    }

}
