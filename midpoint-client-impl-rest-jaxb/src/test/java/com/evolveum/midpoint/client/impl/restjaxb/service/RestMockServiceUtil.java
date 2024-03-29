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
package com.evolveum.midpoint.client.impl.restjaxb.service;

import java.net.URI;

import javax.naming.CommunicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.eclipse.jetty.util.thread.Scheduler.Task;

import com.evolveum.midpoint.client.api.exception.AuthorizationException;
import com.evolveum.midpoint.client.api.exception.ConcurrencyException;
import com.evolveum.midpoint.client.api.exception.ConfigurationException;
import com.evolveum.midpoint.client.api.exception.ExpressionEvaluationException;
import com.evolveum.midpoint.client.api.exception.NoFocusNameSchemaException;
import com.evolveum.midpoint.client.api.exception.ObjectAlreadyExistsException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.api.exception.PolicyViolationException;
import com.evolveum.midpoint.client.api.exception.SchemaException;
import com.evolveum.midpoint.client.api.exception.SecurityViolationException;
import com.evolveum.midpoint.client.api.exception.TunnelException;
import com.evolveum.midpoint.xml.ns._public.common.common_3.OperationResultType;

/**
 *
 * @author katkav
 *
 */
public class RestMockServiceUtil {

		public static final String MESSAGE_PROPERTY_TASK_NAME = "task";
		private static final String QUERY_PARAMETER_OPTIONS = "options";
		public static final String OPERATION_RESULT_STATUS = "OperationResultStatus";
		public static final String OPERATION_RESULT_MESSAGE = "OperationResultMessage";

		public static Response handleException(OperationResultType result, Exception ex) {
			return createErrorResponseBuilder(result, ex).build();
		}

		public static <T> Response createResponse(Response.Status statusCode, OperationResultType result) {

			return createResponse(statusCode, null, result, false);

		}

		public static <T> Response createResponse(Response.Status statusCode, T body, OperationResultType result) {

			return createResponse(statusCode, body, result, false);

		}

		public static <T> Response createResponse(Response.Status statusCode, T body, OperationResultType result, boolean sendOriginObjectIfNotSuccess) {
			OperationResultUtil.computeStatusIfUnknown(result);

			if (OperationResultUtil.isPartialError(result)) {
				return createBody(Response.status(250), sendOriginObjectIfNotSuccess, body, result).build();
			} else if (OperationResultUtil.isHandledError(result)) {
				return createBody(Response.status(240), sendOriginObjectIfNotSuccess, body, result).build();
			}

			return body == null ? Response.status(statusCode).build() : Response.status(statusCode).entity(body).build();
		}

		private static <T> ResponseBuilder createBody(ResponseBuilder builder, boolean sendOriginObjectIfNotSuccess, T body, OperationResultType result) {
			if (sendOriginObjectIfNotSuccess) {
				return builder.entity(body);
			}
			return builder.entity(result);

		}

		public static <T> Response createResponse(Response.Status statusCode, URI location, OperationResultType result) {
			OperationResultUtil.computeStatusIfUnknown(result);

			if (OperationResultUtil.isPartialError(result)) {
				return createBody(Response.status(250), false, null, result).location(location).build();
			} else if (OperationResultUtil.isHandledError(result)) {
				return createBody(Response.status(240), false, null, result).location(location).build();
			}


			return location == null ? Response.status(statusCode).build() : Response.status(statusCode).location(location).build();
		}



		public static Response.ResponseBuilder createErrorResponseBuilder(OperationResultType result, Exception ex) {
			if (ex instanceof ObjectNotFoundException) {
				return createErrorResponseBuilder(Response.Status.NOT_FOUND, result);
			}

			if (ex instanceof CommunicationException || ex instanceof TunnelException) {
				return createErrorResponseBuilder(Response.Status.GATEWAY_TIMEOUT, result);
			}

			if (ex instanceof SecurityViolationException || ex instanceof AuthorizationException) {
				return createErrorResponseBuilder(Response.Status.FORBIDDEN, result);
			}

			if (ex instanceof ConfigurationException) {
				return createErrorResponseBuilder(Response.Status.BAD_GATEWAY, result);
			}

			if (ex instanceof SchemaException
					|| ex instanceof NoFocusNameSchemaException
					|| ex instanceof ExpressionEvaluationException) {
				return createErrorResponseBuilder(Response.Status.BAD_REQUEST, result);
			}

			if (ex instanceof PolicyViolationException
					|| ex instanceof ObjectAlreadyExistsException
					|| ex instanceof ConcurrencyException) {
				return createErrorResponseBuilder(Response.Status.CONFLICT, result);
			}

			return createErrorResponseBuilder(Response.Status.INTERNAL_SERVER_ERROR, result);
		}

//		public static Response.ResponseBuilder createErrorResponseBuilder(Response.Status status, OperationResultType result) {
//
//			return createErrorResponseBuilder(status, result);
//		}

		public static Response.ResponseBuilder createErrorResponseBuilder(Response.Status status, OperationResultType message) {
			OperationResultUtil.computeStatusIfUnknown(message);
			return Response.status(status).entity(message);
		}

//		public static ModelExecuteOptions getOptions(UriInfo uriInfo){
//	    	List<String> options = uriInfo.getQueryParameters().get(QUERY_PARAMETER_OPTIONS);
//			return ModelExecuteOptions.fromRestOptions(options);
//	    }

		public static Task initRequest(MessageContext mc) {
			// No need to audit login. it was already audited during authentication
			return (Task) mc.get(MESSAGE_PROPERTY_TASK_NAME);
		}

//		public static void finishRequest(Task task, SecurityHelper securityHelper) {
//			task.getResult().computeStatus();
//			ConnectionEnvironment connEnv = ConnectionEnvironment.create(SchemaConstants.CHANNEL_REST_URI);
//			connEnv.setSessionIdOverride(task.getTaskIdentifier());
//			securityHelper.auditLogout(connEnv, task);
//		}

		// slightly experimental
		public static Response.ResponseBuilder createResultHeaders(Response.ResponseBuilder builder, OperationResultType result) {
			return builder.entity(result);
//					.header(OPERATION_RESULT_STATUS, OperationResultStatus.createStatusType(result.getStatus()).value())
//					.header(OPERATION_RESULT_MESSAGE, result.getMessage());
		}

		public static void createAbortMessage(ContainerRequestContext requestCtx){
			requestCtx.abortWith(Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate", RestAuthenticationMethod.BASIC.getMethod() + " realm=\"midpoint\", " + RestAuthenticationMethod.SECURITY_QUESTIONS.getMethod()).build());
		}

		public static void createSecurityQuestionAbortMessage(ContainerRequestContext requestCtx, String secQChallenge){
			String challenge = "";
			if (StringUtils.isNotBlank(secQChallenge)) {
				challenge = " " + Base64Utility.encode(secQChallenge.getBytes());
			}

			requestCtx.abortWith(Response.status(Status.UNAUTHORIZED)
					.header("WWW-Authenticate",
							RestAuthenticationMethod.SECURITY_QUESTIONS.getMethod() + challenge)
					.build());
		}

}
