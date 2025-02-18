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
package com.evolveum.midpoint.client.api;

import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.midpoint.client.api.scripting.ScriptingUtil;

/**
 * @author semancik
 */
public interface Service {

	// TODO
//	ProxyService proxy();

    FocusCollectionService<UserType> users();

	<T> RpcService<T> rpc();

	ObjectCollectionService<ValuePolicyType> valuePolicies();

	ObjectCollectionService<ArchetypeType> archetypes();

	ObjectCollectionService<LookupTableType> lookupTables();

	UserType self();

	Service impersonate(String oid);

    Service removeImpersonate();

	Service addHeader(String header, String value);

    Service removeHeader(String header);

	ObjectCollectionService<SecurityPolicyType> securityPolicies();

	ObjectCollectionService<ConnectorType> connectors();

	ObjectCollectionService<ConnectorHostType> connectorHosts();

	ObjectCollectionService<GenericObjectType> genericObjects();

	ResourceCollectionService resources();

	ObjectCollectionService<ObjectTemplateType> objectTemplates();

	ObjectCollectionService<ObjectCollectionType> objectCollections();

    ObjectCollectionService<SequenceType> sequences();

    ObjectCollectionService<SystemConfigurationType> systemConfigurations();

	ObjectCollectionService<FormType> forms();

	TaskCollectionService tasks();

	ShadowCollectionService shadows();

    FocusCollectionService<RoleType> roles();

    FocusCollectionService<OrgType> orgs();

    FocusCollectionService<ServiceType> services();

	ServiceUtil util();

	ScriptingUtil scriptingUtil();

	void close();
}
