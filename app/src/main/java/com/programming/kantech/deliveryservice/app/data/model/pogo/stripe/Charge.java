package com.programming.kantech.deliveryservice.app.data.model.pogo.stripe;

/**
 * Created by patri on 2017-09-27.
 */

public class Charge {

    private String status;
    private Outcome outcome;

    public Charge() {
    }

    public Outcome getOutcome() {
        return outcome;
    }

    public void setOutcome(Outcome outcome) {
        this.outcome = outcome;
    }

    public Charge(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "Charge{" +
                "status='" + status + '\'' +
                '}';
    }
}
