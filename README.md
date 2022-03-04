> ❗ This prototype has fulfilled its purpose and is now archived in favor of [Chasm](https://github.com/QuiltMC/chasm). ❗

# ASMR Processor Prototype
This project is intended to prototype design ideas for the ASMR backend and processor, to see what problems may be encountered in practice.

## What is ASMR?
ASMR (ASM-Regex) is the temporary name for a project eventually (maybe, hopefully) intended to replace Mixin.

Ideas for ASMR were first discussed publicly in [this Fabric issue](https://github.com/FabricMC/fabric-loader/issues/244). The overall idea (having a backend, frontend, compiler and processor) has not changed since then, however specific details have been fleshed out since.

Details about the ASMR ecosystem as a whole can be found in [RFC 3](https://github.com/QuiltMC/rfcs/pull/3), while details about the processor specifically, which goes hand in hand with this project, can be found in [RFC 14](https://github.com/QuiltMC/rfcs/pull/14).

## Short FAQ

### Will I have to rewrite my Mixins?
As explained in RFC 3, ASMR will *not* require you to rewrite your mixins, except possibly if you have used a mixin plugin (if you don't know what I'm talking about then you haven't used one), in which case you will still be able to keep your mixins but *might* have to rewrite your plugin.

The way mixins will be processed internally will be very different, but you will still be able to write them like you can today.

### Will I be able to do \[insert feature that Mixin doesn't have\] with ASMR?
Very likely yes. No-one has yet suggested something they want to do that isn't already possible with the ASMR backend design. ASMR is as powerful as raw ASM, the difference is that modifications to classes are controlled in such a way that conflicts are detected and reported.

For some obscure use cases you may have to write directly in the backend without using a frontend language or compiler. But we hope that compilers will be written for the remotely common use cases. Note that the backend is designed to be powerful, not easy to write with directly, so you want to use a frontend where possible.
