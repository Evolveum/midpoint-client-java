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
package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.client.api.verb.Post;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.TaskType;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ExecuteScriptType;

/**
 *
 * @author katkav
 *
 */
public interface RpcService<T> extends Post<T> {

	ValidateGenerateRpcService validate();
	ValidateGenerateRpcService generate();

	//TODO: implement, change return type etc
	void compare();

	// Preliminary implementation. Adapt as necessary.
	ExecuteScriptRpcService<ExecuteScriptResponseType> executeScript(ExecuteScriptType request);

    ExecuteScriptRpcService<ExecuteScriptResponseType> executeScript(ExecuteScriptType request, long timeout);

	ExecuteScriptRpcService<ObjectReference<TaskType>> executeScriptAsync(ExecuteScriptType request);
}
