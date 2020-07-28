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
package org.xmind.ui.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.osgi.util.NLS;
import org.xmind.core.IRelationship;
import org.xmind.core.IRelationshipEnd;
import org.xmind.core.ITopic;
import org.xmind.ui.util.MindMapUtils;

public class ExportUtils {

//    private static final String SEP_DOT = "."; //$NON-NLS-1$

    static final List<RelationshipDescription> EMPTY_RELATIONSHIPS = Collections
            .emptyList();

    private ExportUtils() {
    }

    public static String getNumberingText(ITopic topic, ITopic centralTopic) {
        return MindMapUtils.getFullNumberingText(topic, null, null);
//        if (topic == null || topic.equals(centralTopic)
//                || (centralTopic == null && topic.isRoot())) {
//            return null;
//        }
//        String number = getNumber(topic);
//        if (number == null)
//            return null;
//
//        ITopic parent = topic.getParent();
//        if (parent == null)
//            return number + SEP_DOT;
//
//        String parentText = getNumberingText(parent, centralTopic);
//        if (parentText == null)
//            return number + SEP_DOT;
//
//        if (parentText.endsWith(SEP_DOT))
//            return parentText + number;
//
//        return parentText + SEP_DOT + number;
    }

//    private static String getNumber(ITopic topic) {
//        if (topic.isRoot())
//            return null;
//        int index = topic.getIndex() + 1;
//        String type = topic.getType();
//        if (ITopic.DETACHED.equals(type)) {
//            return NLS.bind(WizardMessages.Export_Appendix_format, index);
//        } else if (ITopic.SUMMARY.equals(type)) {
//            return NLS.bind(WizardMessages.Export_Summary_format, index);
//        } else {
//            return String.valueOf(index);
//        }
//    }

    public static List<RelationshipDescription> getRelationships(ITopic topic,
            List<IRelationship> relationships) {
        String topicId = topic.getId();
        List<RelationshipDescription> list = null;
        for (IRelationship r : relationships) {
            boolean isEnd1 = topicId.equals(r.getEnd1Id());
            if (isEnd1 || topicId.equals(r.getEnd2Id())) {
                IRelationshipEnd otherEnd = isEnd1 ? r.getEnd2() : r.getEnd1();
                if (otherEnd instanceof ITopic) {
                    String title = ((ITopic) otherEnd).getTitleText();
                    if (r.hasTitle()) {
                        title = NLS.bind("{0} ({1})", title, r.getTitleText()); //$NON-NLS-1$
                    }
                    if (list == null)
                        list = new ArrayList<RelationshipDescription>();
                    list.add(new RelationshipDescription(r, topic, otherEnd,
                            title));
                }
            }
        }
        return list == null ? EMPTY_RELATIONSHIPS : list;
    }

}