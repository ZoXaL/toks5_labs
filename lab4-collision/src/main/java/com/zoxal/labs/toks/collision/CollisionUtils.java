package com.zoxal.labs.toks.collision;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Insert description vere
 *
 * @author Mike
 * @version 11/20/2017
 */
public class CollisionUtils {
    private static final Logger log = LoggerFactory.getLogger(CollisionUtils.class);

    public static final byte JAM_SIGNAL = 12;
    private static final long COLLISION_FRAME_MILIS = 1000;
    private static final AtomicBoolean collisionLocker = new AtomicBoolean(false);

    private CollisionUtils() {

    }

    public static boolean isChannelFree() {
        return (new Date().getTime()/1000) % 3 != 0;
    }

    public static long generateSlotsCount(int numberOfRetires) {
        return (long)(Math.random()*(Math.pow(2, numberOfRetires)));
    }

    public static boolean checkCollision() {
        return (new Date().getTime()/1000) % 2 == 1;
    }

    /**
     * @return true if there was a collision
     */
    public static boolean waitCollisionFrame() {
        try {
            synchronized (collisionLocker) {
                collisionLocker.wait(COLLISION_FRAME_MILIS);
            }
        } catch (InterruptedException e) {
            log.warn("Unexpected exception during waiting for collision frame");
        }
        boolean collisionDetected = collisionLocker.get();
        collisionLocker.set(false);
        return collisionDetected;
    }

    public static void notifyCollision() {
        collisionLocker.set(true);
        synchronized (collisionLocker) {
            collisionLocker.notify();
        }
    }
}
