/*
 * Copyright (c) 2017-2018 Evolveum
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

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ExecuteScriptType;

import java.util.List;

/**
 *
 * @author katkav
 *
 */
public class RestJaxbRpcService<T> implements RpcService<T> {

	private static final String GENERATE_PATH = "/rpc/generate";
	private static final String VALIDATE_PATH = "/rpc/validate";
	private static final String EXECUTE_SCRIPT_PATH = "/rpc/executeScript";

	private RestJaxbService service;

	public RestJaxbRpcService(RestJaxbService service) {
		this.service = service;
	}

	public RestJaxbService getService() {
		return service;
	}

	@Override
	public ValidateGenerateRpcService validate() {
		return new RestJaxbValidateGenerateRpcService(getService(), VALIDATE_PATH);
	}

	@Override
	public void compare() {
		// TODO Auto-generated method stub
	}

	@Override
	public ExecuteScriptRpcService<ExecuteScriptResponseType> executeScript(ExecuteScriptType script) {
		return new RestJaxbExecuteScriptRpcService<>(getService(), EXECUTE_SCRIPT_PATH, script, false);
	}

    @Override
    public ExecuteScriptRpcService<ExecuteScriptResponseType> executeScript(ExecuteScriptType script, long timeout) {
        return new RestJaxbExecuteScriptRpcService<>(getService(), EXECUTE_SCRIPT_PATH, script, false, timeout);
    }

    @Override
    public ExecuteScriptRpcService<ObjectReference<TaskType>> executeScriptAsync(ExecuteScriptType script) {
        return new RestJaxbExecuteScriptRpcService<>(getService(), EXECUTE_SCRIPT_PATH, script, true);
    }

	@Override
	public TaskFuture<T> apost() throws CommonException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ValidateGenerateRpcService generate() {
		return new RestJaxbValidateGenerateRpcService(getService(), GENERATE_PATH);
	}

}
