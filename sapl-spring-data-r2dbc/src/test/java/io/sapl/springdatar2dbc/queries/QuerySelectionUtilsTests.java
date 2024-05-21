/*
 * Copyright (C) 2017-2024 Dominic Heutelbeck (dominic@heutelbeck.com)
 *
 * SPDX-License-Identifier: Apache-2.0
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
package io.sapl.springdatar2dbc.queries;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import io.sapl.springdatar2dbc.database.Person;

class QuerySelectionUtilsTests {

    private static ObjectMapper MAPPER = new ObjectMapper();
    private static ArrayNode    SELECTIONS_BLACKLIST;
    private static ArrayNode    SELECTIONS_WHITELIST;
    private static ArrayNode    SELECTIONS_ALIAS_BLACKLIST;
    private static ArrayNode    SELECTIONS_ALIAS_WHITELIST;
    private static ArrayNode    SELECTIONS_ALIAS_IS_EMPTY_WHITELIST;
    private static ArrayNode    TRANSFORMATIONS;

    @BeforeAll
    static void initJsonNodes() throws JsonProcessingException {

        SELECTIONS_BLACKLIST = MAPPER.readValue("""
                [
                	{
                		"type": "blacklist",
                		"columns": ["firstname"]
                	},
                	{
                		"type": "whitelist",
                		"columns": ["age"]
                	}
                ]
                """, ArrayNode.class);

        SELECTIONS_WHITELIST = MAPPER.readValue("""
                [
                	{
                		"type": "whitelist",
                		"columns": ["age","active"]
                	}
                ]
                """, ArrayNode.class);

        SELECTIONS_ALIAS_BLACKLIST = MAPPER.readValue("""
                [
                	{
                		"type": "blacklist",
                		"columns": ["firstname", "age"],
                		"alias" : "p"
                	}
                ]
                """, ArrayNode.class);

        SELECTIONS_ALIAS_WHITELIST = MAPPER.readValue("""
                [
                	{
                		"type": "whitelist",
                		"columns": ["firstname", "age"]
                	}
                ]
                """, ArrayNode.class);

        SELECTIONS_ALIAS_IS_EMPTY_WHITELIST = MAPPER.readValue("""
                [
                	{
                		"type": "whitelist",
                		"columns": ["firstname", "age"]
                	}
                ]
                """, ArrayNode.class);
        TRANSFORMATIONS                     = MAPPER.readValue("""
                [
                	{
                		"firstname": "UPPER"
                	},
                	{
                		"birthday": "YEAR"
                	}
                ]
                """, ArrayNode.class);

    }

    @Test
    void when_createSelectionPartForMethodNameQuery_then_createSelectionPartBlacklist() {
        // GIVEN
        var expected = "SELECT id,age,active FROM XXXXX WHERE ";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForMethodNameQuery(SELECTIONS_BLACKLIST, TRANSFORMATIONS,
                Person.class);

        // THEN
        assertEquals(result, expected);
    }

    @Test
    void when_createSelectionPartForMethodNameQuery_then_createSelectionPartWhitelist() {
        // GIVEN
        var expected = "SELECT age,active FROM XXXXX WHERE ";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForMethodNameQuery(SELECTIONS_WHITELIST, TRANSFORMATIONS,
                Person.class);

        // THEN
        assertEquals(result, expected);
    }

    @Test
    void when_createSelectionPartForMethodNameQuery_then_createSelectionPartAlias1() {
        // GIVEN
        var expected = "SELECT id,active FROM XXXXX WHERE ";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForMethodNameQuery(SELECTIONS_ALIAS_BLACKLIST,
                TRANSFORMATIONS, Person.class);

        // THEN
        assertEquals(result, expected);
    }

    @Test
    void when_createSelectionPartForMethodNameQuery_then_createSelectionPartAlias2() {
        // GIVEN
        var expected = "SELECT UPPER(firstname),age FROM XXXXX WHERE ";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForMethodNameQuery(SELECTIONS_ALIAS_WHITELIST,
                TRANSFORMATIONS, Person.class);

        // THEN
        assertEquals(result, expected);
    }

    @Test
    void when_createSelectionPartForMethodNameQuery_then_createSelectionPartAlias3() {
        // GIVEN
        var expected = "SELECT UPPER(firstname),age FROM XXXXX WHERE ";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForMethodNameQuery(SELECTIONS_ALIAS_IS_EMPTY_WHITELIST,
                TRANSFORMATIONS, Person.class);

        // THEN
        assertEquals(result, expected);
    }

    @Test
    void when_createSelectionPartForMethodNameQuery_then_returnEmptyStringBecauseNoCondition() {
        // GIVEN
        var expected = "SELECT * FROM XXXXX WHERE ";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForMethodNameQuery(MAPPER.createArrayNode(),
                TRANSFORMATIONS, Person.class);

        // THEN
        assertEquals(result, expected);
    }

    @Test
    void when_createSelectionPartForAnnotation_then_returnSelectionPart() {
        // GIVEN
        var expected  = "SELECT id,age,active FROM Person WHERE firstname = 'Juni'";
        var baseQuery = "SELECT * FROM Person WHERE firstname = 'Juni'";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForAnnotation(baseQuery, SELECTIONS_BLACKLIST,
                TRANSFORMATIONS, "", Person.class);

        // THEN
        assertEquals(expected, result);
    }

    @Test
    void when_createSelectionPartForAnnotation_then_returnQueryBecauseSelectionIsEmpty() {
        // GIVEN
        var baseQuery = "SELECT * FROM Person WHERE firstname = 'Juni'";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForAnnotation(baseQuery, MAPPER.createArrayNode(),
                TRANSFORMATIONS, "", Person.class);

        // THEN
        assertEquals(baseQuery, result);
    }

    @Test
    void when_createSelectionPartForAnnotation_then_returnQueryBecauseSelectionAndTransformationIsEmpty() {
        // GIVEN
        var baseQuery = "SELECT * FROM Person WHERE firstname = 'Juni'";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForAnnotation(baseQuery, MAPPER.createArrayNode(),
                MAPPER.createArrayNode(), "", Person.class);

        // THEN
        assertEquals(baseQuery, result);
    }

    @Test
    void when_createSelectionPartForAnnotation_then_returnQueryBecauseSelectionIsEmpty2() {
        // GIVEN
        var baseQuery = "SELECT lastname, birthday FROM Person WHERE firstname = 'Juni'";
        var expected  = "SELECT lastname,YEAR(birthday) FROM Person WHERE firstname = 'Juni'";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForAnnotation(baseQuery, MAPPER.createArrayNode(),
                TRANSFORMATIONS, "", Person.class);

        // THEN
        assertEquals(expected, result);
    }

    @Test
    void when_createSelectionPartForAnnotation_then_returnQueryBecauseIsAsterixAndelectionIsEmpty() {
        // GIVEN
        var baseQuery = "SELECT lastname,firstname FROM Person WHERE firstname = 'Juni'";
        var expected  = "SELECT id,age,active FROM Person WHERE firstname = 'Juni'";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForAnnotation(baseQuery, SELECTIONS_BLACKLIST,
                TRANSFORMATIONS, "", Person.class);

        // THEN
        assertEquals(expected, result);
    }

    @Test
    void when_createSelectionPartForAnnotation_then_returnQueryBecauseAliasIsNotEmpty() {
        // GIVEN
        var baseQuery = "SELECT age,firstname FROM Person WHERE firstname = 'Juni'";
        var expected  = "SELECT UPPER(p.firstname),p.age FROM Person WHERE firstname = 'Juni'";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForAnnotation(baseQuery, SELECTIONS_ALIAS_WHITELIST,
                TRANSFORMATIONS, "p", Person.class);

        // THEN
        assertEquals(expected, result);
    }

    @Test
    void when_createSelectionPartForAnnotation_then_returnQueryBecauseBlacklistRemovesField() {
        // GIVEN
        var baseQuery = "SELECT active FROM Person WHERE firstname = 'Juni'";
        var expected  = "SELECT id,active FROM Person WHERE firstname = 'Juni'";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForAnnotation(baseQuery, SELECTIONS_ALIAS_BLACKLIST,
                TRANSFORMATIONS, "", Person.class);

        // THEN
        assertEquals(expected, result);
    }

    @Test
    void when_createSelectionPartForAnnotation_then_returnQueryBecauseBlacklistRemovesField1() {
        // GIVEN
        var baseQuery = "SELECT active FROM Person WHERE firstname = 'Juni'";
        var expected  = "SELECT id,active FROM Person WHERE firstname = 'Juni'";

        // WHEN
        var result = QuerySelectionUtils.createSelectionPartForAnnotation(baseQuery, SELECTIONS_ALIAS_BLACKLIST,
                MAPPER.createArrayNode(), "", Person.class);

        // THEN
        assertEquals(expected, result);
    }

}
