package homerep.springy.entity.type;

import jakarta.persistence.Embeddable;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class Token {
    private String val; // "value" causes issues with SQL
    private Instant refreshAt;
    private Instant expireAt;

    protected Token() {}

    public Token(Instant refreshAt, Instant expireAt) {
        this(UUID.randomUUID().toString(), refreshAt, expireAt);
    }

    public Token(String value, Instant refreshAt, Instant expireAt) {
        this.val = value;
        this.refreshAt = refreshAt;
        this.expireAt = expireAt;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String tokenValue) {
        this.val = tokenValue;
    }

    public Instant getRefreshAt() {
        return refreshAt;
    }

    public void setRefreshAt(Instant refreshAt) {
        this.refreshAt = refreshAt;
    }

    public boolean canRefresh() {
        return Instant.now().isAfter(refreshAt);
    }

    public Instant getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Instant expireAt) {
        this.expireAt = expireAt;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expireAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Token token = (Token) o;
        return Objects.equals(val, token.val) && Objects.equals(refreshAt, token.refreshAt) && Objects.equals(expireAt, token.expireAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(val, refreshAt, expireAt);
    }

    @Override
    public String toString() {
        return "Token{" +
                "value='" + val + '\'' +
                ", refreshAt=" + refreshAt +
                ", expireAt=" + expireAt +
                '}';
    }
}
