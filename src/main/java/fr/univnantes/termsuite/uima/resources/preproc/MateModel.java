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
package fr.univnantes.termsuite.uima.resources.preproc;

import org.apache.uima.resource.DataResource;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.SharedResourceObject;

/**
 * 
 * A thread-safe mate engine. 
 * 
 * The Mate engine is considered as a UIMA resource, since the only way 
 * to load resources in Mate is to create an instance of the engine with
 * its model given as a constructor parameter. 
 * 
 * @author Damien Cram
 *
 */
public abstract class MateModel<T> implements SharedResourceObject {
	public ThreadLocal<T> mateEngine = new ThreadLocal<T>();
	
	@Override
	public void load(DataResource aData) throws ResourceInitializationException {
		String url = aData.getUri().toString().replaceFirst("file:", "");
		if(mateEngine.get() == null) {
			mateEngine.set(instanciate(url));
		}
	}
	
	protected abstract T instanciate(String url);

	public T getEngine() {
		return mateEngine.get();
	}
	
}
