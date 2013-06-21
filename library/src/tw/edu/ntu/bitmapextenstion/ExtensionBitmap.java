package tw.edu.ntu.bitmapextenstion;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.Log;

public class ExtensionBitmap {

	private int width;
	private int height;
	private Bitmap origBitmap;
	private int[] values;
	
	public ExtensionBitmap(Bitmap bm) {
		origBitmap = bm;
		width = origBitmap.getWidth();
		height = origBitmap.getHeight();
		values = new int[width * height];
		origBitmap.getPixels(values, 0, width, 0, 0, width, height);
	}
	
	public Bitmap getGrayScaledBitmap() {
		short[][] arr = getGrayScaleArray();
		
		Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		for(int i = 0 ; i < height ; i++) {
			for (int j = 0 ; j < width ; j++) {
				bm.setPixel(j, i, 0xff000000 | arr[i][j] | (arr[i][j] << 8) | (arr[i][j] << 16));
			}
		}
		return bm;
	}
	
	public Bitmap getQuantizedBitmap(int num) {
		Bitmap bm = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		int[] arr = new int[num];
		float step = 255 / num;
		for (int i = 0 ; i < num ; ++i)
			arr[i] = (int)(step * i);
		arr[num-1] = 255;
		
		int[] thre = new int[num-1];
		for (int i = 0 ; i < num-1 ; ++i) {
			thre[i] = (arr[i] + arr[i+1]) / 2;
			Log.d("!!", "!! thre = " + thre[i]);
		}
		short[][] gray = getGrayScaleArray();
		for (int i = 0 ; i < height ; ++i) {
			for (int j = 0 ; j < width ; ++j) {
				for (int k = 0 ; k < num-1 ; ++k) {
					if(gray[i][j] < thre[k]) {
						gray[i][j] = (short) arr[k];
						break;
					} else {
						gray[i][j] = 255;
					}
				}
			}
		}
		
		for(int i = 0 ; i < height ; i++) {
			for (int j = 0 ; j < width ; j++) {
				bm.setPixel(j, i, 0xff000000 | gray[i][j] | (gray[i][j] << 8) | (gray[i][j] << 16));
			}
		}
		
		return bm;
	}
	
	public short[][] getGrayScaleArray() {
		short[][] src = new short[height][width];
		Log.d("!!","!! " + src.length + "  " + src[0].length);
		Log.d("!!","!! " + height + "  " + width);
		int index = 0;
		for (int i = 0 ; i < height ; ++i) {
			for (int j = 0 ; j < width ; ++j) {
				src[i][j] = (short) ((float)((values[index] & 0x00ff0000) >> 16) * 0.3f  + 
									(float)((values[index] & 0x0000ff00) >> 8 ) * 0.58f +
									(float)((values[index] & 0x000000ff) * 0.12f ));
				if (src[i][j] < 0) src[i][j] = 0;
				if (src[i][j] > 255) src[i][j] = 0;
				
			    index++;
			}
		}
		return src;
	}
	
	public short[][] getSmoothImage(short[][] image) {
		short[][] src = new short[height][width];
		
		final float[][] filter = {
				{0.01258f ,0.02516f, 0.03145f, 0.02516f, 0.01258f},
				{0.02516f, 0.05660f, 0.07547f, 0.05660f, 0.02516f},
				{0.03145f, 0.07547f, 0.09434f, 0.07547f, 0.03145f},
				{0.02516f, 0.05660f, 0.07547f, 0.05660f, 0.02516f},
				{0.01258f, 0.02516f, 0.03145f, 0.02516f, 0.01258f}
		};
		
		for (int i = 2 ; i < height - 2; ++i) {
			for (int j = 2 ; j < width - 2; ++j) {
				float ret = 0;
				for (int m = -2 ; m < 3 ; ++m) {
					for (int n = -2 ; n < 3 ; ++n) {
						ret += ((float)image[i+m][j+n] * filter[m+2][n+2]);
					}
				}
				src[i][j] = (short) ret;
			}
		}
		
		return src;
	}
	
	public boolean[][] getEdgeImage() {
		boolean[][] src = new boolean[height][width];
		for(int i = 0 ; i < height ; ++i) {
			for (int j = 0 ; j < width ; ++j) {
				src[i][j] = false;
			}
		}
		
		short[][] grayImage = getGrayScaleArray();
		grayImage = getSmoothImage(grayImage);
		grayImage = getSmoothImage(grayImage);
		Log.d("!!","!! gray: " + grayImage.length + grayImage[0].length);
		
		byte[][] angle = new byte[height][width];
		short[][] arr = new short[height][width];
		
		for (int i = 1 ; i < height - 1 ; ++i) {
			for (int j = 1 ; j < width - 1; ++j) {
				float gr = (grayImage[i-1][j-1] + grayImage[i-1][j] + grayImage[i-1][j+1] - 
						    grayImage[i+1][j-1] - grayImage[i+1][j] - grayImage[i+1][j+1]);
				
				float gc = (grayImage[i-1][j-1] + grayImage[i][j-1] + grayImage[i+1][j-1] - 
							grayImage[i-1][j+1] - grayImage[i][j+1] - grayImage[i+1][j+1]);
				
				arr[i][j] = (short) (gr*gr + gc*gc);
				
				float r = gr / gc;
				if (r < -1.732f) {
					angle[i][j] = 0;
				} else if (r >= -1.732f && r < -0.57735) {
					angle[i][j] = 1;
				} else if (r >= -0.57735 && r < 0.57735) {
					angle[i][j] = 2;
				} else if (r > 0.57735 && r < 1.732) {
					angle[i][j] = 3;
				} else {
					angle[i][j] = 0;
				}
				
			}
		}
		
		short[][] out = new short[height][width];
		for (int i = 1 ; i < height-1 ; ++i) {
			for (int j = 0 ; j < width-1 ; ++j) {
				switch (angle[i][j]) {
				case 2:
					out[i][j] = isMax(arr[i][j], arr[i][j-1], arr[i][j+1]);
					break;
				case 3:
					out[i][j] = isMax(arr[i][j], arr[i+1][j+1], arr[i-1][j-1]);
					break;
				case 0:
					out[i][j] = isMax(arr[i][j], arr[i-1][j], arr[i+1][j]);
					break;
				default:
					out[i][j] = isMax(arr[i][j], arr[i+1][j-1], arr[i-1][j+1]);
					break;
				}
			}
		}
		
		arr = null;
		angle = null;
		
		for (int i = 0 ; i < height ; ++i) {
			for (int j = 0 ; j < width ; ++j) {
				if (out[i][j] < 100) {
					if (out[i][j] < 90) {
						out[i][j] = 255;
					} else {
						out[i][j] = 128;
					}
				} else {
					out[i][j] = 0;
				}
			}
		}
		
		for (int i = 1 ; i < height - 1 ; ++i) {
			for (int j = 1 ; j < width - 1 ; ++j) {
				if (out[i][j] == 255) {
					for (int m = 0 ; m < 3 ; ++m) {
						for (int n = 0 ; n < 3 ; ++n) {
							if (out[i-1+m][j-1+n] == 128)
								out[i-1+m][j-1+n] = 0;
						}
					}
				} else if (out[i][j] == 128) {
					for (int m = 0 ; m < 3 ; ++m) {
						for (int n = 0 ; n < 3 ; ++n) {
							if (out[i-1+m][j-1+n] == 0)
								out[i][j] = 0;
						}
						if (out[i][j] == 128)
							out[i][j] = 255;
					}
				}
			}
		}
		
		for (int i = 0 ; i < height ; ++i) {
			for (int j = 0 ; j < width ; ++j) {
				src[i][j] = (out[i][j] == 255);
			}
		}
		
		return src;
	}
	
	private short isMax(short a, short b, short c) {
		if ( a > b && a > c)
			return a;
		else 
			return 0;
	}
}
