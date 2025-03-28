/*
 * Copyright (C) 2017-2025 Dominic Heutelbeck (dominic@heutelbeck.com)
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
package io.sapl.grammar.ide.contentassist;

import java.util.List;

import org.junit.jupiter.api.Test;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class FunctionSchemaTests extends CompletionTests {

    @Test
    void testCompletion_PolicyBody_function() {
        final var document = """
                policy "test" deny where schemaTest.person("willi", "french", 23).§""";
        final var expected = List.of("name", "nationality", "age");
        assertProposalsContain(document, expected);
    }

    @Test
    void testCompletion_PolicyBody_function_wildcard_import() {
        final var document = """
                import schemaTest.*
                policy "test" deny where person("willi", "french", 23).§""";
        final var expected = List.of("name", "nationality", "age");
        assertProposalsContain(document, expected);
    }
}
