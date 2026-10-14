/*
 * Copyright (c) 2026 Evolveum
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import java.lang.reflect.Field;

import com.evolveum.midpoint.client.api.SearchService;
import com.evolveum.midpoint.client.api.query.AtomicFilterExit;
import com.evolveum.midpoint.client.api.query.FilterEntryOrEmpty;
import com.evolveum.midpoint.xml.ns._public.common.common_3.UserType;
import com.evolveum.prism.xml.ns._public.query_3.QueryType;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Tests construction and XML representation of REST JAXB query filters,
 * including negation, missing-item conditions, and logical operators.
 *
 * The tests verify query generation without executing REST requests.
 */
public class TestQueryBuilder {

    private static final String UNUSED_ENDPOINT = "http://localhost:1/midpoint/ws/rest/";
    private static final String ALTERNATE_EMAIL_PATH = "extension/uwoAlternateEmail";

    private RestJaxbService service;

    @BeforeClass
    public void setUp() throws Exception {
        service = new RestJaxbServiceBuilder()
                .url(UNUSED_ENDPOINT)
                .build();
    }

    @Test
    public void testNotEqualBuildsQuery() throws Exception {
        AtomicFilterExit<UserType> filter = queryBuilder()
                .not()
                .item(itemPath(ALTERNATE_EMAIL_PATH))
                .eq("user@example.com");

        Element not = filterClause(filter);
        assertElementName(not, "not");

        Element equal = onlyElementChild(not);
        assertElementName(equal, "equal");
        assertPath(equal, ALTERNATE_EMAIL_PATH);
        Element value = elementChild(equal, "value");
        assertElementName(value, "value");
        assertEquals(value.getTextContent(), "user@example.com");
        assertEquals(elementChildCount(equal), 2, "An equal filter must contain a path and a value");
    }

    @Test
    public void testIsNullBuildsQuery() throws Exception {
        AtomicFilterExit<UserType> filter = queryBuilder()
                .item(itemPath(ALTERNATE_EMAIL_PATH))
                .isNull();

        Element equal = filterClause(filter);
        assertMissingItemFilter(equal);
    }

    @Test
    public void testNotIsNullBuildsQuery() throws Exception {
        AtomicFilterExit<UserType> filter = queryBuilder()
                .not()
                .item(itemPath(ALTERNATE_EMAIL_PATH))
                .isNull();

        Element not = filterClause(filter);
        assertElementName(not, "not");
        assertMissingItemFilter(onlyElementChild(not));
    }

    @Test
    public void testNotAfterAnd() throws Exception {
        AtomicFilterExit<UserType> filter = queryBuilder()
                .item(itemPath("a"))
                .eq("x")
                .and()
                .not()
                .item(itemPath("b"))
                .eq("y");

        Element and = filterClause(filter);
        assertElementName(and, "and");
        assertEquals(elementChildCount(and), 2, "AND must contain both conditions");

        Element firstCondition = elementChildAt(and, 0);
        assertEqualFilter(firstCondition, "a", "x");

        Element not = elementChildAt(and, 1);
        assertElementName(not, "not");
        Element secondCondition = onlyElementChild(not);
        assertEqualFilter(secondCondition, "b", "y");
    }

    @Test
    public void testNotAfterOr() throws Exception {
        AtomicFilterExit<UserType> filter = queryBuilder()
                .item(itemPath("a"))
                .eq("x")
                .or()
                .not()
                .item(itemPath("b"))
                .eq("y");

        Element or = filterClause(filter);
        assertElementName(or, "or");
        assertEquals(elementChildCount(or), 2, "OR must contain two AND groups");

        Element firstAnd = elementChildAt(or, 0);
        assertElementName(firstAnd, "and");
        Element firstCondition = onlyElementChild(firstAnd);
        assertEqualFilter(firstCondition, "a", "x");

        Element secondAnd = elementChildAt(or, 1);
        assertElementName(secondAnd, "and");
        Element not = onlyElementChild(secondAnd);
        assertElementName(not, "not");
        Element secondCondition = onlyElementChild(not);
        assertEqualFilter(secondCondition, "b", "y");
    }

    @Test
    public void testNotDoesNotLeakAcrossAndOr() throws Exception {
        AtomicFilterExit<UserType> filter = queryBuilder()
                .not()
                .item(itemPath("name"))
                .eq("administrator")
                .and()
                .item(itemPath("givenName"))
                .eq("Admin")
                .or()
                .item(itemPath("familyName"))
                .eq("User");

        Element or = filterClause(filter);
        assertElementName(or, "or");
        assertEquals(elementChildCount(or), 2, "OR must contain two AND groups");

        Element firstAnd = elementChildAt(or, 0);
        assertElementName(firstAnd, "and");
        assertEquals(elementChildCount(firstAnd), 2, "The first AND group must contain two conditions");

        Element not = elementChildAt(firstAnd, 0);
        assertElementName(not, "not");
        Element firstCondition = onlyElementChild(not);
        assertElementName(firstCondition, "equal");
        assertPath(firstCondition, "name");

        Element secondCondition = elementChildAt(firstAnd, 1);
        assertElementName(secondCondition, "equal");
        assertPath(secondCondition, "givenName");

        Element secondAnd = elementChildAt(or, 1);
        assertElementName(secondAnd, "and");
        Element thirdCondition = onlyElementChild(secondAnd);
        assertElementName(thirdCondition, "equal");
        assertPath(thirdCondition, "familyName");
    }

    private FilterEntryOrEmpty<UserType> queryBuilder() {
        return service.users()
                .search()
                .queryFor(UserType.class);
    }

    private static ItemPathType itemPath(String value) {
        ItemPathType path = new ItemPathType();
        path.setValue(value);
        return path;
    }

    private static Element filterClause(AtomicFilterExit<UserType> filter) throws Exception {
        assertNotNull(filter, "The atomic filter builder must not be null");

        SearchService<UserType> searchService = filter.build();
        assertNotNull(searchService, "Building the query must return a search service");

        Field queryField = RestJaxbSearchService.class.getDeclaredField("query");
        queryField.setAccessible(true);
        QueryType query = (QueryType) queryField.get(searchService);

        assertNotNull(query, "The search service must contain the built query");
        assertNotNull(query.getFilter(), "The built query must contain a filter");
        assertNotNull(query.getFilter().getFilterClause(), "The built filter must contain a filter clause");
        return query.getFilter().getFilterClause();
    }

    private static void assertMissingItemFilter(Element equal) {
        assertElementName(equal, "equal");
        assertPath(equal, ALTERNATE_EMAIL_PATH);
        assertEquals(elementChildCount(equal), 1, "A missing-item filter must not contain a value element");
    }

    private static void assertEqualFilter(Element equal, String expectedPath, String expectedValue) {
        assertElementName(equal, "equal");
        assertPath(equal, expectedPath);
        assertEquals(elementChild(equal, "value").getTextContent(), expectedValue);
        assertEquals(elementChildCount(equal), 2, "An equal filter must contain a path and a value");
    }

    private static void assertPath(Element filter, String expectedPath) {
        Element path = elementChild(filter, "path");
        assertElementName(path, "path");
        assertEquals(path.getTextContent(), expectedPath);
    }

    private static Element onlyElementChild(Element parent) {
        assertEquals(elementChildCount(parent), 1, "Expected exactly one child element");
        return firstElementChild(parent);
    }

    private static Element elementChild(Element parent, String name) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element && name.equals(element.getLocalName())) {
                return element;
            }
        }
        throw new AssertionError("No '" + name + "' child in '" + parent.getLocalName() + "'");
    }

    private static Element firstElementChild(Element parent) {
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element) {
                return element;
            }
        }
        throw new AssertionError("No element child in '" + parent.getLocalName() + "'");
    }

    private static Element elementChildAt(Element parent, int index) {
        int currentIndex = 0;
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element element) {
                if (currentIndex == index) {
                    return element;
                }
                currentIndex++;
            }
        }
        throw new AssertionError("No element child at index " + index + " in '" + parent.getLocalName() + "'");
    }

    private static int elementChildCount(Element parent) {
        int count = 0;
        for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
            if (child instanceof Element) {
                count++;
            }
        }
        return count;
    }

    private static void assertElementName(Element element, String expectedName) {
        assertEquals(element.getLocalName(), expectedName);
        assertEquals(element.getNamespaceURI(), SchemaConstants.NS_QUERY);
    }
}
