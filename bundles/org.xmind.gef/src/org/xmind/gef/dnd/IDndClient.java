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
package org.xmind.gef.dnd;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.part.IPart;

public interface IDndClient {

    Transfer getTransfer();

    Object getData(Transfer transfer, TransferData data);

    Object toTransferData(Object[] viewerElements, IViewer viewer);

    /**
     * 
     * @param transferData
     * @param viewer
     * @param target
     * @return
     * @deprecated Use makeDNDCommand(Object, Request)
     */
    Object[] toViewerElements(Object transferredData, IViewer viewer,
            Object target);

    Command makeDNDCommand(Object transferredData, Request request);

    boolean canCopy(TransferData data, IViewer viewer, Point location,
            IPart target);

    boolean canMove(TransferData data, IViewer viewer, Point location,
            IPart target);

    boolean canLink(TransferData data, IViewer viewer, Point location,
            IPart target);

}