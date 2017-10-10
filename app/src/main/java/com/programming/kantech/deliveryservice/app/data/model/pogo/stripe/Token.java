package com.programming.kantech.deliveryservice.app.data.model.pogo.stripe;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by patri on 2017-09-26.
 */

public class Token {

    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("card")
    @Expose
    private Card card;

    @SerializedName("client_ip")
    @Expose
    private Object clientIp;

    @SerializedName("created")
    @Expose
    private Integer created;

    @SerializedName("livemode")
    @Expose
    private Boolean livemode;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("used")
    @Expose
    private Boolean used;

    public Token() {
    }

    public Token(String id, Card card, Object clientIp, Integer created, Boolean livemode, String type, Boolean used) {
        this.id = id;
        this.card = card;
        this.clientIp = clientIp;
        this.created = created;
        this.livemode = livemode;
        this.type = type;
        this.used = used;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Card getCard() {
        return card;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public Object getClientIp() {
        return clientIp;
    }

    public void setClientIp(Object clientIp) {
        this.clientIp = clientIp;
    }

    public Integer getCreated() {
        return created;
    }

    public void setCreated(Integer created) {
        this.created = created;
    }

    public Boolean getLivemode() {
        return livemode;
    }

    public void setLivemode(Boolean livemode) {
        this.livemode = livemode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "Token{" +
                "id='" + id + '\'' +
                ", card=" + card +
                ", clientIp=" + clientIp +
                ", created=" + created +
                ", livemode=" + livemode +
                ", type='" + type + '\'' +
                ", used=" + used +
                '}';
    }
}
