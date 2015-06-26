package org.xmind.ui.internal.biggerplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;

import net.xmind.signin.IDataStore;
import net.xmind.signin.internal.XMindNetRequest;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.program.Program;
import org.osgi.framework.Bundle;
import org.xmind.ui.biggerplate.BiggerplatePlugin;
import org.xmind.ui.internal.biggerplate.jobs.CancelableJob;
import org.xmind.ui.internal.biggerplate.jobs.IJobClosedListener;
import org.xmind.ui.internal.biggerplate.utils.UrlUtils;

@SuppressWarnings("restriction")
public final class BiggerplateOauth {

    public static final String PREFERENCE_STORE_REFRESH_TOKEN_KEY = "org.xmind.ui.biggerplate.preference.refreshToken"; //$NON-NLS-1$

    public static final String PREFERENCE_STORE_USERNAME_KEY = "org.xmind.ui.biggerplate.preference.username"; //$NON-NLS-1$

    private static final String AUTH_URL = "https://accounts.biggerplate.com/oauth/auth"; //$NON-NLS-1$

    private static final String TOKEN_URL = "https://accounts.biggerplate.com/oauth/token"; //$NON-NLS-1$

    private static final String CLIENT_ID = "AzdnpEQti5wqHQhoMj7JQn6D5gVmNvCdl5SiU2oN"; //$NON-NLS-1$

    private static final String CLIENT_SECRET = "XyDWoxJGJ8RZYocuE41MyVvYMF0uvYvUmVIRYfmN"; //$NON-NLS-1$

    private static final String DATA_ACCESS_TOKEN_KEY = "access_token"; //$NON-NLS-1$

    private static final String DATA_REFRESH_TOKEN_KEY = "refresh_token"; //$NON-NLS-1$

    private static final int REDIRECT_URI_PORT = 13524;

    private static final String REDIRECT_URI = "http://localhost:" //$NON-NLS-1$
            + REDIRECT_URI_PORT + "/"; //$NON-NLS-1$

    private static ServerSocket server;

    public static String getAccessToken(Info info, CancelableJob job) {
        String refreshToken = getPreferenceStore().getString(
                PREFERENCE_STORE_REFRESH_TOKEN_KEY);

        if (refreshToken == null || refreshToken.equals("")) { //$NON-NLS-1$
            generateAccessToken(info, job);
        } else {
            refreshAccessToken(refreshToken, info, job);
        }

        return info.getString(Info.ACCESS_TOKEN);
    }

    // get code by socket.
    private static String getCode(final Info info, final CancelableJob job) {
        if (isJobCanceled(job)) {
            info.setBoolean(Info.CANCELED, true);
            return null;
        }
        final String permalink = AUTH_URL + "?client_id=" + CLIENT_ID //$NON-NLS-1$
                + "&redirect_uri=" + REDIRECT_URI //$NON-NLS-1$
                + "&response_type=code&scope=account+maps"; //$NON-NLS-1$
        Program.launch(permalink);

        Thread t = new Thread(new Runnable() {

            public void run() {
                if (!prepare(permalink, job, info)) {
                    if (server != null && !server.isClosed()) {
                        try {
                            server.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });

        if (isJobCanceled(job)) {
            info.setBoolean(Info.CANCELED, true);
            return null;
        }
        String code = null;
        Socket socket = null;
        BufferedReader br = null;
        try {
            server = new ServerSocket(REDIRECT_URI_PORT);
            t.setDaemon(true);
            t.start();

            if (job != null) {
                job.addJobClosedListener(new IJobClosedListener() {

                    public void jobClosed() {
                        if (server != null && !server.isClosed()) {
                            try {
                                info.setBoolean(Info.CANCELED, true);
                                server.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        job.removeJobClosedListener(this);
                    }
                });
            }

            socket = server.accept();

            br = new BufferedReader(new InputStreamReader(
                    socket.getInputStream()));
            String url = br.readLine();
            while (url != null) {
                code = UrlUtils.getParameter(url, "code"); //$NON-NLS-1$
                if (code != null) {
                    break;
                }
                url = br.readLine();
            }
            return code;
        } catch (IOException e) {
            e.printStackTrace();
            return code;
        } finally {
            try {
                if (br != null) {
                    br.close();
                    br = null;
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
                if (server != null) {
                    server.close();
                    server = null;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean prepare(String permalink, final CancelableJob job,
            final Info info) {
        final XMindNetRequest request = new XMindNetRequest();
        request.uri(permalink);

        if (job != null) {
            job.addJobClosedListener(new IJobClosedListener() {

                public void jobClosed() {
                    if (request != null && request.isRunning()) {
                        info.setBoolean(Info.CANCELED, true);
                        request.abort();
                    }
                    job.removeJobClosedListener(this);
                }
            });
        }

        request.get();

        int code = request.getStatusCode();
        return code == 200;
    }

    public static String generateAccessToken(final Info info) {
        generateAccessToken(info, null);
        return info.getString(Info.ACCESS_TOKEN);
    }

    private static void generateAccessToken(final Info info,
            final CancelableJob job) {
        String code = getCode(info, job);
        if (code == null) {
            if (!info.getBoolean(Info.CANCELED)) {
                Program.launch(getFullFilePath("pages/fail.html")); //$NON-NLS-1$
            }
            return;
        }

        if (isJobCanceled(job)) {
            info.setBoolean(Info.CANCELED, true);
            return;
        }

        final XMindNetRequest request = new XMindNetRequest();
        request.uri(TOKEN_URL);
        request.addParameter("grant_type", "authorization_code"); //$NON-NLS-1$ //$NON-NLS-2$
        request.addParameter("code", code); //$NON-NLS-1$
        request.addParameter("redirect_uri", REDIRECT_URI); //$NON-NLS-1$
        request.addParameter("client_id", CLIENT_ID); //$NON-NLS-1$
        request.addParameter("scope", "account+maps"); //$NON-NLS-1$ //$NON-NLS-2$
        request.addParameter("client_secret", CLIENT_SECRET); //$NON-NLS-1$

        if (job != null) {
            job.addJobClosedListener(new IJobClosedListener() {

                public void jobClosed() {
                    if (request != null && request.isRunning()) {
                        info.setBoolean(Info.CANCELED, true);
                        request.abort();
                    }
                    job.removeJobClosedListener(this);
                }
            });
        }

        request.post();

        Throwable e = request.getError();
        if (e != null) {
            e.printStackTrace();
        }

        IDataStore data = request.getData();
        if (data != null) {
            info.setProperty(Info.ACCESS_TOKEN,
                    data.getString(DATA_ACCESS_TOKEN_KEY));

            String refreshToken = data.getString(DATA_REFRESH_TOKEN_KEY);
            if (refreshToken != null && !refreshToken.equals("")) { //$NON-NLS-1$
                getPreferenceStore().setValue(
                        PREFERENCE_STORE_REFRESH_TOKEN_KEY,
                        data.getString(DATA_REFRESH_TOKEN_KEY));
                getPreferenceStore().setValue(PREFERENCE_STORE_USERNAME_KEY,
                        BiggerplateAPI.getUsername(info));
            }
        }

        if (!info.getBoolean(Info.CANCELED)) {
            String accessToken = info.getString(Info.ACCESS_TOKEN);
            if (accessToken != null && !accessToken.equals("")) { //$NON-NLS-1$
                Program.launch(getFullFilePath("pages/success.html")); //$NON-NLS-1$
            } else {
                Program.launch(getFullFilePath("pages/fail.html")); //$NON-NLS-1$
            }
        }
    }

    private static String getFullFilePath(String path) {
        Bundle bundle = Platform.getBundle(BiggerplatePlugin.PLUGIN_ID);
        Path path1 = new Path(path);
        URL url = FileLocator.find(bundle, path1, null);
        try {
            return FileLocator.toFileURL(url).toString();
        } catch (IOException e) {
            e.printStackTrace();
            return ""; //$NON-NLS-1$
        }
    }

    private static void refreshAccessToken(String refreshToken,
            final Info info, final CancelableJob job) {
        if (isJobCanceled(job)) {
            info.setBoolean(Info.CANCELED, true);
            return;
        }

        final XMindNetRequest request = new XMindNetRequest();
        request.uri(TOKEN_URL);
        request.addParameter("client_id", CLIENT_ID); //$NON-NLS-1$
        request.addParameter("redirect_uri", REDIRECT_URI); //$NON-NLS-1$
        request.addParameter("grant_type", DATA_REFRESH_TOKEN_KEY); //$NON-NLS-1$
        request.addParameter("scope", "account+maps"); //$NON-NLS-1$ //$NON-NLS-2$
        request.addParameter("client_secret", CLIENT_SECRET); //$NON-NLS-1$
        request.addParameter(DATA_REFRESH_TOKEN_KEY, refreshToken);

        if (job != null) {
            job.addJobClosedListener(new IJobClosedListener() {

                public void jobClosed() {
                    if (request != null && request.isRunning()) {
                        info.setBoolean(Info.CANCELED, true);
                        request.abort();
                    }
                    job.removeJobClosedListener(this);
                }
            });
        }

        request.post();

        IDataStore data = request.getData();
        if (data != null) {
            String accessToken = data.getString(DATA_ACCESS_TOKEN_KEY);
            info.setProperty(Info.ACCESS_TOKEN, accessToken);

            if (accessToken != null && !accessToken.equals("")) { //$NON-NLS-1$
                String newRefreshToken = data.getString(DATA_REFRESH_TOKEN_KEY);
                if (newRefreshToken != null && !newRefreshToken.equals("")) { //$NON-NLS-1$
                    getPreferenceStore()
                            .setValue(PREFERENCE_STORE_REFRESH_TOKEN_KEY,
                                    newRefreshToken);
                    getPreferenceStore().setValue(
                            PREFERENCE_STORE_USERNAME_KEY,
                            BiggerplateAPI.getUsername(info));
                }
            } else {
                getPreferenceStore().setValue(
                        PREFERENCE_STORE_REFRESH_TOKEN_KEY, ""); //$NON-NLS-1$
                getPreferenceStore()
                        .setValue(PREFERENCE_STORE_USERNAME_KEY, ""); //$NON-NLS-1$
                generateAccessToken(info, job);
            }
        }
    }

    private static IPreferenceStore getPreferenceStore() {
        return BiggerplatePlugin.getDefault().getPreferenceStore();
    }

    private static boolean isJobCanceled(CancelableJob job) {
        return job != null ? job.isCanceled() : false;
    }

}
