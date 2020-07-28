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


public class LeftHeaded extends AbstractMainFishboneDirection {

    public LeftHeaded() {
        super(false);
    }

//    public int calcChildNavigation(IBranchPart branch, String navReqType) {
//        return super.calcChildNavigation(branch, navReqType);
//    }
//
//    public int calcNavigation(IBranchPart branch, String navReqType) {
//        if (GEF.REQ_NAV_RIGHT.equals(navReqType)) {
//            return Fishbone.NAV_CHILD;
//        }
//        return super.calcNavigation(branch, navReqType);
//    }

//    public int calcNavigation(int direction) {
//        switch (direction) {
//        case PositionConstants.WEST:
//            return NAVI_PARENT;
//        case PositionConstants.EAST:
//            return NAVI_CHILD;
//        case PositionConstants.NORTH:
//            return NAVI_PREV;
//        case PositionConstants.SOUTH:
//            return NAVI_NEXT;
//        }
//        return NAVI_SELF;
//    }

//    public double getChildRotateAngle(boolean upwards) {
//        return upwards ? ISubDirection.NER.getRotateAngle() : ISubDirection.SER
//                .getRotateAngle();
//    }

//    public int getChildTargetOrientation(boolean upwards) {
//        return upwards ? PositionConstants.WEST : PositionConstants.EAST;
//    }
//
//    public int getSourceOrientation() {
//        return PositionConstants.EAST;
//    }

    public ISubDirection getDownNormal() {
        return ISubDirection.SE;
    }

    public ISubDirection getDownRotated() {
        return ISubDirection.SER;
    }

    public ISubDirection getUpNormal() {
        return ISubDirection.NE;
    }

    public ISubDirection getUpRotated() {
        return ISubDirection.NER;
    }

}