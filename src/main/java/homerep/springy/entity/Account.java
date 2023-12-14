package homerep.springy.entity;

import homerep.springy.authorities.AccountType;
import homerep.springy.entity.type.Token;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        indexes = {
                @Index(columnList = "email", unique = true)
        }
)
public class Account {
    @Id
    @GeneratedValue
    private Long id;

    @Column(unique = true)
    private String email;

    private String password;

    private boolean verified;

    //TODO verification email token expiration
    private String verificationToken;

    @Embedded
    private Token resetPasswordToken;

    @Enumerated(EnumType.STRING)
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

    public Token getResetPasswordToken() {
        return resetPasswordToken;
    }

    public void setResetPasswordToken(Token resetPasswordToken) {
        this.resetPasswordToken = resetPasswordToken;
    }

    public AccountType getType() {
        return type;
    }

    public void setType(AccountType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        return verified == account.verified && Objects.equals(id, account.id) && Objects.equals(email, account.email) && Objects.equals(password, account.password) && Objects.equals(verificationToken, account.verificationToken) && Objects.equals(resetPasswordToken, account.resetPasswordToken) && type == account.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email, password, verified, verificationToken, resetPasswordToken, type);
    }

    @Override
    public String toString() {
        return "Account{" + "id=" + id + ", email='" + email + '\'' + ", password='" + password + '\'' + ", verified=" + verified + ", verificationToken='" + verificationToken + '\'' + ", resetPasswordToken=" + resetPasswordToken + ", type=" + type + '}';
    }
}
