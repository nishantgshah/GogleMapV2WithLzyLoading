package com.ngshah.goglemapv2withlazyloading;

import java.util.Hashtable;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ngshah.goglemapv2withlazyloading.R;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

public class MainActivity extends FragmentActivity {

	private GoogleMap googleMap;
	private final LatLng HAMBURG = new LatLng(53.558, 9.927);
	private final LatLng KIEL = new LatLng(53.551, 9.993);
	private Marker marker;
	private Hashtable<String, String> markers;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                .getMap();
        
        initImageLoader();
        markers = new Hashtable<String, String>();
        imageLoader = ImageLoader.getInstance();
        
        options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.ic_launcher)		//	Display Stub Image
			.showImageForEmptyUri(R.drawable.ic_launcher)	//	If Empty image found
			.cacheInMemory()
			.cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
        
        if ( googleMap != null ) {
        	
        	googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        	
        	final Marker hamburg = googleMap.addMarker(new MarkerOptions().position(HAMBURG)
        		        .title("Hamburg"));
        	markers.put(hamburg.getId(), "http://img.india-forums.com/images/100x100/37525-a-still-image-of-akshay-kumar.jpg");
        	
        	final Marker kiel = googleMap.addMarker(new MarkerOptions()
        		        .position(KIEL)
        		        .title("Kiel")
        		        .snippet("Kiel is cool")
        		        .icon(BitmapDescriptorFactory
        		            .fromResource(R.drawable.ic_launcher)));
        	markers.put(kiel.getId(), "http://www.yodot.com/images/jpeg-images-sm.png");
        	  
        	googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));
        	googleMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        }
    }
    
    private class CustomInfoWindowAdapter implements InfoWindowAdapter {

		private View view;

		public CustomInfoWindowAdapter() {
			view = getLayoutInflater().inflate(R.layout.custom_info_window,
					null);
		}

		@Override
		public View getInfoContents(Marker marker) {

			if (MainActivity.this.marker != null
					&& MainActivity.this.marker.isInfoWindowShown()) {
				MainActivity.this.marker.hideInfoWindow();
				MainActivity.this.marker.showInfoWindow();
			}
			return null;
		}

		@Override
		public View getInfoWindow(final Marker marker) {
			MainActivity.this.marker = marker;

			String url = null;

			if (marker.getId() != null && markers != null && markers.size() > 0) {
				if ( markers.get(marker.getId()) != null &&
						markers.get(marker.getId()) != null) {
					url = markers.get(marker.getId());
				}
			}
			final ImageView image = ((ImageView) view.findViewById(R.id.badge));

			if (url != null && !url.equalsIgnoreCase("null")
					&& !url.equalsIgnoreCase("")) {
				imageLoader.displayImage(url, image, options,
						new SimpleImageLoadingListener() {
							@Override
							public void onLoadingComplete(String imageUri,
									View view, Bitmap loadedImage) {
								super.onLoadingComplete(imageUri, view,
										loadedImage);
								getInfoContents(marker);
							}
						});
			} else {
				image.setImageResource(R.drawable.ic_launcher);
			}

			final String title = marker.getTitle();
			final TextView titleUi = ((TextView) view.findViewById(R.id.title));
			if (title != null) {
				titleUi.setText(title);
			} else {
				titleUi.setText("");
			}

			final String snippet = marker.getSnippet();
			final TextView snippetUi = ((TextView) view
					.findViewById(R.id.snippet));
			if (snippet != null) {
				snippetUi.setText(snippet);
			} else {
				snippetUi.setText("");
			}

			return view;
		}
	}

    private void initImageLoader() {
		int memoryCacheSize;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
			int memClass = ((ActivityManager) 
					getSystemService(Context.ACTIVITY_SERVICE))
					.getMemoryClass();
			memoryCacheSize = (memClass / 8) * 1024 * 1024;
		} else {
			memoryCacheSize = 2 * 1024 * 1024;
		}

		final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
				this).threadPoolSize(5)
				.threadPriority(Thread.NORM_PRIORITY - 2)
				.memoryCacheSize(memoryCacheSize)
				.memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize-1000000))
				.denyCacheImageMultipleSizesInMemory()
				.discCacheFileNameGenerator(new Md5FileNameGenerator())
				.tasksProcessingOrder(QueueProcessingType.LIFO).enableLogging() 
				.build();

		ImageLoader.getInstance().init(config);
	}
}