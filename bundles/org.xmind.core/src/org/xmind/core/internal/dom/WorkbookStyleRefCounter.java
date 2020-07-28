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
package org.xmind.core.internal.dom;

import static org.xmind.core.internal.zip.ArchiveConstants.STYLES_XML;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xmind.core.IFileEntry;
import org.xmind.core.internal.AbstractRefCounter;
import org.xmind.core.style.IStyle;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.core.util.IStyleRefCounter;
import org.xmind.core.util.Property;

public class WorkbookStyleRefCounter extends AbstractRefCounter implements
        IStyleRefCounter {

    private StyleSheetImpl sheet;

    private ManifestImpl manifest;

    private Map<String, String> groupCache = new HashMap<String, String>();

    WorkbookStyleRefCounter(StyleSheetImpl sheet, ManifestImpl manifest) {
        this.sheet = sheet;
        this.manifest = manifest;
    }

    protected Object findResource(String resourceId) {
        return sheet.findStyle(resourceId);
    }

    protected void postIncreaseRef(String resourceId, Object resource) {
        IStyle style = (IStyle) resource;
        if (sheet.equals(style.getOwnedStyleSheet())) {
            String group = sheet.findOwnedGroup(style);
            if (group != null) {
                groupCache.put(resourceId, group);
            } else {
                String cachedGroup = groupCache.get(resourceId);
                if (cachedGroup != null) {
                    addStyleToGroup(style, cachedGroup);
                }
            }

            Iterator<Property> defaultStyles = style.defaultStyles();
            while (defaultStyles.hasNext()) {
                Property ds = defaultStyles.next();
                increaseRef(ds.value);
            }

            IFileEntry entry = manifest.getFileEntry(STYLES_XML);
            if (entry == null) {
                entry = manifest.createFileEntry(STYLES_XML);
            }
            entry.increaseReference();

            Iterator<Property> it = style.properties();
            while (it.hasNext()) {
                Property p = it.next();
                if (HyperlinkUtils.isAttachmentURL(p.value)) {
                    String entryPath = HyperlinkUtils.toAttachmentPath(p.value);
                    entry = manifest.getFileEntry(entryPath);
                    if (entry != null) {
                        entry.increaseReference();
                    }
                }
            }

        }
    }

    private void addStyleToGroup(IStyle style, String groupName) {
        sheet.addStyle(style, groupName);
    }

    protected void postDecreaseRef(String resourceId, Object resource) {
        IStyle style = (IStyle) resource;
        if (sheet.equals(style.getOwnedStyleSheet())) {
            String group = sheet.findOwnedGroup(style);
            if (group != null) {
                if (getRefCount(resourceId) <= 0) {
                    sheet.removeStyle(style);
                }
            }

            Iterator<Property> defaultStyles = style.defaultStyles();
            while (defaultStyles.hasNext()) {
                Property ds = defaultStyles.next();
                decreaseRef(ds.value);
            }

            if (sheet.isEmpty()) {
                IFileEntry entry = manifest.getFileEntry(STYLES_XML);
                if (entry != null) {
                    entry.decreaseReference();
                }
            }

            Iterator<Property> it = style.properties();
            while (it.hasNext()) {
                Property p = it.next();
                if (HyperlinkUtils.isAttachmentURL(p.value)) {
                    String entryPath = HyperlinkUtils.toAttachmentPath(p.value);
                    IFileEntry entry = manifest.getFileEntry(entryPath);
                    if (entry != null) {
                        entry.decreaseReference();
                    }
                }
            }
        }
    }

}