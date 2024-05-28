package ru.cinimex.codeStyle.example1.mocks;

import lombok.Data;
import ru.cinimex.codeStyle.example1.mocks.dto.MarketingDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class MarketClient {

    private Option option;

    public List<Marketing> getAllMarketingOptionsForOrder(Integer id){
        return new ArrayList<Marketing>(Arrays.asList(Marketing.builder().build()));
    }

    public List<OptionAvailability> checkOptionAvailabilityForClient(Long id){
        return new ArrayList<>(Arrays.asList(new OptionAvailability()));
    }

    public Option getOption(String str, Integer x){
        return new Option();
    }

    public Marketing saveOrUpdateMarketing(MarketingDto marketing){
        return Marketing.builder().build();
    }
}
