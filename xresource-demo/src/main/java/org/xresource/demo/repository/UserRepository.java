package org.xresource.demo.repository;

import org.xresource.core.annotations.XJSONFormFieldValidator;
import org.xresource.core.annotations.XJSONFormValidatorRule;
import org.xresource.core.annotations.XJSONFormValidatorType;
import org.xresource.core.annotations.XJSONFormValidators;
import org.xresource.core.annotations.XQueries;
import org.xresource.core.annotations.XQuery;
import org.xresource.demo.entity.Team;
import org.xresource.demo.entity.User;

import io.swagger.v3.oas.annotations.Hidden;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
// @XResourceAuthGroups(value = { @XResourceAuthGroup(role = "*", access =
// AccessLevel.NONE) }) //
// users
@XQueries(value = {
                @XQuery(name = "filterByTeam", where = "team.teamId=:context_loggeduser_teamId", contextParams = {
                                "context_loggeduser_teamId" }, autoApply = true, appliesToRoles = { "ROLE_USER" }),
                @XQuery(name = "filterByEmail", where = "email=:email", contextParams = { "email" })
})
@XJSONFormValidators(value = {
                @XJSONFormFieldValidator(name = "firstName", rules = {
                                @XJSONFormValidatorRule(type = XJSONFormValidatorType.MAX_LENGTH, value = "100")
                })
})
public interface UserRepository extends JpaRepository<User, String> {

        Optional<User> findByEmail(String email);

        Optional<List<User>> findByTeam(Team team);

        Optional<User> findByUserId(String userId); // Explicit finder for userId
}
