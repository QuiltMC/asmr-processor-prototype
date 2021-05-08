package org.quiltmc.asmr.processor.test;

import org.junit.jupiter.api.Test;
import org.quiltmc.asmr.tree.member.AsmrClassNode;
import org.quiltmc.asmr.tree.AsmrTreeUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.quiltmc.asmr.processor.test.AsmrClassTestUtil.findClass;

public class AsmrClassNodeCopyTest {
	@interface Foo {
		String value();

		String[] array() default {};
	}

	@Foo(value = "bar", array = {"foo", "baz"})
	void method() {
	}

	@Test
	public void testClassReader() {
		AsmrClassNode classNode = findClass(AsmrClassNodeCopyTest.class);
		String originalDump = AsmrTreeUtil.dump(classNode);

		AsmrClassNode copy = classNode.copy(null);
		String copyDump = AsmrTreeUtil.dump(copy);

		System.out.println(originalDump);

		assertEquals(originalDump, copyDump);
	}
}
