package org.xresource.demo.cron;

import java.util.List;

import org.xresource.core.cron.XJobTask;

//@XCronJob(name = "sessionMonitor", cron = "*/10 * * * * *", description = "Monitor and remove expired sessions", resource = "session", query = "expiredSessions")
//@Component
public class SessionMonitor implements XJobTask {

    @Override
    public void run(List<?> data) {
        if (data.isEmpty()) {
            System.out.println("No Data Found");
        } else {
            System.out.println(data);
        }
    }

}
