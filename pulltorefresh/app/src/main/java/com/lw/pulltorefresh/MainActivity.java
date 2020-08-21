package com.lw.pulltorefresh;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.lw.refresh_module.PullRefreshView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MainActivity extends AppCompatActivity {
	
	/**
	 * RecyclerView
	 */
	private RecyclerView mRecyclerView;
	
	/**
	 * 下拉刷新组件
	 */
	private PullRefreshView mPullRefreshView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mPullRefreshView = findViewById(R.id.pull_refresh);
		
		mRecyclerView = mPullRefreshView.getRecyclerView();
		
		
		mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
		mRecyclerView.setAdapter(new MyAdapter());
		
		mPullRefreshView.setOnRefreshListener(new PullRefreshView.OnRefreshListener() {
			@Override
			public void onRefresh() {
				
				/** 模拟网络请求 */
				mPullRefreshView.postDelayed(new Runnable() {
					@Override
					public void run() {
						mPullRefreshView.refreshComplete();
					}
				}, 2000);
			}
		});
	}
	
	/**
	 * 适配器
	 */
	public class MyAdapter extends RecyclerView.Adapter {
		
		@NonNull
		@Override
		public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
				int viewType) {
			
			View view = LayoutInflater.from(MainActivity.this)
			                          .inflate(R.layout.layout_refresh_item, parent, false);
			return new MyVH(view);
		}
		
		@Override
		public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
		
		}
		
		@Override
		public int getItemCount() {
			return 40;
		}
	}
	
	public class MyVH extends RecyclerView.ViewHolder {
		
		/**
		 * tag
		 */
		private static final String TAG = "MyVH";
		
		/**
		 *
		 * @param itemView
		 */
		public MyVH(@NonNull View itemView) {
			super(itemView);
			
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(MainActivity.this, "我被点击了！", Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
}
