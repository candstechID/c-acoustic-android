# C ACOUSTIC — Android audio measurement scaffold

C ACOUSTIC by CANDSTECH is an Android-native starter for a professional audio measurement application.

## Included
- Kotlin dashboard with tabs for RTA, Transfer Function, Impulse Response, SPL Meter, Audio I/O and Auto Phase.
- Native C++ DSP primitives: Hann window, radix-2 FFT, basic RMS, cross-correlation delay estimate and a fractional-delay candidate helper.
- CMake/NDK JNI bridge.
- GitHub Actions workflow for extracting an uploaded source ZIP into the repository.
- Architecture notes and validation checklist.

## Important status
This is a substantially expanded **prototype scaffold**, not a validated professional measurement instrument. USB audio capture/routing, calibrated SPL, synchronized dual-channel capture, frequency-dependent transfer function/coherence, full IR acquisition, and actual external DSP control still require implementation and hardware testing. The Auto Phase screen is a demonstration of recommendations only; it does not control a mixer/DSP.

## Build
Open this folder in Android Studio (JDK 17). Install Android SDK Platform 35, NDK, and CMake 3.22.1 via SDK Manager, sync Gradle, then Run. Gradle wrapper is not included.

## GitHub ZIP workflow
The workflow must already be committed at `.github/workflows/extract-uploaded-zip.yml` before uploading `c-acoustic-android-source.zip`. Upload the ZIP to the repository root, commit it, then go to Actions → Extract uploaded source ZIP → Run workflow. It safely extracts the archive, removes the archive from the working tree, and commits the source files.

## DSP limitations
The FFT implementation is a basic radix-2 educational primitive. It is not a substitute for calibration, window/averaging design, coherent capture, or validated transfer-function algorithms. Validate all results against known signals and reference equipment before relying on them for live PA tuning.
