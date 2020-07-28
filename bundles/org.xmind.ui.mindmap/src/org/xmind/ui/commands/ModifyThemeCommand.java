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
package org.xmind.ui.commands;

import java.util.Collection;

import org.xmind.core.ISheet;
import org.xmind.gef.command.ModifyCommand;

public class ModifyThemeCommand extends ModifyCommand {

    public ModifyThemeCommand(ISheet sheet, String themeId) {
        super(sheet, themeId);
    }

    public ModifyThemeCommand(Collection<? extends ISheet> sheets,
            String themeId) {
        super(sheets, themeId);
    }

    protected Object getValue(Object source) {
        if (source instanceof ISheet)
            return ((ISheet) source).getThemeId();
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof ISheet) {
            ISheet sheet = (ISheet) source;
            if (value == null) {
                sheet.setThemeId(null);
            } else if (value instanceof String) {
                sheet.setThemeId((String) value);
            }
        }
    }
}