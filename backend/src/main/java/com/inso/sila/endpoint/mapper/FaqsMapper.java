package com.inso.sila.endpoint.mapper;

import com.inso.sila.endpoint.dto.studio.faqs.FaqsDto;
import com.inso.sila.entity.Faqs;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FaqsMapper {

    FaqsDto faqsToFaqsDto(Faqs faqs);

    List<FaqsDto> faqsToFaqsDto(List<Faqs> faqs);

    Faqs faqsDtoToFaqs(FaqsDto faqsDto);

    List<Faqs> faqsDtoToFaqs(List<FaqsDto> faqsDtoList);


}
