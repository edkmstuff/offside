package com.offsidegame.offside.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.widget.ImageView;

import com.offsidegame.offside.R;
import com.offsidegame.offside.models.OffsideApplication;
import com.squareup.picasso.Picasso;

import org.acra.ACRA;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by KFIR on 3/13/2017.
 */

public class ImageHelper {

    private static Integer [] backgroundDefaultImage = {
            R.color.defaultImageBackgroundColor1,
            R.color.defaultImageBackgroundColor2,
            R.color.defaultImageBackgroundColor3,
            R.color.defaultImageBackgroundColor4,
            R.color.defaultImageBackgroundColor5,
            R.color.defaultImageBackgroundColor6,


    };
    public static void loadImage(final Activity activity, final String imageUrl, final ImageView imageView, final String callerName, final boolean isRoundedImage) {

        try
        {

            Picasso.with(activity).load(imageUrl).into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    if (isRoundedImage) {
                        Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        RoundImage roundedImage = new RoundImage(bm);
                        imageView.setImageDrawable(roundedImage);
                    }
                }

                @Override
                public void onError() {
                    Exception ex = new RuntimeException(callerName + " - onCreate - Error loading image with url: " + imageUrl);
                    ACRA.getErrorReporter().handleSilentException(ex);
                }
            });
        } catch (Exception ex) {
                    ACRA.getErrorReporter().handleSilentException(ex);

        }

    }

    public static void loadImage(Context context, final File imagePath, final ImageView imageView, final String callerName, final boolean isRoundedImage) {
        try
        {
            Picasso.with(context).load(imagePath).into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    if (isRoundedImage) {
                        Bitmap bm = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                        RoundImage roundedImage = new RoundImage(bm);
                        imageView.setImageDrawable(roundedImage);
                    }
                }

                @Override
                public void onError() {
                    throw new RuntimeException(callerName + " - onCreate - Error loading image with path: " + imagePath.getPath());
                }
            });

        } catch (Exception ex) {
                    ACRA.getErrorReporter().handleSilentException(ex);

        }

    }

    public static void loadImage(Context context, final ImageView fbProfilePicture, Uri fbImageUri, final boolean isRoundedImage) {
        try
        {
            Picasso.with(context).load(fbImageUri).into(fbProfilePicture, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    if (isRoundedImage) {
                        Bitmap bm = ((BitmapDrawable) fbProfilePicture.getDrawable()).getBitmap();
                        RoundImage roundedImage = new RoundImage(bm);
                        fbProfilePicture.setImageDrawable(roundedImage);
                    }
                }

                @Override
                public void onError() {

                }
            });

        } catch (Exception ex) {
                    ACRA.getErrorReporter().handleSilentException(ex);

        }

    }

    public static void loadImage(Context context, final ImageView fbProfilePicture, int resourceId, final boolean isRoundedImage ) {
        try
        {
            Picasso.with(context).load(resourceId).into(fbProfilePicture, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    if (isRoundedImage) {
                        Bitmap bm = ((BitmapDrawable) fbProfilePicture.getDrawable()).getBitmap();
                        RoundImage roundedImage = new RoundImage(bm);
                        fbProfilePicture.setImageDrawable(roundedImage);
                    }
                }

                @Override
                public void onError() {

                }
            });

        } catch (Exception ex) {
                    ACRA.getErrorReporter().handleSilentException(ex);

        }

    }

    public static void storeImage(Bitmap image, Context context) {
        try
        {

            String filename = OffsideApplication.getProfileImageFileName();
            FileOutputStream outputStream;
            try {
                outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
                image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.close();

            } catch (Exception ex) {
                ACRA.getErrorReporter().handleSilentException(ex);
            }
        } catch (Exception ex) {
                    ACRA.getErrorReporter().handleSilentException(ex);

        }


    }

    public static Bitmap generateInitialsBasedProfileImage(String initials, Context context, int selectedColorId) {

        try
        {

            Bitmap bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            int colorPrimary = Color.parseColor("#" + Integer.toHexString(ContextCompat.getColor(context, selectedColorId)));

            paint.setColor(colorPrimary);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(50, 50, 51, paint);


            Paint textPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
            textPaint.setColor(Color.WHITE);
            textPaint.setTextAlign(Paint.Align.CENTER);

            textPaint.setTextSize(40);


            textPaint.setStyle(Paint.Style.FILL);

            //paint.setTextSize(20);
            int xPos = (canvas.getWidth() / 2);
            int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));


            canvas.drawText(initials, xPos, yPos, textPaint);
            return bitmap;
        } catch (Exception ex) {
                    ACRA.getErrorReporter().handleSilentException(ex);
                    return null;

        }

    }

    public static int getRandomColor(){

        int selectedColorId =0;
        Random r = new Random();
        int Low = 0;
        int High = 6;
        int result = r.nextInt(High-Low) + Low;
        selectedColorId = backgroundDefaultImage[result];

        return selectedColorId;

    }

    // convert from bitmap to byte array
    public static byte[] getBytesFromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
        return stream.toByteArray();
    }

    @Nullable
    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (Exception ex) {
            ACRA.getErrorReporter().handleSilentException(ex);
            return null;

        }
    }

}
