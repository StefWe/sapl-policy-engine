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
package io.sapl.test.junit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import io.sapl.test.utils.ClasspathHelper;

class TestDiscoveryHelperTests {

    protected final MockedStatic<ClasspathHelper> classpathHelperMockedStatic = mockStatic(ClasspathHelper.class);
    protected final MockedStatic<FileUtils>       fileUtilsMockedStatic       = mockStatic(FileUtils.class);

    @AfterEach
    void tearDown() {
        classpathHelperMockedStatic.close();
        fileUtilsMockedStatic.close();
    }

    @Test
    void discoverTests_ClasspathHelperThrows_ThrowsException() {
        classpathHelperMockedStatic.when(() -> ClasspathHelper.findPathOnClasspath(any(), eq("")))
                .thenThrow(new RuntimeException("no path here"));

        final var exception = assertThrows(RuntimeException.class, TestDiscoveryHelper::discoverTests);

        assertEquals("no path here", exception.getMessage());
    }

    @Test
    void discoverTests_handlesEmptyListOfFiles_returnsEmptyList() {
        final var pathMock = mock(Path.class);

        classpathHelperMockedStatic.when(() -> ClasspathHelper.findPathOnClasspath(any(), eq(""))).thenReturn(pathMock);

        final var directoryMock = mock(File.class);
        when(pathMock.toFile()).thenReturn(directoryMock);

        fileUtilsMockedStatic.when(() -> FileUtils.listFiles(directoryMock, new String[] { "sapltest" }, true))
                .thenReturn(Collections.emptyList());

        final var result = TestDiscoveryHelper.discoverTests();

        assertTrue(result.isEmpty());
    }

    @Test
    void discoverTests_usesCorrectFileExtension_returnsPaths() {
        final var pathMock = mock(Path.class);

        classpathHelperMockedStatic.when(() -> ClasspathHelper.findPathOnClasspath(any(), eq(""))).thenReturn(pathMock);

        final var directoryMock = mock(File.class);
        when(pathMock.toFile()).thenReturn(directoryMock);

        final var file1Mock = mock(File.class);
        final var file2Mock = mock(File.class);

        fileUtilsMockedStatic.when(() -> FileUtils.listFiles(directoryMock, new String[] { "sapltest" }, true))
                .thenReturn(List.of(file1Mock, file2Mock));

        final var directoryPathMock = mock(Path.class);
        when(directoryMock.toPath()).thenReturn(directoryPathMock);

        final var file1PathMock = mock(Path.class);
        when(file1Mock.toPath()).thenReturn(file1PathMock);

        final var file2PathMock = mock(Path.class);
        when(file2Mock.toPath()).thenReturn(file2PathMock);

        final var file1RelativePathMock = mock(Path.class);
        when(directoryPathMock.relativize(file1PathMock)).thenReturn(file1RelativePathMock);

        final var file2RelativePathMock = mock(Path.class);
        when(directoryPathMock.relativize(file2PathMock)).thenReturn(file2RelativePathMock);

        when(file1RelativePathMock.toString()).thenReturn("filePath1");
        when(file2RelativePathMock.toString()).thenReturn("folder/filePath2");

        final var result = TestDiscoveryHelper.discoverTests();

        assertEquals(2, result.size());
        assertEquals("filePath1", result.get(0));
        assertEquals("folder/filePath2", result.get(1));
    }
}
