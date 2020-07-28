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
package org.xmind.ui.style;

import static org.xmind.ui.style.Styles.LAYER_AFTER_ALL_VALUE;
import static org.xmind.ui.style.Styles.LAYER_BEFORE_DEFAULT_VALUE;
import static org.xmind.ui.style.Styles.LAYER_BEFORE_THEME_VALUE;
import static org.xmind.ui.style.Styles.LAYER_BEFORE_USER_VALUE;

import org.xmind.core.ISheet;
import org.xmind.core.ISheetComponent;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.gef.IViewer;
import org.xmind.gef.graphicalpolicy.AbstractStyleSelector;
import org.xmind.gef.graphicalpolicy.IStyleValueProvider;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public abstract class MindMapStyleSelectorBase extends AbstractStyleSelector {

    protected static final String FAMILY_UNKNOWN = "unknown"; //$NON-NLS-1$

    public String getAutoValue(IGraphicalPart part, String key,
            IStyleValueProvider defaultValueProvider) {
        String familyName = getFamilyName(part);
        if (familyName == null)
            return null;
        return getAutoValueByFamilyName(part, familyName, key,
                defaultValueProvider);
    }

    protected String getAutoValueByFamilyName(IGraphicalPart part,
            String familyName, String key,
            IStyleValueProvider defaultValueProvider) {
        String value = null;
        value = getThemeStyleValue(part, familyName, key);
        if (isValidValue(part, key, value))
            return value;

        if (defaultValueProvider != null) {
            value = defaultValueProvider.getValue(part, key);
            if (isValidValue(part, key, value)
                    || defaultValueProvider.isKeyInteresting(part, key))
                return value;
        }
        value = getDefaultStyleValue(part, familyName, key,
                defaultValueProvider);
        if (isValidValue(part, key, value))
            return value;

        return value;
    }

    protected String getDefaultStyleValue(IGraphicalPart part,
            String familyName, String key,
            IStyleValueProvider defaultValueProvider) {
        String value = getLayeredProperty(part, LAYER_BEFORE_DEFAULT_VALUE,
                familyName, key);
        if (isValidValue(part, key, value))
            return getCheckedValue(value);

        IStyle defaultStyle = getDefaultStyle(part, familyName);
        if (defaultStyle != null) {
            value = defaultStyle.getProperty(key);
            if (isValidValue(part, key, value))
                return value;
        }

        value = getLayeredProperty(part, LAYER_AFTER_ALL_VALUE, familyName,
                key);
        return null;
    }

    protected String getThemeStyleValue(IGraphicalPart part, String familyName,
            String key) {
        String value = getLayeredProperty(part, LAYER_BEFORE_THEME_VALUE,
                familyName, key);
        if (isValidValue(part, key, value))
            return getCheckedValue(value);

        IStyle themeStyle = getThemeStyle(part, familyName);
        if (themeStyle != null) {
            value = themeStyle.getProperty(key);
            if (isValidValue(part, key, value))
                return value;
        }
        return null;
    }

    protected IStyle getDefaultStyle(IGraphicalPart part, String familyName) {
        return MindMapUI.getResourceManager().getDefaultStyleSheet()
                .findStyle(familyName);
    }

    private IStyle getThemeStyle(IGraphicalPart part, String familyName) {
        ISheet sheet = getSheet(part);
        if (sheet != null) {
            IStyle theme = sheet.getTheme();
            if (theme != null) {
                return theme.getDefaultStyle(familyName);
            }
        }
        return null;
    }

    protected String getCheckedValue(String value) {
        if (Styles.NULL.equals(value))
            return null;
        return value;
    }

    protected ISheet getSheet(IGraphicalPart part) {
        ISheet sheet = (ISheet) part.getAdapter(ISheet.class);
        if (sheet != null)
            return sheet;

        Object model = MindMapUtils.getRealModel(part);
        if (model instanceof ISheet)
            return (ISheet) sheet;

        ISheetComponent sheetComponent = getSheetComponent(part);
        if (sheetComponent != null) {
            sheet = sheetComponent.getOwnedSheet();
            if (sheet != null)
                return sheet;
        }

        IViewer viewer = part.getSite().getViewer();
        if (viewer != null) {
            sheet = (ISheet) viewer.getAdapter(ISheet.class);
        }
        return sheet;
    }

    protected ISheetComponent getSheetComponent(IGraphicalPart part) {
        ISheetComponent sc = (ISheetComponent) part
                .getAdapter(ISheetComponent.class);
        if (sc != null)
            return sc;
        Object model = MindMapUtils.getRealModel(part);
        if (model instanceof ISheetComponent)
            return (ISheetComponent) model;
        return null;
    }

    public String getUserValue(IGraphicalPart part, String key) {
        String familyName = getFamilyName(part);
        if (familyName != null) {
            String value = getLayeredProperty(part, LAYER_BEFORE_USER_VALUE,
                    familyName, key);
            if (value != null)
                return getCheckedValue(value);
        }
        IStyle style = getStyle(part);
        return style == null ? null : style.getProperty(key);
    }

    protected IStyle getStyle(IGraphicalPart part) {
        IStyled styleOwner = getStyleOwner(part);
        if (styleOwner != null) {
            String styleId = styleOwner.getStyleId();
            if (styleId != null) {
                if (styleOwner instanceof IWorkbookComponent) {
                    IStyleSheet ss = ((IWorkbookComponent) styleOwner)
                            .getOwnedWorkbook().getStyleSheet();
                    if (ss != null) {
                        return ss.findStyle(styleId);
                    }
                }
            }
        }
        return null;
    }

    protected IStyled getStyleOwner(IGraphicalPart part) {
        IStyled styleOwner = (IStyled) part.getAdapter(IStyled.class);
        if (styleOwner != null)
            return styleOwner;
        Object model = MindMapUtils.getRealModel(part);
        if (model instanceof IStyled)
            return (IStyled) model;
        return null;
    }

    protected IWorkbook getOwnedWorkbook(IGraphicalPart part,
            IStyled styleOwner) {
        if (styleOwner instanceof IWorkbookComponent) {
            IWorkbook workbook = ((IWorkbookComponent) styleOwner)
                    .getOwnedWorkbook();
            if (workbook != null)
                return workbook;
        }
        IWorkbook workbook = (IWorkbook) part.getAdapter(IWorkbook.class);
        if (workbook != null)
            return workbook;

        IViewer viewer = part.getSite().getViewer();
        if (viewer != null) {
            ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
            if (sheet != null)
                return sheet.getOwnedWorkbook();
        }
        return null;
    }

    protected String getLayeredProperty(IGraphicalPart part, String layerName,
            String familyName, String key) {
        return null;
    };

    public abstract String getFamilyName(IGraphicalPart part);

}