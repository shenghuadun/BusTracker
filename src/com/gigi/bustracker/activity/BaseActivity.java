package com.gigi.bustracker.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.gigi.av.GigiLayout;
import com.gigi.bustracker.R;
import com.gigi.bustracker.util.Util;

public class BaseActivity extends Activity
{
//	private MyDialogFragment fragment = new MyDialogFragment();

	Dialog dialog;
	
	private Handler adHandler = new Handler()
	{
        public void handleMessage(Message msg) 
        {  
        	//广告设置
    		final GigiLayout adsMogoView = (GigiLayout) findViewById(R.id.adsMogoView);
    		if(null != adsMogoView)
    		{
    			adsMogoView.setVisibility(View.INVISIBLE);
    			adsMogoView.postDelayed(new Runnable()
    			{
    				
    				@Override
    				public void run()
    				{
    					adsMogoView.setVisibility(View.VISIBLE);				
    				}
    			}, 4000);
    		}
        }
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
//		adHandler.sendEmptyMessageDelayed(0, 1000);
	}

	public void showProcess()
	{
//		fragment.show(getFragmentManager(), "");
		if(null == dialog)
		{
			dialog = new Dialog(this, R.style.dialog);
			
			View view = this.getLayoutInflater().inflate(R.layout.dialog, null);
			
			dialog.setContentView(view);
		}

		ImageView img = (ImageView) dialog.findViewById(R.id.imageView1);
		img.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate));
		dialog.show();
	}

	public void hideProcess()
	{
		timerHandler.sendEmptyMessageDelayed(0, 500);
	}

	private Handler timerHandler = new Handler()
	{
		public void handleMessage(android.os.Message msg)
		{
//			fragment.dismiss();
			if(null != dialog)
			{
				dialog.dismiss();
			}
		}
	};
//
//	public static class MyDialogFragment extends DialogFragment
//	{
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState)
//		{
//			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialog);
//
//			View view = getActivity().getLayoutInflater().inflate(R.layout.dialog, null);
//			ImageView img = (ImageView) view.findViewById(R.id.imageView1);
//			img.startAnimation(AnimationUtils.loadAnimation(getActivity(), R.anim.rotate));
//
//			builder.setView(view);
//			return builder.create();
//		}
//	}

	public int dip2px(float dipValue)
	{
		return Util.dip2px(dipValue, getResources());
	}

	public int px2dip(float pxValue)
	{
		return Util.px2dip(pxValue, getResources());
	}
}
