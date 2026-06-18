package ASLENIX.pharmacy.demo.utils;


import java.util.Locale;

import com.ibm.icu.text.RuleBasedNumberFormat;

public class LongNumberToStringWord {

    private final Locale southAsianLocale = Locale.of("en", "IN");

    public String convertLongToNeplaiString(Double number){

        RuleBasedNumberFormat formatter = new RuleBasedNumberFormat(
                southAsianLocale,
                RuleBasedNumberFormat.SPELLOUT
        );

        return formatter.format(number);
    }

}
