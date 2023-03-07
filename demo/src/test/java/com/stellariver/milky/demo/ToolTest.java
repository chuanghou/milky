package com.stellariver.milky.demo;


public class ToolTest {

    interface Subject {

        void print();


    }

    static class SubjectImpl implements Subject {

        @Override
        public void print() {
            System.out.println("hello");
        }

    }

    public static void main(String[] args) {

    }


}