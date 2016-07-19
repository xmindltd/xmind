package org.xmind.core.internal.dom;

import static org.xmind.core.internal.zip.ArchiveConstants.PATH_REVISIONS;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xmind.core.Core;
import org.xmind.core.IFileEntry;
import org.xmind.core.IFileEntryFilter;
import org.xmind.core.IManifest;
import org.xmind.core.IRevisionManager;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.RevisionRepository;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.util.DOMUtils;

public class RevisionRepositoryImpl extends RevisionRepository {

    private static final Pattern REVISIONS_RESOURCE_DIR = Pattern
            .compile("Revisions/([^/]+)/"); //$NON-NLS-1$

    private WorkbookImpl ownedWorkbook;

    private Map<String, RevisionManagerImpl> managers = new HashMap<String, RevisionManagerImpl>();

    public RevisionRepositoryImpl(WorkbookImpl ownedWorkbook) {
        this.ownedWorkbook = ownedWorkbook;
    }

    public IRevisionManager getRevisionManager(String resourceId,
            String contentType) {
        if (resourceId == null)
            throw new IllegalArgumentException("Resource id is null"); //$NON-NLS-1$
        return doGetRevisionManager(resourceId, contentType, true);
    }

    public IRevisionManager getRegisteredRevisionManager(String resourceId) {
        if (resourceId == null)
            throw new IllegalArgumentException("Resource id is null"); //$NON-NLS-1$
        return doGetRevisionManager(resourceId, null, false);
    }

    private RevisionManagerImpl doGetRevisionManager(String resourceId,
            String contentType, boolean createIfNotFound) {
        RevisionManagerImpl manager = managers.get(resourceId);
        if (manager == null) {
            manager = loadRevisionManager(resourceId, contentType,
                    createIfNotFound);
            if (manager != null) {
                manager.addNotify(ownedWorkbook);
                managers.put(resourceId, manager);
            }
        }
        return manager;
    }

    private RevisionManagerImpl loadRevisionManager(String resourceId,
            String contentType, boolean createIfNotFound) {
        String dirPath = PATH_REVISIONS + resourceId + "/"; //$NON-NLS-1$
        String metaPath = dirPath + ArchiveConstants.REVISIONS_XML;
        IManifest manifest = ownedWorkbook.getManifest();
        IFileEntry metaEntry = manifest.getFileEntry(metaPath);
        if (metaEntry != null) {
            RevisionManagerImpl manager = loadRevisionManager(resourceId,
                    metaEntry, dirPath);
            if (manager != null)
                return manager;
        }
        if (!createIfNotFound)
            return null;

        Document doc = DOMUtils.createDocument();
        Element ele = DOMUtils.createElement(doc, DOMConstants.TAG_REVISIONS);
        ele.setAttribute(DOMConstants.ATTR_RESOURCE_ID, resourceId);

        ele.setAttribute(DOMConstants.ATTR_MEDIA_TYPE, contentType);
        ele.setAttribute(DOMConstants.ATTR_NEXT_REVISION_NUMBER, "1"); //$NON-NLS-1$
        manifest.createFileEntry(metaPath);
        return new RevisionManagerImpl(doc, ownedWorkbook, dirPath);
    }

    private RevisionManagerImpl loadRevisionManager(String resourceId,
            IFileEntry metaEntry, String path) {
        InputStream stream;
        try {
            stream = metaEntry.openInputStream();
        } catch (IOException e) {
            Core.getLogger().log(e,
                    "Failed to load document at " + metaEntry.getPath()); //$NON-NLS-1$
            return null;
        }
        if (stream == null)
            return null;

        try {
            Document doc = DOMUtils.loadDocument(stream);
            return new RevisionManagerImpl(doc, ownedWorkbook, path);
        } catch (Throwable e) {
            Core.getLogger().log(e,
                    "Failed to load document at " + metaEntry.getPath()); //$NON-NLS-1$
        }
        return null;
    }

    public void setRevisionManager(String resourceId,
            IRevisionManager manager) {
        if (resourceId == null)
            throw new IllegalArgumentException("Resource id is null"); //$NON-NLS-1$
        if (manager != null && !(manager instanceof RevisionManagerImpl))
            throw new IllegalArgumentException(
                    "Invalid type of revision manager"); //$NON-NLS-1$
        if (manager != null && manager.getOwnedWorkbook() != getOwnedWorkbook())
            throw new IllegalArgumentException(
                    "Revision manager owned by another workbook"); //$NON-NLS-1$
        RevisionManagerImpl newManager = (RevisionManagerImpl) manager;
        RevisionManagerImpl oldManager = doGetRevisionManager(resourceId, null,
                false);
        if (oldManager == newManager)
            return;

        if (oldManager != null) {
            oldManager.removeNotify(ownedWorkbook);
            managers.remove(oldManager);
        }
        if (newManager != null) {
            managers.put(resourceId, newManager);
            newManager.addNotify(ownedWorkbook);
        }
    }

    public Set<String> getRegisteredResourceIds() {
        Set<String> resourceIds = new HashSet<String>();
        Iterator<IFileEntry> entryIter = ownedWorkbook.getManifest()
                .iterFileEntries(new IFileEntryFilter() {
                    public boolean select(String path, String mediaType,
                            boolean isDirectory) {
                        return REVISIONS_RESOURCE_DIR.matcher(path).matches();
                    }
                });
        while (entryIter.hasNext()) {
            IFileEntry entry = entryIter.next();
            String path = entry.getPath();
            Matcher m = REVISIONS_RESOURCE_DIR.matcher(path);
            if (m.matches()) {
                String resourceId = m.group(1);
                resourceIds.add(resourceId);
            }
        }
        return resourceIds;
    }

    public IWorkbook getOwnedWorkbook() {
        return ownedWorkbook;
    }

    public boolean isOrphan() {
        return false;
    }

}
