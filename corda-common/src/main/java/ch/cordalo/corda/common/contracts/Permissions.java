/*******************************************************************************
 * Copyright (c) 2019 by cordalo.ch - MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/

package ch.cordalo.corda.common.contracts;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.corda.core.identity.Party;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Permissions {
    public static final Map<String, Permissions> permissionMap = Collections.synchronizedMap(new LinkedHashMap<>());

    public static Permissions get(String name) {
        return permissionMap.get(name);
    }

    public static Map<String, Object> getAllPermissions(Party party) {
        Map<String, Set<String>> permissions = new LinkedHashMap<>();
        permissions.put("roles", new LinkedHashSet<>());
        permissions.put("actions", new LinkedHashSet<>());
        Map<String, Map<String, String>> attributes = new LinkedHashMap<>();
        attributes.put("attributes", new LinkedHashMap<>());
        for (Permissions permission : permissionMap.values()) {
            permissions = permission.getAllPermissionsFor(party, permissions);
            attributes = permission.getAllAttributesFor(party, attributes);
        }
        Map<String, Object> finalMap = new LinkedHashMap<>();
        finalMap.putAll(permissions);
        finalMap.putAll(attributes);
        return finalMap;
    }


    private final String name;
    private final Map<String, PartyAndRoles> partyRoleMapping = new LinkedHashMap<>();
    private final Map<String, Role> rolesMapping = new LinkedHashMap<>();

    public Permissions(String name) {
        this.name = name;
        permissionMap.put(name, this);
        this.initPartiesAndRoles();
        this.initPermissions();
        this.initPartiesAndAttributes();
    }

    public String getName() {
        return name;
    }

    protected abstract void initPermissions();

    protected abstract void initPartiesAndRoles();

    protected abstract void initPartiesAndAttributes();

    protected void addPartyAndRoles(String partyName, String... roles) {
        if (roles == null || roles.length == 0) throw new IllegalArgumentException("at least 1 role must be provided");
        List<Role> allRoles = Stream.of(roles).map(x -> getRoleIfAbsendPut(x)).collect(Collectors.toList());
        this.getPartyIfAbsendPut(partyName).addRoles(allRoles);
    }

    private Role getRoleIfAbsendPut(String role) {
        Role roleObject = this.rolesMapping.get(role);
        if (roleObject == null) {
            roleObject = new Role(role);
            this.rolesMapping.put(role, roleObject);
        }
        return roleObject;
    }

    private PartyAndRoles getPartyIfAbsendPut(String party) {
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(party);
        if (partyAndRoles == null) {
            partyAndRoles = new PartyAndRoles(party);
            this.partyRoleMapping.put(party, partyAndRoles);
        }
        return partyAndRoles;
    }

    private String getPermissionNameFromAction(String action) {
        return this.getName() + ":action:" + action;
    }

    private String getPermissionNameFromStateAction(String action) {
        return this.getName() + ":state:" + action;
    }

    protected void addCommandActionsForRole(String role, String... actions) {
        if (actions == null || actions.length == 0)
            throw new IllegalArgumentException("at least 1 action must be provided");
        this.getRoleIfAbsendPut(role).addPermissions(actions);
    }

    protected void addStateActionsForRole(String role, String... actions) {
        if (actions == null || actions.length == 0)
            throw new IllegalArgumentException("at least 1 action must be provided");
        List<String> permissions = Arrays.stream(actions).map(this::getPermissionNameFromStateAction).collect(Collectors.toList());
        this.getRoleIfAbsendPut(role).addPermissions(permissions);
    }

    protected void addPartyAndAttribute(String party, String attribute, String value) {
        this.getPartyIfAbsendPut(party).addAttribute(attribute, value);
    }

    public boolean isPermitted(Party party, String action) {
        if (party == null) throw new IllegalArgumentException("party must be provided");
        if (action == null || action.isEmpty()) throw new IllegalArgumentException("action must be provided");
        ;
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(getPartyName(party));
        return partyAndRoles != null && partyAndRoles.isPermitted(action);
    }

    public boolean isStateActionPermitted(Party party, String action) {
        return isPermitted(party, getPermissionNameFromStateAction(action));
    }

    public boolean isActionPermitted(Party party, String action) {
        return isPermitted(party, getPermissionNameFromAction(action));
    }

    public boolean hasAttribute(Party party, String attribute, String value) {
        if (party == null) throw new IllegalArgumentException("party must be provided");
        if (attribute == null || attribute.isEmpty()) throw new IllegalArgumentException("attribute must be provided");
        ;
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(getPartyName(party));
        return partyAndRoles != null && partyAndRoles.hasAttribute(attribute, value);
    }

    public String getAttribute(Party party, String attribute) {
        if (party == null) throw new IllegalArgumentException("party must be provided");
        if (attribute == null || attribute.isEmpty()) throw new IllegalArgumentException("attribute must be provided");
        ;
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(getPartyName(party));
        if (partyAndRoles != null) {
            return partyAndRoles.getAttribute(attribute);
        } else {
            return null;
        }
    }

    public List<String> isPermitted(Party party, List<String> actions) {
        if (party == null) throw new IllegalArgumentException("party must be provided");
        if (actions == null || actions.isEmpty())
            throw new IllegalArgumentException("at least 1 action must be provided");
        ;
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(getPartyName(party));
        if (partyAndRoles == null) {
            return Collections.emptyList();
        } else {
            return partyAndRoles.isPermitted(actions);
        }
    }

    public Set<String> getActions(Party party) {
        if (party == null) throw new IllegalArgumentException("party must be provided");
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(getPartyName(party));
        return partyAndRoles == null ? Collections.emptySet() : partyAndRoles.getValidActions();
    }

    public Set<String> getRoles(Party party) {
        if (party == null) throw new IllegalArgumentException("party must be provided");
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(getPartyName(party));
        return partyAndRoles == null ? Collections.emptySet() : partyAndRoles.getRoles().stream().map(Role::getRole).collect(Collectors.toSet());
    }

    public Map<String, String> getAttributes(Party party) {
        if (party == null) throw new IllegalArgumentException("party must be provided");
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(getPartyName(party));
        return partyAndRoles == null ? Collections.emptyMap() : partyAndRoles.getAttributes();
    }

    @NotNull
    private String getPartyName(Party party) {
        return party.getName().getX500Principal().getName();
    }

    private Map<String, Set<String>> getAllPermissionsFor(Party party, Map<String, Set<String>> map) {
        map.get("actions").addAll(this.getActions(party));
        map.get("roles").addAll(this.getRoles(party));
        return map;
    }

    private Map<String, Map<String, String>> getAllAttributesFor(Party party, Map<String, Map<String, String>> map) {
        map.get("attributes").putAll(this.getAttributes(party));
        return map;
    }

    public Map<String, Object> getAllPermissionsFor(Party party) {
        Map<String, Object> finalMap = new LinkedHashMap<>();
        finalMap.putAll(this.getAllPermissionsFor(party, new LinkedHashMap<>()));
        finalMap.putAll(this.getAllAttributesFor(party, new LinkedHashMap<>()));
        return finalMap;
    }

    private static class PartyAndRoles {
        String party;
        Set<Role> roles;
        Map<String, String> attributes;

        private PartyAndRoles(String party, Role... roles) {
            this.party = party;
            this.roles = roles == null ? Sets.newHashSet() : Sets.newHashSet(roles);
            this.attributes = new LinkedHashMap<>();
        }

        private void addRoles(List<Role> roles) {
            this.roles.addAll(roles);
        }

        private void addAttribute(String attribute, String value) {
            this.getAttributes().put(attribute, value);
        }

        private String getParty() {
            return party;
        }

        private Set<Role> getRoles() {
            return roles;
        }

        private Map<String, String> getAttributes() {
            return attributes;
        }

        private boolean isPermitted(String action) {
            return this.getRoles().stream().anyMatch(x -> x.isPermitted(action));
        }

        private List<String> isPermitted(List<String> actions) {
            return actions.stream().filter(this::isPermitted).collect(Collectors.toList());
        }

        private String getAttribute(String attribute) {
            return this.getAttributes().get(attribute);
        }

        private boolean hasAttribute(String attribute, String value) {
            return Objects.equals(this.getAttribute(attribute), value);
        }

        private Set<String> getValidActions() {
            Set<String> set = new HashSet<>();
            for (Role role : this.getRoles()) {
                role.addValidActions(set);
            }
            return set;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PartyAndRoles)) return false;
            if (o instanceof String) return o != null && o.equals(this.getParty());
            PartyAndRoles that = (PartyAndRoles) o;
            return getParty().equals(that.getParty());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getParty());
        }
    }

    private static class Role {
        String role;
        Set<String> permissions;

        private Role(String role) {
            this.role = role;
            this.permissions = new HashSet<>();
        }

        private String getRole() {
            return role;
        }

        private Set<String> getPermissions() {
            return permissions;
        }

        private boolean hasRole(String role) {
            return this.role.equals(role);
        }

        private boolean isPermitted(String action) {
            return this.getPermissions().contains(action);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof String) return o != null && o.equals(this.role);
            if (!(o instanceof Role)) return false;
            Role role1 = (Role) o;
            return Objects.equals(role, role1.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(role);
        }

        private void addPermissions(String[] permissions) {
            this.permissions.addAll(Lists.newArrayList(permissions));
        }

        private void addPermissions(List<String> permissions) {
            this.permissions.addAll(permissions);
        }

        private void addPermissions(String permission) {
            this.permissions.add(permission);
        }

        private void addValidActions(Set<String> set) {
            set.addAll(this.getPermissions());
        }

    }


}
