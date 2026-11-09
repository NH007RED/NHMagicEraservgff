# NH Magic Eraser v3 (Premium UI)

Offline object / logo / watermark eraser for your own images and videos.
Kotlin, Jetpack Compose, Material 3. Package `com.nh.magiceraser`. minSdk 24, targetSdk 34.

## What's new in v3
- Dark premium design: animated gradient background, glass cards, gradient buttons with press animations
- Animated splash and staggered home screen with shimmering feature cards
- Image editor: pinch-zoom and pan, two-finger gestures, before/after slider, undo and redo, scan animation while erasing, save sheet with animated success check
- Video editor: animated dashed selection boxes with corner handles, area chips, frame scrubber with timecode
- Video export screen: live cleaned-frame preview with scan line, gradient progress ring, elapsed and remaining time, cancel
- Success screen with animated check, confetti, Play and Share buttons

## Open and build
1. Open the folder in Android Studio or Code On The Go.
2. If `gradlew` is missing, copy `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` from another project.
   Gradle 8.9, AGP 8.5.2, Kotlin 2.0.20.
3. Build and run `app`.

## Optional: better quality with LaMa
Place `lama_inpaint.tflite` in `app/src/main/assets/` (image `[1,S,S,3]` + mask `[1,S,S,1]` in, `[1,S,S,3]` out).
Without it the built-in pure Kotlin fill is used.

## Known limits
- Video export is frame by frame and can be slow. Keep the app open.
- Boxes apply to the whole video.
- Fallback fill is soft on large areas.

## Use responsibly
Use only on content you own or have permission to edit. Don't strip ownership, copyright or AI-origin marks to mislead others.
