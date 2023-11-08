package homerep.springy.controller;

import homerep.springy.aspect.annotation.RateLimited;
import homerep.springy.exception.ApiException;
import homerep.springy.model.resetpassword.ResetPasswordModel;
import homerep.springy.model.resetpassword.SendResetPasswordModel;
import homerep.springy.service.AccountService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reset_password")
public class ResetPasswordController {

    private final AccountService accountService;

    public ResetPasswordController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping(value = "/send")
    @RateLimited(capacity = 1, refillAmount = 1, refillDuration = 30)
    public void sendResetPassword(@RequestBody @Validated SendResetPasswordModel model) {
        accountService.sendResetPassword(model.email());
    }

    @PostMapping
    public void resetPassword(@RequestBody @Validated ResetPasswordModel resetPasswordModel) {
        if (!accountService.resetPassword(resetPasswordModel)) {
            throw new ApiException("invalid_token", "Reset password token is invalid.");
        }
    }
}
