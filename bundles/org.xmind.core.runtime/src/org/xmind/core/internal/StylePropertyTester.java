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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.xmind.core.style.IStyle;

public class StylePropertyTester extends PropertyTester {

    private static final String P_ID = "id"; //$NON-NLS-1$

    private static final String P_TYPE = "type"; //$NON-NLS-1$

    private static final String P_GROUP = "group"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        if (receiver instanceof IStyle) {
            IStyle style = (IStyle) receiver;
            if (P_ID.equals(property)) {
                return expectedValue != null
                        && expectedValue.equals(style.getId());
            } else if (P_TYPE.equals(property)) {
                return expectedValue != null
                        && expectedValue.equals(style.getType());
            } else if (P_GROUP.equals(property)) {
                return expectedValue != null
                        && expectedValue.equals(style.getOwnedStyleSheet()
                                .findOwnedGroup(style));
            }
        }
        Assert.isTrue(false);
        return false;
    }

}
