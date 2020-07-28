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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.draw2d.PositionConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.core.ISheet;
import org.xmind.core.IWorkbook;
import org.xmind.core.io.ByteArrayStorage;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.draw2d.decoration.IDecoration;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.graphicalpolicy.IStyleValueProvider;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.ui.decorations.IArrowDecoration;
import org.xmind.ui.decorations.IBoundaryDecoration;
import org.xmind.ui.decorations.IBranchConnectionDecoration;
import org.xmind.ui.decorations.IDecorationDescriptor;
import org.xmind.ui.decorations.IRelationshipDecoration;
import org.xmind.ui.decorations.ISummaryDecoration;
import org.xmind.ui.decorations.ITopicDecoration;
import org.xmind.ui.internal.decorations.ArrowDecorationAdapter;
import org.xmind.ui.internal.decorations.BoundaryDecorationAdapter;
import org.xmind.ui.internal.decorations.BranchConnectionDecorationAdapter;
import org.xmind.ui.internal.decorations.CurlySummaryDecoration;
import org.xmind.ui.internal.decorations.CurvedRelationshipDecoration;
import org.xmind.ui.internal.decorations.DefaultBranchDecoration;
import org.xmind.ui.internal.decorations.RelationshipDecorationAdapter;
import org.xmind.ui.internal.decorations.RoundedRectBoundaryDecoration;
import org.xmind.ui.internal.decorations.RoundedRectTopicDecoration;
import org.xmind.ui.internal.decorations.StraightBranchConnection;
import org.xmind.ui.internal.decorations.SummaryDecorationAdapter;
import org.xmind.ui.internal.decorations.TopicDecorationAdapter;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ColorUtils;
import org.xmind.ui.resources.FontUtils;
import org.xmind.ui.util.MindMapUtils;

public class StyleUtils {

    private StyleUtils() {
    }

    public static IStyleSelector getStyleSelector(IGraphicalPart part) {
        if (part == null)
            return null;
        return (IStyleSelector) part.getAdapter(IStyleSelector.class);
    }

    public static RGB getRGB(IGraphicalPart part, IStyleSelector ss, String key,
            String decorationId) {
        if (part != null && ss != null) {
            String value = ss.getStyleValue(part, key);
            return convertRGB(key, value);
        }
        return null;
    }

    public static RGB convertRGB(String key, String value) {
        if (Styles.NONE.equals(value))
            return null;
        if (Styles.SYSTEM.equals(value)) {
            return getSystemRGB(key, null);
        }
        return ColorUtils.toRGB(value);
    }

    public static Color getColor(IGraphicalPart part, IStyleSelector ss,
            String key, String decorationId, String defaultColor) {
        if (part != null && ss != null) {
            String value = ss.getStyleValue(part, key,
                    getDecorationDefaultValueProvider(decorationId, key));
            if (value != null)
                return convertColor(key, value);
        }
        return convertColor(key, defaultColor);
    }

    public static Color convertColor(String key, String value) {
        if (Styles.NONE.equals(value)) {
            return null;
        } else if (Styles.SYSTEM.equals(value)) {
            return getSystemColor(key, null);
        }
        return ColorUtils.getColor(value);
    }

    private static RGB getSystemRGB(String key, RGB defaultValue) {
        if (Styles.FillColor.equals(key)) {
            return Display.getCurrent()
                    .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND).getRGB();
        } else if (Styles.LineColor.equals(key)) {
            return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BORDER)
                    .getRGB();
        } else if (Styles.TextColor.equals(key)) {
            return Display.getCurrent()
                    .getSystemColor(SWT.COLOR_WIDGET_FOREGROUND).getRGB();
        }
        return defaultValue;
    }

    public static Color getSystemColor(String key, Color defaultValue) {
        if (Styles.FillColor.equals(key)) {
            return Display.getCurrent()
                    .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
        } else if (Styles.LineColor.equals(key)) {
            return Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BORDER);
        } else if (Styles.TextColor.equals(key)) {
            return Display.getCurrent()
                    .getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);
        }
        return defaultValue;
    }

    public static int getInteger(IGraphicalPart part, IStyleSelector ss,
            String key, int defaultValue) {
        return getInteger(part, ss, key, null, defaultValue);
    }

    public static int getInteger(IGraphicalPart part, IStyleSelector ss,
            String key, String decorationId, int defaultValue) {
        if (part != null && ss != null) {
            String value = ss.getStyleValue(part, key,
                    getDecorationDefaultValueProvider(decorationId, key));
            if (value != null) {
                value = trimNumber(value);
                try {
                    return Integer.parseInt(value);
                } catch (Exception e) {
                }
            }
        }
        return defaultValue;
    }

    public static double getDouble(IGraphicalPart part, IStyleSelector ss,
            String key, double defaultValue) {
        if (part != null && ss != null) {
            String value = ss.getStyleValue(part, key);
            if (value != null) {
                value = trimNumber(value);
                try {
                    return Double.parseDouble(value);
                } catch (Exception e) {
                }
            }
        }
        return defaultValue;
    }

    public static Font getFont(IGraphicalPart part, IStyleSelector ss,
            String key) {
        if (part != null && ss != null) {
            String value = ss.getStyleValue(part, key);
            if (value != null) {
                Font f = FontUtils.getFont(value);
                if (f != null)
                    return f;
            }
        }
        return null;
    }

    public static Font getFont(IGraphicalPart part, IStyleSelector ss,
            String key, Font defaultValue) {
        Font f = getFont(part, ss, key);
        if (f != null)
            return f;
        return defaultValue;
    }

    public static String getString(IGraphicalPart part, IStyleSelector ss,
            String key, String defaultValue) {
        if (part != null && ss != null) {
            String value = ss.getStyleValue(part, key);
            if (value != null)
                return value;
        }
        return defaultValue;
    }

    public static TextStyleData getTextStyleData(IGraphicalPart part,
            IStyleSelector ss, TextStyleData defaultData) {
        Object cache = MindMapUtils.getCache(part, MindMapUI.CACHE_TEXT_STYLE);
        if (cache instanceof TextStyleData)
            return (TextStyleData) cache;

        TextStyleData data;
        if (defaultData == null) {
            data = new TextStyleData();
            defaultData = data;
        } else {
            data = new TextStyleData(defaultData);
        }

        String name = getString(part, ss, Styles.FontFamily, defaultData.name);
        String availableFontName = FontUtils.getAAvailableFontNameFor(name);
        name = availableFontName != null ? availableFontName : name;

        if (Styles.SYSTEM.equals(name)) {
            name = JFaceResources.getDefaultFont().getFontData()[0].getName();
        }
        data.name = name;

        data.height = getInteger(part, ss, Styles.FontSize, defaultData.height);

        String weight = getString(part, ss, Styles.FontWeight, null);
        if (weight != null) {
            data.bold = weight.contains(Styles.FONT_WEIGHT_BOLD);
        }

        String style = getString(part, ss, Styles.FontStyle, null);
        if (style != null) {
            data.italic = style.contains(Styles.FONT_STYLE_ITALIC);
        }

        RGB c = getRGB(part, ss, Styles.TextColor, null);
        if (c != null) {
            data.color = c;
        }

        String decoration = getString(part, ss, Styles.TextDecoration, null);
        if (decoration != null) {
            data.underline = decoration
                    .contains(Styles.TEXT_DECORATION_UNDERLINE);
            data.strikeout = decoration
                    .contains(Styles.TEXT_DECORATION_LINE_THROUGH);
        }

        int align = getAlignValue(part, ss, Styles.TextAlign);
        data.align = align;
        int textCase = getCaseValue(part, ss, Styles.TextCase);
        data.textCase = textCase;
        return data;
    }

    public static int getAlign(IGraphicalPart part, IStyleSelector ss,
            String key) {
        return getAlignValue(part, ss, Styles.TextAlign);
    }

    private static int getAlignValue(IGraphicalPart part, IStyleSelector ss,
            String key) {
        // TODO Auto-generated method stub
        if (part != null && ss != null) {
            String value = ss.getStyleValue(part, key);
            if (Styles.ALIGN_CENTER.equals(value))
                return PositionConstants.CENTER;
            else if (Styles.ALIGN_RIGHT.equals(value))
                return PositionConstants.RIGHT;
            else
                return PositionConstants.LEFT;
        }
        return PositionConstants.LEFT;
    }

    public static int getCaseValue(IGraphicalPart part, IStyleSelector ss,
            String key) {
        if (part != null && ss != null) {
            String value = ss.getStyleValue(part, key);
            if (Styles.MANUAL.equals(value))
                return GEF.MANUAL;
            else if (Styles.UPPERCASE.equals(value))
                return GEF.UPPERCASE;
            else if (Styles.LOWERCASE.equals(value))
                return GEF.LOWERCASE;
            else if (Styles.CAPITALIZE.equals(value))
                return GEF.CAPITALIZE;
        }
        return GEF.MANUAL;
    }

    public static FontData getCompositeFontData(IGraphicalPart part,
            IStyleSelector ss, FontData defaultFontData) {
        if (defaultFontData == null)
            defaultFontData = JFaceResources.getDefaultFont().getFontData()[0];
        TextStyleData data = getTextStyleData(part, ss,
                new TextStyleData(defaultFontData));
        return data.createFontData();
    }

    public static Font getCompositeFont(IGraphicalPart part, IStyleSelector ss,
            Font defaultFont) {
        if (defaultFont == null)
            defaultFont = JFaceResources.getDefaultFont();
        TextStyleData data = getTextStyleData(part, ss,
                new TextStyleData(defaultFont.getFontData()[0]));
        return data.createFont();
    }

    public static TextStyle getTextStyle(IGraphicalPart part,
            IStyleSelector ss) {
        Object cache = MindMapUtils.getCache(part, MindMapUI.CACHE_TEXT_STYLE);
        if (cache instanceof TextStyle)
            return (TextStyle) cache;

        TextStyleData data = getTextStyleData(part, ss, null);
        return data.createTextStyle();
    }

    public static int getMajorSpacing(IGraphicalPart part, int defaultValue) {
        IStyleSelector ss = getStyleSelector(part);
        String connectionId = getString(part, ss, Styles.LineClass, null);
        return getInteger(part, ss, Styles.MajorSpacing, connectionId,
                defaultValue);
    }

    public static int getLineStyle(IGraphicalPart part, IStyleSelector ss,
            String decorationId, int defaultValue) {
        return getLineStyle(part, ss, Styles.LinePattern, decorationId,
                defaultValue);
    }

    public static int getLineStyle(IGraphicalPart part, IStyleSelector ss,
            String key, String decorationId, int defaultValue) {
        if (part != null && ss != null) {
            String value = ss.getStyleValue(part, key,
                    getDecorationDefaultValueProvider(decorationId, key));
            if (value != null) {
                return toSWTLineStyle(value, defaultValue);
            }
        }
        return defaultValue;
    }

    public static int toSWTLineStyle(String value, int defaultValue) {
        if (Styles.LINE_PATTERN_SOLID.equals(value))
            return SWT.LINE_SOLID;
        if (Styles.LINE_PATTERN_DASH.equals(value))
            return SWT.LINE_DASH;
        if (Styles.LINE_PATTERN_DASH_DOT.equals(value))
            return SWT.LINE_DASHDOT;
        if (Styles.LINE_PATTERN_DASH_DOT_DOT.equals(value))
            return SWT.LINE_DASHDOTDOT;
        if (Styles.LINE_PATTERN_DOT.equals(value))
            return SWT.LINE_DOT;
        return defaultValue;
    }

    public static int getAlpha(IGraphicalPart part, IStyleSelector ss,
            int defaultValue) {
        return getAlpha(part, ss, Styles.Opacity, defaultValue);
    }

    public static int getAlpha(IGraphicalPart part, IStyleSelector ss,
            String key, int defaultValue) {
        if (part != null && ss != null) {
            double opacity = getDouble(part, ss, key, -1);
            if (opacity >= 0) {
                return (int) (opacity * 255.0d);
            }
        }
        return defaultValue;
    }

    private static Pattern trimNumber = null;

    public static String trimNumber(String value) {
        if (value != null) {
            value = value.trim();
            if (trimNumber == null) {
                // extract digits from the head to the first non-digit charater
                trimNumber = Pattern.compile("^([\\d\\.]+)([^\\d\\.]*)"); //$NON-NLS-1$
            }
            Matcher m = trimNumber.matcher(value);
            if (m.find()) {
                value = m.group(1);
            }
        }
        return value;
    }

    public static boolean isSameDecoration(IDecoration oldDecoration,
            String newId) {
        if (newId != null) {
            if (oldDecoration == null)
                return false;
            String oldId = oldDecoration.getId();
            return newId.equals(oldId);
        }
        return oldDecoration == null;
    }

    public static IDecoration createDecoration(IGraphicalPart part, String id) {
        IDecoration decoration = MindMapUI.getMindMapDecorationFactory()
                .createDecoration(id, part);
        if (decoration != null)
            decoration.setId(id);
        return decoration;
    }

    public static IArrowDecoration createArrowDecoration(IGraphicalPart part,
            String id) {
        IDecoration decoration = id == null ? null : createDecoration(part, id);
        if (decoration instanceof IArrowDecoration)
            return (IArrowDecoration) decoration;
        if (decoration != null)
            return new ArrowDecorationAdapter(decoration);
        return null;
    }

    public static IBoundaryDecoration createBoundaryDecoration(
            IGraphicalPart part, String id) {
        IDecoration decoration = id == null ? null : createDecoration(part, id);
        if (decoration instanceof IBoundaryDecoration)
            return (IBoundaryDecoration) decoration;
        if (decoration != null)
            return new BoundaryDecorationAdapter(decoration);
        return new RoundedRectBoundaryDecoration(id);
    }

    public static IBranchConnectionDecoration createBranchConnection(
            IGraphicalPart part, String id) {
        IDecoration decoration = id == null ? null : createDecoration(part, id);
        if (decoration instanceof IBranchConnectionDecoration)
            return (IBranchConnectionDecoration) decoration;
        if (decoration != null)
            return new BranchConnectionDecorationAdapter(decoration);
        return new StraightBranchConnection(id);
    }

    public static IDecoration createBranchDecoration(IGraphicalPart part,
            String id) {
        IDecoration decoration = id == null ? null : createDecoration(part, id);
        if (decoration != null)
            return decoration;
        if (part instanceof IBranchPart)
            return new DefaultBranchDecoration((IBranchPart) part, id);
        return null;
    }

    public static IRelationshipDecoration createRelationshipDecoration(
            IGraphicalPart part, String id) {
        IDecoration decoration = id == null ? null : createDecoration(part, id);
        if (decoration instanceof IRelationshipDecoration)
            return (IRelationshipDecoration) decoration;
        if (decoration != null)
            return new RelationshipDecorationAdapter(decoration);
        return new CurvedRelationshipDecoration(id);
    }

    public static ISummaryDecoration createSummaryDecoration(
            IGraphicalPart part, String id) {
        IDecoration decoration = id == null ? null : createDecoration(part, id);
        if (decoration instanceof ISummaryDecoration)
            return (ISummaryDecoration) decoration;
        if (decoration != null)
            return new SummaryDecorationAdapter(decoration);
        return new CurlySummaryDecoration(id);
    }

    public static ITopicDecoration createTopicDecoration(IGraphicalPart part,
            String id) {
        IDecoration decoration = id == null ? null : createDecoration(part, id);
        if (decoration instanceof ITopicDecoration)
            return (ITopicDecoration) decoration;
        if (decoration != null)
            return new TopicDecorationAdapter(decoration);
        return new RoundedRectTopicDecoration(id);
    }

    public static boolean isBranchLineTapered(IBranchPart branch,
            IStyleSelector ss) {
        if (branch != null && ss != null && branch.isCentral()) {
            String value = ss.getUserValue(branch, Styles.LineTapered);
            if (value == null) {
                // compatible with former versions:
                IViewer viewer = branch.getSite().getViewer();
                if (viewer != null) {
                    ISheetPart sheet = (ISheetPart) viewer
                            .getAdapter(ISheetPart.class);
                    if (sheet != null) {
                        value = getStyleSelector(sheet).getStyleValue(sheet,
                                Styles.LineTapered);
                    }
                }
            }
            if (value == null) {
                value = ss.getStyleValue(branch, Styles.LineTapered);
            }
            return value != null && value.contains(Styles.TAPERED);
        }
        return false;
    }

    private static String[] getAncestorMultiLineColor(IBranchPart branch) {
        if (branch == null)
            return null;

        IBranchPart parentBranch = branch.getParentBranch();
        if (parentBranch == null)
            return null;

        IStyleSelector ss = getStyleSelector(parentBranch);
        if (ss == null)
            return null;

        String value = ss.getStyleValue(parentBranch, Styles.MultiLineColors);
        if (value != null && !Styles.NONE.equals(value)) {
            int index = parentBranch.getSubBranches().indexOf(branch);
            return new String[] { String.valueOf(index), value };
        }

        return getAncestorMultiLineColor(parentBranch);
    }

    private static int getMultiColorsIndex(IBranchPart branch) {
        IBranchPart parentBranch = branch.getParentBranch();
        if (parentBranch == null)
            return -1;

        if (MindMapUI.BRANCH_CENTRAL.equals(parentBranch.getBranchType())) {
            return parentBranch.getSubBranches().indexOf(branch);
        } else {
            return getMultiColorsIndex(parentBranch);
        }
    }

    public static String getIndexedBranchLineColor(IBranchPart branch) {
        IBranchPart parent = branch.getParentBranch();
        if (parent != null) {
            int index = parent.getSubBranches().indexOf(branch);
            if (index >= 0) {
                String value = null;

                String[] indexAndValue = getAncestorMultiLineColor(branch);
                if (indexAndValue != null && indexAndValue.length == 2) {
                    try {
                        index = Integer.parseInt(indexAndValue[0]);
                        value = indexAndValue[1];
                    } catch (NumberFormatException ignore) {
                    }
                }
                ISheetPart sheet = null;
                if (value == null || Styles.NONE.equals(value)) {
                    // compatible with former versions:
                    IViewer viewer = parent.getSite().getViewer();
                    if (viewer != null) {
                        sheet = (ISheetPart) viewer
                                .getAdapter(ISheetPart.class);
                        if (sheet != null) {
                            value = StyleUtils.getStyleSelector(sheet)
                                    .getStyleValue(sheet,
                                            Styles.MultiLineColors);
                            if (value != null) {
                                index = getMultiColorsIndex(branch);
                            }
                        }
                    }
                }
                if (index >= 0 && value != null) {
                    if (!Styles.NONE.equals(value)) {
                        value = value.trim();
                        // split by whitespaces
                        String[] colors = value.split("[\\s]+"); //$NON-NLS-1$
                        if (colors.length > 0) {
                            index %= colors.length;
                            String color = colors[index].trim();
                            return color;
                        }
                    }
                }
                return null;
            }
        }
        return null;
    }

    public static Color getBranchConnectionColor(final IBranchPart branch,
            final IStyleSelector ss, final IBranchPart child,
            final int childIndex, Color defaultColor) {
        if (child != null) {
            IStyleSelector childSS = getStyleSelector(child);
            String shapeId = getString(child, childSS, Styles.LineClass, null);
            String defaultValue = defaultColor == null ? null
                    : ColorUtils.toString(defaultColor);
            return getColor(child, childSS, Styles.LineColor, shapeId,
                    defaultValue);
        }
        return defaultColor;
    }

    public static String addUnitPixel(int number) {
        return String.valueOf(number) + "px"; //$NON-NLS-1$
    }

    public static String addUnitPoint(int number) {
        return String.valueOf(number) + "pt"; //$NON-NLS-1$
    }

//    public static String addUnit(double number) {
//        return Double.toString(number) + "px"; //$NON-NLS-1$
//    }

    public static IStyleValueProvider getDecorationDefaultValueProvider(
            String decorationId, String key) {
        if (decorationId != null) {
            IDecorationDescriptor descriptor = MindMapUI.getDecorationManager()
                    .getDecorationDescriptor(decorationId);
            if (descriptor != null) {
                return descriptor.getDefaultValueProvider(key);
            }
        }
        return null;
    }

    public static String toTextDecoration(boolean underline,
            boolean strikeout) {
        if (underline || strikeout) {
            if (!underline)
                return Styles.TEXT_DECORATION_LINE_THROUGH;
            if (!strikeout)
                return Styles.TEXT_DECORATION_UNDERLINE;
            return Styles.TEXT_UNDERLINE_AND_LINE_THROUGH;
        }
        return Styles.NORMAL;
    }

    public static boolean isBold(IStyle style) {
        if (style != null) {
            String weight = style.getProperty(Styles.FontWeight);
            return weight != null && weight.contains(Styles.FONT_WEIGHT_BOLD);
        }
        return false;
    }

    public static boolean isItalic(IStyle style) {
        if (style != null) {
            String weight = style.getProperty(Styles.FontStyle);
            return weight != null && weight.contains(Styles.FONT_STYLE_ITALIC);
        }
        return false;
    }

    public static boolean isUnderline(IStyle style) {
        if (style != null) {
            String weight = style.getProperty(Styles.TextDecoration);
            return weight != null
                    && weight.contains(Styles.TEXT_DECORATION_UNDERLINE);
        }
        return false;
    }

    public static boolean isStrikeout(IStyle style) {
        if (style != null) {
            String weight = style.getProperty(Styles.TextDecoration);
            return weight != null
                    && weight.contains(Styles.TEXT_DECORATION_LINE_THROUGH);
        }
        return false;
    }

    public static void setTheme(ISheet sheet, IStyle theme) {
        if (sheet == null)
            return;

        if (theme == null || theme.isEmpty()) {
            sheet.setThemeId(null);
        } else {
            IWorkbook workbook = sheet.getOwnedWorkbook();
            IStyleSheet styleSheet = workbook.getStyleSheet();
            theme = styleSheet.importStyle(theme);
            if (theme == null || theme.isEmpty()) {
                sheet.setThemeId(null);
            } else {
                sheet.setThemeId(theme.getId());
                setThemeStyles(workbook, styleSheet, sheet, theme,
                        Styles.MultiLineColors, Styles.LineTapered);
            }
        }
    }

    private static void setThemeStyles(IWorkbook workbook,
            IStyleSheet styleSheet, ISheet sheet, IStyle theme,
            String... sheetStyleNames) {
        IStyle sheetTheme = theme.getDefaultStyle(Styles.FAMILY_MAP);
        if (sheetTheme == null)
            return;

        IStyle sourceStyle = styleSheet.findStyle(sheet.getStyleId());

        IWorkbook tempWorkbook = Core.getWorkbookBuilder()
                .createWorkbook(new ByteArrayStorage());
        IStyle tempStyle = sourceStyle == null
                ? tempWorkbook.getStyleSheet().createStyle(sheet.getStyleType())
                : tempWorkbook.getStyleSheet().importStyle(sourceStyle);

        for (String styleName : sheetStyleNames) {
            String value = sheetTheme.getProperty(styleName);
            tempStyle.setProperty(styleName, value);
        }
        if (tempStyle.isEmpty()) {
            sheet.setStyleId(null);
        } else {
            IStyle targetStyle = workbook.getStyleSheet()
                    .importStyle(tempStyle);
            if (targetStyle != null) {
                sheet.setStyleId(targetStyle.getId());
            }
        }
    }

}
