package com.udacity.ImageService.service;

import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * Service that tries to guess if an image displays a cat.
 */
public class FakeImageService implements ImageService {
    private final Random r;

    public FakeImageService(){
        this.r = new Random();
    }

    @Override
    public boolean imageContainsCat(BufferedImage image, float confidenceThreshhold) {
        return r.nextBoolean();
    }
}
