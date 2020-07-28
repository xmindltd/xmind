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
package org.xmind.gef;

import org.eclipse.draw2d.IFigure;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IRootPart;

/**
 * @author Brian Sun
 */
public interface IGenre {

    IFigure createFigure(IGraphicalPart part, Object model);

    IFigure createRootFigure(IRootPart rootPart, IGraphicalViewer viewer);

}