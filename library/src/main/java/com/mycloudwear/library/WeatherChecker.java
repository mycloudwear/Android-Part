package com.mycloudwear.library;


import static java.lang.Thread.sleep;

/**
 * @author Group 4
 * @version 1.0.1
 * @since 14/5/2019
 * This class could is used to achieve from one intent to another intent.
 */
public class WeatherChecker {

    public String language;
    public String weather;
    public String windDirection;
    public String locate;

    /**
     * This function is used to get the current location.
     * @param language the current interface language.
     * @param location the current location.
     */
    public void locationChecker(String language, String location){
        if (language.equals("English")){

            MyThread thread = new MyThread(location);
            thread.start();
            while (thread.result == null ) {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            locate = thread.result;
        } else {
            locate = location;
        }
    }

    /**
     * This function is used to get the corresponding language of the weather information.
     * @param language the current interface language.
     * @param weather the given weather.
     * @param windDirection the given wind direction.
     */
    public void weatherChecker(String language, String weather, String windDirection){
        if (language.equals("English")){
            switch (weather){
                case "晴":
                    this.weather = "Clear";
                    break;
                case "多云":
                    this.weather = "Partly cloudy";
                    break;
                case "阴":
                    this.weather = "Yin";
                    break;
                case "阵雨":
                    this.weather = "Shower";
                    break;
                case "雷阵雨":
                    this.weather = "Thunder shower";
                    break;
                case "雷阵雨并伴有冰雹":
                    this.weather = "Thunderstorms accompanied by hail";
                    break;
                case "雨夹雪":
                    this.weather = "Sleet";
                    break;
                case "小雨":
                    this.weather = "Light rain";
                    break;
                case "中雨":
                    this.weather = "Rain";
                    break;
                case "大雨":
                    this.weather = "heavy rain";
                    break;
                case "暴雨":
                    this.weather = "Rainstorm";
                    break;
                case "大暴雨":
                    this.weather = "Heavy rain";
                    break;
                case "特大暴雨":
                    this.weather = "Extraordinary heavy rain";
                    break;
                case "阵雪":
                    this.weather = "Snow shower";
                    break;
                case "小雪":
                    this.weather = "Light snow";
                    break;
                case "中雪":
                    this.weather = "Medium snow";
                    break;
                case "大雪":
                    this.weather = "Heavy snow";
                    break;
                case "暴雪":
                    this.weather = "Blizzard";
                    break;
                case "雾":
                    this.weather = "Fog";
                    break;
                case "冻雨":
                    this.weather = "Frozen rain";
                    break;
                case "沙尘暴":
                    this.weather = "Sandstorm";
                    break;
                case "小雨-中雨":
                    this.weather = "Light rain - moderate rain";
                    break;
                case "中雨-大雨":
                    this.weather = "Medium rain - heavy rain";
                    break;
                case "大雨-暴雨":
                    this.weather = "Heavy rain - heavy rain";
                    break;
                case "暴雨-大暴雨":
                    this.weather = "Heavy rain - heavy rain";
                    break;
                case "大暴雨-特大暴雨":
                    this.weather = "Heavy rain - heavy rain";
                    break;
                case "小雪-中雪":
                    this.weather = "Little snow - medium snow";
                    break;
                case "中雪-大雪":
                    this.weather = "Medium snow - heavy snow";
                    break;
                case "大雪-暴雪":
                    this.weather = "Heavy snow - blizzard";
                    break;
                case "浮尘":
                    this.weather = "Floating dust";
                    break;
                case "扬沙":
                    this.weather = "Yangsha";
                    break;
                case "强沙尘暴":
                    this.weather = "Strong sandstorm";
                    break;
                case "飑":
                    this.weather = "Squall";
                    break;
                case "龙卷风":
                    this.weather = "Tornado";
                    break;
                case "弱高吹雪":
                    this.weather = "Weak snowstorm";
                    break;
                case "轻霾":
                    this.weather = "Frivolous";
                    break;
                case "霾":
                    this.weather = "Haze";
                    break;
            }

            switch (windDirection){
                case "无风向":
                    this.windDirection = "No wind direction";
                    break;
                case "东北":
                    this.windDirection = "Northeast";
                    break;
                case "东":
                    this.windDirection = "East";
                    break;
                case "东南":
                    this.windDirection = "Southeast";
                    break;
                case "南":
                    this.windDirection = "South";
                    break;
                case "西南":
                    this.windDirection = "Southwest";
                    break;
                case "西":
                    this.windDirection = "West";
                    break;
                case "西北":
                    this.windDirection = "Northwest";
                    break;
                case "北":
                    this.windDirection = "North";
                    break;
                case "旋转不定":
                    this.windDirection = "Rotation";
                    break;

            }

        } else {
            this.weather = weather;
            this.windDirection = windDirection;
        }

    }
}
