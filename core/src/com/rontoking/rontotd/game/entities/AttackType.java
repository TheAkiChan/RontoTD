package com.rontoking.rontotd.game.entities;

public class AttackType {
    public String name;
    public float[] multipliers;

    public static AttackType[] attackTypes;

    public static void load(String[] attackTypeParts){
        attackTypes = new AttackType[attackTypeParts.length];
        String[] attackType;
        for(int i = 0; i < attackTypeParts.length; i++){
            attackType = attackTypeParts[i].split(",");
            attackTypes[i] = new AttackType(attackType[0].trim());
            for(int m = 1; m < attackType.length; m++){
                attackTypes[i].multipliers[m - 1] = Float.parseFloat(attackType[m].trim());
            }
        }
    }

    public AttackType(){

    }

    public AttackType(AttackType attackType){
        this.name = attackType.name;
        this.multipliers = new float[attackType.multipliers.length];
        for(int i = 0; i < this.multipliers.length; i++){
            this.multipliers[i] = attackType.multipliers[i];
        }
    }

    public AttackType(String name){
        this.name = name;
        this.multipliers = new float[Enemy.armorTypes.length];
        for(int i = 0; i < this.multipliers.length; i++){
            this.multipliers[i] = 1;
        }
    }
}
