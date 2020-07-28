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

    public int getComputedDepth() {
        String nf = getNumberFormat();
        String noneFormat = "org.xmind.numbering.none"; //$NON-NLS-1$

        if (noneFormat.equals(nf))
            return -1;

        String dv = getDepth();
        if (dv != null) {
            try {
                return Integer.parseInt(dv);
            } catch (NumberFormatException ignore) {
            }
        }

        if (isInherited(1))
            return getParent().getParent().getNumbering().getComputedDepth()
                    - 1;
        else if (getNumberFormat() != null)
            return 3;

        return -1;
    }

    public boolean isInherited(int min) {
        ITopic topic = getParent();
        if (!ITopic.ATTACHED.equals(topic.getType()))
            return false;

        if (getDepth() != null)
            return false;

        if ("org.xmind.numbering.none".equals(getNumberFormat())) //$NON-NLS-1$
            return false;

        ITopic parentTopic = topic.getParent();
        if (parentTopic == null)
            return false;

        int pd = parentTopic.getNumbering().getComputedDepth();
        return pd > min;
    }

}
