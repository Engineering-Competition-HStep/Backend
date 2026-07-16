package com.Hstep.Hstep.domain.member.entity;

public enum MemberRole {
    USER,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
