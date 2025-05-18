package org.xresource.internal.cron;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.xresource.core.annotations.XQuery;
import org.xresource.core.logging.XLogger;
import org.xresource.internal.query.XQueryContextProvider;
import org.xresource.internal.query.XQueryExecutor;
import org.xresource.internal.models.XResourceMetadata;
import org.xresource.internal.registry.XResourceMetadataRegistry;

public class XJobRunner implements ApplicationRunner {

    private final XJobRegistry registry;
    private final XQueryExecutor executor;
    private final XResourceMetadataRegistry resourceRegistry;
    private final XQueryContextProvider xQueryProvider;
    private final XLogger log = XLogger.forClass(XJobRunner.class);

    private final ConcurrentTaskScheduler scheduler = new ConcurrentTaskScheduler();

    @Autowired
    public XJobRunner(
            XJobRegistry registry,
            XQueryExecutor executor,
            XResourceMetadataRegistry resourceRegistry,
            XQueryContextProvider xQueryProvider) {
        this.registry = registry;
        this.executor = executor;
        this.resourceRegistry = resourceRegistry;
        this.xQueryProvider = xQueryProvider;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("🚀 XJobRunner starting...");

        for (XRegisteredJob job : registry.getAllJobs()) {
            Runnable task = () -> {
                try {
                    List<?> data = Collections.emptyList();

                    if (!job.getResource().isEmpty() && !job.getQuery().isEmpty()) {
                        XResourceMetadata metadata = resourceRegistry.get(job.getResource());
                        if (metadata == null) {
                            // try from ignored resource
                            metadata = resourceRegistry.getEmbeddedResource(job.getResource());
                        }
                        if (metadata == null) {
                            metadata = resourceRegistry.getCronResource(job.getResource());
                        }

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
            log.info("📅 Scheduled job: " + job.getName() + " -> " + job.getCron());
        }
    }
}
