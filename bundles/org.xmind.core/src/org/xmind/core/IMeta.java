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

public interface IMeta extends IAdaptable, IWorkbookComponent {

    String SEP = "/"; //$NON-NLS-1$

    String AUTHOR = "Author"; //$NON-NLS-1$

    String DESCRIPTION = "Description"; //$NON-NLS-1$

    String THUMBNAIL = "Thumbnail"; //$NON-NLS-1$

    String ORIGIN_X = THUMBNAIL + SEP + "Origin" + SEP + "X"; //$NON-NLS-1$ //$NON-NLS-2$

    String ORIGIN_Y = THUMBNAIL + SEP + "Origin" + SEP + "Y"; //$NON-NLS-1$ //$NON-NLS-2$

    String BACKGROUND_COLOR = THUMBNAIL + SEP + "BackgroundColor"; //$NON-NLS-1$

    String CREATOR = "Creator"; //$NON-NLS-1$

    String CREATOR_NAME = CREATOR + SEP + "Name"; //$NON-NLS-1$

    String CREATOR_VERSION = CREATOR + SEP + "Version"; //$NON-NLS-1$

    String CREATE = "Create"; //$NON-NLS-1$

    String CREATED_TIME = CREATE + SEP + "Time"; //$NON-NLS-1$

    String AUTHOR_NAME = AUTHOR + SEP + "Name"; //$NON-NLS-1$

    String AUTHOR_EMAIL = AUTHOR + SEP + "Email"; //$NON-NLS-1$

    String AUTHOR_ORG = AUTHOR + SEP + "Org"; //$NON-NLS-1$

    /**
     * Key path prefix for all configuration keys.
     */
    String CONFIGURATIONS = "Configurations"; //$NON-NLS-1$

    /**
     * <p>
     * Key path for configuration of whether to save revisions automatically.
     * <p>
     * Values: "Yes", "No"
     */
    String CONFIG_AUTO_REVISION_GENERATION = CONFIGURATIONS + SEP
            + "AutoRevisionGeneration"; //$NON-NLS-1$

    String V_YES = "Yes"; //$NON-NLS-1$
    String V_NO = "No"; //$NON-NLS-1$

    /**
     * 
     * @param keyPath
     * @return
     */
    String getValue(String keyPath);

    /**
     * 
     * @param key
     * @param value
     */
    void setValue(String keyPath, String value);

    /**
     * 
     * @return
     */
    Set<String> getKeyPaths();

    /**
     * @deprecated
     * @param key
     * @return
     */
    IMetaData[] getMetaData(String key);

    /**
     * @deprecated
     * 
     * @param key
     * @return
     */
    IMetaData createMetaData(String key);

    /**
     * @deprecated
     * 
     * @param data
     */
    void addMetaData(IMetaData data);

    /**
     * @deprecated
     * 
     * @param data
     */
    void removeMetaData(IMetaData data);

}