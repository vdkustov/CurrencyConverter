package com.kustov.currencyconverter.simplexml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

/**
 * Created by Vladislav Kustov on 07.12.2017.
 */

@Root
public class ValCurs {

    @ElementList(entry = "Valute", inline = true, required = false)
    public List<Valute> valutes;

    @Attribute
    public String Date;

    @Attribute
    private String name;

}