package homerep.springy.controller;

import homerep.springy.authorities.AccountType;
import homerep.springy.entity.Account;
import homerep.springy.entity.Customer;
import homerep.springy.entity.ServiceProvider;
import homerep.springy.exception.ApiException;
import homerep.springy.model.ChangePasswordModel;
import homerep.springy.model.accountinfo.CustomerInfoModel;
import homerep.springy.model.accountinfo.ServiceProviderInfoModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.repository.CustomerRepository;
import homerep.springy.repository.ServiceProviderRepository;
import homerep.springy.service.AccountService;
import homerep.springy.type.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private ServiceProviderRepository serviceProviderRepository;

    @Autowired
    private AccountService accountService;

    @GetMapping("/type")
    public AccountType getAccountType(@AuthenticationPrincipal User user) {
        Account account = accountRepository.findByEmail(user.getEmail());
        return account.getType();
    }

    @GetMapping("/verified")
    public boolean isVerified(@AuthenticationPrincipal User user) {
        Account account = accountRepository.findByEmail(user.getEmail());
        return account.isVerified();
    }

    @GetMapping("/email")
    public String getEmail(@AuthenticationPrincipal User user) {
        return user.getUsername();
    }

    @PostMapping("/customer/update")
    public void updateCustomerInfo(@RequestBody @Validated CustomerInfoModel model, @AuthenticationPrincipal User user) {
        Customer customer = customerRepository.findByAccountEmail(user.getUsername());
        accountService.updateCustomerInfo(customer, model);
    }

    @GetMapping("/customer")
    public CustomerInfoModel getCustomerInfo(@AuthenticationPrincipal User user) {
        Customer customer = customerRepository.findByAccountEmail(user.getUsername());
        return CustomerInfoModel.fromEntity(customer);
    }

    @PostMapping("/provider/update")
    public void updateServiceProviderInfo(@RequestBody @Validated ServiceProviderInfoModel model, @AuthenticationPrincipal User user) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        accountService.updateServiceProviderInfo(serviceProvider, model);
    }

    @GetMapping("/provider")
    public ServiceProviderInfoModel getServiceProviderInfo(@AuthenticationPrincipal User user) {
        ServiceProvider serviceProvider = serviceProviderRepository.findByAccountEmail(user.getUsername());
        return ServiceProviderInfoModel.fromEntity(serviceProvider);
    }

    @PostMapping("/change_password")
    public void changePassword(@RequestBody @Validated ChangePasswordModel model, @AuthenticationPrincipal User user) {
        Account account = accountRepository.findByEmail(user.getUsername());
        if (!accountService.changePassword(account, model)) {
            throw new ApiException("incorrect_password", "Current password does not match.");
        }
    }
}
