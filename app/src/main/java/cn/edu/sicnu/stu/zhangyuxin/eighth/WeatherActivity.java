package cn.edu.sicnu.stu.zhangyuxin.eighth;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.List;

import cn.edu.sicnu.stu.zhangyuxin.eighth.model.Weather;

/**
 * 天气详情界面：用 Volley 请求 t.weather.itboy.net 接口，
 * 再用 Gson 把返回的 JSON 解析成 Weather 对象并渲染到界面。
 */
public class WeatherActivity extends Activity {

    public static final String EXTRA_CITY_NAME = "extra_city_name";
    public static final String EXTRA_CITY_CODE = "extra_city_code";

    private static final String TAG = "WeatherActivity";
    private static final String API = "http://t.weather.itboy.net/api/weather/city/";

    private TextView tvCity;
    private TextView tvUpdate;
    private TextView tvTemp;
    private TextView tvType;
    private TextView tvRange;
    private TextView tvQuality;
    private TextView tvGanmao;
    private TextView tvState;
    private TextView btnRetry;
    private ImageView ivIcon;
    private LinearLayout metricContainer;
    private LinearLayout forecastContainer;
    private LinearLayout stateBox;
    private ProgressBar progress;

    private final Gson gson = new Gson();
    private RequestQueue queue;
    private String cityName;
    private String cityCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        cityName = getIntent().getStringExtra(EXTRA_CITY_NAME);
        cityCode = getIntent().getStringExtra(EXTRA_CITY_CODE);

        tvCity = findViewById(R.id.tv_city);
        tvUpdate = findViewById(R.id.tv_update);
        tvTemp = findViewById(R.id.tv_temp);
        tvType = findViewById(R.id.tv_type);
        tvRange = findViewById(R.id.tv_range);
        tvQuality = findViewById(R.id.tv_quality);
        tvGanmao = findViewById(R.id.tv_ganmao);
        tvState = findViewById(R.id.tv_state);
        btnRetry = findViewById(R.id.btn_retry);
        ivIcon = findViewById(R.id.iv_icon);
        metricContainer = findViewById(R.id.metric_container);
        forecastContainer = findViewById(R.id.forecast_container);
        stateBox = findViewById(R.id.state_box);
        progress = findViewById(R.id.progress);

        tvCity.setText(cityName == null ? "未知城市" : cityName);
        tvUpdate.setText("城市代码 " + cityCode);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        findViewById(R.id.btn_refresh).setOnClickListener(v -> requestWeather());
        btnRetry.setOnClickListener(v -> requestWeather());

        queue = Volley.newRequestQueue(this);
        requestWeather();
    }

    /** Volley 发起 GET 请求，成功/失败都回到主线程执行。 */
    private void requestWeather() {
        if (cityCode == null || cityCode.isEmpty()) {
            showState("没有拿到城市代码，无法查询天气", true, false);
            return;
        }
        showState(getString(R.string.loading_weather), false, true);
        String url = API + cityCode;
        StringRequest request = new StringRequest(Request.Method.GET, url,
                this::onWeatherResponse,
                this::onWeatherError);
        queue.add(request);
    }

    private void onWeatherResponse(String body) {
        Weather weather;
        try {
            weather = gson.fromJson(body, Weather.class);
        } catch (Exception e) {
            Log.e(TAG, "解析天气 JSON 失败", e);
            showState("解析天气 JSON 失败：" + e.getMessage(), true, false);
            return;
        }
        if (weather == null || weather.getData() == null) {
            showState("接口没有返回天气数据，请稍后重试", true, false);
            return;
        }
        if (!weather.isOk()) {
            showState("接口返回异常：" + weather.getMessage(), true, false);
            return;
        }
        hideState();
        render(weather);
    }

    private void onWeatherError(VolleyError error) {
        Log.e(TAG, "天气接口请求失败", error);
        String detail = error.getMessage() == null ? error.getClass().getSimpleName() : error.getMessage();
        showState("网络请求失败：" + detail + "\n请检查网络连接后重试", true, false);
    }

    private void render(Weather weather) {
        Weather.Data data = weather.getData();
        List<Weather.Forecast> forecast = data.getForecast();
        Weather.Forecast today = forecast == null || forecast.isEmpty() ? null : forecast.get(0);

        String updateTime = weather.getCityInfo() == null ? null : weather.getCityInfo().getUpdateTime();
        tvUpdate.setText(updateTime == null ? "更新时间 --" : "更新时间 " + updateTime);

        tvTemp.setText(formatTemp(data.getWendu()) + "℃");
        if (today != null) {
            tvType.setText(orDash(today.getType()));
            tvRange.setText("高温 " + degree(today.getHigh()) + " / 低温 " + degree(today.getLow()));
            ivIcon.setImageResource(iconOf(today.getType()));
        } else {
            tvType.setText("--");
            tvRange.setText("");
            ivIcon.setImageResource(R.drawable.cloud);
        }

        String quality = data.getQuality();
        tvQuality.setText("空气质量 " + orDash(quality) + " · PM2.5 " + formatValue(data.getPm25()));
        tvQuality.setTextColor(qualityColor(quality));
        tvGanmao.setText(data.getGanmao() == null ? "" : data.getGanmao());

        buildMetrics(data, today);
        buildForecast(forecast);
    }

    /** 湿度、风向、风力、PM2.5、PM10、日出日落六项指标，两列排布。 */
    private void buildMetrics(Weather.Data data, Weather.Forecast today) {
        metricContainer.removeAllViews();
        String[][] items = {
                {"湿度", orDash(data.getShidu())},
                {"风向", today == null ? "--" : orDash(today.getFx())},
                {"风力", today == null ? "--" : orDash(today.getFl())},
                {"PM2.5", formatValue(data.getPm25())},
                {"PM10", formatValue(data.getPm10())},
                {"日出 / 日落", today == null ? "--"
                        : orDash(today.getSunrise()) + " / " + orDash(today.getSunset())}
        };
        for (int row = 0; row < items.length; row += 2) {
            if (row > 0) {
                metricContainer.addView(divider(16));
            }
            LinearLayout line = new LinearLayout(this);
            line.setOrientation(LinearLayout.HORIZONTAL);
            line.addView(metricCell(items[row][0], items[row][1]));
            if (row + 1 < items.length) {
                line.addView(metricCell(items[row + 1][0], items[row + 1][1]));
            }
            metricContainer.addView(line);
        }
    }

    private View metricCell(String label, String value) {
        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setPadding(dp(16), dp(10), dp(16), dp(10));
        cell.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        TextView labelView = new TextView(this);
        labelView.setText(label);
        labelView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        labelView.setTextColor(getColor(R.color.text_secondary));
        cell.addView(labelView);

        TextView valueView = new TextView(this);
        valueView.setText(value);
        valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f);
        valueView.setTextColor(getColor(R.color.text_primary));
        valueView.setTypeface(Typeface.DEFAULT_BOLD);
        valueView.setPadding(0, dp(3), 0, 0);
        cell.addView(valueView);
        return cell;
    }

    /** 未来 7 天预报：一行一天，第一行标成“今天”。 */
    private void buildForecast(List<Weather.Forecast> forecast) {
        forecastContainer.removeAllViews();
        if (forecast == null || forecast.isEmpty()) {
            return;
        }
        int count = Math.min(forecast.size(), 7);
        for (int i = 0; i < count; i++) {
            if (i > 0) {
                forecastContainer.addView(divider(0));
            }
            forecastContainer.addView(forecastRow(forecast.get(i), i == 0));
        }
    }

    private View forecastRow(Weather.Forecast item, boolean isToday) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));

        LinearLayout dateBox = new LinearLayout(this);
        dateBox.setOrientation(LinearLayout.VERTICAL);
        dateBox.setLayoutParams(new LinearLayout.LayoutParams(dp(86), LinearLayout.LayoutParams.WRAP_CONTENT));

        TextView week = new TextView(this);
        week.setText(isToday ? "今天" : orDash(item.getWeek()));
        week.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        week.setTextColor(getColor(R.color.text_primary));
        if (isToday) {
            week.setTypeface(Typeface.DEFAULT_BOLD);
        }
        dateBox.addView(week);

        TextView ymd = new TextView(this);
        ymd.setText(shortDate(item.getYmd()));
        ymd.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        ymd.setTextColor(getColor(R.color.text_secondary));
        dateBox.addView(ymd);
        row.addView(dateBox);

        ImageView icon = new ImageView(this);
        icon.setImageResource(iconOf(item.getType()));
        icon.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(34)));
        row.addView(icon);

        TextView type = new TextView(this);
        LinearLayout.LayoutParams typeParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        typeParams.leftMargin = dp(10);
        type.setLayoutParams(typeParams);
        type.setText(orDash(item.getType()));
        type.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12.5f);
        type.setTextColor(getColor(R.color.text_primary));
        row.addView(type);

        TextView range = new TextView(this);
        range.setText(degree(item.getHigh()) + " / " + degree(item.getLow()));
        range.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11.5f);
        range.setTextColor(getColor(R.color.text_secondary));
        row.addView(range);
        return row;
    }

    private void showState(String message, boolean showRetry, boolean loading) {
        stateBox.setVisibility(View.VISIBLE);
        tvState.setText(message);
        btnRetry.setVisibility(showRetry ? View.VISIBLE : View.GONE);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void hideState() {
        stateBox.setVisibility(View.GONE);
    }

    private View divider(int insetDp) {
        View line = new View(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(1));
        params.setMargins(dp(insetDp), 0, dp(insetDp), 0);
        line.setLayoutParams(params);
        line.setBackgroundColor(getColor(R.color.divider));
        return line;
    }

    /** 天气现象 -> 帮助中提供的图标。 */
    private static int iconOf(String type) {
        String value = type == null ? "" : type;
        if (value.contains("雷")) {
            return R.drawable.thunder;
        }
        if (value.contains("雨")) {
            return R.drawable.rain;
        }
        if (value.contains("雪")) {
            return R.drawable.snow;
        }
        if (value.contains("雾") || value.contains("霾")) {
            return R.drawable.mcloud;
        }
        if (value.contains("晴")) {
            return value.contains("多云") ? R.drawable.cloud : R.drawable.sun;
        }
        if (value.contains("阴")) {
            return R.drawable.mcloud;
        }
        return R.drawable.cloud;
    }

    private int qualityColor(String quality) {
        if (quality == null) {
            return getColor(R.color.text_secondary);
        }
        if (quality.contains("优") || quality.contains("良")) {
            return getColor(R.color.good);
        }
        if (quality.contains("轻度")) {
            return getColor(R.color.warn);
        }
        return getColor(R.color.bad);
    }

    private static String orDash(String value) {
        return value == null || value.trim().isEmpty() ? "--" : value;
    }

    /** 接口的 high / low 自带“高温”“低温”前缀，先去掉再拼装，避免出现“高温 高温 15℃”。 */
    private static String degree(String value) {
        if (value == null) {
            return "--";
        }
        String text = value.replace("高温", "").replace("低温", "").trim();
        return text.isEmpty() ? "--" : text;
    }

    private static String formatValue(Double value) {
        if (value == null) {
            return "--";
        }
        double number = value;
        if (number == Math.rint(number)) {
            return String.valueOf((long) number);
        }
        return String.valueOf(number);
    }

    private static String formatTemp(String wendu) {
        if (wendu == null) {
            return "--";
        }
        try {
            return formatValue(Double.parseDouble(wendu.trim()));
        } catch (NumberFormatException e) {
            return wendu;
        }
    }

    private static String shortDate(String ymd) {
        if (ymd == null) {
            return "";
        }
        return ymd.length() >= 10 ? ymd.substring(5) : ymd;
    }

    private int dp(int value) {
        return Math.round(getResources().getDisplayMetrics().density * value);
    }
}
