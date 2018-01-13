package io.sapl.interpreter.pip;
/**
 * Copyright © 2017 Dominic Heutelbeck (dheutelbeck@ftk.de)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;

import io.sapl.api.functions.FunctionException;
import io.sapl.api.pip.Attribute;
import io.sapl.api.pip.AttributeException;
import io.sapl.api.pip.PolicyInformationPoint;
import io.sapl.interpreter.pip.geo.KMLImport;
import io.sapl.interpreter.pip.geo.PostGISConnection;
import io.sapl.interpreter.pip.geo.TraccarConnection;

@PolicyInformationPoint(name = GeoAttributeFinder.NAME, description = GeoAttributeFinder.DESCRIPTION)
public class GeoAttributeFinder {

	public static final String NAME = "sapl.pip.geo";
	public static final String DESCRIPTION = "AttributeFinder for geographical data.";

	@Attribute
	public JsonNode traccar(JsonNode value, Map<String, JsonNode> variables)
			throws AttributeException, FunctionException {
		return new TraccarConnection(value).toGeoPIPResponse();
	}

	@Attribute
	public JsonNode postgis(JsonNode value, Map<String, JsonNode> variables)
			throws AttributeException, FunctionException {
		return new PostGISConnection(value).toGeoPIPResponse();
	}

	@Attribute
	public JsonNode kml(JsonNode value, Map<String, JsonNode> variables) throws AttributeException, FunctionException {
		return new KMLImport(value).toGeoPIPResponse();
	}
}
