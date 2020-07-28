/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.net.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.xmind.core.net.Field;
import org.xmind.core.net.FieldSet;
import org.xmind.core.net.internal.Activator;
import org.xmind.core.net.internal.EncodingUtils;
import org.xmind.core.net.internal.FixedLengthInputStream;
import org.xmind.core.net.internal.LoggingOutputStream;
import org.xmind.core.net.internal.MonitoredInputStream;
import org.xmind.core.net.internal.MonitoredOutputStream;
import org.xmind.core.net.internal.TeeInputStream;
import org.xmind.core.net.internal.TeeOutputStream;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class HttpRequest {

    public static final int DEFAULT_PORT = -1;

    public static final String GET = "GET"; //$NON-NLS-1$

    public static final String POST = "POST"; //$NON-NLS-1$

    public static final String PUT = "PUT"; //$NON-NLS-1$

    public static final String DELETE = "DELETE"; //$NON-NLS-1$

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

    /**
     * Property name for connect timeout. Value should be a positive integer.
     */
    public static final String SETTING_CONNECT_TIMEOUT = "connectTimeout"; //$NON-NLS-1$

    /**
     * Property name for read timeout. Value should be a positive integer.
     */
    public static final String SETTING_READ_TIMEOUT = "readTimeout"; //$NON-NLS-1$

    private static Set<String> VALID_METHODS = new HashSet<String>(
            Arrays.asList(GET, PUT, POST, DELETE));

    private URL url;

    private String method;

    private FieldSet requestHeaders;

    private HttpEntity requestEntity;

    private FieldSet settings;

    private IResponseHandler responseHandler;

    private IFinishHandler finishHandler;

    private int statusCode;

    private String statusMessage;

    private FieldSet responseHeaders;

    private boolean debugging;

    private PrintStream logStream;

    private byte[] responseBuffer;

    public HttpRequest(URL url, String method, FieldSet headers,
            HttpEntity entity, FieldSet settings,
            IResponseHandler responseHandler) {
        Assert.isLegal(url != null);
        Assert.isLegal(method != null && VALID_METHODS.contains(method));
        this.url = url;
        this.method = method;
        this.requestHeaders = new FieldSet(headers);
        this.requestEntity = entity;
        this.settings = new FieldSet(settings);
        this.responseHandler = responseHandler;
        this.statusCode = HTTP_PREPARING;
        this.statusMessage = null;
        this.responseHeaders = new FieldSet();

        boolean forceDebugging = Activator
                .isDebugging(Activator.OPTION_HTTP_REQEUSTS);
        this.debugging = forceDebugging
                || System.getProperty(Activator.CONFIG_DEBUG_HTTP_REQUESTS,
                        null) != null;
        this.logStream = forceDebugging ? System.out : null;
        this.responseBuffer = null;
    }

    public HttpRequest(boolean https, String host, int port, String path,
            FieldSet queries, String ref, String method, FieldSet headers,
            HttpEntity entity, FieldSet settings,
            IResponseHandler responseHandler) {
        this(makeURL(https, host, port, path, queries, ref), method, headers,
                entity, settings, responseHandler);
    }

    /**
     * @return the url
     */
    public URL getURL() {
        return url;
    }

    public String getMethod() {
        return method;
    }

    /**
     * @return the requestEntity
     */
    public HttpEntity getRequestEntity() {
        return requestEntity;
    }

    /**
     * @return the requestHeaders
     */
    public FieldSet getRequestHeaders() {
        return new FieldSet(requestHeaders);
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * @return the responseHandler
     */
    public IResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public void setFinishHandler(IFinishHandler finishHandler) {
        this.finishHandler = finishHandler;
    }

    public IFinishHandler getFinishHandler() {
        return finishHandler;
    }

    public byte[] getResponseBuffer() {
        return responseBuffer;
    }

    public String getResponseAsString() {
        if (responseBuffer == null)
            return null;
        return EncodingUtils.toDefaultString(responseBuffer);
    }

    public JSONObject getResponseAsJSON() {
        if (responseBuffer == null)
            return null;
        ByteArrayInputStream input = new ByteArrayInputStream(responseBuffer);
        try {
            return new JSONObject(new JSONTokener(input));
        } catch (JSONException e) {
            // not a valid JSON object
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException e) {
            }
        }
    }

    public JSONObject getResponseAsJSONChecked()
            throws InvalidResponseValueException {
        if (responseBuffer == null)
            throw new InvalidResponseValueException("No response buffer.");
        ByteArrayInputStream input = new ByteArrayInputStream(responseBuffer);
        try {
            return new JSONObject(new JSONTokener(input));
        } catch (JSONException e) {
            // not a valid JSON object
            try {
                throw new InvalidResponseValueException(
                        NLS.bind("Illegal reponse JSON:\n{0}",
                                new String(responseBuffer, "utf-8"))); //$NON-NLS-1$
            } catch (UnsupportedEncodingException e1) {
                throw new InvalidResponseValueException(
                        "Illegal reponse JSON.");
            }

        } finally {
            try {
                input.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * @return the responseHeaders
     */
    public FieldSet getResponseHeaders() {
        return new FieldSet(responseHeaders);
    }

    public String getResponseHeader(String name) {
        return responseHeaders.getString(name);
    }

    public void execute(IProgressMonitor monitor)
            throws HttpException, InterruptedException {
        final SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        if (subMonitor.isCanceled())
            return;

        final boolean[] finished = new boolean[1];
        final Throwable[] exception = new Throwable[1];
        final HttpURLConnection[] connection = new HttpURLConnection[1];

        log("Preparing...."); //$NON-NLS-1$

        finished[0] = false;
        Thread thread = new Thread(new Runnable() {

            public void run() {
                try {
                    doExecute(subMonitor, connection);
                } catch (InterruptedException e) {
                    subMonitor.setCanceled(true);
                } catch (Throwable e) {
                    exception[0] = e;
                } finally {
                    finished[0] = true;
                }
            }
        }, "HttpRequestSession-" + method + "-" + url.toExternalForm()); //$NON-NLS-1$ //$NON-NLS-2$
        thread.setDaemon(true);
        thread.setPriority((Thread.MIN_PRIORITY + Thread.NORM_PRIORITY) / 2);
        thread.start();

        try {
            while (!finished[0] && !subMonitor.isCanceled()) {
                Thread.sleep(1);
            }
        } finally {
            if (thread != null) {
                thread.interrupt();
            }
            if (connection[0] != null) {
                connection[0].disconnect();
            }
        }

        if (subMonitor.isCanceled()) {
            log("Canceled."); //$NON-NLS-1$
            throw new InterruptedException();
        }

        if (exception[0] != null) {
            Throwable e = exception[0];
            log("Error: {0}", e); //$NON-NLS-1$
            if (e instanceof HttpException)
                throw (HttpException) e;
            throw new HttpException(this, e);
        }

        if (getStatusCode() >= 400)
            throw new HttpException(this, null);
    }

    private void doExecute(IProgressMonitor monitor,
            HttpURLConnection[] _connection)
            throws InterruptedException, IOException {
        if (monitor.isCanceled())
            return;

        log("Connecting to {0}....", url.getAuthority()); //$NON-NLS-1$
        setStatusCode(HTTP_CONNECTING, "Connecting"); //$NON-NLS-1$
        if (monitor.isCanceled())
            throw new OperationCanceledException();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        _connection[0] = connection;

        if (monitor.isCanceled())
            throw new InterruptedException();

        assc(connection);
        if (monitor.isCanceled())
            throw new InterruptedException();

        log("Sending...."); //$NON-NLS-1$
        setStatusCode(HTTP_SENDING, "Sending"); //$NON-NLS-1$
        if (monitor.isCanceled())
            throw new InterruptedException();

        try {
            while (true) {
                int connectTimeout = settings.getInt(SETTING_CONNECT_TIMEOUT,
                        -1);
                if (connectTimeout >= 0) {
                    connection.setConnectTimeout(connectTimeout);
                }

                int readTimeout = settings.getInt(SETTING_READ_TIMEOUT, -1);
                if (readTimeout >= 0) {
                    connection.setReadTimeout(readTimeout);
                }

                /// connection auto redirect don't have needed params
                connection.setInstanceFollowRedirects(false);

                connection.setDoOutput(requestEntity != null);
                if (monitor.isCanceled())
                    throw new InterruptedException();

                connection.setRequestMethod(method);
                if (monitor.isCanceled())
                    throw new InterruptedException();

                writeHeaders(connection);
                if (monitor.isCanceled())
                    throw new InterruptedException();

                writeBody(monitor, connection);
                if (monitor.isCanceled())
                    throw new InterruptedException();

                log("Waiting..."); //$NON-NLS-1$
                setStatusCode(HTTP_WAITING, "Waiting"); //$NON-NLS-1$
                if (monitor.isCanceled())
                    throw new InterruptedException();

                /// auto redirect
                int responseCode = connection.getResponseCode();
                if (responseCode == HTTP_MOVED_PERM
                        || responseCode == HTTP_MOVED_TEMP) {
                    String newLocation = connection.getHeaderField("Location"); //$NON-NLS-1$
                    connection = (HttpURLConnection) new URL(newLocation)
                            .openConnection();
                    _connection[0] = connection;

                    if (monitor.isCanceled())
                        throw new InterruptedException();
                } else {
                    break;
                }
            }

            readResponse(monitor, connection, connection.getInputStream(),
                    connection.getResponseCode(),
                    connection.getResponseMessage());
            if (monitor.isCanceled())
                throw new InterruptedException();
        } catch (SocketTimeoutException e) {
            throw e;
        } catch (IOException e) {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream == null) {
                e.printStackTrace();
                log("Error stream is NULL, response state: {0} {1}", //$NON-NLS-1$
                        connection.getResponseCode(),
                        connection.getResponseMessage());
            } else {
                readResponse(monitor, connection, errorStream,
                        connection.getResponseCode(),
                        connection.getResponseMessage());
            }
            if (monitor.isCanceled())
                throw new InterruptedException();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private void setStatusCode(int newStatus, String statusMessage) {
        this.statusCode = newStatus;
        this.statusMessage = statusMessage;
    }

    private void writeHeaders(URLConnection connection) {
        List<Field> writtenHeaders = new ArrayList<Field>();
        Object userAgent = null;
        for (Field header : requestHeaders) {
            writeHeader(connection, header.name, header.getValue(),
                    writtenHeaders);
            if ("User-Agent".equalsIgnoreCase(header.name)) { //$NON-NLS-1$
                userAgent = header.value;
            }
        }

        if (userAgent == null || "".equals(userAgent)) { //$NON-NLS-1$
            writeHeader(connection, "User-Agent", getDefaultUserAgent(), //$NON-NLS-1$
                    writtenHeaders);
        }
        if (requestEntity != null) {
            writeHeader(connection, "Content-Type", //$NON-NLS-1$
                    requestEntity.getContentType(), writtenHeaders);
            writeHeader(connection, "Content-Length", //$NON-NLS-1$
                    String.valueOf(requestEntity.getContentLength()),
                    writtenHeaders);
        }

        log("> {0} {1} HTTP/1.1", method, url.getPath()); //$NON-NLS-1$
        if (debugging) {
            for (Field header : writtenHeaders) {
                log("> {0}: {1}", header.name, //$NON-NLS-1$
                        header.value);
            }
        }
    }

    private void writeHeader(URLConnection connection, String key, String value,
            List<Field> headers) {
        connection.setRequestProperty(key, value);
        headers.add(new Field(key, value));
    }

    private static String getDefaultUserAgent() {
        String buildId = System.getProperty("org.xmind.product.buildid", //$NON-NLS-1$
                null);
        if (buildId == null || "".equals(buildId)) { //$NON-NLS-1$
            Activator p = Activator.getDefault();
            if (p != null) {
                buildId = p.getBundle().getVersion().toString();
            } else {
                buildId = "unknown"; //$NON-NLS-1$
            }
        }
        String os = System.getProperty("osgi.os", "OS"); //$NON-NLS-1$ //$NON-NLS-2$
        String arch = System.getProperty("osgi.arch", "ARCH"); //$NON-NLS-1$ //$NON-NLS-2$
        String ws = System.getProperty("osgi.ws", "WS"); //$NON-NLS-1$ //$NON-NLS-2$
        String nl = System.getProperty("osgi.nl", "NL"); //$NON-NLS-1$ //$NON-NLS-2$
        String osName = System.getProperty("os.name", "OS_NAME"); //$NON-NLS-1$ //$NON-NLS-2$
        String osArch = System.getProperty("os.arch", "OS_ARCH"); //$NON-NLS-1$ //$NON-NLS-2$
        String osVersion = System.getProperty("os.version", "OS_VERSION"); //$NON-NLS-1$ //$NON-NLS-2$
        String javaVersion = System.getProperty("java.version", "JAVA_VERSION"); //$NON-NLS-1$ //$NON-NLS-2$
        return String.format(
                "XMind/%s (%s.%s.%s; %s Arch/%s Version/%s; %s) Java/%s", //$NON-NLS-1$
                buildId, ws, os, arch, osName, osArch, osVersion, nl,
                javaVersion);
    }

    private void writeBody(IProgressMonitor monitor, URLConnection connection)
            throws InterruptedException, IOException {
        if (requestEntity == null)
            return;

        OutputStream output = new MonitoredOutputStream(
                connection.getOutputStream(), monitor);
        if (debugging && logStream != null) {
            output = new TeeOutputStream(output,
                    new LoggingOutputStream(logStream));
        }
        try {
            requestEntity.writeTo(output);
        } catch (OperationCanceledException e) {
            throw new InterruptedIOException();
        }
        if (debugging && logStream != null) {
            logStream.println();
        }
        if (monitor.isCanceled())
            throw new InterruptedException();

        output.flush();
    }

    private void readResponse(IProgressMonitor monitor,
            URLConnection connection, InputStream readStream, int responseCode,
            String responseMessage) throws InterruptedException, IOException {
        if (responseCode < 0) {
            responseCode = HTTP_ERROR;
            log("Unknown error, maybe not a valid HTTP response."); //$NON-NLS-1$
            setStatusCode(responseCode, responseMessage);
            return;
        }

        log("< HTTP/1.1 {0} {1}", responseCode, responseMessage); //$NON-NLS-1$
        setStatusCode(responseCode, responseMessage);
        if (monitor.isCanceled())
            throw new InterruptedException();

        readResponseHeaders(connection);
        if (monitor.isCanceled())
            throw new InterruptedException();

        final long totalBytes = getResponseLength(readStream);
        if (monitor.isCanceled())
            throw new InterruptedException();

        log("Receiving {0} bytes...", totalBytes); //$NON-NLS-1$

        if (totalBytes >= 0) {
            if (readStream == null)
                throw new IOException(
                        "No input stream available to read response from"); //$NON-NLS-1$
            readStream = new FixedLengthInputStream(readStream, this,
                    totalBytes);
        }

        if (readStream != null) {
            if (debugging && logStream != null) {
                readStream = new TeeInputStream(readStream,
                        new LoggingOutputStream(logStream));
            }
            ByteArrayOutputStream bufferStream = new ByteArrayOutputStream(
                    (int) totalBytes);
            readStream = new TeeInputStream(readStream, bufferStream);
            readStream = new MonitoredInputStream(readStream, monitor);

            if (responseHandler != null) {
                final HttpEntity responseEntity = new StreamedEntity(readStream,
                        totalBytes);
                try {
                    responseHandler.handleResponseEntity(monitor, this,
                            responseEntity);
                } catch (OperationCanceledException e) {
                    throw new InterruptedException();
                }
            }

            /// read until the end of stream to receive all response content
            /// TODO potential dead lock?
            byte[] temp = new byte[1024];
            while (readStream.read(temp) != -1) {
                /// do nothing and wait until the stream is consumed
            }

            responseBuffer = bufferStream.toByteArray();
        }

        if (debugging && logStream != null) {
            logStream.println();
        }

        log("Handled."); //$NON-NLS-1$

        if (finishHandler != null) {
            try {
                finishHandler.handleRequestFinished(monitor, this);
            } catch (OperationCanceledException e) {
                throw new InterruptedException();
            }
        }
    }

    private long getResponseLength(InputStream readStream) throws IOException {
        long totalBytes = -1;
        String length = getResponseHeader("Content-Length"); //$NON-NLS-1$
        if (length != null) {
            try {
                totalBytes = Long.parseLong(length, 10);
            } catch (NumberFormatException e) {
            }
        }
        return totalBytes;
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
            responseHeaders.add(key, value);
            log("< {0}: {1}", key, value); //$NON-NLS-1$
            i++;
        }
    }

    public HttpRequest debug(PrintStream logStream) {
        this.debugging = true;
        this.logStream = logStream;
        return this;
    }

    private void log(String format, Object... values) {
        if (!debugging)
            return;

        String prefix;
        if (format.startsWith(">") || format.startsWith("<")) { //$NON-NLS-1$ //$NON-NLS-2$
            prefix = ""; //$NON-NLS-1$
        } else {
            prefix = "* "; //$NON-NLS-1$
        }
        String message = prefix + MessageFormat.format(format, values);
        if (logStream != null) {
            logStream.println(message);
        } else {
            Activator.log(message);
        }
    }

    private void assc(HttpURLConnection connection) {
        if (!Activator.isDebugging(Activator.OPTION_HTTP_ASSC))
            return;

        try {
            TrustModifier.relaxHostChecking(connection);
        } catch (Exception e) {
            if (logStream != null) {
                e.printStackTrace(logStream);
            } else {
                Activator.log(e);
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

    private static final String PROTOCOL_HTTP = "http"; //$NON-NLS-1$
    private static final String PROTOCOL_HTTPS = "https"; //$NON-NLS-1$

    private static URL makeURL(boolean https, String host, int port,
            String path, FieldSet queries, String ref) {
        String protocol = https ? PROTOCOL_HTTPS : PROTOCOL_HTTP;

        String file = (path == null) ? "/" : path; //$NON-NLS-1$

        if (queries != null) {
            file = file + "?" + new FormEntity(queries).toString(); //$NON-NLS-1$
        }

        if (ref != null) {
            file = file + "#" + ref; //$NON-NLS-1$
        }

        URL url;
        try {
            url = new URL(protocol, host, port, file);
        } catch (MalformedURLException e) {
            throw new AssertionError("Failed creating HTTP URL from components", //$NON-NLS-1$
                    e);
        }
        return url;
    }

}
