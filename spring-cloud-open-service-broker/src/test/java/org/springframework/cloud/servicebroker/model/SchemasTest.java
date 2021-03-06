/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.cloud.servicebroker.model;

import org.junit.Test;
import org.springframework.cloud.servicebroker.model.fixture.DataFixture;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.Assert.assertThat;

public class SchemasTest {
	@Test
	public void noSchemas() throws IOException {
		Schemas schemas = Schemas.builder().build();
		String json = DataFixture.toJson(schemas);

		assertThat(json, isJson(allOf(
				withoutJsonPath("$.service_instance"),
				withoutJsonPath("$.service_binding")
		)));
	}
	
	@Test
	public void emptySchemas() throws IOException {
		Schemas schemas = Schemas.builder()
				.serviceInstanceSchema(ServiceInstanceSchema.builder().build())
				.serviceBindingSchema(ServiceBindingSchema.builder().build())
				.build();
		String json = DataFixture.toJson(schemas);

		assertThat(json, isJson(allOf(
				withJsonPath("$.service_instance"),
				withoutJsonPath("$.service_instance.create"),
				withoutJsonPath("$.service_instance.update"),
				withJsonPath("$.service_binding"),
				withoutJsonPath("$.service_binding.create")
		)));
	}

	@Test
	@SuppressWarnings("serial")
	public void allSchemaFields() throws IOException {
		Map<String, Object> schemaProperties = new HashMap<String, Object>() {{
			put("properties", new HashMap<String, Object>() {{
				put("billing-account", new HashMap<String, String>() {{
					put("description", "Billing account number.");
					put("type", "string");
				}});
			}});
		}};

		Schemas schemas = Schemas.builder()
				.serviceInstanceSchema(ServiceInstanceSchema.builder()
						.createMethodSchema(MethodSchema.builder()
								.parameters("$schema", "http://example.com/service/create/schema")
								.parameters("type", "object")
								.parameters(schemaProperties)
								.build())
						.updateMethodSchema(MethodSchema.builder()
								.parameters("$schema", "http://example.com/service/update/schema")
								.parameters("type", "object")
								.build())
						.build())
				.serviceBindingSchema(ServiceBindingSchema.builder()
						.createMethodSchema(MethodSchema.builder()
								.parameters("$schema", "http://example.com/binding/create/schema")
								.parameters("type", "object")
								.build())
						.build())
				.build();
		String json = DataFixture.toJson(schemas);

		assertThat(json, isJson(allOf(
				withJsonPath("$.service_instance.create.parameters.$schema",
						equalTo("http://example.com/service/create/schema")),
				withJsonPath("$.service_instance.create.parameters.type",
						equalTo("object")),
				withJsonPath("$.service_instance.create.parameters.properties.billing-account.description",
						equalTo("Billing account number.")),
				withJsonPath("$.service_instance.create.parameters.properties.billing-account.type",
						equalTo("string")),

				withJsonPath("$.service_instance.update.parameters", allOf(
						hasEntry("$schema", "http://example.com/service/update/schema"),
						hasEntry("type", "object")
				)),
				withJsonPath("$.service_binding.create.parameters", allOf(
						hasEntry("$schema", "http://example.com/binding/create/schema"),
						hasEntry("type", "object")
				))
		)));
	}

}
