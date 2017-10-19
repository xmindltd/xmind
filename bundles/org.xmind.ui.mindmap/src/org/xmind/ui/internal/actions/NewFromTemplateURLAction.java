/**
 * 
 */
package org.xmind.ui.internal.actions;

import java.io.InputStream;
import java.net.URL;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author frankshaka
 */
public class NewFromTemplateURLAction extends BaseNewFromTemplateAction {

    private URL url;

    /**
     * @param window
     * @param resourcePath
     * @param name
     */
    public NewFromTemplateURLAction(IWorkbenchWindow window, URL url,
            String name) {
        super(window);
        if (url == null)
            throw new IllegalArgumentException();
        this.url = url;
        setText(name);
    }

    /*
     * (non-Javadoc)
     * @see
     * org.xmind.ui.internal.actions.BaseNewFromTemplateAction#getTemplateStream
     * (org.eclipse.swt.widgets.Shell)
     */
    protected InputStream getTemplateStream(Shell shell) throws Exception {
        return url.openStream();
    }

}
