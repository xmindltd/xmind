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

public interface IMainDirection {

    IMainDirection LeftHeaded = new LeftHeaded();

    IMainDirection RightHeaded = new RightHeaded();

    boolean isTransformerEnabled();

//    int calcNavigation(IBranchPart branch, String navReqType);
//
//    int calcChildNavigation(IBranchPart branch, String navReqType);

//    int calcNavigation(int direction);

//    int getChildTargetOrientation(boolean upwards);
//
//    int getSourceOrientation();

//    double getChildRotateAngle(boolean upwards);

    ISubDirection getUpNormal();

    ISubDirection getUpRotated();

    ISubDirection getDownNormal();

    ISubDirection getDownRotated();

}