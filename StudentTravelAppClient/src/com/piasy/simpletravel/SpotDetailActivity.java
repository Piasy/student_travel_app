package com.piasy.simpletravel;

import java.math.BigDecimal;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.piasy.simpletravel.controller.Controller;
import com.piasy.simpletravel.model.Constant;

public class SpotDetailActivity extends Activity
{
	Button addToPlanButton;
	Button travelInfoButton, planTableButton, settingButton;	
	TextView titleTextView;
	ImageView spotImage;
	TextView cityNameTextView, introTextView, addressTextView, priceTextView;
	TextView favorTextView, opentimeTextView, suggtimeTextView;
	TextView besttimeTextView, activityTextView, foodTextView, shoppingTextView;
	TextView cultrueTextView, tipTextView, trafficTextView, phoneTextView;
	TextView commitScore, commitContent;
	LinearLayout cityNameLayout, introLayout, addressLayout, ticketLayout, opentimeLayout;
	LinearLayout openSuggLayout, besttimeLayout, activityLayout, foodLayout, commitLayout;
	LinearLayout shoppingLayout, cultrueLayout, tipLayout, trafficLayout, phoneLayout;
	Button evalOp;
	PopMenu popMenu;
	int evalOpType = 2;
	
	Controller myController;
	JSONObject spot;
	final String[] ops = {"去过", "想去", "评价"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_spot_detail);
		
		addToPlanButton = (Button) findViewById(R.id.addToPlanButton);
		evalOp = (Button) findViewById(R.id.evalOperation);
		travelInfoButton = (Button) findViewById(R.id.travelInfoInSpotDetail);
		planTableButton = (Button) findViewById(R.id.planTableInSpotDetail);
		settingButton = (Button) findViewById(R.id.SettingInSpotDetail);
		spotImage = (ImageView) findViewById(R.id.spotImageInSpotDetail);
		titleTextView = (TextView) findViewById(R.id.titleInSpotDetail);
		cityNameTextView = (TextView) findViewById(R.id.cityNameInSpotDetail);
		introTextView = (TextView) findViewById(R.id.introInSpotDetail);
		addressTextView = (TextView) findViewById(R.id.addressInSpotDetail);
		priceTextView = (TextView) findViewById(R.id.priceInSpotDetail);
		favorTextView = (TextView) findViewById(R.id.favorInSpotDetail);
		opentimeTextView = (TextView) findViewById(R.id.opentimeInSpotDetail);
		suggtimeTextView = (TextView) findViewById(R.id.suggesttimeInSpotDetail);
		besttimeTextView = (TextView) findViewById(R.id.besttimeInSpotDetail);
		activityTextView = (TextView) findViewById(R.id.activityInSpotDetail);
		foodTextView = (TextView) findViewById(R.id.foodInSpotDetail);
		shoppingTextView = (TextView) findViewById(R.id.shoppingInSpotDetail);
		cultrueTextView = (TextView) findViewById(R.id.cultureInSpotDetail);
		tipTextView = (TextView) findViewById(R.id.tipInSpotDetail);
		trafficTextView = (TextView) findViewById(R.id.trafficInSpotDetail);
		phoneTextView = (TextView) findViewById(R.id.phoneInSpotDetail);
		commitScore = (TextView) findViewById(R.id.commitScore);
		commitContent = (TextView) findViewById(R.id.commitContent);
		cityNameLayout = (LinearLayout) findViewById(R.id.citynameLayoutInSpotDetail);
		introLayout = (LinearLayout) findViewById(R.id.introLayoutInSpotDetail);
		addressLayout = (LinearLayout) findViewById(R.id.addressLayoutInSpotDetail);
		ticketLayout = (LinearLayout) findViewById(R.id.ticketLayoutInSpotDetail);
		openSuggLayout = (LinearLayout) findViewById(R.id.openSuggLayoutInSpotDetail);
		besttimeLayout = (LinearLayout) findViewById(R.id.besttimeLayoutInSpotDetail);
		activityLayout = (LinearLayout) findViewById(R.id.activityLayoutInSpotDetail);
		foodLayout = (LinearLayout) findViewById(R.id.foodLayoutInSpotDetail);
		commitLayout = (LinearLayout) findViewById(R.id.commitLayoutInSpotDetail);
		shoppingLayout = (LinearLayout) findViewById(R.id.shoppingLayoutInSpotDetail);
		cultrueLayout = (LinearLayout) findViewById(R.id.cultrueLayoutInSpotDetail);
		tipLayout = (LinearLayout) findViewById(R.id.tipLayoutInSpotDetail);
		trafficLayout = (LinearLayout) findViewById(R.id.trafficLayoutInSpotDetail);
		phoneLayout = (LinearLayout) findViewById(R.id.phoneLayoutInSpotDetail);
		opentimeLayout = (LinearLayout) findViewById(R.id.opentimeLayoutInSpotDetail);
		
		myController = Controller.getController();
		myController.setActivityHandler(handler);
		spot = myController.getSpotDetail();
		
		popMenu = new PopMenu(this);
		popMenu.addItems(ops);
		popMenu.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id)
			{
				evalOpType = position;
				popMenu.dismiss();
				
				switch (position)
				{
				case 0:
					Controller.makeToast(ops[position]);
					break;
				case 1:
					Controller.makeToast(ops[position]);
					break;
				case 2:
					View dialogView = LayoutInflater.from(SpotDetailActivity.this).inflate(
								R.layout.eval_dialog, null);
					final TextView ratingVal = (TextView) dialogView.findViewById(R.id.ratingVal);
					final RatingBar score = (RatingBar) dialogView.findViewById(R.id.spotEvalScore);
					score.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener()
					{
						
						@Override
						public void onRatingChanged(RatingBar ratingBar, float rating,
								boolean fromUser)
						{
							final int numStars = ratingBar.getNumStars();

							if(score.getNumStars() != numStars)
							{
								score.setNumStars(numStars);
							}
							if(score.getRating() != rating)
							{
								score.setRating(rating);
							}
							final float ratingBarStepSize = ratingBar.getStepSize();
							if(score.getStepSize() != ratingBarStepSize)
							{
								score.setStepSize(ratingBarStepSize);
							}
							
							float f1 = new BigDecimal(rating)
								.setScale(1, BigDecimal.ROUND_HALF_UP)
								.floatValue();
							ratingVal.setText("" + f1 + "/5");
							
							System.out.println("score " + f1);
						}
					});
					final EditText comment = (EditText) dialogView.findViewById(R.id.spotEvalComment);
					AlertDialog.Builder evalDialog = new AlertDialog.Builder(SpotDetailActivity.this);
					evalDialog.setView(dialogView);
					evalDialog.setTitle("评价景点");
					evalDialog.setPositiveButton("确定", new DialogInterface.OnClickListener()
					{
						
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							float f1 = new BigDecimal(score.getRating())
								.setScale(1, BigDecimal.ROUND_HALF_UP)
								.floatValue();
							try
							{
								myController.commit(spot.getString("name"), spot.getString("city"), 
										f1, comment.getText().toString());
							}
							catch (JSONException e)
							{
								e.printStackTrace();
							}
							dialog.cancel();
						}
					});
					evalDialog.setNegativeButton("取消", new DialogInterface.OnClickListener()
					{
						
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							dialog.cancel();
						}
					});
					evalDialog.create().show();
					break;
				default:
					break;
				}
			}
		});
		evalOp.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				popMenu.showAsDropDown(v);
			}
		});
		
		try
		{
			Bitmap bitmap = BitmapFactory.decodeFile(spot.getString("photo"));
			spotImage.setImageBitmap(bitmap);
			
			titleTextView.setText(spot.getString("name"));
			cityNameTextView.setText(spot.getString("city"));
			
			if (spot.getString("intro").equals("暂无"))
			{
				introLayout.setVisibility(View.GONE);
			}
			else
			{
				String introString = spot.getString("intro");
				if (introString.length() <= Constant.SPOT_INTRO_LEN)
				{
					introString = "      " + introString;
				}
				else
				{
					introString = "      " + introString.substring(0, Constant.SPOT_INTRO_LEN) + "...";
					setOnclickListener(introTextView, spot.getString("intro"));
				}
				introTextView.setText(introString);
			}
			
			if (spot.getString("address").equals("暂无"))
			{
				addressLayout.setVisibility(View.GONE);
			}
			else
			{
				addressTextView.setText("地址：" + spot.getString("address"));
			}
			
			JSONObject ticket = spot.getJSONObject("ticket");
			if (ticket.getString("price").equals("暂无"))
			{
				ticketLayout.setVisibility(View.GONE);
			}
			else
			{
				priceTextView.setText("门票：" + ticket.getString("price"));
				if (ticket.getString("favor").equals("none"))
				{
					favorTextView.setVisibility(View.GONE);
				}
				else
				{
					String str = ticket.getString("favor");
					if (str.length() <= Constant.SPOT_INTRO_LEN)
					{
						str = "优惠信息：" + str;
					}
					else
					{
						str = "优惠信息：" + str.substring(0, Constant.SPOT_INTRO_LEN) + "...";
						setOnclickListener(favorTextView, ticket.getString("favor"));
					}
					favorTextView.setText(str);
				}
			}
			
			if (spot.getString("opentime").equals("暂无"))
			{
				if (spot.getString("suggesttime").equals("暂无"))
				{
					openSuggLayout.setVisibility(View.GONE);
				}
				else
				{
					opentimeLayout.setVisibility(View.GONE);
					suggtimeTextView.setText(spot.getString("suggesttime"));
				}
			}
			else
			{
				opentimeTextView.setText("开放时间：" + spot.getString("opentime"));
				if (spot.getString("suggesttime").equals("暂无"))
				{
					suggtimeTextView.setVisibility(View.GONE);
				}
				else
				{
					suggtimeTextView.setText(spot.getString("suggesttime"));
				}
			}
			
			JSONObject tip = spot.getJSONObject("tip");
			if (tip.getString("besttime").equals("none"))
			{
				besttimeLayout.setVisibility(View.GONE);
			}
			else
			{
				String str = tip.getString("besttime");
				if (str.length() <= Constant.SPOT_INTRO_LEN)
				{
					str = "最佳旅游季节：" + str;
				}
				else
				{
					str = "最佳旅游季节：" + str.substring(0, Constant.SPOT_INTRO_LEN) + "...";
					setOnclickListener(besttimeTextView, tip.getString("besttime"));
				}
				besttimeTextView.setText(str);
			}
			
			if (tip.getString("activity").equals("none"))
			{
				activityLayout.setVisibility(View.GONE);
			}
			else
			{
				String str = tip.getString("activity");
				if (str.length() <= Constant.SPOT_INTRO_LEN)
				{
					str = "活动：" + str;
				}
				else
				{
					str = "活动：" + str.substring(0, Constant.SPOT_INTRO_LEN) + "...";
					setOnclickListener(activityTextView, tip.getString("activity"));
				}
				activityTextView.setText(str);
			}
			
			if (tip.getString("food").equals("none"))
			{
				foodLayout.setVisibility(View.GONE);
			}
			else
			{
				String str = tip.getString("food");
				if (str.length() <= Constant.SPOT_INTRO_LEN)
				{
					str = "美食：" + str;
				}
				else
				{
					str = "美食：" + str.substring(0, Constant.SPOT_INTRO_LEN) + "...";
					setOnclickListener(foodTextView, tip.getString("food"));
				}
				foodTextView.setText(str);
			}
			
			if (tip.getString("shopping").equals("none"))
			{
				shoppingLayout.setVisibility(View.GONE);
			}
			else
			{
				String str = tip.getString("shopping");
				if (str.length() <= Constant.SPOT_INTRO_LEN)
				{
					str = "购物：" + str;
				}
				else
				{
					str = "购物：" + str.substring(0, Constant.SPOT_INTRO_LEN) + "...";
					setOnclickListener(shoppingTextView, tip.getString("shopping"));
				}
				shoppingTextView.setText(str);
			}
			
			if (tip.getString("culture").equals("none"))
			{
				cultrueLayout.setVisibility(View.GONE);
			}
			else
			{
				String str = tip.getString("culture");
				if (str.length() <= Constant.SPOT_INTRO_LEN)
				{
					str = "文化地理：" + str;
				}
				else
				{
					str = "文化地理：" + str.substring(0, Constant.SPOT_INTRO_LEN) + "...";
					setOnclickListener(cultrueTextView, tip.getString("culture"));
				}
				cultrueTextView.setText(str);
			}
			
			if (tip.getString("tip").equals("none"))
			{
				tipLayout.setVisibility(View.GONE);
			}
			else
			{
				String str = tip.getString("tip");
				if (str.length() <= Constant.SPOT_INTRO_LEN)
				{
					str = "小贴士：" + str;
				}
				else
				{
					str = "小贴士：" + str.substring(0, Constant.SPOT_INTRO_LEN) + "...";
					setOnclickListener(tipTextView, tip.getString("tip"));
				}
				tipTextView.setText(str);
			}
			
			if (tip.getString("traffic").equals("none"))
			{
				trafficLayout.setVisibility(View.GONE);
			}
			else
			{
				String str = tip.getString("traffic");
				if (str.length() <= Constant.SPOT_INTRO_LEN)
				{
					str = "交通信息：" + str;
				}
				else
				{
					str = "交通信息：" + str.substring(0, Constant.SPOT_INTRO_LEN) + "...";
					setOnclickListener(trafficTextView, tip.getString("traffic"));
				}
				trafficTextView.setText(str);
			}
			
			if (spot.getString("phone").equals("暂无"))
			{
				phoneLayout.setVisibility(View.GONE);
			}
			else
			{
				phoneTextView.setText("电话：" + spot.getString("phone"));
			}
			
			JSONArray eval = spot.getJSONArray("eval");
			if (eval.length() == 0)
			{
				commitContent.setText("评价：暂无");
			}
			else
			{
				commitContent.setText("评价：" + eval.toString());
			}
			
			float score = (float) spot.getDouble("score");
			if (score == 0)
			{
				commitScore.setText("评分：暂无");
			}
			else
			{
				commitScore.setText("评分：" + score);
			}
			
			travelInfoButton.setOnClickListener(new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					Intent travelInfoIntent = new Intent(SpotDetailActivity.this, SearchActivity.class);
					startActivity(travelInfoIntent);
					
//					finish();
				}
			});
			
			planTableButton.setOnClickListener(new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					Intent planTableIntent = new Intent(SpotDetailActivity.this, AllPlanActivity.class);
					startActivity(planTableIntent);
				}
			});
			
			settingButton.setOnClickListener(new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					Intent settingIntent = new Intent(SpotDetailActivity.this, SettingActivity.class);
					startActivity(settingIntent);
				}
			});
			
			addToPlanButton.setOnClickListener(new View.OnClickListener()
			{
				
				@Override
				public void onClick(View v)
				{
					if (myController.getFromDate() == null)
					{
						Calendar cur = Calendar.getInstance();
						DatePickerDialog dpDialog = new DatePickerDialog
						(SpotDetailActivity.this, new DatePickerDialog.OnDateSetListener()
						{
							
							@Override
							public void onDateSet(DatePicker view, int year, int monthOfYear,
									int dayOfMonth)
							{
								myController.setPlanStartDate(year, monthOfYear, dayOfMonth);
								
								if (myController.addSpot(spot))
								{
									Controller.makeToast("添加成功");
								}
								else
								{
									Controller.makeToast("已经在行程中啦");
								}
							}
						}, cur.get(Calendar.YEAR), cur.get(Calendar.MONTH), cur.get(Calendar.DAY_OF_MONTH));
						dpDialog.setTitle("请选择行程开始日期");
						dpDialog.show();
					}
					else
					{
						if (myController.addSpot(spot))
						{
							Controller.makeToast("添加成功");
						}
						else
						{
							Controller.makeToast("已经在行程中啦");
						}
					}
				}
			});
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "SpotDetailActivity onCreate : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "SpotDetailActivity onCreate : " + e.getMessage());
			}
		}
	}
	
	protected void setOnclickListener(TextView obser, final String content)
	{
		obser.setOnClickListener(new View.OnClickListener()
		{
			
			@Override
			public void onClick(View v)
			{
				new AlertDialog.Builder(SpotDetailActivity.this)
					.setTitle("详细内容")
					.setMessage(content)
					.setPositiveButton("确定", null)
					.show();
			}
		});
	}

	@Override
	protected void onResume()
	{
		super.onResume();
		myController.setActivityHandler(handler);
		
		System.out.println("SpotDetailActivity.onResume()");
		
		if (myController.exiting())
		{
			System.out.println("SpotDetailActivity.onResume()");
			finish();
		}
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		public void handleMessage(Message msg)
		{
			Log.d(Constant.LOG_LEVEL_DEBUG, "get info at launch activity : " + (String)msg.obj);
		}
	};
}
