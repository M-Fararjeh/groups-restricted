package com.example.nuxeo.grouphiding;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi Bundle Activator for the Group Hiding functionality.
 * This activator is responsible for starting and stopping the bundle.
 */
public class GroupHidingBundleActivator implements BundleActivator {

    private static final Logger log = LogManager.getLogger(GroupHidingBundleActivator.class);

    @Override
    public void start(BundleContext context) throws Exception {
        log.info("Starting Nuxeo Group Hiding Bundle");
        
        // Bundle startup logic can be added here if needed
        // The actual functionality is handled by Nuxeo components
        
        log.info("Nuxeo Group Hiding Bundle started successfully");
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        log.info("Stopping Nuxeo Group Hiding Bundle");
        
        // Cleanup logic can be added here if needed
        
        log.info("Nuxeo Group Hiding Bundle stopped");
    }
}