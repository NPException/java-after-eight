package org.codefx.java_after_eight.genealogists.tags;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.RelationType;
import org.codefx.java_after_eight.genealogist.TypedRelation;

import static java.lang.Math.round;
import static java.util.stream.Collectors.toUnmodifiableSet;

public class TagGenealogist implements Genealogist {

	private static final RelationType TYPE = RelationType.from("tag");

	@Override
	public TypedRelation infer(Article article1, Article article2) {
		var article2Tags = article2.tags().collect(toUnmodifiableSet());
		long numberOfSharedTags = article1
				.tags()
				.filter(article2Tags::contains)
				.count();
		long numberOfArticle1Tags = article1.tags().count();
		long score = round((100.0 * 2 * numberOfSharedTags) / (numberOfArticle1Tags + article2Tags.size()));
		return TypedRelation.from(article1, article2, TYPE, score);
	}

}
