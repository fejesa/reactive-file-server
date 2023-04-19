package io.reactivefs.model;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

public record DocumentFileAccess(String userId, String organizationId, String fileName) implements Serializable {

    public DocumentFileAccess {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        if (organizationId == null) {
            throw new IllegalArgumentException("organizationId must not be null");
        }
        if (fileName == null) {
            throw new IllegalArgumentException("fileName must not be null");
        }
    }

    @Override
    public String toString() {
        return "DocumentFileAccess [userId=" + userId + ", organizationId=" + organizationId + ", fileName=" + fileName + "]";
    }

}
