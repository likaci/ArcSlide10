package com.xiazhiri.ArcSlide10;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.IOException;

/**
 * Created by Administrator on 2014/4/2.
 */
public class ActivityCamera extends Activity {

    Camera camera;
    SurfaceHolder surfaceHolder;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        /*
        String picPath = getCacheDir().getAbsolutePath() + "/new.jpg";
        File picFile = new File(picPath);
        try {
            picFile.createNewFile();
            picFile.setWritable(true,false);
        }
        catch (Exception e) {
            Log.e("CameraFileCreateE",e.getMessage());
        }
        */
        SurfaceView surfaceView = (SurfaceView)findViewById(R.id.camPreview);
        camera = Camera.open();
        /*
        ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
        layoutParams.height = camera.getParameters().getSupportedPreviewSizes().get(3).height;
        layoutParams.width = camera.getParameters().getSupportedPreviewSizes().get(3).width;
        surfaceView.setLayoutParams(layoutParams);
        */
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(
                new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                try{
                    //设置预览
                    camera.setPreviewDisplay(surfaceHolder);
                }catch(IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3) {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPictureSize(i2, i3);
                parameters.setPictureFormat(ImageFormat.JPEG);
                camera.startPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                //停止预览
                camera.stopPreview();
                //释放相机资源
                camera.release();
                camera=null;
            }
        });

        ((Button)findViewById(R.id.btnTakePic)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(
                        new Camera.ShutterCallback() {
                            @Override
                            public void onShutter() {
                            }
                        },
                        new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] bytes, Camera camera) {
                            }
                        },
                        new Camera.PictureCallback() {
                            @Override
                            public void onPictureTaken(byte[] bytes, Camera camera) {
                                new SavePictureTask().execute(bytes);
                            }
                        }
                );
            }
        });
    }
}



