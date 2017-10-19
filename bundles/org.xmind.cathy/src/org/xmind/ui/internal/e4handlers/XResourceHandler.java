package org.xmind.ui.internal.e4handlers;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.internal.runtime.PlatformURLPluginConnection;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.internal.workbench.CommandLineOptionModelProcessor;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.ModelAssembler;
import org.eclipse.e4.ui.internal.workbench.URIHelper;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.commands.impl.CommandsPackageImpl;
import org.eclipse.e4.ui.model.application.impl.ApplicationPackageImpl;
import org.eclipse.e4.ui.model.application.ui.advanced.impl.AdvancedPackageImpl;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicPackageImpl;
import org.eclipse.e4.ui.model.application.ui.impl.UiPackageImpl;
import org.eclipse.e4.ui.model.application.ui.menu.impl.MenuPackageImpl;
import org.eclipse.e4.ui.workbench.IModelResourceHandler;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.emf.common.util.TreeIterator;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.osgi.service.datalocation.Location;
import org.osgi.framework.Bundle;
import org.xmind.cathy.internal.CathyPlugin;

/**
 * This class is responsible to load and save the model
 */
@SuppressWarnings("restriction")
public class XResourceHandler implements IModelResourceHandler {

    private static final String WORKBENCH_XMI = "workbench.xmi"; //$NON-NLS-1$
    private ResourceSetImpl resourceSetImpl;
    private Resource resource;

    @Inject
    private Logger logger;

    @Inject
    private IEclipseContext context;

    @Inject
    @Named(E4Workbench.INITIAL_WORKBENCH_MODEL_URI)
    private URI applicationDefinitionInstance;

    @Inject
    @Optional
    @Named(E4Workbench.INSTANCE_LOCATION)
    private Location instanceLocation;

    /**
     * Dictates whether the model should be stored using EMF or with the merging
     * algorithm. https://bugs.eclipse.org/bugs/show_bug.cgi?id=295524
     */
    final private boolean saveAndRestore;
    final private boolean clearPersistedState;

    /**
     * Constructor.
     *
     * @param saveAndRestore
     * @param clearPersistedState
     */
    @Inject
    public XResourceHandler(
            @Named(IWorkbench.PERSIST_STATE) boolean saveAndRestore,
            @Named(IWorkbench.CLEAR_PERSISTED_STATE) boolean clearPersistedState) {
        this.saveAndRestore = saveAndRestore;
        this.clearPersistedState = clearPersistedState;
    }

    @PostConstruct
    void init() {
        resourceSetImpl = new ResourceSetImpl();
        resourceSetImpl.getResourceFactoryRegistry().getExtensionToFactoryMap()
                .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                        new E4XMIResourceFactory());

        resourceSetImpl.getPackageRegistry().put(ApplicationPackageImpl.eNS_URI,
                ApplicationPackageImpl.eINSTANCE);
        resourceSetImpl.getPackageRegistry().put(CommandsPackageImpl.eNS_URI,
                CommandsPackageImpl.eINSTANCE);
        resourceSetImpl.getPackageRegistry().put(UiPackageImpl.eNS_URI,
                UiPackageImpl.eINSTANCE);
        resourceSetImpl.getPackageRegistry().put(MenuPackageImpl.eNS_URI,
                MenuPackageImpl.eINSTANCE);
        resourceSetImpl.getPackageRegistry().put(BasicPackageImpl.eNS_URI,
                BasicPackageImpl.eINSTANCE);
        resourceSetImpl.getPackageRegistry().put(AdvancedPackageImpl.eNS_URI,
                AdvancedPackageImpl.eINSTANCE);
        resourceSetImpl.getPackageRegistry().put(
                org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eNS_URI,
                org.eclipse.e4.ui.model.application.descriptor.basic.impl.BasicPackageImpl.eINSTANCE);

    }

    /**
     * @return {@code true} if the current application model has top-level
     *         windows.
     */
    public boolean hasTopLevelWindows() {
        return hasTopLevelWindows(resource);
    }

    /**
     * @return {@code true} if the specified application model has top-level
     *         windows.
     */
    private boolean hasTopLevelWindows(Resource applicationResource) {
        if (applicationResource == null
                || applicationResource.getContents() == null) {
            // If the application resource doesn't exist or has no contents, then it has no
            // top-level windows (and we are in an error state).
            return false;
        }
        MApplication application = (MApplication) applicationResource
                .getContents().get(0);
        return !application.getChildren().isEmpty();
    }

    @Override
    public Resource loadMostRecentModel() {
        File workbenchData = null;
        URI restoreLocation = null;

        if (saveAndRestore) {
            workbenchData = getWorkbenchSaveLocation();
            restoreLocation = URI
                    .createFileURI(workbenchData.getAbsolutePath());
        }

        if (clearPersistedState && workbenchData != null
                && workbenchData.exists()) {
            workbenchData.delete();
        }

        // last stored time-stamp
        long restoreLastModified = restoreLocation == null ? 0L
                : new File(restoreLocation.toFileString()).lastModified();

        // See bug 380663, bug 381219
        // long lastApplicationModification = getLastApplicationModification();
        // boolean restore = restoreLastModified > lastApplicationModification;
        boolean restore = restoreLastModified > 0;
        boolean initialModel;

        resource = null;
        if (restore && saveAndRestore) {
            resource = loadResource(restoreLocation);
            // If the saved model does not have any top-level windows, Eclipse will exit
            // immediately, so throw out the persisted state and reinitialize with the defaults.
            if (!hasTopLevelWindows(resource)) {
                if (logger != null) {
                    logger.error(new Exception(), // log a stack trace to help debug the corruption
                            "The persisted workbench has no top-level windows, so reinitializing with defaults."); //$NON-NLS-1$
                }
                resource = null;
            }
        }
        if (resource == null) {
            Resource applicationResource = loadResource(
                    applicationDefinitionInstance);
            MApplication theApp = (MApplication) applicationResource
                    .getContents().get(0);
            resource = createResourceWithApp(theApp);
            //Prevent 3.x workbench from using WorkbenchMigrationProcessor to migrate org.eclipse.ui.workbench/workbench.xml
//            context.set(E4Workbench.NO_SAVED_MODEL_FOUND, Boolean.TRUE);
            initialModel = true;
        } else {
            initialModel = false;
        }

        // Add model items described in the model extension point
        // This has to be done before commands are put into the context
        MApplication appElement = (MApplication) resource.getContents().get(0);

        this.context.set(MApplication.class, appElement);
        ModelAssembler contribProcessor = ContextInjectionFactory
                .make(ModelAssembler.class, context);
        contribProcessor.processModel(initialModel);

        if (!hasTopLevelWindows(resource) && logger != null) {
            logger.error(new Exception(), // log a stack trace to help debug the
                    // corruption
                    "Initializing from the application definition instance yields no top-level windows! " //$NON-NLS-1$
                            + "Continuing execution, but the missing windows may cause other initialization failures."); //$NON-NLS-1$
        }

        if (!clearPersistedState) {
            CommandLineOptionModelProcessor processor = ContextInjectionFactory
                    .make(CommandLineOptionModelProcessor.class, context);
            processor.process();
        }

        return resource;
    }

    @Override
    public void save() throws IOException {
        if (saveAndRestore)
            resource.save(null);
    }

    /**
     * Creates a resource with an app Model, used for saving copies of the main
     * app model.
     *
     * @param theApp
     *            the application model to add to the resource
     * @return a resource with a proper save path with the model as contents
     */
    @Override
    public Resource createResourceWithApp(MApplication theApp) {
        Resource res = createResource();
        res.getContents().add((EObject) theApp);
        return res;
    }

    private Resource createResource() {
        if (saveAndRestore) {
            URI saveLocation = URI.createFileURI(
                    getWorkbenchSaveLocation().getAbsolutePath());
            return resourceSetImpl.createResource(saveLocation);
        }
        return resourceSetImpl.createResource(URI.createURI(WORKBENCH_XMI));
    }

    private File getWorkbenchSaveLocation() {
        File workbenchData = new File(getBaseLocation(), WORKBENCH_XMI);
        return workbenchData;
    }

    private File getBaseLocation() {
        File baseLocation;
        try {
            baseLocation = new File(URIUtil.toURI(instanceLocation.getURL()));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        baseLocation = new File(baseLocation, ".metadata"); //$NON-NLS-1$
        baseLocation = new File(baseLocation, ".plugins"); //$NON-NLS-1$
        baseLocation = new File(baseLocation, "org.xmind.cathy"); //$NON-NLS-1$
        baseLocation = new File(baseLocation, "e4Model"); //$NON-NLS-1$
        baseLocation = new File(baseLocation,
                CathyPlugin.getDefault().getBundle().getVersion().toString());
        return baseLocation;
    }

    // Ensures that even models with error are loaded!
    private Resource loadResource(URI uri) {
        Resource resource;
        try {
            resource = getResource(uri);
        } catch (Exception e) {
            // TODO We could use diagnostics for better analyzing the error
            logger.error(e, "Unable to load resource " + uri.toString()); //$NON-NLS-1$
            return null;
        }

        // TODO once we switch from deltas, we only need this once on the default model?
        String contributorURI = URIHelper.EMFtoPlatform(uri);
        if (contributorURI != null) {
            TreeIterator<EObject> it = EcoreUtil
                    .getAllContents(resource.getContents());
            while (it.hasNext()) {
                EObject o = it.next();
                if (o instanceof MApplicationElement) {
                    ((MApplicationElement) o).setContributorURI(contributorURI);
                }
            }
        }
        return resource;
    }

    private Resource getResource(URI uri) throws Exception {
        Resource resource;
        if (saveAndRestore) {
            resource = resourceSetImpl.getResource(uri, true);
        } else {
            // Workaround for java.lang.IllegalStateException: No instance data can be specified
            // thrown by org.eclipse.core.internal.runtime.DataArea.assertLocationInitialized
            // The DataArea.assertLocationInitialized is called by ResourceSetImpl.getResource(URI,
            // boolean)
            resource = resourceSetImpl.createResource(uri);
            resource.load(new URL(uri.toString()).openStream(),
                    resourceSetImpl.getLoadOptions());
        }

        return resource;
    }

    protected long getLastApplicationModification() {
        long appLastModified = 0L;
        ResourceSetImpl resourceSetImpl = new ResourceSetImpl();

        Map<String, ?> attributes = resourceSetImpl.getURIConverter()
                .getAttributes(applicationDefinitionInstance,
                        Collections.singletonMap(
                                URIConverter.OPTION_REQUESTED_ATTRIBUTES,
                                Collections.singleton(
                                        URIConverter.ATTRIBUTE_TIME_STAMP)));

        Object timestamp = attributes.get(URIConverter.ATTRIBUTE_TIME_STAMP);
        if (timestamp instanceof Long) {
            appLastModified = ((Long) timestamp).longValue();
        } else if (applicationDefinitionInstance.isPlatformPlugin()) {
            try {
                java.net.URL url = new java.net.URL(
                        applicationDefinitionInstance.toString());
                // can't just use 'url.openConnection()' as it usually returns a
                // PlatformURLPluginConnection which doesn't expose the
                // last-modification time. So we try to resolve the file through
                // the bundle to obtain a BundleURLConnection instead.
                Object[] obj = PlatformURLPluginConnection
                        .parse(url.getFile().trim(), url);
                Bundle b = (Bundle) obj[0];
                // first try to resolve as an bundle file entry, then as a resource using
                // the bundle's classpath
                java.net.URL resolved = b.getEntry((String) obj[1]);
                if (resolved == null) {
                    resolved = b.getResource((String) obj[1]);
                }
                if (resolved != null) {
                    URLConnection openConnection = resolved.openConnection();
                    appLastModified = openConnection.getLastModified();
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return appLastModified;
    }
}
