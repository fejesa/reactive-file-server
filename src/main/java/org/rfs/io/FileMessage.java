package org.rfs.io;

import java.nio.file.Path;

record FileMessage(Path path, String message) {}