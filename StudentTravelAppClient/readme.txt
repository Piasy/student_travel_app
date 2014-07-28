Todo: 
20:44 2014/2/20
1. webview打开去哪儿网页会弹出提示下载去哪儿客户端的div，要把它过滤掉。
加载：http://touch.qunar.com/h5/hotel/hoteldetail?seq=beijing_city_2882&checkInDate=2014-02-22&checkOutDate=2014-02-23&bd_source=3w_hotel
div：<div id="QNT_box" class="QNT_download_touch" style="padding: 5px 0px 2px; width: 100%; overflow: hidden; background-color: rgba(0, 0, 0, 0.6); z-index: 9999; position: absolute; left: 0px; top: 1169px; display: block;">...</div>
参考：
http://www.zhihu.com/question/20741504
http://developer.android.com/reference/android/webkit/WebViewClient.html#onPageFinished(android.webkit.WebView,%20java.lang.String)
http://developer.android.com/reference/android/webkit/WebView.html#addJavascriptInterface(java.lang.Object,%20java.lang.String)

To do:

pre-alpha, 20:55 2013/11/3-星期日:
1. 美化UI!!!做好alpha发布的准备； 暂且ok了。。@10:45 2013/11/5-星期二
2. 部分请求的等待提示信息，防止重复发送请求 ok，删除修改提示信息已经加了  ok...  @17:28 2013/11/4-星期一
3. 图标	ok...  @12:08 2013/11/4-星期一
4. 检查网络状态  ok...  @18:42 2013/11/4-星期一
5. launch activity的内容  ok @10:45 2013/11/5-星期二


alpha, 10:46 2013/11/5-星期二
1. 增加更新检查，以便发布更新；
2. 增加账户功能，和服务器交互，增加服务器功能；
3. 优化路径规划方式；
4. 其他项目的优化。
5. 记录的列表显示，记录详情放在listview里面，评论区域大小
6. 历史记录查看时，显示线条和箭头选项，及其美化设计；
7. 规划的保存，以便提前规划好之后再查看。
8. 启动画面，功能介绍	
9. 生成规划时，若还在等待交通数据，则要在数据全部收到之后自动跳转到显示界面
