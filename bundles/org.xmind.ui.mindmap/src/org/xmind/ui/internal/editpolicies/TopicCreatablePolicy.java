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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.internal.DPIUtil;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.ICloneData;
import org.xmind.core.IFileEntry;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.IWorkbook;
import org.xmind.core.internal.UserDataConstants;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerSheet;
import org.xmind.core.util.FileUtils;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.GEF;
import org.xmind.gef.GraphicalViewer;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.draw2d.IMinimizable;
import org.xmind.gef.draw2d.geometry.Geometry;
import org.xmind.gef.graphicalpolicy.IStructure;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IGraphicalPart;
import org.xmind.gef.part.IPart;
import org.xmind.gef.service.IAnimationService;
import org.xmind.ui.branch.IBranchStructureExtension;
import org.xmind.ui.branch.ICreatableBranchStructureExtension;
import org.xmind.ui.commands.AddBoundaryCommand;
import org.xmind.ui.commands.AddMarkerCommand;
import org.xmind.ui.commands.AddSummaryCommand;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.CreateBoundaryCommand;
import org.xmind.ui.commands.CreateSummaryCommand;
import org.xmind.ui.commands.CreateTopicCommand;
import org.xmind.ui.commands.DeleteMarkerCommand;
import org.xmind.ui.commands.ModifyBoundaryMasterCommand;
import org.xmind.ui.commands.ModifyImageSizeCommand;
import org.xmind.ui.commands.ModifyImageSourceCommand;
import org.xmind.ui.commands.ModifyRangeCommand;
import org.xmind.ui.commands.ModifyRightNumberOfUnbalancedStructureCommand;
import org.xmind.ui.commands.ModifySummaryTopicCommand;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.commands.ModifyTopicHyperlinkCommand;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.branch.UnbalancedData;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.style.Styles;
import org.xmind.ui.util.ImageFormat;
import org.xmind.ui.util.MindMapUtils;

public class TopicCreatablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_CREATE.equals(requestType)
                || MindMapUI.REQ_CREATE_BEFORE.equals(requestType)
                || MindMapUI.REQ_CREATE_CHILD.equals(requestType)
                || MindMapUI.REQ_CREATE_PARENT.equals(requestType)
                || MindMapUI.REQ_ADD_ATTACHMENT.equals(requestType)
                || MindMapUI.REQ_ADD_MARKER.equals(requestType)
                || MindMapUI.REQ_CREATE_BOUNDARY.equals(requestType)
                || MindMapUI.REQ_CREATE_SUMMARY.equals(requestType)
                || MindMapUI.REQ_ADD_IMAGE.equals(requestType)
                || MindMapUI.REQ_CREATE_SHEET.equals(requestType)
                || MindMapUI.REQ_DUPLICATE_TOPIC.equals(requestType)
                || MindMapUI.REQ_CREATE_CALLOUT.equals(requestType);
    }

    public void handle(Request request) {
        String reqType = request.getType();
        if (GEF.REQ_CREATE.equals(reqType) //
                || MindMapUI.REQ_CREATE_CHILD.equals(reqType) //
                || MindMapUI.REQ_CREATE_BEFORE.equals(reqType) //
                || MindMapUI.REQ_CREATE_PARENT.equals(reqType)
                || MindMapUI.REQ_DUPLICATE_TOPIC.equals(reqType)
                || MindMapUI.REQ_CREATE_CALLOUT.equals(reqType)) {
            createTopic(request);
        } else if (MindMapUI.REQ_ADD_ATTACHMENT.equals(reqType)) {
            addAttachments(request);
        } else if (MindMapUI.REQ_ADD_MARKER.equals(reqType)) {
            addMarker(request);
        } else if (MindMapUI.REQ_CREATE_BOUNDARY.equals(reqType)) {
            createBoundary(request);
        } else if (MindMapUI.REQ_CREATE_SUMMARY.equals(reqType)) {
            createSummary(request);
        } else if (MindMapUI.REQ_ADD_IMAGE.equals(reqType)) {
            addImage(request);
        } else if (MindMapUI.REQ_CREATE_SHEET.equals(reqType)) {
            createSheetFromTopic(request);
        }
    }

    private void createSheetFromTopic(Request request) {
        List<IPart> targets = request.getTargets();
        if (targets.isEmpty())
            return;
        List<ITopic> topics = MindMapUtils.getTopics(targets);
        if (topics.isEmpty())
            return;

        ITopic sourceTopic = topics.get(0);
        CreateSheetFromTopicCommandBuilder builder = new CreateSheetFromTopicCommandBuilder(
                request.getTargetViewer(), request.getTargetCommandStack(),
                sourceTopic);
        if (!builder.canStart())
            return;

        builder.start();
        builder.setLabel(CommandMessages.Command_CreateSheetFromTopic);
        builder.run();
        builder.end();
    }

    private void createSummary(Request request) {
        List<IPart> targets = request.getTargets();
        Command cmd = createCreateSummaryCommand(request, targets);
        if (cmd != null) {
            cmd.setLabel(CommandMessages.Command_CreateSummary);
            saveAndRun(cmd, request.getTargetDomain());
            if (cmd instanceof ISourceProvider) {
                select(((ISourceProvider) cmd).getSources(),
                        request.getTargetViewer());
            }
        }
    }

    private Command createCreateSummaryCommand(Request request,
            List<IPart> targets) {
        List<ITopic> topics = MindMapUtils.getTopics(targets);
        if (topics.isEmpty())
            return null;
        Map<ITopic, Collection<ITopic>> map = categorize(topics, true);
        List<Command> cmds = new ArrayList<Command>(map.size() * 3);
        for (ITopic parent : map.keySet()) {
            Command cmd = createCreateSummariesCommand(parent, map.get(parent));
            if (cmd != null)
                cmds.add(cmd);
        }
        if (cmds.isEmpty())
            return null;
        return new CompoundCommand(cmds);
    }

    private Command createCreateSummariesCommand(ITopic parent,
            Collection<ITopic> topics) {
        if (topics.isEmpty())
            return null;
        List<Range> ranges = getRanges(topics);
        List<Command> cmds = new ArrayList<Command>(ranges.size());
        for (Range range : ranges) {
            if (!hasSameSummary(parent, range)) {
                Command cmd = createCreateSummaryCommand(parent, range);
                if (cmd != null)
                    cmds.add(cmd);
            }
        }
        if (cmds.isEmpty())
            return null;
        return new CompoundCommand(cmds);
    }

    private Command createCreateSummaryCommand(ITopic parent, Range range) {
        IWorkbook workbook = parent.getOwnedWorkbook();
        CreateSummaryCommand createSummary = new CreateSummaryCommand(workbook);
        ModifyRangeCommand modifyStart = new ModifyRangeCommand(createSummary,
                range.start, true);
        ModifyRangeCommand modifyEnd = new ModifyRangeCommand(createSummary,
                range.end, false);
        CreateTopicCommand createSummaryTopic = new CreateTopicCommand(
                workbook);
        ModifyTitleTextCommand modifyTitle = new ModifyTitleTextCommand(
                createSummaryTopic, MindMapMessages.TitleText_SummaryTopic);
        ModifySummaryTopicCommand modifySummaryTopic = new ModifySummaryTopicCommand(
                createSummary, createSummaryTopic);
        AddSummaryCommand addSummary = new AddSummaryCommand(createSummary,
                parent);
        AddTopicCommand addSummaryTopic = new AddTopicCommand(
                createSummaryTopic, parent, -1, ITopic.SUMMARY);
        createSummary.setSourceCollectable(false);
        modifyStart.setSourceCollectable(false);
        modifyEnd.setSourceCollectable(false);
        modifySummaryTopic.setSourceCollectable(false);
        addSummary.setSourceCollectable(false);
        return new CompoundCommand(createSummary, modifyStart, modifyEnd,
                createSummaryTopic, modifyTitle, modifySummaryTopic, addSummary,
                addSummaryTopic);
    }

    private boolean hasSameSummary(ITopic parent, Range range) {
        for (ISummary b : parent.getSummaries()) {
            int s = b.getStartIndex();
            int e = b.getEndIndex();
            if (s == range.start && e == range.end)
                return true;
        }
        return false;
    }

    private void createBoundary(Request request) {
        List<IPart> sources = request.getTargets();
        Command cmd = createCreateBoundariesCommand(request, sources);
        if (cmd != null) {
            cmd.setLabel(CommandMessages.Command_CreateBoundary);
            saveAndRun(cmd, request.getTargetDomain());
            if (cmd instanceof ISourceProvider) {
                select(((ISourceProvider) cmd).getSources(),
                        request.getTargetViewer());
            }
        }
    }

    private Command createCreateBoundariesCommand(Request request,
            List<IPart> sources) {
        List<ITopic> topics = MindMapUtils.getTopics(sources);
        if (topics.isEmpty())
            return null;

        Map<ITopic, Collection<ITopic>> map = categorize(topics, false);
        List<Command> cmds = new ArrayList<Command>(map.size() * 2);
        for (ITopic parent : map.keySet()) {
            Command cmd = createCreateBoundariesCommand(parent,
                    map.get(parent));
            if (cmd != null)
                cmds.add(cmd);
        }
        if (cmds.isEmpty())
            return null;
        return new CompoundCommand(cmds);
    }

    private Command createCreateBoundariesCommand(ITopic parent,
            Collection<ITopic> topics) {
        if (topics.isEmpty())
            return null;
        List<Range> ranges = getRanges(topics);
        List<Command> cmds = new ArrayList<Command>(ranges.size());
        for (Range range : ranges) {
            if (!hasSameBoundary(parent, range)) {
                Command cmd = createCreateBoundaryCommand(parent, range);
                if (cmd != null)
                    cmds.add(cmd);
            }
        }
        if (cmds.isEmpty())
            return null;
        return new CompoundCommand(cmds);
    }

    private boolean hasSameBoundary(ITopic parent, Range range) {
        if (range.overTopic != null) {
            for (IBoundary b : range.overTopic.getBoundaries()) {
                if (b.isMasterBoundary())
                    return true;
            }
        }
        for (IBoundary b : parent.getBoundaries()) {
            int s = b.getStartIndex();
            int e = b.getEndIndex();
            if (s == range.start && e == range.end)
                return true;
        }
        return false;
    }

    private List<Range> getRanges(Collection<ITopic> topics) {
        ITopic[] ts = topics.toArray(new ITopic[topics.size()]);
        Arrays.sort(ts, Core.getTopicComparator());
        List<Range> ranges = new ArrayList<Range>(ts.length);
        Range r = null;
        for (ITopic t : ts) {
            String topicType = t.getType();
            if (ITopic.DETACHED.equals(topicType)
                    || ITopic.CALLOUT.equals(topicType)
                    || ITopic.SUMMARY.equals(topicType)) {
                if (r != null)
                    ranges.add(r);
                r = null;
                ranges.add(new Range(t));
            } else {
                int i = t.getIndex();
                if (i >= 0) {
                    if (r == null) {
                        r = new Range(i);
                    } else if (i == r.end + 1) {
                        r.end = i;
                    } else if (i > r.end + 1) {
                        ranges.add(r);
                        r = new Range(i);
                    }
                }
            }
        }
        if (r != null) {
            ranges.add(r);
        }
        return ranges;
    }

    private Command createCreateBoundaryCommand(ITopic parent, Range range) {
        if (range.overTopic != null) {
            CreateBoundaryCommand create = new CreateBoundaryCommand(
                    range.overTopic.getOwnedWorkbook());
            ModifyBoundaryMasterCommand modify = new ModifyBoundaryMasterCommand(
                    create, true);
            AddBoundaryCommand add = new AddBoundaryCommand(create,
                    range.overTopic);
            return new CompoundCommand(create, modify, add);
        }
        CreateBoundaryCommand create = new CreateBoundaryCommand(
                parent.getOwnedWorkbook());
        ModifyRangeCommand modify1 = new ModifyRangeCommand(create, range.start,
                true);
        ModifyRangeCommand modify2 = new ModifyRangeCommand(create, range.end,
                false);
        AddBoundaryCommand add = new AddBoundaryCommand(create, parent);
        return new CompoundCommand(create, modify1, modify2, add);
    }

    private Map<ITopic, Collection<ITopic>> categorize(List<ITopic> topics,
            boolean onlyAttachedTopics) {
        Map<ITopic, Collection<ITopic>> map = new HashMap<ITopic, Collection<ITopic>>();
        for (ITopic t : topics) {
            ITopic p = t.getParent();
            if (p != null && (!onlyAttachedTopics || t.isAttached())
                    && !isAncestorInCollection(p, topics)) {
                Collection<ITopic> c = map.get(p);
                if (c == null) {
                    c = new HashSet<ITopic>();
                    map.put(p, c);
                }
                c.add(t);
            }
        }
        return map;
    }

    private boolean isAncestorInCollection(ITopic parent,
            Collection<ITopic> topics) {
        if (parent == null)
            return false;
        if (topics.contains(parent))
            return true;
        return isAncestorInCollection(parent.getParent(), topics);
    }

    private void addMarker(Request request) {
        List<IPart> targets = request.getTargets();
        MindMapUIPlugin.getDefault().getUsageDataCollector().trackEvent(
                UserDataConstants.CATEGORY_MARKER,
                UserDataConstants.USE_MARKERS);
        Command cmd = createAddMarkerCommand(request, targets);
        if (cmd != null) {
            cmd.setLabel(CommandMessages.Command_AddMarker);
            saveAndRun(cmd, request.getTargetDomain());
            if (cmd instanceof ISourceProvider) {
                select(((ISourceProvider) cmd).getSources(),
                        request.getTargetViewer());
            }
        }
    }

    private Command createAddMarkerCommand(Request request,
            List<IPart> targets) {
        String[] markerIds = getMarkerIds(request);
        if (markerIds == null || markerIds.length == 0)
            return null;

        List<Command> cmds = new ArrayList<Command>(targets.size());
        createAddMarkerCommand(targets, cmds, markerIds);
        if (!cmds.isEmpty()) {
            return new CompoundCommand(cmds);
        }
        return null;
    }

    private String[] getMarkerIds(Request request) {
        Object param = request.getParameter(MindMapUI.PARAM_MARKER_ID);
        if (param instanceof String)
            return new String[] { (String) param };
        if (param instanceof String[])
            return (String[]) param;
        return null;
    }

    private void createAddMarkerCommand(List<IPart> targets, List<Command> cmds,
            String... markerIds) {
        for (IPart source : targets) {
            Object m = MindMapUtils.getRealModel(source);
            if (m instanceof ITopic) {
                ITopic t = (ITopic) m;
                for (String markerId : markerIds) {
                    createAddMarkerCommand(t, markerId, cmds);
                }
            }
        }
    }

    private void createAddMarkerCommand(ITopic topic, String newMarkerId,
            List<Command> cmds) {
        if (topic.hasMarker(newMarkerId))
            return;

        IMarker marker = findMarker(topic, newMarkerId);
        if (marker != null) {
            IMarkerGroup group = marker.getParent();
            IMarkerSheet sheet = marker.getOwnedSheet();
            if (!sheet.isPermanent()) {
                IMarkerSheet markerSheet = topic.getOwnedWorkbook()
                        .getMarkerSheet();
                IMarker existingMarker = markerSheet.findMarker(marker.getId());
                if (existingMarker == null || !markerSheet
                        .equals(existingMarker.getOwnedSheet())) {
                    ICloneData cloneData = topic.getOwnedWorkbook()
                            .clone(Arrays.asList(marker));
                    Object cloned = cloneData.get(marker);
                    if (cloned instanceof IMarker) {
                        marker = (IMarker) cloned;
                        group = marker.getParent();
                        newMarkerId = marker.getId();
                    }
                }
            }
            if (group != null && group.isSingleton()) {
                removeSingletonMarkers(topic, newMarkerId, group, cmds);
            }
            cmds.add(new AddMarkerCommand(topic, newMarkerId));
        }
    }

    private IMarker findMarker(ITopic topic, String markerId) {
        IWorkbook workbook = topic.getOwnedWorkbook();
        if (workbook != null) {
            IMarkerSheet markerSheet = workbook.getMarkerSheet();
            if (markerSheet != null) {
                IMarker ownedMarker = markerSheet.findMarker(markerId);
                if (ownedMarker == null) {
                    ownedMarker = MindMapUI.getResourceManager()
                            .getUserMarkerSheet().findMarker(markerId);
                }
                return ownedMarker;
            }
        }
        return null;
    }

    private void removeSingletonMarkers(ITopic topic, String newMarkerId,
            IMarkerGroup group, List<Command> cmds) {
        for (IMarker m : group.getMarkers()) {
            String markerId = m.getId();
            if (!newMarkerId.equals(markerId)) {
                if (topic.hasMarker(markerId)) {
                    cmds.add(new DeleteMarkerCommand(topic, markerId));
                }
            }
        }
    }

    private void addImage(Request request) {
        List<ITopic> topics = MindMapUtils.getTopics(request.getTargets());
        if (topics.isEmpty())
            return;

        Command command = createAddImageCommand(request, topics);
        if (command != null) {
            command.setLabel(CommandMessages.Command_InsertImage);
            saveAndRun(command, request.getTargetDomain());
            if (command instanceof ISourceProvider) {
                select(((ISourceProvider) command).getSources(),
                        request.getTargetViewer());
            }
        }
    }

    private Command createAddImageCommand(Request request,
            final List<ITopic> topics) {
        String[] paths = getPaths(request);
        if (paths == null || paths.length == 0)
            return null;

        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return null;

        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        if (sheet == null)
            return null;

        final IWorkbook workbook = sheet.getOwnedWorkbook();
        final String path = paths[0];
        final List<Command> cmds = new ArrayList<Command>(paths.length);
        String errMsg = NLS.bind("Failed to copy file into this workbook: {0}", //$NON-NLS-1$
                path);
        SafeRunner.run(new SafeRunnable(errMsg) {
            public void run() throws Exception {
                Command cmd = createAddImageCommand(workbook, path, topics);
                if (cmd != null)
                    cmds.add(cmd);
            }
        });
        if (cmds.isEmpty())
            return null;
        return new CompoundCommand(cmds);
    }

    private Command createAddImageCommand(IWorkbook workbook, String path,
            List<ITopic> topics) throws Exception {
        Dimension size = getImageSize(path);
        ImageFormat format = ImageFormat
                .findByExtension(FileUtils.getExtension(path), ImageFormat.PNG);
        IFileEntry e = workbook.getManifest().createAttachmentFromFilePath(path,
                format.getMediaType());
        if (e == null)
            return null;

        String entryPath = e.getPath();
        String hyperlink = HyperlinkUtils.toAttachmentURL(entryPath);

        List<Command> cmds = new ArrayList<Command>(topics.size());
        for (ITopic t : topics) {
            Command cmd = createAddImageCommand(t, hyperlink, size);
            if (cmd != null)
                cmds.add(cmd);
        }
        if (cmds.isEmpty())
            return null;
        return new CompoundCommand(cmds);
    }

    private Dimension getImageSize(String path) {
        try {
            Image tempImage = new Image(Display.getCurrent(), path);
            Rectangle imageBounds = tempImage.getBounds();
            tempImage.dispose();
            boolean needZoom = DPIUtil.getDeviceZoom() > 100;
            int width = needZoom ? imageBounds.width / 2 : imageBounds.width;
            int height = needZoom ? imageBounds.height / 2 : imageBounds.height;
            return Geometry.getScaledConstrainedSize(width, height,
                    MindMapUI.IMAGE_INIT_WIDTH, MindMapUI.IMAGE_INIT_HEIGHT);
        } catch (Throwable e) {
        }
        return null;
    }

    private Command createAddImageCommand(ITopic t, String hyperlink,
            Dimension size) {
        ModifyImageSourceCommand modifyImageSource = new ModifyImageSourceCommand(
                t, hyperlink);

        if (size != null) {
            ModifyImageSizeCommand modifySize = new ModifyImageSizeCommand(t,
                    size.width, size.height);
            return new CompoundCommand(modifyImageSource, modifySize);
        } else {
            return new CompoundCommand(modifyImageSource);
        }
    }

    private void addAttachments(Request request) {
        String[] paths = getPaths(request);
        if (paths == null || paths.length == 0)
            return;

        List<ITopic> topics = MindMapUtils.getTopics(request.getTargets());
        if (topics.isEmpty())
            return;

        Command cmd = createAddAttachmentCommand(request, topics, paths);
        if (cmd != null) {
            cmd.setLabel(CommandMessages.Command_InsertAttachment);
            saveAndRun(cmd, request.getTargetDomain());
            if (cmd instanceof ISourceProvider) {
                select(((ISourceProvider) cmd).getSources(),
                        request.getTargetViewer());
            }
        }
    }

    private Command createAddAttachmentCommand(Request request,
            final List<ITopic> topics, String[] paths) {
        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return null;

        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        if (sheet == null)
            return null;

        final IWorkbook workbook = sheet.getOwnedWorkbook();
        final List<Command> cmds = new ArrayList<Command>(paths.length);
        for (final String path : paths) {
            String errMsg = NLS
                    .bind("Failed to copy file into this workbook: {0}", path); //$NON-NLS-1$
            SafeRunner.run(new SafeRunnable(errMsg) {
                public void run() throws Exception {
                    Command cmd = createAddAttachmentCommand(workbook, path,
                            topics);
                    if (cmd != null)
                        cmds.add(cmd);
                }
            });
        }
        if (cmds.isEmpty())
            return null;
        return new CompoundCommand(cmds);
    }

    private Command createAddAttachmentCommand(IWorkbook workbook, String path,
            List<ITopic> topics) throws IOException {
        IFileEntry e = workbook.getManifest()
                .createAttachmentFromFilePath(path);
        if (e == null)
            return null;

        String hyperlink = HyperlinkUtils.toAttachmentURL(e.getPath());
        String title = new File(path).getName();

        List<Command> cmds = new ArrayList<Command>(topics.size());
        for (ITopic t : topics) {
            Command cmd = createAddAttachmentCommand(t, title, hyperlink);
            if (cmd != null)
                cmds.add(cmd);
        }
        if (cmds.isEmpty())
            return null;
        return new CompoundCommand(cmds);
    }

    private Command createAddAttachmentCommand(ITopic parent, String title,
            String hyperlink) {
        CreateTopicCommand create = new CreateTopicCommand(
                parent.getOwnedWorkbook());

        AddTopicCommand insert = new AddTopicCommand(create, parent, -1,
                ITopic.ATTACHED);

        ModifyTitleTextCommand setTitle = new ModifyTitleTextCommand(create,
                title);

        ModifyTopicHyperlinkCommand setHyperlink = new ModifyTopicHyperlinkCommand(
                create, hyperlink);

        return new CompoundCommand(create, insert, setTitle, setHyperlink);
    }

    private String[] getPaths(Request request) {
        Object param = request.getParameter(GEF.PARAM_PATH);
        if (param instanceof String)
            return new String[] { (String) param };
        if (param instanceof String[])
            return (String[]) param;
        return null;
    }

    private void createTopic(Request request) {
        IPart source = request.getPrimaryTarget();
        IBranchPart sourceBranch = MindMapUtils.findBranch(source);
        if (sourceBranch == null)
            return;

        ITopic sourceTopic = sourceBranch.getTopic();
        String reqType = request.getType();
        if (GEF.REQ_CREATE.equals(reqType)) {
            if (ITopic.SUMMARY.equals(sourceTopic.getType()))
                return;
            if (sourceBranch.isCentral()) {
                reqType = MindMapUI.REQ_CREATE_CHILD;//
            }
        } else if (MindMapUI.REQ_CREATE_BEFORE.equals(reqType)
                || MindMapUI.REQ_DUPLICATE_TOPIC.equals(reqType)) {
            if (ITopic.SUMMARY.equals(sourceTopic.getType()))
                return;
        } else if (MindMapUI.REQ_CREATE_PARENT.equals(reqType)) {
            if (sourceBranch.isCentral())
                return;
        }

        IViewer viewer = request.getTargetViewer();
        ITopic centralTopic = (ITopic) viewer.getAdapter(ITopic.class);
        if (centralTopic == null)
            return;

        CreateTopicCommandBuilder builder = null;
        List<ITopic> topics = null;
        if (MindMapUI.REQ_CREATE_PARENT.equals(reqType)) {
            topics = MindMapUtils.filterOutDescendents(
                    MindMapUtils.getTopics(request.getTargets()), null);
            sort(topics);

            if (!topics.isEmpty())
                sourceTopic = topics.get(0);

            builder = new CreateTopicCommandBuilder(viewer,
                    request.getTargetCommandStack(), sourceTopic, reqType,
                    topics);
        } else {
            builder = new CreateTopicCommandBuilder(viewer,
                    request.getTargetCommandStack(), sourceTopic, reqType);
        }

        if (!builder.canStart())
            return;

        if (ITopic.ATTACHED.equals(builder.getTargetType())) {
            IPart parentPart = viewer.findPart(builder.getTargetParent());
            if (parentPart instanceof ITopicPart) {
                IBranchPart parentBranch = ((ITopicPart) parentPart)
                        .getOwnerBranch();
                if (parentBranch != null) {
                    IBranchPart sourceChild;
                    if (parentBranch.getSubBranches().contains(sourceBranch)) {
                        sourceChild = sourceBranch;
                    } else {
                        sourceChild = null;
                    }

                    IStructure structure = parentBranch.getBranchPolicy()
                            .getStructure(parentBranch);
                    if (structure instanceof ICreatableBranchStructureExtension) {
                        ((ICreatableBranchStructureExtension) structure)
                                .decorateCreateRequest(parentBranch,
                                        sourceChild, request);
                    }

                    if (parentBranch.isCentral()) {
                        String centralTopicStructure = centralTopic
                                .getStructureClass();
                        boolean isUnbalancedStructure = centralTopicStructure == null
                                || UnbalancedData.STRUCTUREID_UNBALANCED
                                        .equalsIgnoreCase(
                                                centralTopicStructure);
                        if (isUnbalancedStructure) {
                            ITopicExtension extension = centralTopic
                                    .createExtension(
                                            UnbalancedData.EXTENTION_UNBALANCEDSTRUCTURE);
                            ITopicExtensionElement element = extension
                                    .getContent().getCreatedChild(
                                            UnbalancedData.EXTENTIONELEMENT_RIGHTNUMBER);
                            String preCreateRightNum = element.getTextContent();
                            if (preCreateRightNum == null)
                                preCreateRightNum = String.valueOf(0);
                            int postCreateRightNum = Integer
                                    .valueOf(preCreateRightNum).intValue();

                            int deletedRightNum = 0;
                            if (MindMapUI.REQ_CREATE_PARENT.equals(reqType)) {
                                if (topics != null && !topics.isEmpty()) {
                                    for (ITopic rightTopic : topics) {
                                        if (rightTopic
                                                .getParent() == centralTopic
                                                && rightTopic
                                                        .getIndex() <= postCreateRightNum
                                                                - 1)
                                            deletedRightNum++;
                                    }
                                }
                            }

                            List<IBranchPart> mainBranches = parentBranch
                                    .getSubBranches();
                            int size = mainBranches.size();
                            if ((((IBranchStructureExtension) structure)
                                    .getChildTargetOrientation(parentBranch,
                                            sourceBranch) == PositionConstants.WEST
                                    && parentBranch != sourceBranch)
                                    || (parentBranch == sourceBranch
                                            && size <= 2
                                            && postCreateRightNum == size)) {
                                postCreateRightNum++;
                                if (size > 2
                                        && (mainBranches.indexOf(
                                                sourceBranch) == size - 1)
                                        && !MindMapUI.REQ_CREATE_PARENT
                                                .equals(reqType))
                                    postCreateRightNum--;
                            }

                            postCreateRightNum = postCreateRightNum
                                    - deletedRightNum;

                            if (!preCreateRightNum.equals(postCreateRightNum)) {
                                ModifyRightNumberOfUnbalancedStructureCommand modifyRightNumberCommand = new ModifyRightNumberOfUnbalancedStructureCommand(
                                        centralTopic, preCreateRightNum,
                                        postCreateRightNum);
                                builder.addPendingCommand(
                                        modifyRightNumberCommand, false);
                                //if sourceCollectable is true,the operations of create and move topic 
                                //maybe lead to undo/redo error.
                            }
                        }

                    }

                }
            }
        }

        PropertyCommandBuilder builder2 = new PropertyCommandBuilder(viewer,
                builder, request);
        builder.setLabel(CommandMessages.Command_CreateTopic);
        builder.start();
        builder2.start();

        if (MindMapUI.REQ_DUPLICATE_TOPIC.equals(reqType)) {
            builder.createDuplicateTopic();
        } else {
            builder.createTopic();
        }

        if (builder.getCreatedTopic() != null) {
            builder2.addSource(builder.getCreatedTopic(), true);
        }

        builder2.end();
        builder.end();

        Command command = builder.getCommand();

//        Command cmd = createCreateTopicCommand(request, sourceTopic,
//                sourceBranch, request.getTargetViewer());
//        if (cmd == null)
//            return;
//
//        cmd.setLabel(CommandMessages.Command_CreateTopic);
//        saveAndRun(cmd, request.getTargetDomain());

        if (command instanceof ISourceProvider) {
            Object creation = ((ISourceProvider) command).getSource();
            if (creation != null) {
                boolean needSelect = true;
                if (creation instanceof ITopic) {
                    IPart topicPart = viewer.findPart(creation);
                    IBranchPart branch = MindMapUtils.findBranch(topicPart);
                    IStyleSelector styleSelector = branch.getBranchPolicy()
                            .getStyleSelector(branch);
                    if (styleSelector != null) {
                        String hideCallout = styleSelector.getStyleValue(branch,
                                Styles.HideCallout);
                        needSelect = !(Boolean.parseBoolean(hideCallout)
                                && ITopic.CALLOUT
                                        .equals(((ITopic) creation).getType()));
                    }
                }
                if (needSelect)
                    select(creation, request.getTargetViewer());
                if (isAnimationRequired(request))
                    animateCommand(command, request.getTargetViewer());
            }
        }
    }

//    private Command createCreateTopicCommand(Request request,
//            ITopic sourceTopic, IBranchPart sourceBranch, IViewer viewer) {
//        CreateTopicCommand create = new CreateTopicCommand(sourceTopic
//                .getOwnedWorkbook());
//
//        String reqType = request.getType();
//        ITopic parent = null;
//        Command insert = null;
//        int index = -1;
//        String topicType = null;
//        if (MindMapUI.REQ_CREATE_CHILD.equals(reqType)) {
//            parent = sourceTopic;
//            topicType = ITopic.ATTACHED;
//            insert = new AddTopicCommand(create, parent, -1, topicType);
//            index = parent.getChildren(topicType).size();
//        } else {
//            parent = sourceTopic.getParent();
//            if (parent != null) {
//                if (GEF.REQ_CREATE.equals(reqType)) {
//                    index = sourceTopic.getIndex() + 1;
//                    topicType = sourceTopic.getType();
//                    insert = new AddTopicCommand(create, parent, index,
//                            topicType);
//                } else if (MindMapUI.REQ_CREATE_BEFORE.equals(reqType)
//                        || MindMapUI.REQ_CREATE_PARENT.equals(reqType)) {
//                    index = sourceTopic.getIndex();
//                    topicType = sourceTopic.getType();
//                    insert = new AddTopicCommand(create, parent, index,
//                            topicType);
//                }
//            }
//        }
//        if (insert == null || topicType == null)
//            return null;
//
//        List<Command> cmds = new ArrayList<Command>(5);
//        cmds.add(create);
//        cmds.add(insert);
//
//        if (parent != null && parent.isFolded()) {
//            ModifyFoldedCommand extend = new ModifyFoldedCommand(parent, false);
//            extend.setSourceCollectable(false);
//            cmds.add(extend);
//        }
//
//        if (topicType != null) {
//            Command setTitle = createSetTitleTextCommand(parent, topicType,
//                    create);
//            if (setTitle != null) {
//                cmds.add(setTitle);
//            }
//        }
//
//        if (MindMapUI.REQ_CREATE_PARENT.equals(reqType)) {
//            if (ITopic.DETACHED.equals(topicType)) {
//                cmds.add(new ModifyPositionCommand(create, sourceTopic
//                        .getPosition()));
//                cmds.add(new ModifyPositionCommand(sourceTopic, null));
//            }
//            cmds.add(new DeleteTopicCommand(sourceTopic));
//            cmds.add(new AddTopicCommand(sourceTopic, create));
//        } else {
//            if (GEF.REQ_CREATE.equals(reqType)) {
//                if (ITopic.DETACHED.equals(topicType)) {
//                    Point newPosition = calcNewPosition(sourceTopic, viewer);
//                    cmds.add(new ModifyPositionCommand(create,
//                            new org.xmind.core.util.Point(newPosition.x,
//                                    newPosition.y)));
//                }
//            }
//            if (parent != null && index >= 0
//                    && ITopic.ATTACHED.equals(topicType)) {
//                createModifyRangesCommand(reqType, parent, index, sourceTopic,
//                        cmds);
//            }
//        }
//
//        if (ITopic.ATTACHED.equals(topicType)) {
//            IPart parentPart = viewer.findPart(parent);
//            IBranchPart parentBranch = MindMapUtils.findBranch(parentPart);
//            if (parentBranch != null) {
//                IBranchPart sourceChild;
//                if (parentBranch.getSubBranches().contains(sourceBranch)) {
//                    sourceChild = sourceBranch;
//                } else {
//                    sourceChild = null;
//                }
//                IStructure structure = parentBranch.getBranchPolicy()
//                        .getStructure(parentBranch);
//                if (structure instanceof ICreatableBranchStructureExtension) {
//                    ((ICreatableBranchStructureExtension) structure)
//                            .decorateCreateRequest(parentBranch, sourceChild,
//                                    request);
//                }
//            }
//        }
//
//        PropertyCommandBuilder builder = new PropertyCommandBuilder(viewer,
//                create);
//        builder.addFromRequest(request, false);
//        if (!builder.isEmpty()) {
//            for (Command cmd : builder.getCommands()) {
//                cmds.add(cmd);
//            }
//        }
//
//        return new CompoundCommand(cmds);
//    }
//
//    private Point calcNewPosition(ITopic sourceTopic, IViewer viewer) {
//        IPart part = viewer.findPart(sourceTopic);
//        if (part instanceof IGraphicalPart) {
//            IFigure figure = ((IGraphicalPart) part).getFigure();
//            if (figure instanceof IReferencedFigure) {
//                Point ref = ((IReferencedFigure) figure).getReference();
//                return ref.getTranslated(0,
//                        figure.getPreferredSize().height + 30);
//            }
//        }
//        org.xmind.core.util.Point position = sourceTopic.getPosition();
//        return new Point(position.x, position.y + 60);
//    }
//
//    private Command createSetTitleTextCommand(ITopic parent, String type,
//            ISourceProvider sourceProvider) {
//        if (parent == null)
//            return null;
//
//        String newTitle;
//        if (ITopic.DETACHED.equals(type)) {
//            newTitle = MindMapMessages.TitleText_FloatingTopic;
//        } else {
//            int size = parent.getChildren(type).size();
//            int index = size + 1;
//            if (parent.isRoot()) {
//                newTitle = String.format(MindMapMessages.TitleText_MainTopic,
//                        index);
//            } else {
//                newTitle = String.format(MindMapMessages.TitleText_Subtopic,
//                        index);
//            }
//        }
//        return new ModifyTitleTextCommand(sourceProvider, newTitle);
//    }
//
//    private void createModifyRangesCommand(String reqType, ITopic parent,
//            int index, ITopic sourceTopic, List<Command> cmds) {
//        int sourceTopicIndex = sourceTopic == null ? -1 : sourceTopic
//                .getIndex();
//        fillModifyRangeCommands(parent.getBoundaries(), reqType,
//                sourceTopicIndex, parent, index, cmds);
//        fillModifyRangeCommands(parent.getSummaries(), reqType,
//                sourceTopicIndex, parent, index, cmds);
//    }
//
//    private void fillModifyRangeCommands(Set<? extends ITopicRange> ranges,
//            String reqType, int sourceTopicIndex, ITopic parent, int index,
//            List<Command> cmds) {
//        for (ITopicRange r : ranges) {
//            Command cmd = createModifyRangeCommand(r, reqType,
//                    sourceTopicIndex, parent, index);
//            if (cmd != null)
//                cmds.add(cmd);
//        }
//    }
//
//    private Command createModifyRangeCommand(ITopicRange r, String reqType,
//            int sourceTopicIndex, ITopic parent, int index) {
//        int startIndex = r.getStartIndex();
//        int endIndex = r.getEndIndex();
//        if ((GEF.REQ_CREATE.equals(reqType) && sourceTopicIndex == endIndex)
//                || (MindMapUI.REQ_CREATE_BEFORE.equals(reqType) && sourceTopicIndex == startIndex)) {
//            ModifyRangeCommand cmd = new ModifyRangeCommand(r, endIndex + 1,
//                    false);
//            cmd.setSourceCollectable(false);
//            return cmd;
//        }
//
//        if (startIndex >= index || endIndex >= index) {
//            List<ITopic> subtopics = r.getEnclosingTopics();
//            if (!subtopics.isEmpty()) {
//                ITopic start = subtopics.get(0);
//                ITopic end = subtopics.get(subtopics.size() - 1);
//                return new ModifyTopicRangeCommand(r, start, end);
//            }
//        }
//        return null;
//    }

    private void sort(List<ITopic> topics) {
        if (topics == null || topics.size() == 0) {
            return;
        }

        Collections.sort(topics, new Comparator<ITopic>() {

            public int compare(ITopic o1, ITopic o2) {
                return o1.getIndex() - o2.getIndex();
            }
        });
    }

    protected void doAnimateCommand(Command cmd, IAnimationService anim,
            IViewer viewer) {
        Object source = ((ISourceProvider) cmd).getSource();
        if (source != null) {
            IMinimizable min = getMinimizable(source, viewer);
            if (min != null) {
                min.setMinimized(true);
                ((GraphicalViewer) viewer).getLightweightSystem()
                        .getUpdateManager().performValidation();
            }
        }

        super.doAnimateCommand(cmd, anim, viewer);
    }

    protected void createAnimation(Command cmd, IViewer viewer) {
        Object source = ((ISourceProvider) cmd).getSource();
        IMinimizable min = getMinimizable(source, viewer);
        if (min != null) {
            min.setMinimized(false);
        }
    }

    private IMinimizable getMinimizable(Object o, IViewer viewer) {
        IPart part = viewer.findPart(o);
        if (part instanceof IGraphicalPart) {
            IFigure figure = ((IGraphicalPart) part).getFigure();
            if (figure instanceof IMinimizable) {
                return (IMinimizable) figure;
            }
        }
        return null;
    }

}
