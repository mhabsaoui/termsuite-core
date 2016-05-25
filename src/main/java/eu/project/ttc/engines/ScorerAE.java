package eu.project.ttc.engines;

import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ExternalResource;
import org.apache.uima.jcas.JCas;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.models.scored.ScoredModel;
import eu.project.ttc.models.scored.ScoredTerm;
import eu.project.ttc.models.scored.ScoredVariation;
import eu.project.ttc.resources.ObserverResource;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.termino.engines.VariantScorerConfig;
import eu.project.ttc.termino.engines.TermVariationScorer;

public class ScorerAE extends JCasAnnotator_ImplBase {
	private static final Logger logger = LoggerFactory.getLogger(ScorerAE.class);
	public static final String TASK_NAME = "Scorying of terms";

	@ExternalResource(key=ObserverResource.OBSERVER, mandatory=true)
	protected ObserverResource observerResource;
	
	@ExternalResource(key=TermIndexResource.TERM_INDEX, mandatory=true)
	private TermIndexResource termIndexResource;

	@Override
	public void process(JCas aJCas) throws AnalysisEngineProcessException {
		/*
		 * Do nothing
		 */
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		logger.info("Starting " + TASK_NAME);
		TermIndex termIndex = termIndexResource.getTermIndex();
		VariantScorerConfig defaultScorerConfig = termIndex.getLang().getScorerConfig();
		TermVariationScorer variantScorer = new TermVariationScorer(defaultScorerConfig);
		
		/*
		 * 1- compute scores
		 */
		logger.debug("Scorying");
		ScoredModel scoredModel = variantScorer.score(termIndex);
		
		/*
		 * 2- remove filtered terms
		 */
		logger.debug("Removing filtered terms");
		Set<Term> keptTerms = Sets.newHashSet();
		for(ScoredTerm st:scoredModel.getTerms())
			keptTerms.add(st.getTerm());
		Set<Term> remTerms = Sets.newHashSet();
		int size = termIndex.getTerms().size();
		for(Term t:termIndex.getTerms())
			if(!keptTerms.contains(t))
				remTerms.add(t);
		for(Term t:remTerms)
			termIndex.removeTerm(t);
		logger.debug("{} terms filtered (and removed from term index) out of {}", 
				remTerms.size(),
				size
				);
		
		/*
		 * 3- reset the sorted variations and unique bases
		 */
		logger.debug("Resetting scored variations and unique bases");
		for(ScoredTerm st:scoredModel.getTerms()) {
			TreeSet<TermVariation> sortedVariations = Sets.newTreeSet();
			for(ScoredVariation sv:st.getVariations()) {
				TermVariation termVariation = sv.getTermVariation();
				
				/*
				 * Add this term variation to the variants
				 */
				termVariation.setScore(sv.getVariationScore());
				sortedVariations.add(termVariation);
				
				/*
				 * Set this term variation as the unique base for the variant
				 */
				TreeSet<TermVariation> base = Sets.newTreeSet();
				base.add(termVariation);
				termVariation.getVariant().setBases(base);
			}
			/*
			 * Set the sorted set as the variants
			 */
			st.getTerm().setVariations(sortedVariations);
		}
	}
}