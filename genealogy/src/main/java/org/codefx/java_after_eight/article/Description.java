package org.codefx.java_after_eight.article;

import org.codefx.java_after_eight.Utils;

import static java.util.Objects.requireNonNull;

public record Description(String text) {

	// use static factory method(s)
	@Deprecated
	public Description { }

	static Description from(String text) {
		requireNonNull(text);
		var unquotedText = Utils.removeOuterQuotationMarks(text).strip();
		if (unquotedText.isBlank())
			throw new IllegalArgumentException("Description can't have an empty text.");
		return new Description(unquotedText);
	}

}
