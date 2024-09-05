/*
 * Copyright (c) 2017-2020 Evolveum
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
package com.evolveum.midpoint.client.impl.restjaxb;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.Response;

import com.evolveum.midpoint.client.api.PolicyItemsDefinitionBuilder;
import com.evolveum.midpoint.client.api.TaskFuture;
import com.evolveum.midpoint.client.api.ValidateGenerateRpcService;
import com.evolveum.midpoint.client.api.exception.*;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

import java.util.List;

/**
 *
 * @author katkav
 *
 */
public class RestJaxbValidateGenerateRpcService implements ValidateGenerateRpcService {

	private RestJaxbService service;
	private PolicyItemsDefinitionType policyItemDefinition;
	private String path;


	public RestJaxbValidateGenerateRpcService(RestJaxbService service, String path) {
		this.service = service;
		this.path = path;
	}

	public RestJaxbValidateGenerateRpcService(RestJaxbService service, String path, PolicyItemsDefinitionType policyItemDefinition) {
		this.service = service;
		this.path = path;
		this.policyItemDefinition = policyItemDefinition;
	}

    @Override
	public TaskFuture<PolicyItemsDefinitionType> apost() throws CommonException {

        Response response = service.post(path, policyItemDefinition);

        switch (response.getStatus()) {
            case 200:
                if (response.hasEntity()) {

                    PolicyItemsDefinitionType itemsDefinitionType = response.readEntity(PolicyItemsDefinitionType.class);
                    return new RestJaxbCompletedFuture<>(itemsDefinitionType);
                } else {
                    // Return empty policy definition type
                    return new RestJaxbCompletedFuture<>(new PolicyItemsDefinitionType());
                }
            case 240:
            case 250:
                OperationResultType operationResult = response.readEntity(OperationResultType.class);
                throw new PartialErrorException(RestUtil.getOperationResultTypeMessage(operationResult));
            case 409:
                OperationResultType operationResultType = response.readEntity(OperationResultType.class);
                throw new PolicyViolationException(RestUtil.getFailedValidationMessage(operationResultType));
            default:
                throw new UnsupportedOperationException("Implement other status codes, unsupported return status: " + response.getStatus());
        }
    }

	@Override
	public PolicyItemsDefinitionBuilder items() {
		return new PolicyItemDefinitionBuilderImpl(service, path);
	}
}
