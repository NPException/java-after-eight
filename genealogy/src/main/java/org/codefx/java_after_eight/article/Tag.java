package org.codefx.java_after_eight.article;

import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;

public record Tag(String text) {

	// use static factory method(s)
	@Deprecated
	public Tag {
		requireNonNull(text);
		if (text.isBlank())
			throw new IllegalArgumentException("Tags can't have an empty text.");
	}

	static Set<Tag> from(String tagsText) {
		return Stream.of(tagsText
				.replaceAll("^\\[|\\]$", "")
				.split(","))
				.map(String::strip)
				.filter(not(String::isBlank))
				.map(Tag::new).collect(java.util.stream.Collectors.toUnmodifiableSet());
	}

}
