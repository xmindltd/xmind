/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.core.internal.zip;

import java.io.IOException;

import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;

/**
 * 
 * @author frankshaka
 * @deprecated
 */
public abstract class AbstractArchivedWorkbook implements IArchivedWorkbook {

    private final String file;

    protected final IWorkbook workbook;

    public AbstractArchivedWorkbook(IWorkbook workbook, String file) {
        this.workbook = workbook;
        this.file = file;
    }

    public String getFile() {
        return file;
    }

    /**
     * 
     */
    public void save() throws IOException, CoreException {
        save(null);
    }

}