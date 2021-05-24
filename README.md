# lib-kvstore
基于MMKV封装的本地数据存储库，使用简单方便

## 引入

[![](https://jitpack.io/v/gittosuperfly/lib-kvstore.svg)](https://jitpack.io/#gittosuperfly/lib-kvstore)


**Step 1**. 添加JitPack repository到你项目的build.gradle文件

```groovy
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```

**Step 2**. 添加库依赖
```groovy
	dependencies {
	    implementation 'com.github.gittosuperfly:lib-kvstore:Version'
	}
```


## 使用

**Step 1**. 在Application中初始化KVstore：

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        //初始化
        KVStore.init(this)
        //使用Gson的SerializedName更改变量序列化名称，而不是默认的@FieldRename
        KVStore.isSerializedName(true)
    }
}
```

**Step 2**. 创建一个数据储存类

```kotlin
@StoreRename(value = "app_store_demo") //该注解用于重新设置MMKV的ID,默认为类名
class AppStore : BaseStore() {

    @SerializedName("app_value") //如果设置了isSerializedName(true)则可使用Gson的@SerializedName
    var value = "test"

    @FieldRename("app_name") //如果未设置isSerializedName(true),则使用默认注解
    var name = "test"

    @FieldIgnore //忽略该元素
    var str = "test"
}
```

**Step 3**. 存放数据

```kotlin
fun testFunction(){
    val data = KVStore.load(AppStore::class.java) //加载Store, 每个Store类对应的对象都是一个单例
    data.value = "111"
    data.name = "222"
    
    //提交修改
    data.apply() //同步提交
    data.commit() //异步提交
}
```
