package eu.project.ttc.models.scored;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;

import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermOccurrence;
import eu.project.ttc.models.TermVariation;
import eu.project.ttc.resources.ScoredModel;
import eu.project.ttc.utils.TermOccurrenceUtils;
import eu.project.ttc.utils.TermUtils;

public class ScoredTerm extends ScoredTermOrVariant {

	private List<ScoredVariation> variations;
	
	public ScoredTerm(ScoredModel scoredModel, Term t) {
		super(scoredModel, t);
	}
	
	public void setVariations(Iterable<ScoredVariation> variations) {
		this.variations = Lists.newLinkedList(variations);
	}

	public List<ScoredVariation> getVariations() {
		return Collections.unmodifiableList(this.variations);
	}
	
	private double independance = -1;

	
	/**
	 * 
	 * The ratio of this term appearance without any of its variants
	 * 
	 * @return
	 * 		The ratio of this term appearance without any of its variants.
	 */
	public double getIndependanceScore() {
		if(independance == -1) {
			Collection<TermOccurrence> occs = Lists.newLinkedList(getTerm().getOccurrences());
			for(TermVariation tv:getTerm().getVariations()) {
				TermOccurrenceUtils.removeOverlaps(tv.getVariant().getOccurrences(), occs);
			}
			independance = ((double)occs.size())/this.getTerm().getFrequency();
		}
		return independance;
	}
	
	private int maxFrequency = Integer.MIN_VALUE;
	
	public int getMaxVariationFrequency() {
		if(maxFrequency == Integer.MIN_VALUE) {
			for(ScoredVariation tv:variations)
				if(tv.getFrequency() > maxFrequency)
					maxFrequency = tv.getFrequency();
		}
		return maxFrequency;
	}

	public void reset() {
		super.reset();
		maxFrequency = Integer.MIN_VALUE;
		maxExtensionAffixWRLog = Double.MIN_VALUE;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ScoredTerm) {
			ScoredTerm st = (ScoredTerm) obj;
			return Objects.equal(this.getTerm(), st.getTerm());
		} else
			return false;
	}

	private Double maxExtensionAffixWRLog = Double.MIN_VALUE;
	
	public double getMaxExtensionAffixWRLog() {
		if(maxExtensionAffixWRLog == Double.MIN_VALUE) {
			maxExtensionAffixWRLog = 0.00001d;
			for(ScoredVariation tv:variations)
				if(tv.getExtensionAffix() == null)
					continue;
				else if(tv.getExtensionAffix().getWRLog() > maxExtensionAffixWRLog)
					maxExtensionAffixWRLog = tv.getExtensionAffix().getWRLog();
		}
		return maxExtensionAffixWRLog;

	}
	
	


}
