package eu.project.ttc.test.func.align;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import eu.project.ttc.test.func.FunctionalTests;
import fr.univnantes.termsuite.alignment.AlignmentMethod;
import fr.univnantes.termsuite.alignment.BilingualAligner;
import fr.univnantes.termsuite.alignment.TermSuiteAlignerBuilder;
import fr.univnantes.termsuite.alignment.TranslationCandidate;
import fr.univnantes.termsuite.api.TermIndexIO;
import fr.univnantes.termsuite.model.Lang;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;

public class BilingualAlignerDeEnSpec {

	TermIndex deTermino;
	TermIndex enTermino;
	BilingualAligner aligner;
	
	@Before
	public void setup() {
		deTermino = TermIndexIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.DE));
		enTermino = TermIndexIO.fromJson(FunctionalTests.getTerminoWEShortPath(Lang.EN));
		aligner = TermSuiteAlignerBuilder.start()
				.setSourceTerminology(deTermino)
				.setTargetTerminology(enTermino)
				.setDicoPath(FunctionalTests.getDicoPath(Lang.DE, Lang.EN))
				.setDistanceCosine()
				.create();
	}
	

	@Test
	public void testAlignerMWTComp() {
		Term t1 = deTermino.getTermByGroupingKey("n: windenergie");

		List<TranslationCandidate> results = aligner.alignSize2(t1, 3, 2);
		assertThat(results)
			.extracting("term.groupingKey", "method")
			.contains(
					tuple("nn: wind energy", AlignmentMethod.COMPOSITIONAL)
					)
			.hasSize(3)
			;
	}
	


}
