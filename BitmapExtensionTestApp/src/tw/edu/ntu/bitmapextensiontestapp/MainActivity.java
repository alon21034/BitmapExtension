package tw.edu.ntu.bitmapextensiontestapp;

import tw.edu.ntu.bitmapextenstion.ExtensionBitmap;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

public class MainActivity extends Activity {

	private ImageView mImageView;
	private ExtensionBitmap mExBitmap;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mImageView = (ImageView) findViewById(R.id.main_imageview);
		mImageView.setImageResource(R.drawable.flower);
		
		Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.flower);
		mExBitmap = new ExtensionBitmap(bm);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.main_menu_origin:
			
			break;
		case R.id.main_menu_gray:
			Bitmap grayImage = mExBitmap.getGrayScaledBitmap();
			mImageView.setImageBitmap(grayImage);
			break;
		case R.id.main_menu_balck_white:
			Bitmap bwImage = mExBitmap.getQuantizedBitmap(2);
			mImageView.setImageBitmap(bwImage);
			break;
		default:
			break;
		}
		
		return super.onOptionsItemSelected(item);
	}

}
