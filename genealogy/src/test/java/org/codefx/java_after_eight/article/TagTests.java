package org.codefx.java_after_eight.article;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TagTests {

	@Test
	void emptyElementArray_emptyTag() {
		var tagsText = "[ ]";
		var expectedTags = new String[]{ };

		var tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void singleElementArray_singleTag() {
		var tagsText = "[$TAG]";
		var expectedTags = new String[]{ "$TAG" };

		var tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArray_multipleTags() {
		var tagsText = "[$TAG,$TOG,$TUG]";
		var expectedTags = new String[]{ "$TAG", "$TOG", "$TUG" };

		var tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayWithSpaces_multipleTagsWithoutSpaces() {
		var tagsText = "[$TAG ,  $TOG , $TUG  ]";
		var expectedTags = new String[]{ "$TAG", "$TOG", "$TUG" };

		var tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayWithJustSpacesTag_emptyTagIsIgnored() {
		var tagsText = "[$TAG ,  , $TUG  ]";
		var expectedTags = new String[]{ "$TAG", "$TUG" };

		var tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayWithEmptyTag_emptyTagIsIgnored() {
		var tagsText = "[$TAG ,, $TUG  ]";
		var expectedTags = new String[]{ "$TAG", "$TUG" };

		var tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

	@Test
	void multipleElementsArrayDuplicateTags_duplicateTagIsIgnored() {
		var tagsText = "[$TAG, $TAG]";
		var expectedTags = new String[]{ "$TAG" };

		var tags = Tag.from(tagsText);

		assertThat(tags)
				.extracting(Tag::text)
				.containsExactlyInAnyOrder(expectedTags);
	}

}
