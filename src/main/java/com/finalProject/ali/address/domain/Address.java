package com.finalProject.ali.address.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class Address {
    private Long id;
    private String userId;
    private String rName;
    private String rPhone;
    private String zipcode;
    private String address1;
    private String address2;
    private Boolean isDefault;

}
