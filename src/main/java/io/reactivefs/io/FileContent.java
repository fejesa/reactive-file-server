package io.reactivefs.io;

import java.nio.file.Path;

record FileContent(Path path, byte[] content) {}