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
package org.xmind.ui.mindmap;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;

public class MindMap implements IMindMap {

    private ISheet sheet;

    private ITopic centralTopic;

    public MindMap(ISheet sheet) {
        this(sheet, sheet.getRootTopic());
    }

    public MindMap(ISheet sheet, ITopic centralTopic) {
        this.sheet = sheet;
        this.centralTopic = centralTopic == null ? sheet.getRootTopic()
                : centralTopic;
    }

    public ITopic getCentralTopic() {
        return centralTopic;
    }

    public ISheet getSheet() {
        return sheet;
    }

    public int hashCode() {
        return sheet.hashCode() ^ centralTopic.hashCode();
    }

    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof MindMap))
            return false;
        MindMap that = (MindMap) obj;
        return that.sheet.equals(this.sheet)
                && that.centralTopic.equals(this.centralTopic);
    }

    public String toString() {
        return "[Sheet=" + sheet + ", CentralTopic=" + centralTopic + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

}