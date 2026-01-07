package com.finalProject.ali.payment.mapper;

import com.finalProject.ali.payment.domain.Payment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentMapper {

    void insert(Payment payment);
}
