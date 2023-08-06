package io.github.gaming32.lifesteal;

public class MyMath {
    public static int mul(int a, int b) {
        if (a < b) {
            final int tmp = a;
            a = b;
            b = tmp;
        }
        int sum = 0;
        while (b != 0) {
            sum += (b & 1) != 0 ? a : 0;
            a <<= 1;
            b >>>= 1;
        }
        return sum;
    }
}
