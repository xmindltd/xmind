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
package org.xmind.core.internal;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicPath;
import org.xmind.core.IWorkbook;

public class TopicCompartor implements Comparator<ITopic> {

    private List<String> TOPIC_TYPES = Arrays.asList(ITopic.ROOT,
            ITopic.ATTACHED, ITopic.DETACHED, ITopic.SUMMARY, ITopic.CALLOUT);

    public int compare(ITopic t1, ITopic t2) {
        if (t1 == null && t2 == null)
            return -10000;
        if (t1 == null)
            return 10000;
        if (t2 == null)
            return -10000;

        if (t1.equals(t2))
            return 0;

        ITopicPath p1 = t1.getPath();
        ITopicPath p2 = t2.getPath();

        IWorkbook workbook = p1.getWorkbook();
        if (workbook == null || !workbook.equals(p2.getWorkbook()))
            // can't compare topics that are in different workbooks or not in any workbook
            return -1000;

        ISheet s1 = p1.getSheet();
        ISheet s2 = p2.getSheet();
        // sheets should not be null here since workbook is not null
        if (!s1.equals(s2)) {
            return s1.getIndex() - s2.getIndex();
        }

        List<ITopic> tl1 = p1.toTopicList();
        List<ITopic> tl2 = p2.toTopicList();
        int total = Math.max(tl1.size(), tl2.size());
        for (int i = 0; i < total; i++) {
            ITopic c1 = i < tl1.size() ? tl1.get(i) : null;
            ITopic c2 = i < tl2.size() ? tl2.get(i) : null;
            if (c1 == null)
                return -100;
            if (c2 == null)
                return 100;

            if (c1.equals(c2))
                continue;

            String type1 = c1.getType();
            String type2 = c2.getType();
            // types should not be null here since workbook is not null
            if (!type1.equals(type2))
                return TOPIC_TYPES.indexOf(type1) - TOPIC_TYPES.indexOf(type2);

            return c1.getIndex() - c2.getIndex();
        }

        return 0;
    }

}