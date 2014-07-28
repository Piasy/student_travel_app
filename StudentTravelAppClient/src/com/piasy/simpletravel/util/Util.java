package com.piasy.simpletravel.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Calendar;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.apache.commons.codec.binary.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import Decoder.BASE64Encoder;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;

import com.baidu.mapapi.search.MKTransitRoutePlan;
import com.piasy.simpletravel.model.Constant;

public class Util
{
	static MessageDigest SHA = null;
	static PublicKey publicKey;
	//initialize the SHA algorithm instance
    
	public static boolean init(InputStream is)
    {
		boolean ret = false;
        try 
        {
        	File cacheDir = new File(Constant.APP_CACHE_DIR);
        	cacheDir.mkdir();
        	
        	SHA = MessageDigest.getInstance("SHA");
        	
			DataInputStream dis = new DataInputStream(is);
			byte[] keyBytes = new byte[162];
			dis.readFully(keyBytes);
			dis.close();
			X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			KeyFactory kf = KeyFactory.getInstance("RSA"); 
			publicKey = kf.generatePublic(spec);   
			ret = true;
        } 
        catch (NoSuchAlgorithmException e) 
        {
        	if (e.getMessage() == null)
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util init : NoSuchAlgorithmException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util init : " + e.getMessage());
			}
        }
        catch (InvalidKeySpecException e)
        {
        	if (e.getMessage() == null)
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util init : InvalidKeySpecException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util init : " + e.getMessage());
			}
		}
		catch (FileNotFoundException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Util init : FileNotFoundException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util init : " + e.getMessage());
			}
		}
		catch (IOException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Util init : IOException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util init : " + e.getMessage());
			}
		}
        return ret;
    }
	
	public static String getSHA1Value(String data)
	{
		SHA.reset();
		SHA.update(data.getBytes());
		String encrypt = new String(Hex.encodeHex(SHA.digest()));
		return encrypt;
	}
	
	public static String RSAEncrypt(String crypt)
	{
		String encryptStr = null;
		try
		{
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
			cipher.init(Cipher.ENCRYPT_MODE, publicKey);
			byte [] encrypt = cipher.doFinal(crypt.getBytes());
			encryptStr = URLEncoder.encode(new BASE64Encoder().encode(encrypt), "UTF-8");
		}
		catch (NoSuchAlgorithmException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : NoSuchAlgorithmException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : " + e.getMessage());
			}
		}
		catch (NoSuchPaddingException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : NoSuchPaddingException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : " + e.getMessage());
			}
		}
		catch (InvalidKeyException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : InvalidKeyException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : " + e.getMessage());
			}
		}
		catch (IllegalBlockSizeException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : IllegalBlockSizeException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : " + e.getMessage());
			}
		}
		catch (BadPaddingException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : BadPaddingException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : " + e.getMessage());
			}
		}
		catch (IOException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : IOException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util RSAEncrypt : " + e.getMessage());
			}
		}
		return encryptStr;
	}
	
	/**
	 * to json object format string
	 * {
	 * "time" : time,	//int
	 * "desc" : desc	//string
	 * }
	 * */
	public static JSONObject routePlan2Json(MKTransitRoutePlan route)
	{
		JSONObject info = new JSONObject();
		
		try
		{
			if (route == null)
			{
				info.put("type", Constant.LISTVIEW_ITEM_TRAFFIC);
				info.put("time", 0);
				info.put("overall", "距离很近，建议直接步行");
				info.put("desc", new JSONArray());
				return info;
			}
			
			ArrayList<JSONObject> desc = new ArrayList<JSONObject>();
			for (int i = 0; i < route.getNumLines(); i ++)
			{
				JSONObject item = new JSONObject();
				item.put("type", "bus");
				item.put("desc", route.getLine(i).getTip());
				desc.add(item);
			}
			
			int offset = 1;
			int index = -1;
			for (int i = 0; i < route.getNumRoute(); i ++)
			{
				JSONObject item = new JSONObject();
				item.put("type", "walk");
				item.put("desc", route.getRoute(i).getTip());
				desc.add(route.getRoute(i).getIndex() + offset, 
						item);
				
				if (route.getRoute(i).getIndex() == index)
				{
					offset ++;
				}
				
				index = route.getRoute(i).getIndex() + 1;
			}
		
			info.put("type", Constant.LISTVIEW_ITEM_TRAFFIC);
			int time = route.getTime();
			info.put("time", time);
			String overall = route.getContent();
			String [] lines = overall.split("_");
			overall = "";
			for (int i = 0; i < lines.length; i ++)
			{
				overall += lines[i];
				if (i < lines.length - 1)
				{
					overall += "→";
				}
			}
			overall += "，约";
			int hour = time / 3600, minute;
			if (0 < hour)
			{
				minute = (time % 3600) / 60;
				overall += hour + "小时";
				if (minute != 0)
				{
					overall += minute + "分钟";
				}
			}
			else
			{
				minute = time / 60;
				overall += minute + "分钟";
			}
			info.put("overall", overall);
			JSONArray descArray = new JSONArray(desc);
			info.put("desc", descArray);
		}
		catch (JSONException e)
		{
			if (e.getMessage() == null)
        	{
				Log.e(Constant.LOG_LEVEL_ERROR, "Util routePlan2String : JSONException");
        	}
        	else
        	{
        		Log.e(Constant.LOG_LEVEL_ERROR, "Util routePlan2String : " + e.getMessage());
			}
		}
		
		return info;
	}
	
	public static int dip2px(Context context, float dipValue) 
	{
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
	}
	
	public static Bitmap getBitmapFromView(View view) 
	{
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache(true);
        return bitmap;
	}
	
	public static JSONObject date2Json(Calendar date)
	{
		JSONObject obj = new JSONObject();
		
		return obj;
	}
}
