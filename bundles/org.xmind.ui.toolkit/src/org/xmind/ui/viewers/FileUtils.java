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
package org.xmind.ui.viewers;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.internal.misc.StringMatcher;

public class FileUtils {

    private static final String FILE_PROTOCOL = "file:"; //$NON-NLS-1$

    private FileUtils() {
    }

    private static final long K = 1 << 10;
    private static final long M = 1 << 20;

    public static String fileLengthToString(long length) {
        if (length < K) {
            return Long.toString(length) + " B"; //$NON-NLS-1$
        } else if (length >= K && length < M) {
            double tmp = ((double) length) / (double) K;
            return String.format("%,.2f KB", tmp); //$NON-NLS-1$
        }
        double tmp = ((double) length) / (double) (M);
        return String.format("%,.2f MB", tmp); //$NON-NLS-1$
    }

    public static String[] list(File path) {
        return list(path, null);
    }

    public static String[] list(File path, final FilenameFilter filter) {
        return list(path, false, filter);
    }

    public static String[] list(File path, boolean extend,
            final FilenameFilter filter) {
        String pathName = path.getAbsolutePath();
        if (path.isFile() && path.exists()) {
            if (extend)
                return list(
                        new File(path.getParentFile(), path.getName() + "*"), false, filter); //$NON-NLS-1$
            if (filter == null
                    || filter.accept(path.getParentFile(), path.getName()))
                return new String[] { pathName };
        } else if (path.isDirectory() && path.exists()) {
            File[] subFiles = path.listFiles(filter);
            if (subFiles != null) {
                List<String> list = new ArrayList<String>();
                for (File subFile : subFiles) {
                    list.add(subFile.getAbsolutePath());
                }
                return list.toArray(new String[0]);
            }
        } else {
            int index = pathName.lastIndexOf(File.separatorChar);
            if (index >= 0) {
                String dirName = pathName.substring(0, index + 1);
                String filePattern = pathName.substring(index + 1,
                        pathName.length());
                if (extend && !filePattern.endsWith("*")) //$NON-NLS-1$
                    filePattern += "*"; //$NON-NLS-1$
                File dir = new File(dirName);
                if (dir.isDirectory()) {
                    final StringMatcher matcher = new StringMatcher(
                            filePattern, true, false);
                    return list(dir, false, new FilenameFilter() {
                        public boolean accept(File dir, String name) {
                            return matcher.match(name)
                                    && (filter == null || filter.accept(dir,
                                            name));
                        }
                    });
                }
            }
        }
        return new String[0];
    }

    public static boolean launch(String path) {
        File file = path.startsWith(FILE_PROTOCOL) ? new File(
                path.substring(FILE_PROTOCOL.length())) : new File(path);
        File cwd = new File(System.getProperty("user.home")); //$NON-NLS-1$
        String os = Platform.getOS();
        if (Platform.OS_WIN32.equals(os)) {
            if (launchUsingSWT(cwd, path))
                return true;
            if (file.isDirectory()) {
                if (launchUsingShell(cwd, "explorer.exe", quote(path))) //$NON-NLS-1$
                    return true;
            }
            if (launchUsingShell(cwd, "rundll32.exe", //$NON-NLS-1$
                    "SHELL32.DLL,ShellExec_RunDLL", quote(path))) //$NON-NLS-1$
                return true;
        } else if (Platform.OS_MACOSX.equals(os)) {
            if (launchUsingSWT(cwd, path))
                return true;
            if (launchUsingShell(cwd, "open", path)) //$NON-NLS-1$
                return true;
        } else if (Platform.OS_LINUX.equals(os)) {
            if (launchUsingShell(cwd, "xdg-open", path)) //$NON-NLS-1$
                return true;
            if (launchUsingShell(cwd, "gnome-open", path)) //$NON-NLS-1$
                return true;
            if (launchUsingSWT(cwd, path))
                return true;
        } else {
            if (launchUsingSWT(cwd, path))
                return true;
        }
        return false;
    }

    private static String quote(String path) {
        return "\"" + path + "\""; //$NON-NLS-1$ //$NON-NLS-2$
    }

    private static String getFullPath(File file) {
        try {
            return file.getCanonicalPath();
        } catch (IOException e) {
            return file.getAbsolutePath();
        }
    }

    private static boolean launchUsingSWT(File cwd, String path) {
        Display display = Display.getCurrent();
        if (display == null)
            return false;
        return Program.launch(path, cwd.getAbsolutePath());
    }

    private static boolean launchUsingShell(File cwd, String... args) {
        try {
            Process process = Runtime.getRuntime().exec(args, null, cwd);
            waitForProcessToStart();
            try {
                int exitCode = process.exitValue();
                return exitCode == 0;
            } catch (IllegalThreadStateException e) {
                return true;
            }
        } catch (Throwable e) {
            return false;
        }
    }

    private static final int PROCESS_CHECKING_DELAY = 100;

    private static void waitForProcessToStart() {
        Display display = Display.getCurrent();
        if (display != null) {
            final boolean[] ended = new boolean[1];
            ended[0] = false;
            display.timerExec(PROCESS_CHECKING_DELAY, new Runnable() {
                public void run() {
                    ended[0] = true;
                }
            });
            while (!ended[0] && !display.isDisposed()) {
                if (!display.readAndDispatch())
                    display.sleep();
            }
        } else {
            try {
                Thread.sleep(PROCESS_CHECKING_DELAY);
            } catch (InterruptedException e) {
            }
        }
    }

    public static boolean show(File file) {
        File cwd = new File(System.getProperty("user.home")); //$NON-NLS-1$
        String path = getFullPath(file);
        String os = Platform.getOS();
        if (Platform.OS_WIN32.equals(os)) {
            if (launchUsingShell(cwd, "explorer.exe", "/select,", quote(path))) //$NON-NLS-1$ //$NON-NLS-2$
                return true;
        } else if (Platform.OS_MACOSX.equals(os)) {
            if (launchUsingShell(cwd, "open", "-R", path)) //$NON-NLS-1$//$NON-NLS-2$
                return true;
            if (showUsingScriptForMacOSX(file))
                return true;
            if (launchUsingShell(cwd, "open", file.getParent())) //$NON-NLS-1$
                return true;
        } else if (Platform.OS_LINUX.equals(os)) {
            if (launchUsingShell(cwd, "nautilus", path)) //$NON-NLS-1$
                return true;
            if (launchUsingShell(cwd, "xdg-open", file.getParent())) //$NON-NLS-1$
                return true;
            if (launchUsingShell(cwd, "gnome-open", file.getParent())) //$NON-NLS-1$
                return true;
        }
        return launchUsingSWT(cwd, file.getParent());
    }

    private static boolean showUsingScriptForMacOSX(File file) {
        try {
            Process process = Runtime.getRuntime().exec(
                    new String[] { "/usr/bin/osascript" }); //$NON-NLS-1$
            PrintStream ps = new PrintStream(process.getOutputStream());
            try {
                ps.println("tell application \"Finder\""); //$NON-NLS-1$
                ps.println("  set fullpath to POSIX file \"" //$NON-NLS-1$
                        + file.getAbsolutePath() + "\" as text"); //$NON-NLS-1$
                ps.println("  reveal fullpath"); //$NON-NLS-1$
                ps.println("end tell"); //$NON-NLS-1$
            } finally {
                ps.close();
            }
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (IOException e) {
            return false;
        } catch (InterruptedException e) {
            return false;
        }
    }

}