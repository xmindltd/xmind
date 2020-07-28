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
package org.xmind.ui.internal.mindmap;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.Core;
import org.xmind.core.ITopic;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.core.marker.IMarker;
import org.xmind.core.marker.IMarkerRef;
import org.xmind.gef.GEF;
import org.xmind.gef.draw2d.SizeableImageFigure;
import org.xmind.gef.draw2d.graphics.GraphicsUtils;
import org.xmind.gef.graphicalpolicy.IStyleSelector;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.gef.service.IFeedback;
import org.xmind.ui.internal.svgsupport.SVGImageData;
import org.xmind.ui.internal.svgsupport.SVGImageFigure;
import org.xmind.ui.internal.svgsupport.SVGReference;
import org.xmind.ui.mindmap.IMarkerPart;
import org.xmind.ui.mindmap.ISelectionFeedbackHelper;
import org.xmind.ui.mindmap.ITopicPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.resources.ImageReference;
import org.xmind.ui.style.StyleUtils;
import org.xmind.ui.style.TextStyleData;
import org.xmind.ui.util.MarkerImageDescriptor;

public class MarkerPart extends MindMapPartBase implements IMarkerPart {

    private ImageReference imageRef = null;

    private SVGReference svgRef = null;

    private Dimension preferredSize = null;

    private LocalResourceManager resourceManager;

    public MarkerPart() {
        setDecorator(MarkerDecorator.getInstance());

    }

    @SuppressWarnings("unchecked")
    @Override
    public Object getAdapter(Class adapter) {
        if (adapter.isAssignableFrom(IMarkerRef.class))
            return getMarkerRef();
        if (adapter.isAssignableFrom(IMarker.class))
            return getMarker();
        if (adapter.isAssignableFrom(ITopic.class))
            return getTopic();
        if (adapter == Image.class)
            return getImage();
        if (adapter == SVGImageData.class)
            return getSVGData();
        return super.getAdapter(adapter);
    }

    protected IFigure createFigure() {
        if (svgRef != null) {
            SVGImageFigure figure = new SVGImageFigure();
            figure.setManager(resourceManager);
            return figure;
        } else
            return new SizeableImageFigure();
    }

    public IMarkerRef getMarkerRef() {
        return (IMarkerRef) super.getRealModel();
    }

    protected void register() {
        registerModel(getMarkerRef());
        super.register();
    }

    protected void unregister() {
        super.unregister();
        unregisterModel(getMarkerRef());
    }

    public IMarker getMarker() {
        IMarkerRef markerRef = getMarkerRef();
        return markerRef == null ? null : markerRef.getMarker();
    }

    public ITopic getTopic() {
        return getMarkerRef().getParent();
    }

    public ITopicPart getTopicPart() {
        if (getParent() instanceof ITopicPart)
            return (ITopicPart) getParent();
        return null;
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof TopicPart) {
            ((TopicPart) getParent()).removeMarker(this);
        }
        super.setParent(parent);
        updateMarker();
        if (getParent() instanceof TopicPart) {
            ((TopicPart) getParent()).addMarker(this);
        }
    }

    public Image getImage() {
        if (imageRef != null && !imageRef.isDisposed())
            return imageRef.getImage();
        return null;
    }

    public SVGImageData getSVGData() {
        if (svgRef != null)
            return svgRef.getSVGData();
        return null;
    }

    @Override
    protected void onActivated() {
        this.resourceManager = new LocalResourceManager(
                JFaceResources.getResources());
        super.onActivated();
    }

    protected void onDeactivated() {
        if (imageRef != null) {
            imageRef.dispose();
            imageRef = null;
        }
        if (svgRef != null) {
            svgRef = null;
            this.resourceManager.dispose();
        }
        super.onDeactivated();
    }

    @Override
    public void setModel(Object model) {
        // step 1, new marker part
        // step 2, set model
        super.setModel(model);
        // step 3, ensure image type 
        IMarkerRef markerRef = (IMarkerRef) ((ViewerModel) model)
                .getRealModel();
        IMarker marker = markerRef.getMarker();
        String svgPath = marker != null ? marker.getSVGPath() : null;
        if (svgPath != null && !"".equals(svgPath)) { //$NON-NLS-1$
            this.svgRef = createSVGReference();
        } else {
            this.imageRef = createImageReference();
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.xmind.gef.part.GraphicalEditPart#containsPoint(org.eclipse.draw2d
     * .geometry.Point)
     */
    @Override
    public boolean containsPoint(Point position) {
        return super.containsPoint(position);
    }

    @Override
    protected void updateView() {
        updateToolTip();
        super.updateView();
    }

    public IFigure findTooltipAt(Point position) {
        if (containsPoint(position)) {
            IFigure toolTip = getFigure().getToolTip();
            if (toolTip != null)
                return toolTip;
            return new Label(getMarkerRef().getDescription());
        }
        return null;
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_SELECTABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_DELETABLE,
                MindMapUI.POLICY_DELETABLE);
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                MindMapUI.POLICY_MARKER_MOVABLE);
    }

    protected IFeedback createFeedback() {
        return new SimpleSelectionFeedback(this);
    }

    protected ISelectionFeedbackHelper createSelectionFeedbackHelper() {
        return new SelectionFeedbackHelper();
    }

    @Override
    protected void installModelListeners() {
        super.installModelListeners();
    }

    protected void registerCoreEvents(Object source,
            ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.Style);
        ITopic topic = getMarkerRef().getParent();
        register.setNextSourceFrom(topic);
        register.register(Core.Style);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.Style.equals(type)) {
            updateMarker();
        }
    }

    private void updateMarker() {
        if (svgRef == null) {
            if (imageRef != null) {
                imageRef.dispose();
            }
            imageRef = createImageReference();
        } else {
            // svgRef don't have to create a new reference, 
            // but the preferredSize must be calculated again 
            preferredSize = calculateSVGPreferredSize();
        }

        update();
    }

    private ImageReference createImageReference() {
        preferredSize = calculateImagePreferredSize();
        int hintSize = preferredSize == null ? -1 : preferredSize.width;
        return new ImageReference(MarkerImageDescriptor.createFromMarkerRef(
                getMarkerRef(), hintSize, hintSize), false);
    }

    private SVGReference createSVGReference() {
        preferredSize = calculateSVGPreferredSize();

        String resourcePath = MarkerImageDescriptor.RESOURCE_URL_PREFIX
                + getMarker().getSVGPath();
        SVGReference ref = new SVGReference(resourcePath);
        if (this.resourceManager == null)
            this.resourceManager = new LocalResourceManager(
                    JFaceResources.getResources());
        return ref;
    }

    private Dimension calculateSVGPreferredSize() {
        ITopicPart tp = getTopicPart();
        if (tp == null)
            return null;
        IStyleSelector ss = (IStyleSelector) tp
                .getAdapter(IStyleSelector.class);
        TextStyleData data = StyleUtils.getTextStyleData(tp, ss, null);
        int leading = GraphicsUtils.getAdvanced()
                .getFontMetrics(JFaceResources.getDefaultFont()).getLeading();

        return new Dimension(data.height + leading, data.height + leading);
    }

    private Dimension calculateImagePreferredSize() {
        ITopicPart tp = getTopicPart();
        if (tp == null)
            return null;
        IStyleSelector ss = (IStyleSelector) tp
                .getAdapter(IStyleSelector.class);
        TextStyleData data = StyleUtils.getTextStyleData(tp, ss, null);
        int fontSize = data.height;

        if (fontSize < 13) {
            return new Dimension(16, 16);
        } else if (fontSize >= 13 && fontSize < 25) {
            return new Dimension(24, 24);
        } else {
            return new Dimension(32, 32);
        }

    }

    public Dimension getPreferredSize() {
        return preferredSize;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

}