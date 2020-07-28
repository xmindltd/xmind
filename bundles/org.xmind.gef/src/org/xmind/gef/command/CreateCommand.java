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
package org.xmind.gef.command;

import java.util.Collections;
import java.util.List;

import org.xmind.gef.GEF;
import org.xmind.gef.ISourceProvider2;

public abstract class CreateCommand extends Command implements ISourceProvider2 {

    protected static final List<Object> NO_SOURCES = Collections.emptyList();

    private Object creation;

    private boolean sourceCollectable = true;

    public CreateCommand() {
        this(null);
    }

    public CreateCommand(Object creation) {
        this.creation = creation;
    }

    public Object getSource() {
        return creation;
    }

    public boolean hasSource() {
        return creation != null || canCreate();
    }

    public List<Object> getSources() {
        return creation == null ? NO_SOURCES : Collections
                .singletonList(creation);
    }

    public int getType() {
        return GEF.CMD_CREATE;
    }

    public boolean canExecute() {
        return super.canExecute() || canCreate();
    }

    public void execute() {
        if (creation == null) {
            creation = create();
        }
        super.execute();
    }

    protected abstract Object create();

    protected abstract boolean canCreate();

    public void dispose() {
        creation = null;
        super.dispose();
    }

    public boolean isSourceCollectable() {
        return sourceCollectable;
    }

    public void setSourceCollectable(boolean collectable) {
        this.sourceCollectable = collectable;
    }

}