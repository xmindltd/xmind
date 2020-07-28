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
package org.xmind.core;

/**
 * @author briansun
 * 
 */
public interface INotes extends IAdaptable, ITopicComponent {

    /**
     * Notes format for plain content
     */
    String PLAIN = "plain"; //$NON-NLS-1$

    /**
     * Notes format for html content
     */
    String HTML = "html"; //$NON-NLS-1$

    /**
     * @return
     */
    boolean isEmpty();

    /**
     * 
     * @param format
     * @param content
     */
    void setContent(String format, INotesContent content);

    /**
     * 
     * @see #PLAIN
     * @see #HTML
     * @param format
     * @return
     */
    INotesContent getContent(String format);

}