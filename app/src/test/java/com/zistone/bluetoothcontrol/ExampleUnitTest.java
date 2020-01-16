package com.zistone.bluetoothcontrol;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest
{
    List<String> _list = new ArrayList<String>()
    {
        {
            this.add("aaa");
        }

        {
            this.add("bbb");
        }

        {
            this.add("ccc");
        }
    };

    @Test
    public void TestIterator()
    {
        System.out.println("=======TestIterator===========");
        Iterator<String> iterator = _list.iterator();
        while(iterator.hasNext())
        {
            String temp = iterator.next();
            if(temp.equals("aaa"))
            {
                iterator.remove();
            }
        }
        _list.add("ddd");
        for(String temp : _list)
        {
            System.out.println(temp);
        }
    }

    @Test
    public void TestList()
    {
        System.out.println("========TestList==========");
        List<String> list = _list;
        list.remove(0);
        for(String temp : list)
        {
            System.out.println(temp);
        }
        System.out.println("-------------------------");
        for(String temp : _list)
        {
            System.out.println(temp);
        }
    }
}