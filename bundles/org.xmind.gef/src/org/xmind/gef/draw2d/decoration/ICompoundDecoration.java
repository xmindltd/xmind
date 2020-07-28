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
package org.xmind.gef.draw2d.decoration;

import org.eclipse.draw2d.IFigure;

public interface ICompoundDecoration extends IDecoration {

    IDecoration getDecoration(int index);

    IDecoration setDecoration(IFigure figure, int index, IDecoration decoration);

    void add(IFigure figure, IDecoration decoration);

    void add(IFigure figure, int index, IDecoration decoration);

    int size();

    boolean isEmpty();

    boolean contains(IDecoration decoration);

    int indexOf(IDecoration decoration);

    IDecoration move(IFigure figure, int oldIndex, int newIndex);

    IDecoration remove(IFigure figure, int index);

}