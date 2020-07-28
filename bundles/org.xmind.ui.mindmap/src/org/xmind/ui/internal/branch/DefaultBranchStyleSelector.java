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
package org.xmind.ui.internal.branch;

import org.xmind.ui.style.Styles;

public class DefaultBranchStyleSelector extends AbstractBranchStyleSelector {

    private static DefaultBranchStyleSelector instance = null;

    protected DefaultBranchStyleSelector() {
        registerInheritedStyleKey(Styles.LineColor,
                Styles.LAYER_BEFORE_DEFAULT_VALUE);
        registerInheritedStyleKey(Styles.LineWidth,
                Styles.LAYER_BEFORE_DEFAULT_VALUE);
        registerInheritedStyleKey(Styles.LinePattern,
                Styles.LAYER_BEFORE_DEFAULT_VALUE);
    }

    public static DefaultBranchStyleSelector getDefault() {
        if (instance == null)
            instance = new DefaultBranchStyleSelector();
        return instance;
    }

}