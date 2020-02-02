package org.codefx.java_after_eight.article;

import org.codefx.java_after_eight.Utils;

import static java.util.Objects.requireNonNull;

public record Title(String text) {

	// use static factory method(s)
	@Deprecated
	public Title { }

	static Title from(String text) {
		requireNonNull(text);
		var unquotedText = Utils.removeOuterQuotationMarks(text);
		if (unquotedText.isBlank())
			throw new IllegalArgumentException("Titles can't have an empty text.");
		return new Title(unquotedText);
	}

}
