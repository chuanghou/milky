package com.stellariver.milky.common.base;

import java.util.ArrayList;
import java.util.HashMap;


class ListNode {
     int val;
     ListNode next;
     ListNode() {}
     ListNode(int val) { this.val = val; }
     ListNode(int val, ListNode next) { this.val = val; this.next = next; }
 }
class Solution {


    public static void main(String[] args) {
        Solution solution = new Solution();
        ListNode listNode1 = new ListNode();
        listNode1.val = 9;
        ListNode listNode2 = new ListNode();
        listNode2.val = 1;
        ListNode listNode3 = new ListNode();
        listNode3.val = 9;

        ListNode listNode4 = new ListNode();
        listNode4.val = 9;
        ListNode listNode5 = new ListNode();
        listNode5.val = 9;
        ListNode listNode6 = new ListNode();
        listNode6.val = 9;

        listNode2.next = listNode3;
        listNode3.next = listNode4;
        listNode4.next = listNode5;
        listNode5.next = listNode6;

        ListNode listNode = solution.addTwoNumbers(listNode1, listNode2);
        System.out.printf("");
    }

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        long i = transfer(l1) + transfer(l2);
        return transfer(i);
    }

    private long transfer(ListNode listNode) {
        long number = listNode.val;

        int index = 1;
        while (listNode.next != null) {
            listNode = listNode.next;
            number = (long) (number + listNode.val * Math.pow(10, index++));
        }
        return number;
    }

    private ListNode transfer(long number) {
        int base = 10;
        ListNode listNode = new ListNode();
        listNode.val = (int) (number%10);
        ListNode tempNode = listNode;
        while (true) {
            number = number/10;
            if (number == 0) {
                return listNode;
            }
            ListNode node = new ListNode();
            node.val = (int) (number%10);
            tempNode.next = node;
            tempNode = tempNode.next;
        }
    }

}