package homerep.springy.type;

import homerep.springy.authorities.AccountType;
import homerep.springy.authorities.Verified;
import homerep.springy.entity.Account;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class User implements UserDetails {
    private final long accountId;
    private final String email;
    private final String password;
    private final AccountType accountType;
    private final boolean verified;

    public User(Account account) {
        this.accountId = account.getId();
        this.email = account.getEmail();
        this.password = account.getPassword();
        this.accountType = account.getType();
        this.verified = account.isVerified();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (verified) {
            return List.of(accountType, Verified.INSTANCE);
        } else {
            return List.of(accountType);
        }
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email; // The email is the username
    }

    public long getAccountId() {
        return accountId;
    }

    public String getEmail() {
        return email;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public boolean isVerified() {
        return verified;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
