package io.reactivefs.io;

import java.nio.file.Path;

record FileContent(Path path, String content) {}