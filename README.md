# TTStreet-core
淘淘街核心jar包
- ServiceCaller
    - 服务自动注册和发现
    - 基于hystrix的服务熔断和降级
    - 系统间服务调用
    - 服务监控
    - 网络拓扑（todo）
- @Json
    - 基于注解的调用参数加密&序列化和反序列化
    - 支持get和post
- httpClient
    - 封装的http调用客户端
- graceful redis
    - 封装的redis调用
    - 封装的redis operations
- @TTSCache
    - 通过注解和aspectJ自动添加缓存和查询缓存
    - @TTSCacheClean 清理缓存
    - @TTSCacheUpdate 更新缓存
- todo

如何使用jar包：
1. 下载本工程
1. 执行mvn install
1. 引入maven依赖：

```xml
<dependency>
    <groupId>com.tts.core</groupId>
    <artifactId>tts-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```
some test in：
[DotaCardsPlay](https://github.com/Fish-Potato/DotaCardsPlay/tree/test-tts-core)
