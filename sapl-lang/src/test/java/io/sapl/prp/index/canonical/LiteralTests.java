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
package io.sapl.prp.index.canonical;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class LiteralTests {

    @Test
    void testGuardClauses() {
        assertThrows(NullPointerException.class, () -> new Literal((Bool) null));
        assertThat(new Literal(new Bool(true)), notNullValue());
    }

    @Test
    void isImmutableTest() {
        assertThat(new Literal(new Bool(false)).isImmutable(), is(true));
    }

    @Test
    void evaluateTest() {
        assertThat(new Literal(new Bool(false)).evaluate(), is(false));
        assertThat(new Literal(new Bool(false), true).evaluate(), is(true));
    }

    @Test
    void negateTest() {
        final var literal        = new Literal(new Bool(false));
        final var negatedLiteral = new Literal(new Bool(false), true);

        final var negated       = literal.negate();
        final var doubleNegated = negatedLiteral.negate();

        assertThat(literal.evaluate(), is(!negated.evaluate()));
        assertThat(negatedLiteral.evaluate(), is(!doubleNegated.evaluate()));
    }

    @Test
    void sharesBoolTest() {
        final var trueLiteral  = new Literal(new Bool(true));
        final var falseLiteral = new Literal(new Bool(false));

        assertThat(trueLiteral.sharesBool(trueLiteral), is(true));
        assertThat(trueLiteral.sharesBool(falseLiteral), is(false));
    }

    @Test
    void sharesNegationTest() {
        final var literal        = new Literal(new Bool(true), false);
        final var negatedLiteral = new Literal(new Bool(false), true);

        assertThat(literal.sharesNegation(literal), is(true));
        assertThat(negatedLiteral.sharesNegation(negatedLiteral), is(true));
        assertThat(literal.sharesNegation(negatedLiteral), is(false));
    }

    @Test
    @SuppressWarnings("unlikely-arg-type")
    void equalsTest() {
        final var literal     = new Literal(new Bool(false), true);
        final var literalMock = mock(Literal.class);

        assertThat(literal.equals(literal), is(true));
        assertThat(literal.equals(null), is(false));
        assertThat(literal.equals(""), is(false));
        assertThat(literal.equals(literalMock), is(false));
        assertThat(literal.equals(new Literal(new Bool(false), false)), is(false));
        assertThat(literal.equals(new Literal(new Bool(true), true)), is(false));
        assertThat(literal.equals(new Literal(new Bool(false), true)), is(true));
    }

}
