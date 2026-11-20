package cn.edu.sicnu.stu.zhangyuxin.eighth.model;

import java.util.List;

/**
 * 天气接口 http://t.weather.itboy.net/api/weather/city/{city_code} 的返回结构，
 * 用 Gson 一次性反序列化为 Java 对象。
 */
public class Weather {

    private CityInfo cityInfo;
    private Data data;
    private Object status;
    private String message;

    public CityInfo getCityInfo() {
        return cityInfo;
    }

    public Data getData() {
        return data;
    }

    public String getMessage() {
        return message;
    }

    /** 接口成功时 status 为 200，字符串和数字两种写法都能兼容。 */
    public boolean isOk() {
        return status != null && String.valueOf(status).startsWith("200");
    }

    public static class CityInfo {
        private String city;
        private String citykey;
        private String parent;
        private String updateTime;

        public String getCity() {
            return city;
        }

        public String getCitykey() {
            return citykey;
        }

        public String getParent() {
            return parent;
        }

        public String getUpdateTime() {
            return updateTime;
        }
    }

    public static class Data {
        private String shidu;
        private Double pm25;
        private Double pm10;
        private String wendu;
        private String ganmao;
        private String quality;
        private Forecast yesterday;
        private List<Forecast> forecast;

        public String getShidu() {
            return shidu;
        }

        public Double getPm25() {
            return pm25;
        }

        public Double getPm10() {
            return pm10;
        }

        public String getWendu() {
            return wendu;
        }

        public String getGanmao() {
            return ganmao;
        }

        public String getQuality() {
            return quality;
        }

        public Forecast getYesterday() {
            return yesterday;
        }

        public List<Forecast> getForecast() {
            return forecast;
        }
    }

    public static class Forecast {
        private String date;
        private String high;
        private String low;
        private String ymd;
        private String week;
        private String sunrise;
        private String sunset;
        private String aqi;
        private String fx;
        private String fl;
        private String type;
        private String notice;

        public String getDate() {
            return date;
        }

        public String getHigh() {
            return high;
        }

        public String getLow() {
            return low;
        }

        public String getYmd() {
            return ymd;
        }

        public String getWeek() {
            return week;
        }

        public String getSunrise() {
            return sunrise;
        }

        public String getSunset() {
            return sunset;
        }

        public String getAqi() {
            return aqi;
        }

        public String getFx() {
            return fx;
        }

        public String getFl() {
            return fl;
        }

        public String getType() {
            return type;
        }

        public String getNotice() {
            return notice;
        }
    }
}
