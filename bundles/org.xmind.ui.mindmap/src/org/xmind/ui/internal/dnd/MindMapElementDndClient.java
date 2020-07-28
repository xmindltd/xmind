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
package org.xmind.ui.internal.dnd;

import java.util.Arrays;

import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.xmind.core.ICloneData;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;

public class MindMapElementDndClient extends MindMapDNDClientBase {

    private MindMapElementTransfer transfer = MindMapElementTransfer
            .getInstance();

    public Object toTransferData(Object[] viewerElements, IViewer viewer) {
        return viewerElements;
    }

    @Override
    protected Object[] toViewerElements(Object transferData, Request request,
            IWorkbook workbook, ITopic targetParent, boolean dropInParent) {
        if (transferData != null && transferData instanceof Object[]) {
            Object[] elements = (Object[]) transferData;
            ICloneData cloneData = workbook.clone(Arrays.asList(elements));
            return cloneData.getCloneds().toArray();
        }
        return null;
    }

    public Object getData(Transfer transfer, TransferData data) {
        if (transfer == this.transfer)
            return this.transfer.nativeToJava(data);
        return null;
    }

    public Transfer getTransfer() {
        return transfer;
    }

}