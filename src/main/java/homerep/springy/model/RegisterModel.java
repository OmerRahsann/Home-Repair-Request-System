package homerep.springy.model;

import com.fasterxml.jackson.annotation.*;
import homerep.springy.entity.Account;
import homerep.springy.model.accountinfo.AccountInfoModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;

import java.util.Objects;

public class RegisterModel implements Model {
    @JsonUnwrapped
    private AccountModel account;
    @JsonProperty
    private Account.AccountType type;
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({@JsonSubTypes.Type(CustomerInfoModel.class), @JsonSubTypes.Type(ServiceProviderInfoModel.class)})
    private AccountInfoModel accountInfo;

    public RegisterModel() {}

    public RegisterModel(
            AccountModel account,
            Account.AccountType type,
            AccountInfoModel accountInfo
    ) {
        this.account = account;
        this.type = type;
        this.accountInfo = accountInfo;
    }

    @Override
    public boolean isValid() {
        if (account == null || type == null || accountInfo == null || !account.isValid() || !accountInfo.isValid()) {
            return false;
        }
        return switch (type) {
            case SERVICE_REQUESTER -> accountInfo instanceof CustomerInfoModel;
            case SERVICE_PROVIDER -> accountInfo instanceof ServiceProviderInfoModel;
        };
    }

    public AccountModel account() {
        return account;
    }

    public Account.AccountType type() {
        return type;
    }

    public AccountInfoModel accountInfo() {
        return accountInfo;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RegisterModel) obj;
        return Objects.equals(this.account, that.account) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.accountInfo, that.accountInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, type, accountInfo);
    }

    @Override
    public String toString() {
        return "RegisterModel[" +
                "account=" + account + ", " +
                "type=" + type + ", " +
                "accountInfo=" + accountInfo + ']';
    }

}
