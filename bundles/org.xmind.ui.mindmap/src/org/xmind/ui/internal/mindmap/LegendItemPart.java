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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.draw2d.IFigure;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.graphics.Image;
import org.xmind.core.marker.IMarker;
import org.xmind.gef.GEF;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.part.IPart;
import org.xmind.gef.part.IRequestHandler;
import org.xmind.gef.policy.NullEditPolicy;
import org.xmind.ui.internal.decorators.LegendItemDecorator;
import org.xmind.ui.internal.figures.LegendItemFigure;
import org.xmind.ui.internal.svgsupport.SVGImageData;
import org.xmind.ui.internal.svgsupport.SVGReference;
import org.xmind.ui.mindmap.ILegendItemPart;
import org.xmind.ui.mindmap.ILegendPart;
import org.xmind.ui.mindmap.MindMapUI;
import org.xmind.ui.util.MarkerImageDescriptor;

public class LegendItemPart extends MindMapPartBase
        implements ILegendItemPart, PropertyChangeListener {

    private ImageDescriptor icon = null;

    private Image image = null;

    private SVGReference svgRef = null;

    private boolean deactivated = false;

    private LocalResourceManager resourceManager;

    public LegendItemPart() {
        setDecorator(LegendItemDecorator.getInstance());
    }

    protected IFigure createFigure() {
        boolean useAdvancedRenderer = getSite().getViewer().getProperties()
                .getBoolean(IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
        return new LegendItemFigure(
                useAdvancedRenderer ? RotatableWrapLabel.ADVANCED
                        : RotatableWrapLabel.NORMAL,
                svgRef != null ? getResourceManager() : null);
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof LegendPart) {
            LegendPart legend = (LegendPart) getParent();
            legend.removeItem(this);
        }
        super.setParent(parent);
        if (getParent() instanceof LegendPart) {
            LegendPart legend = (LegendPart) getParent();
            legend.addItem(this);
        }
    }

    public ILegendPart getOwnedLegend() {
        if (getParent() instanceof ILegendPart)
            return (ILegendPart) getParent();
        return null;
    }

    public LegendItem getItem() {
        return (LegendItem) super.getModel();
    }

    public String getMarkerId() {
        return getItem().getMarkerId();
    }

    public String getDescription() {
        return getItem().getDescription();
    }

    public Image getIconImage() {
        if (image == null && !deactivated) {
            image = createImage();
        }
        return image;
    }

    private Image createImage() {
        if (icon == null) {
            IMarker marker = getItem().getMarker();
            if (marker == null)
                return null;
            icon = MarkerImageDescriptor.createFromMarker(marker);
        }
        if (icon == null)
            return null;
        return icon.createImage(false);
    }

    public SVGImageData getSVGData() {
        if (deactivated)
            return null;
        return svgRef == null ? null : svgRef.getSVGData();
    }

    protected void onActivated() {
        super.onActivated();
        getSite().getViewer().getProperties().addPropertyChangeListener(
                IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, this);
    }

    protected void onDeactivated() {
        getSite().getViewer().getProperties().removePropertyChangeListener(
                IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, this);
        deactivated = true;

        if (image != null) {
            image.dispose();
            image = null;
        }

        if (svgRef != null)
            svgRef = null;

        if (resourceManager != null) {
            resourceManager.dispose();
            resourceManager = null;
        }
        super.onDeactivated();
    }

    @Override
    public void setModel(Object model) {
        super.setModel(model);
        if (model instanceof LegendItem) {
            LegendItem item = (LegendItem) model;
            IMarker marker = item.getMarker();
            String svgPath = marker != null ? marker.getSVGPath() : null;
            if (svgPath != null && !"".equals(svgPath)) { //$NON-NLS-1$
                this.svgRef = createSVGReference();
            }
        }
    }

    private SVGReference createSVGReference() {
        String resourcePath = MarkerImageDescriptor.RESOURCE_URL_PREFIX
                + getItem().getMarker().getSVGPath();
        SVGReference ref = new SVGReference(resourcePath);
        return ref;
    }

    public LocalResourceManager getResourceManager() {
        if (resourceManager == null)
            resourceManager = new LocalResourceManager(
                    JFaceResources.getResources());
        return resourceManager;
    }

    protected void declareEditPolicies(IRequestHandler reqHandler) {
        super.declareEditPolicies(reqHandler);
        reqHandler.installEditPolicy(GEF.ROLE_MOVABLE,
                MindMapUI.POLICY_LEGEND_MOVABLE);
        reqHandler.installEditPolicy(GEF.ROLE_EDITABLE,
                NullEditPolicy.getInstance());
        reqHandler.installEditPolicy(GEF.ROLE_MODIFIABLE,
                MindMapUI.POLICY_LEGEND_ITEM_MODIFIABLE);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean useAdvancedRenderer = getSite().getViewer().getProperties()
                .getBoolean(IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
        ((LegendItemFigure) getFigure()).setTextRenderStyle(useAdvancedRenderer
                ? RotatableWrapLabel.ADVANCED : RotatableWrapLabel.NORMAL);
    }

}