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
package org.xmind.ui.texteditor;

public interface IContentProposalListener3 {

    /**
     * A content proposal popup has been opened for content proposal assistance.
     * 
     * @param adapter
     *            the ContentProposalAdapter which is providing content proposal
     *            behavior to a control
     */
    public void proposalPopupOpened(ContentProposalAdapter adapter);

    /**
     * A content proposal popup has been closed.
     * 
     * @param adapter
     *            the ContentProposalAdapter which is providing content proposal
     *            behavior to a control
     */
    public void proposalPopupClosed(ContentProposalAdapter adapter);

}