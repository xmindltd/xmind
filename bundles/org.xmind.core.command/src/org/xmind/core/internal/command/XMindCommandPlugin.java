package org.xmind.core.internal.command;

import java.io.File;

import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.service.debug.DebugOptions;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.xmind.core.command.ICommandService;

public class XMindCommandPlugin implements BundleActivator,
        ServiceTrackerCustomizer<IExtensionRegistry, IExtensionRegistry> {

    public static final String PLUGIN_ID = "org.xmind.core.command"; //$NON-NLS-1$

    private static BundleContext bundleContext;

    private static XMindCommandPlugin singleton;

    private ICommandService commandService = null;

    private ServiceRegistration<ICommandService> commandServiceRegistration = null;

    private ServiceTracker<IExtensionRegistry, IExtensionRegistry> registryTracker = null;

    private File cacheLocation = null;

    private ServiceTracker<DebugOptions, DebugOptions> debugTracker = null;

    public XMindCommandPlugin() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
     * )
     */
    public void start(final BundleContext bundleContext) throws Exception {
        XMindCommandPlugin.bundleContext = bundleContext;
        XMindCommandPlugin.singleton = this;

        commandService = new XMindCommandService();
        commandServiceRegistration = bundleContext.registerService(
                ICommandService.class, commandService, null);

        registryTracker = new ServiceTracker<IExtensionRegistry, IExtensionRegistry>(
                bundleContext, IExtensionRegistry.class, this);
        registryTracker.open();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext bundleContext) throws Exception {
        if (debugTracker != null) {
            debugTracker.close();
            debugTracker = null;
        }
        if (registryTracker != null) {
            registryTracker.close();
            registryTracker = null;
        }

        if (commandServiceRegistration != null) {
            commandServiceRegistration.unregister();
            commandServiceRegistration = null;
        }
        commandService = null;

        if (cacheLocation != null) {
            BinaryUtil.delete(cacheLocation);
            cacheLocation = null;
        }

        XMindCommandPlugin.bundleContext = null;
        XMindCommandPlugin.singleton = null;
    }

    public File getBinaryCacheLocation() {
        if (cacheLocation == null) {
            cacheLocation = new File(Platform.getStateLocation(
                    bundleContext.getBundle()).toFile(), ".binarycaches"); //$NON-NLS-1$
        }
        return cacheLocation;
    }

    public ICommandService getCommandService() {
        return commandService;
    }

    public IExtensionRegistry addingService(
            ServiceReference<IExtensionRegistry> reference) {
        IExtensionRegistry registry = bundleContext.getService(reference);
        XMindCommandHandlerRegistry.getInstance().installRegistryEventListener(
                registry);
        return registry;
    }

    public void modifiedService(ServiceReference<IExtensionRegistry> reference,
            IExtensionRegistry service) {
        // do nothing
    }

    public void removedService(ServiceReference<IExtensionRegistry> reference,
            IExtensionRegistry registry) {
        XMindCommandHandlerRegistry.getInstance()
                .uninstallRegistryEventListener(registry);
        bundleContext.ungetService(reference);
    }

    private DebugOptions getDebugOptions() {
        if (debugTracker == null) {
            debugTracker = new ServiceTracker<DebugOptions, DebugOptions>(
                    bundleContext, DebugOptions.class, null);
            debugTracker.open();
        }
        return debugTracker.getService();
    }

    public static boolean isDebugging(String option) {
        DebugOptions options = getDefault().getDebugOptions();
        return options != null
                && options.getBooleanOption(PLUGIN_ID + option, false);
    }

    public static ILog getLog() {
        return Platform.getLog(bundleContext.getBundle());
    }

    public static XMindCommandPlugin getDefault() {
        return singleton;
    }

}
