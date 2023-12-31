package homerep.springy.controller;

import homerep.springy.aspect.annotation.RateLimited;
import homerep.springy.exception.ApiException;
import homerep.springy.model.RegisterModel;
import homerep.springy.service.AccountService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class RegistrationController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/api/register")
    @RateLimited(capacity = 1, refillAmount = 1, refillDuration = 30)
    public void register(@RequestBody @Validated RegisterModel registerModel) {
        String email = registerModel.account().email();
        if (!accountService.isAllowedEmail(email)) {
            throw new ApiException("forbidden_email", "Your email address is not allowed to register an account.");
        }
        if (accountService.isRegistered(email)) {
            throw new ApiException("already_registered", "An account was already registered with that email address.");
        }
        accountService.registerAccount(registerModel);
    }

    @PostMapping("/api/verify")
    public void verify(@RequestBody @NotBlank String token) {
        if (!accountService.verifyAccount(token)) {
            throw new ApiException("invalid_token", "Verification token is invalid.");
        }
    }

}
