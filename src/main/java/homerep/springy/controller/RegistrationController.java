package homerep.springy.controller;

import homerep.springy.entity.Account;
import homerep.springy.model.AccountModel;
import homerep.springy.repository.AccountRepository;
import homerep.springy.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class RegistrationController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountRepository repository;

    @PostMapping("/api/register")
    public ResponseEntity<Object> register(@RequestBody AccountModel accountModel) {
        if (!validateEmail(accountModel.getEmail())) {
            // TODO proper error class? or move to exceptions from registration service?
            return ResponseEntity.badRequest().body("Invalid email");
        }
        if (!validatePassword(accountModel.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid password");
        }

        if (accountService.isRegistered(accountModel.getEmail())) {
            return ResponseEntity.badRequest().body("Account already exists");
        }
        accountService.registerAccount(accountModel);

        return ResponseEntity.ok("Registered");
    }

    @GetMapping("/api/register")
    public List<Account> get() {
        return repository.findAll();
    }

    @GetMapping("/api/verify")
    public ResponseEntity<Object> verify(@RequestParam(name = "token") String token) {
        if (accountService.verifyAccount(token)) {
            return ResponseEntity.ok("Successfully verified!");
        }
        return ResponseEntity.badRequest().body("Invalid token!");
    }

    private boolean validateEmail(String email) {
        return email != null && email.contains("@");
    }

    private boolean validatePassword(String password) {
        return password != null && password.length() >= 8; // TODO password strength and other requirements?
    }
}
