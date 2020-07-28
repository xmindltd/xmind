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

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.Assert;
import org.eclipse.draw2d.geometry.Point;
import org.xmind.ui.branch.IBranchPolicy;
import org.xmind.ui.branch.IBranchPropertyTester;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.util.MindMapUtils;

public class BranchPartPropertyTester extends PropertyTester {

    private static final String P_TYPE = "type"; //$NON-NLS-1$

    private static final String P_FOLDED = "folded"; //$NON-NLS-1$

    private static final String P_INDEX = "index"; //$NON-NLS-1$

    private static final String P_POLICY_ID = "policyId"; //$NON-NLS-1$

    private static final String P_STRUCTURE_ID = "structureId"; //$NON-NLS-1$

    private static final String P_HAS_PARENT = "hasParent"; //$NON-NLS-1$

    private static final String P_PREFERRED_POSITION = "preferredPosition"; //$NON-NLS-1$

    private static final String P_PROPERTY = "property"; //$NON-NLS-1$

    private static final String ARG_EAST_OF = "eastOf"; //$NON-NLS-1$

    private static final String ARG_WEST_OF = "westOf"; //$NON-NLS-1$

    private static final String ARG_NORTH_OF = "northOf"; //$NON-NLS-1$

    private static final String ARG_SOUTH_OF = "southOf"; //$NON-NLS-1$

    public boolean test(Object receiver, String property, Object[] args,
            Object expectedValue) {
        if (receiver instanceof IBranchPart) {
            IBranchPart branch = (IBranchPart) receiver;
            if (P_TYPE.equals(property)) {
                if (expectedValue == null)
                    return false;
                if (expectedValue instanceof String)
                    return expectedValue.equals(branch.getBranchType());
            } else if (P_FOLDED.equals(property)) {
                if (expectedValue == null)
                    return branch.isFolded();
                if (expectedValue instanceof Boolean) {
                    return branch.isFolded() == ((Boolean) expectedValue)
                            .booleanValue();
                }
            } else if (P_INDEX.equals(property)) {
                if (expectedValue == null)
                    return false;
                if (expectedValue instanceof Integer) {
                    return branch.getBranchIndex() == ((Integer) expectedValue)
                            .intValue();
                }
            } else if (P_POLICY_ID.equals(property)) {
                if (expectedValue == null)
                    return false;
                if (expectedValue instanceof String) {
                    return expectedValue.equals(branch.getBranchPolicyId());
                }
            } else if (P_STRUCTURE_ID.equals(property)) {
                if (expectedValue == null || expectedValue instanceof String) {
                    return isStructureAlgorithmId(branch, args,
                            (String) expectedValue);
                }
            } else if (P_HAS_PARENT.equals(property)) {
                if (expectedValue == null)
                    return hasParent(branch);
                if (expectedValue instanceof Boolean) {
                    return hasParent(branch) == ((Boolean) expectedValue)
                            .booleanValue();
                }
            } else if (P_PREFERRED_POSITION.equals(property)) {
                if (expectedValue == null || expectedValue instanceof String) {
                    return isPreferredPosition(branch, args,
                            (String) expectedValue);
                }
            } else if (P_PROPERTY.equals(property)) {
                if (args.length == 0 || args[0] == null)
                    return false;
                String p = args[0].toString();
                Object[] newArgs = new Object[args.length - 1];
                System.arraycopy(args, 1, newArgs, 0, args.length - 1);
                return testSpecifiedProperty(branch, p, newArgs, expectedValue);
            }
        }
        Assert.isTrue(false);
        return false;
    }

    private boolean isStructureAlgorithmId(IBranchPart branch, Object[] args,
            String expectedValue) {
        String id = (String) MindMapUtils.getCache(branch,
                IBranchPolicy.CACHE_STRUCTURE_ID);
        if (id == null)
            return expectedValue != null;
        return id.equals(expectedValue);
    }

    private boolean testSpecifiedProperty(IBranchPart branch, String property,
            Object[] args, Object expectedValue) {
        IBranchPropertyTester tester = (IBranchPropertyTester) MindMapUtils
                .getCache(branch, IBranchPropertyTester.CACHE_PROPERTY_TESTER);
        if (tester != null) {
            return tester.test(branch, property, args, expectedValue);
        }
        return expectedValue == null;
    }

    private boolean isPreferredPosition(IBranchPart branch, Object[] args,
            String expectedValue) {
        Point expPos = toPoint(expectedValue);
        Point prefPos = (Point) MindMapUtils.getCache(branch,
                IBranchPart.CACHE_PREF_POSITION);
        if (args.length > 0) {
            Object arg = args[0];
            if (arg instanceof String) {
                String direction = (String) arg;
                int x1 = prefPos == null ? 0 : prefPos.x;
                int x2 = expPos == null ? 0 : expPos.x;
                int y1 = prefPos == null ? 0 : prefPos.y;
                int y2 = expPos == null ? 0 : expPos.y;
                if (ARG_EAST_OF.equals(direction)) {
                    return x1 > x2;
                } else if (ARG_WEST_OF.equals(direction)) {
                    return x1 < x2;
                } else if (ARG_NORTH_OF.equals(direction)) {
                    return y1 < y2;
                } else if (ARG_SOUTH_OF.equals(direction)) {
                    return y1 > y2;
                }
            }
        }
        if (prefPos == null)
            return expPos == null;

        if (expPos == null)
            return false;

        return prefPos.equals(expPos);
    }

    private Point toPoint(String string) {
        if (string == null || string.length() == 0)
            return null;

        String[] sp = string.split(","); //$NON-NLS-1$
        if (sp.length == 0)
            return null;

        Point p = new Point();
        String x = sp[0];
        try {
            p.x = Integer.parseInt(x);
        } catch (NumberFormatException e) {
            return null;
        }
        if (sp.length > 1) {
            String y = sp[1];
            try {
                p.y = Integer.parseInt(y);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return p;
    }

    private boolean hasParent(IBranchPart branch) {
        return branch.getParentBranch() != null;
    }

}