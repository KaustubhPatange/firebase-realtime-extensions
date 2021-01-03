# Realtime Database Extensions

![build](https://github.com/KaustubhPatange/firebase-realtime-extensions/workflows/build/badge.svg)
![Maven Central](https://img.shields.io/maven-central/v/io.github.kaustubhpatange/realtime-extensions)

A set of Kotlin extensions for Firebase Realtime database to seamlessly suspend the callback listeners.

I wrote these extensions couple of months back & found to be copy pasting them in most of the projects, hence decided to combine them in a library.

## Download

Library is available on `MavenCentral()`.

```gradle
implementation "io.github.kaustubhpatange:realtime-extensions:<version>"
```

## Usage

Library provides some suspending extension functions which can be used through [Coroutines](https://kotlinlang.org/docs/reference/coroutines-overview.html).

A basic setup is shown below. You can find the whole list of extensions [here](https://github.com/KaustubhPatange/firebase-realtime-extensions/blob/master/library/src/main/java/com/kpstv/firebase/extensions/DataReferenceExt.kt).

- Since all the calls are suspending, we will chain [`CoroutineContext`](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.coroutines/-coroutine-context/) with a [`SupervisorJob`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-supervisor-job.html) to create our custom [`CoroutineScope`](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines/-coroutine-scope/). This will help us to cancel the job (Firebase events) if needed.

```kotlin
val ref: DataReference = ...

val job = SupervisorJob()
CoroutineScope(... + job).launch {
   ... // all the suspending calls.
}

// To cancel the job/event use
job.cancel()
```

- Listen for **ValueEventListener** or **ChildEventListener** on a `DatabaseReference`.

```kotlin
// Or childEventFlow()
ref.valueEventFlow().collect { result ->
   when(result) {
      ...
   }
}
```

- Use the following suspendable methods which returns [`DataResponse<T>`](https://github.com/KaustubhPatange/firebase-realtime-extensions/blob/master/library/src/main/java/com/kpstv/firebase/Response.kt) object.

```kotlin
suspend DatabaseReference.setValueAsync(value): DataResponse<DatabaseReference>
suspend DatabaseReference.updateChildrenAsync(value): DataResponse<DatabaseReference>
suspend DatabaseReference.removeValueAsync(): DataResponse<DatabaseReference>

suspend DatabaseReference.singleValueEvent(): DataResponse<DataSnapshot>
```

## Resources

- **Medium Articles**
  - [Suspending Firebase Realtime Database with Kotlin Coroutines](https://developerkp16.medium.com/suspending-firebase-realtime-database-with-kotlin-coroutines-76b4651bc0e8).

## License

- [The Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt)

```
Copyright 2020 Kaustubh Patange

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
