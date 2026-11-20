package cn.edu.sicnu.stu.zhangyuxin.eighth;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.edu.sicnu.stu.zhangyuxin.eighth.model.City;

/**
 * 把 citycode.json 解析成“省 / 直辖市 -> 城市列表”的分组结构。
 * citycode.json 里 pid 可能指向省级节点，也可能指向中间层城市，
 * 所以向上回溯 pid 直到 pid == 0，才能拿到真正的省级分组名。
 */
public final class CityRepository {

    public static final String OTHER_PROVINCE = "其他";

    private final List<CityGroup> groups;
    private final int cityCount;

    private CityRepository(List<CityGroup> groups) {
        this.groups = groups;
        int count = 0;
        for (CityGroup group : groups) {
            count += group.cities.size();
        }
        this.cityCount = count;
    }

    public List<CityGroup> getGroups() {
        return groups;
    }

    public int getCityCount() {
        return cityCount;
    }

    public int getGroupCount() {
        return groups.size();
    }

    public static CityRepository parse(String json) {
        Type listType = new TypeToken<List<City>>() {
        }.getType();
        List<City> cities = new Gson().fromJson(json, listType);
        if (cities == null || cities.isEmpty()) {
            throw new IllegalStateException("citycode.json 解析结果为空");
        }

        Map<Integer, City> byId = new HashMap<>(cities.size() * 2);
        for (City city : cities) {
            byId.put(city.getId(), city);
        }

        Map<String, CityGroup> grouped = new LinkedHashMap<>();
        List<City> orphans = new ArrayList<>();
        for (City city : cities) {
            if (!city.hasCode()) {
                continue;
            }
            String province = resolveProvince(city, byId);
            if (province == null) {
                orphans.add(city);
                continue;
            }
            CityGroup group = grouped.get(province);
            if (group == null) {
                group = new CityGroup(province);
                grouped.put(province, group);
            }
            group.cities.add(city);
        }

        List<CityGroup> groups = new ArrayList<>(grouped.values());
        final Collator collator = Collator.getInstance(Locale.CHINA);
        Collections.sort(groups, new Comparator<CityGroup>() {
            @Override
            public int compare(CityGroup left, CityGroup right) {
                return collator.compare(left.province, right.province);
            }
        });
        for (CityGroup group : groups) {
            Collections.sort(group.cities, new Comparator<City>() {
                @Override
                public int compare(City left, City right) {
                    return collator.compare(left.getCityName(), right.getCityName());
                }
            });
        }
        if (!orphans.isEmpty()) {
            CityGroup other = new CityGroup(OTHER_PROVINCE);
            other.cities.addAll(orphans);
            Collections.sort(other.cities, new Comparator<City>() {
                @Override
                public int compare(City left, City right) {
                    return collator.compare(left.getCityName(), right.getCityName());
                }
            });
            groups.add(other);
        }
        return new CityRepository(groups);
    }

    /** 沿 pid 向上找到 pid == 0 的节点，就是省 / 直辖市；数据有环时返回 null。 */
    private static String resolveProvince(City city, Map<Integer, City> byId) {
        Set<Integer> visited = new HashSet<>();
        City cursor = city;
        while (cursor != null && visited.add(cursor.getId())) {
            if (cursor.getPid() == 0) {
                return cursor.getCityName();
            }
            cursor = byId.get(cursor.getPid());
        }
        return null;
    }

    public static final class CityGroup {
        public final String province;
        public final List<City> cities = new ArrayList<>();

        CityGroup(String province) {
            this.province = province;
        }
    }
}
