/* ******************************************************************************
 * Copyright (c) 2006-2016 XMind Ltd. and others.
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
/**
 * 
 */
package org.xmind.ui.internal.e4handlers;

import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.inject.Named;

import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IFilter;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.xmind.core.Core;
import org.xmind.core.CoreException;
import org.xmind.core.IWorkbook;
import org.xmind.gef.ui.editor.IEditable;
import org.xmind.ui.internal.MindMapMessages;
import org.xmind.ui.internal.MindMapUIPlugin;
import org.xmind.ui.internal.dialogs.SaveWizardDialog;
import org.xmind.ui.internal.editor.DecryptionDialog;
import org.xmind.ui.internal.editor.IEncryptable;
import org.xmind.ui.internal.editor.SaveWizardManager.SaveWizardDescriptor;
import org.xmind.ui.mindmap.IWorkbookRef;
import org.xmind.ui.mindmap.IWorkbookRefFactory;
import org.xmind.ui.prefs.PrefConstants;
import org.xmind.ui.wizards.ISaveContext;
import org.xmind.ui.wizards.ISaveWizard;
import org.xmind.ui.wizards.ISaveWizard.SaveWizardNotAvailable;
import org.xmind.ui.wizards.SaveOptions;

/**
 * @author Frank Shaka
 * @since 3.6.50
 */
public class SaveWorkbookAsHandler {

    @Execute
    public void execute(
            @Named(IServiceConstants.ACTIVE_SELECTION) Object selection,
            @Optional IProgressService progressProvider,
            final @Optional IServiceLocator serviceLocator)
            throws InvocationTargetException {
        if (selection instanceof IStructuredSelection) {
            selection = ((IStructuredSelection) selection).getFirstElement();
        }
        if (selection instanceof IWorkbookRef) {
            saveWorkbookAs(new ISaveContext() {
                @Override
                public Object getContextVariable(String key) {
                    IEvaluationService service = serviceLocator
                            .getService(IEvaluationService.class);
                    Assert.isNotNull(service);
                    Object variable = service.getCurrentState()
                            .getVariable(key);
                    return variable == IEvaluationContext.UNDEFINED_VARIABLE
                            ? null : variable;
                }

                @Override
                public <T> T getContextVariable(Class<T> key) {
                    return serviceLocator.getService(key);
                }
            }, (IWorkbookRef) selection, progressProvider, null, false);
        }
    }

    /**
     * @param context
     *            the context where this operation happens
     * @param workbookRef
     *            the source workbook ref to be saved
     * @param runner
     *            an {@link IRunnableContext} to run the job
     * @param optionFilter
     *            <code>null</code> to accept all options at beginning, or an
     *            {@link IFilter} instance that selects available option ids
     * @param onlyToLocal
     *            is only save to local
     * @return a new workbook ref that is filled with contents from the source
     *         workbook ref
     * @throws InvocationTargetException
     */
    public static IWorkbookRef saveWorkbookAs(ISaveContext context,
            final IWorkbookRef workbookRef, IRunnableContext runner,
            IFilter optionFilter, boolean onlyToLocal)
            throws InvocationTargetException {
        Assert.isLegal(context != null);

        Shell shell = context.getContextVariable(Shell.class);

        // list all available location options
        List<SaveWizardDescriptor> wizards = MindMapUIPlugin.getDefault()
                .getSaveWizardManager().getWizards();
        if (wizards.isEmpty()) {
            // no location options available....
            // should not happen?
            // no need to extract strings
            MessageDialog.openWarning(shell, "Save As", //$NON-NLS-1$
                    "No 'Save As' options available in this application. " //$NON-NLS-1$
                            + "Please contact the software provider for this issue."); //$NON-NLS-1$
            return null;
        }

        // make a new array so that we can remove unavailable ones
        wizards = new ArrayList<SaveWizardDescriptor>(wizards);
        if (optionFilter != null) {
            Iterator<SaveWizardDescriptor> locationProviderIt = wizards
                    .iterator();
            while (locationProviderIt.hasNext()) {
                SaveWizardDescriptor locationProvider = locationProviderIt
                        .next();
                if (!optionFilter.select(locationProvider.getId())) {
                    locationProviderIt.remove();
                }
            }
        }

        sortLocationProviders(wizards);

        // ensure runnable context available
        if (runner == null) {
            runner = new ProgressMonitorDialog(shell);
        }

        // prepare save options
        String oldName = workbookRef.getName();
        if (oldName == null) {
            IWorkbook workbook = workbookRef.getWorkbook();
            if (workbook != null) {
                oldName = workbook.getPrimarySheet().getRootTopic()
                        .getTitleText();
            }
            if (oldName == null)
                oldName = MindMapMessages.SaveWorkbookAsHandler_oldName_default;
        }

        while (true) {
            if (wizards.isEmpty()) {
                // tried out all possible location options....
                // should not happen?
                // no need to extract strings
                MessageDialog.openError(shell, "Save As", //$NON-NLS-1$
                        "No 'Save As' options available now for this workbook. " //$NON-NLS-1$
                                + "Please contact the software provider for this issue."); //$NON-NLS-1$
                return null;
            }

            SaveOptions options = SaveOptions.getDefault() //
                    .proposalName(oldName) //
                    .oldURI(workbookRef.getURI());

            SaveWizardDescriptor wizard;
            if (onlyToLocal || wizards.size() == 1) {
                wizard = wizards.get(0);
            } else {
                String oldWizardId = workbookRef.getSaveWizardId();
                SaveWizardDescriptor defaultWizard = oldWizardId == null ? null
                        : findSaveWizard(wizards, oldWizardId, false);
                if (defaultWizard == null) {
                    int maxPriority = 0;
                    for (SaveWizardDescriptor wizardDescriptor : wizards) {
                        /// sort by priority
                        /// choose highest priority
                        /// exclude those whose priority < 0
                        ISaveWizard wizard_0 = wizardDescriptor.getWizard();
                        if (wizard_0 != null) {
                            int priority = wizard_0.getPriorityFor(context,
                                    options);
                            if (priority > maxPriority) {
                                maxPriority = priority;
                                defaultWizard = wizardDescriptor;
                            }
                            if (priority < 0)
                                wizards.remove(wizardDescriptor);
                        }
                    }

                }
                if (defaultWizard == null) {
                    defaultWizard = wizards.get(0);
                }
                SaveWizardDialog dialog = new SaveWizardDialog(shell, wizards,
                        defaultWizard, options);
                if (dialog.open() != SaveWizardDialog.OK)
                    // canceled
                    return null;

                wizard = dialog.getTargetWizard();
                options = dialog.getTargetOptions();
            }

            ISaveWizard wizardImpl = wizard.getWizard();
            Assert.isNotNull(wizardImpl);

            URI newURI;
            try {
                newURI = wizardImpl.askForTargetURI(context, options);
            } catch (SaveWizardNotAvailable na) {

                wizards.remove(wizard);

                String saveText;
                if (wizards.size() == 0) {
                    MessageDialog.openInformation(shell, "No options", //$NON-NLS-1$
                            na.getMessage());
                    return null;
                } else if (wizards.size() == 1) {
                    saveText = NLS.bind(
                            MindMapMessages.SaveWorkbookAsHandler_saveToOtherDialog_saveTo_text,
                            wizards.get(0).getName());
                } else {
                    saveText = MindMapMessages.SaveWorkbookAsHandler_saveToOtherDialog_saveToAnother_text;
                }

                MessageDialog dialog = new MessageDialog(

                        shell,

                        MindMapMessages.SaveWorkbookAsHandler_saveToOtherDialog_title,

                        null,

                        na.getMessage(),

                        MessageDialog.CONFIRM,

                        new String[] {

                                saveText,

                                IDialogConstants.CANCEL_LABEL

                        }, 0

                );

                if (dialog.open() != MessageDialog.OK)
                    return null;

                continue;
            }
            if (newURI == null)
                // canceled
                return null;

            IWorkbookRefFactory workbookRefFactory = MindMapUIPlugin
                    .getDefault().getWorkbookRefFactory();
            final IWorkbookRef newWorkbookRef = workbookRefFactory
                    .createWorkbookRef(newURI, null);
            Assert.isNotNull(newWorkbookRef);
            if (!newWorkbookRef.isInState(IEditable.CLOSED)) {
                MessageDialog.openWarning(shell,
                        MindMapMessages.SaveWorkbookAsHandler_warningDialog_title,
                        MindMapMessages.SaveWorkbookAsHandler_warningDialog_description);
                continue;
            }

            newWorkbookRef.setActiveContext(workbookRef.getActiveContext());

            if (newWorkbookRef.equals(workbookRef)) {
                // same location
                // just save the old workbook ref
                if (!workbookRef.isInState(IWorkbookRef.CLOSED)) {
                    try {
                        runner.run(true, true, new IRunnableWithProgress() {
                            @Override
                            public void run(IProgressMonitor monitor)
                                    throws InvocationTargetException,
                                    InterruptedException {
                                workbookRef.save(monitor);
                            }
                        });
                    } catch (InterruptedException e) {
                        // canceled
                        return null;
                    }
                }
                return newWorkbookRef;
            }

            if (!newWorkbookRef.canImportFrom(workbookRef)) {
                MessageDialog.openError(shell,
                        MindMapMessages.SaveWorkbookAsHandler_saveAsDialog_title,
                        MindMapMessages.SaveWorkbookAsHandler_saveAsDialog_description);
                wizards.remove(wizard);
                continue;
            }

            try {
                runner.run(true, true, new IRunnableWithProgress() {
                    public void run(IProgressMonitor monitor)
                            throws InvocationTargetException,
                            InterruptedException {
                        doSaveAs(monitor, workbookRef, newWorkbookRef, 0);
                    }
                });
            } catch (InterruptedException e) {
                // canceled
                return null;
            }

            // should return here in normal cases
            return newWorkbookRef;
        }
    }

    private static void doSaveAs(final IProgressMonitor monitor,
            final IWorkbookRef oldWorkbookRef,
            final IWorkbookRef newWorkbookRef, int times) {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 100);
        try {
            oldWorkbookRef.open(subMonitor.newChild(15));
            newWorkbookRef.importFrom(subMonitor.newChild(80), oldWorkbookRef);
        } catch (final InvocationTargetException e) {
            CoreException coreEx = getCoreException(e);
            if (coreEx != null) {
                int errType = coreEx.getType();
                if (errType == Core.ERROR_WRONG_PASSWORD) {
                    openDecryptionDialog(oldWorkbookRef, newWorkbookRef,
                            monitor,
                            MindMapMessages.MindMapEditor_passwordPrompt_message2,
                            times);
                    return;
                }
                return;
            }
        } catch (InterruptedException e) {
            return;
        } finally {
            subMonitor.setWorkRemaining(5);
            try {
                oldWorkbookRef.close(subMonitor.newChild(5));
            } catch (InvocationTargetException e) {
            } catch (InterruptedException e) {
            }
        }
    }

    private static CoreException getCoreException(Throwable e) {
        if (e == null)
            return null;
        if (e instanceof CoreException)
            return (CoreException) e;
        return getCoreException(e.getCause());
    }

    private static void openDecryptionDialog(final IWorkbookRef oldWorkbookRef,
            final IWorkbookRef newWorkbookRef, final IProgressMonitor monitor,
            String message, final int times) {
        final int nextTime = times + 1;
        final IEncryptable encryptable = oldWorkbookRef
                .getAdapter(IEncryptable.class);

        Display.getDefault().asyncExec(new Runnable() {

            public void run() {
                new DecryptionDialog(Display.getDefault().getActiveShell(),
                        oldWorkbookRef.getName(), encryptable.getPasswordHint(),
                        times) {
                    protected void okPressed() {
                        super.okPressed();

                        encryptable.setPassword(getPassword());
                        doSaveAs(monitor, oldWorkbookRef, newWorkbookRef,
                                nextTime);
                    };

                    protected void cancelPressed() {
                        super.cancelPressed();
                    };
                }.open();

            }
        });
    }

    private static void sortLocationProviders(List<SaveWizardDescriptor> list) {
        IPreferenceStore pref = MindMapUIPlugin.getDefault()
                .getPreferenceStore();
        List<SaveWizardDescriptor> preferredWizards = new ArrayList<SaveWizardDescriptor>();
        List<SaveWizardDescriptor> nameSortedWizards = new ArrayList<SaveWizardDescriptor>();

        String[] preferredWizardIds = getPreferredSaveWizardIds(pref);
        if (preferredWizardIds != null) {
            for (String wizardId : preferredWizardIds) {
                SaveWizardDescriptor wizard = findSaveWizard(list, wizardId,
                        true);
                if (wizard != null) {
                    preferredWizards.add(wizard);
                }
            }
        }

        nameSortedWizards.addAll(list);
        if (!nameSortedWizards.isEmpty()) {
            Collections.sort(nameSortedWizards,
                    new Comparator<SaveWizardDescriptor>() {
                        @Override
                        public int compare(SaveWizardDescriptor w1,
                                SaveWizardDescriptor w2) {
                            return w1.getName().compareTo(w2.getName());
                        }
                    });
        }

        list.clear();
        list.addAll(preferredWizards);
        list.addAll(nameSortedWizards);
    }

    /**
     * @param list
     * @param id
     * @param remove
     * @return
     */
    private static SaveWizardDescriptor findSaveWizard(
            List<SaveWizardDescriptor> list, String id, boolean remove) {
        Iterator<SaveWizardDescriptor> it = list.iterator();
        while (it.hasNext()) {
            SaveWizardDescriptor wizard = it.next();
            if (id.equals(wizard.getId())) {
                if (remove)
                    it.remove();
                return wizard;
            }
        }
        return null;
    }

    private static String[] getPreferredSaveWizardIds(IPreferenceStore pref) {
        String preferredWizardIds = pref.getString(PrefConstants.SAVE_WIZARDS);
        if (preferredWizardIds != null && !"".equals(preferredWizardIds)) { //$NON-NLS-1$
            return preferredWizardIds.split(","); //$NON-NLS-1$
        }
        return null;
    }

}
