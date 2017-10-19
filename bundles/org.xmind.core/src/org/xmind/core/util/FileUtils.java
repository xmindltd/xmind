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
package org.xmind.core.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.xmind.core.CoreException;
import org.xmind.core.IFileEntryFilter;
import org.xmind.core.io.IInputSource;
import org.xmind.core.io.IOutputTarget;
import org.xmind.core.io.IStorage;

/**
 * @author briansun
 * 
 */
public class FileUtils {

    /**
     * @param f
     * @return
     */
    public static File ensureFileParent(File f) {
        ensureDirectory(f.getParentFile());
        return f;
    }

    /**
     * @param dir
     * @return
     */
    public static File ensureDirectory(File dir) {
        if (!dir.exists())
            dir.mkdirs();
        return dir;
    }

    public static void copy(String src, String dest) throws IOException {
        FileInputStream is = new FileInputStream(src);
        FileOutputStream os = new FileOutputStream(dest);
        transfer(is, os, true);
    }

    public static void copy(File src, File dest) throws IOException {
        FileInputStream is = new FileInputStream(src);
        FileOutputStream os = new FileOutputStream(dest);
        transfer(is, os, true);
    }

    public static void transfer(IStorage oldStorage, IStorage newStorage)
            throws IOException, CoreException {
        transfer(oldStorage, newStorage, null);
    }

    public static void transfer(IStorage oldStorage, IStorage newStorage,
            IFileEntryFilter filter) throws IOException, CoreException {
        IInputSource inSource = oldStorage.getInputSource();
        IOutputTarget outTarget = newStorage.getOutputTarget();
        transfer(inSource, outTarget, filter);
    }

    public static void transfer(IInputSource inputSource,
            IOutputTarget outputTarget) throws IOException {
        transfer(inputSource, outputTarget, null);
    }

    public static void transfer(IInputSource inputSource,
            IOutputTarget outputTarget, IFileEntryFilter filter)
                    throws IOException {
        Iterator<String> entries = inputSource.getEntries();
        while (entries.hasNext()) {
            String entryPath = entries.next();
            if ((filter == null || filter.select(entryPath, null, false))) {
                if (inputSource.hasEntry(entryPath)
                        && inputSource.isEntryAvailable(entryPath)) {
                    InputStream is = inputSource.openEntryStream(entryPath);
                    try {
                        OutputStream os = outputTarget
                                .openEntryStream(entryPath);
                        try {
                            transfer(is, os);
                        } finally {
                            os.close();
                        }
                    } finally {
                        is.close();
                    }
                }
            }
        }
    }

    public static void transfer(IInputSource source, String sourcePath,
            IOutputTarget target, String targetPath) throws IOException {
        InputStream input = source.openEntryStream(sourcePath);
        try {
            OutputStream output = target.openEntryStream(targetPath);
            try {
                transfer(input, output, false);
            } finally {
                output.close();
            }
        } finally {
            input.close();
        }
    }

    public static void transfer(InputStream is, OutputStream os)
            throws IOException {
        transfer(is, os, true, null);
    }

    public static void transfer(InputStream is, OutputStream os,
            boolean closeOnFinish) throws IOException {
        transfer(is, os, closeOnFinish, null);
    }

    public static void transfer(InputStream is, OutputStream os,
            boolean closeOnFinish, String taskName) throws IOException {
        try {
            byte[] buffer = new byte[4096];
            int numRead;
            while ((numRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, numRead);
            }
        } finally {
            if (closeOnFinish) {
                try {
                    is.close();
                } finally {
                    os.close();
                }
            }
        }
    }

    public static void transfer(File source, File target) throws IOException {
        ensureFileParent(target);
        InputStream in = new FileInputStream(source);
        try {
            OutputStream out = new FileOutputStream(target);
            try {
                transfer(in, out, false);
            } finally {
                out.close();
            }
        } finally {
            in.close();
        }
    }

    /**
     * Deletes the given file and, if it is a directory, delete all its
     * sub-directories and sub-files.
     * 
     * @param f
     *            The file or directory to delete
     * @return Whether the given file or directory is successfully deleted.
     */
    public static boolean delete(File f) {
        if (f.isFile())
            return f.delete();
        else if (f.isDirectory()) {
            boolean b = clearDir(f);
            b &= f.delete();
            return b;
        } else
            return false;
    }

    /**
     * Deletes all sub-files and sub-directories in the given directory.
     * 
     * @param dir
     *            The directory to clear
     * @return Whether the given directory is successfully cleared.
     */
    public static boolean clearDir(File dir) {
        if (!dir.isDirectory())
            return false;
        File[] files = dir.listFiles();
        if (files == null || files.length == 0)
            return true;
        boolean cleared = true;
        for (File sub : files) {
            cleared &= delete(sub);
        }
        return cleared;
    }

    /**
     * Determines corresponding media type of the given path.
     * 
     * @param path
     * @return
     */
    public static String getMediaType(String path) {
        if (path != null) {
            String ext = getExtension(path);
            if (".jpg".equals(ext) || ".jpeg".equals(ext)) //$NON-NLS-1$ //$NON-NLS-2$
                return "image/jpeg"; //$NON-NLS-1$
            if (".png".equals(ext)) //$NON-NLS-1$
                return "image/png"; //$NON-NLS-1$
            if (".bmp".equals(ext)) //$NON-NLS-1$
                return "image/bmp"; //$NON-NLS-1$
            if (".gif".equals(ext)) //$NON-NLS-1$
                return "image/gif"; //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }

    private static Pattern FileNamePattern = null;

    public static String getFileName(String fullPath) {
        if (FileNamePattern == null)
            FileNamePattern = Pattern.compile("([^/\\\\]*)[/|\\\\]?$"); //$NON-NLS-1$
        Matcher m = FileNamePattern.matcher(fullPath);
        if (m.find())
            return m.group(1);
        return fullPath;
    }

    /**
     * Returns the extension part of a file path, e.g., <code>.jpg</code>,
     * <code>.html</code>. If the file does not have an extension, an empty
     * string is returned.
     * 
     * @param fullPath
     * @return
     */
    public static String getExtension(String fullPath) {
        String fileName = getFileName(fullPath);
        int i = fileName.lastIndexOf('.');
        if (i >= 0)
            return fileName.substring(i);
        return ""; //$NON-NLS-1$
    }

    public static String getNoExtensionFileName(String fullPath) {
        String fileName = getFileName(fullPath);
        int i = fileName.lastIndexOf('.');
        if (i >= 0)
            return fileName.substring(0, i);
        return fileName;
    }

    public static void extractZipFile(String filename, IOutputTarget target)
            throws IOException {
        extractZipFile(filename, target, null);
    }

    public static void extractZipFile(String filename, IOutputTarget target,
            IFileEntryFilter filter) throws IOException {
        FileInputStream fin = new FileInputStream(filename);
        try {
            ZipInputStream zin = new ZipInputStream(
                    new BufferedInputStream(fin));
            try {
                extractZipFile(zin, target, filter);
            } finally {
                zin.close();
            }
        } finally {
            fin.close();
        }
    }

    public static void extractZipFile(File file, IOutputTarget target)
            throws IOException {
        extractZipFile(file, target, null);
    }

    public static void extractZipFile(File file, IOutputTarget target,
            IFileEntryFilter filter) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        try {
            ZipInputStream zin = new ZipInputStream(
                    new BufferedInputStream(fin));
            try {
                extractZipFile(zin, target, filter);
            } finally {
                zin.close();
            }
        } finally {
            fin.close();
        }
    }

    public static void extractZipFile(ZipInputStream zin, IOutputTarget target)
            throws IOException {
        extractZipFile(zin, target, null);
    }

    public static void extractZipFile(ZipInputStream zin, IOutputTarget target,
            IFileEntryFilter filter) throws IOException {
        ZipEntry entry;
        while ((entry = zin.getNextEntry()) != null) {
            String entryPath = entry.getName();
            if (!entry.isDirectory() && (filter == null
                    || filter.select(entryPath, null, false))) {
                if (target.isEntryAvaialble(entryPath)) {
                    OutputStream out = target.openEntryStream(entryPath);
                    try {
                        FileUtils.transfer(zin, out, false);
                    } finally {
                        try {
                            out.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
        }
    }

    public static void extractZipStream(InputStream stream,
            IOutputTarget target) throws IOException {
        extractZipStream(stream, target, null);
    }

    public static void extractZipStream(InputStream stream,
            IOutputTarget target, IFileEntryFilter filter) throws IOException {
        extractZipFile(new ZipInputStream(stream), target, filter);
    }

}