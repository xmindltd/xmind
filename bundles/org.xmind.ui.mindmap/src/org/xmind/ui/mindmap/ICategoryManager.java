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
package org.xmind.ui.mindmap;

public interface ICategoryManager {

    String MULTIPLE_CATEGORIES = "multiple categories"; //$NON-NLS-1$

    String NO_CATEGORY = "no category"; //$NON-NLS-1$

    String UNKNOWN_CATEGORY = "unknown category"; //$NON-NLS-1$

    String[] getAllCategories();

    boolean belongsToCategory(Object element, String category);

    ICategoryAnalyzation analyze(Object[] elements);

    String getCategoryName(String category);

}