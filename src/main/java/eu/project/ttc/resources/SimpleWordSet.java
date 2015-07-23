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
package eu.project.ttc.resources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import eu.project.ttc.utils.TermSuiteConstants;

public class SimpleWordSet implements SharedResourceObject {
	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleWordSet.class);

	private Set<String> elements;
	
	public void load(DataResource data) throws ResourceInitializationException {
		InputStream inputStream;
		this.elements = Sets.newHashSet();
		try {
			inputStream = data.getInputStream();
			Scanner scanner = null;
			try {
				scanner = new Scanner(inputStream);
				scanner.useDelimiter(TermSuiteConstants.LINE_BREAK);
				while (scanner.hasNext()) {
					String line = scanner.next().split(TermSuiteConstants.DIESE)[0].trim();
					if(!line.isEmpty())
						this.elements.add(line);
				}
				this.elements = ImmutableSet.copyOf(this.elements);
			} catch (Exception e) {
				e.printStackTrace();
				throw new ResourceInitializationException(e);
			} finally {
				IOUtils.closeQuietly(scanner);
			}
		} catch (IOException e) {
			LOGGER.error("Could not load file {}",data.getUrl());
			throw new ResourceInitializationException(e);
		}
	}

	public boolean contains(Object o) {
		return elements.contains(o);
	}

	public boolean containsAll(Collection<?> c) {
		return elements.containsAll(c);
	}

	public Set<String> getElements() {
		return this.elements;
	};
	
}
