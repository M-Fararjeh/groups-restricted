package com.example.nuxeo.grouphiding.impl;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

import java.util.HashSet;
import java.util.Set;

/**
 * XMap descriptor for configuring group hiding rules via XML contributions.
 */
@XObject("hiddenGroup")
public class GroupHidingDescriptor {

    @XNode("@username")
    private String username;

    @XNodeList(value = "group", type = String[].class, componentType = String.class)
    private String[] groups;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<String> getHiddenGroups() {
        if (groups == null) {
            return new HashSet<>();
        }
        
        Set<String> result = new HashSet<>();
        for (String group : groups) {
            if (group != null && !group.trim().isEmpty()) {
                result.add(group.trim());
            }
        }
        return result;
    }

    public void setGroups(String[] groups) {
        this.groups = groups;
    }
}