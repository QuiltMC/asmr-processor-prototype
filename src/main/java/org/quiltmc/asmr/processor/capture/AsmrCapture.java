package org.quiltmc.asmr.processor.capture;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.asmr.processor.annotation.AllowLambdaCapture;

/**
 * Marker interface for all capture types
 */
@AllowLambdaCapture
@ApiStatus.NonExtendable
public interface AsmrCapture {
}
