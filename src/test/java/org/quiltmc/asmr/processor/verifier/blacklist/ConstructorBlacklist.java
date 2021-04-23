package org.quiltmc.asmr.processor.verifier.blacklist;

import org.quiltmc.asmr.processor.test.annotation.InternalMembers;

public class ConstructorBlacklist {
	void method() {
		new InternalMembers();
	}
}
