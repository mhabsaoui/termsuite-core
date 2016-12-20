package fr.univnantes.termsuite.engines;

import java.util.stream.Collectors;

import org.apache.commons.lang.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.univnantes.termsuite.framework.TerminologyService;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.Terminology;

public class TermMerger {

	private static final Logger LOGGER = LoggerFactory.getLogger(TermMerger.class);
	
	private static final Double MERGING_THRESHOLD = 2d;
	public void mergeTerms(Terminology termino) {
		mergeGraphicalVariants(termino);
	}

	private void mergeGraphicalVariants(Terminology termino) {
		LOGGER.info("Merging graphical variations");
		final MutableInt nbMerged = new MutableInt(0);
		
		termino.getRelations(RelationType.VARIATION)
			.filter(rel -> rel.isPropertySet(RelationProperty.IS_GRAPHICAL)
							&& rel.isPropertySet(RelationProperty.IS_PREXATION)
							&& rel.isPropertySet(RelationProperty.IS_DERIVATION)
					)
			.filter(rel -> rel.getPropertyBooleanValue(RelationProperty.IS_GRAPHICAL)
								&& !rel.getPropertyBooleanValue(RelationProperty.IS_DERIVATION)
								&& !rel.getPropertyBooleanValue(RelationProperty.IS_PREXATION)
								)
			.filter(rel -> rel.getFrom().getFrequency() > rel.getTo().getFrequency())
			.filter(rel -> rel.getTo().getFrequency() > 0)
			.filter(rel -> (double)rel.getFrom().getFrequency() / rel.getTo().getFrequency() > MERGING_THRESHOLD)
			.collect(Collectors.toList())
			.forEach(rel -> {
				LOGGER.trace("Merging variant {} into variant {}", rel.getTo(), rel.getFrom());
				OccurrenceStore occStore = termino.getOccurrenceStore();
				for(TermOccurrence occ:occStore.getOccurrences(rel.getTo()))
					occ.setTerm(rel.getFrom());
				for(TermOccurrence o2:occStore.getOccurrences(rel.getTo()))
					occStore.addOccurrence(rel.getFrom(), o2.getSourceDocument().getUrl(), o2.getBegin(), o2.getEnd(), o2.getForm().getText());
				rel.getFrom().setFrequency(rel.getFrom().getFrequency() + rel.getTo().getFrequency());
				rel.getFrom().setFrequencyNorm(rel.getFrom().getFrequencyNorm() + rel.getTo().getFrequencyNorm());
				rel.getFrom().setGeneralFrequencyNorm(rel.getFrom().getGeneralFrequencyNorm() + rel.getTo().getGeneralFrequencyNorm());
				termino.removeTerm(rel.getTo());
				nbMerged.increment();
			});
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Nb merges operated: {}", nbMerged);
			LOGGER.debug("Number of terms in termino: {}, Number of variations in termino: {}", 
					termino.getTerms().size(),
					new TerminologyService(termino).variations().count());
		}
	}
}
