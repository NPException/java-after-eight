package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.genealogist.TypedRelation;

import java.util.stream.Stream;

import static java.lang.Math.round;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.averagingDouble;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.teeing;
import static org.codefx.java_after_eight.Utils.collectEqualElement;

public record Relation(Article article1,Article article2,long score) {

public Relation {
		requireNonNull(article1);
		requireNonNull(article2);
		if (score < 0 || 100 < score)
		throw new IllegalArgumentException("Score should be in interval [0; 100]: " + toString());
		}

static Relation aggregate(Stream<TypedRelation> typedRelations, Weights weights) {
		record Articles(Article article1,Article article2) {}
		return typedRelations.collect(
		teeing(
		mapping(
		rel -> new Articles(rel.article1(), rel.article2()),
		collectEqualElement()),
		averagingDouble(rel -> rel.score() * weights.weightOf(rel.type())),
		(articles, score) -> articles
		.map(arts -> new Relation(arts.article1(), arts.article2, round(score)))
		))
		.orElseThrow(() -> new IllegalArgumentException("Can't create relation from zero typed relations."));
		}

		}
