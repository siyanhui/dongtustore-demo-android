# 表情云商店SDK接入说明

欢迎使用`表情云商店SDK`。

SDK主要包括jar包、jni库、AndroidManifest.xml，以及res目录下的一些资源文件。我们建议将它作为一个模块（module）集成到gradle项目中去。

## 第一步：下载并引入module

首先将`DongtuStoreSDK`目录复制到项目中。

修改`settings.gradle`，加入如下代码，让gradle识别这个module：

```groovy
include 'DongtuStoreSDK'
```

找到需要使用DongtuStoreSDK的module（一般是应用的module，或者应用所用到的UI组件库module），修改它的`build.gradle`，在dependencies中加入：

```groovy
api project(':DongtuStoreSDK')
```

然后，module的引入就宣告完成了。

## 第二步：传入必要信息

开发者通过如下方法传入配置信息：

```java
DongtuStore.initConfig(context, APP_ID, APP_SECRET);
```

该方法在APP的生命周期中调用一次即可，但需要早于SDK的任何其他方法被调用。我们建议在Application.onCreate()中调用它。

该方法的主要作用有两个：

1. 注册在动图宇宙官网申请到的`App ID`和`App Secret`，以便SDK调用后台接口；

2. 传入一个`Context`进行一些数据的准备（例如查找资源ID、得到cache的路径等）。

在使用SDK的功能之前，还需要确保用户信息已传入。传入用户信息的代码如下：

```java
DongtuStore.setUserInfo(USER_ID, USER_NAME, USER_GENDER, USER_ADDRESS, USER_EMAIL, USER_PHONE, EXTRA_INFO);
```

## 第三步：使用功能

### 1. 单例的加载与销毁

`DongtuStore`在使用时，会有一个单例一直存在于内存中。开发者需要对单例的生命周期进行管理。

使用前加载单例：

```java
DongtuStore.load()
```

使用后销毁单例：

```java
DongtuStore.destroy()
```

开发者可以根据情况选择是加载一次单例之后让它常驻内存，还是在Activity的onCreate()和onDestroy()中进行它的加载和销毁。但需要注意的是，如果在onCreate()中进行的单例的加载，请不要在onPause()中进行单例销毁，否则会引发问题。

另外需要特别注意的是，SDK提供的控件有可能会在初始化时调用`DongtuStoreSDK`的功能。如果在xml布局中使用了`DTStoreKeyboard`、`DTStoreEditView`等控件，那么在使用xml生成View之前（包括inflate、setContentView等操作），必须确保已经调用了load()，否则会引发闪退。

### 2. 传入UI组件

加载单例之后，需要调用方法传入一些UI组件。`DongtuStore`会给这些组件加上必要的Listener、填充数据。

```java
DongtuStore.setKeyboard(mKeyboard);
DongtuStore.setEditText(mEditView);
```

### 3. 设置联想功能

联想功能可以在用户输入文字时实时地进行文字搜索，将搜到的动图以弹窗形式展示给用户。

```java
DongtuStore.setupSearchPopupAboveView(anchorView, editView);
```

其中anchorView是定位弹窗用的。

### 4. 设置消息发送回调

开发者需要设置回调来处理三种通过`DongtuStore`发送的消息，并对消息进行封装和发送。这里演示的是将消息封装为JSON的方法。

```java
DongtuStore.setSendMessageListener(new DTStoreSendMessageListener() {
    /**
     * 在表情键盘中点击表情发送的消息
     */
    @Override
    public void onSendSticker(String code) {
        JSONArray messageData = new JSONArray();
        JSONArray messageItem = new JSONArray();
        messageItem.put(code);
        messageItem.put(2);
        messageData.put(messageItem);
        sendMessage(messageData);
    }

    /**
     * 通过动图搜索弹窗发送的消息
     */
    @Override
    public void onSendDTImage(DTImage image) {
        HashMap<String, Object> messageDataMap = new HashMap<>();
        messageDataMap.put("image", image.getImage());
        messageDataMap.put("id", image.getId());
        messageDataMap.put("width", image.getWidth());
        messageDataMap.put("height", image.getHeight());
        messageDataMap.put("is_animated", image.isAnimated());
        JSONObject messageData = new JSONObject(messageDataMap)
        sendMessage(messageData);
    }
});
```
### 5. 展示消息

展示消息需要用到的控件有两种。商店表情和文字需要用`DTStoreMessageView`展示，而动图需要使用`DTImageView`来展示。

展示文字时：

```java
dtStoreMessageView.showText(text);
```

展示商店表情时：

```java
dtStoreMessageView.showSticker(stickerCode);
```

展示动图时：

```java
DongtuStore.loadImageInto(dtImageView, image, id, width, height);
```

`width`和`height`参数的作用是指定动图的显示长宽。

### 6. 消息的封装与解析

封装消息的格式与SDK无关，只要保证将必要的信息传送到接收端，令其能够正确解析和展示动图即可。

对于不同的消息类型，必要的信息分别为：

* 文字消息
    * 文本

* 商店表情消息
    * 表情code

* 动图消息
    * image（String，由`DTImage.getImage()`获得）
    * ID（String，由`DTImage.getId()`获得）
    * width（int，由`DTImage.getWidth()`获得）
    * height（int，由`DTImage.getHeight()`获得）
    * isAnimated（int，0代表否，1代表是，由`DTImage.isAnimated()`获得，iOS需要）

### 7. UI定制

SDK对部分UI提供了定制。开发者如果需要更改颜色，可以修改`dtstore_color.xml`中的相应值，如果需要更改控件大小，可以修改`dtstore_dimens.xml`中的相应值