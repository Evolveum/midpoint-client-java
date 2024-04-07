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

import java.util.List;
import java.util.Map;

import com.evolveum.midpoint.client.api.*;

import com.evolveum.midpoint.client.api.exception.*;

import jakarta.ws.rs.core.Response;

import com.evolveum.midpoint.xml.ns._public.common.common_3.ObjectType;

/**
 * @author Z14tk0
 * @author Z14tk0
 *
 */
public class RestJaxbObjectReplaceService<O extends ObjectType> extends AbstractObjectWebResource<O> implements ObjectReplaceService<O> {

	private final O object;
    private final ExecuteOptionSupport.WithPost<ObjectReference<O>> options = new ExecuteOptionsBuilder.WithPost<ObjectReference<O>>() {
        @Override
        public TaskFuture<ObjectReference<O>> apost() throws ObjectAlreadyExistsException, ObjectNotFoundException {
            return RestJaxbObjectReplaceService.this.apost(optionsAsStringList());
        }
    };

	public RestJaxbObjectReplaceService(final RestJaxbService service, final Class<O> type, String oid, final O object) {
		super(service, type, oid);
		this.object = object;
	}

    @Override
    public ExecuteOptionSupport.WithPost<ObjectReference<O>> options() {
        return options;
    }

    @Override
	public TaskFuture<ObjectReference<O>> apost() throws CommonException {
        return options().apost();
    }

    public TaskFuture<ObjectReference<O>> apost(List<String> options) throws ObjectAlreadyExistsException, ObjectNotFoundException {

        String restPath = RestUtil.subUrl(Types.findType(getType()).getRestPath(), getOid());
        Response response = getService().put(restPath, object, Map.of("options", options));

		switch(response.getStatus()) {
			case 409:
				throw new ObjectAlreadyExistsException(response.getStatusInfo().getReasonPhrase());
			case 500:
				throw new SystemException(response.getStatusInfo().getReasonPhrase());
			case 201:
			case 202:
            case 240:
            case 250:
                String location = response.getLocation().toString();
                String oid = location.substring(location.indexOf(restPath) + restPath.lastIndexOf("/") + 1);
                RestJaxbObjectReference<O> ref = new RestJaxbObjectReference<>(getService(), getType(), oid);
                return new RestJaxbCompletedFuture<>(ref);
			default:
                throw new UnsupportedOperationException("Implement other status codes, unsupported return status: " + response.getStatus());
		}
	}
}
