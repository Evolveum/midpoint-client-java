/*
 * Copyright (C) 2019-2022 Evolveum
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
package com.evolveum.midpoint.client.impl.prism;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.fluent.Response;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.client.api.exception.*;
import com.evolveum.midpoint.client.api.scripting.ScriptingUtil;
import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.prism.PrismObject;
import com.evolveum.midpoint.schema.constants.ObjectTypes;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

import org.apache.hc.core5.http.message.BasicClassicHttpResponse;

public class RestPrismService implements Service {

    private PrismContext prismContext;
    private CloseableHttpClient httpClient;
    private String baseUrl;


    RestPrismService (CloseableHttpClient httpClient, String baseUrl, PrismContext prismContext) {
        this.httpClient = httpClient;
        this.prismContext = prismContext;
        this.baseUrl = baseUrl;
    }

    <O extends ObjectType> O getObject(ObjectTypes type, String oid) throws ObjectNotFoundException, SchemaException {

        try {
            String fullPath = baseUrl + "/" + type.getRestType() + "/" + oid;
            Response response = httpGet(fullPath);

            ClassicHttpResponse httpResponse = (ClassicHttpResponse) response.returnResponse();
            switch (httpResponse.getCode()) {
                case 240:
                case 250:
                case HttpStatus.SC_OK :
                    return parseObject(httpResponse.getEntity());
                case HttpStatus.SC_NOT_FOUND:
                    OperationResult result = getOperationResult(httpResponse.getEntity());
                    if (result.getCause() != null) {
                        //TODO error handling?
                    }
                    String message = result.getMessage();
                    throw new ObjectNotFoundException("Cannot find object located at: " + fullPath + ", " + message);
                case HttpStatus.SC_FORBIDDEN:
                    throw new SecurityViolationException("Cannot read object, " + httpResponse.getReasonPhrase());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }


        return null;
    }

    void deleteObject(ObjectTypes type, String oid) throws SchemaException, ObjectNotFoundException {
        String fullPath = baseUrl + "/" + type.getRestType() + "/" + oid;
        Response response = httpDelete(fullPath, null);

        ClassicHttpResponse httpResponse;
        try {
            httpResponse = (ClassicHttpResponse) response.returnResponse();
        } catch (IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
        switch (httpResponse.getCode()) {
            case 240:
            case 250:
            case HttpStatus.SC_OK :
            case HttpStatus.SC_NO_CONTENT:
                return;
            case HttpStatus.SC_NOT_FOUND:
                OperationResult result = getOperationResult(httpResponse.getEntity());
                if (result.getCause() != null) {
                    //TODO error handling?
                }
                String message = result.getMessage();
                throw new ObjectNotFoundException("Cannot find object located at: " + fullPath + ", " + message);
            case HttpStatus.SC_FORBIDDEN:
                throw new SecurityViolationException("Cannot read object, " + httpResponse.getReasonPhrase());
        }
    }

    <O extends ObjectType> O parseObject(HttpEntity httpEntity) throws SchemaException {
        PrismObject<O> object;
        try {

            object = prismContext.parserFor(httpEntity.getContent()).language(getParserLanguage(httpEntity.getContentType())).parse();
        } catch (com.evolveum.midpoint.util.exception.SchemaException | IOException e) {
            throw new SchemaException(e.getMessage(), e);
        }

        return object.asObjectable();
    }

    OperationResult getOperationResult(HttpEntity httpEntity) throws SchemaException {
        OperationResultType result = parseOperationResult(httpEntity);
        return OperationResult.createOperationResult(result);
    }

    OperationResultType parseOperationResult(HttpEntity httpEntity) throws SchemaException {
        try {
            return prismContext.parserFor(httpEntity.getContent()).language(getParserLanguage(httpEntity.getContentType())).parseRealValue(OperationResultType.class);
        } catch (IOException | com.evolveum.midpoint.util.exception.SchemaException e) {
            throw new SchemaException(e.getMessage(), e);
        }
    }

    private String getParserLanguage(String contentType) {
        if (ContentType.APPLICATION_XML.toString().equals(contentType)) {
            return "xml";
        }
        if (ContentType.APPLICATION_JSON.toString().equals(contentType)) {
            return "json";
        }
        if ("application/yaml".equals(contentType)) {
            return "yaml";
        }
        return "xml";
    }

    Response httpGet(String relativePath) throws IOException {
        return Request.get(relativePath).execute(httpClient);
    }

    ClassicHttpResponse httpPost(String relativePath, HttpEntity object) throws SchemaException {
        return httpPost(relativePath, object, null);
    }

    ClassicHttpResponse httpPost(String relativePath, HttpEntity object, List<String> options) throws SchemaException {
        StringBuilder uri = new StringBuilder(baseUrl + "/" + relativePath);
        if (options != null && options.size() > 0) {
            uri.append("?options=");
            for (String option : options) {
                if (options.indexOf(option) > 0) {
                    uri.append(",");
                }
                uri.append(option);
            }
        }
        Request req = Request.post(uri.toString());
        if (object != null) {
            req.body(object);
        }
        try {
            return (ClassicHttpResponse) req.execute(httpClient).returnResponse();
        } catch (IOException e) {
            throw new SchemaException(e.getMessage(), e);
        }
    }

    Response httpDelete(String fullPath, List<String> options) {

        StringBuilder uri = new StringBuilder(fullPath);
        if (options != null && options.size() > 0) {
            uri.append("?options=");
            for (String option : options) {
                if (options.indexOf(option) > 0) {
                    uri.append(",");
                }
                uri.append(option);
            }
        }
        Request req = Request.delete(uri.toString());
        try {
            return req.execute(httpClient);
        } catch (IOException e) {
            throw new SystemException(e.getMessage(), e);
        }
    }

    String addObject(ObjectTypes type, ObjectType object, List<String> options) throws ObjectAlreadyExistsException, SchemaException {
        ClassicHttpResponse httpResponse = httpPost(type.getRestType(), createEntity(object), options);

        switch (httpResponse.getCode()) {
            case HttpStatus.SC_OK:
            case HttpStatus.SC_CREATED:
            case HttpStatus.SC_ACCEPTED:
            case 240:
            case 250:
                try {
                    return getOidFromLocation(httpResponse);
                } catch (ProtocolException e) {
                    throw new IllegalStateException("Unexpected header found: " + e.getMessage(), e);
                }
            case HttpStatus.SC_CONFLICT:
                OperationResult result = getOperationResult(httpResponse.getEntity());
                throw new ObjectAlreadyExistsException(result.getMessage());
            case HttpStatus.SC_FORBIDDEN:
                result = getOperationResult(httpResponse.getEntity());
                throw new SecurityViolationException(result.getMessage());
            default:
                throw new UnsupportedOperationException("Not impelemnted yet: " + httpResponse);
        }
    }

    String getOidFromLocation(HttpResponse httpResponse) throws ProtocolException {
        Header header = httpResponse.getHeader("Location");
        if (header == null) {
            throw new IllegalStateException("No location returned, something went wrong");
        }
        String location = header.getValue();
        if (StringUtils.isNotBlank(location)) {
            return location.substring(location.lastIndexOf("/") + 1);
        }
        return null;
    }

    private <O extends ObjectType> StringEntity createEntity(O object) throws SchemaException {
        try {
            return new StringEntity(prismContext.xmlSerializer().serializeRealValue(object), ContentType.APPLICATION_XML);
        } catch (com.evolveum.midpoint.util.exception.SchemaException e) {
            throw new SchemaException(e);
        }
    }


    @Override
    public FocusCollectionService<UserType> users() {
        return new RestPrismFocusCollectionService<>(this, ObjectTypes.USER);
    }

    @Override
    public <T> RpcService<T> rpc() {
        return null;
    }

    @Override
    public ObjectCollectionService<ValuePolicyType> valuePolicies() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.PASSWORD_POLICY);
    }

    @Override
    public UserType self() throws AuthenticationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Service impersonate(String oid) {
        return null;
    }

    @Override
    public Service removeImpersonate() {
        return null;
    }

    @Override
    public Service addHeader(String header, String value) {
        return null;
    }

    @Override
    public Service removeHeader(String header) {
        return null;
    }

    @Override
    public ObjectCollectionService<ArchetypeType> archetypes() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.ARCHETYPE);
    }

    @Override
    public ObjectCollectionService<LookupTableType> lookupTables() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.LOOKUP_TABLE);
    }

    @Override
    public ObjectCollectionService<SecurityPolicyType> securityPolicies() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.SECURITY_POLICY);
    }

    @Override
    public ObjectCollectionService<ConnectorType> connectors() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.CONNECTOR);
    }

    @Override
    public ObjectCollectionService<ConnectorHostType> connectorHosts() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.CONNECTOR_HOST);
    }

    @Override
    public ObjectCollectionService<GenericObjectType> genericObjects() {
        return null;
    }

    @Override
    public ResourceCollectionService resources() {
        return new RestPrismResourceCollectionService(this);
    }

    @Override
    public ObjectCollectionService<ObjectTemplateType> objectTemplates() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.OBJECT_TEMPLATE);
    }

    @Override
    public ObjectCollectionService<ObjectCollectionType> objectCollections() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.OBJECT_COLLECTION);
    }

    @Override
    public ObjectCollectionService<SequenceType> sequences() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.SEQUENCE);
    }

    @Override
    public ObjectCollectionService<SystemConfigurationType> systemConfigurations() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.SYSTEM_CONFIGURATION);
    }

    @Override
    public ObjectCollectionService<FormType> forms() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.FORM);
    }

    @Override
    public TaskCollectionService tasks() {
        return new RestPrismTaskCollectionService(this);
    }

    @Override
    public ShadowCollectionService shadows() {
        return null;
    }

    @Override
    public FocusCollectionService<RoleType> roles() {
        return new RestPrismFocusCollectionService<>(this, ObjectTypes.ROLE);
    }

    @Override
    public FocusCollectionService<OrgType> orgs() {
        return new RestPrismFocusCollectionService<>(this, ObjectTypes.ORG);
    }

    @Override
    public FocusCollectionService<ServiceType> services() {
        return new RestPrismFocusCollectionService<>(this, ObjectTypes.SERVICE);
    }

    @Override
    public ObjectCollectionService<AccessCertificationDefinitionType> accessCertificationDefinitions() {
        return new RestPrismObjectCollectionService<>(this, ObjectTypes.ACCESS_CERTIFICATION_DEFINITION);
    }

    @Override
    public ServiceUtil util() {
        return null;
    }

    @Override
    public ScriptingUtil scriptingUtil() {
        return null;
    }

    @Override
    public void close() {
        try {
            httpClient.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
