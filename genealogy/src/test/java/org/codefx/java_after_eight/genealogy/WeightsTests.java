package org.codefx.java_after_eight.genealogy;

import org.codefx.java_after_eight.genealogist.RelationType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WeightsTests {

	public static final RelationType TAG_TYPE = RelationType.from("tag");
	public static final RelationType LIST_TYPE = RelationType.from("list");

	@Test
	void nullRelationType_throwsException() {
		var weightMap = new HashMap<RelationType, Double>();
		weightMap.put(null, 1.0);
		assertThatThrownBy(() -> Weights.from(weightMap, 0.5)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void nullWeight_throwsException() {
		var weightMap = new HashMap<RelationType, Double>();
		weightMap.put(TAG_TYPE, null);
		assertThatThrownBy(() -> Weights.from(weightMap, 0.5)).isInstanceOf(NullPointerException.class);
	}

	@Test
	void knownRelationType_returnsWeight() {
		var weights = Weights.from(Map.of(TAG_TYPE, 0.42), 0.5);

		assertThat(weights.weightOf(TAG_TYPE)).isEqualTo(0.42);
	}

	@Test
	void unknownRelationType_returnsDefaultWeight() {
		var weights = Weights.from(Map.of(TAG_TYPE, 0.42), 0.5);

		assertThat(weights.weightOf(LIST_TYPE)).isEqualTo(0.5);
	}

}
