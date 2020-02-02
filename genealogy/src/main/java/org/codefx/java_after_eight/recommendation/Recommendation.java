package org.codefx.java_after_eight.recommendation;

import org.codefx.java_after_eight.article.Article;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toUnmodifiableList;

public record Recommendation(Article article,List<Article>recommendedArticles) {

	public Recommendation {
		requireNonNull(article);
		requireNonNull(recommendedArticles);
	}

	static Recommendation from(Article article, Stream<Article> sortedRecommendations, int perArticle) {
		var recommendations = sortedRecommendations.limit(perArticle).collect(toUnmodifiableList());
		return new Recommendation(requireNonNull(article), recommendations);
	}

	public List<Article> recommendedArticles() {
		return List.copyOf(recommendedArticles);
	}

}
