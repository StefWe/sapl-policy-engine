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
package io.sapl.server.ce.model.sapldocument;

import io.sapl.api.SaplVersion;
import lombok.NonNull;

/**
 * Exception thrown if the name of a {@link SaplDocument} to publish is not
 * unique.
 */
public class PublishedDocumentNameCollisionException extends Exception {

    private static final long serialVersionUID = SaplVersion.VERISION_UID;

    /**
     * Creates a new instance of the {@link PublishedDocumentNameCollisionException}
     * class.
     *
     * @param name the name of the already published SAPL document
     * @param innerEx the inner exception
     */
    public PublishedDocumentNameCollisionException(@NonNull String name, @NonNull Throwable innerEx) {
        super(String.format("Another SAPL document with name \"%s\" is already published.", name), innerEx);
    }
}
