package com.example.myinteriorapp.models;

public class ImageData {
    private String imageId;
    private String imageUrl;
    // 다른 필드들을 필요에 따라 추가할 수 있습니다.

    public ImageData(String imageId, String imageUrl) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}

