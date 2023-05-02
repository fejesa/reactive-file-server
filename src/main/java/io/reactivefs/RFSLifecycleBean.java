package io.reactivefs;

import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;

@ApplicationScoped
public class RFSLifecycleBean {

    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    void onStart(@Observes StartupEvent ev) {
        logger.info("The File server is starting...");
    }

    void onStop(@Observes ShutdownEvent ev) {
        logger.info("The File Server is stopping...");
    }

}