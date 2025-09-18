package com.example.nuxeo.grouphiding.security;

import com.example.nuxeo.grouphiding.api.GroupHidingConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.core.api.NuxeoPrincipal;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.core.api.security.Access;
import org.nuxeo.ecm.core.api.security.SecurityPolicy;
import org.nuxeo.ecm.core.model.Document;
import org.nuxeo.ecm.core.query.sql.model.SQLQuery;
import org.nuxeo.runtime.api.Framework;

import java.security.Principal;
import java.util.Set;

/**
 * Security policy that denies access when permissions are granted via hidden groups.
 * This policy intercepts permission checks and denies access if the only way
 * the user would have access is through a group that should be hidden from them.
 */
public class GroupHidingSecurityPolicy implements SecurityPolicy {

    private static final Logger log = LogManager.getLogger(GroupHidingSecurityPolicy.class);

    @Override
    public Access checkPermission(Document doc, ACP mergedAcp, Principal principal, 
                                String permission, String[] resolvedPermissions, String[] additionalPrincipals) {
        
        if (!(principal instanceof NuxeoPrincipal)) {
            return Access.UNKNOWN;
        }

        NuxeoPrincipal nuxeoPrincipal = (NuxeoPrincipal) principal;
        if (nuxeoPrincipal.getName() == null) {
            return Access.UNKNOWN;
        }

        try {
            GroupHidingConfiguration config = Framework.getService(GroupHidingConfiguration.class);
            if (config == null) {
                log.debug("GroupHidingConfiguration service not available");
                return Access.UNKNOWN;
            }

            String username = nuxeoPrincipal.getName();
            Set<String> hiddenGroups = config.getHiddenGroupsForUser(username);
            
            if (hiddenGroups.isEmpty()) {
                // No hidden groups for this user, allow normal processing
                return Access.UNKNOWN;
            }

            // Check if user has the permission through visible means
            boolean hasAccessThroughVisibleMeans = hasPermissionThroughVisibleMeans(
                mergedAcp, nuxeoPrincipal, permission, hiddenGroups);
            
            if (hasAccessThroughVisibleMeans) {
                // User has access through visible means, allow it
                log.debug("User '{}' has access to permission '{}' through visible means", username, permission);
                return Access.UNKNOWN;
            }

            // Check if user would have access only through hidden groups
            boolean hasAccessThroughHiddenGroups = hasPermissionThroughHiddenGroups(
                mergedAcp, nuxeoPrincipal, permission, hiddenGroups);
            
            if (hasAccessThroughHiddenGroups) {
                // User would have access only through hidden groups, deny it
                log.info("Denying access for user '{}' to permission '{}' - access would be via hidden groups", 
                        username, permission);
                return Access.DENY;
            }

        } catch (Exception e) {
            log.error("Error in GroupHidingSecurityPolicy", e);
        }

        return Access.UNKNOWN;
    }

    private boolean hasPermissionThroughVisibleMeans(ACP mergedAcp, NuxeoPrincipal principal, 
                                                   String permission, Set<String> hiddenGroups) {
        if (mergedAcp == null) {
            return false;
        }

        // Create a set of visible principals (user + visible groups)
        Set<String> visiblePrincipals = getVisiblePrincipals(principal, hiddenGroups);
        
        // Check if any of the visible principals have the required permission
        for (String visiblePrincipal : visiblePrincipals) {
            Access access = mergedAcp.getAccess(visiblePrincipal, permission);
            if (Access.GRANT.equals(access)) {
                return true;
            }
        }
        
        return false;
    }

    private boolean hasPermissionThroughHiddenGroups(ACP mergedAcp, NuxeoPrincipal principal,
                                                   String permission, Set<String> hiddenGroups) {
        if (mergedAcp == null || hiddenGroups.isEmpty()) {
            return false;
        }

        // Check if any hidden group has the required permission
        for (String hiddenGroup : hiddenGroups) {
            if (principal.isMemberOf(hiddenGroup)) {
                Access access = mergedAcp.getAccess(hiddenGroup, permission);
                if (Access.GRANT.equals(access)) {
                    return true;
                }
            }
        }

        return false;
    }

    private Set<String> getVisiblePrincipals(NuxeoPrincipal principal, Set<String> hiddenGroups) {
        Set<String> visiblePrincipals = new java.util.HashSet<>();
        
        // Add the user principal
        visiblePrincipals.add(principal.getName());
        
        // Add all groups except hidden ones
        for (String group : principal.getGroups()) {
            if (!hiddenGroups.contains(group)) {
                visiblePrincipals.add(group);
            }
        }
        
        return visiblePrincipals;
    }

    @Override
    public boolean isRestrictingPermission(String permission) {
        // This policy can restrict any permission
        return true;
    }

    @Override
    public boolean isExpressibleInQuery() {
        // This policy affects query results
        return true;
    }

    @Override
    public SQLQuery.Transformer getQueryTransformer() {
        // For now, return null - query transformation could be implemented
        // if more sophisticated query-level filtering is needed
        return null;
    }
}