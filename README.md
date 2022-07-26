# burp_regexkey
通过正则匹配返回包中一些key之类的敏感信息的BurpSuite被动扫描插件
# 简介
之前看到一篇公众号的文章，通过正则表达式搜索返回包中的敏感信息，感觉这个方法挺好的，运气好的话还可以搜到云存储桶的key和id
https://mp.weixin.qq.com/s/kqIxdCAqBFZ8a50QMK9THw
![图片](https://user-images.githubusercontent.com/48166761/181022447-9fa661b0-b26a-4ae6-b2de-85429b8a7191.png)

后来另一个扫描apk敏感信息的项目的正则表达式
https://github.com/dwisiswant0/apkleaks/blob/master/config/regexes.json
![图片](https://user-images.githubusercontent.com/48166761/181022972-82018cf4-dd48-4837-9d6f-baf59f302c56.png)

但是时不时要搜一次，搜完后还得从返回包中再去找匹配到的关键词，就挺麻烦的，而且搜集到的正则表达式越多就越麻烦。
所以写了个被动扫描的插件，自动去对返回包内容匹配，就方便多了


