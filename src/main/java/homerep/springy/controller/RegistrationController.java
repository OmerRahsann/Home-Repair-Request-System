package homerep.springy.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import homerep.springy.model.RegisterModel;
import homerep.springy.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class RegistrationController {
    @Autowired
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/api/register")
    public ResponseEntity<Object> register(@RequestBody RegisterModel registerModel) throws JsonProcessingException {
        if (!registerModel.isValid()) {
            // TODO proper error class? or move to exceptions from registration service?
            // TODO Need proper error messages? Yes. TODO figure out how
            return ResponseEntity.badRequest().body("Invalid registration data");
        }

        if (accountService.isRegistered(registerModel.account().email())) {
            return ResponseEntity.badRequest().body("Account already exists");
        }
        accountService.registerAccount(registerModel);

        return ResponseEntity.ok("Registered");
    }

    @GetMapping("/api/verify")
    public ResponseEntity<Object> verify(@RequestParam(name = "token") String token) {
        if (accountService.verifyAccount(token)) {
            return ResponseEntity.ok("Successfully verified!");
        }
        return ResponseEntity.badRequest().body("Invalid token!");
    }

}
