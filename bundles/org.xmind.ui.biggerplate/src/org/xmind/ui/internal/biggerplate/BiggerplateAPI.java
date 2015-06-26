package org.xmind.ui.internal.biggerplate;

import java.io.File;

import net.xmind.signin.IDataStore;
import net.xmind.signin.internal.XMindNetRequest;

import org.xmind.ui.internal.biggerplate.jobs.CancelableJob;
import org.xmind.ui.internal.biggerplate.jobs.IJobClosedListener;

@SuppressWarnings("restriction")
public class BiggerplateAPI {

    private static final String UPLOAD_URL = "http://api.staging.biggerplate.com/maps"; //$NON-NLS-1$

    private static final String USER_INFO_URL = "http://api.staging.biggerplate.com/users/me"; //$NON-NLS-1$

    private static final String DATA_MAP_KEY = "map"; //$NON-NLS-1$

    private static final String DATA_USERNAME_KEY = "UserName"; //$NON-NLS-1$

    public static void submitMap(final Info info, final CancelableJob job) {
        final XMindNetRequest request = new XMindNetRequest();
        request.multipart();
        request.uri(UPLOAD_URL);
        request.addHeader("Authorization", info.getString(Info.ACCESS_TOKEN)); //$NON-NLS-1$
        request.addParameter("title", info.getString(Info.TITLE)); //$NON-NLS-1$

        String description = info.getString(Info.DESCRIPTION);
        if (description == null || description.equals("")) { //$NON-NLS-1$
            description = info.getString(Info.TITLE);
        }
        request.addParameter("description", description); //$NON-NLS-1$

        BiggerplateUploader.validateUploadFile(((File) info
                .getProperty(Info.FILE)).getAbsolutePath());
        request.addParameter("map", (File) info.getProperty(Info.FILE)); //$NON-NLS-1$

        job.addJobClosedListener(new IJobClosedListener() {

            public void jobClosed() {
                if (request != null && request.isRunning()) {
                    info.setBoolean(Info.CANCELED, true);
                    request.abort();
                }
                job.removeJobClosedListener(this);
            }
        });

        request.post();

        if (request.getError() != null) {
            request.getError().printStackTrace();
        }

        IDataStore data = request.getData();
        if (data != null) {
            info.setProperty(Info.RESULT_URL, data.getString(DATA_MAP_KEY));
        }
    }

    public static String getUsername(Info info) {
        XMindNetRequest request = new XMindNetRequest();
        request.uri(USER_INFO_URL);
        request.addHeader("Authorization", info.getString(Info.ACCESS_TOKEN)); //$NON-NLS-1$
        request.get();

        if (request.getError() != null) {
            request.getError().printStackTrace();
        }

        IDataStore data = request.getData();
        if (data != null) {
            return data.getString(DATA_USERNAME_KEY);
        } else {
            return ""; //$NON-NLS-1$
        }
    }

}
