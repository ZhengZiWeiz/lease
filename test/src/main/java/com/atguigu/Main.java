package com.atguigu;

import com.atguigu.test1.Color;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        String name = Color.getName(1);
        System.out.println(name);

        for (Color value : Color.values()) {
            if (value.getIndex()==1 ) {
                System.out.println(value);
            }
        }

        System.out.println(Color.RED.getIndex());

    }
}