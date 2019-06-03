package com.mycloudwear.bean;

/**
 * @author kongkongdaren
 * @version 1.0.1
 * @since 15/5/2019
 * Created by Android on 26/6/2018
 * The original code was provided by kongkongdaren (https://github.com/kongkongdaren), but in our app
 * we only use part of his code to achieve our function.
 * The FolderBean class is a Java class which with the properties of the folder, these folders
 * contains every images which come from the users. This class contains the get/set construction
 * of folders.
 */
public class FolderBean {
    private String dir;//This string is the path of the current folder.
    private String firstImamgPath;//This string is the path of the first image which is in the folder.
    private String name;//The string is the name of the folder
    private int count; //This integer is the amount of the images in a current folder.

    //The method get the path of the folder.
    public String getDir() {
        return dir;
    }

    //This method sets the path of the folder.
    public void setDir(String dir) {
        this.dir = dir;
        int indexOf = this.dir.lastIndexOf("/")+1;
        this.name=this.dir.substring(indexOf);
    }

    //The method gets the path of the first image which is in the specified folder.
    public String getFirstImamgPath() {
        return firstImamgPath;
    }

    //The method sets the path of the first image which is in the specified folder.
    public void setFirstImamgPath(String firstImamgPath) {
        this.firstImamgPath = firstImamgPath;
    }

    //The method gets the name of a specified folder.
    public String getName() {
        return name;
    }

    //The method sets the name of a specified folder.
    public void setName(String name) {
        this.name = name;
    }

    //The method gets the amount of the images in a current folder.
    public int getCount() {
        return count;
    }

    //The method sets the amount of the images in a current folder.
    public void setCount(int count) {
        this.count = count;
    }
}
