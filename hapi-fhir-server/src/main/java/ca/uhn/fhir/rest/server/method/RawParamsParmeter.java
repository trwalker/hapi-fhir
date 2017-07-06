package ca.uhn.fhir.rest.server.method;

/*
 * #%L
 * HAPI FHIR - Core Library
 * %%
 * Copyright (C) 2014 - 2017 University Health Network
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.Validate;
import org.hl7.fhir.instance.model.api.IBaseResource;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.RawParam;
import ca.uhn.fhir.rest.api.server.RequestDetails;
import ca.uhn.fhir.rest.param.IParameter;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.method.SearchMethodBinding.QualifierDetails;

public class RawParamsParmeter implements IParameter {

	private final List<IParameter> myAllMethodParameters;

	public RawParamsParmeter(List<IParameter> theParameters) {
		myAllMethodParameters = theParameters;
	}

	@Override
	public void translateClientArgumentIntoQueryArgument(FhirContext theContext, Object theSourceClientArgument, Map<String, List<String>> theTargetQueryArguments, IBaseResource theTargetResource)
			throws InternalErrorException {
		// not supported on client for now
	}

	@Override
	public Object translateQueryParametersIntoServerArgument(RequestDetails theRequest, BaseMethodBinding<?> theMethodBinding) throws InternalErrorException, InvalidRequestException {
		HashMap<String, List<String>> retVal = null;

		for (String nextName : theRequest.getParameters().keySet()) {
			if (nextName.startsWith("_")) {
				continue;
			}
			
			QualifierDetails qualifiers = SearchMethodBinding.extractQualifiersFromParameterName(nextName);
			
			boolean alreadyCaptured = false;
			for (IParameter nextParameter : myAllMethodParameters) {
				if (nextParameter instanceof SearchParameter) {
					SearchParameter nextSearchParam = (SearchParameter)nextParameter;
					if (nextSearchParam.getName().equals(qualifiers.getParamName())) {
						if (qualifiers.passes(nextSearchParam.getQualifierWhitelist(), nextSearchParam.getQualifierBlacklist())) {
							alreadyCaptured = true;
							break;
						}
					}
				}
			}
			
			if (!alreadyCaptured) {
				if (retVal == null) {
					retVal = new HashMap<String, List<String>>();
				}
				retVal.put(nextName, Arrays.asList(theRequest.getParameters().get(nextName)));
			}
			
		}
		
		return retVal;
	}

	@Override
	public void initializeTypes(Method theMethod, Class<? extends Collection<?>> theOuterCollectionType, Class<? extends Collection<?>> theInnerCollectionType, Class<?> theParameterType) {
		Validate.isTrue(theParameterType.equals(Map.class), "Parameter with @" + RawParam.class + " must be of type Map<String, List<String>>");
	} 

}