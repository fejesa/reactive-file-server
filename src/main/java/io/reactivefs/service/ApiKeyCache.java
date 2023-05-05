package io.reactivefs.service;

import io.reactivefs.RFSConfig;
import io.reactivefs.ext.DocumentAccessResourceService;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.enterprise.context.ApplicationScoped;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicReference;

@ApplicationScoped
public class ApiKeyCache {

    @RestClient
    DocumentAccessResourceService documentAccessService;

    /** Initial interval, wait for the first retry */
    @ConfigProperty(name = RFSConfig.RETRY_INITIAL_BACKOFF_MS, defaultValue = "200")
    int RETRY_INITIAL_BACKOFF_MS;

    /** The absolute time in millis that specifies when to give up the retry */
    @ConfigProperty(name = RFSConfig.RETRY_EXPIRATION_MS, defaultValue = "2000")
    int RETRY_EXPIRATION_MS;

    private AtomicReference<String> cache = new AtomicReference<>("");

    /**
     * This function verifies the provided <i>ApiKey</i> key. If the key has not been set previously,
     * it is validated by making a call to the access checker service (ACL).
     * If the value is found to be invalid, an error is thrown. If the value is valid, it is stored in the cache.
     *
     * @param apiKey The key that must be checked.
     * @return firing the result of the operation when completed, or a failure if the operation failed.
     * @throws IllegalArgumentException if the key is empty or not valid
     */
    public Uni<Void> checkOrSet(String apiKey) {
        return Uni.createFrom().item(apiKey)
            .onItem().transformToUni(this::isChanged)
            .onItem().transformToUni(changed -> validateOrSkip(apiKey, changed))
            .onItem().transformToUni(this::update);
    }

    private Uni<Void> update(String key) {
        return Uni.createFrom().item(key)
            .invoke(() -> cache.set(key)).replaceWithVoid();
    }

    private Uni<Boolean> isChanged(String key) {
        return Uni.createFrom().item(Unchecked.supplier(() -> {
            if (StringUtils.isBlank(key)) {
                throw new IllegalArgumentException("ApiKey must be set");
            }
            var cached = cache.get();
            return cached.isEmpty() || !cached.equals(key);
        }));
    }

    private Uni<String> validateOrSkip(String key, boolean validate) {
        return validate ?
                documentAccessService.validateApiKey(key)
                    .onFailure()
                    .retry()
                    .withBackOff(Duration.ofMillis(RETRY_INITIAL_BACKOFF_MS))
                    .expireIn(RETRY_EXPIRATION_MS)
                    .map(Unchecked.function(applicationAuth -> {
                        if (applicationAuth.authorized()) {
                            return key;
                        }
                        throw new IllegalArgumentException("Invalid ApiKey");
                    })) : Uni.createFrom().item(key);
    }
}
