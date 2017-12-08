package com.kustov.currencyconverter.simplexml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;

/**
 * Created by Vladislav Kustov on 07.12.2017.
 */

@Element
public class Valute {

    @Element(name = "Value", required = false)
    public String value;

    @Element(name = "Name", required = false)
    public String name;

    @Element(name = "Nominal", required = false)
    public String nominal;

    @Element(name = "NumCode", required = false)
    public String numCode;

    @Element(name = "CharCode", required = false)
    public String charCode;

    @Attribute
    private String ID;

}