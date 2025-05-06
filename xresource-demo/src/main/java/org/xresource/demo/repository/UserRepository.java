package org.xresource.demo.repository;

import org.xresource.demo.config.AuthorizationRoles;
import org.xresource.demo.entity.User;

import org.xresource.core.annotation.AccessLevel;
import org.xresource.core.annotation.XJSONFormFieldValidaor;
import org.xresource.core.annotation.XJSONFormValidatorRule;
import org.xresource.core.annotation.XJSONFormValidatorType;
import org.xresource.core.annotation.XJSONFormValidators;
import org.xresource.core.annotation.XQueries;
import org.xresource.core.annotation.XQuery;
import org.xresource.core.annotation.XResource;
import org.xresource.core.annotation.XResourceAuthGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@XResource(table = "user")
@XResourceAuthGroup(role = "ROLE_ANONYMOUS", access = AccessLevel.NONE) // users
@XQueries(value = {
        @XQuery(name = "filterByTeam", where = "team.teamId=:context_loggeduser_teamId", contextParams = {
                "context_loggeduser_teamId" }, autoApply = true, appliesToRoles = { "ROLE_USER" }),
})
@XJSONFormValidators(value = {
        @XJSONFormFieldValidaor(name = "email", rules = {
                @XJSONFormValidatorRule(type = XJSONFormValidatorType.EMAIL)
        }),
        @XJSONFormFieldValidaor(name = "firstName", rules = {
                @XJSONFormValidatorRule(type = XJSONFormValidatorType.MAX_LENGTH, value = "100")
        })
})
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserId(String userId); // Explicit finder for userId
}
