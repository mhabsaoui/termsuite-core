/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/
package fr.univnantes.termsuite.model.termino;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import fr.univnantes.termsuite.model.Component;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;
import fr.univnantes.termsuite.model.TermWord;
import fr.univnantes.termsuite.model.Word;
import fr.univnantes.termsuite.uima.engines.termino.morpho.CompoundUtils;
import fr.univnantes.termsuite.utils.CollectionUtils;
import fr.univnantes.termsuite.utils.Pair;
import fr.univnantes.termsuite.utils.TermSuiteConstants;
import fr.univnantes.termsuite.utils.TermUtils;

public class TermValueProviders {
	private static final Logger LOGGER = LoggerFactory.getLogger(TermValueProviders.class);
	
	private static final Collection<String> EMPTY_COLLECTION = Lists.newArrayList();
	
	private static final String PAIR_FORMAT = "%s+%s";

	public static final TermValueProvider TERM_SINGLE_WORD_LEMMA_PROVIDER = new AbstractTermValueProvider(TermIndexes.SINGLE_WORD_LEMMA) {
		
		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			if(term.isSingleWord())
				return Lists.newArrayList(term.getWords().get(0).getWord().getLemma());
			return EMPTY_COLLECTION;
		}
	};

	public static final TermValueProvider TERM_LEMMA_LOWER_CASE_PROVIDER = new AbstractTermValueProvider(TermIndexes.LEMMA_LOWER_CASE) {
		public java.util.Collection<String> getClasses(TermIndex termIndex, Term term) {
			return ImmutableList.of(term.getLemma().toLowerCase());
		};
	};

	public static final TermValueProvider TERM_NOCLASS_PROVIDER = new AbstractTermValueProvider(TermIndexes.TERM_NOCLASS) {
		private String value = "noclass";
		public java.util.Collection<String> getClasses(TermIndex termIndex, Term term) {
			return ImmutableList.of(value);
		};
	};

	public static final TermValueProvider WORD_SWT_GROUPING_KEYS_PROVIDER = new AbstractTermValueProvider(TermIndexes.WORD_LEMMA_IF_SWT) {
		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			List<String> swtGroupingKeys = Lists.newArrayListWithCapacity(term.getWords().size());
			for(TermWord tw:term.getWords()) {
				if(termIndex.getTermByGroupingKey(TermUtils.toGroupingKey(tw)) != null) 
					swtGroupingKeys.add(tw.toGroupingKey());
			}
			return swtGroupingKeys;
		}
	};

	public static final TermValueProvider WORD_LEMMA_IF_SWT_PROVIDER = new AbstractTermValueProvider(TermIndexes.WORD_LEMMA_IF_SWT) {
		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			List<String> lemmas = Lists.newArrayListWithCapacity(term.getWords().size());
			for(TermWord tw:term.getWords()) {
				if(termIndex.getTermByGroupingKey(TermUtils.toGroupingKey(tw)) != null) 
					lemmas.add(tw.getWord().getLemma());
			}
			return lemmas;
		}
	};


	
	public static final TermValueProvider WORD_LEMMA_STEM_PROVIDER = new AbstractTermValueProvider(TermIndexes.WORD_COUPLE_LEMMA_STEM) {

		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			List<String> lemmas = Lists.newArrayListWithCapacity(term.getWords().size());
			
			Map<String, String> stems = new HashMap<String, String>();
			for(TermWord w:term.getWords()) {
				if (w.getWord().getLemma() == null || w.getWord().getLemma().isEmpty()) {
					LOGGER.warn("lemma is null or empty: " + w);
					continue;
				} else if(TermSuiteConstants.TERM_MATCHER_LABELS.contains(w.getSyntacticLabel())) {
						lemmas.add(w.getWord().getNormalizedLemma());
					if(w.getWord().getStem() == null || w.getWord().getStem().isEmpty()) {
						LOGGER.warn("stem is null or empty: " + w);
					} else 
						stems.put(w.getWord().getNormalizedLemma(), w.getWord().getNormalizedStem());
				}
			}	
			Collections.sort(lemmas);
			List<String> keys = Lists.newArrayListWithCapacity(lemmas.size());
			
			for (int i = 0 ; i < lemmas.size(); i++) {
				for (int j = i + 1; j < lemmas.size(); j++) {
					StringBuilder sb = new StringBuilder();
					sb.append(lemmas.get(i));
					sb.append(TermSuiteConstants.PLUS);
					sb.append(stems.get(lemmas.get(j)));
					keys.add(sb.toString());
				}
			}
			

			return keys;
		}
	};
		
	/**
	 * Provides all lemma-lemma pairs found in the term.
	 * 
	 * Ex1: offshore wind energy
	 * 			--> {offshore+wind, energy+wind, offshore+energy}
	 * 
	 * Performs two iterations when there are compounds
	 * 
	 * Ex2: horizontal-axis wind turbine
	 * 			it1 --> {horizontal-axis+turbine, horizontal-axis+turbine, wind+turbine}
	 * 			it2 --> {axis+horizontal, axis+turbine, axis+wind, horizontal+turbine, horizontal+wind, wind+turbine}
	 * 			total -->  it1 U it2
	 */
	public static final TermValueProvider ALLCOMP_LEMMA_SUBSTRING_PAIRS = new AbstractTermValueProvider(TermIndexes.WORD_COUPLE_LEMMA_LEMMA) {

		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			Set<Pair<Component>> componentPairs = Sets.newHashSet();
			Set<Word> significantWords = Sets.newHashSetWithExpectedSize(term.getWords().size());
			
			/*
			 * 1- select significant words
			 */
			for(TermWord w:term.getWords()) {
				if(w.isSwt())
					significantWords.add(w.getWord());
			}
			
			/*
			 * 2- Adds intra-compound component pairs (for compound words only)
			 */
			significantWords.stream()
				.filter(Word::isCompound)
				.forEach(w -> componentPairs.addAll(CompoundUtils.innerComponentPairs(w)));
			
			/*
			 * 3- Add inter-word component pairs
			 */
			componentPairs.addAll(CollectionUtils.combineAndProduct(TermUtils.toComponentSets(significantWords)));
			
			
			/*
			 * 4- transform each component pair to class
			 */
			Set<String> classes = new HashSet<>();
				
			componentPairs.stream()
				.forEach(pair -> classes.addAll(CompoundUtils.toIndexStrings(pair)));
			return classes;
		}
	};

	/**
	 * Provides all lemmas of the terms
	 */
	public static final TermValueProvider WORD_LEMMA_PROVIDER = new AbstractTermValueProvider(TermIndexes.WORD_COUPLE_LEMMA_LEMMA) {

		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			Set<String> classes = Sets.newHashSet();
			for(TermWord w:term.getWords()) 
				classes.add(w.getWord().getLemma().toLowerCase());
			return classes;
			
		}
	};

	private static final Map<String, TermValueProvider> valueProviders = Maps.newHashMap();


	static {
		valueProviders.put(TermIndexes.SINGLE_WORD_LEMMA, TERM_SINGLE_WORD_LEMMA_PROVIDER);
		valueProviders.put(TermIndexes.TERM_NOCLASS, TERM_NOCLASS_PROVIDER);
		valueProviders.put(TermIndexes.SWT_GROUPING_KEYS, WORD_SWT_GROUPING_KEYS_PROVIDER);
		valueProviders.put(TermIndexes.WORD_LEMMA, WORD_LEMMA_PROVIDER);
		valueProviders.put(TermIndexes.LEMMA_LOWER_CASE, TERM_LEMMA_LOWER_CASE_PROVIDER);
		valueProviders.put(TermIndexes.WORD_COUPLE_LEMMA_STEM, WORD_LEMMA_STEM_PROVIDER);
		valueProviders.put(TermIndexes.WORD_COUPLE_LEMMA_LEMMA, ALLCOMP_LEMMA_SUBSTRING_PAIRS);
	}

	public static TermValueProvider get(String key) {
		return valueProviders.get(key);
	}

	/*
	 * TODO Bad design, unify model with all indexes even though
	 * 		that do not need TermIndex
	 */
	public static TermValueProvider get(String indexName, TermIndex termIndex) {
		switch(indexName) {
		case TermIndexes.TERM_HAS_PREFIX_LEMMA:
			return new SelectorTermValueProvider(
						TermIndexes.WORD_LEMMA,
						new HasSingleWordVariationSelector(RelationType.IS_PREFIX_OF), 
						termIndex
					);
		case TermIndexes.TERM_HAS_DERIVATES_LEMMA:
			return new SelectorTermValueProvider(
						TermIndexes.WORD_LEMMA,
						new HasSingleWordVariationSelector(RelationType.DERIVES_INTO), 
						termIndex
					);
		default:
			return get(indexName);
		}
	}
	
	
	static class SelectorTermValueProvider extends AbstractTermValueProvider {
		private static final String ERR_MSG_NO_SUCH_VALUE_PROVIDER = "No such TermValueProvider: %s";

		TermSelector selector;
		TermValueProvider termValueProvider;
		TermIndex termIndex;
		
		public SelectorTermValueProvider(String name, TermSelector selector, TermIndex termIndex) {
			super(name);
			this.termValueProvider = TermValueProviders.get(this.getName());
			Preconditions.checkNotNull(
					this.termValueProvider,
					ERR_MSG_NO_SUCH_VALUE_PROVIDER,
					this.getName());
			this.selector = selector;
			this.termIndex = termIndex;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Collection<String> getClasses(TermIndex termIndex, Term term) {
			if(selector.select(termIndex, term)) {
				return termValueProvider.getClasses(termIndex, term);
			} else 
				return Collections.EMPTY_SET;
		}
	}
}