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
package org.xmind.gef.ui.editor;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public interface IPageContainerPresentation extends IPageChangeProvider {

    Composite createContainer(Composite parent);

    void addPage(Composite container, int index, Control pageControl);

    int getActivePage(Composite container);

    int getPageCount(Composite container);

    Control getPageControl(Composite container, int pageIndex);

    Image getPageImage(Composite container, int pageIndex);

    String getPageText(Composite container, int pageIndex);

    void disposePage(Composite container, int pageIndex);

    void setActivePage(Composite container, int pageIndex);

    void setPageControl(Composite container, int pageIndex, Control pageControl);

    void setPageImage(Composite container, int pageIndex, Image image);

    void setPageText(Composite container, int pageIndex, String text);

    int findPage(Composite container, int x, int y);

}