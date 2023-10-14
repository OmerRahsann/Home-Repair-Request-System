package homerep.springy.authorities;

import org.springframework.security.core.GrantedAuthority;

public record Verified() implements GrantedAuthority {
    public static final Verified INSTANCE = new Verified();

    @Override
    public String getAuthority() {
        return "VERIFIED";
    }
}
