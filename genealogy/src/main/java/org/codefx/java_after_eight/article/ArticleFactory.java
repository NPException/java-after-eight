package org.codefx.java_after_eight.article;

import org.codefx.java_after_eight.Utils;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;

public final class ArticleFactory {

	private static final String TITLE = "title";
	private static final String TAGS = "tags";
	public static final String DATE = "date";
	private static final String DESCRIPTION = "description";
	private static final String SLUG = "slug";
	public static final String FRONT_MATTER_SEPARATOR = "---";

	private ArticleFactory() {
		// private constructor to prevent accidental instantiation of utility class
	}

	public static Article createArticle(Path file) {
		try {
			Stream<String> eagerLines = Utils.uncheckedFilesLines(file);
			Stream<String> frontMatter = extractFrontMatter(eagerLines);
			Content content = () -> {
				Stream<String> lazyLines = Utils.uncheckedFilesLines(file);
				return extractContent(lazyLines);
			};
			return createArticle(frontMatter, content);
		} catch (RuntimeException ex) {
			throw new RuntimeException("Creating article failed: " + file, ex);
		}
	}

	public static Article createArticle(List<String> fileLines) {
		Stream<String> frontMatter = extractFrontMatter(fileLines.stream());
		Content content = () -> extractContent(fileLines.stream());
		return createArticle(frontMatter, content);
	}

	private static Stream<String> extractFrontMatter(Stream<String> markdownFile) {
		return markdownFile
				.map(String::strip)
				.dropWhile(not(FRONT_MATTER_SEPARATOR::equals))
				.skip(1)
				.takeWhile(not(FRONT_MATTER_SEPARATOR::equals));
	}

	private static Stream<String> extractContent(Stream<String> markdownFile) {
		return markdownFile
				.dropWhile(line -> !line.strip().equals(FRONT_MATTER_SEPARATOR))
				.skip(1)
				.dropWhile(line -> !line.strip().equals(FRONT_MATTER_SEPARATOR))
				.skip(1);
	}

	public static Article createArticle(Stream<String> frontMatter, Content content) {
		Map<String, String> entries = frontMatter
				.map(FrontMatterLine::fromLine)
				.collect(toMap(FrontMatterLine::key, FrontMatterLine::value));
		return new Article(
				Title.from(entries.get(TITLE)),
				Tag.from(entries.get(TAGS)),
				LocalDate.parse(entries.get(DATE)),
				Description.from(entries.get(DESCRIPTION)),
				Slug.from(entries.get(SLUG)),
				content);
	}

	private record FrontMatterLine(String key,String value) {

		static FrontMatterLine fromLine(String line) {
			String[] pair = line.split(":", 2);
			if (pair.length < 2)
				throw new IllegalArgumentException("Line doesn't seem to be a key/value pair (no colon): " + line);
			String key = pair[0].strip().toLowerCase();
			if (key.isBlank())
				throw new IllegalArgumentException("Line \"" + line + "\" has no key.");

			String value = pair[1].strip();
			return new FrontMatterLine(key, value);
		}

	}

}
