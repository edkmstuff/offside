package com.offsidegame.offside.models;

/**
 * Created by KFIR on 12/7/2016.
 */
public class Score {
    @com.google.gson.annotations.SerializedName("P")
    private int position;

    @com.google.gson.annotations.SerializedName("N")
    private String name;

    @com.google.gson.annotations.SerializedName("PI")
    private String playerId;

    @com.google.gson.annotations.SerializedName("IU")
    private String imageUrl;

    @com.google.gson.annotations.SerializedName("OC")
    private int offsideCoins;

    @com.google.gson.annotations.SerializedName("PGC")
    private String privateGameCode;

    @com.google.gson.annotations.SerializedName("SDI")
    private ScoreDetailedInfo ScoreDetailedInfo;

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getOffsideCoins() {
        return offsideCoins;
    }

    public void setOffsideCoins(int offsideCoins) {
        this.offsideCoins = offsideCoins;
    }

    public String getPrivateGameCode() {
        return privateGameCode;
    }

    public void setPrivateGameCode(String privateGameCode) {
        this.privateGameCode = privateGameCode;
    }

    public com.offsidegame.offside.models.ScoreDetailedInfo getScoreDetailedInfo() {
        return ScoreDetailedInfo;
    }

    public void setScoreDetailedInfo(com.offsidegame.offside.models.ScoreDetailedInfo scoreDetailedInfo) {
        ScoreDetailedInfo = scoreDetailedInfo;
    }

    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }
}
