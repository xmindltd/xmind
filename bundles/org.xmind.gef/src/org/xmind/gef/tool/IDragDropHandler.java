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
package org.xmind.gef.tool;

import org.xmind.gef.IViewer;
import org.xmind.gef.event.DragDropEvent;

/**
 * @author Frank Shaka
 */
public interface IDragDropHandler {

    public abstract void dragStarted(DragDropEvent de, IViewer viewer);

    public abstract void dragDismissed(DragDropEvent de, IViewer viewer);

    public abstract void dragEntered(DragDropEvent de, IViewer viewer);

    public abstract void dragExited(DragDropEvent de, IViewer viewer);

    public abstract void dragOver(DragDropEvent de, IViewer viewer);

    public abstract void dragOperationChanged(DragDropEvent de, IViewer viewer);

    public abstract void drop(DragDropEvent de, IViewer viewer);

    public abstract void dropAccept(DragDropEvent de, IViewer viewer);

}