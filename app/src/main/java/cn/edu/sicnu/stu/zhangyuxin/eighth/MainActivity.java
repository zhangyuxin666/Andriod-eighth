package cn.edu.sicnu.stu.zhangyuxin.eighth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.edu.sicnu.stu.zhangyuxin.eighth.model.City;

/**
 * 主界面：在后台线程逐行读取 res/raw/citycode.json，用 Gson 解析成分组城市列表并显示；
 * 点击城市跳转到 WeatherActivity 查看该城市天气。
 */
public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private EditText etSearch;
    private ListView listCities;
    private ProgressBar progress;
    private TextView tvStatus;
    private CityAdapter adapter;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etSearch = findViewById(R.id.et_search);
        listCities = findViewById(R.id.list_cities);
        progress = findViewById(R.id.progress);
        tvStatus = findViewById(R.id.tv_status);

        adapter = new CityAdapter(this);
        listCities.setAdapter(adapter);
        listCities.setOnItemClickListener((parent, view, position, id) -> openWeather(adapter.getCity(position)));

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                adapter.filter(s.toString());
            }
        });

        loadCityList();
    }

    /** 多线程读取本地城市列表：读文件 + Gson 解析都放在工作线程，避免阻塞主线程。 */
    private void loadCityList() {
        progress.setVisibility(View.VISIBLE);
        tvStatus.setText(R.string.loading_cities);
        ioExecutor.execute(() -> {
            long start = SystemClock.elapsedRealtime();
            try {
                String json = readRawJson(R.raw.citycode);
                CityRepository repository = CityRepository.parse(json);
                long cost = SystemClock.elapsedRealtime() - start;
                mainHandler.post(() -> onCityListLoaded(repository, cost, json.length()));
            } catch (Exception e) {
                Log.e(TAG, "读取或解析城市列表失败", e);
                mainHandler.post(() -> onCityListFailed(e));
            }
        });
    }

    /** 逐行读取 raw 目录下的 JSON 文本。 */
    private String readRawJson(int rawId) throws IOException {
        StringBuilder builder = new StringBuilder(360 * 1024);
        try (InputStream input = getResources().openRawResource(rawId);
             BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
        }
        return builder.toString();
    }

    private void onCityListLoaded(CityRepository repository, long cost, int byteLength) {
        progress.setVisibility(View.GONE);
        adapter.setGroups(repository.getGroups());
        tvStatus.setText("已解析 " + repository.getCityCount() + " 个城市 · " + repository.getGroupCount()
                + " 个省级分组 · 后台线程耗时 " + cost + " ms / " + (byteLength / 1024) + " KB");
    }

    private void onCityListFailed(Exception e) {
        progress.setVisibility(View.GONE);
        tvStatus.setText("城市列表读取失败：" + e.getMessage());
        Toast.makeText(this, "城市列表读取失败：" + e.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void openWeather(City city) {
        if (city == null) {
            return;
        }
        Intent intent = new Intent(this, WeatherActivity.class);
        intent.putExtra(WeatherActivity.EXTRA_CITY_NAME, city.getCityName());
        intent.putExtra(WeatherActivity.EXTRA_CITY_CODE, city.getCityCode());
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        ioExecutor.shutdownNow();
        super.onDestroy();
    }
}
