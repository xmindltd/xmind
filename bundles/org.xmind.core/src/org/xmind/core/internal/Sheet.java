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
package org.xmind.core.internal;

import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;

/**
 * @author briansun
 * 
 */
public abstract class Sheet extends AbstractWorkbookComponent
        implements ISheet {

    public String getStyleType() {
        return IStyle.MAP;
    }

    /**
     * Clients may override this method.
     * 
     * @see org.xmind.core.ITitled#getTitleText()
     */
    public String getTitleText() {
        String t = getLocalTitleText();
        return t == null ? "" : t; //$NON-NLS-1$
    }

    /**
     * @see org.xmind.core.ITitled#hasTitle()
     */
    public boolean hasTitle() {
        return getLocalTitleText() != null;
    }

    /**
     * @return
     */
    protected abstract String getLocalTitleText();

    public int getIndex() {
        IWorkbook w = getParent();
        if (w != null) {
            return w.getSheets().indexOf(this);
        }
        return -1;
    }

    public <T> T getAdapter(Class<T> adapter) {
        if (adapter == ITopic.class)
            return adapter.cast(getRootTopic());

        return super.getAdapter(adapter);
    }

    public IStyle getTheme() {
        String themeId = getThemeId();
        if (themeId == null)
            return null;

        IStyleSheet styleSheet = getOwnedWorkbook().getStyleSheet();
        if (styleSheet == null)
            return null;

        return styleSheet.findStyle(themeId);
    }

    public String toString() {
        return "SHEET (" + getTitleText() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}