package io.promptshield.module;

import io.promptshield.core.DetectionContext;
import io.promptshield.core.DetectionSignal;

/**
 * Strategy interface — implement this to plug a custom detector into the pipeline.
 *
 * <pre>
 * public class MyCustomModule implements DetectionModule {
 *     &#64;Override
 *     public DetectionSignal analyze(String input, DetectionContext ctx) {
 *         // your logic here
 *     }
 * }
 * </pre>
 *
 * Register via {@link io.promptshield.core.PromptShield.Builder#addModule(DetectionModule)}.
 */
public interface DetectionModule {

    /**
     * Analyse the prompt and return a detection signal.
     *
     * @param input   the raw user prompt
     * @param context caller-supplied context (expected task, session id, etc.)
     * @return a {@link DetectionSignal} — never null
     */
    DetectionSignal analyze(String input, DetectionContext context);

    /** Optional human-readable name for this module (used in logs / signal sources). */
    default String name() { return getClass().getSimpleName(); }
}
