package com.zistone.blecontrol.pojo;

public class Materiel
{
    private int id;
    private String name;
    private int row;
    private int column;
    private String bindDeviceAddress;

    @Override
    public String toString()
    {
        return "Materiel{" + "id=" + id + ", name='" + name + '\'' + ", row=" + row + ", column=" + column + ", bindDeviceAddress='" + bindDeviceAddress + '\'' + '}';
    }

    public String getBindDeviceAddress()
    {
        return bindDeviceAddress;
    }

    public void setBindDeviceAddress(String bindDeviceAddress)
    {
        this.bindDeviceAddress = bindDeviceAddress;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getRow()
    {
        return row;
    }

    public void setRow(int row)
    {
        this.row = row;
    }

    public int getColumn()
    {
        return column;
    }

    public void setColumn(int column)
    {
        this.column = column;
    }
}
