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
package org.xmind.ui.internal.mindmap;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.Core;
import org.xmind.core.IImage;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.util.HyperlinkUtils;
import org.xmind.gef.GEF;
import org.xmind.gef.Request;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.service.IFeedback;
import org.xmind.gef.util.GEFUtils;
import org.xmind.ui.internal.AttachmentImageDescriptor;
import org.xmind.ui.mindmap.IImagePart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ImageReference;

public class ImagePart extends MindMapPartBase implements IImagePart {

    private static final String FILE_PROTOCOL = "file"; //$NON-NLS-1$

    private ImageDescriptor imageDescriptor = null;

    private ImageReference imageRef = null;

    private String imageURL = null;

    private Runnable imageUpdater = null;

    public ImagePart() {
        setDecorator(ImageDecorator.getInstance());
    }

    protected IFigure createFigure() {
        return new SizeableImageFigure();
    }

    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }

    protected synchronized void setImageDescriptor(
            ImageDescriptor imageDescriptor) {
        if (imageDescriptor == this.imageDescriptor || (imageDescriptor != null
                && imageDescriptor.equals(this.imageDescriptor)))
            return;
        ImageReference oldImageRef = this.imageRef;
        this.imageDescriptor = imageDescriptor;
        if (oldImageRef != null) {
            oldImageRef.dispose();
        }
        this.imageRef = this.imageDescriptor == null ? null
                : new ImageReference(this.imageDescriptor, true);
    }

    public Image getImage() {
        if (imageRef != null && !imageRef.isDisposed())
            return imageRef.getImage();
        return null;
    }

    public SizeableImageFigure getImageFigure() {
        return (SizeableImageFigure) super.getFigure();
    }

    @SuppressWarnings("unchecked")
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        if (adapter == ITopicPart.class)
            return getTopicPart();
        if (adapter == IImage.class)
            return getImageModel();
        if (adapter == Image.class)
            return getImage();
        return super.getAdapter(adapter);
    }

    public void setModel(Object model) {
        super.setModel(model);
        updateImageDescriptor();
    }

    protected void onDeactivated() {
        getImageFigure().setImage(null);
        setImageURL(null);
        if (imageRef != null) {
            imageRef.dispose();
            imageRef = null;
        }
        super.onDeactivated();
    }

    public IImage getImageModel() {
        return (IImage) super.getRealModel();
    }

    protected void register() {
        registerModel(getImageModel());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getImageModel());
    }

    public ITopic getTopic() {
        return getImageModel().getParent();
    }

    public ITopicPart getTopicPart() {
        if (getParent() instanceof ITopicPart)
            return (ITopicPart) getParent();
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof TopicPart) {
            TopicPart topicPart = (TopicPart) getParent();
            if (topicPart.getImagePart() == this)
                topicPart.setImagePart(null);
        }
        super.setParent(parent);
        if (getParent() instanceof TopicPart) {
            TopicPart topicPart = (TopicPart) getParent();
            topicPart.setImagePart(this);
        }
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.ImageAlignment);
        register.register(Core.ImageHeight);
        register.register(Core.ImageSource);
        register.register(Core.ImageWidth);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.ImageAlignment.equals(type)) {
            getFigure().revalidate();
        } else if (Core.ImageHeight.equals(type)
                || Core.ImageWidth.equals(type)) {
            update();
        } else if (Core.ImageSource.equals(type)) {
            updateImageDescriptor();
            update();
        } else {
            super.handleCoreEvent(event);
        }
    }

    private void updateImageDescriptor() {
        IImage imageModel = getImageModel();
        String source = imageModel.getSource();
        if (source != null) {
            if (HyperlinkUtils.isAttachmentURL(source)) {
                setImageURL(null);
                String path = HyperlinkUtils.toAttachmentPath(source);
                setImageDescriptor(
                        AttachmentImageDescriptor.createFromEntryPath(
                                imageModel.getOwnedWorkbook(), path));
                setToolTip(null);
            } else {
                URL url = checkFileURL(source);
                if (url != null) {
                    setImageURL(null);
                    setImageDescriptor(ImageDescriptor.createFromURL(url));
                    setToolTip(url.getPath());
                } else {
                    setImageURL(source);
                }
            }
        } else {
            setImageURL(null);
            setImageDescriptor(null);
            setToolTip(null);
        }
    }

    private URL checkFileURL(String source) {
        try {
            URL url = new URL(source);
            if (FILE_PROTOCOL.equalsIgnoreCase(url.getProtocol()))
                return url;
        } catch (MalformedURLException e) {
        }
        return null;
    }

    private void setImageURL(String newURL) {
        if (newURL == imageURL || (newURL != null && newURL.equals(imageURL)))
            return;

        String oldImageURL = this.imageURL;

        this.imageURL = newURL;

        ImageDownloader.getInstance().unregister(oldImageURL,
                getImageUpdater());
        if (imageURL != null) {
            ImageDownloader.getInstance().register(imageURL, getImageUpdater());
            setImageDescriptor(
                    ImageDownloader.getInstance().getImage(imageURL));
            IStatus status = ImageDownloader.getInstance().getStatus(imageURL);
            if (status.getSeverity() == IStatus.OK) {
                setToolTip(imageURL);
            } else {
                setToolTip(status.getMessage());
            }
        }
    }

    private Runnable getImageUpdater() {
        if (imageUpdater == null) {
            imageUpdater = new Runnable() {
                public void run() {
                    if (imageURL == null)
                        return;

                    setImageDescriptor(
                            ImageDownloader.getInstance().getImage(imageURL));
                    IStatus status = ImageDownloader.getInstance()
                            .getStatus(imageURL);
                    if (status.getSeverity() == IStatus.OK) {
                        setToolTip(imageURL);
                    } else {
                        setToolTip(status.getMessage());
                    }
                    update();
                }
            };
        }
        return imageUpdater;
    }

    private void setToolTip(String message) {
        getFigure().setToolTip(message == null ? null : new Label(message));
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_DELETABLE,
                MindMapUI.POLICY_DELETABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                MindMapUI.POLICY_IMAGE_MOVABLE);
    }

    protected IFeedback createFeedback() {
        return new ImageFeedback(this);
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new ImageSelectionHelper();
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.gef.part.GraphicalEditPart#findAt(org.eclipse.draw2d.geometry
     * .Point)
     */
//    @Override
//    public IPart findAt(Point position) {
//        if (!getStatus().isSelected()) {
//            ITopicPart topicPart = getTopicPart();
//            if (topicPart != null) {
//                if (!topicPart.getStatus().isSelected())
//                    return null;
//            }
//        }
//        return super.findAt(position);
//    }

    public boolean containsPoint(Point position) {
        return super.containsPoint(position) || (getSelectionOrientation(
                position) != PositionConstants.NONE);
    }

    public Cursor getCursor(Point pos) {
        int orientation = getSelectionOrientation(pos);
        if (orientation != PositionConstants.NONE) {
            return GEFUtils.getPositionCursor(orientation);
        }
        return super.getCursor(pos);
    }

    private int getSelectionOrientation(Point point) {
        if (getStatus().isPreSelected() || getStatus().isSelected()) {
            return ((ImageFeedback) getFeedback()).getOrientation(point);
        }
        return PositionConstants.NONE;
    }

    @Override
    public void handleRequest(Request request, String role) {
        String type = request.getType();
        if (GEF.REQ_PASTE.equals(type)) {
            request.setPrimaryTarget(getTopicPart());
            ArrayList<IPart> parts = new ArrayList<IPart>();
            parts.add(getTopicPart());
            request.setTargets(parts);
        }
        super.handleRequest(request, role);
    }

}
