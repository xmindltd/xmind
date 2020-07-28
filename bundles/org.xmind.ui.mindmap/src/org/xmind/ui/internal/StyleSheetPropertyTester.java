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
package org.xmind.ui.internal;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.xmind.ui.mindmap.MindMapUI;

public class StyleSheetPropertyTester extends PropertyTester {

    private static final String P_TYPE = "type"; //$NON-NLS-1$

    private static final String TYPE_DEFAULT = "default"; //$NON-NLS-1$

    private static final String TYPE_SYSTEM = "system"; //$NON-NLS-1$

    private static final String TYPE_USER = "user"; //$NON-NLS-1$

    private static final String TYPE_WORKBOOK = "workbook"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        if (P_TYPE.equals(property)) {
            if (receiver == MindMapUI.getResourceManager()
                    .getDefaultStyleSheet())
                return TYPE_DEFAULT.equals(expectedValue);
            if (receiver == MindMapUI.getResourceManager()
                    .getSystemStyleSheet()
                    || receiver == MindMapUI.getResourceManager()
                            .getSystemThemeSheet())
                return TYPE_SYSTEM.equals(expectedValue);
            if (receiver == MindMapUI.getResourceManager().getUserStyleSheet()
                    || receiver == MindMapUI.getResourceManager()
                            .getUserThemeSheet())
                return TYPE_USER.equals(expectedValue);
            return TYPE_WORKBOOK.equals(expectedValue);
        }
        Assert.isTrue(false);
        return false;
    }

}
