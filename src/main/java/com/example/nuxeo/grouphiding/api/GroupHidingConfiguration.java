package com.example.nuxeo.grouphiding.api;

import java.util.Set;

/**
 * Configuration interface for group hiding functionality.
 * Defines which groups should be hidden for which users.
 */
public interface GroupHidingConfiguration {

    /**
     * Check if a group should be hidden for a specific user.
     * 
     * @param username the username to check
     * @param groupName the group name to check
     * @return true if the group should be hidden for this user
     */
    boolean isGroupHiddenForUser(String username, String groupName);

    /**
     * Get all hidden groups for a specific user.
     * 
     * @param username the username
     * @return set of hidden group names for this user
     */
    Set<String> getHiddenGroupsForUser(String username);

    /**
     * Get all users that have hidden groups configured.
     * 
     * @return set of usernames that have group hiding rules
     */
    Set<String> getUsersWithHiddenGroups();

    /**
     * Add a group hiding rule.
     * 
     * @param username the username
     * @param groupName the group name to hide
     */
    void addHiddenGroup(String username, String groupName);

    /**
     * Remove a group hiding rule.
     * 
     * @param username the username
     * @param groupName the group name to unhide
     */
    void removeHiddenGroup(String username, String groupName);

    /**
     * Clear all hidden groups for a user.
     * 
     * @param username the username
     */
    void clearHiddenGroupsForUser(String username);
}