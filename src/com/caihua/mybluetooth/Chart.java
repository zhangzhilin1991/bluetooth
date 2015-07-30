package com.caihua.mybluetooth;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.achartengine.ChartFactory;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import android.R.color;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;

public class Chart extends Activity {

	private DBManger dbManger;
	private LinearLayout layout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_chart);
		layout = (LinearLayout) findViewById(R.id.chart);
		// 查询时间和温度
				String sql = "select datetime(timestamp,'localtime'),temperature from temperature ";
				dbManger = new DBManger(this);
				List<Data> datas = dbManger.query(sql, null);
		
		View chartView=ChartFactory.getTimeChartView(this,bulidXyMultipleSeriesDataset(datas)
				,bulidXyMultipleSeriesRenderer(datas),"MM/dd HH:mm");
	
		layout.addView(chartView, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		dbManger.releaseconn();
	}
	
	/**
	 * 
	 * 将温度数据按分钟取平均值??? 或者取每分钟开始的数据？？？
	 * 
	 * @return
	 */
	private List<Data> getChartData(List<Data> datas) {

		ArrayList<Data> temps = new ArrayList<Data>();
		String mTime = null;
		double temperature = 0;
		int count = 0;
		for (int i = 0; i < datas.size(); i++) {
			String time = (datas.get(i).getDate()).substring(11, 15);
			if (mTime == null) {
				mTime = time;
			}
			// 同一分钟内，求温度平均值
			if (mTime == time) {
				count++;

				temperature += Double
						.parseDouble(datas.get(i).getTemperature());

			} else {
				double temp = temperature / count;
				temps.add(new Data(datas.get(i-1).getDate(), temp + ""));
				mTime = time;
				count = 1;
				temperature = Double.parseDouble(datas.get(i).getTemperature());
			}
		}
		return temps;
	}

	/**
	 * 构造显示用渲染图
	 * 
	 * @return
	 */
	private XYMultipleSeriesRenderer bulidXyMultipleSeriesRenderer(List<Data> datas) {

		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date start = null;
		Date end = null;
		try {
			start = dateFormat.parse(datas.get(0).getDate());
			end=dateFormat.parse(datas.get(datas.size()-1).getDate());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// 对点的绘制进行设置
		XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
		// 设置颜色
		xyRenderer.setColor(Color.RED);
		//设置点的样式
		xyRenderer.setPointStyle(PointStyle.POINT);
		// 设置在图中显示值
		xyRenderer.setDisplayChartValues(true);
		//将要绘制的点添加到坐标绘制中
		renderer.addSeriesRenderer(xyRenderer);

		// 重复 1~3的步骤绘制第二个系列点

		renderer.setAxisTitleTextSize(16);// 设置轴标题文字的大小
		renderer.setLabelsColor(Color.BLACK);//设置轴标题颜色
		renderer.setXLabelsColor(Color.BLACK);//设置轴刻度颜色
		renderer.setYLabelsColor(0,Color.BLACK);
		renderer.setChartTitleTextSize(40);// 设置整个图表标题文字的大小
		renderer.setLabelsTextSize(15);// 设置轴刻度文字的大小
		renderer.setLegendTextSize(15);// 设置图例文字大小
		renderer.setPointSize(5f);// 设置点的大小(图上显示的点的大小和图例中点的大小都会被设置)
		renderer.setChartTitle("体温变化统计表");// 设置表名
		renderer.setApplyBackgroundColor(true);// 设置图表背景颜色
		renderer.setMargins(new int[]{70,20,20,20});//图形四周留白，外边距
		renderer.setMarginsColor(Color.WHITE);
		renderer.setXTitle("日期");
		renderer.setYTitle("体温");
		renderer.setXAxisMin(start.getTime());//设置x轴刻度范围
		renderer.setXAxisMax(end.getTime());
		renderer.setYAxisMax(30);//设置y轴刻度范围（20-30）
		renderer.setYAxisMin(20);
		renderer.setGridColor(Color.LTGRAY);//设置grid
		renderer.setShowGrid(true);
		renderer.setAxesColor(Color.BLACK);//设置轴
		renderer.setZoomEnabled(false);
		return renderer;
	}

	/**
	 * 
	 * 构造图表显示数据
	 * 
	 * @return
	 */
	private XYMultipleSeriesDataset bulidXyMultipleSeriesDataset(List<Data> datas) {
		
		// 2,进行显示
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		// 2.1, 构建数据
		// 设置图样名
		// x为da
		TimeSeries timeSeries = new TimeSeries("体温");
		
		// 获得处理过的数据
		//List<Data> temps = getChartData(datas);
		
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		// 填充数据
		for (int i = 0; i < datas.size(); i++) {
			// 从数据库中去除时间和温度赋值给x，y

			double t = Double.parseDouble(datas.get(i).getTemperature());
			Date d = null;
			try {
				d = dateFormat.parse(datas.get(i).getDate());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			timeSeries.add(d, t);
		}

		// 需要绘制的点放进dataset中，string类型不好弄,date
		dataset.addSeries(timeSeries);
		return dataset;
	}

}
