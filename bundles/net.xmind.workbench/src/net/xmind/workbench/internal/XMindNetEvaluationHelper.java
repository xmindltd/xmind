/* ******************************************************************************
 * Copyright (c) 2006-2015 XMind Ltd. and others.
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
package net.xmind.workbench.internal;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.services.IEvaluationService;

import net.xmind.signin.IAccountInfo;
import net.xmind.signin.IAuthenticationListener;
import net.xmind.signin.XMindNet;

public class XMindNetEvaluationHelper implements IAuthenticationListener {

    private IEvaluationService evaluationService;

    public XMindNetEvaluationHelper(IWorkbench workbench) {
        this.evaluationService = (IEvaluationService) workbench
                .getService(IEvaluationService.class);
        XMindNet.addAuthenticationListener(this);
        if (XMindNet.getAccountInfo() != null) {
            handleSignIn();
        } else {
            handleSignOut();
        }
    }

    public void shutdown() {
        XMindNet.removeAuthenticationListener(this);
    }

    public void postSignOut(IAccountInfo oldAccountInfo) {
        handleSignOut();
    }

    public void postSignIn(IAccountInfo accountInfo) {
        handleSignIn();
    }

    private void handleSignOut() {
        System.setProperty(XMindNetWorkbench.PROP_AUTHENTICATED,
                XMindNetWorkbench.VALUE_UNAUTHENTICATED);
        if (evaluationService != null) {
            evaluationService
                    .requestEvaluation(XMindNetWorkbench.PROP_AUTHENTICATED);
        }
    }

    private void handleSignIn() {
        System.setProperty(XMindNetWorkbench.PROP_AUTHENTICATED,
                XMindNetWorkbench.VALUE_AUTHENTICATED);
        if (evaluationService != null) {
            evaluationService
                    .requestEvaluation(XMindNetWorkbench.PROP_AUTHENTICATED);
        }
    }

}
