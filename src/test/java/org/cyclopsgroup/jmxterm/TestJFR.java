package org.cyclopsgroup.jmxterm;

public class TestJFR {
    public static void main(String[] args) throws InterruptedException {
        while(true) {
            System.out.println("Sleeping for 10 seconds.");
            Thread.sleep(1000 * 10);
        }
    }
}
