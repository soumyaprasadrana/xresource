package org.xresource.internal.cron;

import java.util.Map;

import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.xresource.core.annotations.XCronJob;
import org.xresource.core.cron.XJobTask;

@Component
@Order(1)
public class XJobAutoRegistrar implements ApplicationRunner {
    private final ApplicationContext context;
    private final XJobRegistry registry;

    public XJobAutoRegistrar(ApplicationContext context, XJobRegistry registry) {
        this.context = context;
        this.registry = registry;
    }

    @Override
    public void run(ApplicationArguments args) {
        Map<String, XJobTask> beans = context.getBeansOfType(XJobTask.class);
        for (XJobTask bean : beans.values()) {

            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);

            // Retrieve the @XCronJob annotation from the real class
            XCronJob annotation = targetClass.getAnnotation(XCronJob.class);

            if (annotation != null) {
                registry.register(annotation.name(), new XRegisteredJob(
                        annotation.name(),
                        annotation.cron(),
                        annotation.description(),
                        annotation.resource(),
                        annotation.query(),
                        bean));
            } else {
                System.out.println(" No cron configuration found for a job task.");
            }
        }
        System.out.println("✅ XJobAutoRegistrar finished registering all jobs.");
    }
}
