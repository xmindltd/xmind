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
package org.xmind.ui.resources;

import java.awt.GraphicsEnvironment;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.xmind.ui.internal.ToolkitPlugin;

/**
 * 
 * @author Frank Shaka
 */
public class FontUtils {

    public static interface IFontNameListCallback {
        void setAvailableFontNames(List<String> fontNames);
    }

    private static List<String> availableFontNames = null;

    private static List<IFontNameListCallback> callbacks = null;

    private FontUtils() {
        throw new AssertionError();
    }

    /**
     * Get all font names available in the current graphics environment.
     * 
     * @return A string list containing all available font names
     */
    public static List<String> getAvailableFontNames() {
        if (availableFontNames == null) {
            availableFontNames = findAvailableFontNamesBySWT();
        }
        return Collections.unmodifiableList(availableFontNames != null
                ? availableFontNames : new ArrayList<String>());
    }

    private static List<String> findAvailableFontNamesBySWT() {
        Display display = Display.getCurrent();
        if (display == null)
            return null;
        FontData[] fonts = display.getFontList(null, true);
        Set<String> set = new TreeSet<String>();
        for (int i = 0; i < fonts.length; i++) {
            String name = fonts[i].getName();
            if (!name.startsWith("@")) //$NON-NLS-1$
                set.add(name);
        }
        return new ArrayList<String>(set);
    }

    public static String getAAvailableFontNameFor(String nameContainer) {
        if (nameContainer == null)
            return null;
        List<String> availableFontNames = getAvailableFontNames();
        String[] suggestedNames = nameContainer.split(","); //$NON-NLS-1$
        for (String suggestedName : suggestedNames) {
            if (availableFontNames.contains(suggestedName)) {
                return suggestedName;
            }
        }
        return null;
    }

    /**
     * Use AWT to retrieve all available font names.
     * 
     * @return font names
     * @deprecated Use {@link #findAvailableFontNamesBySWT()}.
     */
    protected static ArrayList<String> findAvailableFontNamesByAWT() {
        String[] names = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getAvailableFontFamilyNames();
        ArrayList<String> list = new ArrayList<String>(names.length);
        Display display = Display.getCurrent();
        if (display == null) {
            display = Display.getDefault();
        }
        for (String name : names) {
            FontData[] fontList = display.getFontList(name, true);
            if (fontList != null && fontList.length > 0) {
                list.add(name);
            }
        }
        list.trimToSize();
        return list;
    }

    /**
     * <p>
     * <b>NOTE: This method has been deprecated and is not recommended. Use
     * {@link #getAvailableFontNames()} instead.</b>
     * </p>
     * <p>
     * Fetch all available font names from the current graphics environment.
     * </p>
     * <p>
     * Since SWT doesn't provide any convenient API for just getting current
     * system's all available font names, we have to try acquiring a basic font
     * name list from AWT and then filter it by removing names that SWT doesn't
     * support. This process may cause some delay (maybe more than 2 seconds).
     * So a <i>callback</i> is provided to avoid making the UI thread wait too
     * long for this method to return a complete result. In this way, the whole
     * process will be performed in a new job thread, so that this method
     * returns immediately to let clients continue handling other events, and
     * when the process is over, the result is passed to the callback and cached
     * for quick access in the future.
     * </p>
     * <p>
     * If you insist in getting a result right now and care not much about the
     * delay issue, you may call {@link #getAvailableFontNames()} instead, but
     * that is NOT recommended.
     * </p>
     * 
     * @param display
     *            The display from which font names are fetched
     * @param callback
     *            A callback to handle with the result list after the 'fetching'
     *            process is over
     * @deprecated AWT API's result lacks of localized names and not fully
     *             compatible with SWT API, so use
     *             {@link #getAvailableFontNames()} instead.
     */
    public static void fetchAvailableFontNames(final Display display,
            final IFontNameListCallback callback) {
        if (callback == null)
            return;

        if (availableFontNames != null) {
            callback.setAvailableFontNames(availableFontNames);
            return;
        }

        if (display == null || display.isDisposed()) {
            return;
        }

        if (callbacks != null) {
            if (callbacks.contains(callback)) {
                return;
            }
            callbacks.add(callback);
            return;
        }

        callbacks = new ArrayList<IFontNameListCallback>();
        callbacks.add(callback);

        Job fetch = new Job(Messages.FetchFontList_jobName) {
            protected IStatus run(IProgressMonitor monitor) {
                fetchAvailableFontNames(display, callback, monitor);
                return new Status(IStatus.OK, ToolkitPlugin.PLUGIN_ID,
                        "Font Name Fetched"); //$NON-NLS-1$
            }
        };
        fetch.schedule();
    }

    private static void fetchAvailableFontNames(final Display display,
            final IFontNameListCallback callback,
            final IProgressMonitor progress) {
        if (display.isDisposed()) {
            progress.done();
            return;
        }

        if (availableFontNames != null) {
            progress.done();
            display.asyncExec(new Runnable() {
                public void run() {
                    callback.setAvailableFontNames(availableFontNames);
                }
            });
            return;
        }

        new Runnable() {
            public void run() {
                progress.beginTask(null, 10);
                progress.subTask(Messages.FetchFontNames);
                String[] names = getAllFontNames(progress);
                if (names.length == 0) {
                    availableFontNames = Collections.emptyList();
                    notifyCallbacks();
                    return;
                }
                progress.worked(1);

                progress.subTask(Messages.FilterFontList);
                filterFontList(new SubProgressMonitor(progress, 90), names);
            }

            /**
             * @param progress
             * @return
             */
            private String[] getAllFontNames(final IProgressMonitor progress) {
                final String[][] nameList = new String[1][];
                Thread th = new Thread(new Runnable() {
                    public void run() {
                        nameList[0] = GraphicsEnvironment
                                .getLocalGraphicsEnvironment()
                                .getAvailableFontFamilyNames();
                    }
                }, "Get Available Font Family Names From AWT-GraphicsEnvironment"); //$NON-NLS-1$
                th.setDaemon(true);
                th.start();
                while (nameList[0] == null) {
                    if (progress.isCanceled()) {
                        nameList[0] = new String[0];
                        break;
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        nameList[0] = new String[0];
                        break;
                    }
                }
                return nameList[0];
            }

            private void filterFontList(IProgressMonitor monitor,
                    String[] names) {
                monitor.beginTask(null, names.length);
                final ArrayList<String> list = new ArrayList<String>(
                        names.length);
                final Iterator<String> it = Arrays.asList(names).iterator();

                while (it.hasNext()) {
                    if (display.isDisposed()) {
                        progress.done();
                        return;
                    }
                    if (availableFontNames != null) {
                        notifyCallbacks();
                        return;
                    }
                    final String name = it.next();
                    progress.subTask(name);
                    display.syncExec(new Runnable() {
                        public void run() {
                            FontData[] fontList = display.getFontList(name,
                                    true);
                            if (fontList != null && fontList.length > 0) {
                                list.add(name);
                            }
                        }
                    });
                    Thread.yield();
                    progress.worked(1);
                }
                list.trimToSize();
                availableFontNames = list;
                notifyCallbacks();
            }

            private synchronized void notifyCallbacks() {
                progress.done();
                if (callbacks == null)
                    return;
                if (display.isDisposed())
                    return;
                for (int i = 0; i < callbacks.size(); i++) {
                    final IFontNameListCallback callback = callbacks.get(i);
                    if (callback != null) {
                        display.asyncExec(new Runnable() {
                            public void run() {
                                callback.setAvailableFontNames(
                                        availableFontNames);
                            }
                        });
                    }
                }
                callbacks = null;
            }

        }.run();
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getFont(String key, FontData[] fontData) {
        if (key == null) {
            key = toString(fontData);
            if (key == null)
                return null;
        }

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (!reg.hasValueFor(key) && !isDefaultKey(key)) {
            if (fontData == null) {
                fontData = toFontData(key);
                if (fontData == null)
                    return null;
            }
            reg.put(key, fontData);
        }
        return reg.get(key);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getBold(String key, FontData[] fontData) {
        if (key == null) {
            key = toString(fontData);
            if (key == null)
                return null;
        }

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (!reg.hasValueFor(key) && !isDefaultKey(key)) {
            if (fontData == null) {
                fontData = toFontData(key);
                if (fontData == null)
                    return null;
            }
            reg.put(key, fontData);
        }
        return reg.getBold(key);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getItalic(String key, FontData[] fontData) {
        if (key == null) {
            key = toString(fontData);
            if (key == null)
                return null;
        }

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (!reg.hasValueFor(key) && !isDefaultKey(key)) {
            if (fontData == null) {
                fontData = toFontData(key);
                if (fontData == null)
                    return null;
            }
            reg.put(key, fontData);
        }
        return reg.getItalic(key);
    }

    /**
     * Parses a description to a set of font data.
     * 
     * @param string
     *            a string of the description of a set of font data, e.g.
     *            "(Arial,12,bi)"
     * @return
     */
    public static FontData[] toFontData(String string) {
        if (string == null)
            return null;

        string = string.trim();
        if (string.startsWith("(") && string.endsWith(")")) { //$NON-NLS-1$ //$NON-NLS-2$
            String[] eles = string.substring(1, string.length() - 1).split(","); //$NON-NLS-1$
            if (eles.length > 0) {
                String name = eles[0].trim();
                if ("".equals(name)) { //$NON-NLS-1$
                    name = JFaceResources.getFontRegistry()
                            .getFontData(JFaceResources.DEFAULT_FONT)[0]
                                    .getName();
                }

                int size = -1;
                if (eles.length > 1) {
                    try {
                        size = Integer.parseInt(eles[1].trim());
                    } catch (Exception e) {
                    }
                }
                if (size < 0) {
                    size = JFaceResources.getFontRegistry()
                            .getFontData(JFaceResources.DEFAULT_FONT)[0]
                                    .getHeight();
                }

                int style = -1;
                if (eles.length > 2) {
                    style = SWT.NORMAL;
                    String styles = eles[2].trim().toLowerCase();
                    if (!"".equals(styles)) { //$NON-NLS-1$
                        if (styles.indexOf('b') >= 0)
                            style |= SWT.BOLD;
                        if (styles.indexOf('i') >= 0)
                            style |= SWT.ITALIC;
                    }
                }
                if (style < 0) {
                    style = JFaceResources.getFontRegistry()
                            .getFontData(JFaceResources.DEFAULT_FONT)[0]
                                    .getStyle();
                }
                FontData[] fontData = new FontData[] {
                        new FontData(name, size, style) };
                return fontData;
            }
        }
        return null;
    }

    public static String toString(FontData[] fontData) {
        if (fontData == null || fontData.length == 0)
            return null;

        return toString(fontData[0]);
    }

    public static String toString(FontData fontData) {
        int style = fontData.getStyle();
        return toString(fontData.getName(), fontData.getHeight(),
                (style & SWT.BOLD) != 0, (style & SWT.ITALIC) != 0);
    }

    public static String toString(String name, int height, boolean bold,
            boolean italic) {
        StringBuilder sb = new StringBuilder(10);
        sb.append("("); //$NON-NLS-1$
        sb.append(name);
        sb.append(","); //$NON-NLS-1$
        sb.append(height);

        if (bold || italic) {
            sb.append(","); //$NON-NLS-1$
            if (bold)
                sb.append("b"); //$NON-NLS-1$
            if (italic)
                sb.append("i"); //$NON-NLS-1$
        }
        sb.append(")"); //$NON-NLS-1$
        return sb.toString();
    }

    public static FontDescriptor scaleHeight(FontDescriptor font, float scale) {
        if (scale == 1) {
            return font;
        }
        FontData[] data = font.getFontData();

        Method getHeight = null;
        Method setHeight = null;
        boolean getHeightAccessible = false;
        boolean setHeightAccessible = false;

        try {
            getHeight = FontData.class.getDeclaredMethod("getHeightF"); //$NON-NLS-1$
            getHeightAccessible = getHeight.isAccessible();
        } catch (Exception e) {
        }
        try {
            setHeight = FontData.class.getDeclaredMethod("setHeight", //$NON-NLS-1$
                    float.class);
            setHeightAccessible = setHeight.isAccessible();
        } catch (Exception e) {
        }

        if (getHeight != null)
            getHeight.setAccessible(true);
        try {
            if (setHeight != null)
                setHeight.setAccessible(true);
            try {
                for (int i = 0; i < data.length; i++) {
                    FontData next = data[i];

                    if (getHeight != null && setHeight != null) {
                        try {
                            // try float-height methods
                            Object height = getHeight.invoke(next);
                            setHeight.invoke(next,
                                    ((Float) height).floatValue() * scale);
                            next = null;
                        } catch (Exception e) {
                        }
                    }

                    if (next != null) {
                        // fall back to integer-height methods
                        int height = next.getHeight();
                        next.setHeight((int) (height * scale));
                    }
                }
            } finally {
                if (setHeight != null)
                    setHeight.setAccessible(setHeightAccessible);
            }
        } finally {
            if (getHeight != null)
                getHeight.setAccessible(getHeightAccessible);
        }

        return FontDescriptor.createFrom(data);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getFont(String name, int size, boolean bold,
            boolean italic) {
        int style = SWT.NORMAL;
        if (bold)
            style |= SWT.BOLD;
        if (italic)
            style |= SWT.ITALIC;
        FontData fd = new FontData(name, size, style);
        String key = toString(name, size, bold, italic);
        return getFont(key, new FontData[] { fd });
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getFont(FontData fontData) {
        return getFont(null, new FontData[] { fontData });
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getFont(String key) {
        return getFont(key, null);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getFont(FontData[] fontData) {
        return getFont(null, fontData);
    }

    public static FontData[] newName(FontData[] fontData, String name) {
        if (name == null || fontData == null)
            return fontData;

        FontData[] newFontData = new FontData[fontData.length];
        for (int i = 0; i < fontData.length; i++) {
            FontData old = fontData[i];
            newFontData[i] = new FontData(name, old.getHeight(),
                    old.getStyle());
        }
        return newFontData;
    }

    public static FontData[] newHeight(FontData[] fontData, int height) {
        if (height < 0 || fontData == null)
            return fontData;
        FontData[] newFontData = new FontData[fontData.length];
        for (int i = 0; i < fontData.length; i++) {
            FontData old = fontData[i];
            newFontData[i] = new FontData(old.getName(), height,
                    old.getStyle());
        }
        return newFontData;
    }

    public static FontData[] relativeHeight(FontData[] fontData,
            int deltaHeight) {
        if (deltaHeight == 0 || fontData == null)
            return fontData;
        FontData[] newFontData = new FontData[fontData.length];
        for (int i = 0; i < fontData.length; i++) {
            FontData old = fontData[i];
            newFontData[i] = new FontData(old.getName(),
                    old.getHeight() + deltaHeight, old.getStyle());
        }
        return newFontData;
    }

    public static FontData[] style(FontData[] fontData, Boolean bold,
            Boolean italic) {
        FontData[] newFontData = new FontData[fontData.length];
        for (int i = 0; i < fontData.length; i++) {
            FontData old = fontData[i];
            int newStyle = old.getStyle();
            if (bold != null) {
                if (bold.booleanValue()) {
                    newStyle |= SWT.BOLD;
                } else {
                    newStyle &= ~SWT.BOLD;
                }
            }
            if (italic != null) {
                if (italic.booleanValue()) {
                    newStyle |= SWT.ITALIC;
                } else {
                    newStyle &= ~SWT.ITALIC;
                }
            }
            newFontData[i] = new FontData(old.getName(), old.getHeight(),
                    newStyle);
        }
        return newFontData;
    }

    public static FontData[] bold(FontData[] fontData, boolean bold) {
        return style(fontData, Boolean.valueOf(bold), null);
    }

    public static FontData[] italic(FontData[] fontData, boolean italic) {
        return style(fontData, null, Boolean.valueOf(italic));
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getNewName(Font font, String name) {
        if (font == null || name == null)
            return font;
        return getNewName(toString(font.getFontData()), name);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getNewHeight(Font font, int height) {
        if (font == null || height < 0)
            return font;
        return getNewHeight(toString(font.getFontData()), height);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getRelativeHeight(Font font, int deltaHeight) {
        if (font == null || deltaHeight == 0)
            return font;
        return getRelativeHeight(toString(font.getFontData()), deltaHeight);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getNewName(String key, String name) {
        if (key == null)
            return null;

        String newKey;
        if (name == null)
            newKey = key;
        else
            newKey = key + "@name=" + name; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (name == null)
                return reg.get(key);

            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey, newName(fontData, name));
        }
        return null;
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getNewHeight(String key, int height) {
        if (key == null)
            return null;

        String newKey;
        if (height < 0)
            newKey = key;
        else
            newKey = key + "@height=" + height; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (height < 0)
                return reg.get(key);
            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey, newHeight(fontData, height));
        }
        return null;
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getRelativeHeight(String key, int deltaHeight) {
        if (key == null)
            return null;

        String newKey;
        if (deltaHeight == 0)
            newKey = key;
        else
            newKey = key + "@height+=" + deltaHeight; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (deltaHeight == 0)
                return reg.get(key);
            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey, relativeHeight(fontData, deltaHeight));
        }
        return null;
    }

    private static boolean isDefaultKey(String key) {
        return JFaceResources.DEFAULT_FONT.equals(key)
                || JFaceResources.DIALOG_FONT.equals(key)
                || JFaceResources.HEADER_FONT.equals(key)
                || JFaceResources.TEXT_FONT.equals(key)
                || JFaceResources.BANNER_FONT.equals(key);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getBold(FontData[] fontData) {
        return getBold(null, fontData);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getBold(Font font) {
        if (font == null)
            return font;
        return getBold(font.getFontData());
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getBold(String key) {
        return getBold(key, null);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getBold(String key, int newHeight) {
        if (key == null)
            return null;

        String newKey;
        if (newHeight < 0)
            newKey = key + "@bold"; //$NON-NLS-1$
        else
            newKey = key + "@bold,height=" + newHeight; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (newHeight < 0)
                return reg.get(key);
            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey, bold(newHeight(fontData, newHeight), true));
        }
        return null;
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getBoldRelative(String key, int relativeHeight) {
        if (key == null)
            return null;

        String newKey;
        if (relativeHeight == 0)
            newKey = key + "@bold"; //$NON-NLS-1$
        else
            newKey = key + "@bold,height+=" + relativeHeight; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey,
                    bold(relativeHeight(fontData, relativeHeight), true));
        }
        return null;
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getNewStyle(String key, int newStyle) {
        if (key == null)
            return null;

        String newKey;
        if (newStyle < 0)
            newKey = key;
        else
            newKey = key + "@style=" + newStyle; //$NON-NLS-1$

        FontRegistry reg = JFaceResources.getFontRegistry();
        if (reg.hasValueFor(newKey))
            return reg.get(newKey);

        if (!reg.hasValueFor(key)) {
            FontData[] fontData = toFontData(key);
            if (fontData != null)
                reg.put(key, fontData);
        }

        if (reg.hasValueFor(key) || isDefaultKey(key)) {
            if (newStyle < 0)
                return reg.get(key);
            FontData[] fontData = reg.getFontData(key);
            return getFont(newKey, style(fontData, ((newStyle & SWT.BOLD) != 0),
                    ((newStyle & SWT.ITALIC) != 0)));
        }
        return null;
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getStyled(Font font, int newStyle) {
        if (font == null)
            return null;
        return getNewStyle(toString(font.getFontData()), newStyle);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getItalic(FontData[] fontData) {
        return getItalic(null, fontData);
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getItalic(Font font) {
        if (font == null)
            return font;
        return getItalic(font.getFontData());
    }

    /**
     * @deprecated Use {@link LocalResourceManager}
     */
    public static Font getItalic(String key) {
        return getItalic(key, null);
    }

    /**
     * This little program tests whether SWT's
     * {@link Device#getFontList(String, boolean)} can retrieve as much
     * available font names as AWT's API.
     * 
     * <p>
     * Although SWT can not retrieve the same font name list as AWT, the results
     * are fairly enough for applications that provide font names for users to
     * choose.
     * </p>
     * 
     * @param args
     */
//    public static void main(String[] args) {
//        Set<String> swtFontSet = new HashSet<String>();
//
//        System.out.println("============== SWT Fonts ============="); //$NON-NLS-1$
//        Display display = new Display();
//        FontData[] swtFonts = display.getFontList(null, true);
//        FontData[] swtFonts2 = display.getFontList(null, false);
//        System.out.println("Scalable Fonts: " + swtFonts.length); //$NON-NLS-1$
//        System.out.println("Non-scalable Fonts: " + swtFonts2.length); //$NON-NLS-1$
//        for (int i = 0; i < swtFonts.length; i++) {
//            System.out.println(swtFonts[i].getName() + " - " //$NON-NLS-1$
//                    + swtFonts[i].toString());
//            swtFontSet.add(swtFonts[i].getName());
//        }
//        for (int i = 0; i < swtFonts2.length; i++) {
//            System.out.println(swtFonts2[i].getName() + " - " //$NON-NLS-1$
//                    + swtFonts2[i].toString());
//            swtFontSet.add(swtFonts2[i].getName());
//        }
//        System.out.println("--------------------------------------"); //$NON-NLS-1$
//
//        System.out.println();
//        System.out.println();
//
//        String[] awtFonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
//                .getAvailableFontFamilyNames();
//        System.out.println("============== AWT Fonts =============="); //$NON-NLS-1$
//        for (int i = 0; i < awtFonts.length; i++) {
//            System.out.println(awtFonts[i]);
//        }
//        System.out.println("--------------------------------------"); //$NON-NLS-1$
//        Set<String> awtFontSet = new HashSet<String>(Arrays.asList(awtFonts));
//        display.dispose();
//
//        System.out.println();
//        System.out.println("SWT Fonts: " + swtFontSet.size()); //$NON-NLS-1$
//        System.out.println("AWT Fonts: " + awtFontSet.size()); //$NON-NLS-1$
//        System.out.println("Equality: " + swtFontSet.equals(awtFontSet)); //$NON-NLS-1$
//
//        Set<String> set1 = new HashSet<String>(awtFontSet);
//        set1.removeAll(swtFontSet);
//        System.out.println("Fonts Not In SWT: " + set1); //$NON-NLS-1$
//
//        Set<String> set2 = new HashSet<String>(swtFontSet);
//        set2.removeAll(awtFontSet);
//        System.out.println("Fonts Not In AWT: " + set2); //$NON-NLS-1$
//
//        System.exit(0);
//    }

}