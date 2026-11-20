package cn.edu.sicnu.stu.zhangyuxin.eighth.model;

import com.google.gson.annotations.SerializedName;

/**
 * citycode.json 中的一条城市记录。
 * 文件里的字段是下划线风格，这里用 Gson 的 @SerializedName 做映射。
 */
public class City {

    private int id;
    private int pid;

    @SerializedName("city_code")
    private String cityCode;

    @SerializedName("city_name")
    private String cityName;

    @SerializedName("post_code")
    private String postCode;

    @SerializedName("area_code")
    private String areaCode;

    private String ctime;

    public int getId() {
        return id;
    }

    public int getPid() {
        return pid;
    }

    public String getCityCode() {
        return cityCode;
    }

    public String getCityName() {
        return cityName;
    }

    public String getPostCode() {
        return postCode;
    }

    public String getAreaCode() {
        return areaCode;
    }

    public String getCtime() {
        return ctime;
    }

    /** 省级节点只有子节点编号，没有城市代码，不能查询天气。 */
    public boolean hasCode() {
        return cityCode != null && !cityCode.trim().isEmpty();
    }
}
