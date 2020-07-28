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
package org.xmind.ui.internal.notes;

import java.util.Iterator;
import java.util.List;

import org.xmind.core.INotes;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.ui.richtext.IRichDocument;
import org.xmind.ui.richtext.RichDocument;
import org.xmind.ui.util.MindMapUtils;

public class NotesUtils {

    private NotesUtils() {
    }

    public static IRichDocument fromPlain(String text) {
        return new RichDocument(text);
    }

    public static IRichDocument fromHtml(String html) {
        return null;
    }

    public static String toHtml(IRichDocument document) {
        return null;
    }

    public static List<ITopic> getAllTopicsWithNotes(ISheet sheet) {
        List<ITopic> topics = MindMapUtils.getAllTopics(sheet, true, true);
        Iterator<ITopic> ite = topics.iterator();
        while (ite.hasNext()) {
            ITopic topic = ite.next();
            if (!hasNotes(topic)) {
                ite.remove();
            }
        }
        return topics;
    }

    private static boolean hasNotes(ITopic topic) {
        if (topic == null) {
            return false;
        }

        INotes notes = topic.getNotes();
        return notes != null && !notes.isEmpty();
    }

}