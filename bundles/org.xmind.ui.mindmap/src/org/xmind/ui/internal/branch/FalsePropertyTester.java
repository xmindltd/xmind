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

import org.xmind.ui.branch.IBranchPropertyTester;
import org.xmind.ui.mindmap.IBranchPart;

class FalsePropertyTester implements IBranchPropertyTester {

    private static final FalsePropertyTester instance = new FalsePropertyTester();

    public boolean test(IBranchPart branch, String property, Object[] args,
            Object expectedValue) {
        return false;
    }

    public static IBranchPropertyTester getInstance() {
        return instance;
    }

}