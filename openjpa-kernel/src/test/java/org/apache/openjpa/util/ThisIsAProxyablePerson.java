package org.apache.openjpa.util;

import java.util.Random;

/**
 * this person may not have a great sense of humor ... but is proxyable for sure though!
 */
public class ThisIsAProxyablePerson {
    private final Random random = new Random(System.currentTimeMillis());
    private String name;
    private int age;

    public ThisIsAProxyablePerson() {
        // Public no-arg constructor to respect the rules
        if(random.nextInt()%2 == 0){
            this.name = "Kevin";
            this.age = 23;
        }else{
            this.name = "Karen";
            this.age = 32;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void sayHello(){
        System.out.println("Hello, This is " + name + " and I'm " + age + " years old...");
        if(random.nextInt()%2 == 0){
            System.out.println("I fail to understand the appeal of Cars.\n");
        }else{
            System.out.println("Cars are simply machines designed for transportation.\n");
        }
    }
}
