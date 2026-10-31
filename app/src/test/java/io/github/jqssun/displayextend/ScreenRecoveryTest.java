package io.github.jqssun.displayextend;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class ScreenRecoveryTest {
  @Test
  public void restoresPowerBeforeStoppingKeysAndDismissingOverlay() {
    List<String> calls = new ArrayList<>();
    ScreenRecovery.restore(
        () -> calls.add("power"),
        () -> calls.add("keys"),
        () -> calls.add("dismiss"),
        error -> calls.add("unexpected failure"));
    assertEquals(Arrays.asList("power", "keys", "dismiss"), calls);
  }

  @Test
  public void brokenPowerBinderDoesNotLeaveBlackOverlay() {
    List<String> calls = new ArrayList<>();
    ScreenRecovery.restore(
        () -> { throw new Exception("binder died"); },
        () -> calls.add("keys"),
        () -> calls.add("dismiss"),
        error -> calls.add(error.getMessage()));
    assertEquals(Arrays.asList("binder died", "keys", "dismiss"), calls);
  }

  @Test
  public void listenerFailureDoesNotPreventOverlayDismissal() {
    List<String> calls = new ArrayList<>();
    ScreenRecovery.restore(
        () -> calls.add("power"),
        () -> { throw new Exception("listener unavailable"); },
        () -> calls.add("dismiss"),
        error -> calls.add(error.getMessage()));
    assertEquals(Arrays.asList("power", "listener unavailable", "dismiss"), calls);
  }

  @Test
  public void reportsEveryFailureAndStillAttemptsAllSteps() {
    List<Exception> errors = new ArrayList<>();
    ScreenRecovery.restore(
        () -> { throw new Exception("power"); },
        () -> { throw new Exception("keys"); },
        () -> { throw new Exception("overlay"); },
        errors::add);
    assertEquals(3, errors.size());
    assertEquals("power", errors.get(0).getMessage());
    assertEquals("keys", errors.get(1).getMessage());
    assertEquals("overlay", errors.get(2).getMessage());
  }
}
