/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2017 Samir Vaidya
 */

package com.openstego.desktop.plugin.lsb;

import com.openstego.desktop.OpenStegoException;

/**
 * Class to store error codes for LSB plugin
 */
public class LSBErrors {
    /**
     * Private constructor to prevent instantiation
     */
    private LSBErrors() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Error Code - Error while reading image data
     */
    public static final int ERR_IMAGE_DATA_READ = 1;

    /**
     * Error Code - Null value provided for image
     */
    public static final int NULL_IMAGE_ARGUMENT = 2;

    /**
     * Error Code - Image size insufficient for data
     */
    public static final int IMAGE_SIZE_INSUFFICIENT = 3;

    /**
     * Error Code - maxBitsUsedPerChannel is not a number
     */
    public static final int MAX_BITS_NOT_NUMBER = 4;

    /**
     * Error Code - maxBitsUsedPerChannel is not in valid range
     */
    public static final int MAX_BITS_NOT_IN_RANGE = 5;

    /**
     * Error Code - Invalid stego header data
     */
    public static final int INVALID_STEGO_HEADER = 6;

    /**
     * Error Code - Invalid image header version
     */
    public static final int INVALID_HEADER_VERSION = 7;

    /**
     * Static variable to keep track if we added error codes or not
     */
    private static boolean initialized = false;

    /**
     * Initialize the error code - message key map
     */
    public static void addErrorCodes() {
        if (!initialized) {
            OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, ERR_IMAGE_DATA_READ, "err.image.read");
            OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, NULL_IMAGE_ARGUMENT, "err.image.arg.nullValue");
            OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, IMAGE_SIZE_INSUFFICIENT, "err.image.insufficientSize");
            OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, MAX_BITS_NOT_NUMBER, "err.config.maxBitsUsedPerChannel.notNumber");
            OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, MAX_BITS_NOT_IN_RANGE, "err.config.maxBitsUsedPerChannel.notInRange");
            OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, INVALID_STEGO_HEADER, "err.invalidHeaderStamp");
            OpenStegoException.addErrorCode(LSBPlugin.NAMESPACE, INVALID_HEADER_VERSION, "err.invalidHeaderVersion");
        }
    }
}
