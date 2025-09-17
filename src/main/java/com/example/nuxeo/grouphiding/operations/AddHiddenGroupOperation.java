package com.example.nuxeo.grouphiding.operations;

import com.example.nuxeo.grouphiding.api.GroupHidingConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.runtime.api.Framework;

/**
 * Automation operation to add a hidden group rule for a user.
 */
@Operation(id = AddHiddenGroupOperation.ID, category = "GroupHiding", 
           label = "Add Hidden Group", description = "Add a group hiding rule for a specific user")
public class AddHiddenGroupOperation {

    public static final String ID = "GroupHiding.AddHiddenGroup";
    
    private static final Logger log = LogManager.getLogger(AddHiddenGroupOperation.class);

    @Param(name = "username", description = "Username to add the rule for")
    protected String username;

    @Param(name = "groupName", description = "Group name to hide")
    protected String groupName;

    @OperationMethod
    public void run() {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be null or empty");
        }

        try {
            GroupHidingConfiguration config = Framework.getService(GroupHidingConfiguration.class);
            if (config == null) {
                throw new RuntimeException("GroupHidingConfiguration service not available");
            }

            config.addHiddenGroup(username.trim(), groupName.trim());
            log.info("Added hidden group '{}' for user '{}' via automation", groupName, username);
            
        } catch (Exception e) {
            log.error("Failed to add hidden group '{}' for user '{}'", groupName, username, e);
            throw new RuntimeException("Failed to add hidden group: " + e.getMessage(), e);
        }
    }
}