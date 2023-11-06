package io.sapl.functions;

import static io.sapl.functions.SchemaTestFunctionLibrary.schemaFun;
import static io.sapl.hamcrest.Matchers.val;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

class SchemaTestFunctionLibraryTests {

    @Test
    void schemaFun_returns_true() {
        var result = schemaFun();
        assertThat(result, is(val(true)));
    }
}
