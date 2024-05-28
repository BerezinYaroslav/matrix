package ru.cinimex.codeStyle.example1.mocks;

import lombok.Data;

@Data
public class OptionAvailability {

    public String getIsSixAvailable(){
        return "";
    }

    public boolean getIsShiftAvailable(){
        return false;
    }
}
