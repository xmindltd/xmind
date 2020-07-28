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
package org.xmind.ui.internal.imports;

import org.xmind.core.ITopic;

public class ImporterUtils {

    public static String getDefaultTopicTitle(ITopic topic) {
        if (topic.isRoot())
            return ImportMessages.Importer_CentralTopic;

        String type = topic.getType();
        if (ITopic.DETACHED.equals(type))
            return ImportMessages.Importer_FloatingTopic;
        if (ITopic.SUMMARY.equals(type))
            return ImportMessages.Importer_Summary;

        ITopic parent = topic.getParent();
        if (parent != null && parent.isRoot())
            return ImportMessages.Importer_MainTopic;
        return ImportMessages.Importer_Subtopic;
    }

}