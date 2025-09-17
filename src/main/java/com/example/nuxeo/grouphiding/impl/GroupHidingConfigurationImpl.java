package com.example.nuxeo.grouphiding.impl;

import com.example.nuxeo.grouphiding.api.GroupHidingConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.runtime.model.ComponentInstance;
import org.nuxeo.runtime.model.DefaultComponent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of GroupHidingConfiguration.
 * Stores configuration in memory with thread-safe access.
 */
public class GroupHidingConfigurationImpl extends DefaultComponent implements GroupHidingConfiguration {

    private static final Logger log = LogManager.getLogger(GroupHidingConfigurationImpl.class);

    // Thread-safe map to store user -> hidden groups mapping
    private final Map<String, Set<String>> userHiddenGroups = new ConcurrentHashMap<>();

    @Override
    public boolean isGroupHiddenForUser(String username, String groupName) {
        if (username == null || groupName == null) {
            return false;
        }

        Set<String> hiddenGroups = userHiddenGroups.get(username);
        boolean isHidden = hiddenGroups != null && hiddenGroups.contains(groupName);
        
        if (isHidden) {
            log.debug("Group '{}' is hidden for user '{}'", groupName, username);
        }
        
        return isHidden;
    }

    @Override
    public Set<String> getHiddenGroupsForUser(String username) {
        if (username == null) {
            return Collections.emptySet();
        }

        Set<String> hiddenGroups = userHiddenGroups.get(username);
        return hiddenGroups != null ? new HashSet<>(hiddenGroups) : Collections.emptySet();
    }

    @Override
    public Set<String> getUsersWithHiddenGroups() {
        return new HashSet<>(userHiddenGroups.keySet());
    }

    @Override
    public void addHiddenGroup(String username, String groupName) {
        if (username == null || groupName == null) {
            log.warn("Cannot add null username or groupName");
            return;
        }

        userHiddenGroups.computeIfAbsent(username, k -> ConcurrentHashMap.newKeySet()).add(groupName);
        log.info("Added hidden group '{}' for user '{}'", groupName, username);
    }

    @Override
    public void removeHiddenGroup(String username, String groupName) {
        if (username == null || groupName == null) {
            return;
        }

        Set<String> hiddenGroups = userHiddenGroups.get(username);
        if (hiddenGroups != null) {
            hiddenGroups.remove(groupName);
            if (hiddenGroups.isEmpty()) {
                userHiddenGroups.remove(username);
            }
            log.info("Removed hidden group '{}' for user '{}'", groupName, username);
        }
    }

    @Override
    public void clearHiddenGroupsForUser(String username) {
        if (username == null) {
            return;
        }

        Set<String> removed = userHiddenGroups.remove(username);
        if (removed != null && !removed.isEmpty()) {
            log.info("Cleared {} hidden groups for user '{}'", removed.size(), username);
        }
    }

    @Override
    public void registerContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if ("hiddenGroups".equals(extensionPoint) && contribution instanceof GroupHidingDescriptor) {
            GroupHidingDescriptor descriptor = (GroupHidingDescriptor) contribution;
            processGroupHidingDescriptor(descriptor);
        }
    }

    @Override
    public void unregisterContribution(Object contribution, String extensionPoint, ComponentInstance contributor) {
        if ("hiddenGroups".equals(extensionPoint) && contribution instanceof GroupHidingDescriptor) {
            GroupHidingDescriptor descriptor = (GroupHidingDescriptor) contribution;
            removeGroupHidingDescriptor(descriptor);
        }
    }

    private void processGroupHidingDescriptor(GroupHidingDescriptor descriptor) {
        String username = descriptor.getUsername();
        Set<String> groups = descriptor.getHiddenGroups();
        
        if (username != null && groups != null) {
            for (String group : groups) {
                addHiddenGroup(username, group);
            }
            log.info("Processed group hiding descriptor for user '{}' with {} groups", username, groups.size());
        }
    }

    private void removeGroupHidingDescriptor(GroupHidingDescriptor descriptor) {
        String username = descriptor.getUsername();
        Set<String> groups = descriptor.getHiddenGroups();
        
        if (username != null && groups != null) {
            for (String group : groups) {
                removeHiddenGroup(username, group);
            }
            log.info("Removed group hiding descriptor for user '{}' with {} groups", username, groups.size());
        }
    }
}