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
package org.xmind.core;

import java.util.Collection;

public interface ICloneData {

    /**
     * Category for getting/putting workbook component IDs.
     */
    String WORKBOOK_COMPONENTS = "workbookComponents"; //$NON-NLS-1$

    /**
     * Category for getting/putting style IDs.
     */
    String STYLESHEET_COMPONENTS = "styleSheetComponents"; //$NON-NLS-1$

    /**
     * Category for getting/putting marker IDs.
     */
    String MARKERSHEET_COMPONENTS = "markerSheetComponents"; //$NON-NLS-1$

    /**
     * Category for getting/putting URLs.
     */
    String URLS = "urls"; //$NON-NLS-1$

    /**
     * Category for getting/putting file entry paths.
     */
    String FILE_ENTRIES = "fileEntries"; //$NON-NLS-1$

    Collection<Object> getSources();

    Collection<Object> getCloneds();

    boolean hasCloned();

    Object get(Object source);

    void put(Object source, Object cloned);

    String getString(String category, String source);

    void putString(String category, String source, String cloned);

    boolean isCloned(Object source);

    boolean isCloned(String category, String source);

}