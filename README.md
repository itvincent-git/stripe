# Kotlin 扩展方法

## CollectionsEx.kt
- `random()`: 从一个List中随机取1个数据

## NullPointerEx.kt
- `notNullElse()`: 判断非null执行第一个block,it为对象的非null类型;为null则执行第二个block
- `allNotNull()`: 判断2个变量都非空执行block, 参数a, b为对象的非null类型
- `allNotNullElse()`: 判断3个变量都非空执行第一个block, 参数a, b, c为对象的非null类型；为null则执行第二个block


## TaskEx.kt
- `runInMainThread()`: 在main线程执行，可延迟执行，带生命周期管理

## TryEx.kt
- `tryCatch()`: 捕获block的异常，然后返回block的值，默认异常是打印logcat日志
- `tryTimes()`: try catch运行block，如果有异常则再运行，直接超时times的次数