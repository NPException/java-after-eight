package org.codefx.java_after_eight;

import org.codefx.java_after_eight.article.Article;
import org.codefx.java_after_eight.article.ArticleFactory;
import org.codefx.java_after_eight.genealogist.Genealogist;
import org.codefx.java_after_eight.genealogist.GenealogistService;
import org.codefx.java_after_eight.genealogy.Genealogy;
import org.codefx.java_after_eight.genealogy.Weights;
import org.codefx.java_after_eight.recommendation.Recommendation;
import org.codefx.java_after_eight.recommendation.Recommender;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toUnmodifiableList;

public class Main {

	public static void main(String[] args) {
		System.out.println(ProcessDetails.details());

		var config = Config.create(args).join();
		var genealogy = createGenealogy(config.articleFolder());
		var recommender = new Recommender();

		var relations = genealogy.inferRelations();
		var recommendations = recommender.recommend(relations, 3);
		var recommendationsAsJson = recommendationsToJson(recommendations);

		config.outputFile().ifPresentOrElse(
				outputFile -> Utils.uncheckedFilesWrite(outputFile, recommendationsAsJson),
				() -> System.out.println(recommendationsAsJson));
	}

	private static Genealogy createGenealogy(Path articleFolder) {
		var articles = Utils.uncheckedFilesList(articleFolder)
				.filter(Files::isRegularFile)
				.filter(file -> file.toString().endsWith(".md"))
				.map(ArticleFactory::createArticle)
				.collect(toUnmodifiableList());
		var genealogists = getGenealogists(articles);
		return new Genealogy(articles, genealogists, Weights.allEqual());
	}

	private static Collection<Genealogist> getGenealogists(Collection<Article> articles) {
		var genealogists = ServiceLoader
				.load(GenealogistService.class).stream()
				.map(ServiceLoader.Provider::get)
				.map(service -> service.procure(articles))
				.collect(toUnmodifiableList());
		if (genealogists.isEmpty())
			throw new IllegalArgumentException("No genealogists found.");
		return genealogists;
	}

	private static String recommendationsToJson(Stream<Recommendation> recommendations) {
		var frame = """
				[
				$RECOMMENDATIONS
				]
				""";
		var recommendation = """
					{
						"title": "$TITLE",
						"recommendations": [
				$RECOMMENDED_ARTICLES
						]
					}
				""";
		var recommendedArticle = """
				\t\t\t{ "title": "$TITLE" }""";

		var recs = recommendations
				.map(rec -> {
					String articles = rec
							.recommendedArticles()
							.map(recArt -> recArt.title().text())
							.map(recTitle -> recommendedArticle.replace("$TITLE", recTitle))
							.collect(joining(",\n"));
					return recommendation
							.replace("$TITLE", rec.article().title().text())
							.replace("$RECOMMENDED_ARTICLES", articles);
				})
				.collect(joining(",\n"));
		return frame.replace("$RECOMMENDATIONS", recs);
	}

}
