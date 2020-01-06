package com.zistone.bluetoothcontrol.util;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableHelper
{
    public static <T> T Copy(Parcelable input)
    {
        Parcel parcel = null;
        try
        {
            parcel = Parcel.obtain();
            parcel.writeParcelable(input, 0);
            parcel.setDataPosition(0);
            ClassLoader classLoader = input.getClass().getClassLoader();
            return parcel.readParcelable(classLoader);
        }
        finally
        {
            parcel.recycle();
        }
    }
}
