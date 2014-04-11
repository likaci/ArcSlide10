package com.xiazhiri.ArcSlide10;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SavePictureTask extends AsyncTask<byte[], String, String> {
    @Override
    protected String doInBackground(byte[]... params) {
        //创建文件
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File picture = new File(Environment.getExternalStorageDirectory(), timeStamp + ".jpg");
        //如果文件存在，删除现存文件
        if(picture.exists())
            picture.delete();
        try{
            //获取文件输出流
            FileOutputStream fos = new FileOutputStream(picture);
            //写到该文件
            fos.write(params[0]);
            //关闭文件流
            fos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
