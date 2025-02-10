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

import java.util.Arrays;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;

import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteCredentialResetRequestType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectListType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ExecuteScriptType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;

/**
 *
 * @author katkav
 *
 */
public enum Types {

	//ObjectTypes
	USERS(UserType.class, new QName(SchemaConstants.NS_COMMON, "UserType"), new QName(SchemaConstants.NS_COMMON, "user"), "users"),
	VALUE_POLICIES(ValuePolicyType.class, new QName(SchemaConstants.NS_COMMON, "ValuePolicyType"), new QName(SchemaConstants.NS_COMMON, "valuePolicy"), "valuePolicies"),
	SECURITY_POLICIES(SecurityPolicyType.class, new QName(SchemaConstants.NS_COMMON, "SecurityPolicyType"), new QName(SchemaConstants.NS_COMMON, "securityPolicy"), "securityPolicies"),
	CONNECTORS(ConnectorType.class, new QName(SchemaConstants.NS_COMMON, "ConnectorType"), new QName(SchemaConstants.NS_COMMON, "connector"), "connectors"),
	CONNECTOR_HOSTS(ConnectorHostType.class, new QName(SchemaConstants.NS_COMMON, "ConnectorHostType"), new QName(SchemaConstants.NS_COMMON, "connectorHost"), "connectorHosts"),
	GENERIC_OBJECTS(GenericObjectType.class, new QName(SchemaConstants.NS_COMMON, "GenericObjectType"), new QName(SchemaConstants.NS_COMMON, "genericObject"), "genericObjects"),
	RESOURCES(ResourceType.class, new QName(SchemaConstants.NS_COMMON, "ResourceType"), new QName(SchemaConstants.NS_COMMON, "resource"), "resources"),
	OBJECT_TEMPLATES(ObjectTemplateType.class, new QName(SchemaConstants.NS_COMMON, "ObjectTemplateType"), new QName(SchemaConstants.NS_COMMON, "objectTemplate"), "objectTemplates"),
	OBJECT_COLLECTIONS(ObjectCollectionType.class, new QName(SchemaConstants.NS_COMMON, "ObjectCollectionType"), new QName(SchemaConstants.NS_COMMON, "objectCollection"), "objectCollections"),
	SYSTEM_CONFIGURATIONS(SystemConfigurationType.class, new QName(SchemaConstants.NS_COMMON, "SystemConfigurationType"), new QName(SchemaConstants.NS_COMMON, "systemConfiguration"), "systemConfigurations"),
	TASKS(TaskType.class, new QName(SchemaConstants.NS_COMMON, "TaskType"), new QName(SchemaConstants.NS_COMMON, "task"), "tasks"),
	SHADOWS(ShadowType.class, new QName(SchemaConstants.NS_COMMON, "ShadowType"), new QName(SchemaConstants.NS_COMMON, "shadow"), "shadows"),
	ROLES(RoleType.class, new QName(SchemaConstants.NS_COMMON, "RoleType"), new QName(SchemaConstants.NS_COMMON, "role"), "roles"),
	ORGS(OrgType.class, new QName(SchemaConstants.NS_COMMON, "OrgType"), new QName(SchemaConstants.NS_COMMON, "org"), "orgs"),
	SERVICES(ServiceType.class, new QName(SchemaConstants.NS_COMMON, "ServiceType"), new QName(SchemaConstants.NS_COMMON, "service"), "services"),
    ARCHETYPES(ArchetypeType.class, new QName(SchemaConstants.NS_COMMON, "ArchetypeType"), new QName(SchemaConstants.NS_COMMON, "archetype"), "archetypes"),
    FORMS(FormType.class, new QName(SchemaConstants.NS_COMMON, "FormType"), new QName(SchemaConstants.NS_COMMON, "form"), "forms"),
    SEQUENCES(SequenceType.class, new QName(SchemaConstants.NS_COMMON, "SequenceType"), new QName(SchemaConstants.NS_COMMON, "sequence"), "sequences"),

    LOOKUP_TABLE(LookupTableType.class, new QName(SchemaConstants.NS_COMMON, "LookupTableType"), new QName(SchemaConstants.NS_COMMON, "lookupTable"), "lookupTables"),

	//Other types
	QUERY(QueryType.class, new QName(SchemaConstants.NS_QUERY, "QueryType"), new QName(SchemaConstants.NS_QUERY, "query"), null),
	OBJECT_LIST_TYPE(ObjectListType.class, new QName(SchemaConstants.NS_API_TYPES, "ObjectListType"), new QName(SchemaConstants.NS_API_TYPES, "objectList"), ""),
	POLICY_ITEMS_DEFINITION(PolicyItemsDefinitionType.class, new QName(SchemaConstants.NS_API_TYPES, "PolicyItemsDefinitionType"), new QName(SchemaConstants.NS_API_TYPES, "policyItemsDefinition"), ""),
	OBJECT_MODIFICATION_TYPE(ObjectModificationType.class, new QName(SchemaConstants.NS_API_TYPES, "ObjectModificationType"), new QName(SchemaConstants.NS_API_TYPES, "objectModification"), ""),
	EXECUTE_CREDENTIAL_RESET_REQUEST(ExecuteCredentialResetRequestType.class, new QName(SchemaConstants.NS_API_TYPES, "ExecuteCredentialResetRequestType"), new QName(SchemaConstants.NS_API_TYPES, "executeCredentialResetRequest"), ""),
	EXECUTE_SCRIPT(ExecuteScriptType.class, new QName(SchemaConstants.NS_SCRIPTING, "ExecuteScriptType"), new QName(SchemaConstants.NS_SCRIPTING, "executeScript"), ""),
	ASSIGNMENT(AssignmentType.class, new QName(SchemaConstants.NS_COMMON, "AssignmentType"), new QName(SchemaConstants.NS_COMMON, "assignment"), "");

	private Class<?> clazz;
	/**
	 * element name - used for XML jaxb serialization
	 */
	private QName elementName;
	/**
	 * type name as QName, e.g. c:RoleType, c:ValuePolicyType.. e.g. we need it to the reference
	 */
	private QName typeQName;
	private String restPath;

	private Types(Class<?> clazz, QName typeQName, QName elementName, String restPath) {
		this.clazz = clazz;
		this.typeQName = typeQName;
		this.elementName = elementName;
		this.restPath = restPath;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public QName getElementName() {
		return elementName;
	}

	public String getRestPath() {
		return restPath;
	}

	public QName getTypeQName() {
		return typeQName;
	}

	public static Types findType(Class<?> clazz) {
		return Arrays.asList(values()).stream().filter(type -> type.getClazz().equals(clazz)).findAny().orElse(null);
	}

	public static Types findType(String localname) {
		if (StringUtils.isBlank(localname)) {
			return null;
		}
		for (Types type : values()) {
			if (type.getTypeQName().getLocalPart().equals(localname)) {
				return type;
			}
		}
		throw new UnsupportedOperationException("Not supported type: " + localname);
	}
}
