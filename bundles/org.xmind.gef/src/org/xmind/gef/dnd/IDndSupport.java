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

import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;

public interface IDndSupport {

    Object PENDING_DATA = new Object() {
        public String toString() {
            return "Pending Data"; //$NON-NLS-1$
        }
    };

    int getStyle();

    Transfer[] getTransfers();

    IDndClient getDndClient(String id);

    String[] getDndClientIds();

    DndData parseData(TransferData[] dataTypes, Object source,
            boolean usePendingData);

}