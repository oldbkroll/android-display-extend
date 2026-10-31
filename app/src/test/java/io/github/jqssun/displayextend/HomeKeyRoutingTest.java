package io.github.jqssun.displayextend;
import org.junit.Test;
import static org.junit.Assert.*;

public class HomeKeyRoutingTest {
  @Test public void phoneAndUnknownKeysRemainWithSystem() {
    assertFalse(HomeKeyRouting.shouldHandle(0, 2));
    assertFalse(HomeKeyRouting.shouldHandle(-1, 2));
    assertFalse(HomeKeyRouting.shouldHandle(1, 2));
  }
  @Test public void onlyMatchingExternalSessionReceivesHome() {
    assertTrue(HomeKeyRouting.shouldHandle(2, 2));
    assertFalse(HomeKeyRouting.shouldHandle(2, -1));
    assertFalse(HomeKeyRouting.shouldHandle(0, 0));
  }
}
