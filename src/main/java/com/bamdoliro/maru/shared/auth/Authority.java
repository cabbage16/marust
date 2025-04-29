package com.bamdoliro.maru.shared.auth;

import java.util.ArrayList;
import java.util.List;

public enum Authority {
    ALL, USER, ADMIN;

    public static List<String> getRoles(Authority[] authorities) {
        List<String> roles = new ArrayList<>();

        for (Authority authority : authorities) {
            if (authority == ALL) {
                roles.add("ROLE_ADMIN");
                roles.add("ROLE_USER");
            } else {
                roles.add("ROLE_" + authority.name());
            }
        }

        return roles;
    }
}
