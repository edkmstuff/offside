package com.offsidegame.offside.models;

/**
 * Created by KFIR on 11/17/2016.
 */

public class GameInfo {

    @com.google.gson.annotations.SerializedName("GI")
    private String gameId;

    @com.google.gson.annotations.SerializedName("PGC")
    private String privateGameCode;

    @com.google.gson.annotations.SerializedName("PGT")
    private String privateGameTitle;

    @com.google.gson.annotations.SerializedName("HT")
    private String homeTeam;

    @com.google.gson.annotations.SerializedName("AT")
    private String awayTeam;

    @com.google.gson.annotations.SerializedName("OC")
    private int offsideCoins;

    @com.google.gson.annotations.SerializedName("MBS")
    private int minBetSize;

    @com.google.gson.annotations.SerializedName("T")
    private int trophies;

    @com.google.gson.annotations.SerializedName("MARVWPG")
    private int maxAllowedRewardVideosWatchPerGame;


    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public String getPrivateGameCode() {
        return privateGameCode;
    }

    public String getPrivateGameTitle() {
        return privateGameTitle;
    }

    public String getHomeTeam() {
        return homeTeam;
    }

    public String getAwayTeam() {
        return awayTeam;
    }

    public int getOffsideCoins() {
        return offsideCoins;
    }

    public int getMinBetSize() {
        return minBetSize;
    }

    public int getTrophies() {
        return trophies;
    }

    public void setTrophies(int trophies) {
        this.trophies = trophies;
    }

    public int getMaxAllowedRewardVideosWatchPerGame() {
        return maxAllowedRewardVideosWatchPerGame;
    }

    public void setMaxAllowedRewardVideosWatchPerGame(int maxAllowedRewardVideosWatchPerGame) {
        this.maxAllowedRewardVideosWatchPerGame = maxAllowedRewardVideosWatchPerGame;
    }
}

