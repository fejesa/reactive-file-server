package io.fileserver.ext;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.lang.invoke.MethodHandles;

@ApplicationScoped
public class FSLifecycleBean {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    void onStart(@Observes StartupEvent ev) {
        logger.info("The File server is starting...");
    }

    void onStop(@Observes ShutdownEvent ev) {
        logger.info("The File Server is stopping...");
    }

}