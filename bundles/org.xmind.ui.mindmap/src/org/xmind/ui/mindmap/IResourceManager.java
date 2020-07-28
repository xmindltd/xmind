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
package org.xmind.ui.mindmap;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;

public interface IResourceManager {

    IMarkerSheet getSystemMarkerSheet();

    IMarkerSheet getUserMarkerSheet();

    void saveUserMarkerSheet();

    IMarkerGroup getRecentMarkerGroup();

    IStyleSheet getDefaultStyleSheet();

    IStyleSheet getSystemStyleSheet();

    IStyleSheet getUserStyleSheet();

    void saveUserStyleSheet();

    IStyle getBlankTheme();

    IStyle getDefaultTheme();

    void setDefaultTheme(String id);

    IStyleSheet getSystemThemeSheet();

    IStyleSheet getUserThemeSheet();

    void saveUserThemeSheet();

    /**
     * Finds a resource located at the specified URI.
     * <p>
     * The URI must be of the specification: <strong> <code>schema:path</code>
     * </strong>:
     * <ul>
     * <li><strong><code>marker:system/GROUP_ID/MARKER_ID</code></strong>
     * returns a {@link IMarker} representing a marker in the system marker
     * list; (if <code>GROUP_ID</code> is "<code>any</code>",
     * {@link IMarkerSheet#findMarker(String)} will be used to find the marker)
     * </li>
     * <li><strong><code>marker:system/GROUP_ID</code></strong> returns a
     * {@link IMarkerGroup} representing a marker group in the system marker
     * list;</li>
     * <li><strong><code>marker:system</code></strong> returns a
     * {@link IMarkerSheet} representing the system marker list;</li>
     * <li><strong><code>marker:user/GROUP_ID/MARKER_ID</code></strong> returns
     * a {@link IMarker} representing a marker in the user custom marker list;
     * (if <code>GROUP_ID</code> is "<code>any</code>",
     * {@link IMarkerSheet#findMarker(String)} will be used to find the marker)
     * </li>
     * <li><strong><code>marker:user/GROUP_ID</code></strong> returns a
     * {@link IMarkerGroup} representing a marker group in the user custom
     * marker list;</li>
     * <li><strong><code>marker:user</code></strong> returns a
     * {@link IMarkerSheet} representing the user custom marker list;</li>
     * <li><strong><code>style:default/STYLE_ID</code></strong> returns a
     * {@link IStyle} representing a style in the default style list;</li>
     * <li><strong><code>style:default</code></strong> returns a
     * {@link IStyleSheet} representing the default style list;</li>
     * <li><strong><code>style:system/STYLE_ID</code></strong> returns a
     * {@link IStyle} representing a style in the system style list;</li>
     * <li><strong><code>style:system</code></strong> returns a
     * {@link IStyleSheet} representing the system style list;</li>
     * <li><strong><code>style:user/STYLE_ID</code></strong> returns a
     * {@link IStyle} representing a style in the user custom style list;</li>
     * <li><strong><code>style:user</code></strong> returns a
     * {@link IStyleSheet} representing the user custom style list;</li>
     * <li><strong><code>theme:system/blank</code></strong> returns a
     * {@link IStyle} representing the blank theme;</li>
     * <li><strong><code>theme:system/__default__</code></strong> returns a
     * {@link IStyle} representing the default theme;</li>
     * <li><strong><code>theme:system/STYLE_ID</code></strong> returns a
     * {@link IStyle} representing a theme in the system theme list;</li>
     * <li><strong><code>theme:system</code></strong> returns a
     * {@link IStyleSheet} representing the system theme list;</li>
     * <li><strong><code>theme:user/STYLE_ID</code></strong> returns a
     * {@link IStyle} representing a theme in the user custom theme list;</li>
     * <li><strong><code>theme:user</code></strong> returns a
     * {@link IStyleSheet} representing the user custom theme list.</li>
     * </ul>
     * </p>
     * 
     * @param uri
     *            the URI locating a resource
     * @return the resource located at the specified URI, or <code>null</code>
     *         if the resource is not found
     */
    Object findResource(String uri);

    /**
     * Generates a URI representing the specified resource.
     * <p>
     * Returns
     * <ul>
     * <li><strong><code>marker:system/GROUP_ID/MARKER_ID</code></strong> for a
     * {@link IMarker} representing a marker in the system marker list;</li>
     * <li><strong><code>marker:system/GROUP_ID</code></strong> for a
     * {@link IMarkerGroup} representing a marker group in the system marker
     * list;</li>
     * <li><strong><code>marker:system</code></strong> for a
     * {@link IMarkerSheet} representing the system marker list;</li>
     * <li><strong><code>marker:user/GROUP_ID/MARKER_ID</code></strong> for a
     * {@link IMarker} representing a marker in the user custom marker list;
     * </li>
     * <li><strong><code>marker:user/GROUP_ID</code></strong> for a
     * {@link IMarkerGroup} representing a marker group in the user custom
     * marker list;</li>
     * <li><strong><code>marker:user</code></strong> for a {@link IMarkerSheet}
     * representing the user custom marker list;</li>
     * <li><strong><code>style:default/STYLE_ID</code></strong> for a
     * {@link IStyle} representing a style in the default style list;</li>
     * <li><strong><code>style:default</code></strong> for a {@link IStyleSheet}
     * representing the default style list;</li>
     * <li><strong><code>style:system/STYLE_ID</code></strong> for a
     * {@link IStyle} representing a style in the system style list;</li>
     * <li><strong><code>style:system</code></strong> for a {@link IStyleSheet}
     * representing the system style list;</li>
     * <li><strong><code>style:user/STYLE_ID</code></strong> for a
     * {@link IStyle} representing a style in the user custom style list;</li>
     * <li><strong><code>style:user</code></strong> for a {@link IStyleSheet}
     * representing the user custom style list;</li>
     * <li><strong><code>theme:system/blank</code></strong> for a {@link IStyle}
     * representing the blank theme;</li>
     * <li><strong><code>theme:system/STYLE_ID</code></strong> for a
     * {@link IStyle} representing a theme in the system theme list;</li>
     * <li><strong><code>theme:system</code></strong> for a {@link IStyleSheet}
     * representing the system theme list;</li>
     * <li><strong><code>theme:user/STYLE_ID</code></strong> for a
     * {@link IStyle} representing a theme in the user custom theme list;</li>
     * <li><strong><code>theme:user</code></strong> for a {@link IStyleSheet}
     * representing the user custom theme list.</li>
     * </ul>
     * </p>
     * 
     * @param resource
     *            the resource to be located
     * @return a URI locating the specified resource, or <code>null</code> if
     *         the specified object is not a valid resource
     */
    String toResourceURI(Object resource);

    @Deprecated
    List<ITemplate> getSystemTemplates();

    List<ITemplate> getUserTemplates();

    List<ITemplateGroup> getSystemTemplateGroups();

    boolean isSystemTemplate(ITemplate template);

    boolean isUserTemplate(ITemplate template);

    ITemplate addUserTemplateFromWorkbookURI(URI sourceWorkbookURI)
            throws InvocationTargetException;

    void removeUserTemplate(ITemplate template);

    ITemplate getDefaultTemplate();

    void setDefaultTemplate(ITemplate template);

    void addResourceManagerListener(IResourceManagerListener listener);

    void removeResourceManagerListener(IResourceManagerListener listener);

}
