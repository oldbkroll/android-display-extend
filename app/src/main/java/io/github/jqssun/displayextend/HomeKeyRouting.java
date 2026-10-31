package io.github.jqssun.displayextend;

/** Never redirect phone or unknown-display HOME input to the external app. */
final class HomeKeyRouting {
  static boolean shouldHandle(int eventDisplay, int appDisplay) {
    return appDisplay > 0 && eventDisplay == appDisplay;
  }
  private HomeKeyRouting() {}
}
