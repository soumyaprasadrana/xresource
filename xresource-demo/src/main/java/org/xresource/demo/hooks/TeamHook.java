package org.xresource.demo.hooks;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xresource.core.hook.XResourceEventContext;
import org.xresource.core.hook.XResourceEventType;
import org.xresource.core.hook.XResourceHook;
import org.xresource.core.hook.XResourceHookRegistry;
import org.xresource.demo.entity.Team;
import org.xresource.demo.entity.User;
import org.xresource.demo.repository.UserRepository;

import jakarta.annotation.PostConstruct;

@Component

public class TeamHook implements XResourceHook {

    @Autowired
    private XResourceHookRegistry xResourceHookRegistry;

    @Autowired
    private UserRepository userRepo;

    @Override
    public void execute(XResourceEventContext context) {

        Team team = (Team) context.getResourceObject();
        // Check if there is any user left
        Optional<List<User>> teamUsers = userRepo.findByTeam(team);
        if (teamUsers.isPresent() && !teamUsers.get().isEmpty()) {
            System.out.println("Error Users still present");
        } else {
            System.out.println(" Users removed for the team");
        }
    }

    @PostConstruct
    public void register() {
        this.xResourceHookRegistry.registerHook("team", XResourceEventType.AFTER_DELETE, this);
    }

}
