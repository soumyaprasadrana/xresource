package org.xresource.demo.validations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.xresource.core.validation.ValidationContext;
import org.xresource.core.validation.XValidator;
import org.xresource.core.validation.XValidatorRegistry;
import org.xresource.demo.entity.User;
import org.xresource.core.exception.XValidationException;

import jakarta.annotation.PostConstruct;

@Component
public class UserPassValidator implements XValidator {
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private XValidatorRegistry xValidatorRegistry;

    @PostConstruct
    public void addSelf() {
        this.xValidatorRegistry.register(User.class, this);
    }

    @Override
    public void validate(Object entity, ValidationContext context) throws XValidationException {
        try {
            User user = (User) entity;
            String password = user.getUserPass();

            boolean valid = true;
            StringBuilder error = new StringBuilder("Password requirements not met: ");

            if (password.length() < 8) {
                error.append("minimum 8 characters, ");
                valid = false;
            }
            if (!password.matches(".*[A-Z].*")) {
                error.append("one uppercase letter, ");
                valid = false;
            }
            if (!password.matches(".*[a-z].*")) {
                error.append("one lowercase letter, ");
                valid = false;
            }
            if (!password.matches(".*\\d.*")) {
                error.append("one digit, ");
                valid = false;
            }
            if (!password.matches(".*[^a-zA-Z0-9].*")) {
                error.append("one special character, ");
                valid = false;
            }

            if (!valid) {
                // throw new RuntimeException("User Pass is not valid");
                context.addViolation("userPass", "Field 'userPass': " +
                        error.toString().replaceAll(", $", "."), null, password, "Password");
                return;
            }

            // Encode and update the password
            String encoded = passwordEncoder.encode(password);
            user.setUserPass(encoded);

        } catch (Exception e) {
            context.addViolation("userPass", e.getMessage(), e, null, "UserPass");
        }
    }

}
