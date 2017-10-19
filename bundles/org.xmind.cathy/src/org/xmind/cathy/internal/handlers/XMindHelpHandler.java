package org.xmind.cathy.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.util.SafeRunnable;
import org.xmind.cathy.internal.CathyPlugin;
import org.xmind.ui.browser.BrowserSupport;

public class XMindHelpHandler extends AbstractHandler {

    public Object execute(ExecutionEvent event) throws ExecutionException {
        SafeRunner.run(new SafeRunnable() {
            @Override
            public void run() throws Exception {
//                String internalURL = findInternalHelpURL();
//                if (internalURL != null) {
//                    BrowserSupport.getInstance().createBrowser()
//                            .openURL(internalURL);
//                } else {
                BrowserSupport.getInstance().createBrowser()
                        .openURL(CathyPlugin.ONLINE_HELP_URL);
//                }
            }
        });
        return null;
    }

//    private String findInternalHelpURL() {
//        Bundle helpBundle = Platform.getBundle("org.xmind.ui.help"); //$NON-NLS-1$
//        if (helpBundle != null) {
//            URL url = FileLocator.find(helpBundle,
//                    new Path("$nl$/contents/index.html"), null); //$NON-NLS-1$
//            if (url != null) {
//                try {
//                    url = FileLocator.toFileURL(url);
//                    return url.toExternalForm();
//                } catch (IOException e) {
//                }
//            }
//        }
//        return null;
//    }
}
