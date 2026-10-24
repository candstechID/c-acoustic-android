# C ACOUSTIC technical roadmap

## Phase 1 — Foundation (current scaffold)
- Kotlin dashboard and navigation
- JNI bridge / CMake / native C++ DSP examples
- FFT magnitude, RMS and integer-lag cross-correlation test helpers

## Phase 2 — Audio I/O
- USB device enumeration and permission lifecycle
- Evaluate Android USB Audio Class compatibility and native audio backend
- Select AAudio/Oboe where appropriate; retain fallback strategy only where supported
- Dual-channel capture, format negotiation, ring buffers, drop/overrun detection
- Verify reference and measurement channels share a clock

## Phase 3 — Measurement engine
- RTA with configurable FFT size, window, averaging, smoothing and peak hold
- Transfer function via cross spectra, magnitude, phase, coherence and stable reference
- IR capture with excitation, windowing, timing and quality checks
- SPL calibration profiles and weighting/time averaging

## Phase 4 — Auto Phase
- Capture TF1 (main) and TF2 (sub) using consistent measurement geometry and reference
- Estimate coarse lag using correlation; refine with fractional-delay interpolation
- Optimize complex summation in the selected crossover band, including polarity candidates
- Present confidence, recommended delay and before/after verification
- Do not directly change external DSP unless an explicit supported control protocol exists

## Phase 5 — Validation
- Unit tests with generated sine, impulse and known-delay signals
- Reference comparisons against established measurement equipment/software
- Hardware matrix across Android models and USB interfaces
- Document measurement uncertainty, latency and supported combinations

## Real-time constraints
No heap allocations, blocking I/O, file access or UI calls on the audio callback. Use preallocated buffers and bounded work. Measure callback duration and count dropped frames.
