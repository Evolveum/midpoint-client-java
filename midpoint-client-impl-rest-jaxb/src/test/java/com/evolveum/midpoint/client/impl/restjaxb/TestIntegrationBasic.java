package com.evolveum.midpoint.client.impl.restjaxb;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.namespace.QName;

import com.evolveum.midpoint.client.api.AuthenticationChallenge;
import com.evolveum.midpoint.client.api.Service;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.SchemaException;
import com.evolveum.midpoint.client.api.query.AtomicFilterExit;
import com.evolveum.midpoint.client.api.query.ConditionEntry;
import com.evolveum.prism.xml.ns._public.query_3.PagingType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.query_3.SearchFilterType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

import org.apache.commons.lang3.StringUtils;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.evolveum.midpoint.client.api.ObjectReference;
import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.api.scripting.ObjectProcessingOutput;
import com.evolveum.midpoint.client.api.scripting.OperationSpecificData;
import com.evolveum.midpoint.client.api.scripting.ValueGenerationData;
import com.evolveum.midpoint.xml.ns._public.common.api_types_3.ExecuteScriptResponseType;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.midpoint.xml.ns._public.model.scripting_3.ExecuteScriptType;

/**
 * This is integration test that requires running midPoint (e.g. from other project).
 * This midPoint must have clean midpoint.home, otherwise some tests fail on conflicts.
 * Because of these prerequisites it is NOT part of the Maven build.
 * TODO: Make it less brittle I guess.
 */
public class TestIntegrationBasic extends AbstractTest {

    private static final String ENDPOINT_ADDRESS = "http://localhost:8080/midpoint/ws/rest";

    private static final String TEST_DIR = "src/test/resources/integration";

    private static final String USER_JACK_OID = "229487cb-59b6-490b-879d-7a6d925dd08c";
    private static final File USER_JACK_FILE = new File(TEST_DIR, "user-jack.xml");
    private static final String REQUEST_DIR = "src/test/resources/request";
    private static final File SCRIPT_GENERATE_PASSWORD = new File(REQUEST_DIR, "request-script-generate-passwords.xml");
    private static final File SCRIPT_MODIFY_VALID_TO = new File(REQUEST_DIR, "request-script-modify-validTo.xml");

    private static final File USER_GUYBRUSH_FILE = new File(TEST_DIR, "user-guybrush.xml");
    private static final String USER_GUYBRUSH_OID = "c0c010c0-d34d-b33f-f00d-111111111116";
    private static final String USER_GUYBRUSH_NAME = "guybrush";

    private static final File SECURITY_POLICY_FILE = new File(TEST_DIR, "security-policy-secQ.xml");
    private static final String SECURITY_POLICY_OID = "300b4418-234e-4dbc-ae09-7216c8ea9055";
    private RestJaxbService service;

    @BeforeClass
    public void initService() throws Exception {
        service = getService();
    }

    @Test
    public void test210createUserjack() throws Exception {
        UserType userJack = unmarshallFromFile(UserType.class, USER_JACK_FILE);
        ObjectReference<UserType> userJackRef = service.users().add(userJack).post();
        UserType userJackAfter = userJackRef.get();

        assertNotNull("Unexpected null object", userJackAfter);
    }

    @Test
    public void test211jackResolvePhoto() throws Exception {
        service.users().oid(USER_JACK_OID).get(null, Collections.singletonList("jpegPhoto"), null);

        URI currentUri = service.getCurrentUri();
        String query = currentUri.getQuery();
        System.out.println("query: " + query);
    }

    @Test
    public void test211jackResolvePhotoAgain() throws Exception {
        service.users().oid(USER_JACK_OID).get(null, Collections.singletonList("jpegPhoto"), null);

        //Check service, if query params where correcly handled
        URI currentUri = service.getCurrentUri();
        String query = currentUri.getQuery();
        System.out.println("query: " + query);
    }

    @Test
    public void test212getValuePolicyForJack() throws Exception {
        CredentialsPolicyType credentialsPolicyType = service.users().oid(USER_JACK_OID).credentialsPolicy().get();

        //Default security policy distributed with midPoint should be returned
        PasswordCredentialsPolicyType passwordPolicy = credentialsPolicyType.getPassword();
        assertNotNull("Password policy not provded.", passwordPolicy);
        assertEquals(passwordPolicy.getMinOccurs(), "0", "Min occurs doesn't match.");
        assertEquals(passwordPolicy.getLockoutMaxFailedAttempts().intValue(), 3, "lockoutMaxFailedAttempts doesn't match.");

        assertEquals(createDuration("PT3M").compare(passwordPolicy.getLockoutFailedAttemptsDuration()), 0, "lockoutFailedAttemptsDuration doesn't match.");
        assertEquals(createDuration("PT15M").compare(passwordPolicy.getLockoutDuration()), 0, "lockoutDuration doesn't match.");

        assertNull(credentialsPolicyType.getSecurityQuestions());
        assertNull(credentialsPolicyType.getDefault());
        assertEquals(0, credentialsPolicyType.getNonce().size(), "No nonce configuration expected");
    }

    private Duration createDuration(String lexicalRepresentation) throws DatatypeConfigurationException {
        DatatypeFactory datatypefactory = DatatypeFactory.newInstance();
        return datatypefactory.newDuration(lexicalRepresentation);
    }

    // see analogous test 520 in midPoint TestAbstractRestService
    @Test
    public void test220GeneratePasswordsUsingScripting() throws Exception {
        // WHEN
        ExecuteScriptType request = unmarshallFromFile(ExecuteScriptType.class, SCRIPT_GENERATE_PASSWORD);
        ExecuteScriptResponseType response = service.rpc().executeScript(request).post();

        // THEN
        List<ObjectProcessingOutput<ValueGenerationData<String>>> outputs = service.scriptingUtil()
                .extractPasswordGenerationResults(response);
        System.out.println("extracted outputs:\n" + outputs);
        AssertJUnit.assertEquals("Wrong # of extracted outputs", 2, outputs.size());

        ObjectProcessingOutput<ValueGenerationData<String>> first = outputs.get(0);
        AssertJUnit.assertEquals("Wrong OID in first output", "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX", first.getOid());
        AssertJUnit.assertEquals("Wrong status in first output", OperationResultStatusType.FATAL_ERROR, first.getStatus());
        AssertJUnit.assertNull("Object present in first output", first.getObject());
        AssertJUnit.assertNotNull("Operation result missing in first output", first.getResult());

        ObjectProcessingOutput<ValueGenerationData<String>> second = outputs.get(1);
        AssertJUnit.assertEquals("Wrong OID in second output", USER_JACK_OID, second.getOid());
        AssertJUnit.assertEquals("Wrong name in second output", "jack", second.getName());
        AssertJUnit.assertTrue("Missing password in second output", StringUtils.isNotBlank(second.getData().getValue()));
        AssertJUnit.assertEquals("Wrong status in second output", OperationResultStatusType.SUCCESS, second.getStatus());
        AssertJUnit.assertNotNull("Object missing in second output", second.getObject());
        AssertJUnit.assertNotNull("Operation result missing in second output", second.getResult());

        assertNull("No query should be here", getQuery(service));
    }

    // see analogous test 530 in midPoint TestAbstractRestService
    @Test
    public void test230ModifyValidToUsingScripting() throws Exception {
        // WHEN
        ExecuteScriptType request = unmarshallFromFile(ExecuteScriptType.class, SCRIPT_MODIFY_VALID_TO);
        ExecuteScriptResponseType response = service.rpc().executeScript(request).post();

        // THEN
        List<ObjectProcessingOutput<OperationSpecificData>> outputs = service.scriptingUtil().extractObjectProcessingOutput(response);
        System.out.println("extracted outputs:\n" + outputs);
        AssertJUnit.assertEquals("Wrong # of extracted outputs", 2, outputs.size());

        ObjectProcessingOutput<OperationSpecificData> first = outputs.get(0);
        AssertJUnit.assertEquals("Wrong OID in first output", "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX", first.getOid());
        AssertJUnit.assertEquals("Wrong status in first output", OperationResultStatusType.FATAL_ERROR, first.getStatus());
        AssertJUnit.assertNull("Object present in first output", first.getObject());
        AssertJUnit.assertNotNull("Operation result missing in first output", first.getResult());

        ObjectProcessingOutput<OperationSpecificData> second = outputs.get(1);
        AssertJUnit.assertEquals("Wrong OID in second output", USER_JACK_OID, second.getOid());
        AssertJUnit.assertEquals("Wrong name in second output", "jack", second.getName());
        AssertJUnit.assertEquals("Wrong status in second output", OperationResultStatusType.SUCCESS, second.getStatus());
        AssertJUnit.assertNotNull("Object missing in second output", second.getObject());
        AssertJUnit.assertNotNull("Operation result missing in second output", second.getResult());

        assertNull("No query should be here", getQuery(service));
    }

    private String test300oid;

    @Test
    public void test300OrgAdd() throws Exception {
        OrgType orgBefore = new OrgType();
        orgBefore.setName(service.util().createPoly("test300"));

        // WHEN
        ObjectReference<OrgType> ref = service.orgs().add(orgBefore).post();

        // THEN
        test300oid = ref.getOid();
        assertNotNull("Null oid", test300oid);

        OrgType orgAfter = ref.get();
        Asserts.assertPoly(service, "Wrong name", "test300", orgAfter.getName());

        assertNull("No query should be here", getQuery(service));
    }

    private String test310oid;

    @Test
    public void test310SubOrgAdd() throws Exception {
        OrgType orgBefore = new OrgType();
        orgBefore.setName(service.util().createPoly("test310"));
        ObjectReferenceType parentRef = new ObjectReferenceType();
        parentRef.setOid(test300oid);
        parentRef.setType(new QName("OrgType"));
        AssignmentType assignment = new AssignmentType();
        assignment.setTargetRef(parentRef);
        orgBefore.getAssignment().add(assignment);

        // WHEN
        ObjectReference<OrgType> ref = service.orgs().add(orgBefore).post();

        // THEN
        test310oid = ref.getOid();
        assertNotNull("Null oid", test310oid);

        OrgType orgAfter = ref.get();
        Asserts.assertPoly(service, "Wrong name", "test310", orgAfter.getName());

        assertNull("No query should be here", getQuery(service));
    }

    private String test320oid;

    @Test
    public void test320SubOrgAdd() throws Exception {
        OrgType orgBefore = new OrgType();
        orgBefore.setName(service.util().createPoly("test320"));
        ObjectReferenceType parentRef = new ObjectReferenceType();
        parentRef.setOid(test310oid);
        parentRef.setType(new QName("OrgType"));
        AssignmentType assignment = new AssignmentType();
        assignment.setTargetRef(parentRef);
        orgBefore.getAssignment().add(assignment);

        // WHEN
        ObjectReference<OrgType> ref = service.orgs().add(orgBefore).post();

        // THEN
        test320oid = ref.getOid();
        assertNotNull("Null oid", test320oid);

        OrgType orgAfter = ref.get();
        Asserts.assertPoly(service, "Wrong name", "test320", orgAfter.getName());
        assertNull("No query should be here", getQuery(service));
    }

    @Test
    public void test330OrgDirectChildSearch() throws Exception {
        // WHEN
        SearchResult<OrgType> result = service.orgs().search()
                .queryFor(OrgType.class)
                .isDirectChildOf(test300oid)
                .get();

        // THEN
        assertEquals(result.size(), 1);
        Asserts.assertPoly(service, "Wrong name", "test310", result.get(0).getName());

        assertNull("No query should be here", getQuery(service));
    }

    @Test
    public void test340OrgChildSearch() throws Exception {
        // WHEN
        SearchResult<OrgType> result = service.orgs().search()
                .queryFor(OrgType.class)
                .isChildOf(test300oid)
                .get();

        // THEN
        assertEquals(result.size(), 2);
        Set<String> names = result.stream()
                .map(org -> service.util().getOrig(org.getName()))
                .collect(Collectors.toSet());
        assertEquals(new HashSet<>(Arrays.asList("test310", "test320")), names);

        assertNull("No query should be here", getQuery(service));
    }

    @Test
    public void test350RootSearch() throws Exception {
        // WHEN
        SearchResult<OrgType> result = service.orgs().search()
                .queryFor(OrgType.class)
                .isRoot()
                .get();

        // THEN
        Set<String> names = result.stream()
                .map(org -> service.util().getOrig(org.getName()))
                .collect(Collectors.toSet());
        assertTrue("test300 is not among roots", names.contains("test300"));

        assertNull("No query should be here", getQuery(service));
    }

    //TODO finish test, MID-6851
    @Test
    public void test490modifyReplaceNull() throws Exception {
        service.users().oid(USER_JACK_OID).modify().replace("assignment[1]/activation/validFrom", null).post();
    }

    @Test
    public void test500deleteUserJack() throws Exception {
        service.users().oid(USER_JACK_OID).delete();

        try {
            service.users().oid(USER_JACK_OID).get();
            fail("Unexpected object found");
        } catch (ObjectNotFoundException e) {
            //expected
        }

        assertNull("No query should be here", getQuery(service));
    }

    @Test
    public void test510deleteOrg300() throws Exception {
        service.orgs().oid(test300oid).delete();

        try {
            service.orgs().oid(test300oid).get();
            fail("Unexpected object found");
        } catch (ObjectNotFoundException e) {
            // expected
        }

        assertNull("No query should be here", getQuery(service));
    }

    @Test
    public void test510deleteOrg310() throws Exception {
        service.orgs().oid(test310oid).delete();

        try {
            service.orgs().oid(test310oid).get();
            fail("Unexpected object found");
        } catch (ObjectNotFoundException e) {
            // expected
        }

        assertNull("No query should be here", getQuery(service));
    }

    @Test
    public void test510deleteOrg320() throws Exception {
        service.orgs().oid(test320oid).delete();

        try {
            service.orgs().oid(test320oid).get();
            fail("Unexpected object found");
        } catch (ObjectNotFoundException e) {
            // expected
        }

        assertNull("No query should be here", getQuery(service));
    }

    /**
     * Test for MID-7459, import attached objects from Jira issue before test run
     */
    @Test(enabled = false) // enable after the midPoint is set up appropriately
    public void test600searchDistinct() throws Exception {
        Service service2 = getService("pavol", "western", ENDPOINT_ADDRESS);
        SearchResult<UserType> users = service2.users().search().queryFor(UserType.class)
                .item(createAssignmentTargetRefPath())
                    .ref("be1a87e3-67ae-4ff9-bb84-95a4ce2ba068")
                .and()
                .item(createArtMovementPath())
                    .eq("96658ae1-2b9e-4189-b450-399d6d924889")
                .build().get(Arrays.asList("distinct"));

        for (UserType user : users) {
            System.out.println("user: " + service.util().getOrig(user.getName()));
        }
        assertEquals(users.size(), 10);
    }

    @Test
    public void test610searchUserFilterQuery() throws Exception {
        Service service = getService();

        QueryType query = new QueryType();
        PagingType pagingType = new PagingType();
        pagingType.setMaxSize(1);
        pagingType.setOffset(0);
        query.setPaging(pagingType);
        SearchFilterType searchFilterType = new SearchFilterType();
        searchFilterType.setText("name contains \"admin\" or familyName startsWith \"X\"");
        query.setFilter(searchFilterType);

        SearchResult<UserType> users = service.users().search()
                .queryFor(UserType.class)
                .build(query)
                .options()
                .resolveNames()
                .get();

        assertEquals(users.size(), 1);
    }


    @Test
    public void test700addSecurityPolicy() throws Exception {
        SecurityPolicyType securityPolicyType = unmarshallFromFile(SecurityPolicyType.class, SECURITY_POLICY_FILE);

        service.securityPolicies().add(securityPolicyType).post();

        SecurityPolicyType securityPolicyAfter = service.securityPolicies().oid(SECURITY_POLICY_OID).get();
        assertNotNull(securityPolicyAfter);
        AuthenticationsPolicyType authentication = securityPolicyAfter.getAuthentication();
        assertNotNull(authentication);

        assertTrue(!authentication.getModules().getHttpSecQ().isEmpty());
    }

    @Test
    public void test701modifySystemConfigSecurityPolicy() throws Exception {

        ObjectReferenceType securityPolicyRef = new ObjectReferenceType();
        securityPolicyRef.setOid(SECURITY_POLICY_OID);
        securityPolicyRef.setType(Types.SECURITY_POLICIES.getTypeQName());

        service.systemConfigurations()
                .oid(SystemObjectsType.SYSTEM_CONFIGURATION.value())
                .modify()
                .replace("globalSecurityPolicyRef", securityPolicyRef)
                .post();

        SystemConfigurationType systemConfigurationType = service.systemConfigurations().oid(SystemObjectsType.SYSTEM_CONFIGURATION.value()).get();
        assertNotNull(systemConfigurationType);

        ObjectReferenceType globalSecurityPolicy = systemConfigurationType.getGlobalSecurityPolicyRef();
        assertNotNull(globalSecurityPolicy);
        assertEquals(globalSecurityPolicy.getOid(), SECURITY_POLICY_OID);
    }

    @Test
    public void test710addUserGuybrush() throws Exception {
        UserType userGuybrush= unmarshallFromFile(UserType.class, USER_GUYBRUSH_FILE);

        ObjectReference<UserType> user = service.users().add(userGuybrush).post();

        UserType guybrushAfter = user.get();
        assertNotNull(guybrushAfter.getCredentials().getSecurityQuestions());
    }

    @Test
    public void test720securityQuestionsAuthenticationFailure() throws Exception {
        RestJaxbServiceBuilder builder = new RestJaxbServiceBuilder();

        List<SecurityQuestionAnswer> answers = new ArrayList<>();
        SecurityQuestionAnswer answer = new SecurityQuestionAnswer();
        answer.setQid("id1");
        answer.setQans("wrong answer");
        answers.add(answer);


        builder = builder.url(ENDPOINT_ADDRESS)
                .username(USER_GUYBRUSH_NAME)
                .authentication(AuthenticationType.SECQ)
                .authenticationChallenge(answers);

        RestJaxbService service = builder.build();

        try {
            service.users().oid(SystemObjectsType.USER_ADMINISTRATOR.value()).get();
            fail("authentication should fail");
        } catch (AuthenticationException ex) {
            return;
        }

        fail("Should fail with authentication exception");
    }

    @Test
    public void test730securityQuestionsAuthenticationSuccess() throws Exception {
        RestJaxbServiceBuilder builder = new RestJaxbServiceBuilder();

        List<SecurityQuestionAnswer> answers = new ArrayList<>();
        SecurityQuestionAnswer answer = new SecurityQuestionAnswer();
        answer.setQid("id1");
        answer.setQans("I'm pretty good, thanks for AsKinG");
        answers.add(answer);


        builder = builder.url(ENDPOINT_ADDRESS)
                .username(USER_GUYBRUSH_NAME)
                .authentication(AuthenticationType.SECQ)
                .authenticationChallenge(answers);

        RestJaxbService service = builder.build();

        try {
            service.users().oid(SystemObjectsType.USER_ADMINISTRATOR.value()).get();
        } catch (AuthenticationException ex) {
            fail("authentication should be successful");
        }
    }

    @Test
    public void test740securityQuestionsAuthenticationChallenge() throws Exception {
        RestJaxbServiceBuilder builder = new RestJaxbServiceBuilder();

        builder = builder.url(ENDPOINT_ADDRESS)
                .username(USER_GUYBRUSH_NAME)
                .authentication(AuthenticationType.SECQ);
//                .authenticationChallenge(answers);

        RestJaxbService service = builder.build();

        try {
            service.users().oid(SystemObjectsType.USER_ADMINISTRATOR.value()).get();
            fail("authentication should fail");
        } catch (AuthenticationException ex) {

        }

        AuthenticationChallenge challenge = service.getAuthenticationManager().getChallenge();
        assertTrue(challenge instanceof SecurityQuestionChallenge);

        SecurityQuestionChallenge secQchallenge = (SecurityQuestionChallenge) challenge;
        List<SecurityQuestionAnswer> answers = secQchallenge.getAnswer();
        assertTrue(answers.size() == 2);

        SecurityQuestionAnswer qa1 = answers.get(0);
        assertEquals("id1", qa1.getQid());
        assertEquals("How are you?", qa1.getQtxt());

        SecurityQuestionAnswer qa2 = answers.get(1);
        assertEquals("id2", qa2.getQid());
        assertEquals("What's your favorite color?", qa2.getQtxt());

        //setup answers and try again
        qa1.setQans("I'm pretty good, thanks for AsKinG");
        qa2.setQans("I do NOT have FAVORITE c0l0r!");

        service = (RestJaxbService) getService(USER_GUYBRUSH_NAME, ENDPOINT_ADDRESS, answers);

        try {
            service.users().oid(SystemObjectsType.USER_ADMINISTRATOR.value()).get();

        } catch (AuthenticationException ex) {
            fail("authentication should be successful");
        }
    }

    @Test
    public void test750deleteUserGuybrush() throws Exception {
        service.users().oid(USER_GUYBRUSH_OID).delete();

        try {
            UserType guybrushAfter = service.users().oid(USER_GUYBRUSH_OID).get();
            fail("Unexpected user guybrush found");
        } catch (ObjectNotFoundException e) {
            //expected
        }

    }

    @Test
    public void test760modifySystemConfigSecurityPolicy() throws Exception {

        ObjectReferenceType securityPolicyRef = new ObjectReferenceType();
        securityPolicyRef.setOid(SystemObjectsType.SECURITY_POLICY.value());
        securityPolicyRef.setType(Types.SECURITY_POLICIES.getTypeQName());

        service.systemConfigurations()
                .oid(SystemObjectsType.SYSTEM_CONFIGURATION.value())
                .modify()
                .replace("globalSecurityPolicyRef", securityPolicyRef)
                .post();

        SystemConfigurationType systemConfigurationType = service.systemConfigurations().oid(SystemObjectsType.SYSTEM_CONFIGURATION.value()).get();
        assertNotNull(systemConfigurationType);

        ObjectReferenceType globalSecurityPolicy = systemConfigurationType.getGlobalSecurityPolicyRef();
        assertNotNull(globalSecurityPolicy);
        assertEquals(globalSecurityPolicy.getOid(), SystemObjectsType.SECURITY_POLICY.value());

        //TODO how to clear the env properly?
        service.securityPolicies().oid(SECURITY_POLICY_OID).delete();
    }

    @Test
    public void test770SearchEqPoly() throws SchemaException, ObjectNotFoundException {
        assertFound(searchSystemConfigurationByName().eq("SystemConfiguration"));
        assertNotFound(searchSystemConfigurationByName().eq("systemconfiguration"));

        assertFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration"));
        assertNotFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration1"));
        assertNotFound(searchSystemConfigurationByName().eqPoly("systemconfiguration"));
        assertFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration").matchingStrict());
        assertNotFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration1").matchingStrict());
        assertNotFound(searchSystemConfigurationByName().eqPoly("systemconfiguration").matchingStrict());
        assertFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration").matchingOrig());
        assertNotFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration1").matchingOrig());
        assertNotFound(searchSystemConfigurationByName().eqPoly("systemconfiguration").matchingOrig());
        assertFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration").matchingNorm());
        assertNotFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration1").matchingNorm());
        assertFound(searchSystemConfigurationByName().eqPoly("systemconfiguration").matchingNorm());

        // MidPoint recomputes the "norm" value, so its value does not matter in any way.
        assertFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration", "xxx"));
        assertNotFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration1", "xxx"));
        assertNotFound(searchSystemConfigurationByName().eqPoly("systemconfiguration", "xxx"));
        assertFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration", "xxx").matchingStrict());
        assertNotFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration1", "xxx").matchingStrict());
        assertNotFound(searchSystemConfigurationByName().eqPoly("systemconfiguration", "xxx").matchingStrict());
        assertFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration", "xxx").matchingOrig());
        assertNotFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration1", "xxx").matchingOrig());
        assertNotFound(searchSystemConfigurationByName().eqPoly("systemconfiguration", "xxx").matchingOrig());
        assertFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration", "xxx").matchingNorm());
        assertNotFound(searchSystemConfigurationByName().eqPoly("SystemConfiguration1", "xxx").matchingNorm());
        assertFound(searchSystemConfigurationByName().eqPoly("systemconfiguration", "xxx").matchingNorm());
    }

    private void assertFound(AtomicFilterExit<SystemConfigurationType> entry) throws SchemaException, ObjectNotFoundException {
        SearchResult<SystemConfigurationType> result = entry.get();
        assertEquals(result.size(), 1);
        assertEquals(service.util().getOrig(result.get(0).getName()), "SystemConfiguration");
    }

    private void assertNotFound(AtomicFilterExit<SystemConfigurationType> entry) throws SchemaException, ObjectNotFoundException {
        SearchResult<SystemConfigurationType> result = entry.get();
        assertEquals(result.size(), 0);
    }

    private ConditionEntry<SystemConfigurationType> searchSystemConfigurationByName() {
        var name = new ItemPathType();
        name.setValue("name");

        return service.systemConfigurations().search()
                .queryFor(SystemConfigurationType.class)
                .item(name);
    }

    private ItemPathType createAssignmentTargetRefPath() {
        return service.util()
                .createItemPathType(new QName(SchemaConstants.NS_COMMON, "assignment"), new QName(SchemaConstants.NS_TYPES, "targetRef"));
    }

    private ItemPathType createArtMovementPath() {
        return service.util()
                .createItemPathType(new QName(SchemaConstants.NS_COMMON, "extension"), new QName("http://whatever.com/my", "artMovement"));
    }

    private RestJaxbService getService() throws IOException {
        return (RestJaxbService) getService(ADMIN, ADMIN_PASS, ENDPOINT_ADDRESS);
    }

    private static String getQuery(RestJaxbService service) {
        URI currentUri = service.getCurrentUri();
        String query = currentUri.getQuery();
        System.out.println("query: " + query);
        return query;
    }

}
