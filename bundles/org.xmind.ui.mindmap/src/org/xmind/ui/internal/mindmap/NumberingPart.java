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
import org.xmind.gef.IGraphicalViewer;
import org.xmind.gef.draw2d.RotatableWrapLabel;
import org.xmind.gef.part.IPart;
import org.xmind.ui.internal.decorators.NumberingDecorator;
import org.xmind.ui.mindmap.INumberingPart;
import org.xmind.ui.mindmap.ITopicPart;

public class NumberingPart extends MindMapPartBase implements INumberingPart,
        PropertyChangeListener {

    public NumberingPart() {
        setDecorator(NumberingDecorator.getInstance());
    }

    public void setParent(IPart parent) {
        if (getParent() instanceof TopicPart) {
            TopicPart topic = (TopicPart) getParent();
            if (topic.getNumberingPart() == this)
                topic.setNumberingPart(null);
        }
        super.setParent(parent);
        if (getParent() instanceof TopicPart) {
            TopicPart topic = (TopicPart) getParent();
            topic.setNumberingPart(this);
        }
    }

    public ITopicPart getTopicPart() {
        if (getParent() instanceof ITopicPart)
            return (ITopicPart) getParent();
        return null;
    }

    public String getNumberingText() {
        ITopicPart topicPart = getTopicPart();
        if (topicPart != null)
            return topicPart.getNumberingText();
        return null;
    }

    public String getFullNumberingText() {
        ITopicPart topicPart = getTopicPart();
        if (topicPart != null)
            return topicPart.getFullNumberingText();
        return null;
    }

    protected IFigure createFigure() {
        boolean useAdvancedRenderer = getSite().getViewer().getProperties()
                .getBoolean(IGraphicalViewer.VIEWER_RENDER_TEXT_AS_PATH, false);
        return new RotatableWrapLabel(
                useAdvancedRenderer ? RotatableWrapLabel.ADVANCED
                        : RotatableWrapLabel.NORMAL);
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