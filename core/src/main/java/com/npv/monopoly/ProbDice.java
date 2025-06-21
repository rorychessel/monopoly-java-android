package com.npv.monopoly;

import java.util.Random;

public class ProbDice implements Dice {
    private final int N; // số lượng xúc xắc
    private final int SIDES; // số lượng mặt của xúc xắc
    private final Random rand;
    private int lastDice1, lastDice2;

    public ProbDice() {
        N = 2;
        SIDES = 6;
        rand = new Random();
    }

    public int numDice() {
        return N;
    }

    public int sides() {
        return SIDES;
    }

    public Roll roll() {
        Roll roll = new Roll();

        lastDice1 = rand.nextInt(SIDES) + 1;

        lastDice2 = rand.nextInt(SIDES) + 1;
        roll.dice1 = lastDice1;
        roll.dice2 = lastDice2;

        if (lastDice1 == lastDice2) {
            roll.is_double = true;
        } else {
            roll.is_double = false;
        }

        roll.val = lastDice1 + lastDice2;

        return roll;
    }

    public int getLastDice1() {
        return lastDice1;
    }

    public int getLastDice2() {
        return lastDice2;
    }
}
