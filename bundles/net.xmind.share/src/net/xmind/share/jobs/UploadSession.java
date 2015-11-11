package net.xmind.share.jobs;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.xmind.core.net.IDataStore;
import org.xmind.core.net.internal.XMindNetRequest;

import net.xmind.share.Info;
import net.xmind.share.Uploader;
import net.xmind.share.XmindSharePlugin;

/**
 * The whole process:
 * <ol>
 * <li>Retrieve upload session</li>
 * <li>Upload file</li>
 * <li>Retrieve uploading progress</li>
 * <li>Cancel uploading</li>
 * </ol>
 * 
 * @author Frank Shaka
 * 
 */
public class UploadSession {

    public static final int INITIALIZED = 0;

    public static final int PREPARING = 1;

    public static final int UPLOADING = 2;

    public static final int COMPLETED = 3;

    public static final int CANCELING = 4;

    public static final int CANCELED = 5;

    public static final int CODE_VERIFICATION_FAILURE = 1001;

    private static final boolean DEBUG = System
            .getProperty("org.xmind.debug.share") != null; //$NON-NLS-1$

    private static final String UPLOAD_API = "/_fs/mapfile/%s"; //$NON-NLS-1$
    private static final String SESSION_API = "/_fs/mapfile/%s/%s"; //$NON-NLS-1$

    private String userName;

    private String authToken;

    private String title;

    private File sourceFile;

    private String sessionId;

    private int status;

    private IStatus termination;

    private String permalink;

    private String viewLink;

    private double uploadProgress;

    private XMindNetRequest prepareRequest = null;

    private XMindNetRequest transferRequest = null;

    private XMindNetRequest retrieveProgressRequest = null;

    public UploadSession(Info info) {
        this.userName = info.getString(Info.USER_ID);
        Assert.isNotNull(this.userName);
        this.authToken = info.getString(Info.TOKEN);
        Assert.isNotNull(this.authToken);
        this.title = info.getString(Info.TITLE);
        Assert.isNotNull(this.title);
        this.sourceFile = (File) info.getProperty(Info.FILE);
        Assert.isNotNull(this.sourceFile);
        setStatus(INITIALIZED);
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public String getPermalink() {
        return permalink;
    }

    public String getViewLink() {
        return viewLink;
    }

    public double getUploadProgress() {
        return uploadProgress;
    }

    public int getStatus() {
        return status;
    }

    public IStatus getError() {
        return termination;
    }

    public boolean hasError() {
        return termination != null && !termination.isOK();
    }

    /**
     * Retrieve upload session.
     * <p>
     * [Request:]<br>
     * Url: http://www.xmindshare.com/_fs/mapfile/{USERNAME}<br>
     * Method: POST<br>
     * Headers: AuthToken<br>
     * Params: title:{TITLE}<br>
     * </p>
     * <p>
     * [Response]<br>
     * 400: Url is invalid, or the required fields not present in request.<br>
     * 401: User must signin.<br>
     * 403: User is allowed only to upload file for themselves.<br>
     * 500: Server error.<br>
     * 200: OK<br>
     * session: {SESSION}<br>
     * url: {URL}
     */
    public IStatus prepare() {
        setStatus(PREPARING);
        debug("[upload][prepare] userName=%s, authToken=%s, title=%s", //$NON-NLS-1$ 
                userName, authToken, title);

        XMindNetRequest request = new XMindNetRequest();
        if (DEBUG)
            request.debug();
        prepareRequest = request;
        request.path(UPLOAD_API, userName);
        request.setAuthToken(authToken);
        request.addParameter("title", title); //$NON-NLS-1$
        request.post();

        if (request.isAborted()) {
            setStatus(CANCELED);
            return ok();
        }

        int code = request.getStatusCode();
        IDataStore data = request.getData();

        debug("[upload][prepare] response code=%s", code); //$NON-NLS-1$
        debug("[upload][prepare] response text='%s'", //$NON-NLS-1$
                request.getResponseText());

        if (code == XMindNetRequest.HTTP_OK && data != null) {
            String sessionId = data.getString("session"); //$NON-NLS-1$
            if (sessionId == null)
                return error(XMindNetRequest.HTTP_INTERNAL_ERROR,
                        request.getResponseText());
            this.sessionId = sessionId;
            this.permalink = data.getString("url"); //$NON-NLS-1$
            String mapname = data.getString("mapname"); //$NON-NLS-1$
            if (mapname != null) {
                this.viewLink = String.format(
                        "http://www.xmind.net/xmind/map/%s/%s/%s", //$NON-NLS-1$
                        userName, authToken, mapname);
            }
            return ok();
        }

        return error(code, request.getError(), request.getResponseText());
    }

    /**
     * Upload file.
     * <p>
     * [Request]<br>
     * Url: http://www.xmindshare.com/_fs/mapfile/{USERNAME}/{SESSION}<br>
     * Method: POST<br>
     * Params: map:{MAP FILE}
     * </p>
     * <p>
     * [Response]<br>
     * 400: Url is invalid, or request is not valid file upload request, or User
     * cancel uploading process.<br>
     * 500: Server error.<br>
     * 200: OK
     */
    public IStatus transfer() {
        setStatus(UPLOADING);

        debug("[upload][transfer] userName=%s, sessionId=%s, sourceFile=%s", //$NON-NLS-1$
                userName, sessionId, sourceFile.getAbsolutePath());

//        System.out.println("About to send file: "
//                + sourceFile.getAbsolutePath());
        Uploader.validateUploadFile(sourceFile.getAbsolutePath());

        XMindNetRequest request = new XMindNetRequest().multipart();
        if (DEBUG)
            request.debug();
        transferRequest = request;
        request.path(SESSION_API, userName, sessionId);
        request.addParameter("map", sourceFile); //$NON-NLS-1$
        request.post();

        if (request.isAborted()) {
            setStatus(CANCELED);
            return ok();
        }

        int code = request.getStatusCode();
        debug("[upload][transfer] response code=%s", code); //$NON-NLS-1$
        debug("[upload][transfer] response text=%s", request.getResponseText()); //$NON-NLS-1$

        if (code == XMindNetRequest.HTTP_OK)
            return ok();

        return error(code, request.getError(), request.getResponseText());

    }

    /**
     * Retrieve uploading progress.
     * <p>
     * [Request]<br>
     * Url: http://www.xmindshare.com/_fs/mapfile/{USERNAME}/{SESSION}<br>
     * Method: GET<br>
     * Headers: AuthToken
     * </p>
     * <p>
     * [Response]<br>
     * 400: Url is invalid<br>
     * 403: User is allowed only view its own file uploading progress.<br>
     * 404: Request file uploading progress is not found, maybe finished,
     * canceled or no exist.<br>
     * 200: OK<br>
     * status: <br>
     * read: {READ}<br>
     * total: {TOTAL}<br>
     * items: {ITEMS}<br>
     * progress: {PROGRESS} // 0 ~ 1
     * 
     * @return
     */
    public IStatus retrieveProgress() {
        debug("[upload][progress] userName=%s, sessionId=%s", //$NON-NLS-1$
                userName, sessionId);

        XMindNetRequest request = new XMindNetRequest();
        if (DEBUG)
            request.debug();
        retrieveProgressRequest = request;
        request.path(SESSION_API, userName, sessionId);
        request.setAuthToken(authToken);
        request.get();

        if (request.isAborted()) {
            setStatus(CANCELED);
            return ok();
        }

        int code = request.getStatusCode();
        IDataStore data = request.getData();
        debug("[upload][progress] response code=%s", code); //$NON-NLS-1$
        debug("[upload][progress] response text=%s", request.getResponseText()); //$NON-NLS-1$

        if (code == XMindNetRequest.HTTP_OK && data != null) {
            String status = data.getString("status"); //$NON-NLS-1$
            if (status != null) {
                debug("[upload][progress] status=%s", status); //$NON-NLS-1$
                if ("forbidden".equals(status)) { //$NON-NLS-1$
                    return error(CODE_VERIFICATION_FAILURE,
                            request.getResponseText());
                } else if ("error".equals(status)) { //$NON-NLS-1$
                    return error(XMindNetRequest.HTTP_INTERNAL_ERROR,
                            request.getResponseText());
                } else if ("finished".equals(status)) { //$NON-NLS-1$
                    setStatus(COMPLETED);
                    return ok();
                }
            }
            uploadProgress = data.getDouble("progress"); //$NON-NLS-1$
            return ok();
        }

        return error(code, request.getError(), request.getResponseText());
    }

    /**
     * Cancel uploading.
     * <p>
     * [Request]<br>
     * Url: http://www.xmindshare.com/_fs/mapfile/{USERNAME}/{SESSION}<br>
     * Method: DELETE<br>
     * Headers: AuthToken
     * </p>
     * <p>
     * [Response]<br>
     * 400: Url is invalid<br>
     * 403: User is allowed only to cancel his own file<br>
     * upload process.<br>
     * 200: OK
     * 
     */
    public IStatus cancel() {
        if (prepareRequest != null)
            prepareRequest.abort();
        if (transferRequest != null)
            transferRequest.abort();
        if (retrieveProgressRequest != null)
            retrieveProgressRequest.abort();

        debug("[upload][cancel] userName=%s, sessionId=%s", //$NON-NLS-1$
                userName, sessionId);

        if (sessionId == null)
            return Status.CANCEL_STATUS;

        setStatus(CANCELING);
        XMindNetRequest request = new XMindNetRequest();
        if (DEBUG)
            request.debug();
        request.path(SESSION_API, userName, sessionId);
        request.setAuthToken(authToken);
        request.delete();

        if (request.isAborted()) {
            setStatus(CANCELED);
            return ok();
        }

        int code = request.getStatusCode();
        debug("[upload][cancel] response code=%s", code); //$NON-NLS-1$
        debug("[upload][cancel] response text=%s", request.getResponseText()); //$NON-NLS-1$

        if (code == XMindNetRequest.HTTP_OK) {
            setStatus(CANCELED);
            return ok();
        }

        return error(code, request.getError(), request.getResponseText());
    }

    private void setStatus(int status) {
        this.status = status;
    }

    private IStatus ok() {
        if (termination == null)
            termination = Status.OK_STATUS;
        return termination;
    }

    private IStatus error(int code, String message) {
        return error(code, null, message);
    }

    private IStatus error(int code, Throwable error, String message) {
        setError(code, error, message);
        return termination;
    }

    private void setError(int code, Throwable err, String message) {
        if (hasError() || status == COMPLETED || status == CANCELED)
            return;
        termination = createTermination(code, err, message);
    }

    private static IStatus createTermination(int code, Throwable err,
            String message) {
        return new Status(IStatus.ERROR, XmindSharePlugin.PLUGIN_ID, code,
                message, err);
    }

    private static void debug(String message, Object... values) {
        if (DEBUG) {
            XmindSharePlugin.log(String.format(message, values));
        }
    }

}
