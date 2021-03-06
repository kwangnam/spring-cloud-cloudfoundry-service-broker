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

package org.springframework.cloud.servicebroker.autoconfigure.web.servlet.fixture;

import org.springframework.cloud.servicebroker.model.ServiceDefinition;
import org.springframework.cloud.servicebroker.model.ServiceDefinitionRequires;

public class ServiceFixture {

	public static ServiceDefinition getSimpleService() {
		return ServiceDefinition.builder()
				.id("service-one-id")
				.name("Service One")
				.description("Description for Service One")
				.bindable(true)
				.plans(PlanFixture.getAllPlans())
				.build();
	}

	public static ServiceDefinition getServiceWithRequires() {
		return ServiceDefinition.builder()
				.id("service-one-id")
				.name("Service One")
				.description("Description for Service One")
				.bindable(true)
				.planUpdateable(true)
				.plans(PlanFixture.getAllPlans())
				.requires(ServiceDefinitionRequires.SERVICE_REQUIRES_SYSLOG_DRAIN.toString(),
						ServiceDefinitionRequires.SERVICE_REQUIRES_ROUTE_FORWARDING.toString())
				.build();
	}

}
