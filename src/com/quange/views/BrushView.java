package com.quange.views;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.quange.jhds.AppCommon;
import com.quange.jhds.AppSetManager;
import com.quange.jhds.DrawActivity;
import com.quange.model.JHDSBrushModel;
import com.quange.model.JHDSBrushModel.JHDSBrushLineModel;
import com.quange.model.JHDSCopyModel;
import com.quange.jhds.*;
import com.umeng.analytics.MobclickAgent;


import android.R.integer;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


public class BrushView extends View {
	private float lastx = 0;
	private float lasty = 0;
	private List<Path> pathList = new ArrayList<Path>();
	private List<Paint> brushList = new ArrayList<Paint>();
	//paths //lines //points
	private ArrayList<JHDSBrushModel> mDrawing = new ArrayList<JHDSBrushModel>();
	private int brushColor = AppSetManager.getBrushColor();
	private int brushwidth = AppSetManager.getBrushWidth();
	private boolean enable;
	public boolean actCanBack = true; 
	private Timer timer;

	public BrushView(Context context) {
		this(context, null);
		enable = true;
		//loadSaveDataAndDraw();
	}
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				
				postInvalidate();
				closeLoading();
				break;
			case 2:
				Toast.makeText(getContext(), "您还没有画过画呦，开始您的创作之旅吧", Toast.LENGTH_SHORT).show();
				closeLoading();
				break;
			}
		}
	};
	
	public void closeLoading()
	{
		DrawActivity at = (DrawActivity) this.getContext();
		at.loadingView.setVisibility(View.GONE);
	}
	public void loadSaveDataAndDraw()
	{
		DrawActivity at = (DrawActivity) this.getContext();
		at.loadingView.setVisibility(View.VISIBLE);
		new Thread() {
			public void run() {
				String str = AppCommon.getInstance().readLineData();
				if(str != "")
				{
					try { 
						str = "{\"a\":"+str+"}";
						JSONObject jsObj = new JSONObject(str);
						JSONArray ja =  jsObj.getJSONArray("a");
						mDrawing.clear();
						pathList.clear();
						brushList.clear();
						for(int i = 0;i<ja.length();i++)
						{
							JSONObject brushModel = ja.getJSONObject(i);
							updateBrushInfor(brushModel.getInt("brushwidth"),brushModel.getInt("brushColor"));
							JHDSBrushModel bm = mDrawing.get(i);
							JSONArray lines =  brushModel.getJSONArray("lines");
							for(int j = 0;j<lines.length();j++)
							{
								JSONObject line = lines.getJSONObject(j);
								JHDSBrushLineModel lm = new JHDSBrushLineModel();
								JSONArray points = line.getJSONArray("points");
								for(int m = 0;m<points.length();m++)
								{
									JSONObject point = points.getJSONObject(m);
									PointF p = new PointF();
									p.x = (float) point.getDouble("x");
									p.y = (float) point.getDouble("y");
									if(m ==0)
									{
										pathList.get(i).moveTo(p.x, p.y);
									}
									else
									{
										pathList.get(i).lineTo(p.x, p.y);
									}
									lm.points.add(p);
								}
								bm.lines.add(lm);
							}
							
							
						}
						System.out.println("");
					 } 
			        catch (Exception e) { 
			            e.printStackTrace(); 
			        } 
				
					Message message=new Message();  
	                message.what=1;  
	                mHandler.sendMessage(message); 
					
					
				}
				else
				{
					Message message=new Message();  
	                message.what=2;  
	                mHandler.sendMessage(message); 
				}
			}
		}.start();
		
		
	}
	
	public int getBrushWidth()
	{
		return brushwidth;
	}
	
	public void backToFront()
	{
		MobclickAgent.onEvent(getContext(), "canvas_last");
		if(pathList.size() != mDrawing.size())
		{
			Toast.makeText(this.getContext(), "程序已凌乱", Toast.LENGTH_SHORT).show();
		}
		if(pathList.size()>0)
		{
			
		 	pathList.get(pathList.size()-1).reset();
		 	if(mDrawing.size()>0)
		 	{
				JHDSBrushModel bm = mDrawing.get(mDrawing.size()-1);
				
				if(bm.lines.size()>0)
				{
					bm.lines.remove(bm.lines.size() -1);
					for(int i = 0;i<bm.lines.size();i++)
					{
						JHDSBrushLineModel line = bm.lines.get(i);
						pathList.get(pathList.size()-1).moveTo(line.points.get(0).x,line.points.get(0).y);
						for(int j =1;j<line.points.size();j++)
						{
							pathList.get(pathList.size()-1).lineTo(line.points.get(j).x,line.points.get(j).y);
						}
					}
					if(bm.lines.size() ==0)
					{
						pathList.remove(pathList.size()-1);
						brushList.remove(brushList.size()-1);
						mDrawing.remove(mDrawing.size()-1);
						if(mDrawing.size()>0)
						{
							DrawActivity at = (DrawActivity) this.getContext();
							JHDSBrushModel bbb = mDrawing.get(mDrawing.size()-1);
							at.updateBrushWidthAndColor(bbb.brushwidth, bbb.brushColor);
							AppSetManager.saveBrushWidth(bbb.brushwidth);
							AppSetManager.saveBrushColor(bbb.brushColor);
							brushColor = bbb.brushColor;
							brushwidth = bbb.brushwidth;
						}
					}
					
					if(mDrawing.size()==0)
						clearAll();
					postInvalidate();
					
					Toast.makeText(this.getContext(), "已经后退至上一次落笔处", Toast.LENGTH_SHORT).show();
				}
				else
				{
					if(pathList.size() == 1)
						Toast.makeText(this.getContext(), "您还没有开始画，现在开始动笔吧", Toast.LENGTH_SHORT).show();
					else
					{
						pathList.remove(pathList.size()-1);
						brushList.remove(brushList.size()-1);
						mDrawing.remove(mDrawing.size()-1);
						if(mDrawing.size()>0)
						{
							DrawActivity at = (DrawActivity) this.getContext();
							JHDSBrushModel bbb = mDrawing.get(mDrawing.size()-1);
							at.updateBrushWidthAndColor(bbb.brushwidth, bbb.brushColor);
							AppSetManager.saveBrushWidth(bbb.brushwidth);
							AppSetManager.saveBrushColor(bbb.brushColor);
							brushColor = bbb.brushColor;
							brushwidth = bbb.brushwidth;
						}
						else
						{
							clearAll();
						}
						Toast.makeText(this.getContext(), "已经后退至上一次落笔处", Toast.LENGTH_SHORT).show();Toast.makeText(this.getContext(), "已经后退至上一次落笔处", Toast.LENGTH_SHORT).show();
					}
				}
				
				
		 	}
		}
		else
		{
			clearAll();
			
		}
		
		
	}
	
	public void setEnable(boolean e)
	{
		this.enable =e;
	}
	
	private synchronized String convertLineToString()
	{
		String str = "";
		/*
		Gson gson = new Gson(); 
		str = gson.toJson(mDrawing,new TypeToken<ArrayList<JHDSBrushModel>>() {
		}.getType());*/
		str = "[";
		for (int i = 0 ;i<mDrawing.size();++i)
		{
			JHDSBrushModel bm = mDrawing.get(i);
			str = str+ "{"+"\"brushColor\":"+bm.brushColor+",\"brushwidth\":"+bm.brushwidth+",\"lines\":[";
			for (int j = 0 ;j<bm.lines.size();++j)
			{
				JHDSBrushLineModel lm = bm.lines.get(j);
				str = str+ "{"+"\"points\":[";
				for(int m = 0;m<lm.points.size();++m)
				{
					PointF p = lm.points.get(m);
					str= str+"{\"x\":"+p.x+",\"y\":"+p.y+"}";
					if(m != lm.points.size()-1)
						str= str+",";
				}
				str = str+"]}";
				if(j != bm.lines.size()-1)
					str= str+",";
			}
					
			str = str+"]}";
			if(i != mDrawing.size()-1)
				str= str+",";
		}
		
		str = str+"]";
		return str;
	}

	public void clearAll(){
		MobclickAgent.onEvent(getContext(), "canvas_clear");
		pathList.clear();
		brushList.clear();
	
		mDrawing.clear();
		updateBrushColor(brushColor);
		// invalidate the view
		postInvalidate();
	}
	
	public void updateBrushWidth(boolean up)
	{
		if(mDrawing.size()>0)
		{
			JHDSBrushModel bm = mDrawing.get(mDrawing.size()-1);
			if(bm.lines.size()==0)
			{
				pathList.remove(pathList.size()-1);
				brushList.remove(brushList.size()-1);
				mDrawing.remove(mDrawing.size()-1);
			}
		}
		brushwidth = brushwidth +(up?1:-1);
		if(brushwidth<1)
		{
			brushwidth =1;
			//return;
		}
		else if(brushwidth>50)
		{
			brushwidth = 50;
			//return;
		}
		Path path = new Path();
		pathList.add(path);
		JHDSBrushModel bm = new JHDSBrushModel();
		bm.brushwidth = brushwidth;
		bm.brushColor = brushColor;
		mDrawing.add(bm);
		Paint brush = new Paint();
		brush.setAntiAlias(true);
		
		brush.setColor(brushColor);
		brush.setStyle(Paint.Style.STROKE);
		brush.setStrokeJoin(Paint.Join.ROUND);
		DisplayMetrics dm = new DisplayMetrics();  
		dm = getContext().getApplicationContext().getResources().getDisplayMetrics();  
		
		brush.setStrokeWidth((float) (brushwidth*dm.density*0.8));
		
		brushList.add(brush);
		
		AppSetManager.saveBrushWidth(brushwidth);
	}
	
	public void updateBrushInfor(int width,int color) 
	{
		if(mDrawing.size()>0)
		{
			JHDSBrushModel bm = mDrawing.get(mDrawing.size()-1);
			if(bm.lines.size()==0)
			{
				pathList.remove(pathList.size()-1);
				brushList.remove(brushList.size()-1);
				mDrawing.remove(mDrawing.size()-1);
			}
		}
		Path path = new Path();
		pathList.add(path);
		JHDSBrushModel bm = new JHDSBrushModel();
		bm.brushwidth = width;
		bm.brushColor = color;
		mDrawing.add(bm);
		
		Paint brush = new Paint();
		brush.setAntiAlias(true);
		
		brushColor = color;
		brush.setColor(color);
		brush.setStyle(Paint.Style.STROKE);
		brush.setStrokeJoin(Paint.Join.ROUND);
		DisplayMetrics dm = new DisplayMetrics();  
		dm = getContext().getApplicationContext().getResources().getDisplayMetrics();  
		
		brush.setStrokeWidth(width*dm.density);
		brushList.add(brush);
		AppSetManager.saveBrushWidth(width);
		AppSetManager.saveBrushColor(color);
	}
	
	public void updateBrushColor(int color)
	{
		if(mDrawing.size()>0)
		{
			JHDSBrushModel bm = mDrawing.get(mDrawing.size()-1);
			if(bm.lines.size()==0)
			{
				pathList.remove(pathList.size()-1);
				brushList.remove(brushList.size()-1);
				mDrawing.remove(mDrawing.size()-1);
			}
		}
		brushColor = color;
		Path path = new Path();
		pathList.add(path);
		JHDSBrushModel bm = new JHDSBrushModel();
		bm.brushwidth = brushwidth;
		bm.brushColor = color;
		mDrawing.add(bm);
		Paint brush = new Paint();
		brush.setAntiAlias(true);
		brush.setColor(brushColor);
		brush.setStyle(Paint.Style.STROKE);
		brush.setStrokeJoin(Paint.Join.ROUND);
		DisplayMetrics dm = new DisplayMetrics();  
		dm = getContext().getApplicationContext().getResources().getDisplayMetrics();  
		
		brush.setStrokeWidth(brushwidth*dm.density);
	
		brushList.add(brush);
		AppSetManager.saveBrushColor(color);
	
	}
	
	public int getBrushColor()
	{
		return brushColor;
	}
	public BrushView(Context context, AttributeSet attrs) {
		super(context, attrs);
		updateBrushColor(brushColor);
		enable = true;
		//loadSaveDataAndDraw();
		
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float pointX = event.getX();
		float pointY = event.getY();
		
		// Checks for the event that occurs
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			if(timer != null)
				timer.cancel();
			if(enable)
			{
				lastx = pointX;
				lasty = pointY;
				JHDSBrushLineModel line = new JHDSBrushLineModel();
				line.points.add(new PointF(pointX,pointY));
				if(mDrawing.size()>0)
					mDrawing.get(mDrawing.size()-1).lines.add(line);
				if(pathList.size()>0)
					pathList.get(pathList.size()-1).moveTo(pointX, pointY);
				actCanBack = false;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			if(enable)
			{
				lastx = pointX;
				lasty = pointY;
				if(mDrawing.size()>0)
				{
					JHDSBrushModel bm  =  mDrawing.get(mDrawing.size()-1);
					if(bm.lines.size()>0)
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX,pointY));
					if(pathList.size()>0)
						pathList.get(pathList.size()-1).lineTo(pointX, pointY);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if(lastx==pointX &&lasty == pointY &&enable)
			{
				if(mDrawing.size()>0)
				{
					JHDSBrushModel bm  =  mDrawing.get(mDrawing.size()-1);
					if(bm.lines.size()>0)
					{
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX,pointY));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX+1, pointY));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX+1, pointY+1));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX, pointY+1));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX, pointY));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX+1, pointY));
					}
					if(pathList.size()>0)
					{
						pathList.get(pathList.size()-1).lineTo(pointX+1, pointY);
						pathList.get(pathList.size()-1).lineTo(pointX+1, pointY+1);
						pathList.get(pathList.size()-1).lineTo(pointX, pointY+1);
						pathList.get(pathList.size()-1).lineTo(pointX, pointY);
						pathList.get(pathList.size()-1).lineTo(pointX+1, pointY);
					}
				}
			}
			
			timer = new Timer();
			timer.schedule(new TimerTask() { // schedule方法(安排,计划)需要接收一个TimerTask对象和一个代表毫秒的int值作为参数
				@Override
				public void run() {
					AppCommon.getInstance().saveLineData(convertLineToString());
				}
			}, 1000);
			MobclickAgent.onEvent(getContext(), "canvas_draw");
			/*
			 new Thread() {
					public void run() {
						AppCommon.getInstance().saveLineData(convertLineToString());
					}
				}.start();
			 */
			
			break;
		case MotionEvent.ACTION_CANCEL:
			if(lastx==pointX &&lasty == pointY &&enable)
			{
				if(mDrawing.size()>0)
				{
					JHDSBrushModel bm  =  mDrawing.get(mDrawing.size()-1);
					if(bm.lines.size()>0)
					{
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX,pointY));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX+1, pointY));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX+1, pointY+1));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX, pointY+1));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX, pointY));
						bm.lines.get(bm.lines.size()-1).points.add(new PointF(pointX+1, pointY));
					}
					if(pathList.size()>0)
					{
						pathList.get(pathList.size()-1).lineTo(pointX+1, pointY);
						pathList.get(pathList.size()-1).lineTo(pointX+1, pointY+1);
						pathList.get(pathList.size()-1).lineTo(pointX, pointY+1);
						pathList.get(pathList.size()-1).lineTo(pointX, pointY);
						pathList.get(pathList.size()-1).lineTo(pointX+1, pointY);
					}
				}
			}
			
			timer = new Timer();
			timer.schedule(new TimerTask() { // schedule方法(安排,计划)需要接收一个TimerTask对象和一个代表毫秒的int值作为参数
				@Override
				public void run() {
					AppCommon.getInstance().saveLineData(convertLineToString());
				}
			}, 1000);
			MobclickAgent.onEvent(getContext(), "canvas_draw");
			break;
			
		default:
			return true;
		}
		// Force a view to draw.
		postInvalidate();
		return true;

	}
	@Override
	protected void onDraw(Canvas canvas) {
		
		canvas.drawColor(Color.WHITE); 
		if(!enable)
			return;
		for(int i = 0;i<pathList.size();i++)
		{
			
			canvas.drawPath(pathList.get(i), brushList.get(i));
		}
	}
	
	
}