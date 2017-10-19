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
package org.xmind.ui.browser;

import org.eclipse.jface.action.IContributionManager;

/**
 * 
 * @author Frank Shaka
 * 
 */
public interface IBrowserViewerContribution {

    /**
     * Contribution items filled in must implement <code>fill(Composite)</code>
     * method.
     * 
     * @param viewer
     * @param toolBar
     */
    void fillToolBar(IBrowserViewer viewer, IContributionManager toolBar);

}