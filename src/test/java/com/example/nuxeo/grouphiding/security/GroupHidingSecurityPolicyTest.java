package com.example.nuxeo.grouphiding.security;

import com.example.nuxeo.grouphiding.api.GroupHidingConfiguration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.model.Document;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class GroupHidingSecurityPolicyTest {

    @Mock
    private Document document;

    @Mock
    private ACP mergedAcp;

    @Mock
    private NuxeoPrincipal principal;

    @Mock
    private GroupHidingConfiguration configuration;

    private GroupHidingSecurityPolicy policy;

    @Before
    public void setUp() {
        policy = new GroupHidingSecurityPolicy();
        
        // Mock principal setup
        when(principal.getName()).thenReturn("testuser");
        when(principal.getGroups()).thenReturn(Arrays.asList("group1", "group2", "hiddengroup"));
        when(principal.isMemberOf("hiddengroup")).thenReturn(true);
        when(principal.isMemberOf("group1")).thenReturn(true);
        when(principal.isMemberOf("group2")).thenReturn(true);
    }

    @Test
    public void testCheckPermission_NoHiddenGroups_ReturnsUnknown() {
        // Setup
        when(configuration.getHiddenGroupsForUser("testuser")).thenReturn(new HashSet<>());

        // Execute
        Access result = policy.checkPermission(document, mergedAcp, principal, "Read", 
                                             new String[]{"Read"}, new String[]{});

        // Verify
        assertEquals(Access.UNKNOWN, result);
    }

    @Test
    public void testCheckPermission_AccessViaVisibleGroups_ReturnsUnknown() {
        // Setup
        Set<String> hiddenGroups = new HashSet<>();
        hiddenGroups.add("hiddengroup");
        when(configuration.getHiddenGroupsForUser("testuser")).thenReturn(hiddenGroups);
        
        // Mock ACP to grant access via visible groups
        when(mergedAcp.getAccess(any(String[].class), eq("Read"))).thenReturn(Access.GRANT);

        // Execute
        Access result = policy.checkPermission(document, mergedAcp, principal, "Read", 
                                             new String[]{"Read"}, new String[]{});

        // Verify
        assertEquals(Access.UNKNOWN, result);
    }

    @Test
    public void testCheckPermission_AccessOnlyViaHiddenGroups_ReturnsDeny() {
        // Setup
        Set<String> hiddenGroups = new HashSet<>();
        hiddenGroups.add("hiddengroup");
        when(configuration.getHiddenGroupsForUser("testuser")).thenReturn(hiddenGroups);
        
        // Mock ACP to deny access via visible principals but grant via hidden group
        when(mergedAcp.getAccess(argThat(principals -> 
            Arrays.asList(principals).contains("hiddengroup")), eq("Read")))
            .thenReturn(Access.GRANT);
        when(mergedAcp.getAccess(argThat(principals -> 
            !Arrays.asList(principals).contains("hiddengroup")), eq("Read")))
            .thenReturn(Access.DENY);

        // Execute
        Access result = policy.checkPermission(document, mergedAcp, principal, "Read", 
                                             new String[]{"Read"}, new String[]{});

        // Verify
        assertEquals(Access.DENY, result);
    }

    @Test
    public void testIsRestrictingPermission_ReturnsTrue() {
        assertTrue(policy.isRestrictingPermission("Read"));
        assertTrue(policy.isRestrictingPermission("Write"));
        assertTrue(policy.isRestrictingPermission("Everything"));
    }

    @Test
    public void testIsExpressibleInQuery_ReturnsTrue() {
        assertTrue(policy.isExpressibleInQuery("default"));
    }
}