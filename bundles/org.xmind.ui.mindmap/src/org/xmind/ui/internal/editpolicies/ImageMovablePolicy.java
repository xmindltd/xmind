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
import java.util.List;

import org.eclipse.draw2d.geometry.Dimension;
import org.xmind.core.Core;
import org.xmind.core.IImage;
import org.xmind.core.ITopic;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.command.Command;
import org.xmind.gef.command.CompoundCommand;
import org.xmind.gef.part.IPart;
import org.xmind.ui.commands.CommandMessages;
import org.xmind.ui.commands.ModifyImageAlignmentCommand;
import org.xmind.ui.commands.ModifyImageSizeCommand;
import org.xmind.ui.commands.ModifyImageSourceCommand;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MindMapUtils;

public class ImageMovablePolicy extends MindMapPolicyBase {

    public boolean understands(String requestType) {
        return super.understands(requestType)
                || GEF.REQ_RESIZE.equals(requestType)
                || GEF.REQ_MOVETO.equals(requestType)
                || GEF.REQ_COPYTO.equals(requestType);
    }

    public void handle(Request request) {
        String type = request.getType();
        if (GEF.REQ_RESIZE.equals(type)) {
            resizeImage(request);
        } else if (GEF.REQ_MOVETO.equals(type)) {
            moveImage(request);
        } else if (GEF.REQ_COPYTO.equals(type)) {
            copyImage(request);
        }
    }

    private List<IImage> getImages(Request request) {
        List<IPart> targets = request.getTargets();
        ArrayList<IImage> list = new ArrayList<IImage>(targets.size());
        for (IPart part : targets) {
            Object o = MindMapUtils.getRealModel(part);
            if (o instanceof IImage) {
                list.add((IImage) o);
            }
        }
        return list;
    }

    private void resizeImage(Request request) {
        List<IImage> images = getImages(request);
        if (images.isEmpty())
            return;

        Dimension newSize = (Dimension) request.getParameter(GEF.PARAM_SIZE);
        int width = newSize == null ? IImage.UNSPECIFIED : newSize.width;
        int height = newSize == null ? IImage.UNSPECIFIED : newSize.height;
        ModifyImageSizeCommand command = new ModifyImageSizeCommand(images,
                width, height);
        command.setLabel(CommandMessages.Command_ResizeImage);
        saveAndRun(command, request.getTargetDomain());
    }

    private void moveImage(Request request) {
        copyImage(request, true);
    }

    private void copyImage(Request request) {
        copyImage(request, false);
    }

    private void copyImage(Request request, boolean deleteSource) {
        List<IImage> images = getImages(request);
        if (images.isEmpty())
            return;

        IImage sourceImage = images.get(0);

        Object param = request.getParameter(GEF.PARAM_PARENT);
        if (param == null || !(param instanceof ITopicPart))
            return;

        ITopicPart targetParent = (ITopicPart) param;

        String alignParamName = MindMapUI.PARAM_PROPERTY_PREFIX
                + Core.ImageAlignment;
        String alignment = (String) request.getParameter(alignParamName);
        List<Command> cmds = new ArrayList<Command>(images.size() * 2);
        ITopic targetTopic = targetParent.getTopic();
        if (sourceImage.getParent() == targetTopic) {
            if (request.hasParameter(alignParamName)) {
                cmds
                        .add(new ModifyImageAlignmentCommand(sourceImage,
                                alignment));
            }
        } else {
            IImage targetImage = targetTopic.getImage();
            cmds.add(new ModifyImageSourceCommand(targetImage, sourceImage
                    .getSource()));
            cmds.add(new ModifyImageSizeCommand(targetImage, sourceImage
                    .getWidth(), sourceImage.getHeight()));
            if (request.hasParameter(alignParamName)) {
                cmds
                        .add(new ModifyImageAlignmentCommand(targetImage,
                                alignment));
            } else {
                cmds.add(new ModifyImageAlignmentCommand(targetImage,
                        sourceImage.getAlignment()));
            }

            if (deleteSource) {
                ModifyImageSourceCommand m1 = new ModifyImageSourceCommand(
                        sourceImage, null);
                m1.setSourceCollectable(false);
                cmds.add(m1);

                ModifyImageSizeCommand m2 = new ModifyImageSizeCommand(
                        sourceImage, IImage.UNSPECIFIED, IImage.UNSPECIFIED);
                m2.setSourceCollectable(false);
                cmds.add(m2);

                ModifyImageAlignmentCommand m3 = new ModifyImageAlignmentCommand(
                        sourceImage, null);
                m3.setSourceCollectable(false);
                cmds.add(m3);
            }
        }
        if (cmds.isEmpty())
            return;

        CompoundCommand command = new CompoundCommand(cmds);
        command.setLabel(deleteSource ? CommandMessages.Command_MoveImage
                : CommandMessages.Command_CopyImage);
        saveAndRun(command, request.getTargetDomain());
        select(command.getSources(), request.getTargetViewer());
    }

}