# SelectPhoto_Android
一行代码选择图片

关键点就是把相机和相册的回调放在了一个透明的Activiy里处理，这样就不会和业务Activity耦合了！

使用：
1.选择一张正方形图片
```
  SelectPhotoSheet.showCropImageSheet(this, new AlbumResultListener() {
            @Override
            public void complete(View tapedView, List<String> pathList) {

            }
        });
```
2.选择多张全尺寸图片
```
    SelectPhotoSheet.showFullImageSheet(this, 8, new AlbumResultListener() {
            @Override
            public void complete(View tapedView, List<String> pathList) {

            }
        });
```
pathList里存的都是原始图片的路径，使用时候要压缩处理。可以使用
```
FileUtil.getCompressedPath(String originImagePath, String imageName, int maxPx); 
```
获取压缩后的图片本地路径。

![image](https://github.com/AndyFightting/SelectPhoto_Android/blob/master/tmp_image.png)

Thakes to [PhotoView](https://github.com/chrisbanes/PhotoView)
