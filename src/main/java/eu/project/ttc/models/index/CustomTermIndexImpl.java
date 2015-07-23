/*******************************************************************************
 * Copyright 2015 - CNRS (Centre National de Recherche Scientifique)
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
package eu.project.ttc.models.index;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Sets;

import eu.project.ttc.models.Term;

public class CustomTermIndexImpl implements CustomTermIndex {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(CustomTermIndexImpl.class);
	
	private ListMultimap<String, Term> index;

	private TermClassProvider classProvider;
	
	CustomTermIndexImpl(TermClassProvider classProvider) {
		super();
		this.classProvider = classProvider;
		this.index = ArrayListMultimap.create();
	}

	@Override
	public Collection<String> keySet() {
		return this.index.keySet();
	}

	@Override
	public List<Term> getTerms(String key) {
		return this.index.get(key);
	}

	@Override
	public void indexTerm(Term term) {
		for(String cls:classProvider.getClasses(term))
			this.index.put(cls, term);
	}

	@Override
	public void cleanSingletonKeys() {
		Iterator<String> it = this.index.keySet().iterator();
		while(it.hasNext())
			if(this.index.get(it.next()).size() == 1)
				it.remove();
	}

	@Override
	public int size() {
		return this.index.size();
	}

	@Override
	public void removeTerm(Term t) {
		for(String k:classProvider.getClasses(t))
			this.index.remove(k, t);
	}

	@Override
	public void dropBiggerEntries(int threshholdSize, boolean logWarning) {
		Set<String> toRemove = Sets.newHashSet();
		for(String key:index.keySet()) {
			if(index.get(key).size() >= threshholdSize)
				toRemove.add(key);
		}
		for(String rem:toRemove) {
			LOGGER.warn("Removing key {} from custom index {} because its size {} is bigger than the threshhold {}",
					rem,
					this.classProvider.getName(),
					this.index.get(rem).size(),
					threshholdSize);
			index.removeAll(rem);
		}
	}

}
