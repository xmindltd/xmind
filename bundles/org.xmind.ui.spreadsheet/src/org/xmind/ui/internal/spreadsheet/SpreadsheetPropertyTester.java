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
package org.xmind.ui.internal.spreadsheet;

import org.eclipse.core.runtime.Assert;
import org.xmind.ui.branch.IBranchPropertyTester;
import org.xmind.ui.mindmap.IBranchPart;

public class SpreadsheetPropertyTester implements IBranchPropertyTester {

    public boolean test(IBranchPart branch, String property, Object[] args,
            Object expectedValue) {
        if (Spreadsheet.P_IS_ALTERNATIVE_ROW.equals(property)) {
            if (expectedValue == null)
                return isAlternativeRow(branch);
            if (expectedValue instanceof Boolean) {
                return isAlternativeRow(branch) == ((Boolean) expectedValue)
                        .booleanValue();
            }
            return false;
        } else if (Spreadsheet.P_IS_ALTERNATIVE_Colunm.equals(property)) {
            if (expectedValue == null)
                return isAlternativeRow(branch);
            if (expectedValue instanceof Boolean) {
                return isAlternativeRow(branch) == ((Boolean) expectedValue)
                        .booleanValue();
            }
            return false;
        } else if (Spreadsheet.P_USER_STRUCTURE_CLASS.equals(property)) {
            if (expectedValue == null || expectedValue instanceof String) {
                return isUserStructureClass(branch, (String) expectedValue);
            }
        }
        Assert.isTrue(false);
        return false;
    }

    private boolean isUserStructureClass(IBranchPart branch,
            String expectedValue) {
        String value = branch.getTopic().getStructureClass();
        return value == expectedValue
                || (value != null && value.equals(expectedValue));
    }

    private boolean isAlternativeRow(IBranchPart branch) {
        return (branch.getBranchIndex() & 1) != 0;
    }

}