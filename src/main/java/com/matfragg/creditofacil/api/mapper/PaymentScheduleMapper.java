package com.matfragg.creditofacil.api.mapper;

import com.matfragg.creditofacil.api.dto.response.PaymentScheduleResponse;
import com.matfragg.creditofacil.api.model.entities.PaymentSchedule;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PaymentScheduleMapper {

    PaymentScheduleResponse toResponse(PaymentSchedule paymentSchedule);

    List<PaymentScheduleResponse> toResponseList(List<PaymentSchedule> paymentSchedules);
}
