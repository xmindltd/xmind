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
package org.xmind.ui.util;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * @author frankshaka
 */
public class ResourceFinder {

    //public static String DEFAULT_NL = "en_US"; //$NON-NLS-1$

    //public static String DEFAULT_PROVIDER = "brainy"; //$NON-NLS-1$

    //private static final String NL1_PLUGIN_ID = MindMapUI.PLUGIN_ID + ".nl1"; //$NON-NLS-1$

    private static final String SEP = "/"; //$NON-NLS-1$

    private static final String LINK = "_"; //$NON-NLS-1$

    private static final char Link = '_';

    protected ResourceFinder() {
    }

    /**
     * Find resource by the given path and the platform language.
     * 
     * @param mainPath
     * @param prefix
     * @param suffix
     * @return
     */
    public static URL findResource(String bundleName, String mainPath,
            String prefix, String suffix) {
        return findResource(bundleName, mainPath, prefix, suffix, Platform
                .getNL());
    }

    /**
     * Find the resource associated to the given path and the given language.
     * This method will look into both this plugin and the NL plugins.
     * <p>
     * If the desired resource is defined as:
     * </p>
     * 
     * <pre>
     * String mainPath = &quot;/markers/&quot;;   // same as &quot;markers&quot;, &quot;/markers&quot;, &quot;markers/&quot;
     * String prefix = &quot;markerSheet&quot;;
     * String suffix = &quot;.properties&quot;;
     * String nl = &quot;zh_CN&quot;;
     * URL resourceUrl = findResource(mainPath, prefix, suffix, nl);
     * ....
     * </pre>
     * <p>
     * Then the following possible resource paths will be checked from NL
     * plugins to this plugin:
     * <ul>
     * <li>/markers/zh/CN/markerSheet.properties</li>
     * <li>/markers/zh/markerSheet.properties</li>
     * <li>/markers/markerSheet_zh_CN.properties</li>
     * <li>/markers/markerSheet_zh.properties</li>
     * <li>/markers/markerSheet.properties</li>
     * </ul>
     * </p>
     * <p>
     * The returned value will be either <code>null</code> if no resource is
     * found available, or a bundle resource URL associated to the required
     * resource.
     * </p>
     * 
     * @param mainPath
     *            The main path of the resource
     * @param prefix
     *            The prefix of the resource name
     * @param suffix
     *            The suffix of the resource name
     * @param nl
     *            The language name, e.g. <code>en_US</code>, <code>zh_CN</code>
     *            , <code>fr</code>, etc.
     * @return A URL associated to the required resource, or <code>null</code>
     *         if no resource was found
     */
    public static URL findResource(String bundleName, String mainPath,
            String prefix, String suffix, String nl) {
        return findResource(Platform.getBundle(bundleName), mainPath, prefix,
                suffix, nl);
    }

    public static URL findResource(Bundle bundle, String mainPath,
            String prefix, String suffix) {
        return findResource(bundle, mainPath, prefix, suffix, Platform.getNL());
    }

    public static URL findResource(Bundle bundle, String mainPath,
            String prefix, String suffix, String nl) {
        if (bundle == null)
            return null;

        String ma = getMajorLang(nl);
        String mi = getMinorLang(nl);

        if (!mainPath.endsWith(SEP)) {
            mainPath += SEP;
        }

        URL url;
//        url = findResource(NL1_PLUGIN_ID, mainPath, prefix, suffix, nl, ma,
//                mi);
//        if (url != null)
//            return url;
        url = findResource(bundle, mainPath, prefix, suffix, nl, ma, mi);
        if (url != null)
            return url;

        return null;
    }

    private static URL findResource(Bundle bundle, String mainPath,
            String prefix, String suffix, String nl, String ma, String mi) {
        String path;
        URL url;
        if (mi != null) {
            // "/markers/zh/CN/markerSheet.properties"
            path = mainPath + ma + SEP + mi + SEP + prefix + suffix;
            url = FileLocator.find(bundle, new Path(path), null);
            if (url != null)
                return url;
        }

        // "/markers/zh/markerSheet.properties"
        path = mainPath + ma + SEP + prefix + suffix;
        url = FileLocator.find(bundle, new Path(path), null);
        if (url != null)
            return url;

        // "/markers/markerSheet_zh_CN.properties"
        path = mainPath + prefix + LINK + nl + suffix;
        url = FileLocator.find(bundle, new Path(path), null);
        if (url != null)
            return url;

        if (!ma.equals(nl)) {
            // "/markers/markerSheet_zh.properties"
            path = mainPath + prefix + LINK + ma + suffix;
            url = FileLocator.find(bundle, new Path(path), null);
            if (url != null)
                return url;

        }
        // "/markers/markerSheet.properties"
        path = mainPath + prefix + suffix;
        url = FileLocator.find(bundle, new Path(path), null);
        if (url != null)
            return url;

        return null;
    }

    public static String getMajorLang(String nl) {
        int i = nl.indexOf(Link);
        if (i > 0) {
            return nl.substring(0, i);
        }
        return nl;
    }

    public static String getMinorLang(String nl) {
        int i = nl.indexOf(Link);
        if (i > 0) {
            return nl.substring(i + 1);
        }
        return null;
    }

}