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

package org.xmind.ui;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author Frank Shaka
 * 
 */
public interface IWordContext {

    String getContent();

    String getName();

    ImageDescriptor getIcon();

    void reveal();

    void revealWord(int start, int length);

    boolean replaceWord(int start, int length, String replacement);

}
