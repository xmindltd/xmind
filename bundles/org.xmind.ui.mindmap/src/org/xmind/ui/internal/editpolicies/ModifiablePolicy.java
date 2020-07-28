/*
 * *****************************************************************************
 * * Copyright (c) 2006-2012 XMind Ltd. and others. This file is a part of XMind
 * 3. XMind releases 3 and above are dual-licensed under the Eclipse Public
 * License (EPL), which is available at
 * http://www.eclipse.org/legal/epl-v10.html and the GNU Lesser General Public
 * License (LGPL), which is available at http://www.gnu.org/licenses/lgpl.html
 * See https://www.xmind.net/license.html for details. Contributors: XMind Ltd. -
 * initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.editpolicies;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.xmind.core.Core;
import org.xmind.core.IBoundary;
import org.xmind.core.ILabeled;
import org.xmind.core.IPositioned;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ISummary;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicRange;
import org.xmind.core.style.IStyle;
import org.xmind.core.style.IStyled;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.core.util.Point;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyLabelCommand;
import org.xmind.ui.commands.ModifyNumberPrependingCommand;
import org.xmind.ui.commands.ModifyNumberingDepthCommand;
import org.xmind.ui.commands.ModifyNumberingFormatCommand;
import org.xmind.ui.commands.ModifyNumberingPrefixCommand;
import org.xmind.ui.commands.ModifyNumberingSeparatorCommand;
import org.xmind.ui.commands.ModifyNumberingSuffixCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.commands.ModifyTopicHyperlinkCommand;
import org.xmind.ui.commands.ModifyTopicRangeCommand;
import org.xmind.ui.mindmap.IBranchPart;
import org.xmind.ui.mindmap.IMindMapViewer;
import org.xmind.ui.mindmap.ISheetPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.util.MindMapUtils;

public class ModifiablePolicy extends MindMapPolicyBase {

//    private static final String NULL = "$NULL$"; //$NON-NLS-1$
//
//    private static final Map<String, String> EMPTY_CONTENTS = Collections
//            .emptyMap();

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_MODIFY.equals(requestType)
                || MindMapUI.REQ_MODIFY_HYPERLINK.equals(requestType)
                || MindMapUI.REQ_MODIFY_STYLE.equals(requestType)
                || MindMapUI.REQ_MODIFY_LABEL.equals(requestType)
                || MindMapUI.REQ_MODIFY_RANGE.equals(requestType)
                || MindMapUI.REQ_MODIFY_NUMBERING.equals(requestType)
                || MindMapUI.REQ_RESET_POSITION.equals(requestType)
                || MindMapUI.REQ_MODIFY_THEME.equals(requestType)
                || MindMapUI.REQ_REMOVE_ALLSTYLES.equals(requestType);
    }

    public void handle(Request request) {
        String reqType = request.getType();
        if (GEF.REQ_MODIFY.equals(reqType)) {
            modifyText(request);
        } else if (MindMapUI.REQ_MODIFY_HYPERLINK.equals(reqType)) {
            modifyHyperlink(request);
        } else if (MindMapUI.REQ_MODIFY_STYLE.equals(reqType)) {
            modifyStyle(request);
        } else if (MindMapUI.REQ_MODIFY_RANGE.equals(reqType)) {
            modifyRange(request);
        } else if (MindMapUI.REQ_MODIFY_LABEL.equals(reqType)) {
            modifyLabel(request);
        } else if (MindMapUI.REQ_MODIFY_NUMBERING.equals(reqType)) {
            modifyNumbering(request);
        } else if (MindMapUI.REQ_RESET_POSITION.equals(reqType)) {
            resetPosition(request);
        } else if (MindMapUI.REQ_MODIFY_THEME.equals(reqType)) {
            modifyTheme(request);
        } else if (MindMapUI.REQ_REMOVE_ALLSTYLES.equals(reqType)) {
            removeAllStyles(request);
        }
    }

    private void removeAllStyles(Request request) {
        ModifyStyleCommandBuilder builder = new ModifyStyleCommandBuilder(
                request);
        String commandLabel = (String) request
                .getParameter(MindMapUI.PARAM_COMMAND_LABEL);
        if (commandLabel == null)
            commandLabel = CommandMessages.Command_RemoveStyle;
        builder.setLabel(commandLabel);

        if (!builder.canStart())
            return;

        builder.start();
        List<IStyled> sources = getAllSources(request);
        for (IStyled source : sources)
            builder.removeStyle(source);
        builder.end();
    }

    private List<IStyled> getAllSources(Request request) {
        List<IStyled> sources = new ArrayList<IStyled>();
        ISheet sheet = (ISheet) request.getTargetViewer()
                .getAdapter(ISheet.class);
        if (sheet == null)
            return sources;

        if (sheet.getStyleId() != null)
            sources.add(sheet);

        List<ITopic> topics = MindMapUtils.getAllTopics(sheet, true, true);

        for (ITopic topic : topics) {
            if (topic.getStyleId() != null)
                sources.add(topic);

            Set<IBoundary> boundaries = topic.getBoundaries();
            for (IBoundary boundary : boundaries) {
                if (boundary.getStyleId() != null)
                    sources.add(boundary);
            }

            Set<ISummary> summaries = topic.getSummaries();
            for (ISummary summary : summaries) {
                if (summary.getStyleId() != null)
                    sources.add(summary);
            }
        }

        Set<IRelationship> relationships = sheet.getRelationships();
        for (IRelationship relationship : relationships) {
            if (relationship.getStyleId() != null)
                sources.add(relationship);
        }

        return sources;
    }

    private void modifyTheme(Request request) {
        IPart target = request.getPrimaryTarget();
        if (!(target instanceof ISheetPart))
            return;

        if (!request.hasParameter(MindMapUI.PARAM_RESOURCE))
            return;

        ISheetPart sheetPart = (ISheetPart) target;
        ISheet sheet = sheetPart.getSheet();
        Object param = request.getParameter(MindMapUI.PARAM_RESOURCE);
        IStyle theme = (IStyle) param;
        ModifyThemeCommandBuilder builder = new ModifyThemeCommandBuilder(
                request.getTargetViewer(), request.getTargetCommandStack(),
                theme);
        builder.setLabel(CommandMessages.Command_ModifyTheme);
        if (!builder.canStart())
            return;

        builder.start();

        Object override = request.getParameter(MindMapUI.PARAM_OVERRIDE);
        if (PrefConstants.THEME_OVERRIDE.equals(override)) {
            List<IStyled> sources = getAllSources(request);
            for (IStyled source : sources)
                builder.removeStyle(source);
        }

        builder.modify(sheet);
        builder.end();
    }

    private void resetPosition(Request request) {

        List<IPositioned> positionOwners = new ArrayList<IPositioned>();

        List<IRelationship> rels = new ArrayList<IRelationship>();
        IPart target = request.getPrimaryTarget();

        if (target == null || target instanceof ISheetPart) {
            ISheetPart sheet;
            if (target == null) {
                sheet = ((IMindMapViewer) request.getTargetViewer())
                        .getSheetPart();
            } else {
                sheet = (ISheetPart) target;
            }
            if (sheet != null) {
                IBranchPart centralBranch = sheet.getCentralBranch();
                if (centralBranch != null
                        && MindMapUtils.isSubBranchesFreeable(centralBranch)) {
                    for (IBranchPart main : centralBranch.getSubBranches()) {
                        if (MindMapUtils.isBranchFreeable(main)) {
                            positionOwners.add(main.getTopic());
                        }
                    }
                }
            }
        } else {
            List<IPart> targets = request.getTargets();
            for (IPart p : targets) {
                Object o = MindMapUtils.getRealModel(p);
                if (o instanceof IPositioned) {
                    IPositioned positionOwner = (IPositioned) o;
                    Point position = positionOwner.getPosition();
                    if (position != null)
//                    if (positionOwner.getPosition() != null)
                        positionOwners.add(positionOwner);
                } else if (o instanceof IRelationship) {
                    rels.add((IRelationship) o);
                }
            }
        }

        if (positionOwners.isEmpty() && rels.isEmpty())
            return;

        List<Command> commands = new ArrayList<Command>(
                positionOwners.size() + rels.size() * 2);

        for (IPositioned p : positionOwners) {
            if (!(p instanceof ITopic && !((ITopic) p).isAttached())) {
                commands.add(new ModifyPositionCommand(p, null));
            }
        }
        for (IRelationship r : rels) {
            commands.add(new ModifyPositionCommand(r.getControlPoint(0), null));
            commands.add(new ModifyPositionCommand(r.getControlPoint(1), null));
//            commands.add(new ResetRelationshipControlPointCommand(r, 0));
//            commands.add(new ResetRelationshipControlPointCommand(r, 1));
        }
        if (commands.isEmpty())
            return;

        CompoundCommand cmd = new CompoundCommand(commands);
        cmd.setLabel(CommandMessages.Command_ResetPosition);
        saveAndRun(cmd, request.getTargetDomain());
    }

    private void modifyNumbering(Request request) {
        List<IPart> targets = request.getTargets();
        List<ITopic> topics = new ArrayList<ITopic>(targets.size());
        for (IPart p : targets) {
            Object o = MindMapUtils.getRealModel(p);
            if (o instanceof ITopic) {
                ITopic t = ((ITopic) o).getParent();
                if (t == null)
                    t = (ITopic) o;
                if (!topics.contains(t)) {
                    topics.add(t);
                }
            }
        }
        if (topics.isEmpty())
            return;

        List<Command> commands = new ArrayList<Command>(4);

        if (request.hasParameter(MindMapUI.PARAM_NUMBERING_FORMAT)) {
            String newFormat = (String) request
                    .getParameter(MindMapUI.PARAM_NUMBERING_FORMAT);
            commands.add(new ModifyNumberingFormatCommand(topics, newFormat));
        }

        if (request.hasParameter(MindMapUI.PARAM_NUMBERING_SEPARATOR)) {
            String newSeparator = (String) request
                    .getParameter(MindMapUI.PARAM_NUMBERING_SEPARATOR);
            commands.add(
                    new ModifyNumberingSeparatorCommand(topics, newSeparator));
        }

        if (request.hasParameter(MindMapUI.PARAM_NUMBERING_PREFIX)) {
            String newPrefix = (String) request
                    .getParameter(MindMapUI.PARAM_NUMBERING_PREFIX);
            commands.add(new ModifyNumberingPrefixCommand(topics, newPrefix));
        }

        if (request.hasParameter(MindMapUI.PARAM_NUMBERING_SUFFIX)) {
            String newSuffix = (String) request
                    .getParameter(MindMapUI.PARAM_NUMBERING_SUFFIX);
            commands.add(new ModifyNumberingSuffixCommand(topics, newSuffix));
        }

        if (request.hasParameter(MindMapUI.PARAM_NUMBERING_PREPENDING)) {
            Boolean newPrepending = (Boolean) request
                    .getParameter(MindMapUI.PARAM_NUMBERING_PREPENDING);
            commands.add(
                    new ModifyNumberPrependingCommand(topics, newPrepending));
        }
        if (request.hasParameter(MindMapUI.PARAM_NUMBERING_DEPTH)) {
            String newDepth = (String) request
                    .getParameter(MindMapUI.PARAM_NUMBERING_DEPTH);
            commands.add(new ModifyNumberingDepthCommand(topics, newDepth));
        }

        if (commands.isEmpty())
            return;

        CompoundCommand cmd = new CompoundCommand(commands);
        cmd.setLabel(CommandMessages.Command_ModifyNumbering);
        saveAndRun(cmd, request.getTargetDomain());
    }

    private void modifyLabel(Request request) {
        if (!request.hasParameter(GEF.PARAM_TEXT))
            return;

        String text = (String) request.getParameter(GEF.PARAM_TEXT);
        if (text == null)
            text = EMPTY;

        Collection<String> labels = MindMapUtils.getLabels(text);
        List<IPart> sources = request.getTargets();
        List<Command> cmds = new ArrayList<Command>(sources.size());
        for (IPart p : sources) {
            Object o = MindMapUtils.getRealModel(p);
            if (o instanceof ILabeled) {
                cmds.add(new ModifyLabelCommand((ILabeled) o, labels));
            }
        }
        if (cmds.isEmpty())
            return;

        CompoundCommand cmd = new CompoundCommand(cmds);
        cmd.setLabel(CommandMessages.Command_ModifyLabels);
        saveAndRun(cmd, request.getTargetDomain());
        select(cmd.getSources(), request.getTargetViewer());
    }

    private void modifyRange(Request request) {
        IPart target = request.getPrimaryTarget();
        ITopicRange targetRange = (ITopicRange) target
                .getAdapter(ITopicRange.class);
        if (targetRange == null)
            return;

        Object param = request.getParameter(MindMapUI.PARAM_RANGE);
        if (param == null || !(param instanceof Object[]))
            return;

        Object[] newRange = (Object[]) param;
        if (newRange.length == 0)
            return;

        List<ITopic> topics = new ArrayList<ITopic>(newRange.length);
        ITopic parent = null;
        for (Object o : newRange) {
            ITopic t = findTopic(o);
            if (t != null && !topics.contains(t)) {
                ITopic p = t.getParent();
                if (p != null) {
                    if (parent == null) {
                        parent = p;
                        topics.add(t);
                    } else if (parent == p) {
                        topics.add(t);
                    }
                }
            }
        }
        if (topics.isEmpty())
            return;

        Collections.sort(topics, Core.getTopicComparator());

        ITopic t1 = topics.get(0);
        ITopic t2 = topics.get(topics.size() - 1);
        int index1 = t1.getIndex();
        int index2 = t2.getIndex();

        Collection<? extends ITopicRange> existingRanges = getExistingRanges(
                parent, targetRange);
        if (existingRanges == null)
            return;

        for (ITopicRange r : existingRanges) {
            int start = r.getStartIndex();
            int end = r.getEndIndex();
            if (start == index1 && end == index2)
                return;
        }

        ModifyTopicRangeCommand cmd = new ModifyTopicRangeCommand(targetRange,
                t1, t2);
        cmd.setLabel(getModifyRangeLabel(targetRange, topics));
        saveAndRun(cmd, request.getTargetDomain());
        select(cmd.getSources(), request.getTargetViewer());
    }

    private Collection<? extends ITopicRange> getExistingRanges(ITopic parent,
            ITopicRange source) {
        if (source instanceof IBoundary)
            return parent.getBoundaries();
        if (source instanceof ISummary)
            return parent.getSummaries();
        return null;
    }

    private String getModifyRangeLabel(ITopicRange rangeModel,
            List<ITopic> topics) {
        if (rangeModel instanceof IBoundary)
            return CommandMessages.Command_ModifyBoundaryRange;
        if (rangeModel instanceof ISummary)
            return CommandMessages.Command_ModifySummaryRange;
        return CommandMessages.Command_ModifyRange;
    }

    private ITopic findTopic(Object o) {
        if (o instanceof ITopic)
            return (ITopic) o;
        if (o instanceof IPart) {
            Object model = MindMapUtils.getRealModel((IPart) o);
            if (model instanceof ITopic)
                return (ITopic) model;
        }
        if (o instanceof IAdaptable) {
            return (ITopic) ((IAdaptable) o).getAdapter(ITopic.class);
        }
        return null;
    }

    private void modifyStyle(Request request) {
        ModifyStyleCommandBuilder builder = new ModifyStyleCommandBuilder(
                request);
        String commandLabel = (String) request
                .getParameter(MindMapUI.PARAM_COMMAND_LABEL);
        if (commandLabel == null)
            commandLabel = CommandMessages.Command_ModifyStyle;
        builder.setLabel(commandLabel);

        if (!builder.canStart())
            return;

        builder.start();
        List<IPart> targets = request.getTargets();
        for (IPart target : targets) {
            IStyled source = getStyleSource(target);
            if (source != null) {
                builder.modify(source);
            }
        }
        builder.end();
    }

    private IStyled getStyleSource(IPart part) {
        IStyled s = (IStyled) part.getAdapter(IStyled.class);
        if (s != null)
            return s;
        Object m = MindMapUtils.getRealModel(part);
        return m instanceof IStyled ? (IStyled) m : null;
    }

    private void modifyHyperlink(Request request) {
        String hyperlink = (String) request.getParameter(GEF.PARAM_TEXT);
        List<ITopic> topics = MindMapUtils.getTopics(request.getTargets());
        if (topics.isEmpty())
            return;

//        modifyHyperlinkRef(topics, hyperlink, request);

        ModifyTopicHyperlinkCommand cmd = new ModifyTopicHyperlinkCommand(
                topics, hyperlink);
        cmd.setLabel(CommandMessages.Command_ModifyTopicHyperlink);
        saveAndRun(cmd, request.getTargetDomain());
    }

//    private void modifyHyperlinkRef(List<ITopic> topics, String newHref,
//            Request request) {
//        String newTargetId = null;
//        if (newHref != null && newHref.startsWith("xmind:#")) //$NON-NLS-1$
//            newTargetId = newHref.substring(7);
//
//        ModifyTopicLinkCommand command = new ModifyTopicLinkCommand(topics,
//                newTargetId);
//        saveAndRun(command, request.getTargetDomain());
//    }

//    private void modifyHyperlinkRef(List<ITopic> topics, String newHref,
//            Request request) {
////        ITopicHyperlinkRef topicLinkRef = null;
//        for (ITopic topic : topics) {
////            if (topicLinkRef == null) {
////                IWorkbook workbook = topic.getOwnedWorkbook();
////                topicLinkRef = (ITopicHyperlinkRef) workbook
////                        .getAdapter(ITopicHyperlinkRef.class);
////            }
//            modifyTopicLinkRef(topic, newHref, request);
//        }
//    }

//    private void modifyTopicLinkRef(ITopic topic, String newHref,
//            Request request) {
//        String oldHref = topic.getHyperlink();
//
//        String oldTargetId = null;
//        if (oldHref != null && oldHref.startsWith("xmind:#")) //$NON-NLS-1$
//            oldTargetId = oldHref.substring(7);
//
//        String newTargetId = null;
//        if (newHref != null && newHref.startsWith("xmind:#")) //$NON-NLS-1$
//            newTargetId = newHref.substring(7);
////
////        topicLinkRef.modifyTopicLinks(oldTargetId, newTargetId, topic.getId());
//        //TODO:
//
//        ModifyTopicLinkCommand command = new ModifyTopicLinkCommand(topic,
//                newTargetId);
//        saveAndRun(command, request.getTargetDomain());
//    }

    private void modifyText(Request request) {
        if (!request.hasParameter(GEF.PARAM_TEXT))
            return;

        String text = (String) request.getParameter(GEF.PARAM_TEXT);
        if (text == null)
            text = EMPTY;

        List<IPart> targets = request.getTargets();
        if (targets.isEmpty())
            return;

        List<ITitled> ts = new ArrayList<ITitled>(targets.size());
        for (IPart p : targets) {
            Object m = MindMapUtils.getRealModel(p);
            if (m instanceof ITitled && !ts.contains(m)) {
                ts.add((ITitled) m);
            }
        }

        if (ts.isEmpty())
            return;

        PropertyCommandBuilder builder = new PropertyCommandBuilder(request);
        builder.setLabel(getModifyTitleTextLabel(ts));
        builder.start();
        builder.add(new ModifyTitleTextCommand(ts, text), true);
        builder.addSourcesFromRequest(false);
        ITitled titled = ts.get(0);
        if (titled instanceof ITopic) {
            ITopic topic = (ITopic) titled;
            String hyperlink = topic.getHyperlink();
            if (hyperlink == null || hyperlink.isEmpty()) {
                if (HyperlinkUtils.isUrlAddress(text)) {
                    hyperlink = text;
                } else if (HyperlinkUtils.isEmailAddress(text)) {
                    hyperlink = "mailto:" + text; //$NON-NLS-1$
                }
                builder.add(new ModifyTopicHyperlinkCommand(topic, hyperlink),
                        true);
            }
        }
        builder.end();
    }

    private String getModifyTitleTextLabel(List<ITitled> ts) {
        Object t = ts.get(0);
        if (t instanceof ITopic) {
            return CommandMessages.Command_ModifyTopicTitle;
        } else if (t instanceof ISheet) {
            return CommandMessages.Command_ModifySheetTitle;
        } else if (t instanceof IRelationship) {
            return CommandMessages.Command_ModifyRelationshipTitle;
        } else if (t instanceof IBoundary) {
            return CommandMessages.Command_ModifyBoundaryTitle;
        }
        return CommandMessages.Command_ModifyTitle;
    }

}
