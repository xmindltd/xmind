package org.xmind.ui.internal.dnd;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.dnd.TransferData;
import org.xmind.core.IBoundary;
import org.xmind.core.IImage;
import org.xmind.core.IRelationship;
import org.xmind.core.ISheet;
import org.xmind.core.ITopic;
import org.xmind.core.ITopicExtension;
import org.xmind.core.ITopicExtensionElement;
import org.xmind.core.ITopicRange;
import org.xmind.core.IWorkbook;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerGroup;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.GEF;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.dnd.IDndClient;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.AddBoundaryCommand;
import org.xmind.ui.commands.AddMarkerCommand;
import org.xmind.ui.commands.AddRelationshipCommand;
import org.xmind.ui.commands.AddTopicCommand;
import org.xmind.ui.commands.DeleteMarkerCommand;
import org.xmind.ui.commands.ModifyFoldedCommand;
import org.xmind.ui.commands.ModifyImageAlignmentCommand;
import org.xmind.ui.commands.ModifyImageSizeCommand;
import org.xmind.ui.commands.ModifyImageSourceCommand;
import org.xmind.ui.commands.ModifyRangeCommand;
import org.xmind.ui.commands.ModifyRightNumberOfUnbalancedStructureCommand;
import org.xmind.ui.commands.ModifyTopicHyperlinkCommand;
import org.xmind.ui.internal.branch.UnbalancedData;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.util.MindMapUtils;

public abstract class MindMapDNDClientBase implements IDndClient {

    /**
     * @deprecated
     */
    public final Object[] toViewerElements(Object transferData, IViewer viewer,
            Object target) {
        return null;
    }

    public Command makeDNDCommand(Object transferredData, Request request) {
        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return null;

        ISheet sheet = (ISheet) viewer.getAdapter(ISheet.class);
        if (sheet == null)
            return null;

        IWorkbook workbook = sheet.getOwnedWorkbook();
        IPart parent = (IPart) request.getParameter(GEF.PARAM_PARENT);
        if (parent != null || request.getTargets().size() == 1) {
            boolean dropInParent = request.getTargets().contains(parent);
            if (parent == null) {
                parent = request.getPrimaryTarget();
            }
            ITopic targetParent = findTargetParentTopic(viewer, parent);
            return makeDNDCommand(transferredData, request, workbook,
                    targetParent, dropInParent,
                    request.getParameter(GEF.PARAM_PARENT) == null);
        } else if (!request.getTargets().isEmpty()) {
            List<Command> commands = new ArrayList<Command>();
            for (IPart target : request.getTargets()) {
                ITopic targetParent = findTargetParentTopic(viewer, target);
                if (targetParent != null) {
                    commands.add(makeDNDCommand(transferredData, request,
                            workbook, targetParent, false, false));
                }
            }
            return new CompoundCommand(commands);
        } else {
            return null;
        }
    }

    private ITopic findTargetParentTopic(IViewer viewer, IPart parent) {
        Object targetParentModel = MindMapUtils.getRealModel(parent);
        ITopic targetParent;
        if (targetParentModel == null
                || !(targetParentModel instanceof ITopic)) {
            targetParent = (ITopic) viewer.getAdapter(ITopic.class);
        } else {
            targetParent = (ITopic) targetParentModel;
        }
        return targetParent;
    }

    protected Command makeDNDCommand(Object transferData, Request request,
            IWorkbook workbook, ITopic targetParent, boolean dropInParent,
            boolean floating) {
        Object[] elements = toViewerElements(transferData, request, workbook,
                targetParent, dropInParent);
        if (elements == null || elements.length == 0)
            return null;

        List<Command> commands = new ArrayList<Command>();
        makeDNDCommands(request, workbook, targetParent, elements, commands,
                floating, dropInParent);

        return new CompoundCommand(commands);
    }

    protected void makeDNDCommands(Request request, IWorkbook workbook,
            ITopic targetParent, Object[] elements, List<Command> commands,
            boolean floating, boolean dropInParent) {
        ISheet sheet = (ISheet) request.getTargetViewer()
                .getAdapter(ISheet.class);
        int index = request.getIntParameter(GEF.PARAM_INDEX, -1);
        int sourceIndex = index;
        Point position = (Point) request.getParameter(GEF.PARAM_POSITION);

        int countForUnbalacedStructure = 0;

        preAdded(targetParent, commands);

        for (Object element : elements) {
            if (element instanceof Command) {
                commands.add((Command) element);
            } else if (element instanceof ITopic) {
                if (targetParent != null) {
                    ITopic topic = (ITopic) element;
                    if (floating && position != null) {
                        topic.setPosition(position.x, position.y);
                        commands.add(new AddTopicCommand(topic, targetParent,
                                -1, ITopic.DETACHED));
                    } else {
                        countForUnbalacedStructure = modifyRightNumeberForUnbalancedStructure(
                                request, targetParent,
                                countForUnbalacedStructure);

                        topic.setPosition(null);
                        commands.add(new AddTopicCommand(topic, targetParent,
                                index, ITopic.ATTACHED));
                        if (index >= 0)
                            index++;
                    }
                }
            } else if (element instanceof IRelationship) {
                if (sheet != null) {
                    IRelationship relationship = (IRelationship) element;
                    commands.add(
                            new AddRelationshipCommand(relationship, sheet));
                }
            } else if (element instanceof IBoundary) {
                if (targetParent != null) {
                    IBoundary boundary = (IBoundary) element;
                    commands.add(
                            new AddBoundaryCommand(boundary, targetParent));
                }
            } else if (element instanceof IMarkerRef
                    || element instanceof IMarker) {
                if (targetParent != null) {
                    IMarker marker = (element instanceof IMarker)
                            ? (IMarker) element
                            : ((IMarkerRef) element).getMarker();
                    if (marker != null) {
                        String markerId = (element instanceof IMarker)
                                ? ((IMarker) element).getId()
                                : ((IMarkerRef) element).getMarkerId();
                        if (floating && position != null) {
                            ITopic topic = workbook.createTopic();
                            topic.setPosition(position.x, position.y);
                            commands.add(new AddTopicCommand(topic,
                                    targetParent, -1, ITopic.DETACHED));
                            commands.add(new AddMarkerCommand(topic, markerId));
                            return;
                        } else if (!dropInParent) {
                            ITopic topic = workbook.createTopic();
                            commands.add(new AddTopicCommand(topic,
                                    targetParent, index, ITopic.ATTACHED));
                            commands.add(new AddMarkerCommand(topic, markerId));
                            return;
                        }
                        IMarkerGroup group = marker.getParent();
                        if (group.isSingleton()) {
                            for (IMarker m : group.getMarkers()) {
                                if (targetParent.hasMarker(m.getId())) {
                                    commands.add(new DeleteMarkerCommand(
                                            targetParent, m.getId()));
                                }
                            }
                        }
                        commands.add(
                                new AddMarkerCommand(targetParent, markerId));
                    }
                }
            } else if (element instanceof IImage) {
                IImage image = (IImage) element;
                if (targetParent != null) {
                    commands.add(new ModifyImageSourceCommand(targetParent,
                            image.getSource()));
                    commands.add(new ModifyImageSizeCommand(targetParent,
                            image.getWidth(), image.getHeight()));
                    commands.add(new ModifyImageAlignmentCommand(targetParent,
                            image.getAlignment()));
                }
            } else if (element instanceof URI) {
                URI uri = (URI) element;
                if (targetParent != null) {
                    commands.add(new ModifyTopicHyperlinkCommand(targetParent,
                            uri.toString()));
                }
            }
        }

        if (countForUnbalacedStructure != 0) {
            IViewer viewer = request.getTargetViewer();
            ITopic centralTopic = (ITopic) viewer.getAdapter(ITopic.class);
            ITopicExtension extension = centralTopic.createExtension(
                    UnbalancedData.EXTENTION_UNBALANCEDSTRUCTURE);
            ITopicExtensionElement element = extension.getContent()
                    .getCreatedChild(
                            UnbalancedData.EXTENTIONELEMENT_RIGHTNUMBER);

            String preDndRightNum = element.getTextContent();
            if (preDndRightNum == null)
                preDndRightNum = String.valueOf(0);
            int postDndRightNum = Integer.valueOf(preDndRightNum);
            commands.add(new ModifyRightNumberOfUnbalancedStructureCommand(
                    centralTopic, preDndRightNum,
                    postDndRightNum + countForUnbalacedStructure));
        }

        postAdded(elements, targetParent, sourceIndex, floating, commands);
    }

    private void preAdded(ITopic targetParent, List<Command> commands) {
        ensureParentUnfolded(targetParent, commands);
    }

    private void ensureParentUnfolded(ITopic targetParent,
            List<Command> commands) {
        if (targetParent.isFolded()) {
            commands.add(new ModifyFoldedCommand(targetParent, false));
        }
    }

    private void postAdded(Object[] elements, ITopic targetParent,
            int sourceIndex, boolean floating, List<Command> commands) {

        boolean containsTopic = false;
        for (Object object : elements) {
            if (object instanceof ITopic) {
                containsTopic = true;
                break;
            }
        }

        if (containsTopic && !floating) {
            if (sourceIndex >= 0) {
                modifyRanges(targetParent, sourceIndex, commands);
            }
        }
    }

    private void modifyRanges(ITopic targetParent, int sourceIndex,
            List<Command> commands) {
        modifyRanges(targetParent.getBoundaries(), sourceIndex, commands);
        modifyRanges(targetParent.getSummaries(), sourceIndex, commands);
    }

    private void modifyRanges(Collection<? extends ITopicRange> ranges,
            int sourceIndex, List<Command> commands) {
        for (ITopicRange range : ranges) {
            int startIndex = range.getStartIndex();
            int endIndex = range.getEndIndex();
            if (startIndex >= 0 && endIndex >= 0) {
                if (startIndex >= sourceIndex) {
                    commands.add(new ModifyRangeCommand(range, startIndex + 1,
                            true));
                }
                if (endIndex >= sourceIndex) {
                    commands.add(
                            new ModifyRangeCommand(range, endIndex + 1, false));
                }
            }
        }
    }

    private int modifyRightNumeberForUnbalancedStructure(Request request,
            ITopic targetParent, int count) {
        IViewer viewer = request.getTargetViewer();
        if (viewer == null)
            return count;

        ITopic centralTopic = (ITopic) viewer.getAdapter(ITopic.class);
        if (centralTopic == targetParent) {
            String centralTopicStructure = centralTopic.getStructureClass();
            boolean isUnbalancedStructure = centralTopicStructure == null
                    || UnbalancedData.STRUCTUREID_UNBALANCED
                            .equalsIgnoreCase(centralTopicStructure);

            if (isUnbalancedStructure) {
                ITopicExtension extension = centralTopic.createExtension(
                        UnbalancedData.EXTENTION_UNBALANCEDSTRUCTURE);
                ITopicExtensionElement element = extension.getContent()
                        .getCreatedChild(
                                UnbalancedData.EXTENTIONELEMENT_RIGHTNUMBER);

                String preDndRightNum = element.getTextContent();
                if (preDndRightNum == null)
                    preDndRightNum = String.valueOf(0);
                int postDndRightNum = Integer.valueOf(preDndRightNum);

                ITopicPart parentPart = (ITopicPart) request
                        .getParameter(GEF.PARAM_PARENT);
                if (parentPart != null || postDndRightNum <= 2) {
                    if (parentPart != null) {
                        Rectangle bounds = parentPart.getFigure().getBounds();
                        if (bounds.getCenter()
                                .getDifference((Point) request.getParameter(
                                        GEF.PARAM_POSITION_ABSOLUTE)).width < 0) {
                            count++;
                        }
                    } else if (postDndRightNum <= 2) {
                        count++;
                    }

                }
            }
        }
        return count;
    }

    protected Command createModifyImageCommand(ITopic target, String source,
            int width, int height, String alignment) {
        List<Command> commands = new ArrayList<Command>(3);
        commands.add(new ModifyImageSourceCommand(target, source));
        commands.add(new ModifyImageSizeCommand(target, width, height));
        commands.add(new ModifyImageAlignmentCommand(target, alignment));
        return new CompoundCommand(commands);
    }

    /**
     * Subclasses may override this method.
     * 
     * @param transferData
     * @param request
     * @param workbook
     * @param targetParent
     * @param dropInParent
     *            TODO
     * @return
     */
    protected abstract Object[] toViewerElements(Object transferData,
            Request request, IWorkbook workbook, ITopic targetParent,
            boolean dropInParent);

    public boolean canCopy(TransferData transferData, IViewer viewer,
            Point location, IPart target) {
        return true;
    }

    public boolean canLink(TransferData data, IViewer viewer, Point location,
            IPart target) {
        return false;
    }

    public boolean canMove(TransferData data, IViewer viewer, Point location,
            IPart target) {
        return true;
    }

}
