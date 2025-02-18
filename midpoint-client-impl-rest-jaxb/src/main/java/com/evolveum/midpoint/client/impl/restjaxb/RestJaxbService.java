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

import static java.util.Collections.singletonList;

import com.evolveum.midpoint.client.api.*;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.AuthorizationException;
import com.evolveum.midpoint.client.api.exception.InternalServerErrorException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.api.exception.PartialErrorException;
import com.evolveum.midpoint.client.api.scripting.ScriptingUtil;
import com.evolveum.midpoint.client.impl.restjaxb.scripting.ScriptingUtilImpl;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;

import org.apache.cxf.jaxrs.client.ClientConfiguration;
import org.apache.cxf.jaxrs.client.WebClient;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author semancik
 * @author katkav
 */
public class RestJaxbService implements Service {

    private static final String IMPERSONATE_HEADER = "Switch-To-Principal";
    private final ServiceUtil util;
    private final ScriptingUtil scriptingUtil;

    private final WebClient client;
    private DomSerializer domSerializer;
    private JAXBContext jaxbContext;
    private AuthenticationManager<?> authenticationManager;
    private List<AuthenticationType> supportedAuthenticationsByServer;

    public DomSerializer getDomSerializer() {
        return domSerializer;
    }

    public JAXBContext getJaxbContext() {
        return jaxbContext;
    }

    public List<AuthenticationType> getSupportedAuthenticationsByServer() {
        if (supportedAuthenticationsByServer == null) {
            supportedAuthenticationsByServer = new ArrayList<>();
        }
        return supportedAuthenticationsByServer;
    }

    @SuppressWarnings("unchecked")
    public <T extends AuthenticationChallenge> AuthenticationManager<T> getAuthenticationManager() {
        return (AuthenticationManager<T>) authenticationManager;
    }

    ClientConfiguration getClientConfiguration() {
        return WebClient.getConfig(client);
    }

    public RestJaxbService() {
        super();
        client = WebClient.create("");
        util = new RestJaxbServiceUtil(null);
        scriptingUtil = new ScriptingUtilImpl(util);
    }

    RestJaxbService(String endpoint, String username, String password, AuthenticationType authentication, List<SecurityQuestionAnswer> secQ) throws IOException {
        super();
        try {
            jaxbContext = createJaxbContext();
        } catch (JAXBException e) {
            throw new IOException(e);
        }

        if (AuthenticationType.SECQ == authentication) {
            authenticationManager = new SecurityQuestionAuthenticationManager(username, secQ);
        } else if (authentication != null) {
            authenticationManager = new BasicAuthenticationManager(username, password);
        }

        util = new RestJaxbServiceUtil(jaxbContext);
        scriptingUtil = new ScriptingUtilImpl(util);
        domSerializer = new DomSerializer(jaxbContext);

        CustomAuthNProvider<?> authNProvider = new CustomAuthNProvider<>(authenticationManager, this);
        client = WebClient.create(endpoint, singletonList(new JaxbXmlProvider<>(jaxbContext)));
        ClientConfiguration config = WebClient.getConfig(client);
        config.getInInterceptors().add(authNProvider);
        config.getInFaultInterceptors().add(authNProvider);
        client.accept(MediaType.APPLICATION_XML);
        client.type(MediaType.APPLICATION_XML);

        if (authenticationManager != null) {
            client.header("Authorization", authenticationManager.createAuthorizationHeader());
        }

    }

    /**
     * Add Switch-To-Principal HTTP header with only one value.
     * If this method is executed multiple times than latest oid value will be used.
     * @return this Service instance
     */
    @Override
    public Service impersonate(String oid) {
        client.replaceHeader(IMPERSONATE_HEADER, oid);
        return this;
    }

    /**
     * Remove Switch-To-Principal HTTP header.
     * @return this Service instance
     */
    @Override
    public Service removeImpersonate() {
        client.replaceHeader(IMPERSONATE_HEADER, null);
        return this;
    }

    @Override
    public Service addHeader(String header, String value) {
        client.header(header, value);
        return this;
    }

    /**
     * Remove any HTTP header.
     * @return this Service instance
     */
    @Override
    public Service removeHeader(String header) {
        client.replaceHeader(header, null);
        return this;
    }

    @Override
    public FocusCollectionService<UserType> users() {
        return new RestJaxbFocusCollectionService<>(this, Types.USERS.getRestPath(), UserType.class);
    }

    @Override
    public ObjectCollectionService<ValuePolicyType> valuePolicies() {
        return new RestJaxbObjectCollectionService<>(this, Types.VALUE_POLICIES.getRestPath(), ValuePolicyType.class);
    }

    @Override
    public ObjectCollectionService<ArchetypeType> archetypes() {
        return new RestJaxbObjectCollectionService<>(this, Types.ARCHETYPES.getRestPath(), ArchetypeType.class);
    }

    @Override
    public ObjectCollectionService<LookupTableType> lookupTables() {

        return new RestJaxbObjectCollectionService<>(this, Types.LOOKUP_TABLE.getRestPath(), LookupTableType.class);
        //return null;
    }

    @Override
    public ObjectCollectionService<SecurityPolicyType> securityPolicies() {
        return new RestJaxbObjectCollectionService<>(this, Types.SECURITY_POLICIES.getRestPath(), SecurityPolicyType.class);
    }

    @Override
    public ObjectCollectionService<ConnectorType> connectors() {
        return new RestJaxbObjectCollectionService<>(this, Types.CONNECTORS.getRestPath(), ConnectorType.class);
    }

    @Override
    public ObjectCollectionService<ConnectorHostType> connectorHosts() {
        return new RestJaxbObjectCollectionService<>(this, Types.CONNECTOR_HOSTS.getRestPath(), ConnectorHostType.class);
    }

    @Override
    public ObjectCollectionService<GenericObjectType> genericObjects() {
        return new RestJaxbObjectCollectionService<>(this, Types.GENERIC_OBJECTS.getRestPath(), GenericObjectType.class);
    }

    @Override
    public ResourceCollectionService resources() {
        return new RestJaxbResourceCollectionService(this);
    }

    @Override
    public ObjectCollectionService<ObjectTemplateType> objectTemplates() {
        return new RestJaxbObjectCollectionService<>(this, Types.OBJECT_TEMPLATES.getRestPath(), ObjectTemplateType.class);
    }

    @Override
    public ObjectCollectionService<ObjectCollectionType> objectCollections() {
        return new RestJaxbObjectCollectionService<>(this, Types.OBJECT_COLLECTIONS.getRestPath(), ObjectCollectionType.class);
    }

    @Override
    public ObjectCollectionService<SequenceType> sequences() {
        return new RestJaxbObjectCollectionService<>(this, Types.SEQUENCES.getRestPath(), SequenceType.class);
    }

    @Override
    public ObjectCollectionService<SystemConfigurationType> systemConfigurations() {
        return new RestJaxbObjectCollectionService<>(this, Types.SYSTEM_CONFIGURATIONS.getRestPath(), SystemConfigurationType.class);
    }

    @Override
    public ObjectCollectionService<FormType> forms() {
        return new RestJaxbObjectCollectionService<>(this, Types.FORMS.getRestPath(), FormType.class);
    }

    @Override
    public TaskCollectionService tasks() {
        return new RestJaxbTaskCollectionService(this);
    }

    @Override
    public ShadowCollectionService shadows() {
        return new RestJaxbShadowCollectionService(this);
    }

    @Override
    public FocusCollectionService<RoleType> roles() {
        return new RestJaxbFocusCollectionService<>(this, Types.ROLES.getRestPath(), RoleType.class);
    }

    @Override
    public FocusCollectionService<OrgType> orgs() {
        return new RestJaxbFocusCollectionService<>(this, Types.ORGS.getRestPath(), OrgType.class);
    }

    @Override
    public FocusCollectionService<ServiceType> services() {
        return new RestJaxbFocusCollectionService<>(this, Types.SERVICES.getRestPath(), ServiceType.class);
    }

    @Override
    public <T> RpcService<T> rpc() {
        return new RestJaxbRpcService<>(this);
    }

    @Override
    public ServiceUtil util() {
        return util;
    }

    @Override
    public ScriptingUtil scriptingUtil() {
        return scriptingUtil;
    }

    <O extends ObjectType> O getObject(final Class<O> type, final String oid)
            throws ObjectNotFoundException, AuthenticationException, AuthorizationException, InternalServerErrorException {
        return getObject(type, oid, null, null, null);
    }

    /**
     * Used frequently at several places. Therefore unified here.
     *
     * @throws ObjectNotFoundException
     */
    <O extends ObjectType> O getObject(final Class<O> type, final String oid, List<String> options,
            List<String> include, List<String> exclude)
            throws ObjectNotFoundException, AuthenticationException, AuthorizationException, InternalServerErrorException {

        String urlPrefix = RestUtil.subUrl(Types.findType(type).getRestPath(), oid);
        WebClient cli = client.replacePath(urlPrefix);
        client.resetQuery();
        addQueryParameter("options", options);
        addQueryParameter("include", include);
        addQueryParameter("exclude", exclude);

        Response response = cli.get();

        if (isOk(response.getStatus())) {
            return response.readEntity(type);
        }

        if (Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
            throw new ObjectNotFoundException("Cannot get object with oid " + oid + ". Object doesn't exist");
        }

        if (Status.UNAUTHORIZED.getStatusCode() == response.getStatus()) {
            throw new AuthenticationException(response.getStatusInfo().getReasonPhrase());
        }

        if (Status.FORBIDDEN.getStatusCode() == response.getStatus()) {
            throw new AuthorizationException(response.getStatusInfo().getReasonPhrase());
        }

        if (Status.INTERNAL_SERVER_ERROR.getStatusCode() == response.getStatus()) {
            throw new InternalServerErrorException(response.getStatusInfo().getReasonPhrase());
        }
        return null;
    }

    protected static boolean isOk(int statusCode) {
        return statusCode == 200 || statusCode == 240 || statusCode == 250;
    }

    private void addQueryParameter(String name, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }

        for (String value : values) {
            client.query(name, value);
        }
    }

    <T> Response post(String path, T object) throws ObjectNotFoundException {
        return post(path, object, null);
    }

    <T> Response post(String path, T object, Map<String, List<String>> queryParams) throws ObjectNotFoundException {

        client.resetQuery();
        addQueryParameters(queryParams);
        Response response = client.replacePath("/" + path).post(object);
        handleCommonStatuses(response);
        return response;
    }

    <T> Response put(String path, T object, Map<String, List<String>> queryParams) throws ObjectNotFoundException {

        client.resetQuery();
        addQueryParameters(queryParams);
        Response response = client.replacePath("/" + path).put(object);
        handleCommonStatuses(response);
        return response;
    }

    Response get(String path) throws ObjectNotFoundException {
        client.resetQuery();
        Response response = client.replacePath("/" + path).get();
        handleCommonStatuses(response);
        return response;
    }

    private void handleCommonStatuses(Response response) throws ObjectNotFoundException {
        switch (response.getStatus()) {
            case 250:
                throw new PartialErrorException(response.getStatusInfo().getReasonPhrase());
            case 400:
                throw new BadRequestException(response.getStatusInfo().getReasonPhrase());
            case 401:
                throw new AuthenticationException(response.getStatusInfo().getReasonPhrase());
            case 403:
                throw new AuthorizationException(response.getStatusInfo().getReasonPhrase());
                //TODO: Do we want to return a reference? Might be useful.
            case 404:
                throw new ObjectNotFoundException(response.getStatusInfo().getReasonPhrase());
            case 500:
                throw new InternalServerErrorException(response.getStatusInfo().getReasonPhrase());
        }
    }

    private void addQueryParameters(Map<String, List<String>> queryParams) {
        if (queryParams == null) {
            return;
        }

        for (Map.Entry<String, List<String>> entry : queryParams.entrySet()) {
            addQueryParameter(entry.getKey(), entry.getValue());
        }
    }

    <O extends ObjectType> void deleteObject(final Class<O> type, final String oid) throws BadRequestException, ObjectNotFoundException, AuthenticationException, InternalServerErrorException {
        deleteObject(type, oid, null);
    }
    <O extends ObjectType> void deleteObject(final Class<O> type, final String oid, List<String> options) throws BadRequestException, ObjectNotFoundException, AuthenticationException, InternalServerErrorException {
        String urlPrefix = RestUtil.subUrl(Types.findType(type).getRestPath(), oid);

        client.resetQuery();
        if (options != null && !options.isEmpty()) {
            addQueryParameter("options", options);
        }
        Response response = client.replacePath(urlPrefix).delete();

        //TODO: Looks like midPoint returns a 204 and not a 200 on success
        if (Status.OK.getStatusCode() == response.getStatus()) {
            //TODO: Do we want to return anything on successful delete or just remove this if block?
        }

        if (Status.NO_CONTENT.getStatusCode() == response.getStatus()) {
            //TODO: Do we want to return anything on successful delete or just remove this if block?
        }

        if (Status.BAD_REQUEST.getStatusCode() == response.getStatus()) {
            throw new BadRequestException("Bad request");
        }

        if (Status.NOT_FOUND.getStatusCode() == response.getStatus()) {
            throw new ObjectNotFoundException("Cannot delete object with oid " + oid + ". Object doesn't exist");
        }

        if (Status.UNAUTHORIZED.getStatusCode() == response.getStatus()) {
            throw new AuthenticationException("Cannot authentication user");
        }

        if (Status.INTERNAL_SERVER_ERROR.getStatusCode() == response.getStatus()) {
            throw new InternalServerErrorException(response.getStatusInfo().getReasonPhrase());
        }
    }

    @Override
    public UserType self() {
        String urlPrefix = "/self";
        Response response = client.replacePath(urlPrefix).get();

        if (Status.OK.getStatusCode() == response.getStatus()) {
            return response.readEntity(UserType.class);
        }

        if (Status.BAD_REQUEST.getStatusCode() == response.getStatus()) {
            throw new BadRequestException("Bad request");
        }

        if (Status.UNAUTHORIZED.getStatusCode() == response.getStatus()) {
            throw new AuthenticationException("Cannot authentication user");
        }

        if (Status.INTERNAL_SERVER_ERROR.getStatusCode() == response.getStatus()) {
            throw new InternalServerErrorException(response.getStatusInfo().getReasonPhrase());
        }
        return null;
    }

    private JAXBContext createJaxbContext() throws JAXBException {
        return JAXBContext.newInstance("com.evolveum.midpoint.xml.ns._public.common.api_types_3:"
                + "com.evolveum.midpoint.xml.ns._public.common.audit_3:"
                + "com.evolveum.midpoint.xml.ns._public.common.common_3:"
                + "com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_extension_3:"
                + "com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3:"
                + "com.evolveum.midpoint.xml.ns._public.connector.icf_1.resource_schema_3:"
                + "com.evolveum.midpoint.xml.ns._public.model.extension_3:"
                + "com.evolveum.midpoint.xml.ns._public.model.scripting_3:"
                + "com.evolveum.midpoint.xml.ns._public.model.scripting.extension_3:"
                + "com.evolveum.midpoint.xml.ns._public.report.extension_3:"
                + "com.evolveum.midpoint.xml.ns._public.resource.capabilities_3:"
                + "com.evolveum.midpoint.xml.ns._public.task.jdbc_ping.handler_3:"
                + "com.evolveum.midpoint.xml.ns._public.task.noop.handler_3:"
                + "com.evolveum.prism.xml.ns._public.annotation_3:"
                + "com.evolveum.prism.xml.ns._public.query_3:"
                + "com.evolveum.prism.xml.ns._public.types_3");
    }

    //TODO make something smarter - this is actually neede just for tests
    public URI getCurrentUri() {
        return client.getCurrentURI();
    }

    @Override
    public void close() {
        client.close();
    }
}
