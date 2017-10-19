/* ******************************************************************************
 * Copyright (c) 2006-2012 XMind Ltd. and others.
 * 
 * This file is a part of XMind 3. XMind releases 3 and
 * above are dual-licensed under the Eclipse Public License (EPL),
 * which is available at http://www.eclipse.org/legal/epl-v10.html
 * and the GNU Lesser General Public License (LGPL), 
 * which is available at http://www.gnu.org/licenses/lgpl.html
 * See http://www.xmind.net/license.html for details.
 * 
 * Contributors:
 *     XMind Ltd. - initial API and implementation
 *******************************************************************************/
package org.xmind.ui.internal.editpolicies;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.draw2d.geometry.Point;
import org.xmind.core.Core;
import org.xmind.core.IPositioned;
import org.xmind.core.ITitled;
import org.xmind.core.ITopic;
import org.xmind.gef.IViewer;
import org.xmind.gef.Request;
import org.xmind.gef.command.ICommandStack;
import org.xmind.ui.commands.CommandBuilder;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyFoldedCommand;
import org.xmind.ui.commands.ModifyLabelCommand;
import org.xmind.ui.commands.ModifyPositionCommand;
import org.xmind.ui.commands.ModifyTitleTextCommand;
import org.xmind.ui.commands.ModifyTopicStructureCommand;
import org.xmind.ui.commands.ModifyTopicTitleWidthCommand;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class PropertyCommandBuilder extends CommandBuilder {

    private static final List<Object> EMPTY_SOURCES = Collections.emptyList();

    private Request request;

    private List<Object> sources = null;

    public PropertyCommandBuilder(Request request) {
        this(request.getTargetViewer(), request.getTargetCommandStack(),
                request);
    }

    public PropertyCommandBuilder(IViewer viewer, CommandBuilder delegate,
            Request request) {
        super(viewer, delegate);
        this.request = request;
    }

    public PropertyCommandBuilder(IViewer viewer, ICommandStack commandStack,
            Request request) {
        super(viewer, commandStack);
        this.request = request;
    }

    public Request getRequest() {
        return request;
    }

    public void addSourcesFromRequest(boolean collectable) {
        addSources(MindMapUtils.getRealModels(request.getTargets()).toArray(),
                collectable);
    }

    public void addSources(Object[] sources, boolean collectable) {
        for (Object source : sources) {
            addSource(source, collectable);
        }
    }

    public void addSource(Object source, boolean collectable) {
        boolean commandAdded = false;
        for (Entry<String, Object> param : request.getParameters().entrySet()) {
            String paramName = param.getKey();
            if (paramName.startsWith(MindMapUI.PARAM_PROPERTY_PREFIX)) {
                String propName = paramName
                        .substring(MindMapUI.PARAM_PROPERTY_PREFIX.length());
                commandAdded |= addPropertyCommand(source, propName, param
                        .getValue(), collectable);
            }
        }
        if (commandAdded) {
            addSource(source);
        }
    }

    private void addSource(Object source) {
        if (sources == null)
            sources = new ArrayList<Object>();
        sources.add(source);
    }

    public boolean isSourceAdded(Object source) {
        return sources != null && sources.contains(source);
    }

    public List<Object> getAddedSources() {
        return sources == null ? EMPTY_SOURCES : sources;
    }

    private boolean addPropertyCommand(Object source, String propName,
            Object value, boolean sourceCollectable) {
        if (propName == null || "".equals(propName)) //$NON-NLS-1$
            return false;

        if (Core.TitleText.equals(propName)) {
            return modifyTitle(source, value, sourceCollectable);
        } else if (Core.TopicFolded.equals(propName)) {
            return modifyTopicFolded(source, value, sourceCollectable);
        } else if (Core.Labels.equals(propName)) {
            return modifyTopicLabels(source, value, sourceCollectable);
        } else if (Core.TitleWidth.equals(propName)) {
            return modifyTitleWidth(source, value, sourceCollectable);
        } else if (Core.Position.equals(propName)) {
            return modifyPosition(source, value, sourceCollectable);
        } else if (Core.StructureClass.equals(propName)) {
            return modifyStructure(source, value, sourceCollectable);
        } else {
            // TODO add more property handlers when necessary
        }
        return false;
    }

    /**
     * @param source
     * @param value
     * @param sourceCollectable
     * @return
     */
    private boolean modifyStructure(Object source, Object value,
            boolean sourceCollectable) {
        if (source instanceof ITopic) {
            if (value == null || value instanceof String) {
                String structureClass = (String) value;
                add(new ModifyTopicStructureCommand((ITopic) source,
                        structureClass), sourceCollectable);
                return true;
            }
        }
        return false;
    }

    private boolean modifyPosition(Object source, Object value,
            boolean sourceCollectable) {
        if (source instanceof IPositioned) {
            if (value == null || value instanceof Point) {
                Point p = (Point) value;
                add(new ModifyPositionCommand((IPositioned) source,
                        MindMapUtils.toModelPosition(p)), sourceCollectable);
                return true;
            }
        }
        return false;
    }

    private boolean modifyTitleWidth(Object source, Object value,
            boolean sourceCollectable) {
        if (source instanceof ITopic) {
            if (value == null || value instanceof Integer) {
                int width = value == null ? ITopic.UNSPECIFIED
                        : ((Integer) value).intValue();
                ModifyTopicTitleWidthCommand command = new ModifyTopicTitleWidthCommand(
                        (ITopic) source, width);
                command.setLabel(CommandMessages.Command_ModifyWidth);
                add(command, sourceCollectable);

                return true;
            }
        }
        return false;
    }

    private boolean modifyTopicLabels(Object source, Object value,
            boolean sourceCollectable) {
        if (source instanceof ITopic) {
            if (value == null || value instanceof String
                    || value instanceof String[] || value instanceof Collection) {
                ArrayList<String> labels = new ArrayList<String>();
                if (value != null) {
                    if (value instanceof String) {
                        labels.addAll(MindMapUtils.getLabels((String) value));
                    } else if (value instanceof String[]) {
                        labels.addAll(Arrays.asList((String[]) value));
                    } else if (value instanceof Collection) {
                        for (Object o : (Collection) value) {
                            if (o instanceof String) {
                                labels.add((String) o);
                            }
                        }
                    }
                }
                add(new ModifyLabelCommand((ITopic) source, labels),
                        sourceCollectable);
                return true;
            }
        }
        return false;
    }

    private boolean modifyTopicFolded(Object source, Object value,
            boolean sourceCollectable) {
        if (source instanceof ITopic) {
            if (value instanceof Boolean) {
                boolean newFolded = ((Boolean) value).booleanValue();
                add(new ModifyFoldedCommand((ITopic) source, newFolded),
                        sourceCollectable);
                return true;
            }
        }
        return false;
    }

    private boolean modifyTitle(Object source, Object value,
            boolean sourceCollectable) {
        if (source instanceof ITitled) {
            if (value == null || value instanceof String) {
                add(
                        new ModifyTitleTextCommand((ITitled) source,
                                (String) value), sourceCollectable);
                return true;
            }
        }
        return false;
    }

}