/*
 * Copyright (c) 2021 Evolveum
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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.testng.AssertJUnit;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.evolveum.midpoint.client.api.ObjectReference;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.schema.result.OperationResult;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.PolyStringType;

import static org.testng.AssertJUnit.*;

public class TestRestPrismService {

    private static final String DATA_DIR = "src/test/resources/request";

    private Service service;
    private String testUserOid;

    @BeforeClass
    public void init() throws Exception {
        service = createClient();

        //Create a user to later use in tests, optionally set other fields...
        UserType testUser = new UserType();
        testUser.setName(new PolyStringType("testUser"));
        ObjectReference<UserType> ref = service.users().add(testUser).post();
        assertNotNull(ref.getOid());
        testUserOid = ref.getOid();
    }

    @AfterClass
    public void shutdown() throws ObjectNotFoundException {
        service.users().oid(testUserOid).delete();
        service.close();
    }

    @Test
    public void test001getUser() throws Exception {

        UserType user = service.users().oid(SystemObjectsType.USER_ADMINISTRATOR.value()).read().get();

        System.out.println("User : " + user.getName());

        AssertJUnit.assertEquals("administrator", user.getName().getOrig());
    }

    @Test
    public void test002getUserNotFound() throws Exception {
        try {
            service.users().oid("not-exists").read().get();
            AssertJUnit.fail("ObejctNotFoundException should be thrown.");
        } catch (ObjectNotFoundException e) {
            //this is expected
            System.out.println(e.getMessage());
        }

    }

    @Test
    public void test003getSystemConfig() throws Exception {
        SystemConfigurationType systemConfigurationType = service.systemConfigurations().oid(SystemObjectsType.SYSTEM_CONFIGURATION.value()).read().get();
        AssertJUnit.assertNotNull(systemConfigurationType);
    }

    @Test
    public void test004getTask() throws Exception {
        TaskType taskType = service.tasks().oid(SystemObjectsType.TASK_CLEANUP.value()).read().get();
        AssertJUnit.assertNotNull(taskType);
    }

    private String userOid = null;
    @Test
    public void test010addUser() throws Exception {
        UserType user = new UserType().name("00clientUser").givenName("given").familyName("family");
        ObjectReference<UserType> ref = service.users().add(user).post();

        AssertJUnit.assertNotNull(ref.getOid());
        userOid = ref.getOid();
    }

    @Test
    public void test019deleteUser() throws Exception {
        service.users().oid(userOid).delete();

        try {
            UserType user = service.users().oid(userOid).read().get();
            AssertJUnit.fail("Unexpected user found: " + user);
        } catch (ObjectNotFoundException e) {
            //expected
        }

    }

    /**
     * Calls the modify service using setModifications(InputStream)
     * and verifies that the returned OID matches the test user’s OID. (HTTP PATCH)
     * Org in filter can be found in overlay example project https://github.com/Evolveum/midpoint-overlay-example/blob/master/src/main/resources/initial-objects/920-org-root.xml
     */
    @Test
    public void test030ModifyUser() throws Exception {
        File file = new File(DATA_DIR, "user-patch-delta.json");

        try (InputStream inputStream = new FileInputStream(file)) {
            // Call the modify service using setModifications(inputStream) and then post the changes.
            ObjectReference<UserType> userRef = service.users().oid(testUserOid)
                    .modify()
                    .setModifications(inputStream)
                    .post();

            assertNotNull(userRef.getOid());
            assertEquals("Modified OID should match the test user's OID.", testUserOid, userRef.getOid());
        }
    }

    @Test
    public void test031ModifyUser() throws Exception {
        Map<String, Object> modifications = new HashMap<>();
        modifications.put("description", "test description");

        ObjectReference<UserType> ref = null;

        try {
            ref = service.users().oid(testUserOid)
                    .modify()
                    .replace(modifications)
                    .add("givenName", "Charlie")
                    .post();
        } catch (ObjectNotFoundException e) {
            fail("Cannot modify user, user not found");
        }

        UserType user = ref.get();
        assertEquals("test description", user.getDescription());
        assertEquals("Charlie", user.getGivenName().getOrig());
        ref = service.users().oid(testUserOid).modify().delete("givenName", "Charlie").post();

        assertNull(ref.get().getGivenName());
    }



    @Test
    public void test100testResource() throws Exception {

        OperationResultType resultType = service.resources().oid("ac5199fb-c5bd-46c3-a549-d82e8fd30dc2").test().post();
        OperationResult result = OperationResult.createOperationResult(resultType);

        AssertJUnit.assertTrue(result.isSuccess());
    }

    @Test
    public void test120testResourceDown() throws Exception {

        OperationResultType resultType = service.resources().oid("2a7c7130-7a34-11e4-bdf6-001e8c717e5b").test().post();
        OperationResult result = OperationResult.createOperationResult(resultType);

        AssertJUnit.assertTrue(result.isFatalError());
    }

    private Service createClient() throws Exception {
        RestPrismServiceBuilder builder = RestPrismServiceBuilder.create();
        return builder.username("administrator")
                .password("5ecr3t")
                .baseUrl("http://localhost:8080/midpoint/ws/rest")
                .build();
    }


}
