package homerep.springy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.model.RegisterModel;
import homerep.springy.model.error.ApiErrorModel;
import homerep.springy.service.AccountService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
public class RegistrationController {
    @Autowired
    private AccountService accountService;

    @PostMapping("/api/register")
    public ResponseEntity<Object> register(@RequestBody @Validated RegisterModel registerModel) {
        if (accountService.isRegistered(registerModel.account().email())) {
            return ResponseEntity.badRequest().body(
                    new ApiErrorModel("already_registered",
                            "An account was already registered with that email address.")
            );
        }
        accountService.registerAccount(registerModel);

        return ResponseEntity.ok("Registered");
    }

    @GetMapping("/api/verify")
    public ResponseEntity<Object> verify(@RequestParam(name = "token") @NotBlank String token) {
        if (accountService.verifyAccount(token)) {
            return ResponseEntity.ok("Successfully verified!");
        }
        return ResponseEntity.badRequest().body(
                new ApiErrorModel("invalid_token", "Verification token is invalid.")
        );
    }

}
