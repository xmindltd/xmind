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
package org.xmind.core.internal;

import org.xmind.core.INumbering;
import org.xmind.core.ITopic;

public abstract class Numbering extends AbstractWorkbookComponent
        implements INumbering {

    public String getParentFormat() {
        ITopic topic = getParent();
        if (topic != null) {
            ITopic parent = topic.getParent();
            if (parent != null)
                return parent.getNumbering().getComputedFormat();
        }
        return null;
    }

    public String getComputedFormat() {
        String format = getNumberFormat();
        if (format != null)
            return format;
        ITopic topic = getParent();
        if (!ITopic.ATTACHED.equals(topic.getType()))
            return null;
        return getParentFormat();
    }

    public String getParentSeparator() {
        ITopic topic = getParent();
        if (topic != null) {
            ITopic parent = topic.getParent();
            if (parent != null)
                return parent.getNumbering().getComputedSeparator();
        }
        return null;
    }

    public String getComputedSeparator() {
        String separator = getSeparator();
        if (separator != null)
            return separator;
        ITopic topic = getParent();
        if (!ITopic.ATTACHED.equals(topic.getType()))
            return null;
        return getParentSeparator();
    }

}