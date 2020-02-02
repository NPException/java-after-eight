package org.codefx.java_after_eight;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public record Config(Path articleFolder,Optional<Path>outputFile) {

	private static final String CONFIG_FILE_NAME = ".recs.config";

	// use static factory method(s)
	@Deprecated
	public Config { }

	private static Config fromRawConfig(String[] raw) {
		if (raw.length == 0)
			throw new IllegalArgumentException("No article path defined.");

		Path articleFolder = Path.of(raw[0]);
		if (!Files.exists(articleFolder))
			throw new IllegalArgumentException("Article path doesn't exist: " + articleFolder);
		if (!Files.isDirectory(articleFolder))
			throw new IllegalArgumentException("Article path is no directory: " + articleFolder);

		Optional<String> outputFileString = raw.length >= 2
				? Optional.of(raw[1])
				: Optional.empty();
		Optional<Path> outputFile = outputFileString
				.map(file -> Path.of(System.getProperty("user.dir")).resolve(file));
		outputFile.ifPresent(file -> {
			boolean notWritable = Files.exists(file) && !Files.isWritable(file);
			if (notWritable)
				throw new IllegalArgumentException("Output path is not writable: " + file);
		});
		return new Config(articleFolder, outputFile);
	}

	public static CompletableFuture<Config> create(String[] args) {
		CompletableFuture<String[]> rawConfig = args.length > 0
				? CompletableFuture.completedFuture(args)
				: readProjectConfig()
						.exceptionally(__ -> readUserConfig().join())
						.exceptionally(__ -> new String[0]);

		return rawConfig
				.thenApply(Config::fromRawConfig);
	}

	private static CompletableFuture<String[]> readProjectConfig() {
		Path workingDir = Path.of(System.getProperty("user.dir")).resolve(CONFIG_FILE_NAME);
		return readConfig(workingDir);
	}

	private static CompletableFuture<String[]> readUserConfig() {
		Path workingDir = Path.of(System.getProperty("user.home")).resolve(CONFIG_FILE_NAME);
		return readConfig(workingDir);
	}

	private static CompletableFuture<String[]> readConfig(Path workingDir) {
		return CompletableFuture.supplyAsync(() -> {
			try {
				return Files.readAllLines(workingDir).toArray(String[]::new);
			} catch (IOException ex) {
				throw new UncheckedIOException(ex);
			}
		});
	}

}
