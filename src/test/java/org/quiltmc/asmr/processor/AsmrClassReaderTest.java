package org.quiltmc.asmr.processor;

import org.junit.jupiter.api.Test;
import org.quiltmc.asmr.processor.tree.AsmrClassNode;
import org.quiltmc.asmr.processor.tree.AsmrTreeUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.quiltmc.asmr.processor.AsmrClassTestUtil.findClass;

public class AsmrClassReaderTest {
    @interface Foo {
        String value();
        String[] array() default {};
    }

    @Foo(value = "bar", array = {"foo", "baz"})
    void method() {}

    @Test
    public void testClassReader() {
        AsmrClassNode classNode = findClass(AsmrClassReaderTest.class);
        System.out.println(AsmrTreeUtil.dump(classNode));
        assertEquals(AsmrClassReaderTest.class.getName().replace('.', '/'), classNode.name().value());
    }
}
