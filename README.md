# burp_regexkey
通过正则匹配返回包中一些key之类的敏感信息的BurpSuite被动扫描插件<br>
正则表达式在src/main/resouces/json/1.json，直接增删改后重新打包即可，非常方便(打包后可能会有两个jar包，使用burp_regexkey.jar)
# 简介
之前看到一篇公众号的文章，通过正则表达式搜索返回包中的敏感信息，感觉这个方法挺好的，运气好的话还可以搜到云存储桶的key和id
https://mp.weixin.qq.com/s/kqIxdCAqBFZ8a50QMK9THw  
![图片](https://user-images.githubusercontent.com/48166761/181022447-9fa661b0-b26a-4ae6-b2de-85429b8a7191.png)  
<br>

后来又找到了另一个扫描apk敏感信息的项目的正则表达式 
https://github.com/dwisiswant0/apkleaks/blob/master/config/regexes.json
![图片](https://user-images.githubusercontent.com/48166761/181022972-82018cf4-dd48-4837-9d6f-baf59f302c56.png) 
<br>

但是时不时要搜一次，搜完后还得从返回包中再去找匹配到的关键词，就挺麻烦的，而且搜集到的正则表达式越多就越麻烦。

所以写了个被动扫描的插件，自动去对返回包内容匹配，就方便多了，经过一段时间的测试也删减了一些误报比较多的规则。

效果如下，可以展示出匹配的结果和那条结果对应的正则表达式，如果觉得哪一条正则不合适，可以在1.json进行增删改后重新打包，非常方便
![图片](https://user-images.githubusercontent.com/48166761/181031737-55a88415-df45-4c30-94d2-7442904b0546.png)
