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
package org.xmind.ui.internal.wizards;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.viewers.LabelProviderChangedEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.xmind.ui.internal.ITemplateDescriptor;
import org.xmind.ui.viewers.ImageCachedLabelProvider;

public class TemplateLabelProvider extends ImageCachedLabelProvider {

    private static TemplateImageLoader imageLoader;

    private Display display;

    private PropertyChangeListener templatePropertyListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (ITemplateDescriptor.PROP_IMAGE.equals(evt.getPropertyName())) {
                handleImageChange(evt);
            }
        }
    };

    public TemplateLabelProvider() {
        this.display = Display.getCurrent();
    }

    @Override
    protected Image createImage(Object element) {
        if (element instanceof ITemplateDescriptor) {
            ITemplateDescriptor template = (ITemplateDescriptor) element;
            if (template.getImage() != null)
                return template.getImage().createImage(display);

            template.getPropertyChangeSupport().addPropertyChangeListener(
                    ITemplateDescriptor.PROP_IMAGE, templatePropertyListener);
            if (imageLoader == null || imageLoader.getResult() != null) {
                imageLoader = new TemplateImageLoader(display);
            }
            imageLoader.loadImage(template);
        }
        return null;
    }

    @Override
    public String getText(Object element) {
        if (element instanceof ITemplateDescriptor) {
            ITemplateDescriptor template = (ITemplateDescriptor) element;
            return template.getName();
        }
        return super.getText(element);
    }

    private void handleImageChange(PropertyChangeEvent evt) {
        ITemplateDescriptor template = (ITemplateDescriptor) evt.getSource();
        template.getPropertyChangeSupport().removePropertyChangeListener(
                ITemplateDescriptor.PROP_IMAGE, templatePropertyListener);
        final LabelProviderChangedEvent imageChangedEvent = new LabelProviderChangedEvent(
                this, template);
        if (display != null && !display.isDisposed()) {
            display.asyncExec(new Runnable() {
                public void run() {
                    fireLabelProviderChanged(imageChangedEvent);
                }
            });
        }
    }

}