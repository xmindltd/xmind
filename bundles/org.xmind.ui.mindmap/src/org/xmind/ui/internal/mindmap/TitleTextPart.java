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
import org.xmind.core.Core;
import org.xmind.core.event.CoreEvent;
import org.xmind.core.event.ICoreEventRegister;
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.ITextFigure;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.ui.mindmap.ITitleTextPart;

public class TitleTextPart extends MindMapPartBase implements ITitleTextPart,
        PropertyChangeListener {

    protected TitleTextPart() {
    }

    protected IFigure createFigure() {
        boolean useAdvancedRenderer = getSite().getViewer().getProperties()
                .getBoolean(IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
        return new RotatableWrapLabel(
                useAdvancedRenderer ? RotatableWrapLabel.ADVANCED
                        : RotatableWrapLabel.NORMAL);
    }

    public ITextFigure getTextFigure() {
        return (ITextFigure) super.getFigure();
    }

    protected void registerCoreEvents(Object source, ICoreEventRegister register) {
        super.registerCoreEvents(source, register);
        register.register(Core.TitleText);
    }

    public void handleCoreEvent(CoreEvent event) {
        String type = event.getType();
        if (Core.TitleText.equals(type)) {
            update();
        } else {
            super.handleCoreEvent(event);
        }
    }

    protected void onActivated() {
        super.onActivated();
        getSite()
                .getViewer()
                .getProperties()
                .addPropertyChangeListener(
                        IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, this);
    }

    protected void onDeactivated() {
        getSite()
                .getViewer()
                .getProperties()
                .removePropertyChangeListener(
                        IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, this);
        super.onDeactivated();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        boolean useAdvancedRenderer = getSite().getViewer().getProperties()
                .getBoolean(IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
        ((RotatableWrapLabel) getFigure())
                .setRenderStyle(useAdvancedRenderer ? RotatableWrapLabel.ADVANCED
                        : RotatableWrapLabel.NORMAL);
    }

}