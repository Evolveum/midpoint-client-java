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

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;
import org.apache.cxf.common.util.Base64Exception;
import org.apache.cxf.common.util.Base64Utility;
import org.apache.cxf.configuration.security.AuthorizationPolicy;
import org.apache.cxf.jaxrs.utils.JAXRSUtils;
import org.apache.cxf.message.Message;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;

/**
 *
 * @author katkav
 *
 */
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationProvider implements ContainerRequestFilter {

	protected static final String USER_CHALLENGE = "\"user\" : \"username\"";
	protected static final String USER_QUESTION_ANSWER_CHALLENGE = ", \"answer\" :";
	protected static final String QUESTION = "{\"qid\" : \"$QID\", \"qtxt\" : \"$QTXT\"}";

	private static final String Q_ID = "$QID";
	private static final String Q_TXT = "$QTXT";


	private static Map<String, String> usernamePasswordAuthentication;
	private static Map<String, Map<String, String>> usernameSecurityQuestionAuthneticator;

	private static Map<String, String> securityQuestionAnswer;


	static {

		securityQuestionAnswer = new HashMap<>();
		securityQuestionAnswer.put("id1", "How are you?");
		securityQuestionAnswer.put("id2", "What's your favorite color?");

		usernamePasswordAuthentication = new HashMap<>();
		usernamePasswordAuthentication.put("administrator", "5ecr3t");
		usernamePasswordAuthentication.put("jack", "sp4rr0w");

		usernameSecurityQuestionAuthneticator = new HashMap<>();
		Map<String, String> admnistratorQA = new HashMap<>();
		admnistratorQA.put("id1", "I'm pretty good, thanks for AsKinG");
		admnistratorQA.put("id2", "I do NOT have FAVORITE c0l0r!");
		usernameSecurityQuestionAuthneticator.put("administrator", admnistratorQA);
	}

	@Override
	public void filter(ContainerRequestContext requestCtx) throws IOException {
		Message m = JAXRSUtils.getCurrentMessage();

		AuthorizationPolicy policy = (AuthorizationPolicy) m.get(AuthorizationPolicy.class);
		if (policy != null) {
			String enteredUsername = policy.getUserName();

	        if (enteredUsername == null){
	        	RestMockServiceUtil.createAbortMessage(requestCtx);
	        	return;
	        }

	        //TODO: better impelemtnation:

	        String password = usernamePasswordAuthentication.get(enteredUsername);
	        if (password == null) {
	        	requestCtx.abortWith(Response.status(Status.UNAUTHORIZED).build());
	        	return;
	        }

	        if (password.equals(policy.getPassword())) {
	        	//successfull authn
	        } else {
	        	requestCtx.abortWith(Response.status(Status.UNAUTHORIZED).build());
	        }
			return;
		}

		String authorization = requestCtx.getHeaderString("Authorization");

		if (StringUtils.isBlank(authorization)){
			RestMockServiceUtil.createAbortMessage(requestCtx);
			return;
		}

		String[] parts = authorization.split(" ");
		String authenticationType = parts[0];

		if (parts.length == 1) {
			if (RestAuthenticationMethod.SECURITY_QUESTIONS.equals(authenticationType)) {
				RestMockServiceUtil.createSecurityQuestionAbortMessage(requestCtx, "{\"user\" : \"username\"}");
				return;
			}
		}

		if (parts.length != 2 || (!RestAuthenticationMethod.SECURITY_QUESTIONS.equals(authenticationType))) {
			RestMockServiceUtil.createAbortMessage(requestCtx);
			return;
		}
		String base64Credentials = (parts.length == 2) ? parts[1] : null;
		try {
			String decodedCredentials = new String(Base64Utility.decode(base64Credentials));
			if (RestAuthenticationMethod.SECURITY_QUESTIONS.equals(authenticationType)) {

				policy = new AuthorizationPolicy();
				policy.setAuthorizationType(RestAuthenticationMethod.SECURITY_QUESTIONS.getMethod());
				policy.setAuthorization(decodedCredentials);
			}
			if (!handleSecurityQuestionRequest(policy, m, requestCtx)) {
				return;
			}

		} catch (Base64Exception e) {
			RestMockServiceUtil.createSecurityQuestionAbortMessage(requestCtx, "{\"user\" : \"username\"}");
			return;

		}
	}

	private boolean handleSecurityQuestionRequest(AuthorizationPolicy policy, Message message, ContainerRequestContext requestCtx) {
		JsonFactory f = new JsonFactory();
		ObjectMapper mapper = new ObjectMapper(f);
		JsonNode node = null;
			try {
				node = mapper.readTree(policy.getAuthorization());
			} catch (IOException e) {
				RestMockServiceUtil.createSecurityQuestionAbortMessage(requestCtx, "{" + USER_CHALLENGE + "}");
				return false;
			}
			JsonNode userNameNode = node.findPath("user");
			if (userNameNode instanceof MissingNode) {
				RestMockServiceUtil.createSecurityQuestionAbortMessage(requestCtx, "{" + USER_CHALLENGE + "}");
				return false;
			}
			String userName = userNameNode.asText();
			policy.setUserName(userName);
			JsonNode answerNode = node.findPath("answer");

			if (answerNode instanceof MissingNode) {
				Map<String, String> questionAnswer = usernameSecurityQuestionAuthneticator.get(userName);

				if (questionAnswer == null) {
					requestCtx.abortWith(Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Security question authentication failed. Incorrect username and/or password").build());
					return false;
				}

				if (questionAnswer.isEmpty()){
					requestCtx.abortWith(Response.status(Status.UNAUTHORIZED).header("WWW-Authenticate", "Security question authentication failed. Incorrect username and/or password").build());
					return false;
				}


				String questionChallenge = "";
				Set<Entry<String, String>> securityQuestionAnswerValues = securityQuestionAnswer.entrySet();
				Iterator<Entry<String, String>> securityQuestionAnswerValuesIterator = securityQuestionAnswerValues.iterator();
				while (securityQuestionAnswerValuesIterator.hasNext()) {
					Entry<String, String> questionAnswerEntry = securityQuestionAnswerValuesIterator.next();
					//TODO: this implementation doesn't take iuser's QA into an account. improve implementation if needed
					String challenge = QUESTION.replace(Q_ID, questionAnswerEntry.getKey());
					questionChallenge += challenge.replace(Q_TXT, questionAnswerEntry.getValue());
					if (securityQuestionAnswerValuesIterator.hasNext()) {
						questionChallenge += ",";
					}
				}

				String userChallenge = USER_CHALLENGE.replace("username", userName);
				String challenge = "{" + userChallenge + ", \"answer\" : [" + questionChallenge + "]}";
				RestMockServiceUtil.createSecurityQuestionAbortMessage(requestCtx, challenge);
				return false;

			}
			ArrayNode answers = (ArrayNode) answerNode;
			Iterator<JsonNode> answersList = answers.elements();
			Map<String, String> questionAnswers = new HashMap<>();
			while (answersList.hasNext()) {
				JsonNode answer = answersList.next();
				String questionId = answer.findPath("qid").asText();
				String questionAnswer = answer.findPath("qans").asText();
				questionAnswers.put(questionId, questionAnswer);
			}
			return true;
	}
}
