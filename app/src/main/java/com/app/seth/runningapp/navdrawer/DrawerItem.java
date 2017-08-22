package com.app.seth.runningapp.navdrawer;

public class DrawerItem {
    private int imageId;
    private String optionName;

    public DrawerItem(int imageId, String optionName) {
        this.imageId = imageId;
        this.optionName = optionName;
    }

    public int getImageId() {
        return imageId;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }
}
