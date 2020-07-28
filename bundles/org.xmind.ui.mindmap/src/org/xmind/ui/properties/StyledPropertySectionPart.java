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
package org.xmind.ui.properties;

import org.eclipse.swt.graphics.RGB;
import org.xmind.core.Core;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.style.IStyled;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.graphicalpolicy.IStyleValueProvider;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.ui.color.ColorPicker;
import org.xmind.ui.color.ColorSelection;
import org.xmind.ui.color.IColorSelection;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.Styles;

public abstract class StyledPropertySectionPart
        extends MindMapPropertySectionPartBase {

    protected String getStyleValue(String styleKey, String decorationId) {
        String value = null;
        IViewer viewer = getActiveViewer();
        if (viewer != null) {
            IStyleValueProvider defaultValueProvider = StyleUtils
                    .getDecorationDefaultValueProvider(decorationId, styleKey);
            for (Object o : getSelectedElements()) {
                IGraphicalPart part = getGraphicalPart(o, viewer);
                if (part != null) {
                    IStyleSelector ss = getStyleSelector(part);
                    String v = ss.getStyleValue(part, styleKey,
                            defaultValueProvider);
                    if (v == null)
                        return null;
                    if (value == null) {
                        value = v;
                    } else if (!value.equals(v)) {
                        return null;
                    }
                }
            }
        }
        return value;
    }

    protected String getAutoValue(String styleKey, String decorationId) {
        String value = null;
        IViewer viewer = getActiveViewer();
        if (viewer != null) {
            IStyleValueProvider defaultValueProvider = StyleUtils
                    .getDecorationDefaultValueProvider(decorationId, styleKey);
            for (Object o : getSelectedElements()) {
                IGraphicalPart part = getGraphicalPart(o, viewer);
                if (part != null) {
                    IStyleSelector ss = getStyleSelector(part);
                    String v = ss.getAutoValue(part, styleKey,
                            defaultValueProvider);
                    if (v == null)
                        return null;
                    if (value == null) {
                        value = v;
                    } else if (!value.equals(v)) {
                        return null;
                    }
                }
            }
        }
        return value;
    }

    protected String getUserValue(String styleKey) {
        String value = null;
        IViewer viewer = getActiveViewer();
        if (viewer != null) {
            for (Object o : getSelectedElements()) {
                IGraphicalPart part = getGraphicalPart(o, viewer);
                if (part != null) {
                    IStyleSelector ss = getStyleSelector(part);
                    String v = ss.getUserValue(part, styleKey);
                    if (v == null)
                        return null;
                    if (value == null) {
                        value = v;
                    } else if (!value.equals(v)) {
                        return null;
                    }
                }
            }
        }
        return value;
    }

    protected void updateColorPicker(ColorPicker picker, String styleKey,
            String decorationId) {
        if (picker == null) {
            return;
        }

        String autoColor = getAutoValue(styleKey, decorationId);
        picker.setAutoColor(StyleUtils.convertRGB(styleKey, autoColor));
        String userColor = getUserValue(styleKey);
        int type;
        if (userColor == null) {
            type = IColorSelection.AUTO;
            userColor = autoColor;
        } else {
            type = IColorSelection.CUSTOM;
        }
        if (type != IColorSelection.AUTO && Styles.NONE.equals(userColor)) {
            type = IColorSelection.NONE;
        }
        RGB color = StyleUtils.convertRGB(Styles.TextColor, userColor);
        picker.setSelection(new ColorSelection(type, color));
    }

    protected void changeColor(IColorSelection selection, String styleKey,
            String commandLabel) {
        String color;
        if (selection.isAutomatic()) {
            color = null;
        } else if (selection.isNone()) {
            color = Styles.NONE;
        } else {
            color = ColorUtils.toString(selection.getColor());
        }
        Request request = createStyleRequest(commandLabel);
        addStyle(request, styleKey, color);
        sendRequest(request);
    }

    protected boolean isPropertyModifiable(String propertyKey,
            String secondaryKey) {
        IGraphicalViewer viewer = getActiveViewer();
        if (viewer == null)
            return false;
        for (Object o : getSelectedElements()) {
            IPart part = viewer.findPart(o);
            if (part == null || !(part instanceof ITopicPart))
                return false;
            IBranchPart branch = ((ITopicPart) part).getOwnerBranch();
            if (branch == null)
                return false;
            if (!branch.getBranchPolicy().isPropertyModifiable(branch,
                    propertyKey, secondaryKey))
                return false;
        }
        return true;
    }

    protected void registerEventListener(Object source,
            ICoreEventRegister register) {
        if (source instanceof IStyled) {
            register.register(Core.Style);
        }
    }

    protected Request createStyleRequest(String commandLabel) {
        return fillTargets(new Request(MindMapUI.REQ_MODIFY_STYLE))
                .setParameter(MindMapUI.PARAM_COMMAND_LABEL, commandLabel);
    }

    protected Request addStyle(Request request, String styleKey, String value) {
        return request.setParameter(MindMapUI.PARAM_STYLE_PREFIX + styleKey,
                value);
    }

}
