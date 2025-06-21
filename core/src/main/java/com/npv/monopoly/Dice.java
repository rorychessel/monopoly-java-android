package com.npv.monopoly;

public interface Dice {

    // Trả về số chấm xúc xắc
    int numDice();

    // Trả về số mặt của xúc xắc
    int sides();

    Roll roll();

    class Roll {
        public int val;
        public boolean is_double;
        public int dice1;
        public int dice2;
    }
}
