package org.xresource.demo.validations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xresource.core.validation.ValidationContext;
import org.xresource.core.validation.XValidator;
import org.xresource.core.validation.XValidatorRegistry;
import org.xresource.demo.entity.Authorization;
import org.xresource.demo.entity.User;
import org.xresource.core.exception.XValidationException;

import jakarta.annotation.PostConstruct;

@Component
public class UserAuthorizationValidator implements XValidator {

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
            Authorization authorization = user.getAuthorization();

            if (authorization == null) {
                context.addViolation("authorization", "Field 'authorization': required, can not be null.", null,
                        authorization, "NotNull");
                return;
            }
            // Set back reference
            if (user.getAuthorization() != null) {
                user.getAuthorization().setUser(null); // prevent bad mapping
                user.getAuthorization().setUserId(null); // ensure MapsId works
                user.getAuthorization().setUser(user);
            }
            authorization.setUser(user);

        } catch (Exception e) {
            context.addViolation("userPass", e.getMessage(), e, null, "UserPass");
        }
    }

}
