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
package org.xmind.core.internal.compatibility;

import java.io.IOException;

import org.xmind.core.CoreException;
import org.xmind.core.internal.dom.DeserializerImpl;
import org.xmind.core.internal.dom.WorkbookImpl;

public abstract class FileFormat {

    protected DeserializerImpl deserializer;

    public FileFormat(DeserializerImpl deserializer) {
        super();
        this.deserializer = deserializer;
    }

    public abstract boolean identifies() throws CoreException, IOException;

    public abstract WorkbookImpl load() throws CoreException, IOException;

}