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

import org.xmind.gef.Disposable;
import org.xmind.gef.GEF;

/**
 * @author Brian Sun
 */
public class Command extends Disposable {

    protected static final String EMPTY = ""; //$NON-NLS-1$

    private String label;

    private boolean executed = false;

    /**
     * 
     */
    public Command() {
        this.label = EMPTY;
    }

    /**
     * @param label
     */
    public Command(String label) {
        this.label = label == null ? EMPTY : label;
    }

    public int getType() {
        return GEF.CMD_NORMAL;
    }

    public void redo() {
        executed = true;
    }

    public void undo() {
        executed = false;
    }

    public boolean canExecute() {
        return !executed && !isDisposed();
    }

    public void execute() {
        redo();
    }

    public boolean canUndo() {
        return executed && !isDisposed();
    }

    public boolean canRedo() {
        return !executed && !isDisposed();
    }

    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label == null ? EMPTY : label;
    }

}