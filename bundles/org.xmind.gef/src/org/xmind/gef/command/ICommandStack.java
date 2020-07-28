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

import org.xmind.gef.IDisposable2;

/**
 * @author Brian Sun
 */
public interface ICommandStack extends IDisposable2 {

    ICommandStack NULL = new NullCommandStack();

    void addCSListener(ICommandStackListener listener);

    void removeCSListener(ICommandStackListener listener);

    int getUndoLimit();

    void setUndoLimit(int undoLimit);

    void execute(Command command);

    boolean canUndo();

    void undo();

    String getUndoLabel();

    boolean canRedo();

    void redo();

    String getRedoLabel();

    /**
     * 
     * @return
     * @deprecated
     */
    boolean canRepeat();

    /**
     * @deprecated
     */
    void repeat();

    /**
     * 
     * @return
     * @deprecated
     */
    String getRepeatLabel();

    boolean isDirty();

    void markSaved();

    void clear();

}