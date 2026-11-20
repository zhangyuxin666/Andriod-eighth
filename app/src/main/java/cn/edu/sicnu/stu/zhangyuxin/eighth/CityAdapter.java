package cn.edu.sicnu.stu.zhangyuxin.eighth;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.edu.sicnu.stu.zhangyuxin.eighth.model.City;

/**
 * 城市列表适配器：把分组结果摊平成“省级标题 + 城市行”两种视图，
 * 关键字过滤时重新摊平，标题行不可点击。
 */
public class CityAdapter extends BaseAdapter {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_CITY = 1;

    private final LayoutInflater inflater;
    private final List<Row> rows = new ArrayList<>();
    private List<CityRepository.CityGroup> groups = new ArrayList<>();
    private String keyword = "";

    public CityAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    public void setGroups(List<CityRepository.CityGroup> groups) {
        this.groups = groups == null ? new ArrayList<>() : groups;
        filter(keyword);
    }

    public void filter(String keyword) {
        this.keyword = keyword == null ? "" : keyword.trim();
        rows.clear();
        for (CityRepository.CityGroup group : groups) {
            List<City> matched = new ArrayList<>();
            for (City city : group.cities) {
                if (this.keyword.isEmpty()
                        || city.getCityName().contains(this.keyword)
                        || city.getCityCode().contains(this.keyword)) {
                    matched.add(city);
                }
            }
            if (matched.isEmpty()) {
                continue;
            }
            rows.add(Row.header(group.province + " · " + matched.size() + " 个城市"));
            for (City city : matched) {
                rows.add(Row.city(city));
            }
        }
        notifyDataSetChanged();
    }

    /** 标题行返回 null，点击事件里据此过滤。 */
    public City getCity(int position) {
        return rows.get(position).city;
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Row getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return rows.get(position).isHeader() ? TYPE_HEADER : TYPE_CITY;
    }

    @Override
    public boolean isEnabled(int position) {
        return !rows.get(position).isHeader();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Row row = rows.get(position);
        if (row.isHeader()) {
            TextView header = convertView != null
                    ? (TextView) convertView
                    : (TextView) inflater.inflate(R.layout.item_city_header, parent, false);
            header.setText(row.header);
            return header;
        }
        View view = convertView != null
                ? convertView
                : inflater.inflate(R.layout.item_city, parent, false);
        City city = row.city;
        ((TextView) view.findViewById(R.id.tv_city_name)).setText(city.getCityName());
        ((TextView) view.findViewById(R.id.tv_city_code)).setText("城市代码 " + city.getCityCode());
        return view;
    }

    static final class Row {
        final String header;
        final City city;

        private Row(String header, City city) {
            this.header = header;
            this.city = city;
        }

        static Row header(String text) {
            return new Row(text, null);
        }

        static Row city(City city) {
            return new Row(null, city);
        }

        boolean isHeader() {
            return city == null;
        }
    }
}
