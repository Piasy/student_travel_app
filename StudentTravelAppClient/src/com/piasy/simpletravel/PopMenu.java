package com.piasy.simpletravel;

import java.util.ArrayList;

import com.piasy.simpletravel.model.Constant;

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
	PopAdapter adapter = new PopAdapter();
	int viewType;

	public PopMenu(Context context, int viewType) 
	{
		this.context = context;
		this.viewType = viewType;
		
		itemList = new ArrayList<String>();

		View view = LayoutInflater.from(context)
				.inflate(R.layout.popmenu, null);

		listView = (ListView) view.findViewById(R.id.viewTypeListView);
		listView.setAdapter(adapter);
		listView.setFocusableInTouchMode(true);
		listView.setFocusable(true);

		switch (viewType)
		{
		case Constant.POPUP_VIEW_INMAP:
			popupWindow = new PopupWindow(view, LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
			break;
		case Constant.POPUP_VIEW_INACTIVITY:
			popupWindow = new PopupWindow(view, 120, LayoutParams.WRAP_CONTENT);
		default:
			break;
		}

		// 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景（很神奇的）
		popupWindow.setBackgroundDrawable(new BitmapDrawable());
	}

	// 设置菜单项点击监听器
	public void setOnItemClickListener(OnItemClickListener listener) 
	{
		listView.setOnItemClickListener(listener);
	}

	public void clearItems()
	{
		itemList.clear();
		adapter.notifyDataSetChanged();
	}
	
	// 批量添加菜单项
	public void addItems(String[] items) 
	{
		for (String s : items)
		{
			itemList.add(s);
		}
		adapter.notifyDataSetChanged();
	}

	// 单个添加菜单项
	public void addItem(String item) 
	{
		itemList.add(item);
		adapter.notifyDataSetChanged();
	}

	// 下拉式 弹出 pop菜单 parent 右下角
	public void showAsDropDown(View parent)
	{
		switch (viewType)
		{
		case Constant.POPUP_VIEW_INMAP:
			popupWindow.showAsDropDown(parent, 0, 0);
			break;
		case Constant.POPUP_VIEW_INACTIVITY:
			popupWindow.showAsDropDown(parent, 0, 5);
		default:
			break;
		}

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