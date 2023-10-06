package homerep.springy.model;

import homerep.springy.util.ValidationUtil;

public record AccountModel(String email, String password) implements Model {
    @Override
    public boolean isValid() {
        return ValidationUtil.validateEmail(email) && validatePassword(password);
    }

    private static boolean validatePassword(String password) {
        return password != null && password.length() >= 8; // TODO password strength and other requirements?
    }
}
