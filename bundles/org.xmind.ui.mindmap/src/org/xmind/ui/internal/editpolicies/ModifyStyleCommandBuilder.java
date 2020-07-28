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
package org.xmind.ui.internal.editpolicies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.xmind.core.IFileEntry;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookComponent;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.core.util.Property;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandBuilder;
import org.xmind.ui.commands.ModifyStyleCommand;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.Styles;

public class ModifyStyleCommandBuilder extends CommandBuilder {

    private static final String NULL = "$NULL$"; //$NON-NLS-1$

    private Request request;

    private IStyle sourceStyle;

    private Map<String, String> idCache = null;

    private Map<String, Properties> propertiesCache = new HashMap<String, Properties>();

    private List<IStyled> sources = new ArrayList<IStyled>();

    public ModifyStyleCommandBuilder(Request request) {
        this(request.getTargetViewer(),
                request.getTargetDomain().getCommandStack(), request);
    }

    public ModifyStyleCommandBuilder(IViewer viewer, CommandBuilder delegate,
            Request request) {
        super(viewer, delegate);
        init(request);
    }

    public ModifyStyleCommandBuilder(IViewer viewer, ICommandStack commandStack,
            Request request) {
        super(viewer, commandStack);
        init(request);
    }

    private void init(Request request) {
        this.request = request;
        Object resource = request.getParameter(MindMapUI.PARAM_RESOURCE);
        this.sourceStyle = (resource instanceof IStyle) ? (IStyle) resource
                : null;
    }

    public List<IStyled> getModifiedSources() {
        return sources;
    }

    public boolean isSourceModified(IStyled source) {
        return sources.contains(source);
    }

    public void modify(IStyled source) {
        if (isSourceModified(source))
            return;

        String sourceType = getStyleType(source);
        if (sourceType == null)
            return;

        String sourceStyleId = source.getStyleId();
        String oldStyleId = sourceStyleId;
        if (oldStyleId == null)
            oldStyleId = NULL + sourceType;
        String newStyleId = getCachedId(oldStyleId);
        if (newStyleId == null) {
            newStyleId = getNewStyleId(source, sourceStyleId, sourceType);
            if (newStyleId == null)
                newStyleId = NULL + sourceType;
            cacheId(oldStyleId, newStyleId);
        }

        if (oldStyleId.equals(newStyleId))
            return;

        modifyStyle(source, newStyleId);
        sources.add(source);
    }

    public void removeStyle(IStyled source) {
        if (isSourceModified(source))
            return;

        modifyStyle(source, null);
        sources.add(source);
    }

    private void modifyStyle(IStyled source, String newStyleId) {
        if (newStyleId != null && newStyleId.startsWith(NULL))
            newStyleId = null;
        add(new ModifyStyleCommand(source, newStyleId), true);
    }

    private void cacheId(String oldId, String newId) {
        if (idCache == null)
            idCache = new HashMap<String, String>();
        idCache.put(oldId, newId);
    }

    private String getCachedId(String oldId) {
        if (idCache != null)
            return idCache.get(oldId);
        return null;
    }

    private String getNewStyleId(IStyled source, String sourceStyleId,
            String sourceType) {
        IStyleSheet styleSheet = getStyleSheet(source);
        if (styleSheet == null)
            return null;

        IStyle oldStyle = sourceStyleId == null ? null
                : styleSheet.findStyle(sourceStyleId);
        Properties oldProperties = getProperties(oldStyle);
        if (oldProperties == null) {
            oldProperties = new Properties();
        } else {
            Properties p = new Properties();
            p.putAll(oldProperties);
            oldProperties = p;
        }

        putValues(source, oldProperties, sourceType);

        if (oldProperties.isEmpty())
            return null;

        IStyle similarStyle = findSimilarStyle(oldProperties, styleSheet);
        if (similarStyle != null)
            return similarStyle.getId();

        IStyle newStyle = createStyle(styleSheet, source, oldProperties,
                sourceType);
        if (newStyle != null)
            return newStyle.getId();

        return null;
    }

    private void putValues(IStyled source, Properties oldProperties,
            String sourceType) {
        if (sourceStyle != null && sourceStyle.getType().equals(sourceType)) {
            Iterator<Property> sourceProperties = sourceStyle.properties();
            while (sourceProperties.hasNext()) {
                Property prop = sourceProperties.next();
                if (prop.value == null) {
                    oldProperties.remove(prop.key);
                } else {
                    oldProperties.put(prop.key, prop.value);
                }
            }
        }
        for (String name : request.getParameterNames()) {
            if (name.startsWith(MindMapUI.PARAM_STYLE_PREFIX)) {
                String key = name
                        .substring(MindMapUI.PARAM_STYLE_PREFIX.length());
                String value = (String) request.getParameter(name);
                value = evaluate(source, key, value);
                if (value == null) {
                    oldProperties.remove(key);
                } else {
                    oldProperties.put(key, value);
                }
            }
        }
    }

    private IStyle findSimilarStyle(Properties sourceProperties,
            IStyleSheet styleSheet) {
        Set<IStyle> styles = styleSheet.getStyles(IStyleSheet.NORMAL_STYLES);
        for (IStyle style : styles) {
            Properties contents = getProperties(style);
            if (contents.equals(sourceProperties))
                return style;
        }
        return null;
    }

    private IStyle createStyle(IStyleSheet styleSheet, IStyled source,
            Properties properties, String sourceType) {
        if (sourceType == null)
            return null;

        IStyle newStyle = styleSheet.createStyle(sourceType);
        for (Entry<Object, Object> en : properties.entrySet()) {
            newStyle.setProperty((String) en.getKey(), (String) en.getValue());
        }
        styleSheet.addStyle(newStyle, IStyleSheet.NORMAL_STYLES);
        return newStyle;
    }

    private String getStyleType(IStyled source) {
        return source == null ? null : source.getStyleType();
//        if (source instanceof ITopic)
//            return IStyle.TOPIC;
//        if (source instanceof IBoundary)
//            return IStyle.BOUNDARY;
//        if (source instanceof IRelationship)
//            return IStyle.RELATIONSHIP;
//        if (source instanceof ISheet)
//            return IStyle.MAP;
//        if (source instanceof ISummary)
//            return IStyle.SUMMARY;
//        return null;
    }

    private String evaluate(IStyled source, String key, String value) {
        if (value != null) {
            if (Styles.Background.equals(key)) {
                String attachmentURL = getCachedId(value);
                if (attachmentURL == null) {
                    attachmentURL = createAttachmentURL(source, value);
                    if (attachmentURL != null) {
                        cacheId(value, attachmentURL);
                    }
                }
                if (attachmentURL != null)
                    value = attachmentURL;
            }
        }
        return value;
    }

    private String createAttachmentURL(IStyled source, String value) {
        File file = new File(value);
        if (!file.exists() || !file.canRead())
            return null;

        IWorkbook workbook = getWorkbook(source);
        if (workbook == null)
            return null;

        IFileEntry entry;
        try {
            entry = workbook.getManifest().createAttachmentFromFilePath(value);
        } catch (IOException e) {
            return null;
        }
        if (entry == null)
            return null;
        return HyperlinkUtils.toAttachmentURL(entry.getPath());
    }

    private Properties getProperties(IStyle style) {
        if (style == null)
            return null;

        String id = style.getId();
        if (propertiesCache == null)
            propertiesCache = new HashMap<String, Properties>();
        Properties properties = propertiesCache.get(id);
        if (properties != null)
            return properties;

        properties = new Properties();
        propertiesCache.put(id, properties);
        Iterator<Property> it = style.properties();
        while (it.hasNext()) {
            Property p = it.next();
            properties.put(p.key, p.value);
        }
        return properties;
    }

    private IWorkbook getWorkbook(IStyled styled) {
        if (styled instanceof IWorkbookComponent) {
            return ((IWorkbookComponent) styled).getOwnedWorkbook();
        }
        return null;
    }

    private IStyleSheet getStyleSheet(IStyled styled) {
        if (styled instanceof IWorkbookComponent)
            return ((IWorkbookComponent) styled).getOwnedWorkbook()
                    .getStyleSheet();
        return null;
    }

}
