package io.reactivefs.io;

import java.nio.file.Path;

public record FileContent(Path path, byte[] content) {}