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
package org.xmind.core;

import java.util.Set;

import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyled;

/**
 * @author briansun
 * 
 */
public interface ISheet extends IIdentifiable, ITitled, IWorkbookComponent,
        IAdaptable, IStyled, IModifiable {

    /**
     * @return
     */
    ITopic getRootTopic();

    /**
     * 
     * @param newRootTopic
     */
    void replaceRootTopic(ITopic newRootTopic);

    /**
     * @return
     */
    int getIndex();

    /**
     * 
     * @return
     */
    IWorkbook getParent();

    /**
     * @return
     */
    Set<IRelationship> getRelationships();

    /**
     * @param rel
     */
    void addRelationship(IRelationship relationship);

    /**
     * @param rel
     */
    void removeRelationship(IRelationship relationship);

    /**
     * 
     * @return
     */
    IStyle getTheme();

    /**
     * 
     * @return
     */
    String getThemeId();

    /**
     * 
     */
    void setThemeId(String themeId);

    /**
     * 
     * @return
     */
    ILegend getLegend();

    /**
     * @deprecated
     * @return
     */
    @Deprecated
    ISheetSetting getSetting();

    ISheetSettings getSettings();

}