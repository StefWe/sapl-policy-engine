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
package io.sapl.hamcrest;

import java.util.Objects;
import java.util.function.Predicate;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import com.fasterxml.jackson.databind.JsonNode;

import io.sapl.api.pdp.AuthorizationDecision;

/**
 * Matcher for examining the obligation contained in an AuthorizationDecision.
 */
public class HasObligationMatching extends TypeSafeDiagnosingMatcher<AuthorizationDecision> {

    private final Predicate<? super JsonNode> predicate;

    /**
     * Checks for the presence of an obligation fulfilling a predicate.
     *
     * @param jsonPredicate a JsonNode Predicate.
     */
    public HasObligationMatching(Predicate<? super JsonNode> jsonPredicate) {
        super(AuthorizationDecision.class);
        this.predicate = Objects.requireNonNull(jsonPredicate);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("the decision has an obligation matching the predicate");
    }

    @Override
    protected boolean matchesSafely(AuthorizationDecision decision, Description mismatchDescription) {
        final var obligations = decision.getObligations();
        if (obligations.isEmpty()) {
            mismatchDescription.appendText("decision didn't contain any obligations");
            return false;
        }

        var containsObligation = false;

        for (JsonNode node : obligations.get()) {
            if (this.predicate.test(node))
                containsObligation = true;
        }

        if (containsObligation) {
            return true;
        } else {
            mismatchDescription.appendText("no obligation matched");
            return false;
        }
    }

}
