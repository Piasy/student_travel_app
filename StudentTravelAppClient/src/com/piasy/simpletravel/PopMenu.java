package com.piasy.simpletravel;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopMenu
{
	private ArrayList<String> itemList;
	private Context context;
	private PopupWindow popupWindow;
	private ListView listView;

	public PopMenu(Context context) 
	{
		this.context = context;
		
		itemList = new ArrayList<String>(2);

		View view = LayoutInflater.from(context)
				.inflate(R.layout.popmenu, null);

		listView = (ListView) view.findViewById(R.id.viewTypeListView);
		listView.setAdapter(new PopAdapter());
		listView.setFocusableInTouchMode(true);
		listView.setFocusable(true);

		popupWindow = new PopupWindow(view, 120, LayoutParams.WRAP_CONTENT);

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
	}

	// 设置菜单项点击监听器
	public void setOnItemClickListener(OnItemClickListener listener) 
	{
		listView.setOnItemClickListener(listener);
	}

	// 批量添加菜单项
	public void addItems(String[] items) 
	{
		for (String s : items)
		{
			itemList.add(s);
		}
	}

	// 单个添加菜单项
	public void addItem(String item) 
	{
		itemList.add(item);
	}

	// 下拉式 弹出 pop菜单 parent 右下角
	public void showAsDropDown(View parent)
	{
		popupWindow.showAsDropDown(parent, 0, 5);

		// 使其聚集
		popupWindow.setFocusable(true);
		// 设置允许在外点击消失
		popupWindow.setOutsideTouchable(true);
		// 刷新状态
		popupWindow.update();
	}

	// 隐藏菜单
	public void dismiss() 
	{
		popupWindow.dismiss();
	}

	// 适配器
	private final class PopAdapter extends BaseAdapter 
	{

		@Override
		public int getCount() 
		{
			return itemList.size();
		}

		@Override
		public Object getItem(int position)
		{
			return itemList.get(position);
		}

		@Override
		public long getItemId(int position) 
		{
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ViewHolder holder;
			if (convertView == null) 
			{
				convertView = LayoutInflater.from(context).inflate(
						R.layout.pomenu_item, null);
				holder = new ViewHolder();

				convertView.setTag(holder);

				holder.groupItem = (TextView) convertView
						.findViewById(R.id.popupMenuItem);

			} 
			else 
			{
				holder = (ViewHolder) convertView.getTag();
			}

			holder.groupItem.setText(itemList.get(position));

			return convertView;
		}

		private final class ViewHolder 
		{
			TextView groupItem;
		}
	}
}