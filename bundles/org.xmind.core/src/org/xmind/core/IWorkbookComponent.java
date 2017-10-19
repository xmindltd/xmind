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
package org.xmind.core;

public interface IWorkbookComponent {

    /**
     * Gets the workbook who owns this component.
     * 
     * @return the owned workbook
     */
    IWorkbook getOwnedWorkbook();

    /**
     * Determines whether this component is isolated from the owned workbook.
     * 
     * <p>
     * Isolated objects are objects that is owned by this workbook but will not
     * be saved with this workbook.
     * </p>
     * <p>
     * To isolate an object, generally call <code>removeXXX()</code> from its
     * parent. To attach an isolated object, generally call corresponding
     * <code>addXXX()</code> from a parental object.
     * </p>
     * 
     * @return <code>true</code> if this component is isolated from its owned
     *         workbook, <code>false</code> otherwise
     */
    boolean isOrphan();

}