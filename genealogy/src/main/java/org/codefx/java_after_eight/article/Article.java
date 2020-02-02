package org.codefx.java_after_eight.article;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public record Article(Title title,Set<Tag>tags,LocalDate date,Description description,Slug slug,Content content) {

	public Article {
		requireNonNull(title);
		requireNonNull(tags);
		requireNonNull(date);
		requireNonNull(description);
		requireNonNull(slug);
		requireNonNull(content);
	}

	public Set<Tag> tags() {
		return Set.copyOf(tags);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		return (o instanceof Article article) && slug.equals(article.slug);
	}

	@Override
	public int hashCode() {
		return Objects.hash(slug);
	}

}
