package ru.cinimex.codeStyle.example1.mocks;

import ru.cinimex.codeStyle.example1.mocks.dto.MarketingDto;

public class MarketingMapper {

    public MarketingDto toDto(Marketing marketing){
        return new MarketingDto();
    }
}
