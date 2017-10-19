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
package org.xmind.ui.commands;

import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyled;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.command.ModifyCommand;

public class ModifyStyleCommand extends ModifyCommand {

    public ModifyStyleCommand(IStyled source, String newStyleId) {
        super(source, newStyleId);
    }

    public ModifyStyleCommand(ISourceProvider sourceProvider, String newStyleId) {
        super(sourceProvider, newStyleId);
    }

    public ModifyStyleCommand(IStyled source, ISourceProvider newStyleProvider) {
        super(source, newStyleProvider);
    }

    public ModifyStyleCommand(ISourceProvider sourceProvider,
            ISourceProvider newStyleProvider) {
        super(sourceProvider, newStyleProvider);
    }

    protected Object getValue(Object source) {
        if (source instanceof IStyled) {
            return ((IStyled) source).getStyleId();
        }
        return null;
    }

    protected void setValue(Object source, Object value) {
        if (source instanceof IStyled) {
            IStyled styledSource = (IStyled) source;
            if (value instanceof ISourceProvider) {
                Object newStyle = ((ISourceProvider) value).getSource();
                if (newStyle instanceof String) {
                    value = (String) newStyle;
                } else if (newStyle instanceof IStyle) {
                    value = ((IStyle) newStyle).getId();
                } else if (newStyle instanceof IStyled) {
                    value = ((IStyled) newStyle).getStyleId();
                }
            }
            if (value == null || value instanceof String) {
                styledSource.setStyleId((String) value);
            }
        }
    }

}