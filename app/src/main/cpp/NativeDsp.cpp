#include <jni.h>
#include <algorithm>
#include <cmath>
#include <complex>
#include <cstdint>
#include <string>
#include <vector>

static float rms(const jfloat* x, jsize n) {
    if (n <= 0) return 0.0f;
    double sum = 0.0;
    for (jsize i = 0; i < n; ++i) sum += static_cast<double>(x[i]) * x[i];
    return static_cast<float>(std::sqrt(sum / n));
}

static int estimateLag(const jfloat* ref, const jfloat* measured, jsize n, int maxLag) {
    if (n <= 0) return 0;
    maxLag = std::max(0, std::min<int>(maxLag, static_cast<int>(n) - 1));
    double best = -1.0;
    int bestLag = 0;
    for (int lag = -maxLag; lag <= maxLag; ++lag) {
        double corr = 0.0;
        for (int i = 0; i < n; ++i) {
            const int j = i + lag;
            if (j >= 0 && j < n) corr += static_cast<double>(ref[i]) * measured[j];
        }
        const double score = std::abs(corr);
        if (score > best) { best = score; bestLag = lag; }
    }
    return bestLag;
}

static std::vector<float> fftMagnitude(const jfloat* input, jsize n) {
    if (n <= 0 || (n & (n - 1)) != 0) return {};
    std::vector<std::complex<double>> a(static_cast<size_t>(n));
    for (jsize i = 0; i < n; ++i) {
        const double w = n == 1 ? 1.0 : 0.5 * (1.0 - std::cos(2.0 * M_PI * i / (n - 1)));
        a[static_cast<size_t>(i)] = std::complex<double>(input[i] * w, 0.0);
    }
    for (size_t i = 1, j = 0; i < static_cast<size_t>(n); ++i) {
        size_t bit = static_cast<size_t>(n) >> 1;
        for (; j & bit; bit >>= 1) j ^= bit;
        j ^= bit;
        if (i < j) std::swap(a[i], a[j]);
    }
    for (size_t len = 2; len <= static_cast<size_t>(n); len <<= 1) {
        const double angle = -2.0 * M_PI / static_cast<double>(len);
        const std::complex<double> wlen(std::cos(angle), std::sin(angle));
        for (size_t i = 0; i < static_cast<size_t>(n); i += len) {
            std::complex<double> w(1.0, 0.0);
            for (size_t j = 0; j < len / 2; ++j) {
                const auto u = a[i + j];
                const auto v = a[i + j + len / 2] * w;
                a[i + j] = u + v;
                a[i + j + len / 2] = u - v;
                w *= wlen;
            }
        }
    }
    std::vector<float> out(static_cast<size_t>(n) / 2 + 1);
    for (size_t i = 0; i < out.size(); ++i) out[i] = static_cast<float>(std::abs(a[i]));
    return out;
}

extern "C" JNIEXPORT jstring JNICALL
Java_com_candstech_cacoustic_MainActivity_nativeDspStatus(JNIEnv* env, jobject) {
    return env->NewStringUTF("JNI loaded · FFT/RMS/correlation primitives");
}

extern "C" JNIEXPORT jfloat JNICALL
Java_com_candstech_cacoustic_MainActivity_nativeRms(JNIEnv* env, jobject, jfloatArray samples) {
    if (!samples) return 0.0f;
    const jsize n = env->GetArrayLength(samples);
    jfloat* data = env->GetFloatArrayElements(samples, nullptr);
    const float result = rms(data, n);
    env->ReleaseFloatArrayElements(samples, data, JNI_ABORT);
    return result;
}

extern "C" JNIEXPORT jint JNICALL
Java_com_candstech_cacoustic_MainActivity_nativeEstimateLag(JNIEnv* env, jobject, jfloatArray reference, jfloatArray measured, jint maxLag) {
    if (!reference || !measured) return 0;
    const jsize n = std::min(env->GetArrayLength(reference), env->GetArrayLength(measured));
    jfloat* a = env->GetFloatArrayElements(reference, nullptr);
    jfloat* b = env->GetFloatArrayElements(measured, nullptr);
    const int lag = estimateLag(a, b, n, maxLag);
    env->ReleaseFloatArrayElements(reference, a, JNI_ABORT);
    env->ReleaseFloatArrayElements(measured, b, JNI_ABORT);
    return lag;
}

extern "C" JNIEXPORT jfloatArray JNICALL
Java_com_candstech_cacoustic_MainActivity_nativeFftMagnitude(JNIEnv* env, jobject, jfloatArray samples) {
    if (!samples) return env->NewFloatArray(0);
    const jsize n = env->GetArrayLength(samples);
    jfloat* data = env->GetFloatArrayElements(samples, nullptr);
    auto mags = fftMagnitude(data, n);
    env->ReleaseFloatArrayElements(samples, data, JNI_ABORT);
    jfloatArray out = env->NewFloatArray(static_cast<jsize>(mags.size()));
    if (!mags.empty()) env->SetFloatArrayRegion(out, 0, static_cast<jsize>(mags.size()), mags.data());
    return out;
}
