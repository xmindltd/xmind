package org.xmind.ui.internal.imports.lighten;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.xmind.core.IControlPoint;
import org.xmind.core.INotes;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.internal.dom.DOMConstants;
import org.xmind.core.internal.dom.PlainNotesContentImpl;
import org.xmind.core.internal.dom.SheetImpl;
import org.xmind.core.internal.dom.TopicExtensionElementImpl;
import org.xmind.core.internal.dom.TopicExtensionImpl;
import org.xmind.core.internal.dom.TopicImpl;
import org.xmind.core.internal.dom.WorkbookImpl;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.Point;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.style.Styles;
import org.xmind.ui.wizards.MindMapImporter;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class LightenImporter extends MindMapImporter implements ErrorHandler {

    private ISheet targetSheet;
    private IStyleSheet targetStyleSheet;
    private IStyle theme;
    private Map<IStyled, IStyle> styleMap;

    private static final String PIXEL_POINT = "pt"; //$NON-NLS-1$

    public LightenImporter(String sourcePath, IWorkbook targetWorkbook) {
        super(sourcePath, targetWorkbook);
    }

    @Override
    public void build() throws InvocationTargetException, InterruptedException {
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_IMPORT,
                UserDataConstants.IMPORT_FROM_LIGHTEN);
        ZipInputStream zis = null;
        try {
            getMonitor().beginTask(null, 100);
            zis = new ZipInputStream(new FileInputStream(getSourcePath()));
            ZipEntry entry;
            JSONObject json = null;
            while ((entry = zis.getNextEntry()) != null)
                if (LightenConstants.CONTENT_FILE_NAME
                        .equals(entry.getName())) {
                    getMonitor().worked(40);
                    BufferedInputStream bis = new BufferedInputStream(zis);
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(bis, "utf-8")); //$NON-NLS-1$
                    StringBuilder sb = new StringBuilder();
                    String read;
                    while ((read = reader.readLine()) != null) {
                        sb.append(read).append('\n');
                    }
                    json = new JSONObject(sb.toString());
                    getMonitor().worked(10);
                }
            zis.close();
            zis = null;

            if (json != null) {
                addTargetSheet(parseJsonIntoWorkbook(json));
            }
            getMonitor().worked(50);
            getMonitor().done();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zis != null) {
                try {
                    zis.closeEntry();
                    zis.close();
                } catch (Exception e) {
                    zis = null;
                }
            }
        }
        postBuilded();
    }

    private ISheet parseJsonIntoWorkbook(JSONObject json) {
        WorkbookImpl workbook = (WorkbookImpl) getTargetWorkbook();
        targetSheet = workbook.createSheet();
        SheetImpl sheet = (SheetImpl) targetSheet;
        sheet.setTitleText(getSuggestedSheetTitle());

        if (targetStyleSheet == null) {
            targetStyleSheet = workbook.getStyleSheet();
        }
        if (styleMap == null) {
            styleMap = new HashMap<IStyled, IStyle>();
        }

        if (json.has(LightenConstants.SHEET_ATTR_SKE_THEME))
            parseTheme(
                    json.getJSONObject(LightenConstants.SHEET_ATTR_SKE_THEME));
        if (json.has(LightenConstants.SHEET_ATTR_UPDATE_TIME))
            sheet.getImplementation().setAttribute(DOMConstants.ATTR_TIMESTAMP,
                    ((Long) ((Double) (json.getDouble(
                            LightenConstants.SHEET_ATTR_UPDATE_TIME) * 1000))
                                    .longValue()).toString());

        if (json.has(LightenConstants.SHEET_ATTR_ROOT)) {
            JSONObject rootTopicJson = (JSONObject) json
                    .get(LightenConstants.SHEET_ATTR_ROOT);
            ITopic rootTopic = parseTopic(rootTopicJson, sheet);
            sheet.replaceRootTopic(rootTopic);
            if (json.has(LightenConstants.SHEET_ATTR_RIGHT_NUM)) {
                TopicExtensionImpl topicExtension = (TopicExtensionImpl) rootTopic
                        .createExtension("org.xmind.ui.map.unbalanced"); //$NON-NLS-1$
                TopicExtensionElementImpl element = (TopicExtensionElementImpl) topicExtension
                        .getContent().getCreatedChild("right-number"); //$NON-NLS-1$
                element.setTextContent(((Integer) json
                        .getInt(LightenConstants.SHEET_ATTR_RIGHT_NUM))
                                .toString());
            }
        }

        if (json.has(LightenConstants.SHEET_ATTR_RELATION)) {
            JSONArray relationshipsJson = json
                    .getJSONArray(LightenConstants.SHEET_ATTR_RELATION);
            for (int i = 0; i < relationshipsJson.length(); i++) {
                JSONObject relationshipJson = relationshipsJson
                        .getJSONObject(i);
                IRelationship relationship = workbook.createRelationship();
                relationship.setEnd1Id(relationshipJson
                        .getString(LightenConstants.RELATION_START_ID));
                relationship.setEnd2Id(relationshipJson
                        .getString(LightenConstants.RELATION_END_ID));
                if (relationshipJson
                        .has(LightenConstants.RELATION_CTRL_POINT1)) {
                    IControlPoint controlPoint1 = relationship
                            .getControlPoint(0);
                    controlPoint1.setPosition(deserializePoint(relationshipJson
                            .getString(LightenConstants.RELATION_CTRL_POINT1)));
                }
                if (relationshipJson
                        .has(LightenConstants.RELATION_CTRL_POINT2)) {
                    IControlPoint controlPoint2 = relationship
                            .getControlPoint(1);
                    controlPoint2.setPosition(deserializePoint(relationshipJson
                            .getString(LightenConstants.RELATION_CTRL_POINT2)));
                }

                sheet.addRelationship(relationship);
            }
        }

        if (json.has(LightenConstants.SHEET_ATTR_CLR_THEME)) {
            String themeName = parseTheme(
                    json.getJSONObject(LightenConstants.SHEET_ATTR_CLR_THEME));
            importStyleAndTheme(themeName);
        }

        return targetSheet;
    }

    private ITopic parseTopic(JSONObject json, ISheet sheet) {
        IWorkbook workbook = sheet.getOwnedWorkbook();
        TopicImpl topic = (TopicImpl) workbook.createTopic();

        if (json.has(LightenConstants.TOPIC_ATTR_TITLE))
            topic.setTitleText(
                    json.getString(LightenConstants.TOPIC_ATTR_TITLE));
        if (json.has(LightenConstants.TOPIC_ATTR_ID))
            topic.getImplementation().setAttribute(DOMConstants.ATTR_ID,
                    json.getString(LightenConstants.TOPIC_ATTR_ID));

        if (json.has(LightenConstants.TOPIC_ATTR_SUBTOPIC)) {
            JSONArray subtopicJson = json
                    .getJSONArray(LightenConstants.TOPIC_ATTR_SUBTOPIC);
            for (int i = 0; i < subtopicJson.length(); i++) {
                ITopic subtopic = parseTopic(subtopicJson.getJSONObject(i),
                        sheet);
                topic.add(subtopic);
            }
        }

        if (json.has(LightenConstants.TOPIC_ATTR_NOTE)) {
            JSONObject notesJson = json
                    .getJSONObject(LightenConstants.TOPIC_ATTR_NOTE);
            if (notesJson.has(LightenConstants.NOTE_ATTR_TEXT)) {
                INotes notes = topic.getNotes();
                PlainNotesContentImpl plainNotes = (PlainNotesContentImpl) workbook
                        .createNotesContent(INotes.PLAIN);
                try {
                    plainNotes.setTextContent(notesJson
                            .getString(LightenConstants.NOTE_ATTR_TEXT));
                } catch (Exception e) {
                }
                notes.setContent(INotes.PLAIN, plainNotes);
            }
        }

        if (json.has(LightenConstants.TOPIC_ATTR_DETACH)
                && json.getBoolean(LightenConstants.TOPIC_ATTR_DETACH)
                && json.has(LightenConstants.TOPIC_ATTR_POS)) {
            topic.setPosition(deserializePoint(
                    json.getString(LightenConstants.TOPIC_ATTR_POS)));
        }

        if (json.has(LightenConstants.TOPIC_ATTR_FOLDED))
            topic.setFolded(
                    json.getBoolean(LightenConstants.TOPIC_ATTR_FOLDED));

        if (json.has(LightenConstants.TOPIC_ATTR_STYLE))
            setTopicStyle(topic,
                    json.getJSONObject(LightenConstants.TOPIC_ATTR_STYLE));

        return topic;
    }

    private Point deserializePoint(String point) {
        int commaPos = point.indexOf(',');
        int x = Double.valueOf(point.substring(1, commaPos)).intValue();
        int y = Double
                .valueOf(point.substring(commaPos + 1, point.length() - 1))
                .intValue();
        return new Point(x, y);
    }

    private String parseTheme(JSONObject json) {
        String name = json.getString(LightenConstants.THEME_ATTR_NAME);

        if (json.has(LightenConstants.THEME_ATTR_MAIN))
            parseThemeStyle(IStyle.TOPIC, Styles.FAMILY_MAIN_TOPIC,
                    json.getJSONObject(LightenConstants.THEME_ATTR_MAIN));

        if (json.has(LightenConstants.THEME_ATTR_SUB))
            parseThemeStyle(IStyle.TOPIC, Styles.FAMILY_SUB_TOPIC,
                    json.getJSONObject(LightenConstants.THEME_ATTR_SUB));

        if (json.has(LightenConstants.THEME_ATTR_MAP))
            parseThemeStyle(IStyle.MAP, Styles.FAMILY_MAP,
                    json.getJSONObject(LightenConstants.THEME_ATTR_MAP));

        if (json.has(LightenConstants.THEME_ATTR_CENTRAL))
            parseThemeStyle(IStyle.TOPIC, Styles.FAMILY_CENTRAL_TOPIC,
                    json.getJSONObject(LightenConstants.THEME_ATTR_CENTRAL));

        if (json.has(LightenConstants.THEME_ATTR_FLOAT))
            parseThemeStyle(IStyle.TOPIC, Styles.FAMILY_FLOATING_TOPIC,
                    json.getJSONObject(LightenConstants.THEME_ATTR_FLOAT));

        if (json.has(LightenConstants.THEME_ATTR_RELATION))
            parseThemeStyle(IStyle.RELATIONSHIP, Styles.FAMILY_RELATIONSHIP,
                    json.getJSONObject(LightenConstants.THEME_ATTR_RELATION));

        return name;
    }

    private void parseThemeStyle(String type, String styleFamily,
            JSONObject json) {
        if (json.has(LightenConstants.STYLE_ATTR_LINE_CLR))
            registerTheme(type, styleFamily, DOMConstants.ATTR_LINE_COLOR,
                    colorToRgb(json
                            .getString(LightenConstants.STYLE_ATTR_LINE_CLR)));
        if (json.has(LightenConstants.STYLE_ATTR_FILL_CLR))
            registerTheme(type, styleFamily, DOMConstants.ATTR_FILL, colorToRgb(
                    json.getString(LightenConstants.STYLE_ATTR_FILL_CLR)));
        if (json.has(LightenConstants.STYLE_ATTR_FONT_FAMILY))
            registerTheme(type, styleFamily, DOMConstants.ATTR_FONT_FAMILY,
                    json.getString(LightenConstants.STYLE_ATTR_FONT_FAMILY));
        if (json.has(LightenConstants.STYLE_ATTR_FONT_CLR))
            registerTheme(type, styleFamily, DOMConstants.ATTR_COLOR,
                    colorToRgb(json
                            .getString(LightenConstants.STYLE_ATTR_FONT_CLR)));
        if (json.has(LightenConstants.STYLE_ATTR_CORNER_RADIUS))
            registerTheme(type, styleFamily, DOMConstants.ATTR_LINE_CORNER,
                    json.getInt(LightenConstants.STYLE_ATTR_CORNER_RADIUS)
                            + PIXEL_POINT);
        if (json.has(LightenConstants.STYLE_ATTR_LINE_WIDTH))
            registerTheme(type, styleFamily, DOMConstants.ATTR_LINE_WIDTH,
                    json.getInt(LightenConstants.STYLE_ATTR_LINE_WIDTH)
                            + PIXEL_POINT);

    }

    private void registerStyle(IStyled styleOwner, String key, String value) {
        if (value == null)
            return;

        IStyle style = styleMap.get(styleOwner);
        if (style == null) {
            style = targetStyleSheet.createStyle(styleOwner.getStyleType());
            targetStyleSheet.addStyle(style, IStyleSheet.NORMAL_STYLES);
            styleMap.put(styleOwner, style);
        }
        style.setProperty(key, value);
    }

    private void registerTheme(String type, String styleFamily, String styleKey,
            String styleValue) {
        if (styleFamily == null || styleKey == null || styleValue == null)
            return;

        if (theme == null) {
            theme = targetStyleSheet.createStyle(IStyle.THEME);
            targetStyleSheet.addStyle(theme, IStyleSheet.MASTER_STYLES);
        }

        IStyle defaultStyle = theme.getDefaultStyle(styleFamily);
        if (defaultStyle == null) {
            defaultStyle = targetStyleSheet.createStyle(type);
            targetStyleSheet.addStyle(defaultStyle,
                    IStyleSheet.AUTOMATIC_STYLES);
            theme.setDefaultStyleId(styleFamily, defaultStyle.getId());
        }
        defaultStyle.setProperty(styleKey, styleValue);
    }

    private void importStyleAndTheme(String themeName) {
        IStyleSheet targetStyleSheet = getTargetWorkbook().getStyleSheet();
        for (Entry<IStyled, IStyle> entry : styleMap.entrySet()) {
            IStyled styled = entry.getKey();
            IStyle style = entry.getValue();
            IStyle imported = targetStyleSheet.importStyle(style);
            if (imported != null) {
                styled.setStyleId(imported.getId());
            }
        }

        if (theme != null) {
            theme.setName(themeName);
            IStyle imported = targetStyleSheet.importStyle(theme);
            if (imported != null) {
                targetSheet.setThemeId(imported.getId());
            }
        }

    }

    private String colorToRgb(String colorPoint) {
        if ("0.00".equals(colorPoint.substring(colorPoint.lastIndexOf(',') + 1, //$NON-NLS-1$
                colorPoint.length() - 1).trim())) {//ignore when opacity is 0.00
            return null;
        }

        StringBuilder sb = new StringBuilder().append('#');
        char[] chars = colorPoint.toCharArray();
        StringBuilder splitBuilder = new StringBuilder();
        for (char c : chars) {
            if (c != '{' && c != ' ') {
                if (c == ',') {
                    String value = Integer.toHexString(
                            Integer.valueOf(splitBuilder.toString()));
                    if (value.length() == 1) {
                        sb.append('0');
                    }
                    sb.append(value);
                    splitBuilder = new StringBuilder();
                } else {
                    splitBuilder.append(c);
                }
            }
        }
        return sb.toString();
    }

    private void setTopicStyle(ITopic topic, JSONObject json) {
        if (json.has(LightenConstants.STYLE_ATTR_LINE_CLR)) {
            registerStyle(topic, DOMConstants.ATTR_LINE_COLOR, colorToRgb(
                    json.getString(LightenConstants.STYLE_ATTR_LINE_CLR)));
        }
        if (json.has(LightenConstants.STYLE_ATTR_FONT_CLR)) {
            registerStyle(topic, DOMConstants.ATTR_COLOR, colorToRgb(
                    json.getString(LightenConstants.STYLE_ATTR_FONT_CLR)));
        }
        if (json.has(LightenConstants.STYLE_ATTR_FONT_WEIGHT)) {
            registerStyle(topic, DOMConstants.ATTR_FONT_WEIGHT,
                    json.getString(LightenConstants.STYLE_ATTR_FONT_WEIGHT));
        }
    }

    public void error(SAXParseException arg0) throws SAXException {
        log(arg0, null);
    }

    public void fatalError(SAXParseException arg0) throws SAXException {
        log(arg0, null);
    }

    public void warning(SAXParseException arg0) throws SAXException {
        log(arg0, null);
    }

}
