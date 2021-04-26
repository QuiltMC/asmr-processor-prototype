package org.quiltmc.asmr.processor.verifier;

import java.util.Objects;

public final class VerificationDesc {
	final String owner;
	final String name;
	final String descriptor;

	VerificationDesc(String owner, String name, String descriptor) {
		this.owner = owner;
		this.name = name;
		this.descriptor = descriptor;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VerificationDesc desc = (VerificationDesc) o;
		return Objects.equals(owner, desc.owner) && Objects.equals(name, desc.name) && Objects.equals(descriptor, desc.descriptor);
	}

	@Override
	public int hashCode() {
		return Objects.hash(owner, name, descriptor);
	}
}