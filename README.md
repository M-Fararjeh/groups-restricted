# Nuxeo Group Hiding Bundle

This OSGi bundle for Nuxeo 2023.0.159 implements a security policy that makes certain groups appear invisible to specific users, preventing access granted via those groups without modifying actual directory memberships.

## Features

- **User-Scoped Group Hiding**: Hide specific groups from selected users only
- **Security Policy Implementation**: Uses Nuxeo's security policy mechanism to deny access when permissions would be granted via hidden groups
- **Configuration Flexibility**: Supports both XML configuration and runtime management via automation operations
- **Non-Destructive**: Does not modify actual user/group memberships in the directory
- **Thread-Safe**: All operations are thread-safe for production use

## How It Works

The bundle implements a Nuxeo SecurityPolicy that:

1. Intercepts permission checks for documents
2. Identifies if the user has hidden groups configured
3. Checks if the user would have access through "visible" means (direct user permissions or visible group memberships)
4. If access would only be granted via hidden groups, the policy denies access
5. Otherwise, allows normal Nuxeo security processing

## Installation

1. Build the bundle:
   ```bash
   mvn clean package
   ```

2. Copy the generated JAR to your Nuxeo server:
   ```bash
   cp target/nuxeo-group-hiding-bundle-1.0.0-SNAPSHOT.jar $NUXEO_HOME/bundles/
   ```

3. Restart Nuxeo server

## Configuration

### XML Configuration

Create a configuration file in `$NUXEO_HOME/config/` (see `config-example.xml`):

```xml
<component name="com.example.nuxeo.grouphiding.config">
  <extension target="com.example.nuxeo.grouphiding.security.GroupHidingSecurityPolicy" point="hiddenGroups">
    <hiddenGroup username="john.doe">
      <group>administrators</group>
      <group>managers</group>
    </hiddenGroup>
  </extension>
</component>
```

### Runtime Configuration via Automation

Use the provided automation operations to manage rules at runtime:

- **Add hidden group**: `GroupHiding.AddHiddenGroup`
- **Remove hidden group**: `GroupHiding.RemoveHiddenGroup`

Example REST call to add a hidden group:
```bash
curl -X POST "$NUXEO_URL/api/v1/automation/GroupHiding.AddHiddenGroup" \
  -H "Content-Type: application/json" \
  -H "Authorization: Basic $AUTH" \
  -d '{"params": {"username": "john.doe", "groupName": "administrators"}}'
```

## Architecture

- `GroupHidingConfiguration`: Service interface for managing hiding rules
- `GroupHidingConfigurationImpl`: Thread-safe implementation with XML contribution support
- `GroupHidingSecurityPolicy`: Security policy that enforces access restrictions
- `GroupHidingDescriptor`: XMap descriptor for XML configuration
- `AddHiddenGroupOperation/RemoveHiddenGroupOperation`: Automation operations for runtime management

## Security Considerations

- The policy operates at the security policy level, ensuring comprehensive coverage
- All configuration changes are logged for audit purposes
- The implementation is fail-safe - errors result in allowing access rather than denying it
- Thread-safe implementation supports high-concurrency environments

## Testing

The bundle includes comprehensive logging to help with testing and debugging:
- Set log level to DEBUG for `com.example.nuxeo.grouphiding` to see detailed policy decisions
- Monitor access grants/denials in the logs
- Use automation operations to test configuration changes

## Compatibility

- Nuxeo 2023.0.159 and compatible versions
- Java 11+
- OSGi environment