package org.rfs.ext;

import java.io.Serializable;
import org.apache.commons.lang3.StringUtils;

public record DocumentFileAccess(String userid, String organization, String filename) implements Serializable {

    public DocumentFileAccess {
        if (StringUtils.isBlank(userid)) {
            throw new IllegalArgumentException("userid must not be null or blank");
        }
        if (StringUtils.isBlank(organization)) {
            throw new IllegalArgumentException("organization must not be null or blank");
        }
        if (StringUtils.isBlank(filename)) {
            throw new IllegalArgumentException("filename must not be null or blank");
        }
    }

    @Override
    public String toString() {
        return "DocumentFileAccess [userid=" + userid + ", organization=" + organization + ", filename=" + filename + "]";
    }

}
