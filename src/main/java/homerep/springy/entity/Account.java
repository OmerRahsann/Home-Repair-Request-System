package homerep.springy.entity;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;

import java.util.Objects;

@Entity
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private boolean verified;

    private String verificationToken;

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    private AccountType type;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return verified == account.verified && Objects.equals(id, account.id) && Objects.equals(email, account.email) && Objects.equals(password, account.password) && Objects.equals(verificationToken, account.verificationToken) && type == account.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, verified, verificationToken, type);
    }

    @Override
    public String toString() {
        return "Account{" + "id=" + id + ", email='" + email + '\'' + ", password='" + password + '\'' + ", verified=" + verified + ", verificationToken='" + verificationToken + '\'' + ", type=" + type + '}';
    }

    public enum AccountType implements GrantedAuthority {
        SERVICE_REQUESTER, SERVICE_PROVIDER;

        @Override
        public String getAuthority() {
            return toString();
        }
    }
}
