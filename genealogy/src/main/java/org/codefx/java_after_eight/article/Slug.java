package org.codefx.java_after_eight.article;

import static java.util.Objects.requireNonNull;

public record Slug(String value) implements Comparable<Slug> {

	// use static factory method(s)
	@Deprecated
	public Slug { }

	static Slug from(String value) {
		requireNonNull(value);
		if (value.isBlank())
			throw new IllegalArgumentException("Slugs can't have an empty value.");
		return new Slug(value);
	}

	@Override
	public int compareTo(Slug right) {
		return this.value.compareTo(right.value);
	}

}
