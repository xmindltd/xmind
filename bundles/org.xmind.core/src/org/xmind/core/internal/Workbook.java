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
package org.xmind.core.internal;

import java.util.Arrays;
import java.util.List;

import org.xmind.core.IAdaptable;
import org.xmind.core.ICloneData;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;

/**
 * @author briansun
 * 
 */
public abstract class Workbook implements IWorkbook {

    public <T> T getAdapter(Class<T> adapter) {
        return null;
    }

    public ISheet getPrimarySheet() {
        List<ISheet> sheets = getSheets();
        if (!sheets.isEmpty())
            return sheets.get(0);
        return null;
    }

    public void addSheet(ISheet sheet) {
        addSheet(sheet, -1);
    }

    @Deprecated
    public ITopic cloneTopic(ITopic topic) {
        ICloneData result = clone(Arrays.asList(topic));
        return (ITopic) result.get(topic);
    }

    /**
     * @see org.xmind.core.IWorkbook#findTopic(java.lang.String)
     */
    public ITopic findTopic(String id) {
        Object element = getElementById(id);
        return element instanceof ITopic ? (ITopic) element : null;
    }

    public Object getElementById(String id) {
        return findElement(id, this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.xmind.core.IWorkbook#findTopic(java.lang.String,
     * org.xmind.core.IAdaptable)
     */
    public ITopic findTopic(String id, IAdaptable source) {
        Object element = findElement(id, source);
        return element instanceof ITopic ? (ITopic) element : null;
    }

}