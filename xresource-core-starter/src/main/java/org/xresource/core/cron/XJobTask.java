package org.xresource.core.cron;

import java.util.List;

public interface XJobTask {

    void run(List<?> data);
}