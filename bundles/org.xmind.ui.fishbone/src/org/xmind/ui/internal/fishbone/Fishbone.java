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
package org.xmind.ui.internal.fishbone;

public class Fishbone {

    /**
     * value = 70 (degrees)
     */
    public static final double RotateAngle = 70.0d;

    //ATTN: Used to be 'org.xmind.topicShape.fishhead.left' in version 1.0
    public static final String TOPIC_SHAPE_LEFT_FISHHEAD = "org.xmind.ui.topicShape.fishhead.left"; //$NON-NLS-1$

    //ATTN: Used to be 'org.xmind.topicShape.fishhead.right' in version 1.0
    public static final String TOPIC_SHAPE_RIGHT_FISHHEAD = "org.xmind.ui.topicShape.fishhead.right"; //$NON-NLS-1$

    //ATTN: Used to be 'org.xmind.topicShape.fishbone' in version 1.0
    public static final String TOPIC_SHAPE_FISHBONE = "org.xmind.ui.topicShape.fishbone"; //$NON-NLS-1$

    public static final String BRANCH_POLICY_LEFT_HEADED = "or.xmind.branchPolicy.fishbone.leftHeaded"; //$NON-NLS-1$

    public static final String BRANCH_POLICY_RIGHT_HEADED = "or.xmind.branchPolicy.fishbone.rightHeaded"; //$NON-NLS-1$

    public static final String BRANCH_DECORATION_MAIN_FISHBONE = "org.xmind.ui.branchDecoration.fishbone.main"; //$NON-NLS-1$

    public static final String BRANCH_DECORATION_SUB_FISHBONE = "org.xmind.ui.branchDecoration.fishbone.sub"; //$NON-NLS-1$

    public static final String BRANCH_CONN_FISHBONE = "org.xmind.ui.branchConnection.fishbone"; //$NON-NLS-1$

    public static final String STRUCTURE_RIGHT_HEADED = "org.xmind.ui.branchStructure.fishbone.rightHeaded"; //$NON-NLS-1$ 

    public static final String STRUCTURE_LEFT_HEADED = "org.xmind.ui.branchStructure.fishbone.leftHeaded"; //$NON-NLS-1$

    public static final String STRUCTURE_NE_ROTATED = "org.xmind.ui.branchStructure.fishbone.NE.rotated"; //$NON-NLS-1$

    public static final String STRUCTURE_NE_NORMAL = "org.xmind.ui.branchStructure.fishbone.NE.normal"; //$NON-NLS-1$

    public static final String STRUCTURE_SE_ROTATED = "org.xmind.ui.branchStructure.fishbone.SE.rotated"; //$NON-NLS-1$

    public static final String STRUCTURE_SE_NORMAL = "org.xmind.ui.branchStructure.fishbone.SE.normal"; //$NON-NLS-1$

    public static final String STRUCTURE_NW_ROTATED = "org.xmind.ui.branchStructure.fishbone.NW.rotated"; //$NON-NLS-1$

    public static final String STRUCTURE_NW_NORMAL = "org.xmind.ui.branchStructure.fishbone.NW.normal"; //$NON-NLS-1$

    public static final String STRUCTURE_SW_ROTATED = "org.xmind.ui.branchStructure.fishbone.SW.rotated"; //$NON-NLS-1$

    public static final String STRUCTURE_SW_NORMAL = "org.xmind.ui.branchStructure.fishbone.SW.normal"; //$NON-NLS-1$

    public static final int NAV_NONE = 0;

    public static final int NAV_PARENT = 1;

    public static final int NAV_NEXT = 2;

    public static final int NAV_PREV = 3;

    public static final int NAV_CHILD = 4;
}