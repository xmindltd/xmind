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
package org.xmind.gef.part;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.xmind.gef.EditDomain;
import org.xmind.gef.IViewer;
import org.xmind.gef.util.Properties;

/**
 * @author Brian Sun
 */
public interface IPartSite extends IAdaptable {

    IPart getPart();

    IRootPart getRoot();

    IViewer getViewer();

    EditDomain getDomain();

    IPartFactory getPartFactory();

    PartRegistry getPartRegistry();

//    IModelContentProvider getContentProvider();

    Properties getProperties();

    Shell getShell();

    Control getViewerControl();

}