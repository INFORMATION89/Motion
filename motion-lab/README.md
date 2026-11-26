# Motion Lab

A standalone Android sample app for exploring the public Motion API. It is intentionally independent of Solstice and does not define the planned `:motion-ui` component library.

## Build and install

From the repository root:

```bash
./gradlew :motion-lab:assembleDebug
./gradlew :motion-lab:installDebug
```

Use `gradlew.bat` on Windows.

## Current demonstrations

- **Springs and release:** compare Motion spring tokens, then drag and release a MotionValue.
- **Morph:** open a surface from its measured source anchor, and scrub it closed with a drag.
- **Page movement:** swipe pages with Motion's spring fling, or compare Motion's `springToPage` with Compose's default pager animation. The comparison sends the same selected destination to both previews.
- **Dial:** drag, flick, and snap a dial across a finite range.
- **Scroll edge blur:** explore progressive blur and fade at the edges of a horizontal list.

The slow-motion slider scales springs started after the setting changes. The Lab resets `MotionTime.scale` when its root composition is disposed.

The app uses neutral Material 3 components and generic content. Android 10 / API 29 is the current minimum. Physical-device and emulator behavior should be treated as `NOT TESTED` / `DEFERRED` until actually exercised; a successful CI build is not a device validation.
