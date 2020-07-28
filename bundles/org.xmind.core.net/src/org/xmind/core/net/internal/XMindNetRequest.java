/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 *
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL),
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details.
 *
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.net.internal;

import static org.xmind.core.net.internal.EncodingUtils.toAsciiBytes;
import static org.xmind.core.net.internal.EncodingUtils.urlEncode;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmind.core.net.IDataStore;
import org.xmind.core.net.JSONStore;
import org.xmind.core.net.http.HttpRequest;
import org.xmind.core.net.util.LinkUtils;

/**
 * @deprecated Use {@link HttpRequest} instead
 * @author Frank Shaka
 */
public class XMindNetRequest {

    public static final int HTTP_PREPARING = 0;

    public static final int HTTP_CONNECTING = 1;

    public static final int HTTP_SENDING = 2;

    public static final int HTTP_WAITING = 3;

    public static final int HTTP_RECEIVING = 4;

    public static final int HTTP_ERROR = 999;

    /* 2XX: generally "OK" */

    /**
     * HTTP Status-Code 200: OK.
     */
    public static final int HTTP_OK = HttpURLConnection.HTTP_OK;

    /**
     * HTTP Status-Code 201: Created.
     */
    public static final int HTTP_CREATED = HttpURLConnection.HTTP_CREATED;

    /**
     * HTTP Status-Code 202: Accepted.
     */
    public static final int HTTP_ACCEPTED = HttpURLConnection.HTTP_ACCEPTED;

    /**
     * HTTP Status-Code 203: Non-Authoritative Information.
     */
    public static final int HTTP_NOT_AUTHORITATIVE = HttpURLConnection.HTTP_NOT_AUTHORITATIVE;

    /**
     * HTTP Status-Code 204: No Content.
     */
    public static final int HTTP_NO_CONTENT = HttpURLConnection.HTTP_NO_CONTENT;

    /**
     * HTTP Status-Code 205: Reset Content.
     */
    public static final int HTTP_RESET = HttpURLConnection.HTTP_RESET;

    /**
     * HTTP Status-Code 206: Partial Content.
     */
    public static final int HTTP_PARTIAL = HttpURLConnection.HTTP_PARTIAL;

    /* 3XX: relocation/redirect */

    /**
     * HTTP Status-Code 300: Multiple Choices.
     */
    public static final int HTTP_MULT_CHOICE = HttpURLConnection.HTTP_MULT_CHOICE;

    /**
     * HTTP Status-Code 301: Moved Permanently.
     */
    public static final int HTTP_MOVED_PERM = HttpURLConnection.HTTP_MOVED_PERM;

    /**
     * HTTP Status-Code 302: Temporary Redirect.
     */
    public static final int HTTP_MOVED_TEMP = HttpURLConnection.HTTP_MOVED_TEMP;

    /**
     * HTTP Status-Code 303: See Other.
     */
    public static final int HTTP_SEE_OTHER = HttpURLConnection.HTTP_SEE_OTHER;

    /**
     * HTTP Status-Code 304: Not Modified.
     */
    public static final int HTTP_NOT_MODIFIED = HttpURLConnection.HTTP_NOT_MODIFIED;

    /**
     * HTTP Status-Code 305: Use Proxy.
     */
    public static final int HTTP_USE_PROXY = HttpURLConnection.HTTP_USE_PROXY;

    /* 4XX: client error */

    /**
     * HTTP Status-Code 400: Bad Request.
     */
    public static final int HTTP_BAD_REQUEST = HttpURLConnection.HTTP_BAD_REQUEST;

    /**
     * HTTP Status-Code 401: Unauthorized.
     */
    public static final int HTTP_UNAUTHORIZED = HttpURLConnection.HTTP_UNAUTHORIZED;

    /**
     * HTTP Status-Code 402: Payment Required.
     */
    public static final int HTTP_PAYMENT_REQUIRED = HttpURLConnection.HTTP_PAYMENT_REQUIRED;

    /**
     * HTTP Status-Code 403: Forbidden.
     */
    public static final int HTTP_FORBIDDEN = HttpURLConnection.HTTP_FORBIDDEN;

    /**
     * HTTP Status-Code 404: Not Found.
     */
    public static final int HTTP_NOT_FOUND = HttpURLConnection.HTTP_NOT_FOUND;

    /**
     * HTTP Status-Code 405: Method Not Allowed.
     */
    public static final int HTTP_BAD_METHOD = HttpURLConnection.HTTP_BAD_METHOD;

    /**
     * HTTP Status-Code 406: Not Acceptable.
     */
    public static final int HTTP_NOT_ACCEPTABLE = HttpURLConnection.HTTP_NOT_ACCEPTABLE;

    /**
     * HTTP Status-Code 407: Proxy Authentication Required.
     */
    public static final int HTTP_PROXY_AUTH = HttpURLConnection.HTTP_PROXY_AUTH;

    /**
     * HTTP Status-Code 408: Request Time-Out.
     */
    public static final int HTTP_CLIENT_TIMEOUT = HttpURLConnection.HTTP_CLIENT_TIMEOUT;

    /**
     * HTTP Status-Code 409: Conflict.
     */
    public static final int HTTP_CONFLICT = HttpURLConnection.HTTP_CONFLICT;

    /**
     * HTTP Status-Code 410: Gone.
     */
    public static final int HTTP_GONE = HttpURLConnection.HTTP_GONE;

    /**
     * HTTP Status-Code 411: Length Required.
     */
    public static final int HTTP_LENGTH_REQUIRED = HttpURLConnection.HTTP_LENGTH_REQUIRED;

    /**
     * HTTP Status-Code 412: Precondition Failed.
     */
    public static final int HTTP_PRECON_FAILED = HttpURLConnection.HTTP_PRECON_FAILED;

    /**
     * HTTP Status-Code 413: Request HttpEntity Too Large.
     */
    public static final int HTTP_ENTITY_TOO_LARGE = HttpURLConnection.HTTP_ENTITY_TOO_LARGE;

    /**
     * HTTP Status-Code 414: Request-URI Too Large.
     */
    public static final int HTTP_REQ_TOO_LONG = HttpURLConnection.HTTP_REQ_TOO_LONG;

    /**
     * HTTP Status-Code 415: Unsupported Media Type.
     */
    public static final int HTTP_UNSUPPORTED_TYPE = HttpURLConnection.HTTP_UNSUPPORTED_TYPE;

    /* 5XX: server error */

    /**
     * HTTP Status-Code 500: Internal Server Error.
     */
    public static final int HTTP_INTERNAL_ERROR = HttpURLConnection.HTTP_INTERNAL_ERROR;

    /**
     * HTTP Status-Code 501: Not Implemented.
     */
    public static final int HTTP_NOT_IMPLEMENTED = HttpURLConnection.HTTP_NOT_IMPLEMENTED;

    /**
     * HTTP Status-Code 502: Bad Gateway.
     */
    public static final int HTTP_BAD_GATEWAY = HttpURLConnection.HTTP_BAD_GATEWAY;

    /**
     * HTTP Status-Code 503: Service Unavailable.
     */
    public static final int HTTP_UNAVAILABLE = HttpURLConnection.HTTP_UNAVAILABLE;

    /**
     * HTTP Status-Code 504: Gateway Timeout.
     */
    public static final int HTTP_GATEWAY_TIMEOUT = HttpURLConnection.HTTP_GATEWAY_TIMEOUT;

    /**
     * HTTP Status-Code 505: HTTP Version Not Supported.
     */
    public static final int HTTP_VERSION = HttpURLConnection.HTTP_VERSION;

    private static final boolean DEBUG_ALL = Activator
            .isDebugging("/debug/http/requests"); //$NON-NLS-1$

    private static final boolean DEBUG_TO_STDOUT = DEBUG_ALL
            || Activator.isDebugging("/debug/requests/stdout"); //$NON-NLS-1$

    private static final boolean DEBUG_ASSC = Activator
            .isDebugging("/debug/requests/assc"); //$NON-NLS-1$

    private static final String DEFAULT_DOMAIN = LinkUtils.HOST_NET;

    private static final String HEAD = "HEAD"; //$NON-NLS-1$

    private static final String GET = "GET"; //$NON-NLS-1$

    private static final String POST = "POST"; //$NON-NLS-1$

    private static final String PUT = "PUT"; //$NON-NLS-1$

    private static final String DELETE = "DELETE"; //$NON-NLS-1$

    private static Set<String> WRITABLE_METHODS = new HashSet<String>(
            Arrays.asList(POST, PUT));

    protected static class NamedValue {

        public String name;

        public Object value;

        private String encodedName = null;

        private String encodedValue = null;

        public NamedValue(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public String getValue() {
            return value == null ? "" : value.toString(); //$NON-NLS-1$
        }

        public String getEncodedName() {
            if (encodedName == null) {
                encodedName = urlEncode(name);
            }
            return encodedName;
        }

        public String getEncodedValue() {
            if (encodedValue == null) {
                encodedValue = urlEncode(value);
            }
            return encodedValue;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return String.format("%s: %s", name, value); //$NON-NLS-1$
        }

    }

    protected static abstract class RequestWriter {

        private List<NamedValue> parameters;

        public void init(List<NamedValue> parameters) {
            this.parameters = parameters;
        }

        /**
         * @return the parameters
         */
        protected List<NamedValue> getParameters() {
            return parameters;
        }

        public abstract String getContentType();

        public abstract long getContentLength();

        public abstract void write(OutputStream stream) throws IOException;

    }

    protected static class FormSubmitter extends RequestWriter {

        private static final String FORM_CONTENT_TYPE = "application/x-www-form-urlencoded; charset=utf-8"; //$NON-NLS-1$

        private byte[] formData = null;

        private byte[] getFormData() {
            if (formData != null)
                return formData;
            formData = toAsciiBytes(toQueryString(getParameters()));
            return formData;
        }

        public String getContentType() {
            return FORM_CONTENT_TYPE;
        }

        public long getContentLength() {
            return getFormData().length;
        }

        public void write(OutputStream stream) throws IOException {
            stream.write(getFormData());
        }

    }

    protected static class MultipartWriter extends RequestWriter {

        private static final String MULTIPART_CONTENT_TYPE = "multipart/form-data; boundary="; //$NON-NLS-1$

        /**
         * The pool of ASCII chars to be used for generating a multipart
         * boundary.
         */
        private static final byte[] BOUNDARY_CHARS = toAsciiBytes(
                "-_1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"); //$NON-NLS-1$

        /** Carriage return/linefeed as a byte array */
        private static final byte[] CRLF = toAsciiBytes("\r\n"); //$NON-NLS-1$

        /** Content dispostion as a byte array */
        private static final byte[] QUOTE = toAsciiBytes("\""); //$NON-NLS-1$

        /** Extra characters as a byte array */
        private static final byte[] EXTRA = toAsciiBytes("--"); //$NON-NLS-1$

        /** Content dispostion as a byte array */
        private static final byte[] CONTENT_DISPOSITION = toAsciiBytes(
                "Content-Disposition: form-data; name="); //$NON-NLS-1$

        /** Content type header as a byte array */
        private static final byte[] CONTENT_TYPE = toAsciiBytes(
                "Content-Type: "); //$NON-NLS-1$

        /** Content charset as a byte array */
        private static final byte[] CHARSET = toAsciiBytes("; charset=utf-8"); //$NON-NLS-1$

        /** Content type header as a byte array */
        private static final byte[] CONTENT_TRANSFER_ENCODING = toAsciiBytes(
                "Content-Transfer-Encoding: "); //$NON-NLS-1$

        /** Attachment's file name as a byte array */
        private static final byte[] FILE_NAME = toAsciiBytes("; filename="); //$NON-NLS-1$

        private static final byte[] FILE_CONTENT_TYPE = toAsciiBytes(
                "application/octet-stream"); //$NON-NLS-1$

        private static final byte[] TEXT_CONTENT_TYPE = toAsciiBytes(
                "text/plain"); //$NON-NLS-1$

        private static final byte[] FILE_TRANSFER_ENCODING = toAsciiBytes(
                "binary"); //$NON-NLS-1$

        private static final byte[] TEXT_TRANSFER_ENCODING = toAsciiBytes(
                "8bit"); //$NON-NLS-1$

        private byte[] boundary = null;

        private byte[] getBoundary() {
            if (boundary != null)
                return boundary;
            Random rand = new Random();
            byte[] bytes = new byte[rand.nextInt(11) + 30]; // a random size
                                                           // from 30 to 40
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = BOUNDARY_CHARS[rand.nextInt(BOUNDARY_CHARS.length)];
            }
            boundary = bytes;
            return boundary;
        }

        public String getContentType() {
            StringBuffer typeBuffer = new StringBuffer(MULTIPART_CONTENT_TYPE);
            typeBuffer.append(EncodingUtils.toAsciiString(getBoundary()));
            return typeBuffer.toString();
        }

        public long getContentLength() {
            if (getParameters().isEmpty())
                return 0;
            long length = 0;
            for (NamedValue part : getParameters()) {
                length += EXTRA.length;
                length += getBoundary().length;
                length += CRLF.length;

                length += CONTENT_DISPOSITION.length;
                length += QUOTE.length;
                length += toAsciiBytes(part.getEncodedName()).length;
                length += QUOTE.length;
                length += CRLF.length;

                if (part.value instanceof File) {
                    length += FILE_NAME.length;
                    length += QUOTE.length;
                    length += toAsciiBytes(part.getEncodedName()).length;
                    length += QUOTE.length;
                }

                length += CONTENT_TYPE.length;
                length += getContentType(part.value).length;
                length += CHARSET.length;
                length += CRLF.length;

                length += CONTENT_TRANSFER_ENCODING.length;
                length += getTransferEncoding(part.value).length;
                length += CRLF.length;
                length += CRLF.length;

                length += getPartDataLength(part);

                length += CRLF.length;

            }

            length += EXTRA.length;
            length += getBoundary().length;
            length += EXTRA.length;
            length += CRLF.length;
            return length;
        }

        public void write(OutputStream stream) throws IOException {
            if (getParameters().isEmpty())
                return;

            for (NamedValue part : getParameters()) {
                stream.write(EXTRA);
                stream.write(getBoundary());
                stream.write(CRLF);

                stream.write(CONTENT_DISPOSITION);
                stream.write(QUOTE);
                stream.write(toAsciiBytes(part.getEncodedName()));
                stream.write(QUOTE);
                if (part.value instanceof File) {
                    stream.write(FILE_NAME);
                    stream.write(QUOTE);
                    stream.write(toAsciiBytes(((File) part.value).getName()));
                    stream.write(QUOTE);
                }
                stream.write(CRLF);

                stream.write(CONTENT_TYPE);
                stream.write(getContentType(part.value));
                stream.write(CHARSET);
                stream.write(CRLF);

                stream.write(CONTENT_TRANSFER_ENCODING);
                stream.write(getTransferEncoding(part.value));
                stream.write(CRLF);
                stream.write(CRLF);

                writePartData(stream, part);

                stream.write(CRLF);

                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    throw new OperationCanceledException();
                }
            }

            stream.write(EXTRA);
            stream.write(getBoundary());
            stream.write(EXTRA);
            stream.write(CRLF);
        }

        private static byte[] getContentType(Object value) {
            if (value instanceof File)
                return FILE_CONTENT_TYPE;
            return TEXT_CONTENT_TYPE;
        }

        private static byte[] getTransferEncoding(Object value) {
            if (value instanceof File)
                return FILE_TRANSFER_ENCODING;
            return TEXT_TRANSFER_ENCODING;
        }

        private static long getPartDataLength(NamedValue part) {
            if (part.value instanceof File) {
                return (int) ((File) part.value).length();
            }
            return toAsciiBytes(part.getValue()).length;
        }

        private static void writePartData(OutputStream stream, NamedValue part)
                throws IOException {
            if (part.value instanceof File) {
                writeFromFile(stream, (File) part.value);
            } else {
                writeFromText(stream, part.getValue());
            }
        }

        private static void writeFromFile(OutputStream writeStream, File file)
                throws IOException {
            FileInputStream readStream = new FileInputStream(file);
            try {
                byte[] buffer = new byte[4096];
                int bytes;
                while ((bytes = readStream.read(buffer)) >= 0) {
                    writeStream.write(buffer, 0, bytes);
                    try {
                        Thread.sleep(0);
                    } catch (InterruptedException e) {
                        throw new OperationCanceledException();
                    }
                }
            } finally {
                readStream.close();
            }
        }

        private static void writeFromText(OutputStream writeStream,
                String encodedText) throws IOException {
            // writeStream.write(toAsciiBytes(encodedText));
            writeStream.write(encodedText.getBytes("UTF-8")); //$NON-NLS-1$
        }

    }

    private boolean https;

    private String method = null;

    private String uri = null;

    private String domain = DEFAULT_DOMAIN;

    private String path = null;

    private List<NamedValue> requestHeaders = new ArrayList<NamedValue>();

    private List<NamedValue> params = new ArrayList<NamedValue>();

    private boolean multipart = false;

    private File targetFile = null;

    private int statusCode = HTTP_PREPARING;

    private String responseText = null;

    private IDataStore data = null;

    private List<NamedValue> responseHeaders = new ArrayList<NamedValue>();

    private boolean aborted = false;

    private Throwable error = null;

    private List<IRequestStatusChangeListener> statusChangeListeners = new ArrayList<IRequestStatusChangeListener>();

    private boolean debugging = DEBUG_ALL
            || System.getProperty("org.xmind.debug.httprequests") != null; //$NON-NLS-1$

    private long totalBytes = 0;

    private long transferedBytes = 0;

    private Thread runningThread = null;

    private boolean followRedirects = false;

    public XMindNetRequest() {
        this(false);
    }

    public XMindNetRequest(boolean useHTTPS) {
        this.https = useHTTPS;
    }

    /**
     * Sets the URI of this request.
     * <p>
     * Note that setting this value will override all <code>https</code> /
     * <code>domain</code> / <code>path</code> settings.
     * 
     * @param uri
     * @return
     */
    public XMindNetRequest uri(String uri) {
        this.uri = uri;
        return this;
    }

    /**
     * Sets the absolute path of this request's URI.
     * <p>
     * The <code>path</code> should start with a "/", and may contain formatting
     * tags supported by <code>java.util.Formatter</code>. The URI will be
     * formatted as "[scheme]://[domain][path]".
     * 
     * @param path
     *            the path of this request's URI
     * @param values
     *            objects to be formatted into <code>path</code>
     * @return
     */
    public XMindNetRequest path(String path, Object... values) {
        this.path = EncodingUtils.format(path, values);
        return this;
    }

    protected XMindNetRequest domain(String domain) {
        this.domain = domain;
        return this;
    }

    public XMindNetRequest useHTTPS() {
        this.https = true;
        return this;
    }

    public XMindNetRequest multipart() {
        this.multipart = true;
        return this;
    }

    public XMindNetRequest setAuthToken(String authToken) {
        return this.addHeader("AuthToken", authToken); //$NON-NLS-1$
    }

    public XMindNetRequest addHeader(String name, String value) {
        requestHeaders.add(new NamedValue(name, value));
        return this;
    }

    public XMindNetRequest addParameter(String name, Object value) {
        params.add(new NamedValue(name, value));
        return this;
    }

    public String getMethod() {
        return method;
    }

    public String getURI() {
        StringBuffer uriBuilder = new StringBuffer(50);
        if (uri != null) {
            uriBuilder.append(uri);
        } else if (path != null) {
            String domain = this.domain == null ? DEFAULT_DOMAIN : this.domain;
            if (https) {
                uriBuilder.append("https://"); //$NON-NLS-1$
            } else {
                uriBuilder.append("http://"); //$NON-NLS-1$
            }
            uriBuilder.append(domain);
            uriBuilder.append(path);
        } else {
            return null;
        }
        if (!WRITABLE_METHODS.contains(method) && !params.isEmpty()) {
            int i = uriBuilder.indexOf("?"); //$NON-NLS-1$
            if (i >= 0) {
                if (i < uriBuilder.length() - 1) {
                    uriBuilder.append('&');
                } else {
                    // append nothing if '?' is the last character.
                }
            } else {
                uriBuilder.append('?');
            }
            uriBuilder.append(toQueryString(params));
        }
        return uriBuilder.toString();
    }

    /**
     * @return the totalBytes
     */
    public long getTotalBytes() {
        return totalBytes;
    }

    /**
     * @return the transferedBytes
     */
    public long getTransferedBytes() {
        return transferedBytes;
    }

    /**
     * Set the target file where the response body will be stored.
     * <p>
     * Note that setting the target file to non-null will cause both
     * getResponseText() and getData() return null should the request succeed.
     * 
     * @param file
     *            the target file
     * @return this request
     */
    public XMindNetRequest setTargetFile(File file) {
        this.targetFile = file;
        return this;
    }

    public File getTargetFile() {
        return targetFile;
    }

    public void abort() {
        this.aborted = true;
        Thread theThread = this.runningThread;
        if (theThread != null) {
            theThread.interrupt();
        }
    }

    public boolean isAborted() {
        return aborted;
    }

    public XMindNetRequest head() {
        this.method = HEAD;
        return execute();
    }

    public XMindNetRequest get() {
        this.method = GET;
        return execute();
    }

    public XMindNetRequest put() {
        this.method = PUT;
        return execute();
    }

    public XMindNetRequest delete() {
        this.method = DELETE;
        return execute();
    }

    public XMindNetRequest post() {
        this.method = POST;
        return execute();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public IDataStore getData() {
        return data;
    }

    public String getResponseText() {
        return responseText;
    }

    public List<String> getAllResponseHeaders() {
        List<String> keys = new ArrayList<String>(responseHeaders.size());
        for (NamedValue header : responseHeaders) {
            keys.add(header.name);
        }
        return keys;
    }

    public String getResponseHeader(String name) {
        if (name != null) {
            for (NamedValue header : responseHeaders) {
                if (name.equalsIgnoreCase(header.name))
                    return header.getValue();
            }
        }
        return null;
    }

    public boolean isRunning() {
        return runningThread != null;
    }

    protected synchronized XMindNetRequest execute() {
        runningThread = Thread.currentThread();
        try {
            if (isAborted())
                return this;

            if (method == null)
                throw new IllegalStateException(
                        "Invalid HTTP Request: no method specified"); //$NON-NLS-1$

            final String uri = getURI();
            if (uri == null)
                throw new IllegalStateException(
                        "Invalid HTTP Request: no URI/path specified"); //$NON-NLS-1$

            final RequestWriter writer = createRequestWriter();
            if (isAborted())
                return this;

            Thread thread = new Thread(new Runnable() {
                public void run() {
                    executeInDaemonThread(uri, writer);
                }
            }, "XMindNetRequestConnection:" + uri); //$NON-NLS-1$
            thread.setDaemon(true);
            thread.setPriority(
                    (Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                // probably aborted by user
            }

            return this;
        } finally {
            runningThread = null;
        }
    }

    private void executeInDaemonThread(String uri, RequestWriter writer) {
        error = null;
        try {
            if (isAborted())
                return;
            debug("HTTP Request: (Prepared) %s %s\r\n%s", method, uri, //$NON-NLS-1$
                    requestHeaders);
            send(uri, writer);
            if (isAborted())
                return;
        } catch (OperationCanceledException e) {
            debug("HTTP Request: (Aborted) %s %s", method, uri); //$NON-NLS-1$
            if (!isAborted()) {
                abort();
            }
        } catch (Throwable e) {
            if (!isAborted()) {
                error = e;
                debug("HTTP Request: (Error: %s) %s %s", e, method, uri); //$NON-NLS-1$
            }
        }
    }

    protected void send(String uri, RequestWriter writer) throws IOException {
        this.data = null;
        this.responseText = null;
        this.responseHeaders.clear();

        setStatusCode(HTTP_CONNECTING);
        debug("HTTP Request: (Connecting...) %s %s", method, uri); //$NON-NLS-1$
        if (isAborted())
            throw new OperationCanceledException();

        URL url = new URL(uri);
        if (isAborted())
            throw new OperationCanceledException();

        HttpURLConnection connection;
        Proxy proxy = getProxy(uri);
        if (proxy != null) {
            debug("HTTP Request: (Applying proxy %s) %s %s", proxy, method, //$NON-NLS-1$
                    uri);
            connection = (HttpURLConnection) url.openConnection(proxy);
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        if (isAborted())
            throw new OperationCanceledException();
        if (DEBUG_ASSC) {
            assc(connection);
            if (isAborted())
                throw new OperationCanceledException();
        }

        setStatusCode(HTTP_SENDING);
        debug("HTTP Request: (Sending data...) %s %s", method, uri); //$NON-NLS-1$
        if (isAborted())
            throw new OperationCanceledException();

        try {
            while (true) {
                /// connection auto redirect don't have needed params
                connection.setInstanceFollowRedirects(followRedirects);

                connection.setDoOutput(writer != null);
                if (isAborted())
                    throw new OperationCanceledException();
                connection.setRequestMethod(method);
                if (isAborted())
                    throw new OperationCanceledException();

                if (writer != null) {
                    writer.init(params);
                }
                if (isAborted())
                    throw new OperationCanceledException();

                writeHeaders(uri, connection, writer);
                if (isAborted())
                    throw new OperationCanceledException();

                if (writer != null) {
                    writeBody(uri, connection, writer);
                }
                if (isAborted())
                    throw new OperationCanceledException();

                setStatusCode(HTTP_WAITING);
                debug("HTTP Request: (Waiting...) %s %s", method, uri); //$NON-NLS-1$
                if (isAborted())
                    throw new OperationCanceledException();

                /// auto redirect
                int responseCode = connection.getResponseCode();
                if (responseCode == HTTP_MOVED_PERM
                        || responseCode == HTTP_MOVED_TEMP) {
                    String newLocation = connection.getHeaderField("Location"); //$NON-NLS-1$
                    if (proxy != null) {
                        connection = (HttpURLConnection) new URL(newLocation)
                                .openConnection(proxy);
                    } else {
                        connection = (HttpURLConnection) new URL(newLocation)
                                .openConnection();
                    }
                    if (isAborted()) {
                        throw new OperationCanceledException();
                    }
                } else {
                    break;
                }
            }

            readResponse(uri, connection, connection.getInputStream(),
                    connection.getResponseCode());
            if (isAborted())
                throw new OperationCanceledException();
        } catch (IOException e) {
            if (isAborted())
                throw new OperationCanceledException();
            readResponse(uri, connection, connection.getErrorStream(),
                    connection.getResponseCode());
            if (isAborted())
                throw new OperationCanceledException();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    protected Proxy getProxy(String uri) {
        /*
         * Return null and let HttpURLConnection read proxy settings from system
         * properties. Note that plugin 'org.eclipse.core.net' writes its proxy
         * settings to system properties when it's activated, so if you relies
         * on this plugin to provide proxy settings please make sure to activate
         * it before making any http request.
         */
        return null;
    }

    public Throwable getError() {
        return error;
    }

    private RequestWriter createRequestWriter() {
        if (WRITABLE_METHODS.contains(method)) {
            if (multipart)
                return new MultipartWriter();
            return new FormSubmitter();
        }
        return null;
    }

    protected void writeHeaders(String uri, URLConnection connection,
            RequestWriter writer) {
        List<NamedValue> writtenHeaders = new ArrayList<NamedValue>();
        Object accept = null;
        for (NamedValue header : requestHeaders) {
            writeHeader(connection, header.name, header.getValue(),
                    writtenHeaders);
            if ("Accept".equalsIgnoreCase(header.name)) //$NON-NLS-1$
                accept = header.value;
        }

        if (accept == null || "".equals(accept)) { //$NON-NLS-1$
            writeHeader(connection, "Accept", "application/json", //$NON-NLS-1$//$NON-NLS-2$
                    writtenHeaders);
        }
        writeHeader(connection, "X-Client-ID", getClientId(), writtenHeaders); //$NON-NLS-1$
        if (writer != null) {
            writeHeader(connection, "Content-Type", //$NON-NLS-1$
                    writer.getContentType(), writtenHeaders);
            writeHeader(connection, "Content-Length", //$NON-NLS-1$
                    String.valueOf(writer.getContentLength()), writtenHeaders);
        }
        debug("HTTP Request: (Headers written) %s %s\r\n%s", method, uri, //$NON-NLS-1$
                writtenHeaders);
    }

    private String getClientId() {
        return "xmind_v3.4.1"; //$NON-NLS-1$
    }

    protected void writeHeader(URLConnection connection, String key,
            String value, List<NamedValue> headers) {
        connection.setRequestProperty(key, value);
        headers.add(new NamedValue(key, value));
    }

    private void writeBody(String uri, URLConnection connection,
            RequestWriter writer) throws IOException {
        OutputStream writeStream = connection.getOutputStream();
        if (isAborted())
            throw new OperationCanceledException();
        BufferedOutputStream bufferedWriteStream = new BufferedOutputStream(
                writeStream);
        if (isAborted())
            throw new OperationCanceledException();
        writer.write(bufferedWriteStream);
        if (isAborted())
            throw new OperationCanceledException();
        bufferedWriteStream.flush();
        writeStream.flush();
    }

    protected static String toQueryString(List<NamedValue> parameters) {
        StringBuffer buffer = new StringBuffer(parameters.size() * 15);
        for (int i = 0; i < parameters.size(); i++) {
            NamedValue param = parameters.get(i);
            if (i > 0) {
                buffer.append('&');
            }
            buffer.append(param.getEncodedName());
            buffer.append('=');
            buffer.append(param.getEncodedValue());
        }
        return buffer.toString();
    }

    protected void readResponse(String uri, URLConnection connection,
            InputStream readStream, int responseCode) throws IOException {
        this.responseText = null;
        this.data = null;
        this.responseHeaders.clear();
        if (responseCode < 0) {
            responseCode = HTTP_ERROR;
        }
        try {
            readResponseHeaders(connection);
            if (isAborted())
                throw new OperationCanceledException();
            if (responseCode == HTTP_ERROR) {
                setStatusCode(responseCode);
                debug("HTTP Response: (Unknown error) %s %s", method, uri); //$NON-NLS-1$
            } else {
                this.totalBytes = 0;
                this.transferedBytes = 0;
                String length = getResponseHeader("Content-Length"); //$NON-NLS-1$
                if (length != null) {
                    try {
                        this.totalBytes = Long.parseLong(length, 10);
                    } catch (NumberFormatException e) {
                    }
                }
                if (isAborted())
                    throw new OperationCanceledException();
                if (this.totalBytes == 0) {
                    this.totalBytes = readStream.available();
                }
                if (isAborted())
                    throw new OperationCanceledException();
                setStatusCode(HTTP_RECEIVING);
                debug("HTTP Request: (Receiving data, total %s bytes...) [%s] %s %s", //$NON-NLS-1$
                        totalBytes, responseCode, method, uri);
                if (isAborted())
                    throw new OperationCanceledException();
                if (targetFile != null && responseCode >= 200
                        && responseCode < 300) {
                    saveTargetFile(readStream);
                    setStatusCode(responseCode);
                    debug("HTTP Request: (Response) [%s] %s %s\r\n%s\r\nSaved to '%s' (%s bytes).", //$NON-NLS-1$
                            getStatusCode(), method, uri, responseHeaders,
                            targetFile.getAbsolutePath(), targetFile.length());
                } else {
                    int wrappedResponseCode = readResponseData(readStream);
                    if (wrappedResponseCode >= 100) {
                        setStatusCode(wrappedResponseCode);
                    } else {
                        setStatusCode(responseCode);
                    }
                    debug("HTTP Request: (Response) [%s] %s %s\r\n%s\r\n%s", //$NON-NLS-1$
                            responseCode, method, uri, responseHeaders,
                            responseText);
                }
            }
        } finally {
            if (statusCode < 100 && responseCode >= 100) {
                setStatusCode(responseCode);
            }
        }
    }

    /**
     * @param connection
     */
    private int readResponseData(InputStream readStream) throws IOException {
        this.responseText = readResponseText(readStream);
        if (!"".equals(this.responseText)) { //$NON-NLS-1$
            String respType = getResponseHeader("Content-Type"); //$NON-NLS-1$
            if (respType != null && respType.indexOf("application/json") >= 0) { //$NON-NLS-1$
                try {
                    this.data = new JSONStore(
                            new JSONObject(this.responseText));
                    if (this.data.has("_code")) { //$NON-NLS-1$
                        return this.data.getInt("_code"); //$NON-NLS-1$
                    }
                } catch (JSONException e) {
                    this.error = e;
                }
            }
        }
        return 0;
    }

    private String readResponseText(InputStream readStream) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream(
                Math.max((int) totalBytes, 1024));
        transfer(readStream, bytes);
        return bytes.toString("utf-8"); //$NON-NLS-1$
    }

    /**
     * @param connection
     */
    private void saveTargetFile(InputStream readStream) throws IOException {
        File file = this.targetFile;
        File dir = file.getParentFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        OutputStream fileWriteStream = new FileOutputStream(file);
        try {
            transfer(readStream, fileWriteStream);
        } finally {
            fileWriteStream.close();
        }
    }

    protected void transfer(InputStream readStream, OutputStream writeStream)
            throws IOException {
        transfer(readStream, writeStream, 1024);
    }

    protected void transfer(InputStream readStream, OutputStream writeStream,
            int bufSize) throws IOException {
        if (bufSize <= 0)
            bufSize = 1024;
        byte[] buffer = new byte[bufSize];
        int bytes;
        while ((bytes = readStream.read(buffer)) >= 0) {
            writeStream.write(buffer, 0, bytes);
            this.transferedBytes += bytes;
            if (isAborted())
                throw new OperationCanceledException();
            try {
                Thread.sleep(0);
            } catch (InterruptedException e) {
                throw new OperationCanceledException();
            }
        }
    }

    /**
     * @param connection
     */
    private void readResponseHeaders(URLConnection connection) {
        // Skip status line:
        connection.getHeaderField(0);
        // Start from 2nd header line:
        int i = 1;
        String key, value;
        while ((key = connection.getHeaderFieldKey(i)) != null) {
            value = connection.getHeaderField(i);
            responseHeaders.add(new NamedValue(key, value));
            i++;
        }
    }

    public XMindNetRequest debug() {
        this.debugging = true;
        return this;
    }

    public void addStatusChangeListener(IRequestStatusChangeListener listener) {
        statusChangeListeners.add(listener);
    }

    public void removeStatusChangeListener(
            IRequestStatusChangeListener listener) {
        statusChangeListeners.remove(listener);
    }

    protected void setStatusCode(int newStatus) {
        if (newStatus == this.statusCode)
            return;
        int oldStatus = this.statusCode;
        this.statusCode = newStatus;
        fireStatusChanged(oldStatus);
    }

    protected void fireStatusChanged(final int oldStatus) {
        final int newStatus = this.statusCode;
        IRequestStatusChangeListener[] listeners = statusChangeListeners
                .toArray(new IRequestStatusChangeListener[statusChangeListeners
                        .size()]);
        for (int i = 0; i < listeners.length; i++) {
            try {
                listeners[i].requestStatusChanged(this, oldStatus, newStatus);
            } catch (Throwable e) {
                Activator.getDefault().getLog().log(new Status(IStatus.WARNING,
                        Activator.PLUGIN_ID,
                        "Error occurred when notifying request status change.", //$NON-NLS-1$
                        e));
            }
        }
    }

    protected void debug(String format, Object... values) {
        if (!debugging)
            return;
        if (DEBUG_TO_STDOUT) {
            System.out.println(String.format(format, values));
        } else {
            Activator.log(String.format(format, values));
        }
    }

    public void setInstanceFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    private static void assc(HttpURLConnection connection) {
        try {
            TrustModifier.relaxHostChecking(connection);
        } catch (Exception e) {
            if (DEBUG_TO_STDOUT) {
                e.printStackTrace();
                System.err.println("Failed to relax host checking."); //$NON-NLS-1$
            } else {
                Activator.getDefault().getLog()
                        .log(new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                                "Failed to relax host checking.", e)); //$NON-NLS-1$
            }
        }
    }

    /**
     * A solution to accept self-signed SSL certificates.
     * <p>
     * Source came from Craig Flichel's post <a href=
     * "http://www.javacodegeeks.com/2011/12/ignoring-self-signed-certificates-in.html"
     * >Ignoring Self-Signed Certificates in Java</a> on Dec 1, 2011.
     * </p>
     * 
     * @author Craig Flichel
     */
    private static class TrustModifier {
        private static final TrustingHostnameVerifier TRUSTING_HOSTNAME_VERIFIER = new TrustingHostnameVerifier();
        private static SSLSocketFactory factory;

        /**
         * Call this with any HttpURLConnection, and it will modify the trust
         * settings if it is an HTTPS connection.
         */
        public static void relaxHostChecking(HttpURLConnection conn)
                throws KeyManagementException, NoSuchAlgorithmException,
                KeyStoreException {

            if (conn instanceof HttpsURLConnection) {
                HttpsURLConnection httpsConnection = (HttpsURLConnection) conn;
                SSLSocketFactory factory = prepFactory(httpsConnection);
                httpsConnection.setSSLSocketFactory(factory);
                httpsConnection.setHostnameVerifier(TRUSTING_HOSTNAME_VERIFIER);
            }
        }

        static synchronized SSLSocketFactory prepFactory(
                HttpsURLConnection httpsConnection)
                throws NoSuchAlgorithmException, KeyStoreException,
                KeyManagementException {

            if (factory == null) {
                SSLContext ctx = SSLContext.getInstance("TLS"); //$NON-NLS-1$
                ctx.init(null, new TrustManager[] { new AlwaysTrustManager() },
                        null);
                factory = ctx.getSocketFactory();
            }
            return factory;
        }

        private static final class TrustingHostnameVerifier
                implements HostnameVerifier {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }

        private static class AlwaysTrustManager implements X509TrustManager {
            public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                    throws CertificateException {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }

    }

}
