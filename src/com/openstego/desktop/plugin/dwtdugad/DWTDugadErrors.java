/*
 * Steganography utility to hide messages into cover files
 * Author: Samir Vaidya (mailto:syvaidya@gmail.com)
 * Copyright (c) 2007-2017 Samir Vaidya
 */

package com.openstego.desktop.plugin.dwtdugad;

import com.openstego.desktop.OpenStegoException;

/**
 * Class to store error codes for DWT Xie plugin
 */
public class DWTDugadErrors {
    /**
     * Private constructor to prevent instantiation
     */
    private DWTDugadErrors() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Error Code - No cover file given
     */
    public static final int ERR_NO_COVER_FILE = 1;

    /**
     * Error Code - Invalid signature file provided
     */
    public static final int ERR_SIG_NOT_VALID = 2;

    /**
     * Static variable to keep track if we added error codes or not
     */
    private static boolean initialized = false;

    /**
     * Initialize the error code - message key map
     */
    public static void addErrorCodes() {
        if (!initialized) {
            OpenStegoException.addErrorCode(DWTDugadPlugin.NAMESPACE, ERR_NO_COVER_FILE, "err.cover.missing");
            OpenStegoException.addErrorCode(DWTDugadPlugin.NAMESPACE, ERR_SIG_NOT_VALID, "err.signature.invalid");
            initialized = true;
        }
    }
}
