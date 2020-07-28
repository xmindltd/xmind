/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.core.util;

import static org.xmind.core.internal.dom.DOMConstants.ATTR_OBJECT_ID;
import static org.xmind.core.internal.dom.DOMConstants.ATTR_RESOURCE_PATH;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.xmind.core.CoreException;
import org.xmind.core.IBoundary;
import org.xmind.core.ICloneData;
import org.xmind.core.IComment;
import org.xmind.core.ICommentManager;
import org.xmind.core.IControlPoint;
import org.xmind.core.IFileEntry;
import org.xmind.core.IHtmlNotesContent;
import org.xmind.core.IHyperlinkSpan;
import org.xmind.core.IImage;
import org.xmind.core.IImageSpan;
import org.xmind.core.IManifest;
import org.xmind.core.IMeta;
import org.xmind.core.INotes;
import org.xmind.core.INotesContent;
import org.xmind.core.IParagraph;
import org.xmind.core.IPlainNotesContent;
import org.xmind.core.IRelationship;
import org.xmind.core.IResourceRef;
import org.xmind.core.ISettingEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISpan;
import org.xmind.core.ISpanList;
import org.xmind.core.ISummary;
import org.xmind.core.ITextSpan;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.IWorkbook;
import org.xmind.core.IWorkbookExtension;
import org.xmind.core.IWorkbookExtensionElement;
import org.xmind.core.IWorkbookExtensionManager;
import org.xmind.core.internal.CloneData;
import org.xmind.core.internal.zip.ArchiveConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.core.marker.IMarkerResource;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyleSheet;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class CloneHandler {

    private ICloneData mapper;

    private IWorkbook sourceWorkbook;
    private IWorkbook targetWorkbook;

    private IManifest sourceManifest;
    private IManifest targetManifest;

    private IStyleSheet sourceStyleSheet;
    private IStyleSheet targetStyleSheet;

    private IMarkerSheet sourceMarkerSheet;
    private IMarkerSheet targetMarkerSheet;

    /**
     * 
     */
    public CloneHandler() {
        this(null);
    }

    /**
     * 
     */
    public CloneHandler(ICloneData mapper) {
        this.mapper = mapper == null
                ? new CloneData(Collections.emptyList(), null) : mapper;
        this.sourceWorkbook = null;
        this.targetWorkbook = null;
        this.sourceManifest = null;
        this.targetManifest = null;
        this.sourceStyleSheet = null;
        this.targetStyleSheet = null;
        this.sourceMarkerSheet = null;
        this.targetMarkerSheet = null;
    }

    public CloneHandler withWorkbooks(IWorkbook sourceWorkbook,
            IWorkbook targetWorkbook) {
        if (sourceWorkbook == null)
            throw new IllegalArgumentException();
        if (targetWorkbook == null)
            throw new IllegalArgumentException();

        this.sourceWorkbook = sourceWorkbook;
        this.targetWorkbook = targetWorkbook;
        this.sourceManifest = sourceWorkbook.getManifest();
        this.targetManifest = targetWorkbook.getManifest();
        this.sourceStyleSheet = sourceWorkbook.getStyleSheet();
        this.targetStyleSheet = targetWorkbook.getStyleSheet();
        this.sourceMarkerSheet = sourceWorkbook.getMarkerSheet();
        this.targetMarkerSheet = targetWorkbook.getMarkerSheet();

        return this;
    }

    public CloneHandler withManifests(IManifest sourceManifest,
            IManifest targetManifest) {
        if (sourceManifest == null)
            throw new IllegalArgumentException();
        if (targetManifest == null)
            throw new IllegalArgumentException();

        this.sourceManifest = sourceManifest;
        this.targetManifest = targetManifest;
        return this;
    }

    public CloneHandler withStyleSheets(IStyleSheet sourceStyleSheet,
            IStyleSheet targetStyleSheet) {
        if (sourceStyleSheet == null)
            throw new IllegalArgumentException();
        if (targetStyleSheet == null)
            throw new IllegalArgumentException();

        this.sourceStyleSheet = sourceStyleSheet;
        this.targetStyleSheet = targetStyleSheet;
        this.sourceManifest = sourceStyleSheet.getAdapter(IManifest.class);
        this.targetManifest = targetStyleSheet.getAdapter(IManifest.class);
        return this;
    }

    public CloneHandler withMarkerSheets(IMarkerSheet sourceMarkerSheet,
            IMarkerSheet targetMarkerSheet) {
        if (sourceMarkerSheet == null)
            throw new IllegalArgumentException();
        if (targetMarkerSheet == null)
            throw new IllegalArgumentException();

        this.sourceMarkerSheet = sourceMarkerSheet;
        this.targetMarkerSheet = targetMarkerSheet;
        this.sourceManifest = sourceMarkerSheet.getAdapter(IManifest.class);
        this.targetManifest = targetMarkerSheet.getAdapter(IManifest.class);
        return this;
    }

    /**
     * @return the mapper
     */
    public ICloneData getMapper() {
        return mapper;
    }

    public void copyWorkbookContents() throws IOException {
        if (sourceWorkbook == null)
            throw new AssertionError("sourceWorkbook is null"); //$NON-NLS-1$
        if (targetWorkbook == null)
            throw new AssertionError("targetWorkbook is null"); //$NON-NLS-1$

        List<ISheet> oldSheets = new ArrayList<ISheet>(
                targetWorkbook.getSheets());
        for (ISheet sourceSheet : sourceWorkbook.getSheets()) {
            targetWorkbook.addSheet(cloneSheet(sourceSheet));
        }
        if (targetWorkbook.getSheets().isEmpty()) {
            targetWorkbook.addSheet(targetWorkbook.createSheet());
        }
        for (ISheet oldSheet : oldSheets) {
            targetWorkbook.removeSheet(oldSheet);
        }

        IMeta sourceMeta = sourceWorkbook.getMeta();
        IMeta targetMeta = targetWorkbook.getMeta();
        for (String keyPath : sourceMeta.getKeyPaths()) {
            targetMeta.setValue(keyPath, sourceMeta.getValue(keyPath));
        }

        List<ISheet> sheets = targetWorkbook.getSheets();
        ArrayList<ITopic> targetAllTopics = new ArrayList<ITopic>();
        for (ISheet sheet : sheets) {
            getAllChildrenTopic(targetAllTopics, sheet.getRootTopic());
        }

        IWorkbookExtensionManager sourceExtensionManager = sourceWorkbook
                .getAdapter(IWorkbookExtensionManager.class);
        IWorkbookExtensionManager targetExtensionManager = targetWorkbook
                .getAdapter(IWorkbookExtensionManager.class);
        if (sourceExtensionManager != null && targetExtensionManager != null) {
            for (IWorkbookExtension sourceExtension : sourceExtensionManager
                    .getExtensions()) {
                IWorkbookExtension targetExtension = targetExtensionManager
                        .createExtension(sourceExtension.getProviderName());
                copyWorkbookExtension(sourceExtension, targetExtension);
            }
        }

        fixInternalHyperlinkFor(targetAllTopics);
    }

    private List<ITopic> getAllChildrenTopic(List<ITopic> topics, ITopic root) {
        topics.add(root);
        for (ITopic topic : root.getAllChildren()) {
            getAllChildrenTopic(topics, topic);
        }
        return topics;
    }

    /**
     * Clones a source object into the target container (e.g. workbook or style
     * sheet, etc.). Currently only these kinds of objects can be cloned:
     * <ul>
     * <li>{@link org.xmind.core.ISheet} (require sourceWorkbook/targetWorkbook)
     * </li>
     * <li>{@link org.xmind.core.ITopic} (require sourceWorkbook/targetWorkobok)
     * </li>
     * <li>{@link org.xmind.core.IBoundary} (require
     * sourceWorkbook/targetWorkbook)</li>
     * <li>{@link org.xmind.core.ISummary} (require
     * sourceWorkbook/targetWorkbook)</li>
     * <li>{@link org.xmind.core.IRelationship} (require
     * sourceWorkbook/targetWorkbook)</li>
     * <li>{@link org.xmind.core.IImage} (require sourceWorkbook/targetWorkbook)
     * </li>
     * <li>{@link org.xmind.core.style.IStyle} (require
     * sourceStyleSheet/targetStyleSheet)</li>
     * </ul>
     * 
     * @param source
     *            the source object to clone
     * @return a cloned object, or <code>null</code> if the object is
     *         unrecognized
     * @throws IOException
     *             if any I/O error occurred during the clone process (e.g. when
     *             importing attachments along with its referrence topic)
     * @throws AssertionError
     *             if required target contexts are missing
     */
    public Object cloneObject(Object source) throws IOException {
        Object target = mapper.get(source);
        if (target != null)
            return target;

        if (source instanceof ISheet) {
            if (sourceWorkbook == null)
                throw new AssertionError("sourceWorkbook is null"); //$NON-NLS-1$
            if (targetWorkbook == null)
                throw new AssertionError("targetWorkbook is null"); //$NON-NLS-1$
            if (sourceManifest == null)
                throw new AssertionError("sourceManifest is null"); //$NON-NLS-1$
            if (targetManifest == null)
                throw new AssertionError("targetManifest is null"); //$NON-NLS-1$
            return cloneSheet((ISheet) source);
        } else if (source instanceof ITopic) {
            if (sourceWorkbook == null)
                throw new AssertionError("sourceWorkbook is null"); //$NON-NLS-1$
            if (targetWorkbook == null)
                throw new AssertionError("targetWorkbook is null"); //$NON-NLS-1$
            if (sourceManifest == null)
                throw new AssertionError("sourceManifest is null"); //$NON-NLS-1$
            if (targetManifest == null)
                throw new AssertionError("targetManifest is null"); //$NON-NLS-1$
            ITopic sourceTopic = (ITopic) source;
            ITopic targetTopic = targetWorkbook.createTopic();
            copyTopicContents(sourceTopic, targetTopic);
            return targetTopic;
        } else if (source instanceof IBoundary) {
            if (sourceWorkbook == null)
                throw new AssertionError("sourceWorkbook is null"); //$NON-NLS-1$
            if (targetWorkbook == null)
                throw new AssertionError("targetWorkbook is null"); //$NON-NLS-1$
            if (sourceManifest == null)
                throw new AssertionError("sourceManifest is null"); //$NON-NLS-1$
            if (targetManifest == null)
                throw new AssertionError("targetManifest is null"); //$NON-NLS-1$
            IBoundary sourceBoundary = (IBoundary) source;
            IBoundary targetBoundary = targetWorkbook.createBoundary();
            copyBoundaryContents(sourceBoundary, targetBoundary);
            return targetBoundary;
        } else if (source instanceof ISummary) {
            if (sourceWorkbook == null)
                throw new AssertionError("sourceWorkbook is null"); //$NON-NLS-1$
            if (targetWorkbook == null)
                throw new AssertionError("targetWorkbook is null"); //$NON-NLS-1$
            if (sourceManifest == null)
                throw new AssertionError("sourceManifest is null"); //$NON-NLS-1$
            if (targetManifest == null)
                throw new AssertionError("targetManifest is null"); //$NON-NLS-1$
            ISummary sourceSummary = (ISummary) source;
            ISummary targetSummary = targetWorkbook.createSummary();
            copySummaryContents(sourceSummary, targetSummary);
            return targetSummary;
        } else if (source instanceof IRelationship) {
            if (sourceWorkbook == null)
                throw new AssertionError("sourceWorkbook is null"); //$NON-NLS-1$
            if (targetWorkbook == null)
                throw new AssertionError("targetWorkbook is null"); //$NON-NLS-1$
            if (sourceManifest == null)
                throw new AssertionError("sourceManifest is null"); //$NON-NLS-1$
            if (targetManifest == null)
                throw new AssertionError("targetManifest is null"); //$NON-NLS-1$
            IRelationship sourceRel = (IRelationship) source;
            IRelationship targetRel = targetWorkbook.createRelationship();
            copyRelationshipContents(sourceRel, targetRel);
            return targetRel;
        } else if (source instanceof IStyle) {
            if (sourceStyleSheet == null)
                throw new AssertionError("sourceStyleSheet is null"); //$NON-NLS-1$
            if (targetStyleSheet == null)
                throw new AssertionError("targetStyleSheet is null"); //$NON-NLS-1$
            if (sourceManifest == null)
                throw new AssertionError("sourceManifest is null"); //$NON-NLS-1$
            if (targetManifest == null)
                throw new AssertionError("targetManifest is null"); //$NON-NLS-1$
            return findOrCloneStyle((IStyle) source);
        } else if (source instanceof IImage) {
            if (sourceWorkbook == null)
                throw new AssertionError("sourceWorkbook is null"); //$NON-NLS-1$
            if (targetWorkbook == null)
                throw new AssertionError("targetWorkbook is null"); //$NON-NLS-1$
            if (sourceManifest == null)
                throw new AssertionError("sourceManifest is null"); //$NON-NLS-1$
            if (targetManifest == null)
                throw new AssertionError("targetManifest is null"); //$NON-NLS-1$
            IImage sourceImage = (IImage) source;
            ITopic sourceTopic = sourceImage.getParent();
            ITopic targetTopic = targetWorkbook.createTopic();
            copyTopicContents(sourceTopic, targetTopic);
            IImage targetImage = targetTopic.getImage();
            mapper.put(sourceImage, targetImage);
            return targetImage;
        } else if (source instanceof IMarker) {
            if (sourceMarkerSheet == null)
                throw new AssertionError("sourceMarkerSheet is null"); //$NON-NLS-1$
            if (targetMarkerSheet == null)
                throw new AssertionError("targetMarkerSheet is null"); //$NON-NLS-1$
            /// marker sheets require not manifests, but resource providers, 
            /// so sourceManifest and targetManifest are not checked here
            IMarker marker = findOrCloneMarker((IMarker) source);
            mapper.put(source, marker);
            return marker;
        } else if (source instanceof IMarkerRef) {
            if (sourceWorkbook == null)
                throw new AssertionError("sourceWorkbook is null"); //$NON-NLS-1$
            if (targetWorkbook == null)
                throw new AssertionError("targetWorkbook is null"); //$NON-NLS-1$
            IMarkerRef sourceMarkerRef = (IMarkerRef) source;
            IMarkerRef targetMarkerRef = cloneMarkerRef(sourceMarkerRef);
            mapper.put(sourceMarkerRef, targetMarkerRef);
            return targetMarkerRef;
        }

        return null;
    }

    /**
     * @param sourceMarkerRef
     * @return
     * @throws IOException
     */
    private IMarkerRef cloneMarkerRef(IMarkerRef sourceMarkerRef)
            throws IOException {
        IMarkerRef targetMarkerRef;
        IMarker sourceMarker = sourceMarkerRef.getMarker();
        if (sourceMarker == null) {
            targetMarkerRef = sourceMarkerRef;
        } else {
            IMarker targetMarker = findOrCloneMarker(sourceMarker);
            if (targetMarker == null) {
                targetMarkerRef = null;
            } else {
                mapper.put(sourceMarker, targetMarker);
                mapper.putString(ICloneData.MARKERSHEET_COMPONENTS,
                        sourceMarker.getId(), targetMarker.getId());
                ITopic tempTopic = targetWorkbook.createTopic();
                tempTopic.addMarker(targetMarker.getId());
                targetMarkerRef = tempTopic.getMarkerRefs().iterator().next();
            }
        }
        return targetMarkerRef;
    }

    private IMarker findOrCloneMarker(IMarker sourceMarker) throws IOException {
        if (sourceMarker == null)
            return null;

        IMarker targetMarker = (IMarker) mapper.get(sourceMarker);
        if (targetMarker != null)
            return targetMarker;

        String markerId = sourceMarker.getId();
        targetMarker = targetMarkerSheet.findMarker(markerId);
        if (targetMarker != null) {
            mapper.put(sourceMarker, targetMarker);
            return targetMarker;
        }

        /// markers do not change across permanent marker sheets
        if (sourceMarker.getOwnedSheet().isPermanent())
            return sourceMarker;

        IMarkerGroup sourceGroup = sourceMarker.getParent();
        IMarkerGroup targetGroup = findOrCloneMarkerGroup(sourceGroup);

        String sourceResourcePath = sourceMarker.getResourcePath();
        String targetResourcePath = null;

        /// copy marker resource content
        IMarkerResource sourceResource = sourceMarker.getResource();
        if (sourceResource != null) {
            InputStream input = null;
            try {
                input = sourceResource.openInputStream();
                targetResourcePath = targetMarkerSheet
                        .allocateMarkerResource(input, sourceResourcePath);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } finally {
                if (input != null) {
                    input.close();
                }
            }
        }

        /// create target marker with the same marker id
        targetMarker = targetMarkerSheet.createMarkerById(markerId,
                targetResourcePath);

        targetMarker.setName(sourceMarker.getName());

        if (targetGroup != null)
            targetGroup.addMarker(targetMarker);

        mapper.put(sourceMarker, targetMarker);

        return targetMarker;
    }

    private IMarkerGroup findOrCloneMarkerGroup(
            IMarkerGroup sourceMarkerGroup) {
        if (sourceMarkerGroup == null)
            return null;

        IMarkerGroup targetMarkerGroup = (IMarkerGroup) mapper
                .get(sourceMarkerGroup);
        if (targetMarkerGroup != null)
            return targetMarkerGroup;

        String groupId = sourceMarkerGroup.getId();
        targetMarkerGroup = targetMarkerSheet.findMarkerGroup(groupId);
        if (targetMarkerGroup == null
                || targetMarkerGroup.getOwnedSheet() != targetMarkerSheet) {
            /// make sure the new group is owned by the target marker sheet
            targetMarkerGroup = targetMarkerSheet
                    .createMarkerGroupById(groupId);
            targetMarkerGroup.setSingleton(sourceMarkerGroup.isSingleton());
            targetMarkerGroup.setName(sourceMarkerGroup.getName());
        }
        if (targetMarkerGroup.getParent() == null) {
            targetMarkerSheet.addMarkerGroup(targetMarkerGroup);
        }
        return targetMarkerGroup;
    }

    private ISheet cloneSheet(ISheet sourceSheet) throws IOException {
        ISheet targetSheet = targetWorkbook.createSheet();
        copySheetContents(sourceSheet, targetSheet);

        ArrayList<ITopic> targetAllTopics = new ArrayList<ITopic>();
        getAllChildrenTopic(targetAllTopics, targetSheet.getRootTopic());

        fixInternalHyperlinkFor(targetAllTopics);
        replaceProcessorOldIds(targetAllTopics);
        return targetSheet;
    }

    private void replaceProcessorOldIds(ArrayList<ITopic> targetAllTopics) {
        for (ITopic topic : targetAllTopics) {
            replaceProcessorOldIds(topic);
        }
    }

    private void replaceProcessorOldIds(ITopic topic) {
        ITopicExtension ext = topic.getExtension("org.xmind.ui.taskInfo"); //$NON-NLS-1$
        if (ext != null) {
            ITopicExtensionElement content = ext.getContent();
            List<ITopicExtensionElement> parentElements = content
                    .getChildren("predecessors"); //$NON-NLS-1$

            for (ITopicExtensionElement parentElement : parentElements) {
                List<ITopicExtensionElement> elements = parentElement
                        .getChildren("predecessor"); //$NON-NLS-1$
                for (ITopicExtensionElement element : elements) {
                    String taskId = element.getAttribute("task-id"); //$NON-NLS-1$
                    String newTaskId = mapper
                            .getString(ICloneData.WORKBOOK_COMPONENTS, taskId);
                    if (newTaskId != null) {
                        element.setAttribute("task-id", newTaskId); //$NON-NLS-1$
                    }
                }
            }
        }
    }

    /**
     * @param sourceSheet
     * @param targetSheet
     * @throws IOException
     * @throws CoreException
     */
    private void copySheetContents(ISheet sourceSheet, ISheet targetSheet)
            throws IOException {
        mapper.put(sourceSheet, targetSheet);
        mapper.putString(ICloneData.WORKBOOK_COMPONENTS, sourceSheet.getId(),
                targetSheet.getId());

        targetSheet.setTitleText(sourceSheet.getTitleText());
        targetSheet.setStyleId(convertStyleId(sourceSheet.getStyleId()));
        targetSheet.setThemeId(convertStyleId(sourceSheet.getThemeId()));

        ITopic sourceRootTopic = sourceSheet.getRootTopic();
        if (sourceRootTopic != null) {
            ITopic targetRootTopic = targetSheet.getRootTopic();
            copyTopicContents(sourceRootTopic, targetRootTopic);
        }

        for (IRelationship sourceRel : sourceSheet.getRelationships()) {
            IRelationship targetRel = targetWorkbook.createRelationship();
            copyRelationshipContents(sourceRel, targetRel);
            targetSheet.addRelationship(targetRel);
        }

        targetSheet.getLegend().setVisible(sourceSheet.getLegend().isVisible());
        targetSheet.getLegend()
                .setPosition(sourceSheet.getLegend().getPosition());
        for (String markerId : sourceSheet.getLegend().getMarkerIds()) {
            targetSheet.getLegend().setMarkerDescription(markerId,
                    sourceSheet.getLegend().getMarkerDescription(markerId));
        }

        for (String settingPath : sourceSheet.getSettings().getPaths()) {
            for (ISettingEntry sourceSettingEntry : sourceSheet.getSettings()
                    .getEntries(settingPath)) {
                ISettingEntry targetSettingEntry = targetSheet.getSettings()
                        .createEntry(settingPath);
                for (String key : sourceSettingEntry.getAttributeKeys()) {
                    targetSettingEntry.setAttribute(key,
                            sourceSettingEntry.getAttribute(key));
                }
                targetSheet.getSettings().addEntry(targetSettingEntry);
            }
        }

        copyComments(sourceSheet.getId(), targetSheet.getId());
    }

    private void copyTopicContents(ITopic sourceTopic, ITopic targetTopic)
            throws IOException {
        mapper.put(sourceTopic, targetTopic);
        mapper.putString(ICloneData.WORKBOOK_COMPONENTS, sourceTopic.getId(),
                targetTopic.getId());
        targetTopic.setTitleText(
                sourceTopic.hasTitle() ? sourceTopic.getTitleText() : null);
        targetTopic.setTitleWidth(sourceTopic.getTitleWidth());
        targetTopic.setFolded(sourceTopic.isFolded());
        targetTopic.setStructureClass(sourceTopic.getStructureClass());
        targetTopic.setPosition(sourceTopic.getPosition());

        targetTopic.setHyperlink(convertHyperlink(sourceTopic.getHyperlink()));

        String zClass = sourceTopic.getZClass();
        if (zClass != null) {
            targetTopic.setZClass(zClass);
        }

        for (String label : sourceTopic.getLabels()) {
            targetTopic.addLabel(label);
        }

        for (IMarkerRef markerRef : sourceTopic.getMarkerRefs()) {
            IMarkerRef mr = (IMarkerRef) cloneObject(markerRef);
            if (mr != null) {
                targetTopic.addMarker(mr.getMarkerId());
            }
        }

        targetTopic.setStyleId(convertStyleId(sourceTopic.getStyleId()));

        targetTopic.getImage().setSource(
                convertHyperlink(sourceTopic.getImage().getSource()));
        targetTopic.getImage().setWidth(sourceTopic.getImage().getWidth());
        targetTopic.getImage().setHeight(sourceTopic.getImage().getHeight());
        targetTopic.getImage()
                .setAlignment(sourceTopic.getImage().getAlignment());

        for (String type : sourceTopic.getChildrenTypes()) {
            for (ITopic sourceChild : sourceTopic.getChildren(type)) {
                ITopic targetChild = targetTopic.getOwnedWorkbook()
                        .createTopic();
                copyTopicContents(sourceChild, targetChild);
                targetTopic.add(targetChild, type);
            }
        }

        for (IBoundary sourceBoundary : sourceTopic.getBoundaries()) {
            IBoundary targetBoundary = targetTopic.getOwnedWorkbook()
                    .createBoundary();
            copyBoundaryContents(sourceBoundary, targetBoundary);
            targetTopic.addBoundary(targetBoundary);
        }

        for (ISummary sourceSummary : sourceTopic.getSummaries()) {
            ISummary targetSummary = targetTopic.getOwnedWorkbook()
                    .createSummary();
            copySummaryContents(sourceSummary, targetSummary);
            targetTopic.addSummary(targetSummary);
        }

        for (ITopicExtension sourceExt : sourceTopic.getExtensions()) {
            ITopicExtension targetExt = targetTopic
                    .createExtension(sourceExt.getProviderName());
            copyTopicExtension(sourceExt, targetExt);
        }

        targetTopic.getNumbering()
                .setFormat(sourceTopic.getNumbering().getNumberFormat());
        targetTopic.getNumbering()
                .setPrefix(sourceTopic.getNumbering().getPrefix());
        targetTopic.getNumbering()
                .setSuffix(sourceTopic.getNumbering().getSuffix());
        targetTopic.getNumbering()
                .setSeparator(sourceTopic.getNumbering().getSeparator());
        targetTopic.getNumbering().setPrependsParentNumbers(
                sourceTopic.getNumbering().prependsParentNumbers());
        targetTopic.getNumbering()
                .setDepth(sourceTopic.getNumbering().getDepth());

        INotesContent sourcePlainContent = sourceTopic.getNotes()
                .getContent(INotes.PLAIN);
        if (sourcePlainContent != null
                && sourcePlainContent instanceof IPlainNotesContent) {
            INotesContent targetPlainContent = targetWorkbook
                    .createNotesContent(INotes.PLAIN);
            if (targetPlainContent instanceof IPlainNotesContent) {
                ((IPlainNotesContent) targetPlainContent).setTextContent(
                        ((IPlainNotesContent) sourcePlainContent)
                                .getTextContent());
                targetTopic.getNotes().setContent(INotes.PLAIN,
                        targetPlainContent);
            }
        }

        INotesContent sourceHtmlContent = sourceTopic.getNotes()
                .getContent(INotes.HTML);
        if (sourceHtmlContent != null
                && sourceHtmlContent instanceof IHtmlNotesContent) {
            INotesContent targetHtmlContent = targetWorkbook
                    .createNotesContent(INotes.HTML);
            if (targetHtmlContent instanceof IHtmlNotesContent) {
                copyHtmlNotesContent((IHtmlNotesContent) sourceHtmlContent,
                        (IHtmlNotesContent) targetHtmlContent);
                targetTopic.getNotes().setContent(INotes.HTML,
                        targetHtmlContent);
            }
        }

        List<ITopic> targetAllTopics = new ArrayList<ITopic>();
        getAllChildrenTopic(targetAllTopics, targetTopic);

        fixInternalHyperlinkFor(targetAllTopics);

        copyComments(sourceTopic.getId(), targetTopic.getId());
    }

    /**
     * @param source
     * @param target
     */
    private void copyComments(String sourceObjectId, String targetObjectId) {
        Set<IComment> sourceComments = sourceWorkbook.getCommentManager()
                .getComments(sourceObjectId);
        if (!sourceComments.isEmpty()) {
            ICommentManager targetCommentManager = targetWorkbook
                    .getCommentManager();
            for (IComment sourceComment : sourceComments) {
                IComment targetComment = targetCommentManager.createComment(
                        sourceComment.getAuthor(), sourceComment.getTime(),
                        targetObjectId);
                targetComment.setContent(sourceComment.getContent());
                targetCommentManager.addComment(targetComment);
            }
        }
    }

    private void fixInternalHyperlinkFor(List<ITopic> allTargetTopics) {
        for (ITopic targetTopic : allTargetTopics) {
            String hyperlink = targetTopic.getHyperlink();
            if (HyperlinkUtils.isInternalURL(hyperlink)) {
                Object sourceElement = HyperlinkUtils.findElement(hyperlink,
                        sourceWorkbook);
                Object result = mapper.get(sourceElement);
                if (result != null) {
                    targetTopic
                            .setHyperlink(HyperlinkUtils.toInternalURL(result));
                }
            }
        }
    }

    private void copyHtmlNotesContent(IHtmlNotesContent sourceNotesContent,
            IHtmlNotesContent targetNotesContent) throws IOException {
        for (IParagraph sourceParagraph : sourceNotesContent.getParagraphs()) {
            IParagraph targetParagraph = targetNotesContent.createParagraph();
            targetParagraph
                    .setStyleId(convertStyleId(sourceParagraph.getStyleId()));
            targetNotesContent.addParagraph(targetParagraph);
            copySpanList(sourceParagraph, targetParagraph, targetNotesContent);
        }
    }

    private void copySpanList(ISpanList sourceSpanList,
            ISpanList targetSpanList, IHtmlNotesContent spanFactory)
            throws IOException {
        for (ISpan sourceSpan : sourceSpanList.getSpans()) {
            ISpan targetSpan;
            if (sourceSpan instanceof ITextSpan) {
                targetSpan = spanFactory.createTextSpan(
                        ((ITextSpan) sourceSpan).getTextContent());
            } else if (sourceSpan instanceof IImageSpan) {
                String source = ((IImageSpan) sourceSpan).getSource();
                String newImageSource = convertHyperlink(source);
                targetSpan = spanFactory.createImageSpan(newImageSource);
            } else if (sourceSpan instanceof IHyperlinkSpan) {
                targetSpan = spanFactory.createHyperlinkSpan(
                        ((IHyperlinkSpan) sourceSpan).getHref());
                copySpanList((IHyperlinkSpan) sourceSpan,
                        (IHyperlinkSpan) targetSpan, spanFactory);
            } else {
                continue;
            }
            targetSpan.setStyleId(convertStyleId(sourceSpan.getStyleId()));
            targetSpanList.addSpan(targetSpan);
        }
    }

    private void copyBoundaryContents(IBoundary sourceBoundary,
            IBoundary targetBoundary) throws IOException {
        mapper.put(sourceBoundary, targetBoundary);
        mapper.putString(ICloneData.WORKBOOK_COMPONENTS, sourceBoundary.getId(),
                targetBoundary.getId());
        targetBoundary.setTitleText(sourceBoundary.getTitleText());
        targetBoundary.setStyleId(convertStyleId(sourceBoundary.getStyleId()));
        if (sourceBoundary.isMasterBoundary()) {
            targetBoundary.setMasterBoundary(sourceBoundary.isMasterBoundary());
        } else {
            targetBoundary.setStartIndex(sourceBoundary.getStartIndex());
            targetBoundary.setEndIndex(sourceBoundary.getEndIndex());
        }
    }

    private void copySummaryContents(ISummary sourceSummary,
            ISummary targetSummary) throws IOException {
        mapper.put(sourceSummary, targetSummary);
        mapper.putString(ICloneData.WORKBOOK_COMPONENTS, sourceSummary.getId(),
                targetSummary.getId());
        targetSummary.setStyleId(convertStyleId(sourceSummary.getStyleId()));
        targetSummary.setTopicId(mapper.getString(
                ICloneData.WORKBOOK_COMPONENTS, sourceSummary.getTopicId()));
        targetSummary.setStartIndex(sourceSummary.getStartIndex());
        targetSummary.setEndIndex(sourceSummary.getEndIndex());
    }

    private void copyTopicExtension(ITopicExtension sourceExt,
            ITopicExtension targetExt) throws IOException {
        for (IResourceRef ref : sourceExt.getResourceRefs()) {
            if (IResourceRef.FILE_ENTRY.equals(ref.getType())) {
                String targetEntryPath = convertEntryPath(ref.getResourceId());
                if (targetEntryPath != null) {
                    targetExt.addResourceRef(
                            targetExt.getOwnedWorkbook().createResourceRef(
                                    IResourceRef.FILE_ENTRY, targetEntryPath));
                }
            }
        }

        copyTopicExtensionElement(sourceExt.getContent(),
                targetExt.getContent());
    }

    private void copyTopicExtensionElement(ITopicExtensionElement sourceEle,
            ITopicExtensionElement targetEle) {
        for (String key : sourceEle.getAttributeKeys()) {
            targetEle.setAttribute(key, sourceEle.getAttribute(key));
        }

        targetEle.setTextContent(sourceEle.getTextContent());

        for (ITopicExtensionElement sourceChild : sourceEle.getChildren()) {
            ITopicExtensionElement targetChild = targetEle
                    .createChild(sourceChild.getName());
            copyTopicExtensionElement(sourceChild, targetChild);
        }
    }

    private void copyRelationshipContents(IRelationship sourceRel,
            IRelationship targetRel) throws IOException {
        mapper.put(sourceRel, targetRel);
        mapper.putString(ICloneData.WORKBOOK_COMPONENTS, sourceRel.getId(),
                targetRel.getId());
        targetRel.setTitleText(sourceRel.getTitleText());

        String end1Id = mapper.getString(ICloneData.WORKBOOK_COMPONENTS,
                sourceRel.getEnd1Id());
        targetRel.setEnd1Id(end1Id == null ? sourceRel.getEnd1Id() : end1Id);

        String end2Id = mapper.getString(ICloneData.WORKBOOK_COMPONENTS,
                sourceRel.getEnd2Id());
        targetRel.setEnd2Id(end2Id == null ? sourceRel.getEnd2Id() : end2Id);

        targetRel.setStyleId(convertStyleId(sourceRel.getStyleId()));

        copyControlPointContents(sourceRel, targetRel, 0);
        copyControlPointContents(sourceRel, targetRel, 1);
    }

    private void copyControlPointContents(IRelationship sourceRel,
            IRelationship targetRel, int index) {
        IControlPoint sourceControlPoint = sourceRel.getControlPoint(index);
        if (!sourceControlPoint.hasPosition()
                && !sourceControlPoint.hasPolarAngle()
                && !sourceControlPoint.hasPolarAmount())
            return;

        IControlPoint targetControlPoint = targetRel.getControlPoint(index);
        if (sourceControlPoint.hasPosition()) {
            Point position = sourceControlPoint.getPosition();
            targetControlPoint.setPosition(new Point(position.x, position.y));
        }
        if (sourceControlPoint.hasPolarAngle()) {
            targetControlPoint
                    .setPolarAngle(sourceControlPoint.getPolarAngle());
        }
        if (sourceControlPoint.hasPolarAmount()) {
            targetControlPoint
                    .setPolarAmount(sourceControlPoint.getPolarAmount());
        }
    }

    private void copyWorkbookExtension(IWorkbookExtension sourceExt,
            IWorkbookExtension targetExt) throws IOException {
        for (IResourceRef ref : sourceExt.getResourceRefs()) {
            if (IResourceRef.FILE_ENTRY.equals(ref.getType())) {
                String targetEntryPath = convertEntryPath(ref.getResourceId());
                if (targetEntryPath != null) {
                    targetExt.addResourceRef(
                            targetExt.getOwnedWorkbook().createResourceRef(
                                    IResourceRef.FILE_ENTRY, targetEntryPath));
                }
            }
        }

        copyWorkbookExtensionElement(sourceExt.getContent(),
                targetExt.getContent());
    }

    private void copyWorkbookExtensionElement(
            IWorkbookExtensionElement sourceEle,
            IWorkbookExtensionElement targetEle) throws IOException {
        targetEle.setTextContent(sourceEle.getTextContent());
        for (String key : sourceEle.getAttributeKeys()) {
            String value = sourceEle.getAttribute(key);
            if (ATTR_RESOURCE_PATH.equals(key)) {
                value = convertEntryPath(value);
            } else if (ATTR_OBJECT_ID.equals(key)) {
                value = mapper.getString(ICloneData.WORKBOOK_COMPONENTS, value);
            }
            targetEle.setAttribute(key, value);
        }

        for (IWorkbookExtensionElement sourceE : sourceEle.getChildren()) {
            IWorkbookExtensionElement targetE = targetEle
                    .createChild(sourceE.getName());
            copyWorkbookExtensionElement(sourceE, targetE);
        }
    }

    private String convertHyperlink(String sourceHyperlink) throws IOException {
        if (sourceHyperlink == null)
            return null;

        if (HyperlinkUtils.isAttachmentURL(sourceHyperlink)) {
            String sourceEntryPath = HyperlinkUtils
                    .toAttachmentPath(sourceHyperlink);

            String targetEntryPath = convertEntryPath(sourceEntryPath);
            return targetEntryPath == null ? null
                    : HyperlinkUtils.toAttachmentURL(targetEntryPath);
        } else if (HyperlinkUtils.isInternalURL(sourceHyperlink)) {
            ///TODO handle 'xmind:xxxxx'
        }

        return sourceHyperlink;
    }

    /**
     * @param sourceEntryPath
     * @return
     * @throws IOException
     */
    private String convertEntryPath(String sourceEntryPath) throws IOException {
        IFileEntry sourceEntry = sourceManifest.getFileEntry(sourceEntryPath);
        if (sourceEntry == null || !sourceEntry.canRead())
            // TODO log missing attachment?
            return null;

        String targetEntryPath;
        if (sourceEntryPath.startsWith(ArchiveConstants.PATH_ATTACHMENTS)) {
            // convert attachments to resources (using hash as file name)
            targetEntryPath = mapper.getString(ICloneData.FILE_ENTRIES,
                    sourceEntryPath);
            if (targetEntryPath == null) {
                IFileEntry targetEntry;
                InputStream sourceStream = sourceEntry.openInputStream();
                try {
                    targetEntry = targetManifest.createAttachmentFromStream(
                            sourceStream, sourceEntryPath,
                            sourceEntry.getMediaType());
                } finally {
                    sourceStream.close();
                }
                targetEntryPath = targetEntry.getPath();
                mapper.putString(ICloneData.FILE_ENTRIES, sourceEntryPath,
                        targetEntryPath);
            }
        } else {
            targetEntryPath = sourceEntryPath;
            IFileEntry targetEntry = targetManifest
                    .getFileEntry(targetEntryPath);
            if (targetEntry == null) {
                targetEntry = targetManifest.createFileEntry(targetEntryPath,
                        sourceEntry.getMediaType());
                InputStream sourceStream = sourceEntry.openInputStream();
                try {
                    OutputStream targetStream = targetEntry.openOutputStream();
                    try {
                        FileUtils.transfer(sourceStream, targetStream, false);
                    } finally {
                        targetStream.close();
                    }
                } finally {
                    sourceStream.close();
                }
            }
        }

        return targetEntryPath;
    }

    private IStyle findOrCloneStyle(IStyle sourceStyle) throws IOException {
        if (sourceStyle == null)
            return null;

        String sourceStyleId = sourceStyle.getId();
        String targetStyleId = mapper
                .getString(ICloneData.STYLESHEET_COMPONENTS, sourceStyleId);
        if (targetStyleId != null) {
            return targetStyleSheet.findStyle(targetStyleId);
        }

        /// TODO: find similar style to reduce redundant styles

        String groupName = sourceStyleSheet.findOwnedGroup(sourceStyle);
        if (groupName == null)
            return null;

        IStyle targetStyle = targetStyleSheet
                .createStyle(sourceStyle.getType());

        targetStyle.setName(sourceStyle.getName());

        Iterator<Property> properties = sourceStyle.properties();
        while (properties.hasNext()) {
            Property p = properties.next();

            String value = p.value;
            if (HyperlinkUtils.isAttachmentURL(value)) {
                value = convertHyperlink(value);
            }

            targetStyle.setProperty(p.key, value);
        }

        Iterator<Property> defaultStyleIds = sourceStyle.defaultStyles();
        while (defaultStyleIds.hasNext()) {
            Property p = defaultStyleIds.next();
            targetStyle.setDefaultStyleId(p.key, convertStyleId(p.value));
        }

        targetStyleSheet.addStyle(targetStyle, groupName);
        mapper.put(sourceStyle, targetStyle);
        mapper.putString(ICloneData.STYLESHEET_COMPONENTS, sourceStyleId,
                targetStyle.getId());

        return targetStyle;
    }

    private String convertStyleId(String sourceStyleId) throws IOException {
        if (sourceStyleId == null)
            return null;

        String targetStyleId = mapper
                .getString(ICloneData.STYLESHEET_COMPONENTS, sourceStyleId);
        if (targetStyleId != null)
            return targetStyleId;

        IStyle sourceStyle = sourceStyleSheet.findStyle(sourceStyleId);
        if (sourceStyle == null)
            return null;

        IStyle targetStyle = findOrCloneStyle(sourceStyle);
        return targetStyle == null ? null : targetStyle.getId();
    }

}
