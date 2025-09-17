package com.example.nuxeo.grouphiding.impl;

import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.*;

public class GroupHidingConfigurationImplTest {

    private GroupHidingConfigurationImpl configuration;

    @Before
    public void setUp() {
        configuration = new GroupHidingConfigurationImpl();
    }

    @Test
    public void testAddAndCheckHiddenGroup() {
        // Add hidden group
        configuration.addHiddenGroup("testuser", "hiddengroup");
        
        // Verify it's hidden
        assertTrue(configuration.isGroupHiddenForUser("testuser", "hiddengroup"));
        assertFalse(configuration.isGroupHiddenForUser("testuser", "visiblegroup"));
        assertFalse(configuration.isGroupHiddenForUser("otheruser", "hiddengroup"));
    }

    @Test
    public void testGetHiddenGroupsForUser() {
        // Add multiple hidden groups
        configuration.addHiddenGroup("testuser", "group1");
        configuration.addHiddenGroup("testuser", "group2");
        configuration.addHiddenGroup("otheruser", "group3");
        
        // Verify hidden groups for testuser
        Set<String> hiddenGroups = configuration.getHiddenGroupsForUser("testuser");
        assertEquals(2, hiddenGroups.size());
        assertTrue(hiddenGroups.contains("group1"));
        assertTrue(hiddenGroups.contains("group2"));
        assertFalse(hiddenGroups.contains("group3"));
        
        // Verify hidden groups for otheruser
        Set<String> otherHiddenGroups = configuration.getHiddenGroupsForUser("otheruser");
        assertEquals(1, otherHiddenGroups.size());
        assertTrue(otherHiddenGroups.contains("group3"));
    }

    @Test
    public void testRemoveHiddenGroup() {
        // Add and then remove
        configuration.addHiddenGroup("testuser", "hiddengroup");
        assertTrue(configuration.isGroupHiddenForUser("testuser", "hiddengroup"));
        
        configuration.removeHiddenGroup("testuser", "hiddengroup");
        assertFalse(configuration.isGroupHiddenForUser("testuser", "hiddengroup"));
    }

    @Test
    public void testClearHiddenGroupsForUser() {
        // Add multiple groups
        configuration.addHiddenGroup("testuser", "group1");
        configuration.addHiddenGroup("testuser", "group2");
        configuration.addHiddenGroup("otheruser", "group3");
        
        // Clear for testuser only
        configuration.clearHiddenGroupsForUser("testuser");
        
        // Verify testuser has no hidden groups
        assertTrue(configuration.getHiddenGroupsForUser("testuser").isEmpty());
        
        // Verify otheruser still has hidden groups
        assertFalse(configuration.getHiddenGroupsForUser("otheruser").isEmpty());
    }

    @Test
    public void testGetUsersWithHiddenGroups() {
        // Add groups for different users
        configuration.addHiddenGroup("user1", "group1");
        configuration.addHiddenGroup("user2", "group2");
        
        Set<String> users = configuration.getUsersWithHiddenGroups();
        assertEquals(2, users.size());
        assertTrue(users.contains("user1"));
        assertTrue(users.contains("user2"));
    }

    @Test
    public void testNullInputHandling() {
        // Test null inputs don't cause exceptions
        assertFalse(configuration.isGroupHiddenForUser(null, "group"));
        assertFalse(configuration.isGroupHiddenForUser("user", null));
        assertTrue(configuration.getHiddenGroupsForUser(null).isEmpty());
        
        // These should not throw exceptions
        configuration.addHiddenGroup(null, "group");
        configuration.addHiddenGroup("user", null);
        configuration.removeHiddenGroup(null, "group");
        configuration.removeHiddenGroup("user", null);
        configuration.clearHiddenGroupsForUser(null);
    }
}