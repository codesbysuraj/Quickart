
package com.quickkart.entity;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Column;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Embeddable
public class OrderAddress {
    @Column(name = "full_address")
    @com.fasterxml.jackson.annotation.JsonProperty("fullAddress")
    private String fullAddress;
    @Column(name = "pincode")
    @com.fasterxml.jackson.annotation.JsonProperty("pincode")
    private String pincode;
    @Column(name = "phone")
    @com.fasterxml.jackson.annotation.JsonProperty("phone")
    private String phone;

    public OrderAddress() {}

    public OrderAddress(String fullAddress, String pincode, String phone) {
        this.fullAddress = fullAddress;
        this.pincode = pincode;
        this.phone = phone;
    }

    public String getFullAddress() { return fullAddress; }
    public void setFullAddress(String fullAddress) { this.fullAddress = fullAddress; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
