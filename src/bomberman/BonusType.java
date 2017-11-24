/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bomberman;

import java.util.Random;

/**
 *
 * @author fanda
 */
public enum BonusType {
    SPEED,
    BOMB,
    FLAME;
    
    private static final Random RANDOM = new Random();
    
    public static BonusType randomType()
    {
        
        return values()[RANDOM.nextInt(values().length)];
    }
}
