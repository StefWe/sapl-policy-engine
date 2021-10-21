/*
 * Copyright © 2017-2021 Dominic Heutelbeck (dominic@heutelbeck.com)
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
package io.sapl.spring.pep;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import io.sapl.api.pdp.AuthorizationDecision;
import io.sapl.api.pdp.AuthorizationSubscription;
import io.sapl.api.pdp.Decision;
import io.sapl.api.pdp.PolicyDecisionPoint;
import io.sapl.spring.constraints.ConstraintEnforcementService;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

/**
 * This service can be used to establish a policy enforcement point at any
 * location in users code.
 */
@Service
@RequiredArgsConstructor
public class PolicyEnforcementPoint {

	private final PolicyDecisionPoint pdp;
	private final ConstraintEnforcementService constraintEnforcementService;

	public boolean isPermitted(AuthorizationSubscription authzSubscription) {
		return pdp.decide(authzSubscription).next().defaultIfEmpty(AuthorizationDecision.DENY)
				.map(this::enforceDecision).block();
	}

	private boolean enforceDecision(AuthorizationDecision authzDecision) {

		if (authzDecision.getResource().isPresent())
			return false;

		try {
			constraintEnforcementService
					.enforceConstraintsOfDecisionOnResourceAccessPoint(authzDecision, Flux.empty(), Object.class)
					.blockLast();
		} catch (AccessDeniedException e) {
			return false;
		}

		if (authzDecision.getDecision() != Decision.PERMIT)
			return false;

		return true;
	}

}
