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

import org.eclipse.swt.dnd.TransferData;

public class DndData {

    public final String clientId;

    public final TransferData dataType;

    public Object parsedData;

    public DndData(String clientId, Object parsedData, TransferData dataType) {
        this.clientId = clientId;
        this.parsedData = parsedData;
        this.dataType = dataType;
    }

    public int hashCode() {
        int c = clientId.hashCode();
        if (parsedData != null) {
            c ^= parsedData.hashCode();
        }
        return c;
    }

    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj == null || !(obj instanceof DndData))
            return false;
        DndData that = (DndData) obj;
        return this.clientId.equals(that.clientId)
                && this.dataType.equals(that.dataType)
                && (this.parsedData == that.parsedData || (this.parsedData != null && this.parsedData
                        .equals(that.parsedData)));
    }

    public String toString() {
        return "{" + clientId + ": " + parsedData + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}