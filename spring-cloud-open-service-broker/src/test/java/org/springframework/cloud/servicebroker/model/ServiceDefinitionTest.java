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

import java.io.IOException;

import org.junit.Test;

import org.springframework.cloud.servicebroker.model.fixture.DataFixture;

import static com.jayway.jsonpath.matchers.JsonPathMatchers.isJson;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withJsonPath;
import static com.jayway.jsonpath.matchers.JsonPathMatchers.withoutJsonPath;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;
import static org.springframework.cloud.servicebroker.model.ServiceDefinitionRequires.SERVICE_REQUIRES_ROUTE_FORWARDING;
import static org.springframework.cloud.servicebroker.model.ServiceDefinitionRequires.SERVICE_REQUIRES_SYSLOG_DRAIN;
import static org.springframework.cloud.servicebroker.model.ServiceDefinitionRequires.SERVICE_REQUIRES_VOLUME_MOUNT;

public class ServiceDefinitionTest {
	@Test
	@SuppressWarnings("unchecked")
	public void serviceDefinitionWithDefaults() throws IOException {
		ServiceDefinition serviceDefinition = ServiceDefinition.builder()
				.id("service-definition-id-one")
				.name("service-definition-one")
				.description("Service Definition One")
				.plans(Plan.builder().build())
				.build();
		String json = DataFixture.toJson(serviceDefinition);

		assertThat(json, isJson(allOf(
				withJsonPath("$.id", equalTo("service-definition-id-one")),
				withJsonPath("$.name", equalTo("service-definition-one")),
				withJsonPath("$.description", equalTo("Service Definition One")),
				withJsonPath("$.plans", hasSize(1)),
				withJsonPath("$.bindable", equalTo(false)),
				withJsonPath("$.plan_updateable", equalTo(false)),
				withoutJsonPath("$.tags"),
				withoutJsonPath("$.requires"),
				withoutJsonPath("$.metadata"),
				withoutJsonPath("$.dashboard_client")
		)));
	}

	@Test
	@SuppressWarnings("unchecked")
	public void serviceDefinitionWithAllFields() throws IOException {
		ServiceDefinition serviceDefinition = ServiceDefinition.builder()
				.id("service-definition-id-one")
				.name("service-definition-one")
				.description("Service Definition One")
				.plans(Plan.builder().build())
				.bindable(true)
				.tags("tag1", "tag2")
				.metadata("field1", "value1")
				.metadata("field2", "value2")
				.requires(SERVICE_REQUIRES_ROUTE_FORWARDING,
						SERVICE_REQUIRES_SYSLOG_DRAIN,
						SERVICE_REQUIRES_VOLUME_MOUNT)
				.planUpdateable(true)
				.dashboardClient(DashboardClient.builder()
						.id("dashboard-id")
						.secret("dashboard-secret")
						.redirectUri("https://redirect.example.com")
						.build())
				.build();
		String json = DataFixture.toJson(serviceDefinition);

		assertThat(json, isJson(allOf(
				withJsonPath("$.id", equalTo("service-definition-id-one")),
				withJsonPath("$.name", equalTo("service-definition-one")),
				withJsonPath("$.description", equalTo("Service Definition One")),
				withJsonPath("$.plans", hasSize(1)),
				withJsonPath("$.bindable", equalTo(true)),
				withJsonPath("$.plan_updateable", equalTo(true)),
				withJsonPath("$.tags[*]", contains("tag1", "tag2")),
				withJsonPath("$.requires[*]",
						contains(SERVICE_REQUIRES_ROUTE_FORWARDING.toString(),
								SERVICE_REQUIRES_SYSLOG_DRAIN.toString(),
								SERVICE_REQUIRES_VOLUME_MOUNT.toString())),
				withJsonPath("$.metadata",
						allOf(hasEntry("field1", "value1"),
								hasEntry("field2", "value2"))),
				withJsonPath("$.dashboard_client.id", equalTo("dashboard-id")),
				withJsonPath("$.dashboard_client.secret", equalTo("dashboard-secret")),
				withJsonPath("$.dashboard_client.redirect_uri", equalTo("https://redirect.example.com"))
		)));
	}
}
