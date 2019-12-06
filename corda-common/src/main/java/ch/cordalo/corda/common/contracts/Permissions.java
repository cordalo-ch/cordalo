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

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Permissions {
    public static final Map<String, Permissions> permissionMap = Collections.synchronizedMap(new LinkedHashMap<>());

    private final String name;
    private final Map<String, PartyAndRoles> partyRoleMapping = new LinkedHashMap<>();
    private final Map<String, Role> rolesMapping = new LinkedHashMap<>();

    public Permissions(String name) {
        this.name = name;
        permissionMap.put(name, this);
        this.initPartiesAndRoles();
        this.initPermissions();
    }

    public static Permissions get(String name) {
        return permissionMap.get(name);
    }

    protected abstract void initPermissions();

    protected abstract void initPartiesAndRoles();

    protected void addPartyAndRoles(String partyName, String... roles) {
        if (roles == null) return;
        List<Role> allRoles = Stream.of(roles).map(x -> getRoleIfAbsendPut(x)).collect(Collectors.toList());
        this.partyRoleMapping.put(partyName,
                new PartyAndRoles(partyName, allRoles));
    }

    private Role getRoleIfAbsendPut(String role) {
        Role roleObject = this.rolesMapping.get(role);
        if (roleObject == null) {
            roleObject = new Role(role);
            this.rolesMapping.put(role, roleObject);
        }
        return roleObject;
    }

    protected void addPermissionsForRole(String role, String... permissions) {
        if (permissions == null) return;
        this.getRoleIfAbsendPut(role).addPermissions(permissions);
    }

    protected void addStateActionsForRole(String role, String stateMachine, String... actions) {
        if (actions == null) return;
        List<String> permissions = Arrays.stream(actions).map(x -> stateMachine + ":state:" + x).collect(Collectors.toList());
        this.getRoleIfAbsendPut(role).addPermissions(permissions);
    }

    public boolean isPermitted(Party me, String action) {
        if (me == null) return false;
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(me.toString());
        return partyAndRoles != null && partyAndRoles.isPermitted(action);
    }

    public List<String> isPermitted(Party me, List<String> actions) {
        if (me == null) return Collections.EMPTY_LIST;
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(me.toString());
        if (partyAndRoles == null) {
            return Collections.EMPTY_LIST;
        } else {
            return partyAndRoles.isPermitted(actions);
        }
    }

    public Set<String> getActions(Party me) {
        if (me == null) return Collections.EMPTY_SET;
        PartyAndRoles partyAndRoles = this.partyRoleMapping.get(me.toString());
        return partyAndRoles == null ? Collections.EMPTY_SET : partyAndRoles.getValidActions();
    }

    private static class PartyAndRoles {
        String party;
        Set<Role> roles;

        public PartyAndRoles(String party, Role... roles) {
            this.party = party;
            this.roles = Sets.newHashSet(roles);
        }

        public PartyAndRoles(String party, List<Role> roles) {
            this.party = party;
            this.roles = Sets.newHashSet(roles);
        }

        public String getParty() {
            return party;
        }

        public Set<Role> getRoles() {
            return roles;
        }

        public boolean isPermitted(String action) {
            return this.getRoles().stream().anyMatch(x -> x.isPermitted(action));
        }

        public List<String> isPermitted(List<String> actions) {
            return actions.stream().filter(x -> this.isPermitted(x)).collect(Collectors.toList());
        }

        public Set<String> getValidActions() {
            Set<String> set = new HashSet<>();
            for (Role role : this.getRoles()) {
                role.addValidActions(set);
            }
            return set;
        }
    }

    private static class Role {
        String role;
        Set<String> permissions;

        public Role(String role) {
            this.role = role;
            this.permissions = new HashSet<>();
        }

        public String getRole() {
            return role;
        }

        public Set<String> getPermissions() {
            return permissions;
        }

        public boolean hasRole(String role) {
            return this.role.equals(role);
        }

        public boolean isPermitted(String action) {
            return this.getPermissions().contains(action);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o instanceof String) return this.role.equals(o);
            if (!(o instanceof Role)) return false;
            Role role1 = (Role) o;
            return Objects.equals(role, role1.role);
        }

        @Override
        public int hashCode() {
            return Objects.hash(role);
        }

        public void addPermissions(String[] permissions) {
            this.permissions.addAll(Lists.newArrayList(permissions));
        }

        public void addPermissions(List<String> permissions) {
            this.permissions.addAll(permissions);
        }

        public void addPermissions(String permission) {
            this.permissions.add(permission);
        }

        public void addValidActions(Set<String> set) {
            set.addAll(this.getPermissions());
        }
    }
}
