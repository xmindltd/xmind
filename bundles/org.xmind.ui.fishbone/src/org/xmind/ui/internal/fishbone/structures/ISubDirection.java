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
package org.xmind.ui.internal.fishbone.structures;

import java.util.List;

import org.eclipse.draw2d.PositionConstants;
import org.xmind.gef.draw2d.geometry.IPrecisionTransformer;
import org.xmind.gef.draw2d.geometry.PrecisionRotator;
import org.xmind.ui.mindmap.IBranchPart;

public interface ISubDirection extends PositionConstants {

    ISubDirection NER = new NorthEastRotated();

    ISubDirection NE = new NorthEastNormal();

    ISubDirection SER = new SouthEastRotated();

    ISubDirection SE = new SouthEastNormal();

    ISubDirection NWR = new NorthWestRotated();

    ISubDirection NW = new NorthWestNormal();

    ISubDirection SWR = new SouthWestRotated();

    ISubDirection SW = new SouthWestNormal();

    boolean isRotated();

    boolean isRightHeaded();

    boolean isDownwards();

    boolean isChildrenTraverseReversed();

    int getSourceOrientation();

    int getChildTargetOrientation();

//    /**
//     * Returns the request type with which a navigation request is performed
//     * into navigating to a fishbone branch's child.
//     * 
//     * @return one of {@link org.xmind.gef.GEF#REQ_NAV_UP},
//     *         {@link org.xmind.gef.GEF#REQ_NAV_DOWN},
//     *         {@link org.xmind.gef.GEF#REQ_NAV_LEFT},
//     *         {@link org.xmind.gef.GEF#REQ_NAV_RIGHT}
//     */
//    String getChildNavigationType();

//    int calcNavigation(int direction);

    double getRotateAngle();

    ISubDirection getSubDirection();

    void fillFishboneData(IBranchPart branch, FishboneData data,
            IPrecisionTransformer h, PrecisionRotator r, double spacing,
            List<IBranchPart> subbranches);

    void fillFishboneExtraData(IBranchPart branch, FishboneData data,
            IPrecisionTransformer h, PrecisionRotator r, double spacing,
            boolean extraBranch);

}