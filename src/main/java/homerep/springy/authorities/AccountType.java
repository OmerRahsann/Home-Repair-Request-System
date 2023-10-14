package homerep.springy.authorities;


import org.springframework.security.core.GrantedAuthority;

public enum AccountType implements GrantedAuthority {
    CUSTOMER, SERVICE_PROVIDER;

    @Override
    public String getAuthority() {
        return toString();
    }
}
