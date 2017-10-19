package org.xmind.ui.internal.editor;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Iterator;

import org.eclipse.core.runtime.Assert;

public final class URIParser {

    private URIParser() {
        throw new AssertionError();
    }

    public static String quote(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String unquote(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8"); //$NON-NLS-1$
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static final class QueryParameter {
        public final String name;
        public final String value;

        private QueryParameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public static Iterator<QueryParameter> iterateQueryParameters(URI uri) {
        final String query = uri.getQuery();
        return new Iterator<QueryParameter>() {

            int pos = 0;

            QueryParameter next = findNext();

            public boolean hasNext() {
                return next != null;
            }

            public QueryParameter next() {
                QueryParameter n = this.next;
                this.next = findNext();
                return n;
            }

            public void remove() {
            }

            private QueryParameter findNext() {
                if (query == null)
                    return null;

                String param;
                int sep;
                if (pos < 0)
                    return null;

                sep = query.indexOf('&', pos);
                if (sep < 0) {
                    param = query.substring(pos);
                    pos = -1;
                } else {
                    param = query.substring(pos, sep);
                    pos = sep + 1;
                }

                sep = param.indexOf('=');
                if (sep < 0) {
                    return new QueryParameter(unquote(param.substring(0)), ""); //$NON-NLS-1$
                } else {
                    return new QueryParameter(unquote(param.substring(0, sep)),
                            unquote(param.substring(sep + 1)));
                }

            }
        };
    }

    public static String getQueryParameter(URI uri, String name) {
        Assert.isNotNull(uri);
        Assert.isNotNull(name);
        Iterator<QueryParameter> it = iterateQueryParameters(uri);
        while (it.hasNext()) {
            QueryParameter param = it.next();
            if (name.equals(param.name))
                return param.value;
        }
        return null;
    }

    public static URI appendQueryParameter(URI uri, QueryParameter param) {
        return appendQueryParameter(uri, param.name, param.value);
    }

    public static URI appendQueryParameter(URI uri, String name, String value) {
        String query = uri.getQuery();
        if (name != null && name.length() > 0) {
            if (query != null && query.length() > 0) {
                query = query + "&" + quote(name); //$NON-NLS-1$
            } else {
                query = quote(name);
            }
            if (value != null && value.length() > 0) {
                query = query + "=" + quote(value); //$NON-NLS-1$
            }
        }
        try {
            return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(),
                    uri.getPort(), uri.getPath(), query, uri.getFragment());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
