package org.codefx.java_after_eight.genealogist;

import org.codefx.java_after_eight.article.Article;

import static java.util.Objects.requireNonNull;

public record TypedRelation(Article article1,Article article2,RelationType type,long score) {

	// use static factory method(s)
	@Deprecated
	public TypedRelation { }

	public static TypedRelation from(Article article1, Article article2, RelationType type, long score) {
		if (score < 0 || 100 < score)
			throw new IllegalArgumentException("Score should be in interval [0; 100]: " + score);
		return new TypedRelation(
				requireNonNull(article1),
				requireNonNull(article2),
				requireNonNull(type),
				score);
	}

}
