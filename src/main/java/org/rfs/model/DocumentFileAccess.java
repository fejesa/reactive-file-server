package org.rfs.model;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

public record DocumentFileAccess(String userId, String organizationId, String fileName) implements Serializable {

    public DocumentFileAccess {
        if (StringUtils.isBlank(userId)) {
            throw new IllegalArgumentException("userId must not be null or blank");
        }
        if (StringUtils.isBlank(organizationId)) {
            throw new IllegalArgumentException("organizationId must not be null or blank");
        }
        if (StringUtils.isBlank(fileName)) {
            throw new IllegalArgumentException("fileName must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return "DocumentFileAccess [userId=" + userId + ", organizationId=" + organizationId + ", fileName=" + fileName + "]";
    }

}
