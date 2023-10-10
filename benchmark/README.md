Capture Baseline Profile
========================

Usage
-----

### Authenticator ###

```shell
# for logcat terminal
pidcat -t Benchmark

# open new terminal
./gradlew :benchmark:installBenchmark

adb shell am instrument -w -e class com.example.study.accountmanager.macrobenchmark.baselineprofile.BaselineProfileGenerator#startupLoginActivity com.example.study.accountmanager.macrobenchmark/androidx.test.runner.AndroidJUnitRunner
```

### Client ###

```shell
# for logcat terminal
pidcat -t Benchmark

# open new terminal
./gradlew :benchmark:installBenchmark -PtargetProject=:client

adb shell am instrument -w -e class com.example.study.accountmanager.macrobenchmark.baselineprofile.BaselineProfileGenerator#startupClientMainActivity com.example.study.accountmanager.macrobenchmark/androidx.test.runner.AndroidJUnitRunner
```
