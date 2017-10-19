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
package org.xmind.core.internal.dom;

import java.io.IOException;
import java.util.Collection;

import org.xmind.core.Core;
import org.xmind.core.ICloneData;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.core.internal.CloneData;
import org.xmind.core.marker.IMarker;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.CloneHandler;

public class WorkbookUtilsImpl {

    private WorkbookUtilsImpl() {
    }

    public static ICloneData clone(IWorkbook targetWorkbook,
            Collection<? extends Object> sources, ICloneData prevResult) {
        CloneData result = new CloneData(sources, prevResult);
        for (Object source : sources) {
            if (result.get(source) == null) {
                CloneHandler handler = new CloneHandler(result);
                if (source instanceof IWorkbookComponent) {
                    IWorkbook sourceWorkbook = ((IWorkbookComponent) source)
                            .getOwnedWorkbook();
                    handler.withWorkbooks(sourceWorkbook, targetWorkbook);
                } else if (source instanceof IMarker) {
                    handler.withMarkerSheets(((IMarker) source).getOwnedSheet(),
                            targetWorkbook.getMarkerSheet());
                } else {
                    /// unrecognized object, skip it
                    continue;
                }

                try {
                    handler.cloneObject(source);
                } catch (IOException e) {
                    Core.getLogger().log(e);
                }
            }
        }
        return result;
    }

    public static void increaseStyleRef(WorkbookImpl workbook, IStyled styled) {
        if (workbook == null || styled == null)
            return;

        String styleId = styled.getStyleId();
        if (styleId == null)
            return;

        workbook.getStyleRefCounter().increaseRef(styleId);
    }

    public static void decreaseStyleRef(WorkbookImpl workbook, IStyled styled) {
        if (workbook == null || styled == null)
            return;

        String styleId = styled.getStyleId();
        if (styleId == null)
            return;

        workbook.getStyleRefCounter().decreaseRef(styleId);
    }

}
