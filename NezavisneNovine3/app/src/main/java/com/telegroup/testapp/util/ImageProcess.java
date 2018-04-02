package com.telegroup.testapp.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.security.InvalidParameterException;

/**
 * Created by Nemanja Đokić on 18/03/29.
 */

public class ImageProcess {


    private static final int gaussianMatrix3x3[][] = {
            {1, 2, 1},
            {2, 4, 2},
            {1, 2, 1}
    };
    private static final int gaussianMatrix3x3Denominator = 16;

    private static final int gaussianMatrix5x5[][] = {
            {1, 4, 7, 4, 1},
            {4, 16, 26, 16, 4},
            {7, 26, 41, 26, 7},
            {4, 16, 26, 16, 4},
            {1, 4, 7, 4, 1}
    };
    private static final int gaussianMatrix5x5Denominator = 273;

    public static Bitmap doGaussianBlur(Bitmap bitmap, int matrixDimension) throws IllegalArgumentException{
        int gaussianMatrix[][];
        int gaussianDenominator;
        switch (matrixDimension){
            case 3:
                gaussianMatrix = gaussianMatrix3x3;
                gaussianDenominator = gaussianMatrix3x3Denominator;
                break;
            case 5:
                gaussianMatrix = gaussianMatrix5x5;
                gaussianDenominator = gaussianMatrix5x5Denominator;
                break;
            default:
                throw new IllegalArgumentException("Invalid matrix dimensions. Use 3 or 5");
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        ByteBuffer buffer = ByteBuffer.allocate(bitmap.getWidth() * bitmap.getHeight() * 3);
        bitmap.copyPixelsToBuffer(buffer);
        byte[] pixelData = buffer.array();
        Pixel[][] pixels = new Pixel[bitmap.getWidth()][bitmap.getHeight()];
        for(int i = 0; i < bitmap.getHeight(); i++){
            for(int j = 0; j < bitmap.getWidth(); j+= 3){
                pixels[i][j] = new Pixel(
                        pixelData[i*bitmap.getWidth() + j],
                        pixelData[i*bitmap.getWidth() + j + 1],
                        pixelData[i*bitmap.getWidth() + j + 2]
                );
            }
        }
        for(int i = (matrixDimension/2)+1; i < bitmap.getWidth() - (matrixDimension/2)+1; i++){
            for(int j = (matrixDimension/2)+1; j < bitmap.getHeight() -(matrixDimension/2)+1; j++){
                int redSum = 0;
                int greenSum = 0;
                int blueSum = 0;
                for(int p = ((matrixDimension/2))*(-1); p < ((matrixDimension/2)); p++){
                    for(int q = ((matrixDimension/2))*(-1); q < ((matrixDimension/2)); q++) {
                        System.out.println(" " + i + " " + j);
                        redSum += (pixels[i + p][j + q]).red;
                        greenSum += (pixels[i+p][j+q]).green;
                        blueSum += (pixels[i+p][j+q]).blue;
                    }
                    pixels[i][j].red = (byte)(redSum/gaussianDenominator);
                    pixels[i][j].green = (byte)(greenSum/gaussianDenominator);
                    pixels[i][j].blue = (byte)(blueSum/gaussianDenominator);
                }
            }
        }
        for(int i = 0; i < bitmap.getHeight(); i++){
            for(int j = 0; j < bitmap.getWidth(); j+= 3){
                pixelData[i*bitmap.getWidth() + j] = pixels[i][j].red;
                pixelData[i*bitmap.getWidth() + j + 1] = pixels[i][j].green;
                pixelData[i*bitmap.getWidth() + j + 2] = pixels[i][j].blue;
            }
        }
        bitmap.copyPixelsFromBuffer(ByteBuffer.wrap(pixelData));
        return bitmap;
    }

    public static Bitmap darken(Bitmap bitmap){
        Canvas canvas = new Canvas(bitmap);
        Paint p = new Paint(Color.RED);
        //ColorFilter filter = new LightingColorFilter(0xFFFFFFFF , 0x00222222); // lighten
        ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bitmap, new Matrix(), p);

        return bitmap;
    }

}


class Pixel{

    public Pixel(byte red, byte green, byte blue){
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public byte red;
    public byte green;
    public byte blue;
}