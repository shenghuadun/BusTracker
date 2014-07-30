package com.gigi.bustracker.ui;

import com.gigi.bustracker.util.Util;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.Shape;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ActionButton extends LinearLayout
{
	private static final int WIDTH = 80;
	private static final int HEIGHT = 45;
	
	private RelativeLayout container;
	
	//按钮文字
	private TextView textView;
	//按下时最小的遮罩
	private ImageView mask;
	
	private ImageView icon;
	
	//点击时的动画
	private AnimationSet animationShow = new AnimationSet(false);
	private AnimationSet animationHide = new AnimationSet(false);
	
	public ActionButton(Context context)
	{
		super(context);
		init(context, null);
	}
	public ActionButton(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init(context, attrs);
	}
//	public ActionButton(Context context, AttributeSet attrs, int defStyle)
//	{
//		super(context, attrs, defStyle);
//		init(context, attrs);
//	}
	
	private void init(Context context, AttributeSet attrs)
	{
		this.setGravity(Gravity.CENTER);
		this.setLongClickable(true);
		
		container = new RelativeLayout(context);
		LayoutParams p = new LayoutParams(250, 250);
		container.setLayoutParams(p);
		this.addView(container);
		
		container.setGravity(Gravity.CENTER);

		icon = new ImageView(context);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				Util.dip2px(WIDTH, getResources()),
				Util.dip2px(HEIGHT, getResources()));
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		icon.setLayoutParams(params);
		icon.setScaleType(ScaleType.CENTER_INSIDE  );
		container.addView(icon);
		
		mask = new ImageView(context);
		params = new RelativeLayout.LayoutParams(130, 130);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		mask.setLayoutParams(params);
		Shape shap = new ArcShape(0, 360);
		shap.resize(230, 230);
		ShapeDrawable background = new ShapeDrawable(shap);
		background.getPaint().setColor(Color.parseColor("#6023C4FF"));
		mask.setBackgroundDrawable(background);
		mask.setVisibility(View.INVISIBLE);
		container.addView(mask);

		//添加文字
		textView = new TextView(context);
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		textView.setLayoutParams(params);
		textView.setTextColor(Color.WHITE);
		container.addView(textView);
		
		animationShow = new AnimationSet(false);
		ScaleAnimation s = new ScaleAnimation(0.7f, 1, 0.7f, 1, 75, 80);
		animationShow.addAnimation(s);
		AlphaAnimation a = new AlphaAnimation(0.5f, 1);
		animationShow.addAnimation(a);
		animationShow.setDuration(30);
		animationShow.setFillAfter(true);

		animationHide = new AnimationSet(false);
		ScaleAnimation sa = new ScaleAnimation(1, 0.8f, 1, 0.8f, 75, 80);
		animationHide.addAnimation(sa);
		AlphaAnimation aa = new AlphaAnimation(1, 0);
		animationHide.addAnimation(aa);
		animationHide.setDuration(300);
		animationHide.setFillAfter(true);
	}
	
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev)
	{
		switch (ev.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			mask.startAnimation(animationShow);
			break;

		case MotionEvent.ACTION_UP:
			mask.startAnimation(animationHide);
			break;

		default:
			break;
		}
		
		
		return super.dispatchTouchEvent(ev);
	}
	

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		super.onMeasure(MeasureSpec.makeMeasureSpec(Util.dip2px(WIDTH, getResources()), MeasureSpec.EXACTLY), 
				MeasureSpec.makeMeasureSpec(Util.dip2px(HEIGHT, getResources()), MeasureSpec.EXACTLY));
	}
	
	/**
	 * 设置按钮上的文字
	 * @param text
	 */
	public void setText(String text)
	{
		this.textView.setText(text);
	}
	
	/**
	 * 设置按钮文字颜色
	 * @param color
	 */
	public void setTextColor(int color)
	{
		textView.setTextColor(color);
	}
	
	/**
	 * 设置按钮图片
	 * @param resid
	 */
	public void setIcon(int resid)
	{
		icon.setImageResource(resid);
	}
}
