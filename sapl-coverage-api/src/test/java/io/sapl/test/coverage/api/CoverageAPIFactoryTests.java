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
package io.sapl.test.coverage.api;

import static io.sapl.test.coverage.api.CoverageAPIFactory.constructCoverageHitRecorder;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CoverageAPIFactoryTests {

    @Test
    void test(@TempDir Path tempDir) throws IOException {

        Path hitDir = tempDir.resolve("hits");

        assertThat(countFilesInDir(hitDir)).isZero();

        constructCoverageHitRecorder(tempDir);

        assertThat(countFilesInDir(hitDir)).isEqualTo(3);
    }

    private int countFilesInDir(Path path) throws IOException {
        final var count = new AtomicInteger(0);
        if (!path.toFile().exists()) {
            return count.getPlain();
        }
        try (var stream = Files.newDirectoryStream(path, "*.txt")) {
            stream.forEach(s -> count.incrementAndGet());
        }
        return count.getPlain();
    }

    @Test
    void test_reader() {
        final var object = CoverageAPIFactory.constructCoverageHitReader(Paths.get(""));
        assertThat(object).isNotNull();
    }

}
