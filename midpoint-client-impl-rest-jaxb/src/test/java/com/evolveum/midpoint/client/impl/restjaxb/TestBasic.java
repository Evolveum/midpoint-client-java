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
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.CommonException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.api.exception.PolicyViolationException;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.PolicyItemsDefinitionType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.query_3.PagingType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

import org.apache.cxf.endpoint.Server;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * @author semancik
 */
public class TestBasic extends AbstractTest {

    private static Server server;
    private static final String ENDPOINT_ADDRESS = "http://localhost:18080/midpoint/ws/rest/";
    //TEST
    private static final String _ENDPOINT_ADDRESS = "http://localhost:8080/midpoint/ws/rest/";
    private static final String ADMIN = "administrator";
    private static final String ADMIN_PASS = "5ecr3t";

    @BeforeClass
    public void init() throws IOException {
        server = startServer(ENDPOINT_ADDRESS);
    }

    @AfterClass
    public void destroy() {
        server.stop();
    }

    @Test
    public void test001UserAdd() throws Exception {
        Service service = getService();

        UserType userBefore = new UserType();
        userBefore.setName(service.util().createPoly("foo"));
        userBefore.setOid("123");

        ActivationType activation = new ActivationType();

        XMLGregorianCalendar validFrom = service.util().asXMLGregorianCalendar(new Date());
        activation.setValidFrom(validFrom);
        userBefore.setActivation(activation);

        // WHEN
        ObjectReference<UserType> ref = service.users().add(userBefore)
                .options()
                    .limitPropagation()
                    .isImport()
                .post();

        // THEN
        assertNotNull("Null oid", ref.getOid());
        assertEquals(List.of("limitPropagation", "isImport"), getMockRestService().getLastOptions());
        UserType userAfter = ref.get();
        Asserts.assertPoly(service, "Wrong name", "foo", userAfter.getName());

        // TODO: get user, compare

    }

    @Test
    public void test002UserGet() throws Exception {
        Service service = getService();

        // WHEN
        UserType userType = service.users().oid("123").get();

        // THEN
        assertNotNull("null user", userType);
    }

    @Test
    public void test003UserGetNotExist() throws Exception {
        Service service = getService();

        // WHEN
        try {
            service.users().oid("999").get();
            fail("Unexpected user found");
        } catch (ObjectNotFoundException e) {
            // nothing to do. this is expected
        }
    }

    @Test
    public void test004UserDeleteNotExist() throws Exception {
        Service service = getService();

        // WHEN
        try {
            service.users().oid("999").delete();
            fail("Unexpected user deleted");
        } catch (ObjectNotFoundException e) {
            // nothing to do. this is expected
        }

        try {
            service.users().oid("999")
                    .remove()
                    .options()
                    .limitPropagation()
                    .delete();
            fail("Unexpected user deleted");
        } catch (ObjectNotFoundException e) {
            // nothing to do. this is expected
            assertEquals(List.of("limitPropagation"), getMockRestService().getLastOptions());
        }
    }

    @Test
    public void test005UserModify() throws Exception {
        Service service = getService();
        ServiceUtil util = service.util();

        Map<String, Object> modifications = new HashMap<>();
        modifications.put("description", "test description");

        ObjectReference<UserType> ref = null;

        try {
            ref = service.users().oid("123")
                    .modify()
                    .replace(modifications)
                    .add("givenName", util.createPoly("Charlie"))
                    .options()
                    .reconcile()
                    .post();
        } catch (ObjectNotFoundException e) {
            fail("Cannot modify user, user not found");
        }
        assertEquals(List.of("reconcile"), getMockRestService().getLastOptions());
        UserType user = ref.get();
        assertEquals(user.getDescription(), "test description");
        assertEquals(util.getOrig(user.getGivenName()), "Charlie");
        ref = service.users().oid("123").modify().delete("givenName", util.createPoly("Charlie")).post();

        assertEquals(ref.get().getGivenName(), null);
    }

    @Test
    public void test010UserSearchMock() throws Exception {
        Service service = getService();

        ObjectReferenceType serviceRoleReferenceType = new ObjectReferenceType();
        serviceRoleReferenceType.setOid("00000000-0005-0000-0000-000000000015");
        serviceRoleReferenceType.setType(new QName("RoleType"));

        ItemPathType rolePathType = new ItemPathType();
        rolePathType.setValue("roleMembershipRef");

        ItemPathType assignemtTargetRef = new ItemPathType();
        assignemtTargetRef.setValue("assignment/targetRef");

        ItemPathType namePath = new ItemPathType();
        namePath.setValue("name");

        String jmorr32Oid = "1bae776f-4939-4071-92e2-8efd5bd57799";

        SearchResult<UserType> result = service.users().search()
                .queryFor(UserType.class)
                .item(namePath).eq("aaa")
                .and()
                .item(assignemtTargetRef).ref(serviceRoleReferenceType)
                .maxSize(1000)
                .asc(namePath)
                .build().options()
                .resolveNames()
                .get();

        // THEN
        assertEquals(result.size(), 0);
    }

    @Test
    public void test014UserFilterQuerySearchMock() throws Exception {
        Service service = getService();

        QueryType query = new QueryType();
        PagingType pagingType = new PagingType();
        pagingType.setMaxSize(1);
        pagingType.setOffset(0);
        query.setPaging(pagingType);
        SearchFilterType searchFilterType = new SearchFilterType();
        searchFilterType.setText("name contains \"a\" or familyName startsWith \"X\"");
        query.setFilter(searchFilterType);

        SearchResult<UserType> result = service.users().search()
                .queryFor(UserType.class)
                .build(query)
                .options()
                .resolveNames()
                .get();

        assertEquals(result.size(), 0);
    }


    @Test
    public void test011ValuePolicyGet() throws Exception {
        Service service = getService();

        // WHEN
        ValuePolicyType valuePolicyType = service.valuePolicies().oid("00000000-0000-0000-0000-000000000003").get();

        // THEN
        assertNotNull("null value policy", valuePolicyType);
    }

    @Test
    public void test012SecurityPolicyGet() throws Exception {
        Service service = getService();

        // WHEN
        SecurityPolicyType securityPolicyType =  service.securityPolicies().oid("westernu-0002-0000-0000-000000000001").read()
                .options()
                    .raw()
                    .resolveNames()
                .include("name")
                .include("oid")
                .exclude("jpegPhoto")
                .get();
        // THEN
        assertNotNull("null security policy", securityPolicyType);
        assertEquals(List.of(GetOption.RAW.apiValue(), GetOption.RESOLVE_NAMES.apiValue()), getMockRestService().getLastOptions());
        assertEquals(List.of("name", "oid"), getMockRestService().getLastIncludes());
        assertEquals(List.of("jpegPhoto"), getMockRestService().getLastExcludes());

    }

    @Test
    public void test100challengeRepsonse() throws Exception {
        RestJaxbService service = (RestJaxbService) getService(ADMIN, ENDPOINT_ADDRESS, (List) null);

        try {
            service.users().oid("123").get();
            fail("unexpected success. should fail because of authentication");
        } catch (AuthenticationException ex) {
            //this is expected..
        }

        SecurityQuestionChallenge challenge = (SecurityQuestionChallenge) service.getAuthenticationManager().getChallenge();
        for (SecurityQuestionAnswer qa : challenge.getAnswer()) {
            if ("id1".equals(qa.getQid())) {
                qa.setQans("I'm pretty good, thanks for AsKinG");
            } else {
                qa.setQans("I do NOT have FAVORITE c0l0r!");
            }

        }

        service = (RestJaxbService) getService(ADMIN, ENDPOINT_ADDRESS, challenge.getAnswer());

        try {
            service.users().oid("123").get();

        } catch (AuthenticationException ex) {
            fail("should authenticate user successfully");
        }

    }

    @Test
    public void test200fullChallengeRepsonse() throws Exception {
        RestJaxbService service = (RestJaxbService) getService(null, ENDPOINT_ADDRESS);

        try {
            service.users().oid("123").get();
            fail("unexpected success. should fail because of authentication");
        } catch (AuthenticationException ex) {
            //this is expected..
        }

        List<AuthenticationType> supportedAuthentication = service.getSupportedAuthenticationsByServer();
        assertNotNull("no supported authentication. something wen wrong", supportedAuthentication);
        assertNotEquals(supportedAuthentication.size(), 0);
        AuthenticationType basicAtuh = supportedAuthentication.iterator().next();
        assertEquals(basicAtuh.getType(), AuthenticationType.BASIC.getType(), "expected basic authentication, but got" + basicAtuh);

        service = (RestJaxbService) getService(ADMIN, ADMIN_PASS, ENDPOINT_ADDRESS);

        try {
            service.users().oid("123").get();

        } catch (AuthenticationException ex) {
            fail("should authenticate user successfully");
        }

    }

    @Test
    public void test012Self() throws Exception {
        Service service = getService();

        UserType loggedInUser = null;

        try {
            loggedInUser = service.self();

        } catch (AuthenticationException ex) {
            fail("should authenticate user successfully");
        }

        assertEquals(service.util().getOrig(loggedInUser.getName()), ADMIN);
    }

    @Test
    public void test203rpcValidate() throws Exception {
        Service service = getService();
        PolicyItemsDefinitionType defs = service.rpc().validate()
                .items()
                .item()
                .policy("00000000-0000-0000-0000-000000000003")
                .value("as")
                .build()
                .post();

        boolean allMatch = defs.getPolicyItemDefinition().stream().allMatch(def -> def.getResult().getStatus() == OperationResultStatusType.SUCCESS);
        assertEquals(allMatch, true);
    }

    @Test
    public void test204rpcValidate() throws Exception {
        Service service = getService();
        service.rpc().validate().items()
                .item()
                .value("asdasd")
                .build()
                .post();
    }

    @Test
    public void test205rpcValidateBad() throws Exception {
        Service service = getService();

        service.rpc().validate()
                .items()
                .item()
                .value("asdasd123@#")
                .item()
                .value("asdasdasd345345!!!")
                .item()
                .policy("00000000-0000-0000-0000-p00000000001")
                .value("dfgsdf")
                .build()
                .post();
    }

    @Test
    public void test211rpcGenerate() throws Exception {
        Service service = getService();
        service.rpc().generate()
                .items()
                .item()
                .path("name")
                .build()
                .post();
    }

    @Test
    public void test212rpcGenerate() throws Exception {
        Service service = getService();
        service.rpc().generate()
                .items()
                .item()
                .path("name")
                .item()
                .path("fullName")
                .item()
                .policy("00000000-0000-0000-0000-p00000000001")
                .path("credentials/password/value")
                .build()
                .post();
    }

    @Test
    public void test300UserDelete() throws Exception {
        // SETUP
        Service service = getService();

        // WHEN
        try {
            service.users().oid("123").delete();
        } catch (ObjectNotFoundException e) {
            fail("Cannot delete user, user not found");
        }
    }

    @Test
    public void test301getDeletedUser() throws Exception {
        // SETUP
        Service service = getService();

        // WHEN
        try {
            service.users().oid("123").get();
            fail("Unexpected user found");
        } catch (ObjectNotFoundException e) {
            //expected
        }
    }

    @Test //MID-7686
    public void test401getLookupTableBasedOnName() throws Exception {
        // SETUP
        Service service = getService();
        ItemPathType namePath = new ItemPathType();
        namePath.setValue("name");

        // WHEN
        try {
            SearchService serchService = service.lookupTables().search();

            SearchResult<LookupTableType> result = serchService.queryFor(LookupTableType.class)
                    .item(namePath)
                    .eq("States").matchingOrig()
                    .get();
            // THEN
            assertEquals(result.size(), 0);
        } catch (ObjectNotFoundException e) {
            //expected
        }

    }

    private Service getService() throws IOException {
        return getService(ADMIN, ADMIN_PASS, ENDPOINT_ADDRESS);
    }

}
