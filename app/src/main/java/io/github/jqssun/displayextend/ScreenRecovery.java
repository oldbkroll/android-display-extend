package io.github.jqssun.displayextend;

import java.util.function.Consumer;

/** Attempt all recovery steps even if a Shizuku IPC call fails. No Android dependencies. */
public final class ScreenRecovery {
  @FunctionalInterface
  public interface Step {
    void run() throws Exception;
  }

  private ScreenRecovery() {}

  public static void restore(
      Step powerOn, Step stopKeyListener, Step dismissBlackScreen, Consumer<Exception> onFailure) {
    for (Step step : new Step[] {powerOn, stopKeyListener, dismissBlackScreen}) {
      try {
        step.run();
      } catch (Exception e) {
        onFailure.accept(e);
      }
    }
  }
}
