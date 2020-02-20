package com.zistone.blecontrol.pojo;

import java.util.Date;

public class Material
{
    @Override
    public String toString()
    {
        return "Material{" +
                "id=" + id +
                ", materialName='" + materialName + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", depotRow=" + depotRow +
                ", depotColumn=" + depotColumn +
                ", deviceAddress='" + deviceAddress + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    /**
     * 自增主键(由数据库生成)
     */
    private int id;

    /**
     * 物料名称
     */
    private String materialName;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备地址
     */
    private String deviceAddress;

    /**
     * 库位(行)
     */
    private int depotRow;

    /**
     * 库位(列)
     */
    private int depotColumn;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 修改时间
     */
    private Date updateTime;

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getMaterialName()
    {
        return materialName;
    }

    public void setMaterialName(String materialName)
    {
        this.materialName = materialName;
    }

    public String getDeviceName()
    {
        return deviceName;
    }

    public void setDeviceName(String deviceName)
    {
        this.deviceName = deviceName;
    }

    public int getDepotRow()
    {
        return depotRow;
    }

    public void setDepotRow(int depotRow)
    {
        this.depotRow = depotRow;
    }

    public int getDepotColumn()
    {
        return depotColumn;
    }

    public void setDepotColumn(int depotColumn)
    {
        this.depotColumn = depotColumn;
    }

    public Date getCreateTime()
    {
        return createTime;
    }

    public void setCreateTime(Date createTime)
    {
        this.createTime = createTime;
    }

    public Date getUpdateTime()
    {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime)
    {
        this.updateTime = updateTime;
    }

    public String getDeviceAddress()
    {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress)
    {
        this.deviceAddress = deviceAddress;
    }

}
