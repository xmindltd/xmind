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

import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.xmind.core.IWorkbook;

/**
 * 
 * @author Frank Shaka
 * @since 3.0
 * @deprecated
 */
@Deprecated
public interface IWorkbookRefManager {

    /**
     * @deprecated
     */
    @Deprecated
    IWorkbookRef findRef(IWorkbook workbook);

    /**
     * @deprecated
     */
    @Deprecated
    IWorkbookRef createRef(IEditorInput editorInput, IEditorPart editor);

    /**
     * @deprecated
     */
    @Deprecated
    void disposeRef(IEditorInput editorInput, IEditorPart editor);

}
