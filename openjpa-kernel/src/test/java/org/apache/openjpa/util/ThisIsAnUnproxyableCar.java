package org.apache.openjpa.util;

import java.util.Random;

/**
 * this is NOT a proxyable class... but it's a nice car though!
 */
public class ThisIsAnUnproxyableCar {
    private final Random random = new Random(System.currentTimeMillis());
    private String brand;
    private String model;

    public ThisIsAnUnproxyableCar(String brand, String model) {
        this.brand = brand;
        this.model = model;
    }

    public void broomBroom() {
        if(random.nextInt()%2 == 0){
            System.out.println("Get ready to turn heads! This " + brand + " " + model + " is a showstopper on wheels!\n");
        }else{
            System.out.println("Rev up those engines! This " + brand + " " + model + " is ready to roar!\n");
        }
    }
}