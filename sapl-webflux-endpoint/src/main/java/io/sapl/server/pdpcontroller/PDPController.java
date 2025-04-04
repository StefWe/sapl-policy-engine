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
package io.sapl.server.pdpcontroller;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.sapl.api.pdp.AuthorizationDecision;
import io.sapl.api.pdp.AuthorizationSubscription;
import io.sapl.api.pdp.IdentifiableAuthorizationDecision;
import io.sapl.api.pdp.MultiAuthorizationDecision;
import io.sapl.api.pdp.MultiAuthorizationSubscription;
import io.sapl.api.pdp.PolicyDecisionPoint;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * REST controller providing endpoints for a policy decision point. The
 * endpoints can be connected using the client in the module sapl-pdp-client.
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pdp")
public class PDPController {
    private final PolicyDecisionPoint pdp;
    @Value("#{'${io.sapl.server.keep-alive:${io.sapl.server-lt.keep-alive:0}}'}")
    private long                      keepAliveSeconds = 0;

    /**
     * Enables keep alive comments to keep tcp connection active. This is usually
     * needed to avoid that connections are dropped by firewalls.
     *
     * @param flux a flux emitting the authorization decisions
     * @return the original flux with additional keep-alive messages if keep-alive
     * parameter > 0
     */
    private <T> Flux<ServerSentEvent<T>> wrapWithKeepAlive(Flux<T> flux) {
        if (keepAliveSeconds > 0) {
            return Flux.merge(flux.map(t -> ServerSentEvent.builder(t).build()),
                    Flux.interval(Duration.ofSeconds(this.keepAliveSeconds))
                            .map(aLong -> ServerSentEvent.<T>builder().comment("keep-alive").build()));
        } else {
            return flux.map(decision -> ServerSentEvent.<T>builder().data(decision).build());
        }
    }

    /**
     * Delegates to {@link PolicyDecisionPoint#decide(AuthorizationSubscription)}.
     *
     * @param authzSubscription the authorization subscription to be processed by
     * the PDP.
     * @return a flux emitting the current authorization decisions.
     * @see PolicyDecisionPoint#decide(AuthorizationSubscription)
     */
    @PostMapping(value = "/decide", produces = MediaType.APPLICATION_NDJSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ServerSentEvent<AuthorizationDecision>> decide(
            @Valid @RequestBody AuthorizationSubscription authzSubscription) {
        return wrapWithKeepAlive(
                pdp.decide(authzSubscription).onErrorResume(error -> Flux.just(AuthorizationDecision.INDETERMINATE)));
    }

    /**
     * Delegates to {@link PolicyDecisionPoint#decide(AuthorizationSubscription)}.
     *
     * @param authzSubscription the authorization subscription to be processed by
     * the PDP.
     * @return a Mono for the initial decision.
     * @see PolicyDecisionPoint#decide(AuthorizationSubscription)
     */
    @PostMapping(value = "/decide-once", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<AuthorizationDecision> decideOnce(@Valid @RequestBody AuthorizationSubscription authzSubscription) {
        return pdp.decide(authzSubscription).onErrorResume(error -> Flux.just(AuthorizationDecision.INDETERMINATE))
                .next();
    }

    /**
     * Delegates to
     * {@link PolicyDecisionPoint#decide(MultiAuthorizationSubscription)}.
     *
     * @param multiAuthzSubscription the authorization multi-subscription to be
     * processed by the PDP.
     * @return a flux emitting authorization decisions related to the individual
     * subscriptions contained in the given {@code multiAuthzSubscription} as soon
     * as they are available.
     * @see PolicyDecisionPoint#decide(MultiAuthorizationSubscription)
     */
    @PostMapping(value = "/multi-decide", produces = MediaType.APPLICATION_NDJSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ServerSentEvent<IdentifiableAuthorizationDecision>> decide(
            @Valid @RequestBody MultiAuthorizationSubscription multiAuthzSubscription) {
        return wrapWithKeepAlive(pdp.decide(multiAuthzSubscription)
                .onErrorResume(error -> Flux.just(IdentifiableAuthorizationDecision.INDETERMINATE)));
    }

    /**
     * Delegates to
     * {@link PolicyDecisionPoint#decideAll(MultiAuthorizationSubscription)}.
     *
     * @param multiAuthzSubscription the authorization multi-subscription to be
     * processed by the PDP.
     * @return a flux emitting multi-decisions containing authorization decisions
     * for all the individual authorization subscriptions contained in the given
     * {@code multiAuthzSubscription}.
     * @see PolicyDecisionPoint#decideAll(MultiAuthorizationSubscription)
     */
    @PostMapping(value = "/multi-decide-all", produces = MediaType.APPLICATION_NDJSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ServerSentEvent<MultiAuthorizationDecision>> decideAll(
            @Valid @RequestBody MultiAuthorizationSubscription multiAuthzSubscription) {
        return wrapWithKeepAlive(pdp.decideAll(multiAuthzSubscription)
                .onErrorResume(error -> Flux.just(MultiAuthorizationDecision.indeterminate())));
    }

    /**
     * Delegates to
     * {@link PolicyDecisionPoint#decideAll(MultiAuthorizationSubscription)}.
     *
     * @param multiAuthzSubscription the authorization multi-subscription to be
     * processed by the PDP.
     * @return a Mono emitting the initial multi-decision containing authorization
     * decisions for all the individual authorization subscriptions contained in the
     * given {@code multiAuthzSubscription}.
     * @see PolicyDecisionPoint#decideAll(MultiAuthorizationSubscription)
     */
    @PostMapping(value = "/multi-decide-all-once", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Mono<MultiAuthorizationDecision> decideAllOnce(
            @Valid @RequestBody MultiAuthorizationSubscription multiAuthzSubscription) {
        return pdp.decideAll(multiAuthzSubscription)
                .onErrorResume(error -> Flux.just(MultiAuthorizationDecision.indeterminate())).next();
    }

}
