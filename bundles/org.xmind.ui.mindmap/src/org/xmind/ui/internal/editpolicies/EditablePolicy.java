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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;
import org.xmind.core.Core;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicComponent;
import org.xmind.gef.GEF;
import org.xmind.gef.ISourceProvider;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.dnd.DndData;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.dnd.IDndSupport;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class EditablePolicy extends DeletablePolicy {

//    private static final String DEFAULT_DND_CLIENT_ID = MindMapUI.DND_MINDMAP_ELEMENT;

//    private boolean isCutPrev = false;

//    private Map<Object, Object> transferMap = null;

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_COPY.equals(requestType)
                || GEF.REQ_CUT.equals(requestType)
                || GEF.REQ_PASTE.equals(requestType)
                || MindMapUI.REQ_REPLACE_ALL.equals(requestType);
    }

    public void handle(Request request) {
        String reqType = request.getType();
        if (GEF.REQ_COPY.equals(reqType)) {
            copy(request);
        } else if (GEF.REQ_CUT.equals(reqType)) {
            cut(request);
        } else if (GEF.REQ_PASTE.equals(reqType)) {
            paste(request);
        } else if (MindMapUI.REQ_REPLACE_ALL.equals(reqType)) {
            replaceAll(request);
        } else if (!GEF.REQ_DELETE.equals(reqType)) {
            super.handle(request);
        }
    }

    protected void copy(Request request) {
        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return;

        List<IPart> sources = request.getTargets();
        List<Object> models = filter(MindMapUtils.getRealModels(sources),
                viewer);
        if (models.isEmpty())
            return;

        performCopy(models.toArray(), request.getTargetViewer());
    }

    protected List<Object> filter(List<Object> elements, IViewer viewer) {
        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        ITopic root = (ITopic) viewer.getAdapter(ITopic.class);
        ArrayList<Object> list = new ArrayList<Object>(elements.size());
        for (Object o : elements) {
            if (!shouldRemove(o, elements, sheet, root, viewer))
                list.add(o);
        }
        return list;
    }

    private boolean shouldRemove(Object element, List<Object> elements,
            ISheet sheet, ITopic root, IViewer viewer) {
        if (element instanceof IRelationship) {
            IRelationship r = (IRelationship) element;
            for (Object o : elements) {
                if (o instanceof ISheet && o.equals(r.getParent()))
                    return true;
            }
        } else if (element instanceof ITopicComponent) {
            ITopicComponent c = (ITopicComponent) element;
            ISheet s = null;
            ITopic p = null;
            for (Object o : elements) {
                if (o instanceof ISheet) {
                    if (s == null)
                        s = c.getOwnedSheet();
                    if (o.equals(s))
                        return true;
                }
                if (p == null)
                    p = c.getParent();
                if (o instanceof ITopic && p != null
                        && MindMapUtils.isDescendentOf(p, (ITopic) o))
                    return true;
            }
        }
        return false;
    }

    protected void performCopy(Object[] elements, IViewer viewer) {
        elements = filterAndSort(elements);

        IDndSupport dndSupport = viewer.getDndSupport();
        if (dndSupport != null) {
            String[] ids = dndSupport.getDndClientIds();
            if (ids.length > 0) {
                List<Object> data = new ArrayList<Object>(ids.length);
                List<Transfer> transfers = new ArrayList<Transfer>(ids.length);
                for (String id : ids) {
                    IDndClient client = dndSupport.getDndClient(id);
                    if (client != null) {
                        Object object = client.toTransferData(elements, viewer);
                        if (object != null) {
                            data.add(object);
                            Transfer transfer = client.getTransfer();
                            transfers.add(transfer);
                        }
                    }
                }
                if (!data.isEmpty()) {
                    Clipboard clipboard = new Clipboard(Display.getCurrent());
                    clipboard.setContents(data.toArray(),
                            transfers.toArray(new Transfer[0]));
                    clipboard.dispose();
                }
            }
        }
    }

    private Object[] filterAndSort(Object[] elements) {
        if (elements == null) {
            return null;
        }

        List<ITopic> topics = new ArrayList<ITopic>();
        List<Object> others = new ArrayList<Object>();

        for (Object element : elements) {
            if (element instanceof ITopic) {
                topics.add((ITopic) element);
            } else {
                others.add(element);
            }
        }

        List<ITopic> filteredTopics = MindMapUtils.filterOutDescendents(topics,
                null);
        Collections.sort(filteredTopics, Core.getTopicComparator());

        List<Object> all = new ArrayList<Object>(filteredTopics);
        all.addAll(others);
        return all.toArray();
    }

    protected void cut(Request request) {
        List<IPart> sources = request.getTargets();
        List<Object> models = MindMapUtils.getRealModels(sources);
        if (models.isEmpty())
            return;
//        isCutPrev = true;
        performCopy(models.toArray(), request.getTargetViewer());
        delete(request);
    }

    protected String getDeleteLabel(String type) {
        if (MindMapUI.CATEGORY_TOPIC.equals(type))
            return CommandMessages.Command_CutTopic;
        if (MindMapUI.CATEGORY_RELATIONSHIP.equals(type))
            return CommandMessages.Command_CutRelationship;
        if (MindMapUI.CATEGORY_MARKER.equals(type))
            return CommandMessages.Command_DeleteMarker;
        if (MindMapUI.CATEGORY_SHEET.equals(type))
            return CommandMessages.Command_CutSheet;
        if (MindMapUI.CATEGORY_BOUNDARY.equals(type))
            return CommandMessages.Command_DeleteBoundary;
        return CommandMessages.Command_Cut;
    }

    protected void paste(Request request) {
        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return;

        IDndSupport dndSupport = viewer.getDndSupport();
        if (dndSupport == null)
            return;

        Clipboard cb = new Clipboard(Display.getCurrent());
        DndData dndData;
        try {
            dndData = dndSupport.parseData(cb.getAvailableTypes(), cb, false);
        } finally {
            cb.dispose();
        }
        if (dndData == null)
            return;

        IDndClient client = dndSupport.getDndClient(dndData.clientId);
        if (client == null)
            return;

        request.setParameter(GEF.PARAM_DROP_OPERATION, DND.DROP_COPY);
        Command command = client.makeDNDCommand(dndData.parsedData, request);
        if (command == null)
            return;

        command.setLabel(CommandMessages.Command_Paste);
        saveAndRun(command, request.getTargetDomain());

        if (command instanceof ISourceProvider) {
            select(((ISourceProvider) command).getSources(), viewer);
        }
//
//        ISheet targetSheet = (ISheet) viewer.getAdapter(ISheet.class);
//        if (targetSheet == null)
//            return;
//
//        IWorkbook targetWorkbook = targetSheet.getParent();
//        if (targetWorkbook == null)
//            return;
//
//        IPart target = request.getPrimaryTarget();
//        Object targetModel = target == null ? viewer.getAdapter(ITopic.class)
//                : MindMapUtils.getRealModel(target);
//        Object[] elements = getElementsFromClipboard(viewer, targetModel);
//        if (elements == null || elements.length == 0)
//            return;
//
//        paste(request, elements, viewer, target,
//                MindMapUtils.getRealModels(request.getTargets()).toArray(),
//                targetWorkbook);
    }

//    protected void paste(Request request, Object[] elements, IViewer viewer,
//            IPart targetPart, Object[] targets, IWorkbook targetWorkbook) {
//
//        ICloneData result = targetWorkbook.clone(Arrays.asList(elements));
//        if (!result.hasCloned())
//            return;
//
//        Collection<Object> cloneds = result.getCloneds();
//        ITopic targetTopic;
//        if (targets.length > 0 && targets[0] instanceof ITopic) {
//            targetTopic = (ITopic) targets[0];
//        } else {
//            targetTopic = (ITopic) viewer.getAdapter(ITopic.class);
//        }
//        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
//        List<Command> cmds = new ArrayList<Command>(cloneds.size());
//        for (Object cloned : cloneds) {
//            if (cloned instanceof ITopic) {
//                if (targetTopic != null) {
//                    ITopic clonedTopic = (ITopic) cloned;
//                    AddTopicCommand addTopic = new AddTopicCommand(clonedTopic,
//                            targetTopic, -1, ITopic.ATTACHED);
//                    cmds.add(addTopic);
//                    ModifyPositionCommand resetPosition = new ModifyPositionCommand(
//                            clonedTopic, null);
//                    cmds.add(resetPosition);
//                }
//            } else if (cloned instanceof IRelationship) {
//                if (sheet != null) {
//                    IRelationship clonedRelationship = (IRelationship) cloned;
//                    AddRelationshipCommand addRel = new AddRelationshipCommand(
//                            clonedRelationship, sheet);
//                    cmds.add(addRel);
//                }
//            } else if (cloned instanceof IMarker
//                    || cloned instanceof IMarkerRef) {
//                if (targetTopic != null) {
//                    IMarker marker = (cloned instanceof IMarker) ? (IMarker) cloned
//                            : ((IMarkerRef) cloned).getMarker();
//                    if (marker != null) {
//                        IMarkerGroup group = marker.getParent();
//                        if (group.isSingleton()) {
//                            for (IMarker m : group.getMarkers()) {
//                                if (targetTopic.hasMarker(m.getId())) {
//                                    cmds.add(new DeleteMarkerCommand(
//                                            targetTopic, m.getId()));
//                                }
//                            }
//                        }
//                        String markerId = (cloned instanceof IMarker) ? ((IMarker) cloned)
//                                .getId() : ((IMarkerRef) cloned).getMarkerId();
//                        AddMarkerCommand addMarker = new AddMarkerCommand(
//                                targetTopic, markerId);
//                        cmds.add(addMarker);
//                    }
//                }
//            } else if (cloned instanceof IBoundary) {
//                IBoundary b = (IBoundary) cloned;
//                List<ITopic> topics = getSiblingTopics(targets);
//                if (!topics.isEmpty() && !hasBoundary(topics)) {
//                    if (topics.get(0).isAttached()) {
//                        cmds.add(new ModifyTopicRangeCommand(b, topics.get(0),
//                                topics.get(topics.size() - 1)));
//                        cmds.add(new AddBoundaryCommand(b, topics.get(0)
//                                .getParent()));
//                    } else if (topics.size() == 1 && !topics.get(0).isRoot()) {
//                        cmds.add(new ModifyBoundaryMasterCommand(b, true));
//                        cmds.add(new AddBoundaryCommand(b, topics.get(0)));
//                    }
//                }
//            } else if (cloned instanceof IImage) {
//                IImage image = (IImage) cloned;
//                if (targetTopic != null) {
//                    cmds.add(new ModifyImageSourceCommand(targetTopic, image
//                            .getSource()));
//                    cmds.add(new ModifyImageSizeCommand(targetTopic, image
//                            .getWidth(), image.getHeight()));
//                    cmds.add(new ModifyImageAlignmentCommand(targetTopic, image
//                            .getAlignment()));
//                }
//            } else if (cloned instanceof URI) {
//                URI uri = (URI) cloned;
//                if (targetTopic != null) {
//                    cmds.add(new ModifyTopicHyperlinkCommand(targetTopic, uri
//                            .toString()));
//                }
//            }
//        }
//
////        if (isCutPrev)
////            modifyTopicLinksRef(elements, targetWorkbook, cmds, result);
//
//        if (cmds.isEmpty())
//            return;
//
//        CompoundCommand cmd = new CompoundCommand(cmds);
//        cmd.setLabel(CommandMessages.Command_Paste);
//        saveAndRun(cmd, request.getTargetDomain());
//        select(cmd.getSources(), viewer);
//    }

//    private void modifyTopicLinksRef(Object[] elements,
//            IWorkbook targetWorkbook, List<Command> cmds, ICloneData result) {
//
//        ITopicRefCounter topicLinkRef = (ITopicRefCounter) targetWorkbook
//                .getAdapter(ITopicRefCounter.class);
//        for (Object element : elements) {
//            if (element instanceof ITopic) {
//                ITopic originTopic = (ITopic) transferMap.get(element);
//                ITopic clonedTopic = (ITopic) result.get(element);
//                modifyTopicLink(originTopic, clonedTopic, topicLinkRef, cmds);
//            }
//        }
//    }

//    private void modifyTopicLink(ITopic originTopic, ITopic clonedTopic,
//            ITopicRefCounter topicLinkRef, List<Command> cmds) {

//        String oldHref = originTopic.getId();
//        List<ITopic> topicLinks = topicLinkRef.getLinkTopics(oldHref);
//        if (topicLinks != null && !topicLinks.isEmpty()) {
//            String newHref = clonedTopic.getId();
////            ModifyTopicLinkCommand cmd = new ModifyTopicLinkCommand(topicLinks,
////                    newHref);
////            cmds.add(cmd);
////            topicLinkRef.modifyTargetLink(oldHref, newHref);
//
//            ModifyTopicHyperlinkCommand command = new ModifyTopicHyperlinkCommand(
//                    topicLinks, "xmind:#" + newHref); //$NON-NLS-1$
//            cmds.add(command);
//        }
//
//        List<ITopic> children = originTopic.getAllChildren();
//        if (children != null && !children.isEmpty()) {
//            for (int i = 0; i < children.size(); i++) {
//                ITopic oTopic = children.get(i);
//                ITopic eTopic = clonedTopic.getAllChildren().get(i);
//                modifyTopicLink(oTopic, eTopic, topicLinkRef, cmds);
//            }
//        }
//    }

//    private List<ITopic> getSiblingTopics(Object[] targets) {
//        ITopic start = null;
//        ITopic end = null;
//        for (Object o : targets) {
//            if (o instanceof ITopic) {
//                ITopic t = (ITopic) o;
//                if (start != null && MindMapUtils.isDescendentOf(start, t)) {
//                    start = null;
//                }
//                if (end != null && MindMapUtils.isDescendentOf(end, t)) {
//                    end = null;
//                }
//                if (start == null) {
//                    start = t;
//                } else if (t.getParent().equals(start.getParent())
//                        && t.getType().equals(start.getType())
//                        && t.getIndex() < start.getIndex()) {
//                    start = t;
//                }
//                if (end == null) {
//                    end = t;
//                } else if (t.getParent().equals(end.getParent())
//                        && t.getType().equals(end.getType())
//                        && t.getIndex() > end.getIndex()) {
//                    end = t;
//                }
//            }
//        }
//        if (start == null || end == null)
//            return Collections.emptyList();
//        return Arrays.asList(start, end);
//    }
//
//    private boolean hasBoundary(List<ITopic> topics) {
//        if (topics.get(0).isAttached()) {
//            int startIndex = topics.get(0).getIndex();
//            int endIndex = topics.get(topics.size() - 1).getIndex();
//            for (IBoundary b : topics.get(0).getParent().getBoundaries()) {
//                if (b.getStartIndex() == startIndex
//                        || b.getEndIndex() == endIndex)
//                    return true;
//            }
//        } else if (topics.size() == 1) {
//            for (IBoundary b : topics.get(0).getBoundaries()) {
//                if (b.isMasterBoundary())
//                    return true;
//            }
//        }
//        return false;
//    }
//
//    protected Object[] getElementsFromClipboard(IViewer viewer, Object target) {
//        IDndSupport dndSupport = viewer.getDndSupport();
//        if (dndSupport != null) {
//            String[] ids = dndSupport.getDndClientIds();
//            if (ids.length > 0) {
//                Clipboard clipboard = new Clipboard(Display.getCurrent());
//                try {
//                    return getElementsFromClipboard(viewer, target, clipboard,
//                            dndSupport, ids);
//                } finally {
//                    clipboard.dispose();
//                }
//            }
//        }
//        return null;
//    }
//
//    protected Object[] getElementsFromClipboard(IViewer viewer, Object target,
//            Clipboard clipboard, IDndSupport dndSupport, String[] ids) {
//        TransferData[] types = clipboard.getAvailableTypes();
//        IDndClient elementClient = dndSupport
//                .getDndClient(DEFAULT_DND_CLIENT_ID);
//        if (elementClient != null) {
//            Object[] elements = getElementsFromClipboard(viewer, target,
//                    elementClient, clipboard, types);
//            if (elements != null)
//                return elements;
//        }
//        for (String id : ids) {
//            if (!DEFAULT_DND_CLIENT_ID.equals(id)) {
//                IDndClient client = dndSupport.getDndClient(id);
//                Object[] elements = getElementsFromClipboard(viewer, target,
//                        client, clipboard, types);
//                if (elements != null)
//                    return elements;
//            }
//        }
//        return null;
//    }
//
//    protected Object[] getElementsFromClipboard(IViewer viewer, Object target,
//            IDndClient client, Clipboard clipboard, TransferData[] types) {
//        if (client == null)
//            return null;
//        Transfer transfer = client.getTransfer();
//        if (transfer == null)
//            return null;
////        if (transfer instanceof MindMapElementTransfer) {
////            transferMap = ((MindMapElementTransfer) transfer).getTransferMap();
////        }
//        for (TransferData type : types) {
//            if (transfer.isSupportedType(type)) {
//                Object contents = clipboard.getContents(transfer);
//                if (contents != null)
//                    return client.toViewerElements(contents, viewer, target);
//            }
//        }
//        return null;
//    }

    private void replaceAll(Request request) {
        String text = (String) request.getParameter(GEF.PARAM_TEXT);
        String replacement = (String) request
                .getParameter(MindMapUI.PARAM_REPLACEMENT);
        if (text == null || replacement == null)
            return;

        boolean ignoreCase = Boolean.TRUE
                .equals(request.getParameter(MindMapUI.PARAM_IGNORE_CASE));
        List<IPart> targets = request.getTargets();
        if (targets.isEmpty())
            return;
        if (ignoreCase) {
            text = text.toLowerCase();
        }

        PropertyCommandBuilder builder = new PropertyCommandBuilder(request);
        if (builder.canStart()) {
            builder.start();

            for (IPart target : targets) {
                Object model = MindMapUtils.getRealModel(target);
                if (model instanceof ITitled) {
                    ITitled t = (ITitled) model;
                    String oldText = t.getTitleText();
                    if (oldText != null) {
                        String searchText = ignoreCase ? oldText.toLowerCase()
                                : oldText;
                        StringBuilder sb = new StringBuilder(
                                oldText.length() + replacement.length());
                        int start = 0;
                        int index = searchText.indexOf(text);
                        while (index >= 0) {
                            sb.append(oldText.substring(start, index));
                            sb.append(replacement);
                            start = index + text.length();
                            index = searchText.indexOf(text, start);
                        }
                        if (start < oldText.length()) {
                            sb.append(oldText.substring(start));
                        }
                        String newText = sb.toString();
                        if (!newText.equals(oldText)) {
                            builder.add(new ModifyTitleTextCommand(t, newText),
                                    true);
                        }
                    }

                    builder.addSource(t, true);
                }
            }

            builder.end();
        }
    }

}