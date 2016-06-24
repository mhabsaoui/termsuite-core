package org.ttc.project.test.engines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;
import org.ttc.project.TermFactory;

import eu.project.ttc.engines.GraphicalVariantGatherer;
import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.resources.MemoryTermIndexManager;
import eu.project.ttc.resources.TermIndexResource;

public class GraphicalVariantGathererSpec {
	
	
	private TermIndex termIndex;
	private Term tetetete;
	private Term tetetetx;
	private Term teteteteAccent;
	private Term abcdefghijkl;
	private Term abcdefghijkx;
	private Term abcdefghijklCapped;
	
	
	@Before
	public void setup() {
		this.termIndex = termIndex();
	}
	
	
	private TermIndex termIndex() {
		MemoryTermIndexManager manager = MemoryTermIndexManager.getInstance();
		manager.clear();
		TermIndex termIndex = Fixtures.emptyTermIndex();
		manager.register(termIndex);
		TermFactory termFactory = new TermFactory(termIndex);
		tetetete = termFactory.create("N:tetetete|tetetete");
		tetetetx = termFactory.create("N:tetetetx|tetetetx");
		teteteteAccent = termFactory.create("N:tétetete|tétetete");
		abcdefghijklCapped = termFactory.create("N:Abcdefghijkl|Abcdefghijkl");
		abcdefghijkl = termFactory.create("N:abcdefghijkl|abcdefghijkl");
		abcdefghijkx = termFactory.create("N:abcdefghijkx|abcdefghijkx");
		return termIndex;
	}


	private AnalysisEngine makeAE(Lang lang, float similarityThreashhold) throws Exception {
		AnalysisEngineDescription aeDesc = AnalysisEngineFactory.createEngineDescription(
				GraphicalVariantGatherer.class,
				GraphicalVariantGatherer.LANG, lang.getCode(),
				GraphicalVariantGatherer.SIMILARITY_THRESHOLD, similarityThreashhold
			);
		
		/*
		 * The term index resource
		 */
		ExternalResourceDescription termIndexDesc = ExternalResourceFactory.createExternalResourceDescription(
				TermIndexResource.TERM_INDEX,
				TermIndexResource.class, 
				this.termIndex.getName()
		);
		ExternalResourceFactory.bindResource(aeDesc, termIndexDesc);

		AnalysisEngine ae = AnalysisEngineFactory.createEngine(aeDesc);
		return ae;
	}

	@Test
	public void testCaseInsensitive() throws  Exception {
		makeAE(Lang.FR, 1.0f).collectionProcessComplete();
		assertThat(this.abcdefghijkl.getBases()).hasSize(1)
		.extracting("base")
		.contains(this.abcdefghijklCapped);
		assertThat(this.abcdefghijkl.getVariations()).hasSize(0);
		
		assertThat(this.abcdefghijklCapped.getVariations())
			.hasSize(1)
			.extracting("variant")
			.contains(this.abcdefghijkl);
		assertThat(this.abcdefghijklCapped.getBases()).hasSize(0);
	}


	@Test
	public void testWithDiacritics() throws AnalysisEngineProcessException, Exception {
		makeAE(Lang.FR, 1.0f).collectionProcessComplete();
		assertThat(this.tetetete.getVariations())
			.hasSize(1)
			.extracting("variationType", "variant")
			.contains(tuple(VariationType.GRAPHICAL, this.teteteteAccent));
	}

	@Test
	public void testWith0_9() throws AnalysisEngineProcessException, Exception {
		makeAE(Lang.FR, 0.9f).collectionProcessComplete();
		assertThat(this.abcdefghijklCapped.getVariations())
			.hasSize(2)
			.extracting("variant")
			.contains(this.abcdefghijkl, this.abcdefghijkx);
		
		assertThat(this.tetetete.getVariations())
			.hasSize(1)
			.extracting("variationType", "variant")
			.contains(
					tuple(VariationType.GRAPHICAL, this.teteteteAccent)
					);
	}

	
	@Test
	public void testWith0_8() throws AnalysisEngineProcessException, Exception {
		makeAE(Lang.FR, 0.8f).collectionProcessComplete();
		assertThat(this.abcdefghijklCapped.getVariations())
			.hasSize(2)
			.extracting("variant")
			.contains(this.abcdefghijkl, this.abcdefghijkx);
		
		assertThat(this.tetetete.getVariations())
			.hasSize(2)
			.extracting("variationType", "variant")
			.contains(
					tuple(VariationType.GRAPHICAL, this.teteteteAccent),
					tuple(VariationType.GRAPHICAL, this.tetetetx)
					);

	}

}