/*
 * Copyright 2002-2017 the original author or authors.
 *
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
 */

package org.springframework.cloud.servicebroker.controller;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingDoesNotExistException;
import org.springframework.cloud.servicebroker.exception.ServiceInstanceBindingExistsException;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.CreateServiceInstanceBindingResponse;
import org.springframework.cloud.servicebroker.model.DeleteServiceInstanceBindingRequest;
import org.springframework.cloud.servicebroker.model.ErrorMessage;
import org.springframework.cloud.servicebroker.service.CatalogService;
import org.springframework.cloud.servicebroker.service.ServiceInstanceBindingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;
import static org.springframework.cloud.servicebroker.model.ServiceBrokerRequest.API_INFO_LOCATION_HEADER;
import static org.springframework.cloud.servicebroker.model.ServiceBrokerRequest.ORIGINATING_IDENTITY_HEADER;

/**
 * See: http://docs.cloudfoundry.org/services/api.html
 *
 * @author sgreenberg@pivotal.io
 * @author Scott Frederick
 */
@RestController
public class ServiceInstanceBindingController extends BaseController {
	private static final Logger log = getLogger(ServiceInstanceBindingController.class);
	
	private ServiceInstanceBindingService serviceInstanceBindingService;

	@Autowired
	public ServiceInstanceBindingController(CatalogService catalogService,
											ServiceInstanceBindingService serviceInstanceBindingService) {
		super(catalogService);
		this.serviceInstanceBindingService = serviceInstanceBindingService;
	}

	@PutMapping(value = {
			"/{cfInstanceId}/v2/service_instances/{instanceId}/service_bindings/{bindingId}",
			"/v2/service_instances/{instanceId}/service_bindings/{bindingId}"
	})
	public ResponseEntity<?> createServiceInstanceBinding(@PathVariable Map<String, String> pathVariables,
														  @PathVariable("instanceId") String serviceInstanceId,
														  @PathVariable("bindingId") String bindingId,
														  @RequestHeader(value = API_INFO_LOCATION_HEADER, required = false) String apiInfoLocation,
														  @RequestHeader(value = ORIGINATING_IDENTITY_HEADER, required = false) String originatingIdentityString,
														  @Valid @RequestBody CreateServiceInstanceBindingRequest request) {
		request.setServiceInstanceId(serviceInstanceId);
		request.setBindingId(bindingId);
		request.setServiceDefinition(getServiceDefinition(request.getServiceDefinitionId()));
		setCommonRequestFields(request, pathVariables.get("cfInstanceId"), apiInfoLocation, originatingIdentityString);

		log.debug("Creating a service instance binding: request={}", request);

		CreateServiceInstanceBindingResponse response = serviceInstanceBindingService.createServiceInstanceBinding(request);

		log.debug("Creating a service instance binding succeeded: serviceInstanceId={}, bindingId={}, response={}",
				serviceInstanceId, bindingId, response);

		return new ResponseEntity<>(response, response.isBindingExisted() ? HttpStatus.OK : HttpStatus.CREATED);
	}

	@DeleteMapping(value = {
			"/{cfInstanceId}/v2/service_instances/{instanceId}/service_bindings/{bindingId}",
			"/v2/service_instances/{instanceId}/service_bindings/{bindingId}"
	})
	public ResponseEntity<String> deleteServiceInstanceBinding(@PathVariable Map<String, String> pathVariables,
															   @PathVariable("instanceId") String serviceInstanceId,
															   @PathVariable("bindingId") String bindingId,
															   @RequestParam("service_id") String serviceDefinitionId,
															   @RequestParam("plan_id") String planId,
															   @RequestHeader(value = API_INFO_LOCATION_HEADER, required = false) String apiInfoLocation,
															   @RequestHeader(value = ORIGINATING_IDENTITY_HEADER, required = false) String originatingIdentityString) {
		DeleteServiceInstanceBindingRequest request = new DeleteServiceInstanceBindingRequest();
		request.setServiceInstanceId(serviceInstanceId);
		request.setBindingId(bindingId);
		request.setServiceDefinitionId(serviceDefinitionId);
		request.setPlanId(planId);
		request.setServiceDefinition(getServiceDefinition(serviceDefinitionId));
		setCommonRequestFields(request, pathVariables.get("cfInstanceId"), apiInfoLocation, originatingIdentityString);

		log.debug("Deleting a service instance binding: request={}", request);

		try {
			serviceInstanceBindingService.deleteServiceInstanceBinding(request);
		} catch (ServiceInstanceBindingDoesNotExistException e) {
			log.debug("Service instance binding does not exist: ", e);
			return new ResponseEntity<>("{}", HttpStatus.GONE);
		}

		log.debug("Deleting a service instance binding succeeded: bindingId={}", bindingId);

		return new ResponseEntity<>("{}", HttpStatus.OK);
	}

	@ExceptionHandler(ServiceInstanceBindingExistsException.class)
	public ResponseEntity<ErrorMessage> handleException(ServiceInstanceBindingExistsException ex) {
		log.debug("Service instance binding already exists: ", ex);
		return getErrorResponse(ex.getMessage(), HttpStatus.CONFLICT);
	}
}
