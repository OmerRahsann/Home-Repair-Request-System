package homerep.springy.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import homerep.springy.authorities.AccountType;
import homerep.springy.model.accountinfo.AccountInfoModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.validator.annotation.ValidRegisterModel;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Objects;

@ValidRegisterModel
public class RegisterModel {
    @JsonUnwrapped
    @NotNull
    @Valid
    private AccountModel account;
    @JsonProperty
    @NotNull
    @Valid
    private AccountType type;
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({@JsonSubTypes.Type(CustomerInfoModel.class), @JsonSubTypes.Type(ServiceProviderInfoModel.class)})
    @NotNull
    @Valid
    private AccountInfoModel accountInfo;

    private RegisterModel() {}

    public RegisterModel(
            AccountModel account,
            AccountType type,
            AccountInfoModel accountInfo
    ) {
        this.account = account;
        this.type = type;
        this.accountInfo = accountInfo;
    }

    public AccountModel account() {
        return account;
    }

    public AccountType type() {
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
