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
package org.xmind.core.command.binary;

import java.io.IOException;
import java.io.InputStream;

public class NamedEntry implements INamedEntry, IBinaryEntryDelegate {

    private final String name;

    private final IBinaryEntry entry;

    public NamedEntry(String name, IBinaryEntry entry) {
        this.name = name;
        this.entry = entry;
    }

    public String getName() {
        return name;
    }

    public IBinaryEntry getRealEntry() {
        return entry;
    }

    public InputStream openInputStream() throws IOException {
        return this.entry.openInputStream();
    }

    public void dispose() {
        this.entry.dispose();
    }

    @Override
    public String toString() {
        return name + "[" + entry.toString() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
