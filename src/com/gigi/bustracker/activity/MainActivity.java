package com.gigi.bustracker.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.htmlparser.util.ParserException;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.gigi.buslocation.bean.BusPosition;
import com.gigi.buslocation.bean.BusStation;
import com.gigi.bustracker.R;
import com.gigi.bustracker.bean.FavStationBean;
import com.gigi.bustracker.ui.ActionButton;
import com.gigi.bustracker.ui.BusLineView;
import com.gigi.bustracker.ui.SlideToDeleteListView;
import com.gigi.bustracker.ui.SlideToDeleteListView.OnItemEventListener;
import com.gigi.bustracker.util.Constants;
import com.gigi.bustracker.util.Util;

public class MainActivity extends BaseActivity implements 
												OnItemEventListener
{
	private long backPressedTime = 0;
	
	private SharedPreferences prefHistory;
	private SharedPreferences prefFav;
	
	private ActionButton btnFav;
	private EditText lineInput;
	private ActionButton btnSearch;
	
	//结果显示
	private ScrollView scrollView;
	private BusLineView busLineView;
	
	//查询历史
	private LinearLayout row0, row1;
	//常用站点
	private SlideToDeleteListView favListView;
	
	//当前选中的站点
	private BusStation stationSelected;
	
	//查询常用站点时的详细信息
	private FavStationBean curFavStation = null;
	
	private Animation slideIn = null;
	private Animation slideOut = null;
	
	private Map<String, FavStationBean> favStations = new HashMap<String, FavStationBean>();
	
	private class BusLocatingThread extends Thread
	{
		private BusStation stationClicked ;
		
		public BusLocatingThread(BusStation stationClicked)
		{
			this.stationClicked = stationClicked;	
			Log.d("stationClicked", stationClicked.toString());
		}

		@Override
		public void run()
		{
			Util busUtil = Util.getInstance(getApplicationContext());
			List<BusPosition> positions = null;
			try
			{
				// 查询公交车位置信息
				positions = busUtil.getBusPosition(stationClicked);
			}
			catch (ParserException e)
			{
				e.printStackTrace();
			}
        	
			Map param = new HashMap();
			param.put("stationClicked", stationClicked);
			param.put("positions", positions);
			Message msg = positionHandler.obtainMessage();
			msg.obj = param;
			positionHandler.sendMessage(msg);
		}
		
	}
	
	private Handler stationClickHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	BusStation stationClicked = (BusStation)msg.obj;
        	
        	onStationSeleted(stationClicked);

        	showProcess();
        	new BusLocatingThread(stationClicked).start();
        }
	};

	private Handler positionHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
        	List<BusPosition> positions = (List<BusPosition>) ((Map)msg.obj).get("positions");
        	BusStation stationClicked = (BusStation) ((Map)msg.obj).get("stationClicked");
        	if(null == positions)
        	{
        		Toast.makeText(MainActivity.this, "无法查询公交车的位置，请检查网络设置", Toast.LENGTH_LONG).show();
        	}
        	else if(positions.isEmpty())
        	{
        		//界面还原
        		busLineView.resetStations();
        		Toast.makeText(MainActivity.this, "尚未发车", Toast.LENGTH_LONG).show();
        	}
        	else 
        	{
        		//界面还原
        		busLineView.resetStations();
        		busLineView.setBusPositions(positions, stationClicked);
			}	
        	
        	hideProcess();
        }
	};

	private Handler scrollHandler = new Handler()
	{
        public void handleMessage(android.os.Message msg) 
        {  
			scrollView.smoothScrollTo(0, msg.what);
        }
	};
	
	private Handler lineInfoHandler = new Handler()
	{
        public void handleMessage(Message msg) 
        {  
        	@SuppressWarnings("unchecked")
			List<BusStation> busStations = (List<BusStation>) msg.obj;
        	
        	hideProcess();
        	lineInput.clearFocus();
        	
        	if(busStations.isEmpty())
        	{
        		Toast.makeText(MainActivity.this, "未查询到本路车", Toast.LENGTH_SHORT).show();
        		lineInput.selectAll();
        		return;
        	}

        	//隐藏输入法
//        	lineIdInput.clearFocus();
//        	InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//        	imm.hideSoftInputFromWindow(lineIdInput.getWindowToken(), 0);

    		prefHistory.edit().putLong(busStations.get(0).getLineId(), System.currentTimeMillis()).commit();
    		
        	busLineView.setStations(busStations);
    		scrollHandler.sendEmptyMessageDelayed(0, 200);
    		
    		if(View.INVISIBLE == scrollView.getVisibility())
    		{
    			scrollView.setVisibility(View.VISIBLE);
    			showSearchResult();
    		}
    		
    		//查询常用车站
    		if(null != curFavStation)
    		{
    			int index = -1;
    			for(int i = 0; i< busLineView.getStations().size(); i++)
    			{
    				BusStation station = busLineView.getStations().get(i);
    				if(station.getStationId().equals(curFavStation.getStationId()) 
    						&& station.getDirection().equals(curFavStation.getDirection()))
    				{
    					index = i;
    				}
    			}
    			
    			busLineView.clickStation(index);
    			
    			scrollHandler.sendEmptyMessageDelayed((BusLineView.getRow(index)-3) * dip2px(BusLineView.STATION_HEIGHT), 200);
    			
    			//需要清空，以免下次查询线路时，会查询站点
    			curFavStation = null;
    		}
    		
    		//更新查询记录
    		queryHis();
        };  
	};

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		prefHistory = getSharedPreferences(Constants.PREF_HISTORY, MODE_PRIVATE);
		prefFav = getSharedPreferences(Constants.PREF_FAVORITE, MODE_PRIVATE);
		
		findViews();
		setListeners();
		
		queryHis();
		queryFav();
		
		slideIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_in);
		slideIn.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
			}
			
			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
			
			@Override
			public void onAnimationEnd(Animation animation)
			{
				scrollView.setVisibility(View.VISIBLE);
			}
		});
		
		slideOut = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_out);
		slideOut.setAnimationListener(new AnimationListener()
		{
			@Override
			public void onAnimationStart(Animation animation)
			{
			}
			
			@Override
			public void onAnimationRepeat(Animation animation)
			{
			}
			
			@Override
			public void onAnimationEnd(Animation animation)
			{
				scrollView.setVisibility(View.INVISIBLE);
			}
		});
		
	}

	private void findViews()
	{
		row0 = (LinearLayout)findViewById(R.id.tableRow0);
		row1 = (LinearLayout)findViewById(R.id.tableRow1);
		
		scrollView = (ScrollView) findViewById(R.id.scrollView);
		
		busLineView = (BusLineView) findViewById(R.id.busLineView);
		busLineView.setStationClickHandler(stationClickHandler);
		
		favListView = (SlideToDeleteListView) findViewById(R.id.listView);
		favListView.helper.setOnItemEventListener(this);
		
		btnFav = (ActionButton) findViewById(R.id.btnFav);
		btnFav.setIcon(R.drawable.ic_action_not_important);
	    
	    lineInput = (EditText) findViewById(R.id.lineInput);
	    btnSearch = (ActionButton) findViewById(R.id.btnSearch);
	    btnSearch.setIcon(R.drawable.ic_action_search);
	}


	private void setListeners()
	{
		btnFav.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				if(null != stationSelected)
				{
					FavStationBean bean = new FavStationBean(stationSelected);
					
					if(!favStations.containsKey(bean.toString()))
					{
						onAddFav(stationSelected);
						btnFav.setIcon(R.drawable.ic_action_important);
					}
					else
					{
						deleteFavStation(bean);
						favStations.remove(bean.toString());
						btnFav.setIcon(R.drawable.ic_action_not_important);
					}
				}
			}
		});
		
		btnSearch.setOnClickListener(new OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				if(!lineInput.getText().toString().equals(""))
				{
					showProcess();
					
					new Thread(new Runnable()
					{
						@Override
						public void run()
						{
							String lineId = lineInput.getText().toString();
							
							List<BusStation> result = Util.getInstance(MainActivity.this.getApplicationContext()).getBusStations(lineId, "1");
							result.addAll(Util.getInstance(MainActivity.this.getApplicationContext()).getBusStations(lineId, "0"));
							
							Message msg = lineInfoHandler.obtainMessage();
							msg.obj = result;
							lineInfoHandler.sendMessageDelayed(msg, 1000);
						}
					}).start();		
				}
			}
		});
	}

	private int getScreenX(Point downPoint)
	{
		return downPoint.x;
	}


	private int getScreenY(Point downPoint)
	{
		return (int) (downPoint.y - scrollView.getScrollY());
	}

	private void onStationSeleted(BusStation station)
	{
		btnFav.setVisibility(View.VISIBLE);
		
		stationSelected = station;

		if(!favStations.containsKey((new FavStationBean(stationSelected)).toString()))
		{
			btnFav.setIcon(R.drawable.ic_action_not_important);
		}
		else
		{
			btnFav.setIcon(R.drawable.ic_action_important);
		}
	}

	private void onAddFav(BusStation station)
	{
		if(prefFav.getAll().size() >= Constants.MAXFAVNUM)
		{
			Toast.makeText(this, "常用站点过多，请删除后再添加", Toast.LENGTH_SHORT).show();
		}
		else if(addToFav(station))
		{
			Toast.makeText(this, "添加成功", Toast.LENGTH_SHORT).show();
			queryFav();
		}
		else 
		{
			Toast.makeText(this, "添加失败", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * 长按某车站并添加到常用车站
	 * @param stationLongClicked2
	 */
	private boolean addToFav(BusStation station)
	{
		String s = new FavStationBean(station).toString();
		
		return prefFav.edit().putString(s, s).commit();
	}


	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		
		lineInfoHandler = null;
	}

	/**
	 * 查询常用车站
	 */
	private void queryFav()
	{
		@SuppressWarnings("unchecked")
		Map<String, String> fav = (Map<String, String>)prefFav.getAll();

		favStations.clear();
		
		if(fav != null && !fav.isEmpty())
		{
			Map<String, FavStationBean> map = new HashMap<String, FavStationBean>();
			
			favListView.helper.removeAllView();
			
			//添加到页面
			for(Map.Entry<String, String> entry : fav.entrySet())
			{
				FavStationBean bean = FavStationBean.fromString(entry.getValue());
				map.put(bean.toString(), bean);
				
				LinearLayout layout = getFavBlock(bean);
				favListView.helper.addDeletableView(layout);
			}

//			findViewById(R.id.textView4).setVisibility(View.GONE);
			
			favStations = map;
		}
		else 
		{
//			findViewById(R.id.textView4).setVisibility(View.VISIBLE);
		}
		
	}

	private void queryHis()
	{
		@SuppressWarnings("unchecked")
		Map<String, Long> his = (Map<String, Long>)prefHistory.getAll();
		
		if(his != null && !his.isEmpty())
		{
			//按时间排序
			ArrayList<Map.Entry<String, Long>> mappingList = new ArrayList<Map.Entry<String, Long>>(his.entrySet()); 
			Collections.sort(mappingList, new Comparator<Map.Entry<String, Long>>()
			{ 
				public int compare(Map.Entry<String, Long> mapping1,Map.Entry<String, Long> mapping2)
				{ 
					return mapping2.getValue().compareTo(mapping1.getValue()); 
				} 
			});
			
			row0.removeAllViews();
			row1.removeAllViews();

			row0.setGravity(Gravity.LEFT);
			
			Editor edit = prefHistory.edit();
			edit.clear();
			
			//添加到页面
			for(int i=0; i<mappingList.size(); i++)
			{
				Map.Entry<String, Long> entry = mappingList.get(i);
				
				edit.putLong(entry.getKey(), entry.getValue());

				TextView layout = getHisBlock(entry.getKey(), row0.getWidth());
				switch (i/4)
				{
				case 0:
					row0.addView(layout);
					break;
				case 1:
					row1.addView(layout);
					break;

				default:
					break;
				}
			}
			
			edit.commit();
			
			//有可能一行不满四个，需要添加占位view
			while(row0.getChildCount() < 4)
			{
				View v = new TextView(getApplicationContext());
				LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				p.weight = 1;
				v.setLayoutParams(p);
				row0.addView(v);
			}

			while(row1.getChildCount() < 4)
			{
				View v = new TextView(getApplicationContext());
				LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, 1);
				p.weight = 1;
				v.setLayoutParams(p);
				row1.addView(v);
			}
		}
		else 
		{
			TextView empty = new TextView(this);
			
			empty.setText("无记录");
			empty.setTextColor(Color.parseColor("#B0B0B0"));
			row0.setGravity(Gravity.CENTER);
			row0.addView(empty);
		}
	}


	private TextView getHisBlock(final String text, int parentWidth)
	{
		TextView result = (TextView) this.getLayoutInflater().inflate(R.layout.block, null);
		result.setText(text + "路");
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, dip2px(35));
		params.setMargins(5, 5, 5, 5);
		params.weight = 1;
		result.setLayoutParams(params);
		result.setGravity(Gravity.CENTER);
		
		result.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				lineInput.setText(text);
				btnSearch.performClick();
			}
		});
		
		result.setTextColor(getResources().getColor(R.color.textColor));
		
		return result;
	}

	/**
	 * 删除常用车站
	 */
	@Override
	public void onItemDelete(View item)
	{
		FavStationBean bean = (FavStationBean)item.getTag();
		
		deleteFavStation(bean);
	}

	private void deleteFavStation(FavStationBean bean)
	{
		if(prefFav.edit().remove(bean.toString()).commit())
		{
			Toast.makeText(getApplicationContext(), "删除成功", Toast.LENGTH_SHORT).show();
		}
		else 
		{
			Toast.makeText(getApplicationContext(), "删除失败", Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * 选中常用车站
	 */
	@Override
	public void onItemSelectd(View item)
	{
		FavStationBean bean = (FavStationBean)item.getTag();
		
		lineInput.setText(bean.getLineId());
		btnSearch.performClick();
		
		curFavStation = bean;
	}
	
	private LinearLayout getFavBlock(FavStationBean station)
	{
		LinearLayout result = new LinearLayout(this);
		result.setTag(station);
		result.setOrientation(LinearLayout.HORIZONTAL);
//		result.setBackgroundResource(R.drawable.hisviewblock);
		
		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		result.setLayoutParams(p);
		
		TextView textView = (TextView) this.getLayoutInflater().inflate(R.layout.block, null);

		textView.setText(station.getLineId() + "路-" + station.getStationName() 
				+ "站-" + ("0".equals(station.getDirection()) ? "上行" : "下行") );
		textView.setTextSize(16);

		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		params.setMargins(30, 0, 30, 0);
		textView.setLayoutParams(params);
		textView.setBackgroundColor(Color.TRANSPARENT);
		textView.setTextColor(getResources().getColor(R.color.textColor));
		
		result.addView(textView);
		
		return result;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(View.VISIBLE == scrollView.getVisibility())
			{
				scrollView.scrollTo(0, scrollView.getScrollY());
    			scrollView.setVisibility(View.INVISIBLE);
    			
				hideSearchResult();
			}
			else if(0 == backPressedTime)
			{
				backPressedTime = System.currentTimeMillis();
				Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
			}
			else
			{
				if(System.currentTimeMillis() - backPressedTime < 3000)
				{
					finish();
				}
				else
				{
					backPressedTime = System.currentTimeMillis();
					Toast.makeText(getApplicationContext(), "再按一次退出", Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showSearchResult()
	{
		scrollView.startAnimation(slideIn);
	}

	private void hideSearchResult()
	{
		scrollView.startAnimation(slideOut);
		
		btnFav.setVisibility(View.INVISIBLE);
	}

	
}
