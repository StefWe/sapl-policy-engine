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
package io.sapl.prp.resources;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ScanResult;
import io.sapl.interpreter.SAPLInterpreter;
import io.sapl.prp.PrpUpdateEvent;
import io.sapl.prp.PrpUpdateEvent.Type;
import io.sapl.prp.PrpUpdateEvent.Update;
import io.sapl.prp.PrpUpdateEventSource;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

@Slf4j
public class ResourcesPrpUpdateEventSource implements PrpUpdateEventSource {

    private static final String SAPL_EXTENSION = "sapl";

    private final PrpUpdateEvent initializingPrpUpdate;

    public ResourcesPrpUpdateEventSource(@NonNull String policyPath, @NonNull SAPLInterpreter interpreter) {
        log.debug("Load SAPL documents from resources in path: {}", policyPath);
        final List<Update> updates = new ArrayList<>();
        try (ScanResult scanResult = new ClassGraph().acceptPaths(policyPath).scan()) {
            final var saplDocuments = scanResult.getResourcesWithExtension(SAPL_EXTENSION);
            if (saplDocuments.isEmpty()) {
                log.warn("No SAPL policies/policy sets found in resources under path {}", policyPath);
            }
            saplDocuments.forEachByteArrayThrowingIOException((Resource res, byte[] rawDocument) -> {
                log.debug("Loading SAPL document: {}", res.getPath());
                final var document = interpreter.parseDocument(new String(rawDocument, StandardCharsets.UTF_8));
                if (document.isInvalid()) {
                    log.error("Error in SAPL document: {}", document.errorMessage());
                    log.error("The application will continue to boot up. "
                            + "However, the PDP will act as if there was an error during each "
                            + "authorization subscription/request.");
                    updates.clear();
                    updates.add(new Update(Type.INCONSISTENT, null));
                } else {
                    updates.add(new Update(Type.PUBLISH, document));
                }
            });
        } catch (IOException e) {
            log.error("Failed to load SAPL policies/policy sets from the resources. "
                    + "The application will continue to boot up. "
                    + "However, the PDP will act as if there was an error during each "
                    + "authorization subscription/request.", e);
            updates.clear();
            updates.add(new Update(Type.INCONSISTENT, null));
        }
        initializingPrpUpdate = new PrpUpdateEvent(updates);
    }

    @Override
    public void dispose() {
        // NOP nothing to dispose of
    }

    @Override
    public Flux<PrpUpdateEvent> getUpdates() {
        return Flux.just(initializingPrpUpdate);
    }

}
