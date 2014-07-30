package com.gigi.bustracker.activity;

import cn.domob.android.ads.DomobAdEventListener;
import cn.domob.android.ads.DomobAdManager.ErrorCode;
import cn.domob.android.ads.DomobAdView;

import com.gigi.bustracker.R;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class AdTest extends Activity
{
	RelativeLayout mAdContainer ;
	
	DomobAdView mAdview;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.adtest);
		
		
		
		mAdContainer = (RelativeLayout) findViewById(R.id.duomengContainer);
		// Create ad view
		mAdview = new DomobAdView(this, "56OJyClouMPRrHQyy4", "16TLwbZvAcI9iY7XH5_57Ski",
				DomobAdView.INLINE_SIZE_320X50);
		
//		mAdview.setKeyword("game");
//		mAdview.setUserGender("male");
//		mAdview.setUserBirthdayStr("2000-08-08");
//		mAdview.setUserPostcode("123456");
//		you can set other size, default value is flexible
//		supported sizes:DomobAdView.INLINE_SIZE_320X50、DomobAdView.INLINE_SIZE_300X250、DomobAdView.INLINE_SIZE_600X94、DomobAdView.INLINE_SIZE_600X500、DomobAdView.INLINE_SIZE_728X90、DomobAdView.INLINE_SIZE_FLEXIBLE
//		mAdview.setAdSize(DomobAdView.INLINE_SIZE_320X50);
		mAdview.setAdEventListener(new DomobAdEventListener() {
						
			@Override
			public void onDomobAdReturned(DomobAdView adView) {
				Log.i("DomobSDKDemo", "onDomobAdReturned");				
			}

			@Override
			public void onDomobAdOverlayPresented(DomobAdView adView) {
				Log.i("DomobSDKDemo", "overlayPresented");
			}

			@Override
			public void onDomobAdOverlayDismissed(DomobAdView adView) {
				Log.i("DomobSDKDemo", "Overrided be dismissed");				
			}

			@Override
			public void onDomobAdClicked(DomobAdView arg0) {
				Log.i("DomobSDKDemo", "onDomobAdClicked");				
			}

			@Override
			public void onDomobAdFailed(DomobAdView arg0, ErrorCode arg1) {
				Log.i("DomobSDKDemo", "onDomobAdFailed");				
			}

			@Override
			public void onDomobLeaveApplication(DomobAdView arg0) {
				Log.i("DomobSDKDemo", "onDomobLeaveApplication");				
			}

			@Override
			public Context onDomobAdRequiresCurrentContext() {
				return AdTest.this;
			}

		});
		RelativeLayout.LayoutParams layout=new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		layout.addRule(RelativeLayout.CENTER_HORIZONTAL);
		mAdview.setLayoutParams(layout);
		mAdContainer.addView(mAdview);
	}

	
}
