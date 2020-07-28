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
package org.xmind.ui.internal.protocols;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Karelun Huang
 */
public class FilePathParser {

    public static final String URI_SCHEME = "file"; //$NON-NLS-1$ 

    private static final String FILE_PROTOCOL = URI_SCHEME + ":"; //$NON-NLS-1$

    private static final String FILE_SEP = System.getProperty("file.separator"); //$NON-NLS-1$

    private static final String PROTOCOL_SEP = "//"; //$NON-NLS-1$

    private static final String PATH_SEP = "/"; //$NON-NLS-1$

    private static final String PARENT_DIR = ".."; //$NON-NLS-1$

    private static final String SAME_DIR = "."; //$NON-NLS-1$

    private static final String PARENT_DIR_SEP = PARENT_DIR + FILE_SEP;

    private static final String SAME_DIR_SEP = SAME_DIR + FILE_SEP;

    public static final String ABSTRACT_FILE_BASE = System
            .getProperty("user.home"); //$NON-NLS-1$

    private static final String WIN_NETWORK_PATH_PREFIX = "\\\\"; //$NON-NLS-1$

    static Boolean IS_WINDOWS = null;

    public static boolean isFileURI(String uri) {
        return uri != null && uri.startsWith(FILE_PROTOCOL);
    }

    public static String toPath(String uri) {
        if (uri == null)
            return null;
        try {
            uri = decode(uri, true);
        } catch (Exception e) {
        }
        String path;
        if (uri.startsWith(FILE_PROTOCOL))
            path = uri.substring(FILE_PROTOCOL.length());
        else
            path = uri;

        if (path.startsWith(PROTOCOL_SEP))
            path = path.substring(2);

        if (isWindows()) {
            path = path.replaceAll(PATH_SEP, WIN_NETWORK_PATH_PREFIX);
        }
        return path;
    }

    public static String toURI(String path, boolean relative) {
        if (path == null)
            return null;

        if (isWindows()) {
            path = path.replaceAll(WIN_NETWORK_PATH_PREFIX, PATH_SEP);
        }
        return encode(relative ? FILE_PROTOCOL + path
                : FILE_PROTOCOL + PROTOCOL_SEP + path, true);
    }

    public static boolean isPathRelative(String path) {
        return !(isWindows() && path.startsWith(WIN_NETWORK_PATH_PREFIX))
                && !new File(path).isAbsolute();
    }

    private static List<String> calculateRoutine(File file) {
        ArrayList<String> routine = new ArrayList<String>();
        String name;
        while (file != null) {
            name = file.getName();
            if (name == null || "".equals(name)) { //$NON-NLS-1$
                // A root directory may have no name, so we add its whole path:
                name = file.getPath();
            }
            routine.add(0, name);
            file = file.getParentFile();
        }
        return routine;
    }

    public static String toRelativePath(String base, String absolutePath) {
        if (absolutePath.equals(base))
            return SAME_DIR;

        List<String> baseRoutine = calculateRoutine(new File(base));
        List<String> absRoutine = calculateRoutine(new File(absolutePath));

        // Calculate the segment number of the common root:
        int start = 0;
        while (start < absRoutine.size() && start < baseRoutine.size()
                && absRoutine.get(start).equals(baseRoutine.get(start))) {
            start++;
        }
        if (start == absRoutine.size())
            // Absolute path equals base:
            return SAME_DIR;

        StringBuilder builder = new StringBuilder();
        if (start < baseRoutine.size()) {
            for (int i = start; i < baseRoutine.size(); i++) {
                builder.append(PARENT_DIR);
                builder.append(FILE_SEP);
            }
        }
        for (int i = start; i < absRoutine.size(); i++) {
            builder.append(absRoutine.get(i));
            if (i < absRoutine.size() - 1) {
                builder.append(FILE_SEP);
            }
        }
        return builder.toString();

//        File file = new File(absolutePath);
//        List<File> routine = new ArrayList<File>();
//        routine = getRoutine(file, routine);
//
//        File baseFile = new File(base);
//        List<File> baseRoutine = new ArrayList<File>();
//        baseRoutine.add(baseFile);
//        baseRoutine = getRoutine(baseFile, baseRoutine);
//        int start = findStart(routine, baseRoutine);
//        StringBuilder sb = new StringBuilder(20);
//        String sep = SEP;
//        for (int i = start; i < baseRoutine.size(); i++) {
//            sb.append(".."); //$NON-NLS-1$
//            sb.append(sep);
//        }
//        if (start == 0 && isWindows && !absolutePath.startsWith(SEP)) {
//            return sb.toString() + new File(absolutePath).getAbsolutePath();
//        } else {
//            for (int i = start; i < routine.size(); i++) {
//                sb.append(routine.get(i).getName());
//                sb.append(sep);
//            }
//            sb.append(file.getName());
//        }
//        return sb.toString();
    }

    public static String toAbsolutePath(String base, String relativePath) {
        File file = new File(base);
        while (!"".equals(relativePath)) { //$NON-NLS-1$
            if (isWindows()) {
                if (relativePath.startsWith(WIN_NETWORK_PATH_PREFIX))
                    return relativePath.substring(FILE_SEP.length());
            }

            if (relativePath.startsWith(PARENT_DIR_SEP)) {
                if (file != null)
                    file = file.getParentFile();
                relativePath = relativePath.substring(PARENT_DIR_SEP.length());
            } else if (relativePath.startsWith(SAME_DIR_SEP)) {
                relativePath = relativePath.substring(SAME_DIR_SEP.length());
            } else {
                int sepIndex = relativePath.indexOf(FILE_SEP);
                if (sepIndex < 0) {
                    if (file == null)
                        return relativePath;
                    return new File(file, relativePath).getPath();
                } else {
                    if (file == null) {
                        file = new File(relativePath.substring(0, sepIndex));
                    } else {
                        file = new File(file,
                                relativePath.substring(0, sepIndex));
                    }
                    relativePath = relativePath
                            .substring(sepIndex + FILE_SEP.length());
                }
            }
        }
        if (file == null)
            return ""; //$NON-NLS-1$
        return file.getPath();
//        try {
//            return new File(base, relativePath).getCanonicalPath();
//        } catch (IOException e) {
//            return new File(base, relativePath).getAbsolutePath();
//        }
    }

    /*
     * ECMA 3, 15.1.3 URI Handling Function Properties The following are
     * implementations of the algorithms given in the ECMA specification for the
     * hidden functions 'Encode' and 'Decode'.
     */
    private static String encode(String str, boolean fullUri) {
        byte[] utf8buf = null;
        StringBuffer sb = null;

        for (int k = 0, length = str.length(); k != length; ++k) {
            char C = str.charAt(k);
            if (encodeUnescaped(C, fullUri)) {
                if (sb != null) {
                    sb.append(C);
                }
            } else {
                if (sb == null) {
                    sb = new StringBuffer(length + 3);
                    sb.append(str);
                    sb.setLength(k);
                    utf8buf = new byte[6];
                }
                if (0xDC00 <= C && C <= 0xDFFF) {
                    throw new IllegalArgumentException();
                }
                int V;
                if (C < 0xD800 || 0xDBFF < C) {
                    V = C;
                } else {
                    k++;
                    if (k == length) {
                        throw new IllegalArgumentException();
                    }
                    char C2 = str.charAt(k);
                    if (!(0xDC00 <= C2 && C2 <= 0xDFFF)) {
                        throw new IllegalArgumentException();
                    }
                    V = ((C - 0xD800) << 10) + (C2 - 0xDC00) + 0x10000;
                }
                int L = oneUcs4ToUtf8Char(utf8buf, V);
                for (int j = 0; j < L; j++) {
                    int d = 0xff & utf8buf[j];
                    sb.append('%');
                    sb.append(toHexChar(d >>> 4));
                    sb.append(toHexChar(d & 0xf));
                }
            }
        }
        return (sb == null) ? str : sb.toString();
    }

    private static String decode(String str, boolean fullUri) {
        char[] buf = null;
        int bufTop = 0;

        for (int k = 0, length = str.length(); k != length;) {
            char C = str.charAt(k);
            if (C != '%') {
                if (buf != null) {
                    buf[bufTop++] = C;
                }
                ++k;
            } else {
                if (buf == null) {
                    // decode always compress so result can not be bigger then
                    // str.length()
                    buf = new char[length];
                    str.getChars(0, k, buf, 0);
                    bufTop = k;
                }
                int start = k;
                if (k + 3 > length)
                    throw new IllegalArgumentException();
                int B = unHex(str.charAt(k + 1), str.charAt(k + 2));
                if (B < 0)
                    throw new IllegalArgumentException();
                k += 3;
                if ((B & 0x80) == 0) {
                    C = (char) B;
                } else {
                    // Decode UTF-8 sequence into ucs4Char and encode it into
                    // UTF-16
                    int utf8Tail, ucs4Char, minUcs4Char;
                    if ((B & 0xC0) == 0x80) {
                        // First  UTF-8 should be ouside 0x80..0xBF
                        throw new IllegalArgumentException();
                    } else if ((B & 0x20) == 0) {
                        utf8Tail = 1;
                        ucs4Char = B & 0x1F;
                        minUcs4Char = 0x80;
                    } else if ((B & 0x10) == 0) {
                        utf8Tail = 2;
                        ucs4Char = B & 0x0F;
                        minUcs4Char = 0x800;
                    } else if ((B & 0x08) == 0) {
                        utf8Tail = 3;
                        ucs4Char = B & 0x07;
                        minUcs4Char = 0x10000;
                    } else if ((B & 0x04) == 0) {
                        utf8Tail = 4;
                        ucs4Char = B & 0x03;
                        minUcs4Char = 0x200000;
                    } else if ((B & 0x02) == 0) {
                        utf8Tail = 5;
                        ucs4Char = B & 0x01;
                        minUcs4Char = 0x4000000;
                    } else {
                        // First UTF-8 can not be 0xFF or 0xFE
                        throw new IllegalArgumentException();
                    }
                    if (k + 3 * utf8Tail > length)
                        throw new IllegalArgumentException();
                    for (int j = 0; j != utf8Tail; j++) {
                        if (str.charAt(k) != '%')
                            throw new IllegalArgumentException();
                        B = unHex(str.charAt(k + 1), str.charAt(k + 2));
                        if (B < 0 || (B & 0xC0) != 0x80)
                            throw new IllegalArgumentException();
                        ucs4Char = (ucs4Char << 6) | (B & 0x3F);
                        k += 3;
                    }
                    // Check for overlongs and other should-not-present codes
                    if (ucs4Char < minUcs4Char || ucs4Char == 0xFFFE
                            || ucs4Char == 0xFFFF) {
                        ucs4Char = 0xFFFD;
                    }
                    if (ucs4Char >= 0x10000) {
                        ucs4Char -= 0x10000;
                        if (ucs4Char > 0xFFFFF)
                            throw new IllegalArgumentException();
                        char H = (char) ((ucs4Char >>> 10) + 0xD800);
                        C = (char) ((ucs4Char & 0x3FF) + 0xDC00);
                        buf[bufTop++] = H;
                    } else {
                        C = (char) ucs4Char;
                    }
                }
                if (fullUri && URI_DECODE_RESERVED.indexOf(C) >= 0) {
                    for (int x = start; x != k; x++) {
                        buf[bufTop++] = str.charAt(x);
                    }
                } else {
                    buf[bufTop++] = C;
                }
            }
        }
        return (buf == null) ? str : new String(buf, 0, bufTop);
    }

    private static boolean encodeUnescaped(char c, boolean fullUri) {
        if (('A' <= c && c <= 'Z') || ('a' <= c && c <= 'z')
                || ('0' <= c && c <= '9')) {
            return true;
        }
        if ("-_.!~*'()".indexOf(c) >= 0) //$NON-NLS-1$
            return true;
        if (fullUri) {
            return URI_DECODE_RESERVED.indexOf(c) >= 0;
        }
        return false;
    }

    private static final String URI_DECODE_RESERVED = ";/?:@&=+$,#"; //$NON-NLS-1$

    /*
     * Convert one UCS-4 char and write it into a UTF-8 buffer, which must be at
     * least 6 bytes long. Return the number of UTF-8 bytes of data written.
     */
    private static int oneUcs4ToUtf8Char(byte[] utf8Buffer, int ucs4Char) {
        int utf8Length = 1;

        //JS_ASSERT(ucs4Char <= 0x7FFFFFFF);
        if ((ucs4Char & ~0x7F) == 0)
            utf8Buffer[0] = (byte) ucs4Char;
        else {
            int i;
            int a = ucs4Char >>> 11;
            utf8Length = 2;
            while (a != 0) {
                a >>>= 5;
                utf8Length++;
            }
            i = utf8Length;
            while (--i > 0) {
                utf8Buffer[i] = (byte) ((ucs4Char & 0x3F) | 0x80);
                ucs4Char >>>= 6;
            }
            utf8Buffer[0] = (byte) (0x100 - (1 << (8 - utf8Length)) + ucs4Char);
        }
        return utf8Length;
    }

    private static char toHexChar(int i) {
        if (i >> 4 != 0)
            throw new IllegalArgumentException();
        return (char) ((i < 10) ? i + '0' : i - 10 + 'A');
    }

    private static int unHex(char c) {
        if ('A' <= c && c <= 'F') {
            return c - 'A' + 10;
        } else if ('a' <= c && c <= 'f') {
            return c - 'a' + 10;
        } else if ('0' <= c && c <= '9') {
            return c - '0';
        } else {
            return -1;
        }
    }

    private static int unHex(char c1, char c2) {
        int i1 = unHex(c1);
        int i2 = unHex(c2);
        if (i1 >= 0 && i2 >= 0) {
            return (i1 << 4) | i2;
        }
        return -1;
    }

    private static boolean isWindows() {
        if (IS_WINDOWS == null) {
            // Use reflection to obtain platform information to make it 
            // easy for testing.
            //
            // Original code:
            //
            // IS_WINDOWS = Platform.OS_WIN32.equals(Platform.getOS());
            //
            try {
                Class<?> platformClass = Class
                        .forName("org.eclipse.core.runtime.Platform"); //$NON-NLS-1$
                String os = (String) platformClass.getDeclaredMethod("getOS") //$NON-NLS-1$
                        .invoke(null);
                String os_win32 = (String) platformClass
                        .getDeclaredField("OS_WIN32").get(null); //$NON-NLS-1$
                if (os_win32 != null && os != null) {
                    IS_WINDOWS = Boolean.valueOf(os_win32.equals(os));
                }
            } catch (Throwable e) {
            }
            if (IS_WINDOWS == null)
                IS_WINDOWS = Boolean.FALSE;
        }
        return IS_WINDOWS.booleanValue();
    }

    private static class PathDisambiguator {

        URI uri;

        String head;
        String neck;
        File body;

        public PathDisambiguator(URI uri, File file) {
            this.uri = uri;
            this.head = file.getName();
            this.neck = null;
            this.body = file.getParentFile();
        }

        public void increaseDepth() {
            if (body == null)
                return;
            String name = body.getName();
            body = body.getParentFile();
            if (neck == null) {
                neck = name;
            } else {
                neck = name + File.separator + neck;
            }
        }

        public boolean conflictsWith(PathDisambiguator that) {
            return that != null && this.head.equals(that.head)
                    && (this.neck == null || this.neck == that.neck
                            || (this.neck != null
                                    && this.neck.equals(that.neck)))
                    && (this.body != null || that.body != null);
        }

        @Override
        public String toString() {
            return neck == null ? head
                    : String.format("%2$s [%3$s]", File.separator, head, neck); //$NON-NLS-1$
        }
    }

    public static void calculateFileURILabels(URI[] inputURIs,
            Map<URI, String> labels) {
        List<PathDisambiguator> paths = new ArrayList<PathDisambiguator>();
        for (int i = 0; i < inputURIs.length; i++) {
            URI uri = inputURIs[i];
            if (uri == null)
                continue;
            String uriValue = uri.toString();
            if (FilePathParser.isFileURI(uriValue)) {
                String path = FilePathParser.toPath(uriValue);
                if (path != null) {
                    paths.add(new PathDisambiguator(uri, new File(path)));
                }
            }
        }

        for (int i = 1; i < paths.size(); i++) {
            PathDisambiguator path1 = paths.get(i);
            for (int j = 0; j < i; j++) {
                PathDisambiguator path2 = paths.get(j);
                while (path1.conflictsWith(path2)) {
                    path1.increaseDepth();
                    path2.increaseDepth();
                }
            }
        }

        for (PathDisambiguator path : paths) {
            labels.put(path.uri, path.toString());
        }
    }

    @SuppressWarnings("nls")
    public static void main(String[] args) {
        String absolutePath = "C:/bb/11/11/11/11/1js军队建设";
        String base = "C:/bb";
        String relativePath = toRelativePath(base, absolutePath);
        System.out.println(relativePath);
        System.out.println(isPathRelative(relativePath));
//        String absolutePath2 = toAbsolutePath(base, relativePath);
//        System.out.println(absolutePath2);
//        System.out.println("\\\\pa".startsWith(WIN_NETWORK_PATH_PREFIX));
//        String relativePath2 = toRelativePath("c:/xm/cca", "\\\\xmksd/xmak");
//        System.out.println(relativePath2 + "=======");
//        System.out.println(new File("\\\\").getParentFile());
    }
}
