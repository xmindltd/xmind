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
package org.xmind.core.internal.dom;

import java.util.ArrayList;
import java.util.List;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.TopicPath;

/**
 * @author briansun
 * 
 */
public class TopicPathImpl extends TopicPath {

    private ITopic topic;

    /**
     * 
     */
    public TopicPathImpl(ITopic topic) {
        this.topic = topic;
        toList();
    }

    protected List<Object> createPathEntries() {
        List<Object> entries = new ArrayList<Object>();
        ITopic t = topic;
        ITopic parent = t.getParent();
        while (parent != null) {
            entries.add(0, t);
            t = parent;
            parent = t.getParent();
        }
        entries.add(0, t);
        if (t != null && t.isRoot()) {
            ISheet sheet = t.getOwnedSheet();
            if (sheet != null) {
                entries.add(0, sheet);
                IWorkbook workbook = sheet.getParent();
                if (workbook != null) {
                    entries.add(0, workbook);
                }
            }
        }
        return entries;
    }

}