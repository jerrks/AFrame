package com.xo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * @author JerrksLover at 2012-10-15 16:49:47
 */
public class BitmapUtils {
	private final static int IMAGE_MIN_SIZE = 1024; //1K
	public static Bitmap decodeBitmap(byte[] byteArray, int maxWidth,
			int maxHeight) {
		Bitmap b = null;
		try {
			if(byteArray.length > IMAGE_MIN_SIZE)
				b = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
		} catch (Exception e) {
			b = null;
		}
		return b;
	}

	// Note: this method does not scale the image, it just subsample the
	// original image to save memory.
	public static Bitmap decodeBitmap(File f, int maxWidth, int maxHeight) {

		Bitmap b = null;
		FileInputStream fis = null;
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			fis = new FileInputStream(f);
			BitmapFactory.decodeStream(fis, null, o);
			fis.close();

			int scale = 1;
			while (true) {
				if (o.outWidth / 2 < maxWidth || o.outHeight / 2 < maxHeight)
					break;
				o.outWidth /= 2;
				o.outHeight /= 2;
				scale *= 2;
			}
			
			// Decode with inSampleSize
			o.inJustDecodeBounds = false;
			o.inSampleSize = scale;
			o.inPurgeable = true;
			
			fis = new FileInputStream(f);
			b = BitmapFactory.decodeStream(fis, null, o);
			fis.close();
			
		} catch (Exception e) {
			e.printStackTrace();
			b = null;
		}finally{
			f = null;
		}
		return b;
	}

	public static Bitmap decodeBitmap(File file) {
		if (!file.exists()) {
			return null;
		}
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(file);
			Bitmap bmp = BitmapFactory.decodeStream(fileInputStream);
			fileInputStream.close();
			return bmp;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			file = null;
		}
	}

	/**
	 * @param bitmap
	 * @param name
	 * @param dir
	 * @return
	 */
	public static boolean saveBitmapToSD(Bitmap bitmap, String dir, String name) {
		File path = new File(dir);
		if (!path.exists()) {
			path.mkdirs();
		}
		File file = new File(path + "/" + name);
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100,
					fileOutputStream);
			fileOutputStream.flush();

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				fileOutputStream.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * save binary image data.
	 */
	public static boolean saveImageData(byte[] data, String dir, String name) {
		
		int length = data.length;
		if(data == null || length < IMAGE_MIN_SIZE)
			return false;
		FileOutputStream out = null;
		
		File path = new File(dir);
		try {
			if (!path.exists()) {
				if (!path.mkdirs())
					return false;
			}
			File f = new File(path, name);
			out = new FileOutputStream(f);
			out.write(data);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			path = null;;
			try {
				if(out != null)
					out.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		return true;
	}

	/**
	 * @param path
	 * @return
	 */
	public static Bitmap getBitmapFromSD(String path, int maxWidth,
			int maxHeight) {
		Bitmap bmp;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		// we can not get access time, so just update modify time.
		// we use this time stamp to decide whether cache file expire or not
		try {
			file.setLastModified(System.currentTimeMillis());
			bmp = decodeBitmap(file, maxWidth, maxHeight);
			return bmp;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			if(file != null){
				file = null;
			}
		}
	}

	/**
	 * @param path
	 * @return
	 */
	public static Bitmap getBitmapFromSD(String path) {
		Bitmap bmp = null;
		File file = new File(path);
		if (!file.exists()) {
			return bmp;
		}
		try {
			bmp = decodeBitmap(file);
		} catch (Exception e) {
			bmp = null;
			e.printStackTrace();
		}finally{
			if(file != null){
				file = null;
			}
		}
		return bmp;
	}

	public static InputStream getInputStream(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		try {
			FileInputStream fileInputStream = null;
			try {
				fileInputStream = new FileInputStream(file);
				return fileInputStream;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			if(file != null){
				file = null;
			}
		}
	}

	public static byte[] getByteArray(String path) {
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		try {
			FileInputStream fileInputStream = null;
			try {
				fileInputStream = new FileInputStream(file);
				// define the byte array according to the file length
				byte[] byteArray = new byte[(int) file.length()];
				for (int i = 0; i < byteArray.length; i++) {
					// fetch 1 byte per time
					byteArray[i] = (byte) fileInputStream.read();
				}
				fileInputStream.close();
				return byteArray;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}finally{
			if(file != null){
				file = null;
			}
		}
	}

	public static Bitmap resizeBitmap(Bitmap input, int destWidth,
			int destHeight) {
		int srcWidth = input.getWidth();
		int srcHeight = input.getHeight();
		boolean needsResize = false;
		float p;
		if (srcWidth > destWidth || srcHeight > destHeight) {
			needsResize = true;
			if (srcWidth > srcHeight && srcWidth > destWidth) {
				p = (float) destWidth / (float) srcWidth;
				destHeight = (int) (srcHeight * p);
			} else {
				p = (float) destHeight / (float) srcHeight;
				destWidth = (int) (srcWidth * p);
			}
		} else {
			destWidth = srcWidth;
			destHeight = srcHeight;
		}
		if (needsResize) {
			Bitmap output = Bitmap.createScaledBitmap(input, destWidth,
					destHeight, true);
			return output;
		} else {
			return input;
		}
	}
}
