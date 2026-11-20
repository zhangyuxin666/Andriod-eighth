# 实验八 网络天气应用（WeatherApp）

Android 移动应用开发 实验八作业：基于 **Volley + Gson** 的网络天气应用。
作者：张雨鑫 2024110548

## 功能

- **主界面**：后台线程逐行读取 `res/raw/citycode.json`（324 KB），用 Gson 反序列化 2532 条记录，
  按省份分组（35 个分组、2504 个可查询城市）显示在列表中，支持按城市名或城市代码搜索。
- **天气详情**：点击城市进入第二个界面，用 Volley 请求
  `http://t.weather.itboy.net/api/weather/city/{城市代码}`，Gson 解析后显示实况温度、天气现象、
  高低温、空气质量、湿度、风向、风力、PM2.5、PM10、日出日落以及未来 7 天预报。
- **异常处理**：网络异常、JSON 解析失败、data 为空、status 非 200 时给出中文提示和“重新加载”入口。

## 技术要点

| 项目 | 说明 |
| --- | --- |
| 语言 / 构建 | Java，Gradle 9.5 + AGP 9.3，compileSdk 37，minSdk 23，targetSdk 34 |
| 网络 | Volley 1.2.1（StringRequest + 主线程回调） |
| JSON | Gson 2.13.2（TypeToken 泛型解析、字段名自动映射） |
| 线程 | ExecutorService 读取/解析，Handler 回主线程 |
| 其它 | `android:usesCleartextTraffic="true"`（接口为 http）；图标为 PIL 绘制的透明 PNG |

## 运行

```bash
# 需 JDK 17+（本工程用 Android Studio 自带 JBR 25）
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

模拟器如遇 DNS 解析失败，可用 `emulator -avd <name> -dns-server 8.8.8.8,114.114.114.114` 启动。

## 目录结构

```
app/src/main/java/cn/edu/sicnu/stu/zhangyuxin/eighth/
├── MainActivity.java      主界面、搜索、后台读取城市列表
├── CityRepository.java    Gson 解析与省级分组（pid 回溯）
├── CityAdapter.java       分组列表适配器与关键字过滤
├── WeatherActivity.java   Volley 请求、Gson 解析、天气渲染
└── model/{City,Weather}.java
app/src/main/res/raw/citycode.json   本地城市列表
app/src/main/res/drawable-nodpi/     六类天气图标
```
