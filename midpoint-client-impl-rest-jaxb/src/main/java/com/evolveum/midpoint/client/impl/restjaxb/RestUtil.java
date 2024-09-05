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

import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ObjectModificationType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemTargetType;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.ItemDeltaType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import com.evolveum.prism.xml.ns._public.types_3.ModificationTypeType;

import org.apache.commons.lang3.StringUtils;

import jakarta.ws.rs.core.Response;
import javax.xml.namespace.QName;
import java.net.URI;
import java.util.List;

/**
 * @author semancik
 *
 */
public class RestUtil {

	private static final String VALIDATION_OPERATION_PATH = "com.evolveum.midpoint.model.api.ModelInteractionService.validateValue.value";
	private static final String VALUE_POLICY_EVALUATOR_VALIDATE_VALUE_PATH = "class com.evolveum.midpoint.model.common.stringpolicy.ObjectValuePolicyEvaluator.validateValue";

	public static String subUrl(String... segments) {
		return "/" + StringUtils.join(segments, "/");
	}

	public static String subUrl(final String urlPrefix, final String pathSegment) {
		// TODO: better code (e.g. escaping)
		return "/" + urlPrefix + "/" + pathSegment;
	}

	public static ObjectModificationType buildModifyObject(List<ItemDeltaType> itemDeltas)
	{
		ObjectModificationType objectModificationType = new ObjectModificationType();
		itemDeltas.forEach((itemDelta) ->
				objectModificationType.getItemDelta().add(itemDelta));

		return objectModificationType;
	}

	public static ItemDeltaType buildItemDelta(ModificationTypeType modificationType, String path, Object value)
	{
		//Create ItemDelta
		ItemDeltaType itemDeltaType = new ItemDeltaType();
		itemDeltaType.setModificationType(modificationType);

		//Set Path
		ItemPathType itemPathType = new ItemPathType();
		itemPathType.setValue(path);
		itemDeltaType.setPath(itemPathType);

		if (value != null) {
            itemDeltaType.getValue().add(value);
        }

		return itemDeltaType;
	}
	public static PolicyItemsDefinitionType buildGenerateObject(String targetPath, Boolean execute){
		return buildGenerateObject("", targetPath,execute);
	}

	public static PolicyItemsDefinitionType buildGenerateObject(String policyOid, String targetPath, Boolean execute)
	{
		PolicyItemsDefinitionType policyItemsDefinitionType = new PolicyItemsDefinitionType();
		PolicyItemDefinitionType policyItemDefinitionType = new PolicyItemDefinitionType();

		//Set target path
		ItemPathType itemPathType = new ItemPathType();
		itemPathType.setValue(targetPath);
		PolicyItemTargetType targetType = new PolicyItemTargetType();
		targetType.setPath(itemPathType);
		policyItemDefinitionType.setTarget(targetType);

		if(!"".equals(policyOid))
		{
			//Set valuePolicyRef
			policyItemDefinitionType.setValuePolicyRef(buildValuePolicyRef(policyOid));
		}
		//Set Execute
		policyItemDefinitionType.setExecute(execute);

		policyItemsDefinitionType.getPolicyItemDefinition().add(policyItemDefinitionType);
		return policyItemsDefinitionType;
	}

    private static ObjectReferenceType buildValuePolicyRef(String policyOid) {
		return createObjectReference(policyOid, Types.VALUE_POLICIES);
	}

    public static ObjectReferenceType createObjectReference(String targetOid, Types type) {
        ObjectReferenceType objectReferenceType = new ObjectReferenceType();
        objectReferenceType.setOid(targetOid);
        objectReferenceType.setType(type.getTypeQName());
        return objectReferenceType;
    }

	public static String getFailedValidationMessage(OperationResultType operationResultType){

		if (operationResultType.getMessage() != null) {
			return operationResultType.getMessage();
		}

		if (operationResultType.getUserFriendlyMessage() != null) {
			LocalizableMessageType localizableMessage = operationResultType.getUserFriendlyMessage();
			return getStringMessage(localizableMessage);
		}

		LocalizableMessageType validationResult = getValidationOperationResult(operationResultType);
		return getStringMessage(validationResult);

	}

	private static LocalizableMessageType getValidationOperationResult(OperationResultType operationResultType) {
		List<OperationResultType> partialResults = operationResultType.getPartialResults();
		for(OperationResultType operationResult : partialResults){

			if (VALIDATION_OPERATION_PATH.equals(operationResult.getOperation())) {
				return getValidationDetialsOperationResult(operationResult);
			}
		}

		return null;
	}

	private static LocalizableMessageType getValidationDetialsOperationResult(OperationResultType validationResult) {
		for(OperationResultType operationResult : validationResult.getPartialResults()){
			if(VALUE_POLICY_EVALUATOR_VALIDATE_VALUE_PATH.equals(operationResult.getOperation()))
			{
				return operationResult.getUserFriendlyMessage();
			}
		}
		return null;
	}

	private static String getStringMessage(LocalizableMessageType localizableMessage) {
		if (localizableMessage instanceof SingleLocalizableMessageType) {
			return ((SingleLocalizableMessageType) localizableMessage).getFallbackMessage();
		}

		if (localizableMessage instanceof LocalizableMessageListType) {
			List<LocalizableMessageType> messageList = ((LocalizableMessageListType) localizableMessage).getMessage();
			StringBuilder fallbackMsg = new StringBuilder();
			for (LocalizableMessageType msg : messageList) {
				fallbackMsg.append(getStringMessage(msg));
			}
			return fallbackMsg.toString();
		}

		throw new UnsupportedOperationException("Unknown localizable message type: " + ((localizableMessage != null) ? localizableMessage.getClass() : null));
	}

	public static String getOidFromLocation(Response response, String path) {
        URI uriLocation = response.getLocation();
        // Fixed location null: When you enabled policy rule in Midpoint, for instance an approval step on user's creation
        // The HTTP response is 202 without location reference
        if (uriLocation == null) {
            return null;
        }
		String location = uriLocation.toString();
		String[] locationSegments = location.split(path + "/");
		return locationSegments[1];
	}

    public static String getOidLastFromLocation(Response response) {
        URI uriLocation = response.getLocation();
        // Fixed location null: When you enabled policy rule in Midpoint, for instance an approval step on user's creation
        // The HTTP response is 202 without location reference
        if (uriLocation == null) {
            return null;
        }
        String location = uriLocation.toString();
        String[] locationSegments = location.split("/");
        return locationSegments[locationSegments.length-1];
    }

    public static String getOperationResultTypeMessage(OperationResultType operationResultType) {
        if (operationResultType.getMessage() != null) {
            return operationResultType.getMessage();
        }
        if (operationResultType.getUserFriendlyMessage() != null) {
            LocalizableMessageType localizableMessage = operationResultType.getUserFriendlyMessage();
            return getStringMessage(localizableMessage);
        }
        return null;
    }

}
