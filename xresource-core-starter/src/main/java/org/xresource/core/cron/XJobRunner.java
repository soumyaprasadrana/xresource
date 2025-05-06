package org.xresource.core.cron;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import org.xresource.core.annotation.XQuery;
import org.xresource.core.model.XResourceMetadata;
import org.xresource.core.query.XQueryContextProvider;
import org.xresource.core.query.XQueryExecutor;
import org.xresource.core.registry.XResourceMetadataRegistry;

import jakarta.annotation.PostConstruct;

@Component
@Order(2)
public class XJobRunner implements ApplicationRunner {

    private final XJobRegistry registry;
    private final XQueryExecutor executor;
    private final XResourceMetadataRegistry resourceRegistry;
    private final XQueryContextProvider xQueryProvider;

    private final ConcurrentTaskScheduler scheduler = new ConcurrentTaskScheduler();

    @Autowired
    public XJobRunner(
            XJobRegistry registry,
            XQueryExecutor executor,
            XResourceMetadataRegistry resourceRegistry,
            XQueryContextProvider xQueryProvider
    ) {
        this.registry = registry;
        this.executor = executor;
        this.resourceRegistry = resourceRegistry;
        this.xQueryProvider = xQueryProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("🚀 XJobRunner starting...");

        for (XRegisteredJob job : registry.getAllJobs()) {
            Runnable task = () -> {
                try {
                    List<?> data = Collections.emptyList();

                    if (!job.getResource().isEmpty() && !job.getQuery().isEmpty()) {
                        XResourceMetadata metadata = resourceRegistry.get(job.getResource());
                        Optional<XQuery> optQuery = metadata.getXQuery(job.getQuery());

                        if (optQuery.isEmpty()) {
                            System.err.println("⚠️ XQuery not found: " + job.getQuery());
                            return;
                        }

                        XQuery xQuery = optQuery.get();
                        Map<String, Object> context = xQueryProvider.getEmptyContext(); // or dynamic context building
                        data = executor.executeQuery(metadata.getEntityClass(), xQuery, context);
                    }

                    job.getTask().run(data);

                } catch (Exception ex) {
                    System.err.println("❌ Error in XJob '" + job.getName() + "': " + ex.getMessage());
                    ex.printStackTrace();
                }
            };

            scheduler.schedule(task, new CronTrigger(job.getCron()));
            System.out.println("📅 Scheduled job: " + job.getName() + " -> " + job.getCron());
        }
    }
}
