
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

package eu.project.ttc.test;

import org.apache.uima.jcas.JCas;

import eu.project.ttc.test.func.TermAssert;
import eu.project.ttc.test.func.TermIndexAssert;
import eu.project.ttc.test.unit.CasAssert;
import eu.project.ttc.test.unit.StringAssert;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermIndex;

public class TermSuiteAssertions {

	public static CasAssert assertThat(JCas cas) {
		return new CasAssert(cas);
	}

	public static TermAssert assertThat(Term term) {
		return new TermAssert(term);
	}
	
	public static TermIndexAssert assertThat(TermIndex termIndex) {
		return new TermIndexAssert(termIndex);
	}
	
	public static StringAssert assertThat(String string) {
		return new StringAssert(string);
	}

}
